# Play Data safety answers

Draft for the Play Console Data safety form. Applies to `np.nepdate`, versionCode 1. The answers
come from ADR-0007 and `docs/product/privacy-policy.md`; change them only with a new privacy review.

| Question | Answer |
|---|---|
| Does the app collect or share any required user data? | No |
| Data types collected | None |
| Data types shared | None |
| Is data encrypted in transit? | Not applicable: no data leaves the device |
| Can users request data deletion? | Not applicable: no account and no collected data |
| Does the app have an independent security review? | No |
| Is the app directed to children? | No |
| Does the app contain ads? | No |
| Does the app use analytics, crash reporting or tracking SDKs? | No |
| Does the app request the internet permission? | No |
| Does the app request runtime permissions? | No |

## Evidence

- `AndroidManifest.xml` has no `INTERNET` permission; `checkNoInternet` reads the merged release
  manifest.
- `checkPermissions` allows only `RECEIVE_BOOT_COMPLETED` and the app's AndroidX dynamic-receiver
  permission.
- Settings are stored in Preferences DataStore on the device and shared only with the app's widgets.
- "Add to calendar" opens the system editor. "Copy date" writes to the clipboard at the user's
  request. Neither sends data to the app.
- No analytics, crash reporting, ads, account or tracking dependency is in
  `gradle/libs.versions.toml`.

## Remaining human step

Enter these answers in Play Console. The store listing text must keep the same claims. A3-04 is the
human gate for the closed test and production rollout.
