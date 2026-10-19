#!/usr/bin/env python3
"""Frame the Roborazzi store screenshots for Play.

The Roborazzi images start with a red "Not verified on a device" test label. The label is not
store copy, so this script crops it before framing. The frame is drawn here, not in a design tool,
so the checked-in images can be regenerated from the repository.

Run from the repository root:

    python tools/frame_store_screenshots.py
"""

from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "android" / "app" / "src" / "test" / "screenshots"
DEST = ROOT / "fastlane" / "metadata" / "android"
CROP_TOP = 56
CANVAS = (1080, 1920)
BACKGROUND = "#F3EFE7"
BEZEL = "#181A1F"
BORDER = "#3A3D44"
SCREEN = "#0B0C0E"

SELECTIONS = {
    "en-US": [
        ("a1-month-light.png", "01-month"),
        ("a1-day-detail-light.png", "02-day-detail"),
        ("a1-upcoming-light.png", "03-upcoming"),
        ("a1-converter-light.png", "04-converter"),
        ("a1-month-tithi.png", "05-tithi"),
        ("a1-month-font-scale-200.png", "06-large-text"),
    ],
    "ne-NP": [
        ("a1-month-ne.png", "01-month"),
        ("a1-day-detail-ne.png", "02-day-detail"),
        ("a1-upcoming-ne.png", "03-upcoming"),
        ("a1-converter-ne.png", "04-converter"),
    ],
}


def rounded_mask(size: tuple[int, int], radius: int) -> Image.Image:
    mask = Image.new("L", size, 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, size[0] - 1, size[1] - 1), radius=radius, fill=255)
    return mask


def frame(source: Path) -> Image.Image:
    image = Image.open(source).convert("RGB")
    if CROP_TOP:
        image = image.crop((0, CROP_TOP, image.width, image.height))

    canvas = Image.new("RGB", CANVAS, BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    draw.rounded_rectangle((88, 42, 992, 1878), radius=76, fill=BEZEL, outline=BORDER, width=2)
    draw.rounded_rectangle((120, 78, 960, 1842), radius=40, fill=SCREEN)

    max_width = 840
    max_height = 1764
    scale = min(max_width / image.width, max_height / image.height)
    width = round(image.width * scale)
    height = round(image.height * scale)
    image = image.resize((width, height), Image.Resampling.LANCZOS)
    left = 120 + (max_width - width) // 2
    top = 78 + (max_height - height) // 2
    canvas.paste(image, (left, top), rounded_mask((width, height), 32))
    return canvas


def main() -> None:
    for locale, files in SELECTIONS.items():
        output = DEST / locale / "images" / "phoneScreenshots"
        output.mkdir(parents=True, exist_ok=True)
        for source_name, output_name in files:
            source = SOURCE / source_name
            if not source.is_file():
                raise SystemExit(f"missing screenshot: {source}")
            image = frame(source)
            target = output / f"{output_name}.png"
            image.save(target, format="PNG", optimize=True)
            print(f"{target.relative_to(ROOT)} {image.width}x{image.height}")


if __name__ == "__main__":
    main()
