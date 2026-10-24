#!/usr/bin/env bash
# Fails when the app or widget asks for background work. ADR-0006 and MR-09 AC5 allow only the
# widget timeline; there is no background task, no BGTaskScheduler and no UIBackgroundModes entry.
set -uo pipefail
cd "$(dirname "$0")/.."

status=0

# project.yml too: XcodeGen writes its info properties into the plists at generate time.
if grep -R -n --include='*.plist' 'UIBackgroundModes' Config || grep -n 'UIBackgroundModes' project.yml; then
  echo "background: UIBackgroundModes appears in an Info.plist or project.yml"
  status=1
fi

if grep -R -n --include='*.swift' 'BGTaskScheduler' NepDateApp NepDateWidgets Packages; then
  echo "background: BGTaskScheduler appears in the sources"
  status=1
fi

if [ "$status" -eq 0 ]; then
  echo "background: no UIBackgroundModes or BGTaskScheduler"
fi
exit "$status"
