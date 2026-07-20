# Automower 440 iQ — Part 547656201 (FRAME) 3D Print Model

Parametric model for **OEM 547 65 62-01** (`547656201`), highlighted on the [Husqvarna 440 iQ support page](https://www.husqvarna.com/us/support/automower-440-iq/?highlightId=547656201-11).

## What this part actually is

Husqvarna's product text calls it a **"Frame Sealing Bellow"**, but the official IPL drawing shows a **thin spoked guard ring** above the cutting disc — not an accordion rubber boot.

| Source | Name / callout |
|--------|----------------|
| IPL diagram | **FRAME** — callout **#13** |
| Support-page highlight URL | `547656201-11` (internal map id; same part) |
| Spare-parts page | "FRAME Sealing Bellow" |

Official diagram image (from Husqvarna spare-parts data):

- Full sheet: `reference/ipl-blade-motor-UN-778162.png` (downloaded from Husqvarna CDN)
- Close-up of callout #13: `reference/part13-frame-closeup.png`
- Annotated overview: `reference/blade-motor-annotated.png`

The part sits in the blade stack above cutting disc **599674701** (240 mm) and mates to the hub of disc **#6** in the diagram.

## Files

| File | Purpose |
|------|---------|
| `frame_547656201.scad` | **Main model** — spoked guard frame (edit variables at top) |
| `generate_stl.py` | Python STL exporter |
| `output/frame_547656201_estimated.stl` | Pre-generated mesh |
| `frame_599318201.scad` | *Different part* — square motor plate from 405X/415X platform (not 547656201) |
| `reference/` | IPL diagram crops for visual reference |

## Quick start

### OpenSCAD (recommended)

1. Open `frame_547656201.scad`
2. Adjust dimensions at the top (mm)
3. Render (`F6`) → Export STL

### Python

```bash
pip install -r requirements.txt
python3 generate_stl.py
python3 generate_stl.py --outer-d 198 --center-bore-d 42
```

## Default dimensions (estimated — calibrate!)

Derived from IPL diagram **UN-778162** proportions vs. known 240 mm cutting disc:

| Variable | Default | What to measure on OEM part |
|----------|---------|------------------------------|
| `outer_d` | 196 mm | Outer rim diameter |
| `rim_width` | 10 mm | Outer annulus width |
| `plate_thick` | 3.0 mm | Overall thickness |
| `hub_od` | 72 mm | Central hub outer diameter |
| `center_bore_d` | 40 mm | Center shaft bore |
| `hub_bore_pcd` / `hub_bore_d` | 52 / 4.5 mm | Bolt circle + hole size |
| `arm_count` | 4 | Number of spokes (fixed at 4 on OEM) |
| `arm_root_width` / `arm_tip_width` | 24 / 12 mm | Spoke width at hub / rim |

**You must measure your OEM part** (or 3D-scan it) before printing for final fit. Husqvarna does not publish CAD.

## Print settings

| Setting | Value |
|---------|-------|
| Material | PETG or ASA |
| Orientation | Flat on the bed (hub face down) |
| Layers | 0.2 mm |
| Perimeters | 3+ |
| Infill | 25–35% |

For a flexible replacement closer to the OEM rubber, try TPU — but the diagram geometry is a rigid spoked ring, not a bellow.

## Related parts (same IPL)

| Callout | Part No. | Name |
|---------|----------|------|
| 6 | 599674701 | Cutting disc (240 mm) |
| 7 | 598794901 | Skid plate (Ø211 mm) |
| 13 | **547656201** | **FRAME** (this model) |
| 19 | 547053301 | Worm gear |
| 21 | 548414101 | Cutting module |
| 23 | 547052701 | Housing |

## Safety

Modifying cutting-system parts can affect blade balance, sealing, and warranty. Test carefully before extended use.
