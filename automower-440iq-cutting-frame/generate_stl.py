#!/usr/bin/env python3
"""Generate estimated STL meshes for Automower 440 iQ cutting-frame parts.

No OEM CAD is published by Husqvarna. Defaults are derived from:
  - 599318201 packaging: 110 x 110 x 9 mm (retailer logistics data)
  - 405X/415X IPL blade-motor layout (3x M4 disc screws, 240 mm disc, 211 mm skid)
  - 547656201: flexible frame sealing bellow (dimensions estimated; calibrate!)

Usage:
  python3 generate_stl.py
  python3 generate_stl.py --frame-plate-size 112 --bellow-height 30
"""

from __future__ import annotations

import argparse
import math
from pathlib import Path

import numpy as np
from stl import mesh


def _cylinder_faces(
    center: tuple[float, float, float],
    radius: float,
    z0: float,
    z1: float,
    segments: int = 48,
) -> np.ndarray:
  """Return triangular faces for a vertical cylinder shell (no caps)."""
  cx, cy, _ = center
  angles = np.linspace(0, 2 * math.pi, segments, endpoint=False)
  bottom = np.column_stack([cx + radius * np.cos(angles), cy + radius * np.sin(angles), np.full(segments, z0)])
  top = np.column_stack([cx + radius * np.cos(angles), cy + radius * np.sin(angles), np.full(segments, z1)])
  faces = []
  for i in range(segments):
    j = (i + 1) % segments
    faces.append([bottom[i], bottom[j], top[i]])
    faces.append([top[i], bottom[j], top[j]])
  return np.array(faces, dtype=np.float64)


def _box_faces(x0: float, y0: float, z0: float, x1: float, y1: float, z1: float) -> np.ndarray:
  corners = np.array([
      [x0, y0, z0], [x1, y0, z0], [x1, y1, z0], [x0, y1, z0],
      [x0, y0, z1], [x1, y0, z1], [x1, y1, z1], [x0, y1, z1],
  ], dtype=np.float64)
  tris = [
      (0, 1, 2), (0, 2, 3),  # bottom
      (4, 6, 5), (4, 7, 6),  # top
      (0, 4, 5), (0, 5, 1),
      (1, 5, 6), (1, 6, 2),
      (2, 6, 7), (2, 7, 3),
      (3, 7, 4), (3, 4, 0),
  ]
  return np.array([[corners[a], corners[b], corners[c]] for a, b, c in tris], dtype=np.float64)


def _annulus_prism(
    cx: float,
    cy: float,
    z0: float,
    z1: float,
    inner_r: float,
    outer_r: float,
    segments: int = 64,
) -> np.ndarray:
  angles = np.linspace(0, 2 * math.pi, segments, endpoint=False)
  faces = []
  for i in range(segments):
    j = (i + 1) % segments
    oi, oj = (outer_r * math.cos(angles[i]), outer_r * math.sin(angles[i]))
    ok, ol = (outer_r * math.cos(angles[j]), outer_r * math.sin(angles[j]))
    ii, ij = (inner_r * math.cos(angles[i]), inner_r * math.sin(angles[i]))
    ik, il = (inner_r * math.cos(angles[j]), inner_r * math.sin(angles[j]))
  # outer wall
    faces.append([[cx + oi, cy + oj, z0], [cx + ok, cy + ol, z0], [cx + oi, cy + oj, z1]])
    faces.append([[cx + oi, cy + oj, z1], [cx + ok, cy + ol, z0], [cx + ok, cy + ol, z1]])
  # inner wall (reversed)
    faces.append([[cx + ii, cy + ij, z1], [cx + ik, cy + il, z0], [cx + ii, cy + ij, z0]])
    faces.append([[cx + ii, cy + ij, z1], [cx + ik, cy + il, z1], [cx + ik, cy + il, z0]])
  # top ring
    faces.append([[cx + oi, cy + oj, z1], [cx + ii, cy + ij, z1], [cx + ok, cy + ol, z1]])
    faces.append([[cx + ok, cy + ol, z1], [cx + ii, cy + ij, z1], [cx + ik, cy + il, z1]])
  # bottom ring
    faces.append([[cx + oi, cy + oj, z0], [cx + ok, cy + ol, z0], [cx + ii, cy + ij, z0]])
    faces.append([[cx + ok, cy + ol, z0], [cx + ik, cy + il, z0], [cx + ii, cy + ij, z0]])
  return np.array(faces, dtype=np.float64)


