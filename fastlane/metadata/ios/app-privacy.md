# App Privacy answers

Draft for the App Privacy section of App Store Connect, for `np.nepdate` 1.0.0. The app collects
no data, so the label is **Data Not Collected**.

| Question | Answer |
|---|---|
| Does the app collect data? | No |
| Data types collected | None |
| Is data linked to the user? | Not applicable |
| Is data used for tracking? | No |
| Does the app share data with third parties? | No |
| Does the app use advertising or analytics SDKs? | No |
| Does the app use the advertising identifier? | No |
| Does the app use a crash-reporting SDK? | No |
| Does the app require an account? | No |
| Does the app make network requests? | No |
| Does the app use the device's location? | No |
| Does the app read the user's contacts, photos or calendar? | No. "Add to calendar" opens the system event editor, which the person confirms |

## Evidence

- `ios/scripts/check-network.sh` greps the sources and the release executables for networking
  frameworks and symbols; the iOS gate runs it on every pull request.
- `ios/Config/NepDateApp-Info.plist` has no `NS*UsageDescription` key and no background modes.
- Settings are stored in the App Group `group.np.nepdate`, on the device, and are shared only with
  the app's widget extension.
- The app has no account, no login, no analytics, no ads and no in-app purchases.
- Holiday and event data is bundled in the app, from the C# NepDate library's patro data.
- The app opens external links, such as the privacy policy and source code, in the system browser.
  Those pages are outside the app and are not covered by this answer.

The App Privacy label is entered by hand in App Store Connect; this repository does not upload it.
