// Husqvarna Automower 440 iQ / 410 iQ / 420 iQ
// OEM 547 65 62-01 (547656201) — FRAME / "Frame Sealing Bellow"
//
// Geometry traced from the official IPL diagram:
//   BLADE MOTOR AND CUTTING EQUIPMENT — UN-778162.png
//   (Husqvarna 440 iQ support page, diagram callout #13)
//
// This is a thin spoked guard ring above the cutting disc — NOT an accordion
// bellow and NOT the square motor plate (599318201).
//
// Print: PETG or ASA, 0.2 mm layers, 3+ perimeters.
// Calibrate outer_d / hub sizes against your OEM part before relying on fit.

$fn = 96;

// --- Overall spoked ring (mm) ---
outer_d        = 196;   // outer rim diameter (estimate from IPL vs 240 mm disc)
rim_width      = 10;    // outer annulus width
plate_thick    = 3.0;

// --- Central hub ---
hub_od         = 72;
center_bore_d  = 40;
hub_bore_pcd   = 52;    // mounting hole circle around center
hub_bore_d     = 4.5;   // clearance holes
hub_bore_count = 4;

// --- Four tapered spokes (90° apart) ---
arm_count      = 4;
arm_angle_span = 26;    // degrees occupied by each spoke at the hub
arm_root_width = 24;    // spoke width at hub
arm_tip_width  = 12;    // spoke width at rim
arm_rib_height = 1.2;   // raised spine on each spoke (both faces)

module annulus(od, id, h) {
    difference() {
        cylinder(h = h, d = od);
        translate([0, 0, -0.1])
            cylinder(h = h + 0.2, d = id);
    }
}

module tapered_spoke(angle, r_inner, r_outer) {
    rotate([0, 0, angle])
        linear_extrude(height = plate_thick)
            hull() {
                translate([r_inner, 0])
                    circle(d = arm_root_width, $fn = 32);
                translate([r_outer, 0])
                    circle(d = arm_tip_width, $fn = 32);
            }
}

module spoke_rib(angle, r_inner, r_outer) {
    rotate([0, 0, angle])
        translate([0, 0, plate_thick - arm_rib_height])
            linear_extrude(height = arm_rib_height)
                hull() {
                    translate([r_inner + 4, 0])
                        circle(d = arm_root_width * 0.35, $fn = 24);
                    translate([r_outer - 4, 0])
                        circle(d = arm_tip_width * 0.45, $fn = 24);
                }
}

module hub_bores() {
    translate([0, 0, -0.1])
        cylinder(h = plate_thick + 0.2, d = center_bore_d);
    for (i = [0 : hub_bore_count - 1]) {
        rotate([0, 0, i * 360 / hub_bore_count])
            translate([hub_bore_pcd / 2, 0, -0.1])
                cylinder(h = plate_thick + 0.2, d = hub_bore_d);
    }
}

outer_r = outer_d / 2;
inner_rim_r = outer_r - rim_width;
hub_r = hub_od / 2;

union() {
    // Outer rim ring
    annulus(outer_d, outer_d - 2 * rim_width, plate_thick);

    // Hub ring
    annulus(hub_od, center_bore_d, plate_thick);

    // Four tapered spokes bridging hub to rim
    for (i = [0 : arm_count - 1]) {
        tapered_spoke(i * 360 / arm_count, hub_r + 2, inner_rim_r - 2);
        spoke_rib(i * 360 / arm_count, hub_r + 2, inner_rim_r - 2);
    }
}

// Through features
hub_bores();
