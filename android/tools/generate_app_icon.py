"""Generates the NepDate Patro app icon and every export from one geometry.

The mark is a calendar page on sindoor: a cream page with an ink-red header and two binder rings,
and the marigold Sunny shape, the app's "today" marker, in the middle of the page, with the NepDate
mark small in the sun: the NepDate logo's disc and pennant line, without its moon and sun, which
would not survive the size. It carries no
numeral, so it never reads as a wrong date in the status bar or the notification shade, and it
sits in the same family as the Mitibar desktop icon (a calendar page on a red tile).

Run from the repository root:

    python android/tools/generate_app_icon.py
"""

import math
import pathlib

from PIL import Image, ImageDraw

ROOT = pathlib.Path(".")
RES = ROOT / "android/app/src/main/res"
ASSETS = ROOT / "docs/assets"

GRID = 108.0
BACKGROUND = "#B3261E"
HEADER = "#7A1712"
PAGE = "#FBF3E4"
SUN = "#E8A33D"

# The page stays inside the 33 unit safe circle of the adaptive grid.
PAGE_BOX = (32.0, 33.0, 76.0, 79.0)
PAGE_RADIUS = 7.0
HEADER_BOTTOM = 45.0
RINGS = [(43.0, 29.5), (65.0, 29.5)]
RING_SIZE = (3.6, 8.0)
SUN_CENTRE = (54.0, 62.0)
SUN_OUTER = 13.0
SUN_INNER_SHARE = 0.86
SUN_LOBES = 12

# The NepDate logo's disc and pennant line, in the logo's 936 unit grid (centre 468, disc radius
# 447). The line is a filled outline so the monochrome icon can cut it out of the disc.
LOGO_CENTRE = 468.0
LOGO_RADIUS = 447.0
PENNANT = [(220.0, 100.0), (830.0, 458.0), (296.0, 447.0), (712.0, 850.0)]
MARK_RADIUS = 8.4
MARK_STROKE = 1.4
# A corner sharper than this many half-strokes is bevelled, so the pennant tip stays in the disc.
MITER_LIMIT = 2.2


def sun_points() -> list[tuple[float, float]]:
    points = []
    steps = SUN_LOBES * 12
    for i in range(steps):
        theta = 2 * math.pi * i / steps - math.pi / 2
        inner = SUN_OUTER * SUN_INNER_SHARE
        radius = inner + (SUN_OUTER - inner) * (0.5 + 0.5 * math.cos(SUN_LOBES * theta))
        points.append((SUN_CENTRE[0] + radius * math.cos(theta), SUN_CENTRE[1] + radius * math.sin(theta)))
    return points


def mark_scale() -> float:
    return MARK_RADIUS / LOGO_RADIUS


def disc_path() -> str:
    """The logo's disc, filled in the page colour."""
    cx, cy = SUN_CENTRE
    r = MARK_RADIUS
    return f"M{cx - r:.2f},{cy:.2f}A{r:.2f},{r:.2f} 0,1 1 {cx + r:.2f},{cy:.2f}A{r:.2f},{r:.2f} 0,1 1 {cx - r:.2f},{cy:.2f}Z"


def clip_to_ring(a, b, limit):
    """Moves a segment's outer end [a] onto the circle of radius [limit] round the sun centre."""
    cx, cy = SUN_CENTRE
    dx, dy = a[0] - b[0], a[1] - b[1]
    fx, fy = b[0] - cx, b[1] - cy
    qa = dx * dx + dy * dy
    qb = 2 * (fx * dx + fy * dy)
    qc = fx * fx + fy * fy - limit * limit
    t = (-qb + math.sqrt(qb * qb - 4 * qa * qc)) / (2 * qa)
    return (b[0] + t * dx, b[1] + t * dy)


def pennant_points() -> list[tuple[float, float]]:
    """The pennant line inside the ring, outlined at MARK_STROKE with mitred corners."""
    scale = mark_scale()
    line = [
        (SUN_CENTRE[0] + (x - LOGO_CENTRE) * scale, SUN_CENTRE[1] + (y - LOGO_CENTRE) * scale) for x, y in PENNANT
    ]
    # The line ends at the disc's edge, as it does in the logo.
    line[0] = clip_to_ring(line[0], line[1], MARK_RADIUS - MARK_STROKE / 2)
    line[-1] = clip_to_ring(line[-1], line[-2], MARK_RADIUS - MARK_STROKE / 2)
    half = MARK_STROKE / 2

    def normal(a, b):
        dx, dy = b[0] - a[0], b[1] - a[1]
        length = math.hypot(dx, dy)
        return (-dy / length, dx / length)

    def side(sign):
        out = []
        for i, point in enumerate(line):
            if i == 0 or i == len(line) - 1:
                n = normal(line[0], line[1]) if i == 0 else normal(line[-2], line[-1])
                out.append((point[0] + sign * half * n[0], point[1] + sign * half * n[1]))
                continue
            n1 = normal(line[i - 1], point)
            n2 = normal(point, line[i + 1])
            mx, my = n1[0] + n2[0], n1[1] + n2[1]
            length = math.hypot(mx, my)
            mx, my = mx / length, my / length
            miter = half / (mx * n1[0] + my * n1[1])
            # The outer side of the turn is the one opposite the way the line turns.
            turn = (point[0] - line[i - 1][0]) * (line[i + 1][1] - point[1]) - (point[1] - line[i - 1][1]) * (
                line[i + 1][0] - point[0]
            )
            outer = -1 if turn > 0 else 1
            if sign == outer and miter > MITER_LIMIT * half:
                out.append((point[0] + sign * half * n1[0], point[1] + sign * half * n1[1]))
                out.append((point[0] + sign * half * n2[0], point[1] + sign * half * n2[1]))
            else:
                out.append((point[0] + sign * miter * mx, point[1] + sign * miter * my))
        return out

    return side(1) + list(reversed(side(-1)))


