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
| W-06 | Festival countdown | `systemSmall`, `accessoryRectangular` | 2×2, 3×3, 4×1, 4×4 | previous / next |
| W-07 | Festivals list | `systemLarge` | 4×2, 5×5 | no |
| W-08 | Date chip | `accessoryCircular` | 1×1, 2×2 | no |
| W-09 | Date and tithi | `accessoryRectangular` | 4×1, 5×2 | no |
| W-10 | This week | `systemMedium` | 4×1, 4×2, 5×3, 5×4 | no |
| W-11 | Date text | `accessoryInline` | 3×1, 5×3 | no |

## W-01 Today

The BS day numeral large (design-system hero size), the BS month name, the weekday, and the AD date
in one line. On a public holiday, the first holiday name replaces the AD line and the numeral gets
the holiday mark. Under them, when the face has room: the tithi, the other calendar's date, then one
line with the next holiday and "in N days".
- AC1: on Android the one-line form is the Date and tithi widget (W-09); W-01 itself keeps the
  numeral, the weekday and the month at its smallest size.
- AC2: tap opens the app on today.
- AC3: when "Date follows: Nepal time" is on and the zones differ, a small "Nepal" label sits beside
  the weekday, in the label type role.
- AC4: the next-holiday line is the first line dropped when the face runs short, and a line that does
  not fit is left out rather than truncated.

## W-02 Today and upcoming

W-01's content on the left, the next four days with a holiday or event on the right (name and "in N
days").
- AC1: tapping an entry opens its day detail; tapping the left half opens today.
- AC2: when fewer than four entries remain before the end of coverage, the right side ends with
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

The three lock-screen widgets also ship as widgets of their own on Android, so they have their own IDs
(W-08, W-09, W-11); W-04 keeps the family rules that apply to all three.

## W-05 Control (iOS 18+)

A `ControlWidgetButton` that opens the app on the converter, and one that opens today. SF Symbols
only.
- AC1: "Open converter" opens the Converter tab; "Open today" opens Month on today's month with
  today selected.
- AC2: both appear in the controls gallery and can be placed in Control Center and on the Lock
  Screen (and on the Action button on iPhones that have one).

## W-06 Festival countdown

The next public holiday or festival with its name and "in N days" (or "Today"), and previous / next
buttons that step through the same twelve-day window on both platforms and stop at its ends.
- AC1: the buttons change the shown day inside the widget; the state lives in the App Group on iOS
  and in Glance state on Android, and resets to the next festival at midnight.
- AC2: on a public holiday the numeral or the title carries the holiday mark (H1).
- AC3: tapping opens the shown day; on iOS the same view is offered as `accessoryRectangular`.

## W-07 Festivals list

The next festivals and holidays as a list, the same data as MR-03, with a "Holidays only" filter.
- AC1: on iOS the filter is a configuration parameter, not a button inside the widget.
- AC2: near the end of coverage the list ends with the coverage footer (MR-03 AC3).
- AC3: no row is cut; a row that does not fit is left out.

## W-08 Date chip

The BS day numeral over the short BS month name.
- AC1: the short month names are designed for this widget in English and Nepali; the iOS version does
  not cut the first three letters of the long name.
- AC2: on a public holiday the numeral keeps the holiday mark (H1) in every rendering mode.

## W-09 Date and tithi

The weekday and BS month and year, the BS numeral on the Sunny mark, and the tithi.
- AC1: it is the 4×1 form of W-01 on Android (W-01 AC1).
- AC2: on iOS it checks that the tithi is what the rectangular accessory shows, in both languages.

## W-10 This week

The seven days of the current BS week and the next holiday or event.
- AC1: the week row marks today, holidays and Saturdays as the grid does (H1).
- AC2: the agenda line names the next day in the week that has a holiday or event, or the next one
  after the week when the week has none left.

## W-11 Date text

One line: the BS date, the weekday and the AD date, or the holiday name when the day is one.
- AC1: reads correctly in `accessoryInline` where the system supplies the font size, and does not
  rely on colour.

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
