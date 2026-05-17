package com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.client;

import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.Kaleidoscope_Agriculture_Evolution;
import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.rope.api.RopeAttachable;
import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.rope.core.RopeConnectionData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientRopeHandler {

    private static final Map<String, RopeConnectionData> ropes = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> entityIdCache = new ConcurrentHashMap<>();
    private static final ResourceLocation DEFAULT_TEXTURE =
            new ResourceLocation(Kaleidoscope_Agriculture_Evolution.MODID, "textures/entity/lead_rope.png");

    private static final java.util.List<PendingRope> pendingRopes = new java.util.ArrayList<>();

    private static Entity getEntityByUUID(Level level, UUID uuid) {
        if (level == null || uuid == null) return null;

        if (!(level instanceof ClientLevel clientLevel)) return null;

        Integer entityId = entityIdCache.get(uuid);
        if (entityId != null) {
            Entity entity = clientLevel.getEntity(entityId);
            if (entity != null && entity.getUUID().equals(uuid)) {
                return entity;
            }
            entityIdCache.remove(uuid);
        }

        Iterable<Entity> entities = clientLevel.entitiesForRendering();
        if (entities != null) {
            for (Entity entity : entities) {
                if (entity != null && entity.getUUID().equals(uuid)) {
                    entityIdCache.put(uuid, entity.getId());
                    return entity;
                }
            }
        }

        return null;
    }

    public static void addRope(UUID startId, UUID endId, boolean isLeft) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        Entity startEntity = getEntityByUUID(mc.level, startId);
        Entity endEntity = getEntityByUUID(mc.level, endId);

        if (startEntity instanceof RopeAttachable start &&
                endEntity instanceof RopeAttachable end) {

            String key = startId + "-" + endId + "-" + (isLeft ? "L" : "R");

            if (ropes.containsKey(key)) {
                return;
            }

            RopeConnectionData conn = new RopeConnectionData(start, end, isLeft, DEFAULT_TEXTURE);
            ropes.put(key, conn);

        } else {
            String key = startId + "-" + endId + "-" + (isLeft ? "L" : "R");
            boolean alreadyPending = pendingRopes.stream().anyMatch(p ->
                    p.startId.equals(startId) && p.endId.equals(endId) && p.isLeft == isLeft);

            if (!alreadyPending) {
                pendingRopes.add(new PendingRope(startId, endId, isLeft));
            }
        }
    }

    private static class PendingRope {
        final UUID startId;
        final UUID endId;
        final boolean isLeft;
        int retryCount = 0;

        PendingRope(UUID startId, UUID endId, boolean isLeft) {
            this.startId = startId;
            this.endId = endId;
            this.isLeft = isLeft;
        }
    }

    public static void tick() {
        if (pendingRopes.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Iterator<PendingRope> iter = pendingRopes.iterator();
        while (iter.hasNext()) {
            PendingRope pending = iter.next();

            Entity startEntity = getEntityByUUID(mc.level, pending.startId);
            Entity endEntity = getEntityByUUID(mc.level, pending.endId);

            if (startEntity instanceof RopeAttachable start &&
                    endEntity instanceof RopeAttachable end) {

                String key = pending.startId + "-" + pending.endId + "-" + (pending.isLeft ? "L" : "R");
                if (!ropes.containsKey(key)) {
                    RopeConnectionData conn = new RopeConnectionData(start, end, pending.isLeft, DEFAULT_TEXTURE);
                    ropes.put(key, conn);
                }
                iter.remove();
            } else {
                pending.retryCount++;
                if (pending.retryCount > 100) {
                    iter.remove();
                }
            }
        }
    }

    public static void removeRope(UUID startId, UUID endId, boolean isLeft) {
        String key = startId + "-" + endId + "-" + (isLeft ? "L" : "R");
        ropes.remove(key);
        pendingRopes.removeIf(pending ->
                pending.startId.equals(startId) &&
                        pending.endId.equals(endId) &&
                        pending.isLeft == isLeft);
    }

    public static void clear() {
        ropes.clear();
        pendingRopes.clear();
        entityIdCache.clear();
    }

    public static Map<String, RopeConnectionData> getRopes() {
        return ropes;
    }

    public static void updatePoints(float partialTick) {
        for (RopeConnectionData conn : ropes.values()) {
            conn.updatePoints(partialTick);
        }
    }
}