def build_frame_stl(
    plate_size: float = 110.0,
    plate_thick: float = 9.0,
    motor_bore_d: float = 48.0,
    disc_screw_pcd: float = 35.0,
    disc_screw_d: float = 4.4,
    corner_inset: float = 10.0,
    corner_slot_len: float = 12.0,
    corner_slot_w: float = 6.0,
) -> mesh.Mesh:
  """Approximate 599318201 motor housing frame plate."""
  cx = cy = plate_size / 2
  faces = [_box_faces(0, 0, 0, plate_size, plate_size, plate_thick)]

  # Motor bore (through hole)
  faces.append(_annulus_prism(cx, cy, -0.1, plate_thick + 0.1, 0, motor_bore_d / 2))

  # 3x disc screw holes
  for i in range(3):
    angle = math.radians(i * 120)
    sx = cx + disc_screw_pcd * math.cos(angle)
    sy = cy + disc_screw_pcd * math.sin(angle)
    faces.append(_annulus_prism(sx, sy, -0.1, plate_thick + 0.1, 0, disc_screw_d / 2))

  # Corner slots
  slots = [
      (corner_inset - corner_slot_w / 2, corner_inset - corner_slot_len / 2),
      (corner_inset - corner_slot_w / 2, plate_size - corner_inset - corner_slot_len / 2),
      (plate_size - corner_inset - corner_slot_w / 2, corner_inset - corner_slot_len / 2),
      (plate_size - corner_inset - corner_slot_w / 2, plate_size - corner_inset - corner_slot_len / 2),
  ]
  for x0, y0 in slots:
    faces.append(_box_faces(x0, y0, -0.1, x0 + corner_slot_w, y0 + corner_slot_len, plate_thick + 0.1))

  data = np.concatenate(faces, axis=0)
  m = mesh.Mesh(np.zeros(data.shape[0], dtype=mesh.Mesh.dtype))
  m.vectors = data
  return m


def build_bellow_stl(
    top_id: float = 52.0,
    bottom_id: float = 58.0,
    height: float = 28.0,
    wall: float = 2.0,
    conv_loops: int = 5,
    lip_top: float = 3.0,
    lip_bottom: float = 3.0,
    lip_thick: float = 1.5,
) -> mesh.Mesh:
  """Approximate 547656201 accordion bellow (TPU print)."""
  faces = []
  z = 0.0

  def add_ring(z0: float, z1: float, inner_id: float, extra: float = 0.0) -> None:
    nonlocal faces
    outer_r = inner_id / 2 + wall + extra
    inner_r = inner_id / 2
    faces.append(_annulus_prism(0, 0, z0, z1, inner_r, outer_r))

  add_ring(z, z + lip_bottom, bottom_id, lip_thick)
  z += lip_bottom

  steps = conv_loops * 2
  seg_h = height / steps
  for i in range(steps):
    t0 = i / steps
    t1 = (i + 1) / steps
    id0 = bottom_id + (top_id - bottom_id) * t0
    id1 = bottom_id + (top_id - bottom_id) * t1
    bulge = 4.0 if i % 2 == 0 else 0.0
    z0 = z + i * seg_h
    z1 = z + (i + 1) * seg_h
    outer0 = id0 / 2 + wall + bulge
    outer1 = id1 / 2 + wall + (4.0 if (i + 1) % 2 == 0 else 0.0)
    faces.append(_cylinder_faces((0, 0, 0), (outer0 + outer1) / 2, z0, z1))

  z += height
  add_ring(z, z + lip_top, top_id, lip_thick)

  data = np.concatenate(faces, axis=0)
  m = mesh.Mesh(np.zeros(data.shape[0], dtype=mesh.Mesh.dtype))
  m.vectors = data
  return m


def main() -> None:
  parser = argparse.ArgumentParser(description=__doc__)
  parser.add_argument("--output-dir", type=Path, default=Path(__file__).parent / "output")
  parser.add_argument("--frame-plate-size", type=float, default=110.0)
  parser.add_argument("--frame-thick", type=float, default=9.0)
  parser.add_argument("--bellow-height", type=float, default=28.0)
  parser.add_argument("--bellow-top-id", type=float, default=52.0)
  parser.add_argument("--bellow-bottom-id", type=float, default=58.0)
  args = parser.parse_args()

  out = args.output_dir
  out.mkdir(parents=True, exist_ok=True)

  frame = build_frame_stl(plate_size=args.frame_plate_size, plate_thick=args.frame_thick)
  frame_path = out / "frame_599318201_estimated.stl"
  frame.save(str(frame_path))
  print(f"Wrote {frame_path} ({len(frame.vectors)} triangles)")

  bellow = build_bellow_stl(
      height=args.bellow_height,
      top_id=args.bellow_top_id,
      bottom_id=args.bellow_bottom_id,
  )
  bellow_path = out / "bellow_547656201_estimated.stl"
  bellow.save(str(bellow_path))
  print(f"Wrote {bellow_path} ({len(bellow.vectors)} triangles)")


if __name__ == "__main__":
  main()
