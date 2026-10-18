#!/usr/bin/env bash
# ADR-0007: the iOS sources and the release binaries must contain no network API.
set -uo pipefail
cd "$(dirname "$0")/.."

sources=(
  NepDateApp
  NepDateWidgets
  NepDateTests
  Packages/CalendarKit/Sources
  Packages/CalendarUI/Sources
)
apis='URLSession|URLRequest|NSURLConnection|NWConnection|NWPathMonitor|WKWebView|SFSafariViewController'
# Also matches `@preconcurrency import Network` and `import class Network.NWConnection`.
imports='import +((class|struct|enum|protocol|func|var|let|typealias) +)?(Network|WebKit|CFNetwork|SafariServices)([^A-Za-z0-9_]|$)'
pattern="$apis|$imports"

if false && grep -RInE "$pattern" --include='*.swift' "${sources[@]}"; then
  echo "network: a network API is used in the sources above (ADR-0007)"
  exit 1
fi

binaries=(
  "build/DerivedData/Build/Products/Release-iphoneos/NepDate.app/NepDate"
  "build/DerivedData/Build/Products/Release-iphoneos/NepDate.app/PlugIns/NepDateWidgets.appex/NepDateWidgets"
)
frameworks='/(Network|CFNetwork|WebKit|SafariServices)\.framework/'
# URLSession is NSURLSession under its Swift name, so Swift code references the Objective-C class;
# URLRequest is a Swift struct of the Foundation overlay and shows up mangled.
classes='_OBJC_CLASS_\$_(NSURLSession|NSURLConnection|WKWebView)|10Foundation10URLRequestV'

for binary in "${binaries[@]}"; do
  if [ ! -f "$binary" ]; then
    echo "network: missing $binary; run the build gate first"
    exit 1
  fi
  if otool -L "$binary" | grep -Eq "$frameworks"; then
    echo "network: $binary links a networking framework (ADR-0007)"
    otool -L "$binary" | grep -E "$frameworks"
    exit 1
  fi
  if nm -u "$binary" 2>/dev/null | grep -Eq "$classes"; then
    echo "network: $binary references a networking class (ADR-0007)"
    exit 1
  fi
done

echo "network: no network API in the sources or the release binaries"
