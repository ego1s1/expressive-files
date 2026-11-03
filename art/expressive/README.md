# Expressive Files launcher mark

`icon.svg` is the 512x512 master artwork for the Expressive Files launcher icon.

- Background: `#0B57D0` (blue 700), full-bleed rounded square, corner radius 112
  (21.9% of the canvas).
- Folder: `#FFFFFF`.
- Spark: `#D3E3FD` (blue 100). It is decorative; the folder carries the meaning.
- No typographic or logo text.

The artwork mirrors the adaptive-icon foreground (`res/drawable/launcher_icon_foreground.xml`):
the folder is centred, and the four-point spark sits above its top-right corner, entirely on the
blue field so it keeps its contrast.

## Palette and contrast

| Element | Color | Contrast on `#0B57D0` |
|---|---|---|
| Folder | `#FFFFFF` | 6.4:1 |
| Spark | `#D3E3FD` | 4.9:1 |

Both pass the 3:1 requirement for non-text graphics, and the white folder also passes 4.5:1 for
text if it were ever used at small sizes.

## Android sizes

| Asset | Size | Notes |
|---|---|---|
| Adaptive icon canvas | 108 x 108 dp | Foreground and background layers |
| Adaptive icon safe zone | 66 x 66 dp | Keep all meaningful content inside |
| `mipmap-mdpi` | 48 x 48 px | Legacy launcher icon |
| `mipmap-hdpi` | 72 x 72 px | |
| `mipmap-xhdpi` | 96 x 96 px | |
| `mipmap-xxhdpi` | 144 x 144 px | |
| `mipmap-xxxhdpi` | 192 x 192 px | |
| Google Play listing | 512 x 512 px | 32-bit PNG, no transparency |

## Masks

Android applies its own mask to adaptive icons, so do not bake a shape into the adaptive layers:

- Material 3 / Pixel: squircle (rounded square).
- Samsung / older launchers: circle, rounded square, or teardrop.
- The rounded square in `icon.svg` is only for standalone output: web, store listings, and the
  legacy PNG mipmaps.
- When deriving an adaptive foreground from this art, scale the folder and spark into the central
  66 x 66 dp safe zone and fill the background layer with `#0B57D0`.
- The monochrome layer should contain the folder and spark as a single `#000000` fill on a
  transparent background; the system tints it.

The mark is composed to survive a circular crop: the folder sits in the middle of the canvas and
the spark is offset toward the top-right but still well inside the central area.

## Exporting

```sh
# Master
rsvg-convert -w 512 -h 512 art/expressive/icon.svg -o launcher_icon-512.png

# Legacy mipmaps
for size in 48 72 96 144 192; do
  rsvg-convert -w "$size" -h "$size" art/expressive/icon.svg -o "launcher_icon-${size}.png"
done
```

Do not add text or a wordmark. Keep at least 32 px of clear space around the folder when composing
store or banner artwork from this mark.
