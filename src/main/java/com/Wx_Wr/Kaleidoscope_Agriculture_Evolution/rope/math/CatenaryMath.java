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

        if (totalDist < 0.001) {
            for (int i = 0; i <= segments; i++) {
                double t = (double) i / segments;
                points[i] = start.lerp(end, t);
            }
            return points;
        }

        double a = totalDist / Math.max(0.1, sagFactor * 8.0);
        double b = computeB(totalDist, dy, a);
        double c = -a * Math.cosh(b / a);

        System.out.println("[CatenaryMath] a=" + String.format("%.4f", a) + " b=" + String.format("%.4f", b) + " c=" + String.format("%.4f", c) + " totalDist=" + String.format("%.4f", totalDist) + " sagFactor=" + sagFactor);
        System.out.println("[CatenaryMath] cosh(b/a)=" + String.format("%.4f", Math.cosh(b/a)) + " isValid=" + !Double.isNaN(a) + ":" + !Double.isNaN(b) + ":" + !Double.isNaN(c));

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