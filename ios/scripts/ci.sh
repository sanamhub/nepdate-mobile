#!/usr/bin/env bash
# Runs the iOS gates of ADR-0008 in order, stops at the first failure and ends with one summary
# line. CI calls this from ios/ on a macOS runner (ADR-0010).
set -uo pipefail
cd "$(dirname "$0")/.."

XCODEGEN_VERSION="2.46.0"
XCODEGEN_SHA256="4d9e34b62172d645eed6457cac13fc222569974098ef4ee9c3368bedf0196806"
XCODEGEN_URL="https://github.com/yonaskolb/XcodeGen/releases/download/${XCODEGEN_VERSION}/xcodegen.zip"
SIMULATOR="platform=iOS Simulator,name=iPhone 17,OS=latest"
DERIVED_DATA="build/DerivedData"
PROJECT="NepDate.xcodeproj"
SNAPSHOT_COUNT=6
SNAPSHOT_REFERENCE_COUNT=27
SNAPSHOT_RECORD="${SNAPSHOT_TESTING_RECORD:-never}"
COVERAGE_MIN=80

run() {
  echo "+ $*"
  "$@"
}

gate_xcodegen() {
  local root="${RUNNER_TEMP:-${TMPDIR:-/tmp}}/xcodegen-${XCODEGEN_VERSION}"
  local zip="$root/xcodegen.zip"
  rm -rf "$root"
  mkdir -p "$root"
  run curl -fsSL "$XCODEGEN_URL" -o "$zip"
  local actual
  actual="$(shasum -a 256 "$zip" | awk '{ print $1 }')"
  if [ "$actual" != "$XCODEGEN_SHA256" ]; then
    echo "xcodegen: SHA-256 mismatch, expected ${XCODEGEN_SHA256}, got ${actual}"
    return 1
  fi
  run unzip -q "$zip" -d "$root"
  export PATH="$root/xcodegen/bin:$PATH"
  run xcodegen --version
  run xcodegen generate
}

