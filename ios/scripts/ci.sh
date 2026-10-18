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
    Packages/CalendarKit/Sources \
    Packages/CalendarUI/Sources
}

gate_build() {
  run xcodebuild build \
    -project "$PROJECT" \
    -scheme NepDate \
    -configuration Release \
    -destination "generic/platform=iOS" \
    -derivedDataPath "$DERIVED_DATA" \
    -quiet \
    CODE_SIGNING_ALLOWED=NO \
    SWIFT_TREAT_WARNINGS_AS_ERRORS=YES

  local app="$DERIVED_DATA/Build/Products/Release-iphoneos/NepDate.app"
  local appex="$app/PlugIns/NepDateWidgets.appex"
  if [ ! -d "$app" ]; then
    echo "build: $app is missing after the build"
    return 1
  fi
  echo "size: NepDate.app $(du -sk "$app" | awk '{ print $1 }') KB"
  echo "size: NepDateWidgets.appex $(du -sk "$appex" | awk '{ print $1 }') KB"
}

gate_test() {
  rm -rf "build/TestResults.xcresult"
  run xcodebuild test \
    -project "$PROJECT" \
    -scheme NepDate \
    -destination "$SIMULATOR" \
    -derivedDataPath "$DERIVED_DATA" \
    -resultBundlePath "build/TestResults.xcresult" \
    -quiet \
    CODE_SIGNING_ALLOWED=NO
}

gate_network() {
  run bash scripts/check-network.sh
}

echo "xcodebuild: $(xcodebuild -version | tr '\n' ' ')"

gates=(xcodegen format build test network)
names=(xcodegen format build test network)

for i in "${!gates[@]}"; do
  n=$((i + 1))
  echo "== gate $n/${#gates[@]}: ${names[$i]}"
  if ! "gate_${gates[$i]}"; then
    echo "ci: failed at gate $n: ${names[$i]}"
    exit 1
  fi
done
echo "ci: ${#gates[@]}/${#gates[@]} gates passed"
