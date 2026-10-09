# Widget catalogue (v1.0)

Widgets are the main product, so each one has its own ID (`W-xx`) and acceptance criteria. Common
rules: MR-05, design-system H1 (holiday mark), ADR-0006 (refresh and data). Every widget is built
natively on each platform (WidgetKit, Glance); nothing here is shared code except the libraries.

| ID | Widget | iOS families | Android size (cells) | Interactive |
|---|---|---|---|---|
| W-01 | Today | `systemSmall`, StandBy | 2×2, resizes to 4×1 | no |
| W-02 | Today and upcoming | `systemMedium` | 4×2 | no |
| W-03 | Month | `systemLarge`, `systemExtraLargePortrait` (iOS 27+) | 4×4, resizes to 4×3 and 5×5 | previous / next / today |
| W-04 | Lock screen | `accessoryInline`, `accessoryCircular`, `accessoryRectangular` | lock screen (Android 16 QPR1+) uses W-01 and W-02 | no |
| W-05 | Control | Control Center / Lock Screen / Action button control | (no equivalent) | opens converter or today |

## W-01 Today

The BS day numeral large (design-system hero size), the BS month name, the weekday, and the AD date
in one line. On a public holiday, the first holiday name replaces the AD line and the numeral gets
the holiday mark. One line at the bottom: the next holiday and "in N days".
- AC1: at 4×1 (Android) it becomes one line: "असोज २० मङ्गलवार, 6 Oct" style, holiday name if any.
- AC2: tap opens the app on today.
- AC3: when "Date follows: Nepal time" is on and the zones differ, a small "Nepal" label shows under
  the numeral.

## W-02 Today and upcoming

W-01's content on the left, the next three holidays or events on the right (name and "in N days").
- AC1: tapping an entry opens its day detail; tapping the left half opens today.
- AC2: when fewer than three entries remain before the end of coverage, the right side ends with
  "Holiday data ends on <BS date>" (same date as MR-03 AC3).

## W-03 Month

The month grid from MR-01, compact: BS numerals with AD numerals on sizes where they fit (iOS
`systemLarge` and up, Android ≥ 4×4), holiday marks, event dots, today ring.
- AC1: previous, next and "Today" buttons change the month inside the widget (iOS App Intents
  `Button(intent:)`; Android `actionRunCallback` + Glance state). The widget returns to the current
  month at the next midnight refresh.
- AC2: tapping a day opens that day's detail (iOS `widgetURL`/`Link`; Android deep-link extra).
- AC3: iOS `systemExtraLargePortrait` adds the month's holidays and events as a list under the grid.
- AC4: renders the 6-week month without clipping at the smallest allowed size on each platform.

## W-04 Lock screen (iOS) and lock-screen placement (Android)

- `accessoryInline`: "असोज २०, मङ्गलवार" (or English per language).
- `accessoryCircular`: BS day numeral over the short month name; holiday underline.
- `accessoryRectangular`: BS date on line 1, today's holiday or the next holiday with "in N days" on
  line 2.
- Android: W-01 and W-02 declare `keyguard` eligibility; activities opened from the lock screen
  require unlock (no `showWhenLocked`).
- AC1: legible in the vibrant / tinted rendering modes (no colour-only information).

## W-05 Control (iOS 18+)

A `ControlWidgetButton` that opens the app on the converter, and one that opens today. SF Symbols
only.
- AC1: "Open converter" opens the Converter tab; "Open today" opens Month on today's month with
  today selected.
- AC2: both appear in the controls gallery and can be placed in Control Center and on the Lock
  Screen (and on the Action button on iPhones that have one).

## Quality bar

- **Android configuration:** each widget offers one setting in its configuration activity:
  background opacity (solid, 70 %, transparent; default solid). Users of other calendar apps ask
  for this most. The holiday mark H1 stays visible at every opacity.
- **Android:** Tier 1 of the [widget quality guidelines](https://developer.android.com/docs/quality-guidelines/widget-quality):
  fills all four edges, resizes to 2×2, 4×1 and 4×2 where listed, dynamic colour with light and
  dark, generated previews on Android 15+ with a static `previewLayout` fallback, descriptive name
  and description, system corner radius, launch transitions.
- **iOS:** full-colour, accented (tinted / clear) and vibrant rendering modes all tested
  (`widgetRenderingMode`); `containerBackground(for: .widget)`; `widgetAccentable()` on the numeral
  and holiday mark; placeholder and snapshot states look like the real widget.
- **Both:** a snapshot test per widget × size × mode × language (ADR-0008).

## Later (v1.x candidates, need an ADR)

Apple Watch complications (WidgetKit accessory families, same code as W-04), Wear OS complication
(when `glance-wear` is stable), holiday reminder notifications.
