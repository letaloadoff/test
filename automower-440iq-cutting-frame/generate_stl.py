#!/usr/bin/env python3
"""Generate the STL for Husqvarna 547656201 FRAME from the OpenSCAD source.

The parametric model `frame_547656201.scad` is the single source of truth.
This helper renders it to STL and lets you override key dimensions from the
command line (handy after measuring your OEM part with calipers).

Geometry is traced from the official 440 iQ IPL diagram UN-778162.png
(BLADE MOTOR AND CUTTING EQUIPMENT, callout #13): a thin disc with a
castellated central hub, two tapered fins (E-W), and two snap-clip
towers (N-S).

Usage:
  python3 generate_stl.py
  python3 generate_stl.py --outer-d 206 --bore-d 52 --plate-thick 2.5
  python3 generate_stl.py --set hub_od=76 --set spline_count=10
"""

from __future__ import annotations

import argparse
import shutil
import subprocess
import sys
from pathlib import Path

HERE = Path(__file__).parent
SCAD = HERE / "frame_547656201.scad"

# CLI flag -> SCAD variable
NAMED = {
    "outer_d": "--outer-d",
    "plate_thick": "--plate-thick",
    "hub_od": "--hub-od",
    "bore_d": "--bore-d",
}


def render_with_openscad(defines: dict[str, str], out_path: Path) -> bool:
    exe = shutil.which("openscad") or shutil.which("openscad-nogui")
    if not exe:
        return False
    cmd = [exe, "-o", str(out_path)]
    for key, val in defines.items():
        cmd += ["-D", f"{key}={val}"]
    cmd.append(str(SCAD))
    print("Running:", " ".join(cmd))
    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode != 0:
        print(result.stderr, file=sys.stderr)
        return False
    return True


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--output-dir", type=Path, default=HERE / "output")
    parser.add_argument("--outer-d", type=float, help="outer disc diameter (mm)")
    parser.add_argument("--plate-thick", type=float, help="base plate thickness (mm)")
    parser.add_argument("--hub-od", type=float, help="hub collar outer diameter (mm)")
    parser.add_argument("--bore-d", type=float, help="castellated bore diameter (mm)")
    parser.add_argument("--no-clips", action="store_true", help="omit the snap-clip towers")
    parser.add_argument(
        "--set",
        action="append",
        default=[],
        metavar="VAR=VALUE",
        help="override any SCAD variable directly (repeatable)",
    )
    args = parser.parse_args()

    defines: dict[str, str] = {}
    for var, flag in NAMED.items():
        val = getattr(args, flag.lstrip("-").replace("-", "_"), None)
        if val is not None:
            defines[var] = str(val)
    if args.no_clips:
        defines["enable_clips"] = "false"
    for item in args.set:
        if "=" not in item:
            parser.error(f"--set expects VAR=VALUE, got {item!r}")
        k, v = item.split("=", 1)
        defines[k.strip()] = v.strip()

    out_dir = args.output_dir
    out_dir.mkdir(parents=True, exist_ok=True)
    out_path = out_dir / "frame_547656201.stl"

    if render_with_openscad(defines, out_path):
        print(f"Wrote {out_path}")
        return

    print(
        "OpenSCAD not found. Install it (e.g. `apt-get install openscad` or from\n"
        "https://openscad.org/) and re-run, or open frame_547656201.scad in the\n"
        "OpenSCAD GUI and export the STL manually.",
        file=sys.stderr,
    )
    sys.exit(1)


if __name__ == "__main__":
    main()
