# Runbook: release the iOS app

Decided in ADR-0009 and ADR-0010. Owner: maintainer. Approver: a second person. Everything below
works from a Windows PC browser and the iPhone; no Mac.

## One-time setup

Done in M0-07, because `ios-beta.yml` (M0-04) needs it.

1. Enrol in the Apple Developer Program (individual, USD 99 a year) at developer.apple.com/programs
   with an Apple ID that has two-factor authentication. In-app enrolment through the Apple Developer
   app works only in some countries; the website works wherever enrolment is offered. Pay with a
   card that allows international payments.
2. Certificates, Identifiers and Profiles: register the bundle IDs `np.nepdate` and
   `np.nepdate.widgets` and the App Group `group.np.nepdate`, enabled on both IDs.
3. App Store Connect: create the app record (name, bundle ID `np.nepdate`, SKU).
4. Users and Access, Integrations, App Store Connect API: create a team key with the **Admin** role
   (cloud signing needs it). Download the `.p8` once; note the key ID and issuer ID.
5. GitHub, Settings, Environments: create `testflight` (deployments from `main` only) and
   `production` (tags `ios-v*` and `android-v*` only, required reviewer; ADR-0009). In each, add secrets `ASC_KEY_P8` (file
   content), `ASC_KEY_ID` and `ASC_ISSUER_ID`. Then delete the downloaded `.p8` from the PC.
6. Install TestFlight on the iPhone and add yourself as an internal tester.
7. Run `ios-beta.yml` once from `main` and install the build on the iPhone.

If the key leaks: revoke it in App Store Connect at once, create a new one and replace the secrets in
both environments.

## Before tagging

1. `ios.yml` green on `main`; `docs/runbooks/manual-test.md` done on the reference iPhone.
2. `MARKETING_VERSION` and build number bumped; `ios/CHANGELOG.md` has `## [X.Y.Z] - YYYY-MM-DD`.
3. Data freshness: the bundled `nepdate-swift` public holidays reach at least 183 days ahead (ADR-0003 §4);
   copy any tithi warning into the release PR.
4. First public release only: both **open** rows in `docs/legal/README.md` are closed (ADR-0009).

## Releasing

```bash
git tag ios-vX.Y.Z
git push origin ios-vX.Y.Z
```

`ios-release.yml` archives and uploads to App Store Connect after the `production` approval. Then, by
hand:
1. TestFlight: install on a device; check today's date, each widget family, lock-screen widgets, the
   controls, and that no permission prompt appears.
2. App Store Connect: attach the build, paste "What's new" from the changelog, submit for review with
   phased release on.

## Rolling back

Pause the phased release; if needed remove the version from sale and submit a fix with an expedited
review request. Note it in the changelog.
