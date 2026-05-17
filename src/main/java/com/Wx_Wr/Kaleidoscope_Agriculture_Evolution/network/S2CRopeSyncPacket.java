package com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.network;

import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.Kaleidoscope_Agriculture_Evolution;
import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.client.ClientRopeHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class S2CRopeSyncPacket {

    private final UUID startId;
    private final UUID endId;
    private final boolean isLeft;
    private final boolean shouldAdd; // true=添加, false=移除

    public S2CRopeSyncPacket(UUID startId, UUID endId, boolean isLeft, boolean shouldAdd) {
        this.startId = startId;
        this.endId = endId;
        this.isLeft = isLeft;
        this.shouldAdd = shouldAdd;
    }

    public S2CRopeSyncPacket(FriendlyByteBuf buf) {
        this.startId = buf.readUUID();
        this.endId = buf.readUUID();
        this.isLeft = buf.readBoolean();
        this.shouldAdd = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(startId);
        buf.writeUUID(endId);
        buf.writeBoolean(isLeft);
        buf.writeBoolean(shouldAdd);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (shouldAdd) {
                ClientRopeHandler.addRope(startId, endId, isLeft);
            } else {
                ClientRopeHandler.removeRope(startId, endId, isLeft);
            }
        });
        return true;
    }
}