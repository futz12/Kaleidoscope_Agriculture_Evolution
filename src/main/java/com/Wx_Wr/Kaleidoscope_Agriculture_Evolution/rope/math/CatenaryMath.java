package com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.rope.math;

import net.minecraft.world.phys.Vec3;

public final class CatenaryMath {

    private CatenaryMath() {}

    public static Vec3[] computeCurvePoints(Vec3 start, Vec3 end, double sagFactor, int segments) {
        if (segments < 1) segments = 1;

        Vec3[] points = new Vec3[segments + 1];

        double dx = end.x - start.x;
        double dz = end.z - start.z;
        double dy = end.y - start.y;

        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        double totalDist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (totalDist < 0.001 || Double.isNaN(totalDist)) {
            for (int i = 0; i <= segments; i++) {
                double t = (double) i / segments;
                points[i] = start.lerp(end, t);
            }
            return points;
        }

        double a = totalDist / Math.max(0.1, sagFactor * 8.0);
        if (Double.isNaN(a) || Double.isInfinite(a)) {
            a = totalDist / 0.5;
        }
        double b = computeB(totalDist, dy, a);
        if (Double.isNaN(b) || Double.isInfinite(b)) {
            b = totalDist / 2.0;
        }
        double c = -a * Math.cosh(b / a);
        if (Double.isNaN(c) || Double.isInfinite(c)) {
            c = -a;
        }

        double dirX = horizontalDist > 0.001 ? dx / horizontalDist : 0;
        double dirZ = horizontalDist > 0.001 ? dz / horizontalDist : 0;

        for (int i = 0; i <= segments; i++) {
            double t = (double) i / segments;
            double x = t * horizontalDist;

            double yOffset = a * Math.cosh((x - b) / a) + c - (dy / totalDist) * x;

            double finalX = start.x + t * dx;
            double finalZ = start.z + t * dz;
            double finalY = start.y + t * dy + yOffset;

            points[i] = new Vec3(finalX, finalY, finalZ);
        }
        return points;
    }

    private static double computeB(double totalDist, double heightDiff, double a) {
        if (totalDist < 0.001) return 0;
        double sinhVal = Math.sinh(totalDist / (2.0 * a));
        if (Math.abs(sinhVal) < 0.0001) return totalDist / 2.0;
        double asinhArg = heightDiff / (2.0 * a * sinhVal);
        double asinhVal = Math.log(asinhArg + Math.sqrt(asinhArg * asinhArg + 1.0));
        return totalDist / 2.0 - a * asinhVal;
    }
}