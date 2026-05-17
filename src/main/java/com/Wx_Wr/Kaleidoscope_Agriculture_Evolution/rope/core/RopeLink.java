package com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.rope.core;

import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.rope.api.RopeAttachable;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * 服务端绳子连接 - 简化版，无碰撞箱
 */
public class RopeLink {

    @NotNull
    public final RopeAttachable start;

    @NotNull
    public final RopeAttachable end;

    public final boolean isLeft;

    private boolean alive = true;
    private final String connectionId;

    private RopeLink(@NotNull RopeAttachable start, @NotNull RopeAttachable end, boolean isLeft) {
        this.start = Objects.requireNonNull(start);
        this.end = Objects.requireNonNull(end);
        this.isLeft = isLeft;
        this.connectionId = generateId(start.getRopeUUID(), end.getRopeUUID(), isLeft);
    }

    @Nullable
    public static RopeLink create(@NotNull RopeAttachable start, @NotNull RopeAttachable end, boolean isLeft) {
        if (start.getRopeUUID().equals(end.getRopeUUID())) {
            return null;
        }

        Level level = start.getRopeLevel();
        if (level.isClientSide) {
            return null;
        }

        return new RopeLink(start, end, isLeft);
    }

    public boolean isAlive() {
        return alive;
    }

    public void destroy(boolean mayDrop) {
        if (!alive) return;
        alive = false;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public UUID getStartId() {
        return start.getRopeUUID();
    }

    public UUID getEndId() {
        return end.getRopeUUID();
    }

    public boolean isLeft() {
        return isLeft;
    }

    public static String generateId(UUID start, UUID end, boolean isLeft) {
        return start.toString() + "-" + end.toString() + "-" + (isLeft ? "L" : "R");
    }

    public boolean isValid() {
        return alive && start.isRopeAttachableAlive() && end.isRopeAttachableAlive();
    }
}