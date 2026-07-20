#!/usr/bin/env python3
"""Generate estimated STL for Husqvarna 547656201 FRAME (spoked guard ring).

Geometry is based on the official 440 iQ IPL diagram UN-778162.png
(callout #13 in BLADE MOTOR AND CUTTING EQUIPMENT).

Usage:
  python3 generate_stl.py
  python3 generate_stl.py --outer-d 198 --center-bore-d 42
"""

from __future__ import annotations

import argparse
import math
from pathlib import Path

import numpy as np
from stl import mesh


def _annulus_prism(
    cx: float,
    cy: float,
    z0: float,
    z1: float,
    inner_r: float,
    outer_r: float,
    segments: int = 72,
) -> np.ndarray:
    angles = np.linspace(0, 2 * math.pi, segments, endpoint=False)
    faces = []
    for i in range(segments):
        j = (i + 1) % segments
        oi, oj = outer_r * math.cos(angles[i]), outer_r * math.sin(angles[i])
        ok, ol = outer_r * math.cos(angles[j]), outer_r * math.sin(angles[j])
        ii, ij = inner_r * math.cos(angles[i]), inner_r * math.sin(angles[i])
        ik, il = inner_r * math.cos(angles[j]), inner_r * math.sin(angles[j])
        faces.append([[cx + oi, cy + oj, z0], [cx + ok, cy + ol, z0], [cx + oi, cy + oj, z1]])
        faces.append([[cx + oi, cy + oj, z1], [cx + ok, cy + ol, z0], [cx + ok, cy + ol, z1]])
        faces.append([[cx + ii, cy + ij, z1], [cx + ik, cy + il, z0], [cx + ii, cy + ij, z0]])
        faces.append([[cx + ii, cy + ij, z1], [cx + ik, cy + il, z1], [cx + ik, cy + il, z0]])
        faces.append([[cx + oi, cy + oj, z1], [cx + ii, cy + ij, z1], [cx + ok, cy + ol, z1]])
        faces.append([[cx + ok, cy + ol, z1], [cx + ii, cy + ij, z1], [cx + ik, cy + il, z1]])
        faces.append([[cx + oi, cy + oj, z0], [cx + ok, cy + ol, z0], [cx + ii, cy + ij, z0]])
        faces.append([[cx + ok, cy + ol, z0], [cx + ik, cy + il, z0], [cx + ii, cy + ij, z0]])
    return np.array(faces, dtype=np.float64)


def _cylinder_faces(cx: float, cy: float, radius: float, z0: float, z1: float, segments: int = 48) -> np.ndarray:
    angles = np.linspace(0, 2 * math.pi, segments, endpoint=False)
    bottom = np.column_stack([
        cx + radius * np.cos(angles),
        cy + radius * np.sin(angles),
        np.full(segments, z0),
    ])
    top = np.column_stack([
        cx + radius * np.cos(angles),
        cy + radius * np.sin(angles),
        np.full(segments, z1),
    ])
    faces = []
    for i in range(segments):
        j = (i + 1) % segments
        faces.append([bottom[i], bottom[j], top[i]])
        faces.append([top[i], bottom[j], top[j]])
    return np.array(faces, dtype=np.float64)


def _tapered_spoke(
    angle_deg: float,
    r_inner: float,
    r_outer: float,
    root_w: float,
    tip_w: float,
    z0: float,
    z1: float,
    segments: int = 16,
) -> np.ndarray:
    """Approximate one tapered spoke as a chain of short cylinders."""
    faces = []
    steps = 10
    rad = math.radians(angle_deg)
    for s in range(steps):
        t0 = s / steps
        t1 = (s + 1) / steps
        r0 = r_inner + (r_outer - r_inner) * t0
        r1 = r_inner + (r_outer - r_inner) * t1
        w0 = root_w + (tip_w - root_w) * t0
        w1 = root_w + (tip_w - root_w) * t1
        cx0, cy0 = r0 * math.cos(rad), r0 * math.sin(rad)
        cx1, cy1 = r1 * math.cos(rad), r1 * math.sin(rad)
        faces.append(_cylinder_faces(cx0, cy0, w0 / 2, z0, z1, segments))
        if s > 0:
            faces.append(_cylinder_faces((cx0 + cx1) / 2, (cy0 + cy1) / 2, (w0 + w1) / 4, z0, z1, segments))
    return np.concatenate(faces, axis=0)


def build_frame_547656201_stl(
    outer_d: float = 196.0,
    rim_width: float = 10.0,
    plate_thick: float = 3.0,
    hub_od: float = 72.0,
    center_bore_d: float = 40.0,
    hub_bore_pcd: float = 52.0,
    hub_bore_d: float = 4.5,
    hub_bore_count: int = 4,
    arm_count: int = 4,
    arm_root_width: float = 24.0,
    arm_tip_width: float = 12.0,
) -> mesh.Mesh:
    """Spoked guard frame from 440 iQ IPL diagram callout #13."""
    outer_r = outer_d / 2
    inner_rim_r = outer_r - rim_width
    hub_r = hub_od / 2

    faces = [
        _annulus_prism(0, 0, 0, plate_thick, inner_rim_r, outer_r),
        _annulus_prism(0, 0, 0, plate_thick, center_bore_d / 2, hub_r),
    ]

    for i in range(arm_count):
        angle = i * 360 / arm_count
        faces.append(
            _tapered_spoke(angle, hub_r + 2, inner_rim_r - 2, arm_root_width, arm_tip_width, 0, plate_thick)
        )

    # Cut center bore and hub holes (add as subtractive cylinders through full height)
    faces.append(_annulus_prism(0, 0, -0.1, plate_thick + 0.1, 0, center_bore_d / 2))
    for i in range(hub_bore_count):
        angle = math.radians(i * 360 / hub_bore_count)
        hx = hub_bore_pcd / 2 * math.cos(angle)
        hy = hub_bore_pcd / 2 * math.sin(angle)
        faces.append(_annulus_prism(hx, hy, -0.1, plate_thick + 0.1, 0, hub_bore_d / 2))

    data = np.concatenate(faces, axis=0)
    m = mesh.Mesh(np.zeros(data.shape[0], dtype=mesh.Mesh.dtype))
    m.vectors = data
    return m


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output-dir", type=Path, default=Path(__file__).parent / "output")
    parser.add_argument("--outer-d", type=float, default=196.0)
    parser.add_argument("--center-bore-d", type=float, default=40.0)
    parser.add_argument("--hub-od", type=float, default=72.0)
    parser.add_argument("--plate-thick", type=float, default=3.0)
    args = parser.parse_args()

    out = args.output_dir
    out.mkdir(parents=True, exist_ok=True)

    frame = build_frame_547656201_stl(
        outer_d=args.outer_d,
        center_bore_d=args.center_bore_d,
        hub_od=args.hub_od,
        plate_thick=args.plate_thick,
    )
    frame_path = out / "frame_547656201_estimated.stl"
    frame.save(str(frame_path))
    print(f"Wrote {frame_path} ({len(frame.vectors)} triangles)")


if __name__ == "__main__":
    main()
