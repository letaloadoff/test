# Automower 440 iQ — Cutting Frame 3D Print Models

Parametric starting models for the **Blade Motor & Cutting Equipment** parts on the [Husqvarna 440 iQ support page](https://www.husqvarna.com/us/support/automower-440-iq/?highlightId=547656201-11).

Husqvarna does **not** publish OEM CAD/STL for these parts. These files are **estimated templates** you can measure, tune, and modify in OpenSCAD or your slicer.

## Which part is highlighted?

Your link (`highlightId=547656201-11`) points to:

| Item | OEM part | Name | Material |
|------|----------|------|----------|
| **Primary (your link)** | **547 65 62-01** (`547656201`) | Frame sealing bellow | Flexible rubber (print **TPU 95A**) |
| Related structural part | **599 31 82-01** (`599318201`) | Motor housing frame (IPL ref 15) | Rigid plastic (print **PETG** or **ASA**) |

On the shared P16 cutting platform (405X / 415X / 440 iQ), IPL ref **15** is the rigid **FRAME** (`599318201`). Ref **11** on the 405X IPL is the motor assembly; the 440 iQ page uses ref **11** for part `547656201`.

**Important:** The highlighted part is a **flexible seal**, not the square plastic motor frame. If you want to modify mounting geometry, you likely want `frame_599318201.scad`. If you want to replace or stiffen the rubber boot, use `bellow_547656201.scad`.

## Files

| File | Description |
|------|-------------|
| `bellow_547656201.scad` | Parametric accordion bellow (OEM 547656201) |
| `frame_599318201.scad` | Parametric motor housing plate (OEM 599318201) |
| `generate_stl.py` | Python STL exporter (no OpenSCAD required) |
| `output/*.stl` | Pre-generated estimated meshes |

## Quick start

### Option A — OpenSCAD (recommended for edits)

1. Install [OpenSCAD](https://openscad.org/).
2. Open the `.scad` file for the part you need.
3. Adjust the variables at the top (dimensions in mm).
4. Render (`F6`) → Export STL.

### Option B — Python

```bash
pip install numpy-stl
python3 generate_stl.py
```

Custom sizes:

```bash
python3 generate_stl.py --frame-plate-size 112 --bellow-height 30 --bellow-top-id 54
```

## Calibrating dimensions (required for fit)

Measure your **OEM part** with calipers and update the SCAD variables:

### Frame (`599318201`)

Known retailer packaging: **110 × 110 × 9 mm**, ~25 g.

| Variable | Default | Measure |
|----------|---------|---------|
| `plate_size` | 110 mm | Outer square width/length |
| `plate_thick` | 9 mm | Plate thickness |
| `motor_bore_d` | 48 mm | Central motor opening |
| `disc_screw_pcd` | 35 mm | Bolt-circle diameter for 3 disc screws |
| `disc_screw_d` | 4.4 mm | Clearance for M4 (590 50 87-01) |
| `corner_inset` / slots | 10 / 12 / 6 mm | Chassis mounting slots |

### Bellow (`547656201`)

No published OEM dimensions — defaults are **estimates**.

| Variable | Default | Measure |
|----------|---------|---------|
| `top_id` | 52 mm | Inner diameter at motor side |
| `bottom_id` | 58 mm | Inner diameter at frame side |
| `height` | 28 mm | Compressed installed height |
| `wall` | 2.0 mm | Wall thickness (TPU) |
| `conv_loops` | 5 | Number of accordion folds |

**Tip:** Photograph the part next to a ruler, then iterate one dimension at a time.

## Print settings

| Part | Material | Notes |
|------|----------|-------|
| Frame `599318201` | PETG or ASA | 3 perimeters, 30% infill, print flat on bed |
| Bellow `547656201` | TPU 95A | Slow (25–35 mm/s), no fan, 0.2 mm layers, vase/standard |

## Related OEM parts (440 iQ / P16 platform)

| Part No. | Description |
|----------|-------------|
| 599 52 13-01 | Cutting module assembly |
| 547 05 29-01 | Cutting motor assembly (iQ service part) |
| 599 31 82-01 | Frame (structural) |
| 547 65 62-01 | Frame sealing bellow |
| 599 67 47-01 | Cutting disc (240 mm) |
| 598 79 49-01 | Skid plate (Ø211 mm) |

## Safety / warranty

Modifying cutting-system seals or frames can affect **IPX5** sealing, blade balance, and **warranty**. Test carefully before extended mowing.

## Sources

- [440 iQ support / spare parts](https://www.husqvarna.com/us/support/automower-440-iq/)
- [405X IPL (P16 cutting module)](https://www-static-nw.husqvarna.com/hbd/tdrdownload/v2/pub000081076/doc000147125/IPL/EUIWzyYcuKYBrN73tt0rQv898uc?httproute=True)
- Frame `599318201` packaging: 110×110×9 mm (roboticmowing.com.au / wolfswinkel.at)
