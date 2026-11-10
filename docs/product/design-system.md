# Design system

How the app and widgets look and read, on both platforms. Platform conventions come first (SwiftUI
and Liquid Glass on iOS, Material 3 on Android); this file adds only what makes the product
recognisable and correct for a Nepali calendar. Made with the `frontend-design` skill (org rule §5b).

## 1. Direction

The subject is the printed Nepali patro on a wall: a month, big Bikram Sambat numerals, small
English numerals in the corner, Saturdays and holidays in red. People have read that page for
decades, so the app keeps its grammar and drops everything around it.

One bold thing: **the BS numeral is the hero of every cell and every widget.** Everything else is
quiet: system fonts, system backgrounds, no cards, no gradients, no decoration.

## 2. Colour

Background, surfaces and text use the platform's semantic colours (`Color(.systemBackground)`,
`MaterialTheme.colorScheme`), so light and dark mode, Liquid Glass and dynamic colour work without
special cases. The brand roles are the shipped marigold palette (`android/.../ui/theme/Color.kt`),
which the iOS asset catalog carries as the same values:

| Token | Light | Dark | Use |
|---|---|---|---|
| `holiday` | `#B3261E` | `#FFB4AB` | Saturday column, public-holiday numerals, holiday names |
| `holidayFill` | `holiday` at 10 % | `holiday` at 16 % | holiday cell background (the H1 mark in full colour) |
| `holidayContainer` | `#FFDAD6` | `#8C1D18` | the stronger tile behind a holiday date in lists, sheets and widgets |
| `onHolidayContainer` | `#410002` | `#FFDAD6` | text and numerals on `holidayContainer` |
| `today` | `#E8A33D` | `#F0B456` | the Sunny "today" mark: a filled shape, not a ring |
| `onToday` | `#271900` | `#271900` | the numeral and marks drawn on the Sunny shape |
| `accent` | `#7C5800` | `#F5BF62` | the app accent, from the Material primary |
| `onAccent` | `#FFFFFF` | `#422C00` | the label of a control filled with `accent`, from the Material on-primary |
| `event` | system secondary label | system secondary label | the Diamond event mark |
| `dimmed` | secondary text, white 0.33 | secondary text, white 0.78 | days of neighbouring months, AD numerals |

The navy `#003893` and the red `#C8102E` of the first design system are gone: today is marigold on a
filled Sunny shape, so it can never be confused with a holiday, and holidays are sindoor red.

Each pair is checked for ≥ 4.5:1 contrast on its background (MR-10 AC3), in light, dark and high
contrast, as a unit test on the resolved colours rather than by eye (Android
`HolidayCellContrastTest`; the iOS counterpart belongs to the design foundation). Adjust lightness
only, never hue.

On Android, `event` and `dimmed` both map to `MaterialTheme.colorScheme.onSurfaceVariant`. Material
3 has no tertiary text role, and `outline` is a border colour that falls below 4.5:1 on the app
backgrounds. `onSurfaceVariant` measures 9.11:1 in light and 10.05:1 in dark, so `dimmed` stays above
4.5:1.

On iOS the system tertiary label falls below 4.5:1, so `dimmed` uses the app's secondary text colour:
7.55:1 on the light background and 12.41:1 on the dark one (I1-03 review, `ios/README.md`).

On Android 12+ with dynamic colour, surfaces and the selection follow the wallpaper when Appearance is
System (MR-16); `holiday` and `today` stay fixed, because their meaning is cultural, not decorative.

### iOS

The SwiftUI side of the same system, recorded in ADR-0015:

- The brand roles are colour sets in `ios/Resources/Assets.xcassets` with Any, Dark and **High
  Contrast** appearances, and `AccentColor` is the marigold primary. Surfaces stay system semantic
  colours; the Android cream surfaces are not ported.
