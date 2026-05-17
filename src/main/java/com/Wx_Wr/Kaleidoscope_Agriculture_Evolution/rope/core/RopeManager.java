package com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.rope.core;

import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.Kaleidoscope_Agriculture_Evolution;
import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.network.S2CRopeSyncPacket;
import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.rope.api.RopeAttachable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.PacketDistributor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务端绳子管理器 - 简化版
 */
public class RopeManager {

    private static final RopeManager INSTANCE = new RopeManager();
    private final Map<String, RopeLink> links = new ConcurrentHashMap<>();

    public static RopeManager getInstance() {
        return INSTANCE;
    }

    public RopeLink createLink(RopeAttachable start, RopeAttachable end, boolean isLeft) {
        Level level = start.getRopeLevel();
        if (level.isClientSide) return null;

        String id = RopeLink.generateId(start.getRopeUUID(), end.getRopeUUID(), isLeft);

        if (links.containsKey(id)) {
            return links.get(id);
        }

        RopeLink link = RopeLink.create(start, end, isLeft);
        if (link != null) {
            links.put(id, link);
            syncToClients(link, true);
        }

        return link;
    }

    public void destroyLink(RopeAttachable start, RopeAttachable end, boolean isLeft, boolean mayDrop) {
        String id = RopeLink.generateId(start.getRopeUUID(), end.getRopeUUID(), isLeft);
        RopeLink link = links.remove(id);
        if (link != null) {
            link.destroy(mayDrop);
            syncToClients(link, false);
        }
    }

    public void destroyAllLinks(RopeAttachable entity) {
        UUID uuid = entity.getRopeUUID();
        links.values().removeIf(link -> {
            if (link.getStartId().equals(uuid) || link.getEndId().equals(uuid)) {
                syncToClients(link, false);
                link.destroy(true);
                return true;
            }
            return false;
        });
    }

    private void syncToClients(RopeLink link, boolean add) {
        Level level = link.start.getRopeLevel();
        if (!(level instanceof ServerLevel serverLevel)) return;

        Entity startEntity = (Entity) link.start;
        AABB trackingBox = startEntity.getBoundingBox().inflate(128);

        for (ServerPlayer player : serverLevel.players()) {
            if (player.getBoundingBox().intersects(trackingBox)) {
                Kaleidoscope_Agriculture_Evolution.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new S2CRopeSyncPacket(link.getStartId(), link.getEndId(), link.isLeft(), add)
                );
            }
        }
    }

    public void tick(ServerLevel level) {
        links.values().removeIf(link -> !link.isValid());
    }
}