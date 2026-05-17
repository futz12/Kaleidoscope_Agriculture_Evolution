package com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.rope.core;

public enum RopeQuality {

    LOW(8, 0.10f, 0.05f),      // 低质量：下垂轻微
    MEDIUM(16, 0.08f, 0.08f),   // 中等质量：自然下垂
    HIGH(32, 0.08f, 0.12f),     // 高质量：更明显的下垂
    ULTRA(64, 0.06f, 0.15f);    // 超高质量：精细曲线

    public final int segments;
    public final float width;
    public final float sagFactor;

    RopeQuality(int segments, float width, float sagFactor) {
        this.segments = segments;
        this.width = width;
        this.sagFactor = sagFactor;
    }

    public static RopeQuality fromDistance(double distance) {
        if (distance < 10) return ULTRA;
        if (distance < 20) return HIGH;
        if (distance < 40) return MEDIUM;
        return LOW;
    }
}