- The four brand marks are SwiftUI `Shape`s in `CalendarUI`, so the app and the widget extension share
  them: `SunnyShape` (today), `SoftBurstShape` (holiday), `DiamondShape` (event) and `CookieShape`
  (the day sheet's header tile).
- Glass belongs to the chrome. Standard bars, sheets, buttons and controls get it from the iOS 27 SDK
  with no work and no opt-out. The content layer has none, per Apple's guidance; the one allowed
  exception is the floating Today button, behind `#available(iOS 26, *)` with a `.regularMaterial`
  fallback.

### H1: holidays are never colour alone

A public holiday always has two signals: the holiday colour **and** a shape. In full-colour mode the
shape is `holidayFill` behind the numeral; in tinted, clear, accented or monochrome modes (iOS widget
rendering modes, lock screens, watch) it is a 1.5 pt underline under the numeral. Saturday has
colour plus its fixed column position. Events use the Diamond mark below the numeral. Today uses the
Sunny shape with `onToday` ink. This also covers colour-blind users.

## 3. Type

System fonts only, so Devanagari is shaped by the OS (CoreText, HarfBuzz) and widgets stay within
their memory limits: SF Pro and Kohinoor Devanagari on iOS, Roboto and Noto Sans Devanagari on
Android. A bundled font is allowed only if the M0 spike shows broken shaping.

| Role | iOS | Android | Notes |
|---|---|---|---|
| Cell BS numeral | `.title2`, semibold, monospaced digits | `titleLarge`, 600 | tabular figures so columns align |
| Cell AD numeral | `.caption2` | `labelSmall` | top-right, `dimmed` |
| Month title | `.largeTitle` | `headlineMedium` | BS month name, then year |
| Widget hero numeral | 56 pt rounded | 56 sp | the one large element in Today widgets |
| Body | `.body` | `bodyLarge` | detail, lists |

Sentence case everywhere. No all-caps labels, no letter-spaced eyebrows.

## 4. Layout

```
 असोज २०८३                      ‹   Today   ›     month title left, navigation right
 Sep – Oct 2026
 आइत   सोम   मङ्गल   बुध   बिही   शुक्र   शनि          weekday row; शनि in holiday colour
                               १ ¹⁷   २ ¹⁸   ३ ¹⁹     Asoj 1 is Thursday 17 Sep; ३ is Saturday
 ─────────────────────────────────────────           and Constitution Day: colour + fill
  ४ ²⁰   ५ ²¹   ६ ²²   ७ ²³   ८ ²⁴   ९ ²⁵  १० ²⁶      ९ is a holiday (Indra Jatra, valley): fill
  …
 १८ ⁴   १९ ⁵  (२०)⁶  २१ ⁷   २२ ⁸   २३ ⁹  २४ ¹⁰      (२०) today: the Sunny fill, Tuesday 6 Oct
 २५ ¹¹  …                                          २५ Ghatasthapana Brata: fill
 ─────────────────────────────────────────
 Upcoming: Ghatasthapana Brata, in 5 days          next holiday under the grid on tall screens
```

- The grid is the screen: edge to edge, no card, hairline separators only between weeks.
- Text is start-aligned except numerals, which are centred in their cell.
- Navigation: iOS uses a `TabView` with Patro, Festivals, Converter and Search (the search tab role),
  Settings behind a toolbar button on every tab; Android uses a `NavigationBar` with the same
  destinations and Settings in the top app bar.

## 5. Motion

Only motion that answers a touch: the month follows the finger on swipe; the day sheet rises from
the tapped cell. No entrance animations, no animated backgrounds. Respect Reduce Motion / remove
animations (cross-fade instead of slide).

## 6. Words

- Name things by what people see: "Today", "Upcoming", "Converter", "Add to calendar".
- Holidays and events use the names from the data, in the app language.
- Errors say what is wrong and what to do: "Asoj has 31 days in 2083". No apologies.
- Empty states say what is missing: "No holiday or tithi data for this date yet".
- Nepali copy is written in Nepali, not translated word for word; reviewed by a native speaker.
