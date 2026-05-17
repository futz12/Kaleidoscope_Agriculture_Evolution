package com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.entity.rope;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * 绳子端点坐标计算器
 * 负责将模型空间的定位器坐标转换为世界坐标
 */
public class RopeEndpointCalculator {

    // ==================== 定位器坐标 (像素单位) ====================

    // 牛 (PlowOx) 的数据
    private static final float OX_LEFT_X = -9f;
    private static final float OX_RIGHT_X = 9f;
    private static final float OX_Y = 14.5f;
    private static final float OX_Z = 3.75f;

    // 曲辕犁 (Plow) 的数据
    private static final float PLOW_LEFT_X = -9f;
    private static final float PLOW_RIGHT_X = 9f;
    private static final float PLOW_Y = 5.1f;
    private static final float PLOW_Z = 13.7f;

    // 单位转换: 像素 -> 格数
    private static final float PIXEL_TO_BLOCK = 1f / 16f;

    // ==================== 核心计算方法 ====================

    /**
     * 计算世界坐标
     *
     * @param entity 实体
     * @param localX 模型局部X坐标 (像素)
     * @param localY 模型局部Y坐标 (像素)
     * @param localZ 模型局部Z坐标 (像素)
     * @param partialTick 渲染插值系数
     * @return 世界坐标
     */
    public static Vec3 calculate(Entity entity,
                                 float localX, float localY, float localZ,
                                 float partialTick) {
        // 1. 获取实体的插值位置
        double x = entity.xOld + (entity.getX() - entity.xOld) * partialTick;
        double y = entity.yOld + (entity.getY() - entity.yOld) * partialTick;
        double z = entity.zOld + (entity.getZ() - entity.zOld) * partialTick;

        // 2. 获取实体的插值朝向
        float yaw = getInterpolatedYaw(entity, partialTick);
        float yawRad = (float) Math.toRadians(yaw);

        // 3. 转换单位: 像素 -> 格数
        float offsetX = localX * PIXEL_TO_BLOCK;
        float offsetY = localY * PIXEL_TO_BLOCK;
        float offsetZ = localZ * PIXEL_TO_BLOCK;

        // 4. 绕Y轴旋转 (只影响 X 和 Z)
        double worldX = x + (offsetX * Math.cos(yawRad) - offsetZ * Math.sin(yawRad));
        double worldZ = z + (offsetX * Math.sin(yawRad) + offsetZ * Math.cos(yawRad));
        double worldY = y + offsetY;

        return new Vec3(worldX, worldY, worldZ);
    }

    /**
     * 获取插值后的Yaw角度
     */
    private static float getInterpolatedYaw(Entity entity, float partialTick) {
        float prevYaw = entity.yRotO;
        float currYaw = entity.getYRot();
        float delta = currYaw - prevYaw;

        // 处理角度环绕 (例如 350° -> 10°)
        if (delta > 180) delta -= 360;
        if (delta < -180) delta += 360;

        return prevYaw + delta * partialTick;
    }

    // ==================== 牛的端点计算方法 ====================

    /**
     * 计算牛的左绳附着点
     */
    public static Vec3 getOxLeftPoint(Entity ox, float partialTick) {
        return calculate(ox, OX_LEFT_X, OX_Y, OX_Z, partialTick);
    }

    /**
     * 计算牛的右绳附着点
     */
    public static Vec3 getOxRightPoint(Entity ox, float partialTick) {
        return calculate(ox, OX_RIGHT_X, OX_Y, OX_Z, partialTick);
    }

    // ==================== 曲辕犁的端点计算方法 ====================

    /**
     * 计算曲辕犁的左绳附着点
     */
    public static Vec3 getPlowLeftPoint(Entity plow, float partialTick) {
        return calculate(plow, PLOW_LEFT_X, PLOW_Y, PLOW_Z, partialTick);
    }

    /**
     * 计算曲辕犁的右绳附着点
     */
    public static Vec3 getPlowRightPoint(Entity plow, float partialTick) {
        return calculate(plow, PLOW_RIGHT_X, PLOW_Y, PLOW_Z, partialTick);
    }

    // ==================== 通用端点获取 (通过枚举) ====================

    /**
     * 端点类型枚举
     */
    public enum EndpointType {
        OX_LEFT, OX_RIGHT,
        PLOW_LEFT, PLOW_RIGHT
    }

    /**
     * 根据端点类型获取世界坐标
     */
    public static Vec3 getPoint(EndpointType type, Entity entity, float partialTick) {
        switch (type) {
            case OX_LEFT:
                return getOxLeftPoint(entity, partialTick);
            case OX_RIGHT:
                return getOxRightPoint(entity, partialTick);
            case PLOW_LEFT:
                return getPlowLeftPoint(entity, partialTick);
            case PLOW_RIGHT:
                return getPlowRightPoint(entity, partialTick);
            default:
                return entity.position();
        }
    }

    // ==================== Builder 模式 (用于动态配置) ====================

    /**
     * 可配置的端点计算器 (用于需要动态调整定位器的场景)
     */
    public static class Builder {
        private float localX;
        private float localY;
        private float localZ;

        public Builder localX(float x) { this.localX = x; return this; }
        public Builder localY(float y) { this.localY = y; return this; }
        public Builder localZ(float z) { this.localZ = z; return this; }

        public Builder fromPixels(float x, float y, float z) {
            this.localX = x;
            this.localY = y;
            this.localZ = z;
            return this;
        }

        public Builder fromBlocks(float x, float y, float z) {
            this.localX = x * 16f;
            this.localY = y * 16f;
            this.localZ = z * 16f;
            return this;
        }

        public Vec3 calculate(Entity entity, float partialTick) {
            return RopeEndpointCalculator.calculate(entity, localX, localY, localZ, partialTick);
        }
    }
}