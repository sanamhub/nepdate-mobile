# NepDate app icon

`nepdate-app-icon.svg` is the master artwork, generated with every export below by
`android/tools/generate_app_icon.py`. It uses the 108 unit Android adaptive icon grid: a sindoor
(`#B3261E`) ground, a cream (`#FBF3E4`) calendar page with an ink-red (`#7A1712`) header and two
binder rings, and the marigold (`#E8A33D`) Sunny shape in the middle of the page. The sun is the
"today" marker the app and widgets use, so the icon reads as "today on the calendar". In the sun
sits a small NepDate mark: the logo's disc in the page colour with its pennant line in the header
red. The logo's moon and sun are left out because they would not survive launcher sizes. It sits in
the same family as the Mitibar desktop icon, a calendar page on a red tile.

The icon carries no numeral. A fixed numeral read as a wrong date wherever a phone shows the app
icon next to the date, such as the status bar and the notification shade.

## Geometry

- Page: a rounded rectangle from 32,33 to 76,79 with a 7 unit corner, inside the 33 unit safe
  circle. Header: the top of the page down to y 45.
- Rings: two 3.6 by 8 unit capsules centred at x 43 and x 65, from y 29.5.
- Sun: 12 lobes around 54,62, outer radius 13, inner radius 0.86 of that, sampled 12 points per
  lobe.
- Mark: a disc of radius 8.4 on the sun centre. The logo's pennant line (936 unit grid, disc
  radius 447) is scaled onto it, 1.4 units wide, mitred, with the sharp tip bevelled beyond 2.2
  half-strokes so it stays inside the disc. Its ends stop at the disc edge.

## Exports

| File | How it was made |
| --- | --- |
| `android/app/src/main/res/drawable/ic_launcher_foreground.xml` | The six SVG paths, with colours from `ic_launcher_page`, `ic_launcher_header` and `ic_launcher_sun`. The About screen draws it. |
| `android/app/src/main/res/values/colors.xml` `ic_launcher_background` | The SVG background colour, used as the adaptive icon background layer. |
| `android/app/src/main/res/drawable/ic_launcher_monochrome.xml` | The page, rings, sun, disc and pennant as one path with `fillType="evenOdd"`, so for themed icons the sun is a hole in the page, the disc fills it again and the pennant is a hole in the disc. |
| `android/app/src/main/res/drawable/splash_icon.xml` | Not the launcher mark: the full Nepal flag (crimson `#DC143C`, blue border `#003893`, white crescent and sun) on the splash surface with no icon background, scaled 1.08 so it fills the 36 unit circle. |
| `android/app/src/main/res/drawable/splash_icon_animated.xml` | Grows the flag from 0.93 to 1.08 and turns its sun 30 degrees, 500 ms, overshoot interpolator. |
| `nepdate-play-icon-512.png` | The artwork cropped to the visible 17 to 91 units, 512 x 512, full bleed. |
| `nepdate-mobile-128.png` | The same crop at 128 x 128 with rounded corners, for the README. |
| `android/app/src/main/res/mipmap-*/ic_launcher_fg.png` | The foreground layer rendered at 108 dp for each density; the adaptive icon uses these. |
| `android/app/src/main/res/drawable-nodpi/widget_picker_icon.png` | The same crop at 256 x 256 with rounded corners. The widget picker uses it because some launchers draw vector and adaptive icons there as a blank tile. |
