# Runbook: release the Android app

Decided in ADR-0009. Owner: maintainer. Approver: a second person.

## Before tagging

1. `android.yml` green on `main`; `docs/runbooks/manual-test.md` done on the reference devices for this
   version (attach the ticked copy to the release PR).
2. `versionCode` and `versionName` bumped; `android/CHANGELOG.md` has `## [X.Y.Z] - YYYY-MM-DD`;
   `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt` written (F-Droid and Izzy read it).
3. Data freshness: the bundled `nepdate-kotlin` public holidays reach at least 183 days ahead (ADR-0003 §4);
   copy any tithi warning into the release PR.
4. First public release only: both **open** rows in `docs/legal/README.md` are closed (ADR-0009).

## Releasing

```bash
git tag android-vX.Y.Z
git push origin android-vX.Y.Z
```

`android-release.yml` runs the preflight, the Android gates, the signed build, the attestation, the
Play upload and the draft GitHub Release. It can also be started with `workflow_dispatch`; `dry_run`
defaults to `true`, which stops before signing, Play and the release.

The `production` environment needs these secrets before a tag is pushed:

| Secret | Use |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | base64 of the upload keystore |
| `ANDROID_KEYSTORE_PASSWORD` | keystore password |
| `ANDROID_KEY_ALIAS` | upload-key alias |
| `ANDROID_KEY_PASSWORD` | upload-key password |
| `PLAY_SERVICE_ACCOUNT_JSON` | Play Developer API service-account JSON |

The signing and Play jobs run only after the `production` approval. The workflow uploads the AAB to
the internal track and does not promote it. The release is a draft; a human publishes it.

Then, by hand:
1. Install the internal-track build from Play and the GitHub APK on one device each; check today's
   date, a widget, and that no permission prompt appears.
2. Publish the GitHub release (IzzyOnDroid and Obtainium pick it up; F-Droid builds from the tag and
   publishes once its rebuild matches).
3. Promote in Play Console from internal to production with a staged rollout, 20 % then 100 % after
   48 hours without new crashes in Android vitals. For 1.0 the build goes through the closed test
   first (A3-04).

## Rolling back

Halt the staged rollout in Play Console; mark the GitHub release as pre-release; fix forward with a
new `versionCode` (a `versionCode` can never be reused). Note it in the changelog.
