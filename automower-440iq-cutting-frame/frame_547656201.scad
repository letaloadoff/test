// Husqvarna Automower 440 iQ / 410 iQ / 420 iQ
// OEM 547 65 62-01 (547656201) — FRAME (cutting-disc spider / guard)
//
// Geometry traced from the official IPL diagram UN-778162.png,
// BLADE MOTOR AND CUTTING EQUIPMENT, callout #13.
//
// The part is a thin circular plate with:
//   * a stepped central hub with a castellated (splined) inner bore,
//   * two flat tapered fins (E-W) each with a small alignment hole,
//   * two twin-prong snap-clip towers (N-S) that latch onto the disc hub.
//
// Print: PETG or ASA, 0.2 mm layers, 3+ perimeters, plate face down.
// All dimensions are ESTIMATES from diagram proportions — measure your
// OEM part with calipers and update the variables before relying on fit.

$fn = 120;

/* ---------------- Overall plate ---------------- */
outer_d          = 200;   // outer disc diameter
plate_thick      = 2.2;   // base plate thickness
edge_chamfer     = 1.2;   // chamfer at the outer rim

/* ---------------- Central hub ---------------- */
hub_od           = 74;    // hub collar outer diameter
hub_step_od      = 62;    // inner concentric step outer diameter
hub_height       = 6.0;   // hub rises this far above the plate
bore_d           = 51;    // castellated inner bore diameter

// Internal splines / castellations that grip the disc hub
spline_count     = 8;
spline_depth     = 2.2;   // radial projection into the bore
spline_width     = 5.0;   // tangential width of each spline
spline_height    = 4.0;   // vertical height of splines (within hub)

/* ---------------- Tapered fins (E-W) ---------------- */
fin_count        = 2;     // left + right
fin_root_r       = hub_od/2 + 1;   // start radius (at hub)
fin_tip_r        = outer_d/2 - 8;  // fins nearly reach the rim
fin_root_w       = 18;    // width at hub
fin_tip_w        = 3;     // width at tip (pointed)
fin_spine_h      = 4.0;   // raised central spine height
fin_base_h       = 2.2;   // flat wing thickness above plate
fin_hole_d       = 4.0;   // alignment hole near hub
fin_hole_r       = hub_od/2 + 7;   // radius of alignment hole

/* ---------------- Snap-clip towers (N-S) ---------------- */
enable_clips     = true;  // set false to omit delicate clips
clip_count       = 2;
clip_root_r      = hub_od/2 + 2;
clip_height      = 12;    // tower height above plate
clip_leg_w       = 2.4;   // each prong width
clip_leg_t       = 2.2;   // prong thickness (radial)
clip_gap         = 2.0;   // gap between the twin prongs
clip_barb        = 1.6;   // barb overhang

/* ==================================================== */

module ring(od, id, h) {
    difference() {
        cylinder(h = h, d = od);
        translate([0, 0, -0.1]) cylinder(h = h + 0.2, d = id);
    }
}

module base_plate() {
    // plate with chamfered outer edge
    difference() {
        union() {
            cylinder(h = plate_thick - edge_chamfer, d = outer_d);
            translate([0, 0, plate_thick - edge_chamfer])
                cylinder(h = edge_chamfer, d1 = outer_d, d2 = outer_d - 2*edge_chamfer);
        }
    }
}

module hub() {
    // stepped collar
    union() {
        ring(hub_od, bore_d, hub_height);
        translate([0, 0, hub_height - 1.5])
            ring(hub_step_od, bore_d, 1.5 + 0.01);
    }
}

module splines() {
    // internal tabs projecting inward from bore wall
    for (i = [0 : spline_count - 1]) {
        rotate([0, 0, i * 360 / spline_count])
            translate([bore_d/2 - spline_depth/2, 0, plate_thick])
                cube([spline_depth + 0.5, spline_width, spline_height], center = false);
    }
}

module fin(angle) {
    rotate([0, 0, angle]) {
        // flat wing
        linear_extrude(height = fin_base_h)
            hull() {
                translate([fin_root_r, 0]) circle(d = fin_root_w, $fn = 40);
                translate([fin_tip_r, 0]) circle(d = fin_tip_w, $fn = 20);
            }
        // raised spine
        linear_extrude(height = fin_spine_h)
            hull() {
                translate([fin_root_r + 3, 0]) circle(d = fin_root_w * 0.30, $fn = 24);
                translate([fin_tip_r - 6, 0]) circle(d = fin_tip_w * 0.9 + 0.6, $fn = 16);
            }
    }
}

module fin_hole(angle) {
    rotate([0, 0, angle])
        translate([fin_hole_r, 0, -0.1])
            cylinder(h = plate_thick + fin_base_h + 0.2, d = fin_hole_d);
}

module snap_clip(angle) {
    // twin-prong clip with outward barbs
    rotate([0, 0, angle])
        translate([clip_root_r, 0, plate_thick]) {
            for (s = [-1, 1]) {
                translate([0, s * (clip_gap/2 + clip_leg_w/2) - clip_leg_w/2, 0]) {
                    // prong leg
                    cube([clip_leg_t, clip_leg_w, clip_height]);
                    // barb at top, overhanging radially outward
                    translate([clip_leg_t, 0, clip_height - 2.5])
                        rotate([0, 0, 0])
                            polyhedron(
                                points = [
                                    [0, 0, 0], [0, clip_leg_w, 0],
                                    [0, 0, 2.5], [0, clip_leg_w, 2.5],
                                    [clip_barb, 0, 2.5], [clip_barb, clip_leg_w, 2.5]
                                ],
                                faces = [
                                    [0,1,3,2], [2,3,5,4], [0,2,4],
                                    [1,5,3], [0,4,5,1]
                                ]
                            );
                }
            }
        }
}

/* ---------------- Assembly ---------------- */
difference() {
    union() {
        base_plate();
        translate([0, 0, plate_thick]) hub();
        translate([0, 0, plate_thick]) splines();
        // fins run E-W (0 / 180)
        for (i = [0 : fin_count - 1])
            translate([0, 0, plate_thick]) fin(i * 360 / fin_count);
        // clips run N-S (90 / 270)
        if (enable_clips)
            for (i = [0 : clip_count - 1])
                snap_clip(90 + i * 360 / clip_count);
    }

    // central bore through everything
    translate([0, 0, -0.1])
        cylinder(h = plate_thick + hub_height + 0.2, d = bore_d);

    // alignment holes on fins (E-W)
    for (i = [0 : fin_count - 1])
        fin_hole(i * 360 / fin_count);
}
