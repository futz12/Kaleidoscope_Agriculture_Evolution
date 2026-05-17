package com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.event;

import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.client.ClientRopeHandler;
import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.client.renderer.rope.RopeRenderer;
import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.client.renderer.rope.RopeRendererImpl;
import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.rope.core.RopeQuality;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.Kaleidoscope_Agriculture_Evolution.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RopeClientEventHandler {

    private static RopeRenderer ropeRenderer;

    public static void init() {
        ropeRenderer = new RopeRendererImpl();
        ropeRenderer.setQuality(RopeQuality.HIGH);
    }

    public static RopeRenderer getRopeRenderer() {
        return ropeRenderer;
    }

    public static void tick(float partialTick) {
        ClientRopeHandler.tick();
        ClientRopeHandler.updatePoints(partialTick);

        if (ropeRenderer != null) {
            // 同步连接数据到渲染器
            ropeRenderer.connections.clear();
            ropeRenderer.connections.putAll(ClientRopeHandler.getRopes());
            ropeRenderer.tick(partialTick);
        }
    }

    public static void shutdown() {
        if (ropeRenderer != null) {
            ropeRenderer.clearCache();
        }
        ClientRopeHandler.clear();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ClientRopeHandler.tick();
    }
}