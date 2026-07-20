// Husqvarna Automower 440 iQ / P16 platform cutting-module motor housing frame
// OEM: 599 31 82-01 (599318201) — IPL ref 15 on 405X/415X/440 iQ blade motor diagram
//
// Packaging reference (retailer data): ~110 x 110 x 9 mm, 25 g
// Calibrate all dimensions against your OEM part before printing for fit.

$fn = 72;

// --- Plate envelope (mm) ---
plate_size   = 110;
plate_thick  = 9;

// --- Central motor bore ---
motor_bore_d = 48;

// --- Blade-disc screw pattern (3x M4, 590 50 87-01) ---
disc_screw_count = 3;
disc_screw_pcd   = 35;
disc_screw_d     = 4.4;   // clearance for M4

// --- Corner mounting slots (adjust to match your chassis) ---
corner_slot_len = 12;
corner_slot_w   = 6;
corner_inset    = 10;

// --- Optional modification pads (set true to add mounting ears) ---
enable_mod_ears = false;
mod_ear_w       = 20;
mod_ear_h       = 8;
mod_ear_thick   = 3;

module corner_slots() {
    inset = corner_inset;
    for (xy = [
        [ inset,  inset],
        [ inset,  plate_size - inset],
        [ plate_size - inset, inset],
        [ plate_size - inset, plate_size - inset]
    ]) {
        translate([xy[0] - corner_slot_w/2, xy[1] - corner_slot_len/2, -0.1])
            cube([corner_slot_w, corner_slot_len, plate_thick + 0.2]);
    }
}

module disc_screw_holes() {
    for (i = [0 : disc_screw_count - 1]) {
        rotate([0, 0, i * 360 / disc_screw_count])
            translate([disc_screw_pcd, 0, -0.1])
                cylinder(h = plate_thick + 0.2, d = disc_screw_d);
    }
}

module mod_ears() {
    if (enable_mod_ears) {
        for (x = [0, plate_size - mod_ear_w])
            translate([x, plate_size/2 - mod_ear_h/2, plate_thick])
                cube([mod_ear_w, mod_ear_h, mod_ear_thick]);
    }
}

difference() {
    union() {
        cube([plate_size, plate_size, plate_thick]);
        mod_ears();
    }
    translate([plate_size/2, plate_size/2, -0.1])
        cylinder(h = plate_thick + 0.2, d = motor_bore_d);
    translate([plate_size/2, plate_size/2, 0])
        disc_screw_holes();
    corner_slots();
}
