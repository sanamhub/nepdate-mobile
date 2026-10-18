# Design spike results

Status: Android screenshots recorded, **not verified on a device**. The human check on an A15-class
phone and a low-end phone is open. iOS is not started (M0-03).

## How to reproduce

- Debug screen: `np.nepdate.design.DesignSpikeActivity`. It is debug only and has no launcher icon:

  ```bash
  adb shell am start -n np.nepdate/.design.DesignSpikeActivity
  ```

- Screenshots:

  ```bash
  cd android
  ./gradlew recordRoborazziDebug
  ```

- Images are in [`android/app/src/test/screenshots/`](../../android/app/src/test/screenshots/).
- `./gradlew ci` verifies them with Roborazzi.
- Rendering uses the system font from the Android SDK image (Roboto and Noto Sans Devanagari).
  Robolectric runs API 35 because API 36 needs Java 21 and the build uses JDK 17 (ADR-0005). The
  Android 16 `elegantTextHeight` change is therefore still a device check.

## Devanagari

The strings are the 11 lines of the Rust plan's
[Devanagari test strings](https://github.com/sanamhub/nepdate-rust/blob/main/docs/plan/devanagari-test-strings.md).
The cell screenshot shows each line at 14 sp and 22 sp. The hero screenshot shows each line at
56 sp.

| # | What the line checks | 14 / 22 sp | 56 sp | Android A15 class | Android low-end |
|---|---|---|---|---|---|
| 1 | Devanagari digits U+0966 to U+096F | Recorded | Recorded | Not verified | Not verified |
| 2 | Month names 1 to 6, vowel signs ै and ौ | Recorded | Recorded | Not verified | Not verified |
| 3 | Months 7 to 12, reph र्त, anusvara ं | Recorded | Recorded | Not verified | Not verified |
| 4 | Weekdays, ङ्ग, ि before the consonant, conjunct क्र | Recorded | Recorded | Not verified | Not verified |
| 5 | Every matra on क; ि must sit left of क | Recorded | Recorded | Not verified | Not verified |
| 6 | Conjunct ligatures क्ष ज्ञ श्र त्र द्ध द्व ह्म ट्ट | Recorded | Recorded | Not verified | Not verified |
| 7 | Reph above the next consonant | Recorded | Recorded | Not verified | Not verified |
| 8 | Tithi names with ञ्च, ष्ठ, प्र, द्वि | Recorded | Recorded | Not verified | Not verified |
| 9 | Chandrabindu ँ and event conjuncts | Recorded | Recorded | Not verified | Not verified |
| 10 | A full widget header line | Recorded | Recorded | Not verified | Not verified |
| 11 | Latin fallback on the same line | Recorded | Recorded | Not verified | Not verified |

Screenshots:

- [Cell, light](../../android/app/src/test/screenshots/design-spike-devanagari-cell-light.png)
- [Cell, dark](../../android/app/src/test/screenshots/design-spike-devanagari-cell-dark.png)
- [Cell, Nepali](../../android/app/src/test/screenshots/design-spike-devanagari-cell-ne.png)
- [Hero, light](../../android/app/src/test/screenshots/design-spike-devanagari-hero-light.png)
- [Hero, dark](../../android/app/src/test/screenshots/design-spike-devanagari-hero-dark.png)
- [Hero, Nepali](../../android/app/src/test/screenshots/design-spike-devanagari-hero-ne.png)

The reviewer checks for a broken conjunct and a clipped vowel sign, especially in the 56 sp hero
line and in the Android 16 `elegantTextHeight` change.

## Colour

The holiday cell draws `holidayFill` directly on the screen background. There is no
`surfaceVariant` container between them. The earlier container made the dark fill 2.82:1 and the
light fill 3.86:1, although the token measured 4.82:1 and 4.89:1 over the background. The cell and
the check now call the same `NepdateColors.holidayFillOn(surface)` and `contrastRatio` functions.

| Scheme | Cell surface | Composited fill | Holiday text on fill | Threshold | Result |
|---|---|---|---|---|---|
| Light | `#FFFBFE` | `#F9E3E9` | 4.82:1 | 4.5:1 | Measured pass, not verified on device |
| Dark | `#1C1B1F` | `#40282E` | 4.89:1 | 4.5:1 | Measured pass, not verified on device |
| Dynamic light | Robolectric dynamic surface | `#F5E0EA` | 4.69:1 | 4.5:1 | Measured pass, not verified on device |

The threshold is 4.5:1 (MR-10 AC3). The alphas are unchanged: 10 % in light and 16 % in dark.
`HolidayCellContrastTest` fails below 4.5:1 for light, dark and dynamic, using the same functions as
the cell. At a temporary light alpha of 30 % the test failed at 3.34:1.

The other tokens are measured on the background:

| Token | Light | Dark | Threshold | Result |
|---|---|---|---|---|
| `holiday` on background | 5.74:1 | 6.23:1 | 4.5:1 | Measured pass, not verified on device |
| `today` on background | 10.31:1 | 8.21:1 | 4.5:1 | Measured pass, not verified on device |
| `event` on background | 9.11:1 | 10.05:1 | 4.5:1 | Measured pass, not verified on device |
| `dimmed` on background | 9.11:1 | 10.05:1 | 4.5:1 | Measured pass, not verified on device |

With Android dynamic colour the surface changes, so the same fixed brand tokens measure 5.59:1 for
`holiday`, 10.03:1 for `today`, and 9.39:1 for `event` and `dimmed` in the Robolectric light
surface. All pass the threshold; the real wallpaper colours are still a device check.

Screenshots:

- [Colour, light](../../android/app/src/test/screenshots/design-spike-colour-light.png)
- [Colour, dark](../../android/app/src/test/screenshots/design-spike-colour-dark.png)
- [Colour, Nepali](../../android/app/src/test/screenshots/design-spike-colour-ne.png)
- [Colour, dynamic colour](../../android/app/src/test/screenshots/design-spike-colour-dynamic.png)

The holiday cell in the colour shots carries the H1 mark: the holiday colour and the
`holidayFill` shape behind the numeral. The dynamic shot uses `dynamicLightColorScheme` and keeps
`holiday` and `today` fixed.

## Open

- A15-class and low-end device check for broken conjuncts, clipped vowel signs and the Android 16
  `elegantTextHeight` change.
- Dynamic colour with a real wallpaper on Android 12+.
- iOS screenshots and device check (M0-03, the Swift agent).