def rounded_rect_path(x0, y0, x1, y1, r, top=True, bottom=True) -> str:
    rt = r if top else 0
    rb = r if bottom else 0
    return (
        f"M{x0 + rt:.2f},{y0:.2f}H{x1 - rt:.2f}"
        + (f"A{rt},{rt} 0,0 1 {x1:.2f},{y0 + rt:.2f}" if rt else "")
        + f"V{y1 - rb:.2f}"
        + (f"A{rb},{rb} 0,0 1 {x1 - rb:.2f},{y1:.2f}" if rb else "")
        + f"H{x0 + rb:.2f}"
        + (f"A{rb},{rb} 0,0 1 {x0:.2f},{y1 - rb:.2f}" if rb else "")
        + f"V{y0 + rt:.2f}"
        + (f"A{rt},{rt} 0,0 1 {x0 + rt:.2f},{y0:.2f}" if rt else "")
        + "Z"
    )


def polygon_path(points) -> str:
    head, *rest = points
    return f"M{head[0]:.2f},{head[1]:.2f}" + "".join(f"L{x:.2f},{y:.2f}" for x, y in rest) + "Z"


def ring_paths() -> list[str]:
    w, h = RING_SIZE
    return [rounded_rect_path(x - w / 2, y, x + w / 2, y + h, w / 2) for x, y in RINGS]


def layers() -> dict[str, str]:
    x0, y0, x1, y1 = PAGE_BOX
    return {
        "page": rounded_rect_path(x0, y0, x1, y1, PAGE_RADIUS),
        "header": rounded_rect_path(x0, y0, x1, HEADER_BOTTOM, PAGE_RADIUS, bottom=False),
        "rings": "".join(ring_paths()),
        "sun": polygon_path(sun_points()),
        "disc": disc_path(),
        "mark": polygon_path(pennant_points()),
    }


def write_svg(paths: dict[str, str]) -> None:
    svg = f"""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108">
  <!-- Generated by android/tools/generate_app_icon.py. -->
  <rect width="108" height="108" fill="{BACKGROUND}"/>
  <path d="{paths['page']}" fill="{PAGE}"/>
  <path d="{paths['header']}" fill="{HEADER}"/>
  <path d="{paths['rings']}" fill="{PAGE}"/>
  <path d="{paths['sun']}" fill="{SUN}"/>
  <path d="{paths['disc']}" fill="{PAGE}"/>
  <path d="{paths['mark']}" fill="{HEADER}"/>
</svg>
"""
    (ASSETS / "nepdate-app-icon.svg").write_text(svg, encoding="utf-8")


VECTOR = """<?xml version="1.0" encoding="utf-8"?>
<!-- Generated by android/tools/generate_app_icon.py; see docs/assets/README-icon.md. -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
{paths}</vector>
"""


def vector_path(data: str, color: str, even_odd: bool = False) -> str:
    fill_type = '\n        android:fillType="evenOdd"' if even_odd else ""
    return f'    <path\n        android:fillColor="{color}"{fill_type}\n        android:pathData="{data}" />\n'


def write_vectors(paths: dict[str, str]) -> None:
    foreground = (
        vector_path(paths["page"], "@color/ic_launcher_page")
        + vector_path(paths["header"], "@color/ic_launcher_header")
        + vector_path(paths["rings"], "@color/ic_launcher_page")
        + vector_path(paths["sun"], "@color/ic_launcher_sun")
        + vector_path(paths["disc"], "@color/ic_launcher_page")
        + vector_path(paths["mark"], "@color/ic_launcher_header")
    )
    (RES / "drawable/ic_launcher_foreground.xml").write_text(VECTOR.format(paths=foreground), encoding="utf-8")
    # Themed icons take one colour: the page and rings as the shape, the sun as a hole in it, the
    # disc filled again inside the hole and the pennant a hole in the disc.
    mono = vector_path(paths["page"] + paths["rings"] + paths["sun"] + paths["disc"] + paths["mark"], "#FFFFFFFF", even_odd=True)
    (RES / "drawable/ic_launcher_monochrome.xml").write_text(VECTOR.format(paths=mono), encoding="utf-8")


