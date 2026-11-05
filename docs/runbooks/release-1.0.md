# Runbook: release 1.0.0

For the owner. This puts the Android and iOS apps out as 1.0.0 on the same day as the NepDate
libraries, so a developer, a desktop user, a web user and a phone user all get one family release.
It lists the steps in order and points to the two per-app runbooks for the detail:
[release-android.md](release-android.md) and [release-ios.md](release-ios.md).

Commands are PowerShell first. Run them from the repository root.

## 0. What has to be true first

| Check | How to check |
|---|---|
| The libraries are tagged 1.0.0 | `gh release view 1.0.0 --repo sanamhub/nepdate-swift` and `curl.exe -s https://repo1.maven.org/maven2/io/github/sanamhub/nepdate/maven-metadata.xml` lists `1.0.0` |
| Both apps use library 1.0.0, not the release candidate | `android/gradle/libs.versions.toml` has `nepdate = "1.0.0"`, `ios/project.yml` has `exactVersion: 1.0.0`, and the iOS package manifests and `NEPDATE_LIBRARY_VERSION` match |
| CI is green on `main` | `gh run list --branch main --limit 5` |
| The holiday data reaches 183 days past the release date | Android: the last passing release date is 2027-06-03 with the current data. Change the data only through a new library release |

NepDate's own packages are exempt from the 14-day dependency age rule (the owner's decision of
2026-11-05). Third-party dependencies keep it.

## 1. Close the two open legal rows

Both rows in [docs/legal/README.md](../legal/README.md) gate any public build. Until the second one
is closed, no build that contains the event data is published (ADR-0009).

1. Ask whether a free digital calendar app needs approval from the Nepal Panchanga Nirnayak
   Samiti. Record the answer in the repository, for example a letter or an email thread summary,
   and change the row to closed with a link to the record.
2. Confirm with the upstream maintainer where the tithi, holiday and event data came from. If the
   answer is that it was scraped, stop and ask the maintainer before anything is published.

## 2. Accounts

| Account | Cost | Notes |
|---|---|---|
| Google Play Console, personal developer account | USD 25 once | Identity verification takes days. A new personal account must run a closed test with 12 testers for 14 days before it can publish to production |
| Apple Developer Program, individual | USD 99 a year | Needs an Apple ID with two-factor authentication and a card that allows international payments. Enrol on the website; in-app enrolment works in only some countries |
| A second person for approvals | none | The `production` environment needs a reviewer who is not the person who pushes the tag (organisation rule) |

## 3. Keys and secrets

Secrets live only in the GitHub environment named `production`. Nothing below goes into a file in
the repository, a chat or an issue.

**Android upload key, one key for every channel (ADR-0009):**

```powershell
keytool -genkeypair -v -keystore nepdate-upload.jks -alias nepdate-upload `
  -keyalg RSA -keysize 4096 -validity 10000
```

Keep an offline backup of `nepdate-upload.jks` and both passwords. Then store the secrets:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("nepdate-upload.jks")) |
  gh secret set ANDROID_KEYSTORE_BASE64 --env production
gh secret set ANDROID_KEYSTORE_PASSWORD --env production
gh secret set ANDROID_KEY_ALIAS --env production
gh secret set ANDROID_KEY_PASSWORD --env production
Get-Content play-service-account.json -Raw | gh secret set PLAY_SERVICE_ACCOUNT_JSON --env production
```

`gh secret set` without a value asks for it, so a password never lands in the shell history. Delete
the local `.jks` copy you do not need and the service-account file after the upload.

In Play Console, enrol the key as the app signing key ("use my own key") so the same APK installs
from every channel. Register the package name and the signing key in the Android Developer Console
before the 2027 enforcement.

**iOS cloud signing:** in App Store Connect, Users and Access, Integrations, create an App Store
Connect API key with the Admin role. Download the `.p8` once, then add the secrets `ASC_KEY_P8`,
`ASC_KEY_ID` and `ASC_ISSUER_ID` to the environment, and delete the downloaded file.

**The environment:** in GitHub, Settings, Environments, create `production` with the tags
`android-v*` and `ios-v*` as the only allowed deployment refs and the second person as the required
reviewer.

## 4. Android closed test

The Android plan allows the closed test before the legal rows are closed. ADR-0009 says no build
that contains the event data is published while the provenance row is open. Decide which applies
before inviting testers; the safe order is to close both rows first (step 1).

1. Run the release workflow from `main` with `dry_run` set to false and approve the `production`
   deployment. It signs the bundle and uploads it to the Play internal track.
2. In Play Console, promote that build from internal testing to a closed testing track.
3. Add 12 testers by email and start the test. The 14 days start when they have opted in.
4. Fix what they report with a new `versionCode` (a `versionCode` is never reused).
5. When Play Console allows it, apply for production access.

## 5. Manual test checklist

Run [manual-test.md](manual-test.md) on the reference devices: the Galaxy A15 class phone, the
Android Go class phone and the reference iPhone. Attach the ticked copy to the release pull request.

## 6. Check the release before tagging

Confirm the Android preflight with a dry run. It builds and checks and does not sign, upload or
create a release:

```powershell
gh workflow run android-release.yml --ref main -f dry_run=true
gh run watch
```

The preflight needs `versionName` in `android/app/build.gradle.kts` to equal the tag without
`android-v`, a section `## [1.0.0]` in `android/CHANGELOG.md` and 183 days of public holidays after
the UTC release date. Set the date in the changelog heading in the pull request that prepares the
release.

For iOS, set `MARKETING_VERSION` to `1.0.0` in `ios/project.yml` and add `## [1.0.0] - <date>` to
`ios/CHANGELOG.md`. The iOS release workflow and the App Store listing are not written yet; see
[the iOS release plan](../plan/I3-ios-release.md) for what is possible before the Apple account
exists.

## 7. Tags

Tag only after steps 1 to 6 are done and the release pull request is merged.

```powershell
git switch main
git pull --ff-only
git tag android-v1.0.0
git push origin android-v1.0.0
```

Open the run in GitHub Actions and approve the `production` deployment (the second person does
this). The workflow signs, uploads to the Play internal track and prepares a draft GitHub release.
Then follow "Releasing" in [release-android.md](release-android.md): install the internal build and
the GitHub APK, publish the release, and promote in Play Console with a staged rollout.

```powershell
git tag ios-v1.0.0
git push origin ios-v1.0.0
```

The iOS tag works only after the iOS release workflow exists and the Apple account is set up.
Submission for review stays manual in App Store Connect, with phased release on.

## 8. After release

- Watch crashes in Android vitals and App Store Connect for 48 hours before moving a staged rollout
  to 100 percent.
- The data runs out. With the data of 2026-11-05 the last release date that passes the Android
  freshness gate is 2027-06-03, and tithi ends on 2027-04-13. New data arrives as a library release,
  then an app release.
- To roll back, follow the "Rolling back" sections of the two per-app runbooks.
