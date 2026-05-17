package com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.rope.core;

public final class RopeConstants {

    private RopeConstants() {}

    // ==================== 渲染配置 ====================
    public static final RopeQuality DEFAULT_QUALITY = RopeQuality.MEDIUM;
    public static final float DEFAULT_ROPE_WIDTH = 0.08f;
    public static final float DEFAULT_SAG_FACTOR = 0.08f;  // 从 0.15 改为 0.08
    public static final int MAX_CACHED_MODELS = 256;

    // ==================== 碰撞体配置 ====================
    public static final float COLLIDER_SPACING = 1.2f;
    public static final float COLLIDER_WIDTH = 0.2f;
    public static final float COLLIDER_HEIGHT = 0.2f;
    public static final int COLLIDER_UPDATE_INTERVAL = 5;

    // ==================== 网络配置 ====================
    public static final int CLIENT_TRACKING_RANGE = 64;
    public static final int ENTITY_UPDATE_INTERVAL = 1;
}