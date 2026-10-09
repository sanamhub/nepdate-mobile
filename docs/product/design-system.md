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
special cases. Two brand colours, both from the Nepal flag:

| Token | Light | Dark | Use |
|---|---|---|---|
| `holiday` | `#C8102E` | `#FF6B7A` | Saturday column, public-holiday numerals, holiday names |
| `holidayFill` | `#C8102E` at 10 % | `#FF6B7A` at 16 % | holiday cell background (the H1 mark in full colour) |
| `today` | `#003893` | `#8FB3FF` | today ring (2 pt / 2 dp), "Today" button |
| `event` | system secondary label | system secondary label | event dot |
| `dimmed` | system tertiary label | system tertiary label | days of neighbouring months, AD numerals |

Each pair is checked for ≥ 4.5:1 contrast on its background (MR-10 AC3) in the M0 design spike;
adjust lightness only, never hue.

On Android 12+ with dynamic colour, surfaces and the selection follow the wallpaper; `holiday` and
`today` stay fixed, because their meaning is cultural, not decorative.

### H1: holidays are never colour alone

A public holiday always has two signals: the holiday colour **and** a shape. In full-colour mode the
shape is `holidayFill` behind the numeral; in tinted, clear, accented or monochrome modes (iOS widget
rendering modes, lock screens, watch) it is a 1.5 pt underline under the numeral. Saturday has
colour plus its fixed column position. Events use a dot below the numeral. This also covers
colour-blind users.

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
 १८ ⁴   १९ ⁵  (२०)⁶  २१ ⁷   २२ ⁸   २३ ⁹  २४ ¹⁰      (२०) today ring, Tuesday 6 Oct
 २५ ¹¹  …                                          २५ Ghatasthapana Brata: fill
 ─────────────────────────────────────────
 Upcoming: Ghatasthapana Brata, in 5 days          next holiday under the grid on tall screens
```

- The grid is the screen: edge to edge, no card, hairline separators only between weeks.
- Text is start-aligned except numerals, which are centred in their cell.
- Navigation: iOS uses a `NavigationStack` with a tab bar (Month, Upcoming, Converter) and Settings
  in the toolbar; Android uses a `NavigationBar` with the same three destinations and Settings as
  an icon button in the top app bar of each.

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
