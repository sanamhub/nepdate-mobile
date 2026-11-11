# iOS store metadata

The English listing is in `en-US`. The App Store Connect locale for Nepali does not exist, so there
is no `ne-NP` directory: Apple's App Store localization list has no Nepali entry, and the app is
sold in English only. In-app language is still English and Nepali, through the system per-app
language page.

## What is here

| Path | Use |
|---|---|
| `en-US/name.txt` | App name, within 30 characters |
| `en-US/subtitle.txt` | Subtitle, within 30 characters |
| `en-US/description.txt` | Description, within 4,000 characters |
| `en-US/keywords.txt` | Keywords, within 100 characters |
| `en-US/release_notes.txt` | What is new in the first release |
| `en-US/privacy_url.txt` | Privacy policy URL |
| `app-privacy.md` | Answers for the App Privacy questionnaire |
| `age-rating.md` | Answers for the age rating questionnaire |
| `review-notes.md` | Notes for App Review, including how to reach every screen |
| `export-compliance.md` | Export compliance answer and its evidence |

## Screenshots

The six screens in English and Nepali are rendered by `NepDateTests/StoreScreenshotTests` at
1320 by 2868 pixels, the 6.9-inch iPhone size App Store Connect accepts today. They are uploaded
from every iOS run as the `ios-store-screenshots` artifact; copy the twelve PNGs into App Store
Connect by hand. The Nepali set is for the in-app language review, not a store localization.

The screenshots use a fixed today of Asoj 20, 2083 and the light theme. They are drawn from the real
screens and widget views, with no status bar, no test labels and no device chrome.

## What still needs the owner

The App Store Connect record, the agreements, the DSA status, the App Store Connect API key, the
signed build, TestFlight and submission need the Apple Developer account. The answers in
`app-privacy.md`, `age-rating.md` and `export-compliance.md` are drafts to enter by hand; this
repository does not call App Store Connect.
