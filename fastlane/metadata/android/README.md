# Android store metadata

This directory is the shared source for the Play listing, F-Droid and IzzyOnDroid. The app text
matches `docs/product/requirements.md` and `docs/product/privacy-policy.md`. The app has no network
permission, no analytics, no crash reporting, no ads and no account.

## Files

| Path | Use |
|---|---|
| `en-US/`, `ne-NP/` | title, short description, full description, changelog for versionCode 1 |
| `en-US/images/phoneScreenshots/`, `ne-NP/images/phoneScreenshots/` | Play phone screenshots, 1080x1920 |
| `privacy_policy_url.txt` | privacy policy URL for the Play Console listing |

## Screenshots

The screenshots come from the Roborazzi set in `android/app/src/test/screenshots`. The red
"Not verified on a device" test label is cropped, then the image is placed in a phone frame by
`tools/frame_store_screenshots.py`. Regenerate them with:

```bash
python tools/frame_store_screenshots.py
```

The script is deterministic and uses no external fonts.

## Privacy policy URL

The checked-in URL is the live GitHub blob. GitHub Pages is not enabled for this repository in the
release-readiness phase, and enabling it is a repository setting change. When Pages is enabled,
replace `privacy_policy_url.txt` with the Pages URL and keep the same text.

## Not in this directory yet

Play also needs a 512x512 app icon and a 1024x500 feature graphic for the listing. They are not
generated here and are part of the A3-04 human gate. Gradle Play Publisher in `android-release.yml`
uploads only the AAB; the listing text and images are entered in Play Console by hand.

## Data safety and content rating

The answers are drafts in `docs/legal/play-data-safety.md` and
`docs/legal/play-content-rating.md`. Enter them in Play Console by hand; A3-04 is the human gate.

## Non-Play stores

F-Droid and IzzyOnDroid read the same descriptions and screenshots from this directory. IzzyOnDroid
uses the APK from the GitHub release. F-Droid builds from source and can ship the developer-signed APK
only when its build matches it, which is what the reproducible build is for. The Play listing is not the source of truth for the app text; the product
docs are.
