# NepDate app icon

`nepdate-app-icon.svg` is the master artwork. It uses the 108 unit Android adaptive icon grid:
a sindoor (`#B3261E`) background, the Nepal flag pennant as a deeper red shade (`#8C1D18`), and a
marigold (`#E8A33D`) twelve ray sun holding the Devanagari numeral one in ink (`#271900`). The sun
is the same today marker the app and widgets use, so the icon reads as "the date" first and
"Nepal" second.

The numeral is fixed. Standard Android launchers cannot redraw a third-party app icon every day.

## Geometry

- Pennant shade: the flag pennant `36.4,28.4 71.6,51.6 50,51.6 71.6,79.6 36.4,79.6`, scaled
  1.32 about 54,54 and moved by -4,-2. It runs past the safe circle on purpose: it is a tone
  behind the sun, and launcher masks crop it.
- Sun: 12 lobes around 56,58, outer radius 24, inner radius 0.86 of that, sampled 12 points per
  lobe. It sits inside the 33 unit safe circle.
- Numeral: the glyph `१` from Noto Sans Devanagari at weight 800, scaled to 22 units tall and
  centred on 56,58.

## Exports

| File | How it was made |
| --- | --- |
| `android/app/src/main/res/drawable/ic_launcher_foreground.xml` | The three SVG paths, with colours from `ic_launcher_shade`, `ic_launcher_sun` and `ic_launcher_ink`. |
| `android/app/src/main/res/values/colors.xml` `ic_launcher_background` | The SVG background colour, used as the adaptive icon background layer. |
| `android/app/src/main/res/drawable/ic_launcher_monochrome.xml` | The sun and numeral as one path with `fillType="evenOdd"`, so the numeral is a hole in the sun for themed icons. |
| `android/app/src/main/res/drawable/splash_icon.xml` | The foreground again, with the sun in a group pivoting on 56,58 and the numeral outside it so it stays upright. |
| `android/app/src/main/res/drawable/splash_icon_animated.xml` | Turns the sun group 0 to 30 degrees in 500 ms with the overshoot interpolator. |
| `nepdate-play-icon-512.png` | The SVG with its view box cropped to `12 12 84 84`, rendered at 512 x 512 in a headless browser. |
