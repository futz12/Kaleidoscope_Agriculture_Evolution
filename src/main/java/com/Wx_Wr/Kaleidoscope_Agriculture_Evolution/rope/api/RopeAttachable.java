package com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.rope.api;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

/**
 * 可连接绳子的实体接口
 * 任何需要连接绳子的实体（牛、犁、其他农具）都需要实现此接口
 */
public interface RopeAttachable {

    /**
     * 获取左绳附着点的世界坐标（已插值）
     * @param partialTick 渲染插值系数 (0-1)
     * @return 左绳附着点的世界坐标
     */
    Vec3 getLeftRopeAttachPoint(float partialTick);

    /**
     * 获取右绳附着点的世界坐标（已插值）
     * @param partialTick 渲染插值系数 (0-1)
     * @return 右绳附着点的世界坐标
     */
    Vec3 getRightRopeAttachPoint(float partialTick);

    /**
     * 获取实体的唯一标识符
     */
    UUID getRopeUUID();

    /**
     * 获取实体所在的世界
     */
    Level getRopeLevel();

    /**
     * 检查实体是否存活且可用于绳子连接
     */
    boolean isRopeAttachableAlive();

    /**
     * 获取实体的当前Yaw角度（度）
     * @param partialTick 渲染插值系数
     */
    float getRopeYaw(float partialTick);
}