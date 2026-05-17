package com.Wx_Wr.Kaleidoscope_Agriculture_Evolution;

import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.block.ModBlocks;
import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.client.ClientSetup;
import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.entity.ModEntities;
import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.entity.PlowOxEntity;
import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.item.ModItems;
import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.network.S2CRopeSyncPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Kaleidoscope_Agriculture_Evolution.MODID)
public class Kaleidoscope_Agriculture_Evolution {

    public static final String MODID = "kaleidoscope_agriculture_evolution";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int packetId = 0;

    public Kaleidoscope_Agriculture_Evolution() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModEntities.register(modEventBus);
        Config.register(modEventBus);

        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerAttributes);

        // 注册网络包
        CHANNEL.registerMessage(packetId++, S2CRopeSyncPacket.class,
                S2CRopeSyncPacket::toBytes,
                S2CRopeSyncPacket::new,
                S2CRopeSyncPacket::handle
        );
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MODID, path);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(ClientSetup::setup);
    }

    private void registerAttributes(final EntityAttributeCreationEvent event) {
        event.put(ModEntities.PLOW_OX.get(), PlowOxEntity.createAttributes().build());
    }
}