def raster(size: int, crop: tuple[float, float, float, float], background: bool, mask: str | None) -> Image.Image:
    """Draws the icon at 4x and scales down, which is enough anti-aliasing for flat shapes."""
    scale = 4
    cx0, cy0, cx1, cy1 = crop
    px = size * scale / (cx1 - cx0)

    def pt(x, y):
        return ((x - cx0) * px, (y - cy0) * px)

    img = Image.new("RGBA", (size * scale, size * scale), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    if background:
        draw.rectangle([0, 0, size * scale, size * scale], fill=BACKGROUND)
    x0, y0, x1, y1 = PAGE_BOX
    r = PAGE_RADIUS * px
    draw.rounded_rectangle([*pt(x0, y0), *pt(x1, y1)], radius=r, fill=PAGE)
    draw.rounded_rectangle([*pt(x0, y0), *pt(x1, HEADER_BOTTOM)], radius=r, fill=HEADER, corners=(True, True, False, False))
    w, h = RING_SIZE
    for x, y in RINGS:
        draw.rounded_rectangle([*pt(x - w / 2, y), *pt(x + w / 2, y + h)], radius=w / 2 * px, fill=PAGE)
    draw.polygon([pt(x, y) for x, y in sun_points()], fill=SUN)
    cx, cy = SUN_CENTRE
    draw.ellipse([*pt(cx - MARK_RADIUS, cy - MARK_RADIUS), *pt(cx + MARK_RADIUS, cy + MARK_RADIUS)], fill=PAGE)
    draw.polygon([pt(x, y) for x, y in pennant_points()], fill=HEADER)
    img = img.resize((size, size), Image.LANCZOS)
    if mask:
        m = Image.new("L", (size * scale, size * scale), 0)
        md = ImageDraw.Draw(m)
        if mask == "circle":
            md.ellipse([0, 0, size * scale - 1, size * scale - 1], fill=255)
        else:
            md.rounded_rectangle([0, 0, size * scale - 1, size * scale - 1], radius=size * scale * 0.23, fill=255)
        img.putalpha(Image.composite(img.getchannel("A"), Image.new("L", img.size, 0), m.resize((size, size), Image.LANCZOS)))
    return img


def write_rasters() -> None:
    full = (0.0, 0.0, GRID, GRID)
    for folder, size in {"mdpi": 108, "hdpi": 162, "xhdpi": 216, "xxhdpi": 324, "xxxhdpi": 432}.items():
        raster(size, full, background=False, mask=None).save(RES / f"mipmap-{folder}/ic_launcher_fg.png", optimize=True)
    # Launchers show about the middle 72 units of the 108 unit grid, so the flat tiles crop to it.
    tile = (17.0, 17.0, 91.0, 91.0)
    raster(256, tile, background=True, mask="squircle").save(RES / "drawable-nodpi/widget_picker_icon.png", optimize=True)
    raster(512, tile, background=True, mask=None).save(ASSETS / "nepdate-play-icon-512.png", optimize=True)
    raster(128, tile, background=True, mask="squircle").save(ASSETS / "nepdate-mobile-128.png", optimize=True)


# iOS takes one 1024 square per appearance (iOS 18 and later). Light is opaque and full bleed; dark
# is the same artwork on a transparent ground, because the system supplies the dark background;
# tinted is grayscale, which the system then tints.
IOS_ICON_SET = ROOT / "ios/Resources/Assets.xcassets/AppIcon.appiconset"

IOS_CONTENTS = """{
  "images" : [
    {
      "filename" : "AppIcon-Light.png",
      "idiom" : "universal",
      "platform" : "ios",
      "size" : "1024x1024"
    },
    {
      "appearances" : [
        {
          "appearance" : "luminosity",
          "value" : "dark"
        }
      ],
      "filename" : "AppIcon-Dark.png",
      "idiom" : "universal",
      "platform" : "ios",
      "size" : "1024x1024"
    },
    {
      "appearances" : [
        {
          "appearance" : "luminosity",
          "value" : "tinted"
        }
      ],
      "filename" : "AppIcon-Tinted.png",
      "idiom" : "universal",
      "platform" : "ios",
      "size" : "1024x1024"
    }
  ],
  "info" : {
    "author" : "xcode",
    "version" : 1
  }
}
"""


def write_ios_icons() -> None:
    IOS_ICON_SET.mkdir(parents=True, exist_ok=True)
    # An iOS icon is full bleed, with no adaptive safe area, so it uses the same crop as the store tile.
    tile = (17.0, 17.0, 91.0, 91.0)
    raster(1024, tile, background=True, mask=None).convert("RGB").save(
        IOS_ICON_SET / "AppIcon-Light.png", optimize=True
    )
    raster(1024, tile, background=False, mask=None).save(IOS_ICON_SET / "AppIcon-Dark.png", optimize=True)
    tinted = raster(1024, tile, background=True, mask=None).convert("L").convert("RGBA")
    tinted.save(IOS_ICON_SET / "AppIcon-Tinted.png", optimize=True)
    (IOS_ICON_SET / "Contents.json").write_text(IOS_CONTENTS, encoding="utf-8")


if __name__ == "__main__":
    shapes = layers()
    write_svg(shapes)
    write_vectors(shapes)
    write_rasters()
    write_ios_icons()