gate_format() {
  run swift format lint --strict --recursive \
    NepDateApp \
    NepDateWidgets \
    NepDateTests \
    NepDateUITests \
    Packages/CalendarKit/Sources \
    Packages/CalendarUI/Sources || return 1

  local package_revision project_revision ui_revision library_version
  package_revision="$(sed -n 's/.*revision: "\([0-9a-f]*\)".*/\1/p' \
    Packages/CalendarKit/Package.swift)"
  ui_revision="$(sed -n 's/.*revision: "\([0-9a-f]*\)".*/\1/p' \
    Packages/CalendarUI/Package.swift)"
  project_revision="$(sed -n 's/.*revision: "\([0-9a-f]*\)".*/\1/p' project.yml)"
  library_version="$(sed -n 's/.*NEPDATE_LIBRARY_VERSION: "\([^"]*\)".*/\1/p' project.yml)"
  if [ -z "$project_revision" ] || [ "$package_revision" != "$project_revision" ]; then
    echo "revision: project.yml (${project_revision}) and CalendarKit/Package.swift (${package_revision}) disagree"
    return 1
  fi
  if [ -z "$ui_revision" ] || [ "$package_revision" != "$ui_revision" ]; then
    echo "revision: CalendarUI/Package.swift (${ui_revision}) and CalendarKit/Package.swift (${package_revision}) disagree"
    return 1
  fi
  if [ -z "$library_version" ] || [ "$library_version" != "$package_revision" ]; then
    echo "library: NepDateLibraryVersion (${library_version}) and the pin (${package_revision}) disagree"
    return 1
  fi
  echo "revision: nepdate-swift ${project_revision}"
  echo "library: NepDateLibraryVersion ${library_version}"

  local missing
  missing="$(jq -r '
    .strings | to_entries[]
    | select((.value.localizations.ne.stringUnit.value // "") == "")
    | .key
  ' Resources/Localizable.xcstrings)"
  if [ -n "$missing" ]; then
    echo "localisation: no translated ne value for: ${missing}"
    return 1
  fi
  echo "localisation: every key has a translated ne value"
}

gate_build() {
  run xcodebuild build \
    -project "$PROJECT" \
    -scheme NepDate \
    -configuration Release \
    -destination "generic/platform=iOS" \
    -derivedDataPath "$DERIVED_DATA" \
    -quiet \
    CODE_SIGNING_ALLOWED=NO || return 1

  local app="$DERIVED_DATA/Build/Products/Release-iphoneos/NepDate.app"
  local appex="$app/PlugIns/NepDateWidgets.appex"
  if [ ! -d "$app" ] || [ ! -d "$appex" ]; then
    echo "build: NepDate.app or NepDateWidgets.appex is missing after the build"
    return 1
  fi
  echo "size: NepDate.app $(du -sk "$app" | awk '{ print $1 }') KB"
  echo "size: NepDate executable $(stat -f%z "$app/NepDate") bytes"
  echo "size: NepDateWidgets.appex $(du -sk "$appex" | awk '{ print $1 }') KB"
  echo "size: NepDateWidgets executable $(stat -f%z "$appex/NepDateWidgets") bytes"
}

gate_test() {
  rm -rf "build/TestResults.xcresult" "build/snapshots"
  echo "test: snapshot record mode ${SNAPSHOT_RECORD}"
  # xcodebuild passes a variable into the test process on the simulator only with the
  # TEST_RUNNER_ prefix, which it strips; without it the library falls back to recording
  # missing references silently.
  export TEST_RUNNER_SNAPSHOT_TESTING_RECORD="$SNAPSHOT_RECORD"
  run xcodebuild test \
    -project "$PROJECT" \
    -scheme NepDate \
    -destination "$SIMULATOR" \
    -derivedDataPath "$DERIVED_DATA" \
    -resultBundlePath "build/TestResults.xcresult" \
    -enableCodeCoverage YES \
    CODE_SIGNING_ALLOWED=NO || return 1

  local count
  count="$(find build/snapshots -name '*.png' 2>/dev/null | wc -l | tr -d ' ')"
  echo "test: ${count} screenshots in build/snapshots"
  if [ "$count" -ne "$SNAPSHOT_COUNT" ]; then
    echo "test: expected ${SNAPSHOT_COUNT} screenshots, found ${count}"
    return 1
  fi

  local references
  references="$(find NepDateTests/__Snapshots__ -name '*.png' 2>/dev/null | wc -l | tr -d ' ')"
  echo "test: ${references} reference snapshots in NepDateTests/__Snapshots__"
  if [ "$references" -ne "$SNAPSHOT_REFERENCE_COUNT" ]; then
    echo "test: expected ${SNAPSHOT_REFERENCE_COUNT} reference snapshots, found ${references}"
    return 1
  fi
}

gate_coverage() {
  local bundle="build/TestResults.xcresult"
  local report="build/coverage.json"
  if [ ! -d "$bundle" ]; then
    echo "coverage: $bundle is missing; run the test gate first"
    return 1
  fi
  echo "+ xcrun xccov view --report --json $bundle > $report"
  xcrun xccov view --report --json "$bundle" > "$report" || return 1
  local fraction percent
  fraction="$(jq -r \
    '[.targets[] | select(.name == "CalendarKit") | .lineCoverage] | max // empty' "$report")"
  if [ -n "$fraction" ] && awk -v f="$fraction" 'BEGIN { exit !(f > 0) }'; then
    percent="$(awk -v f="$fraction" 'BEGIN { printf "%.1f", f * 100 }')"
  else
    # xccov does not see the SwiftPM package framework on this Xcode; its coverage map and the
    # test run's profile are both on disk, so read those.
    echo "coverage: xccov has no CalendarKit lines; reading the profile with llvm-cov"
    percent="$(calendar_kit_llvm_coverage)" || return 1
  fi
  echo "coverage: CalendarKit ${percent}% (floor ${COVERAGE_MIN}%)"
  if ! awk -v p="$percent" -v m="$COVERAGE_MIN" 'BEGIN { exit !(p >= m) }'; then
    echo "coverage: CalendarKit is below the ${COVERAGE_MIN}% floor"
    return 1
  fi
}

# Line coverage of the CalendarKit framework from the profile the test run wrote.
calendar_kit_llvm_coverage() {
  local framework profraws
  framework="$(find "$DERIVED_DATA/Build/Products/Debug-iphonesimulator/PackageFrameworks" \
    -name 'CalendarKit_*' -type f 2>/dev/null | head -1)"
  profraws="$(find "$DERIVED_DATA" -name '*.profraw' 2>/dev/null)"
  if [ -z "$framework" ] || [ -z "$profraws" ]; then
    echo "coverage: no CalendarKit framework or profile data to read"
    return 1
  fi
  rm -f build/coverage.profdata
  echo "+ xcrun llvm-profdata merge -sparse <profraws> -o build/coverage.profdata" >&2
  # The profile paths have no spaces: the runner checkout path and DerivedData are fixed.
  xcrun llvm-profdata merge -sparse $profraws -o build/coverage.profdata || return 1
  # Only CalendarKit's own sources count: the framework may also carry code from its
  # dependencies, and a filter that matches nothing must fail the gate, not pass it.
  local summary lines
  summary="$(xcrun llvm-cov export -instr-profile build/coverage.profdata -summary-only \
    "$framework" "$PWD/Packages/CalendarKit/Sources")" || return 1
  lines="$(jq -r '.data[0].totals.lines.count // 0' <<<"$summary")"
  if ! [ "$lines" -gt 0 ] 2>/dev/null; then
    echo "coverage: llvm-cov found no CalendarKit source lines" >&2
    return 1
  fi
  echo "coverage: llvm-cov counted ${lines} lines in Packages/CalendarKit/Sources" >&2
  jq -r '.data[0].totals.lines.percent' <<<"$summary" | awk '{ printf "%.1f", $1 }'
}

gate_network() {
  run bash scripts/check-network.sh
}

echo "xcodebuild: $(xcodebuild -version | tr '\n' ' ')"

gates=(xcodegen format build test coverage network)
names=(xcodegen format build test coverage network)

for i in "${!gates[@]}"; do
  n=$((i + 1))
  echo "== gate $n/${#gates[@]}: ${names[$i]}"
  if ! "gate_${gates[$i]}"; then
    echo "ci: failed at gate $n: ${names[$i]}"
    exit 1
  fi
done
echo "ci: ${#gates[@]}/${#gates[@]} gates passed"
