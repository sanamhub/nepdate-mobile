# NepDate app icon

`nepdate-app-icon.svg` is the master artwork. It uses the 108 unit Android adaptive icon grid:
a sindoor (`#B3261E`) background and a paper (`#FFF8F3`) Nepal flag pennant with a sindoor
crescent and 12 point sun.

## Geometry

The design source draws the pennant as `35,22 79,51 52,51 79,86 35,86`. Those corners reach
40.6 units from the centre, so a circular launcher mask would clip them. Every point is mapped
with `x' = 54 + 0.8 * (x - 57)` and `y' = 54 + 0.8 * (y - 54)` (scale 0.8 about the pennant's
bounding box centre, then centred on 54,54). The furthest corner is now 31.1 units from the
centre, inside the 33 unit safe circle.

- Pennant: `36.4,28.4 71.6,51.6 50,51.6 71.6,79.6 36.4,79.6`
- Crescent: circle r 5.6 at 46,44.4 minus circle r 5.6 at 46,41.68, written as one path of
  two arcs between the intersection points 40.57,43.04 and 51.43,43.04.
- Sun: 12 points around 46.8,67.6, outer radius 8, inner radius 5.12, first point straight up.

## Exports

| File | How it was made |
| --- | --- |
| `android/app/src/main/res/drawable/ic_launcher_foreground.xml` | The SVG `foreground` group copied by hand into a vector drawable (same path data). |
| `android/app/src/main/res/values/colors.xml` `ic_launcher_background` | The SVG background colour, used as the adaptive icon background layer. |
| `android/app/src/main/res/drawable/ic_launcher_monochrome.xml` | The three paths joined into one path with `fillType="evenOdd"`, so the crescent and sun are holes in the pennant for themed icons. |
| `android/app/src/main/res/drawable/splash_icon.xml` | The foreground again, with the sun in a group pivoting on 46.8,67.6. |
| `android/app/src/main/res/drawable/splash_icon_animated.xml` | Turns that group 0 to 30 degrees in 500 ms with the overshoot interpolator. |
| `nepdate-play-icon-512.png` | No SVG converter was installed, so a Python script with Pillow drew the same coordinates at 2048 px (4x supersampled), cropped to the centre 84 units of the grid so the mark fits Google Play's 384 px keyline circle, and scaled to 512 px. Re-export from the SVG with any converter if it changes: crop the view box to `12 12 84 84` and render at 512 x 512. |

If the artwork changes, edit the SVG first and copy the path data into every file above.
