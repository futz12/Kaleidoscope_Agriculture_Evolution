package com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.rope.core;

import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.rope.api.RopeAttachable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

/**
 * 绳子连接数据
 * 表示一根绳子（左绳或右绳）的连接信息
 */
public class RopeConnectionData {

    // 端点实体ID
    public final UUID startEntityId;
    public final UUID endEntityId;

    // 端点实体引用（每帧更新）
    public RopeAttachable startEntity;
    public RopeAttachable endEntity;

    // 是否是左绳（false 为右绳）
    public final boolean isLeft;

    // 绳子纹理
    public final ResourceLocation texture;

    // 当前帧的世界坐标（每帧更新）
    public Vec3 startPoint;
    public Vec3 endPoint;

    // 连接ID（用于缓存标识）
    public final String connectionId;

    // 是否有效
    public boolean valid = true;

    public RopeConnectionData(RopeAttachable start, RopeAttachable end,
                              boolean isLeft, ResourceLocation texture) {
        this.startEntityId = start.getRopeUUID();
        this.endEntityId = end.getRopeUUID();
        this.startEntity = start;
        this.endEntity = end;
        this.isLeft = isLeft;
        this.texture = texture;
        this.connectionId = generateId(startEntityId, endEntityId, isLeft);
        this.startPoint = Vec3.ZERO;
        this.endPoint = Vec3.ZERO;
    }

    public static String generateId(UUID start, UUID end, boolean isLeft) {
        return start.toString() + "-" + end.toString() + "-" + (isLeft ? "L" : "R");
    }

    /**
     * 检查连接是否仍然有效（两个实体都存在且存活）
     */
    public boolean isValid() {
        return valid && startEntity != null && endEntity != null
                && startEntity.isRopeAttachableAlive()
                && endEntity.isRopeAttachableAlive();
    }

    /**
     * 更新当前帧的端点世界坐标
     */
    public void updatePoints(float partialTick) {
        if (startEntity != null && endEntity != null) {
            if (isLeft) {
                this.startPoint = startEntity.getLeftRopeAttachPoint(partialTick);
                this.endPoint = endEntity.getLeftRopeAttachPoint(partialTick);
            } else {
                this.startPoint = startEntity.getRightRopeAttachPoint(partialTick);
                this.endPoint = endEntity.getRightRopeAttachPoint(partialTick);
            }
        }
    }
}