// Husqvarna Automower 440 iQ frame sealing bellow
// OEM: 547 65 62-01 (547656201) — highlighted on Husqvarna support page ref 11
//
// This is a flexible rubber seal, not the structural plastic frame (599318201).
// Print in TPU 95A. Calibrate diameters and height against your OEM part.

$fn = 96;

// --- Bellow geometry (mm) ---
top_id       = 52;    // inner diameter at motor side
bottom_id    = 58;    // inner diameter at frame side
height       = 28;
wall         = 2.0;
conv_loops   = 5;     // accordion folds
lip_top      = 3;     // retention lip height at top
lip_bottom   = 3;     // retention lip height at bottom
lip_thick    = 1.5;

module ring(od, h) {
    difference() {
        cylinder(h = h, d = od);
        translate([0, 0, -0.1])
            cylinder(h = h + 0.2, d = od - 2 * wall);
    }
}

module convolute_section(z0, id0, id1, h) {
    steps = conv_loops * 2;
    for (i = [0 : steps - 1]) {
        t0 = i / steps;
        t1 = (i + 1) / steps;
        id_a = id0 + (id1 - id0) * t0;
        id_b = id0 + (id1 - id0) * t1;
        od_a = id_a + 2 * wall + (i % 2 == 0 ? 4 : 0);
        od_b = id_b + 2 * wall + (i % 2 == 0 ? 0 : 4);
        hull() {
            translate([0, 0, z0 + t0 * h])
                cylinder(h = 0.01, d = od_a);
            translate([0, 0, z0 + t1 * h])
                cylinder(h = 0.01, d = od_b);
        }
        translate([0, 0, z0 + t0 * h])
            cylinder(h = (t1 - t0) * h + 0.01, d1 = od_a, d2 = od_b);
    }
}

union() {
    // Bottom retention lip (frame side)
    ring(bottom_id + 2 * wall + 2 * lip_thick, lip_bottom);

    // Accordion body
    translate([0, 0, lip_bottom])
        convolute_section(0, bottom_id, top_id, height);

    // Top retention lip (motor side)
    translate([0, 0, lip_bottom + height])
        ring(top_id + 2 * wall + 2 * lip_thick, lip_top);
}
