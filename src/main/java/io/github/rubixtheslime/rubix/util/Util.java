package io.github.rubixtheslime.rubix.util;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

public class Util {

    private static final char[] PREFIXES = new char[]{'μ', 'n', 'p', 'f', 'a', 'z', 'y', 'r', 'q'};

    public static Vector3d vec3dToVector3d(Vec3d v) {
        return new Vector3d(v.x, v.y, v.z);
    }

    public static @NotNull String formatTime(double millis) {
        if (millis == 0) return "0s";
        double scaled;
        String unit;
        if (millis >= 3_600_000d) {
            scaled = millis / 3_600_000d;
            unit = "hr";
        } else if (millis >= 60_000d) {
            scaled = millis / 60_000d;
            unit = "min";
        } else if (millis >= 1_000d) {
            scaled = millis / 1_000d;
            unit = "s";
        } else {
            scaled = millis;
            char p = 'm';
            for (var prefix : PREFIXES) {
                if (scaled >= 1) break;
                scaled *= 1000;
                p = prefix;
            }
            unit = String.format("%cs", p);
        }
        String accuracy = "%.1f%s";
        if (scaled < 10d) accuracy = "%.3f%s";
        else if (scaled < 100d) accuracy = "%.2f%s";
        return String.format(accuracy, scaled, unit);
    }
}
