package com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.event;

import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.client.renderer.rope.RopeRenderer;
import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.client.renderer.*;
import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.entity.PlowOxEntity;
import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.item.WhipItem;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

import static com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.Kaleidoscope_Agriculture_Evolution.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class ClientEventHandler {

    private static final double RENDER_DISTANCE = 32.0;

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null) return;

        var bufferSource = mc.renderBuffers().bufferSource();
        var camera = event.getCamera();
        var poseStack = event.getPoseStack();
        float partialTick = event.getPartialTick();

        // 更新绳子渲染器
        RopeClientEventHandler.tick(partialTick);

        // 渲染绳子
        RopeRenderer ropeRenderer = RopeClientEventHandler.getRopeRenderer();
        if (ropeRenderer != null) {
            poseStack.pushPose();
            Vec3 cameraPos = camera.getPosition();
            poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            ropeRenderer.render(poseStack, bufferSource, partialTick, camera);
            poseStack.popPose();
        }

        renderWhipSelections(player, poseStack, bufferSource, camera);
        renderOxRelated(player, mc, poseStack, bufferSource, camera, partialTick);

        bufferSource.endBatch();
    }

    private static void renderWhipSelections(net.minecraft.world.entity.player.Player player,
                                             PoseStack poseStack,
                                             MultiBufferSource bufferSource,
                                             Camera camera) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof WhipItem)) return;

        CompoundTag tag = stack.getOrCreateTag();

        if (tag.getBoolean("editing") && tag.contains("corner1")) {
            BlockPos c1 = BlockPos.of(tag.getLong("corner1"));
            BlockPos c2 = tag.contains("corner2")
                    ? BlockPos.of(tag.getLong("corner2"))
                    : player.blockPosition();
            RangeHighlightRenderer.render(poseStack, bufferSource, camera, c1, c2);
        }

        List<BlockPos[]> ranges = WhipItem.getRanges(stack);
        for (BlockPos[] range : ranges) {
            RangeHighlightRenderer.render(poseStack, bufferSource, camera, range[0], range[1]);
        }
    }

    private static void renderOxRelated(net.minecraft.world.entity.player.Player player,
                                        Minecraft mc,
                                        PoseStack poseStack,
                                        MultiBufferSource bufferSource,
                                        Camera camera,
                                        float partialTick) {
        var entities = player.level().getEntitiesOfClass(PlowOxEntity.class,
                player.getBoundingBox().inflate(RENDER_DISTANCE));

        for (PlowOxEntity ox : entities) {
            if (ox.getOxState() == PlowOxEntity.OxState.SELECT_DIRECTION
                    && ox.getTargetCorner() != null && ox.getOtherCorner() != null) {
                renderDirectionArrows(ox, poseStack, camera);
            }

            if (ox.getOxState() == PlowOxEntity.OxState.PLOWING) {
                String pathData = ox.getPlowPathData();
                if (!pathData.isEmpty()) {
                    List<BlockPos[]> rows = PlowOxEntity.parsePlowPathData(pathData);
                    PlowPathRenderer.renderPath(poseStack, camera, rows);
                }
            }
        }
    }

    private static void renderDirectionArrows(PlowOxEntity ox,
                                              PoseStack poseStack,
                                              Camera camera) {
        BlockPos corner = ox.getTargetCorner();
        BlockPos other = ox.getOtherCorner();

        int minX = Math.min(corner.getX(), other.getX());
        int maxX = Math.max(corner.getX(), other.getX());
        int minZ = Math.min(corner.getZ(), other.getZ());
        int maxZ = Math.max(corner.getZ(), other.getZ());

        if (minX != maxX) {
            int xDir = Integer.compare(other.getX(), corner.getX());
            DirectionArrowRenderer.renderArrow(poseStack, camera,
                    corner.offset(xDir, 0, 0), true, xDir);
        }
        if (minZ != maxZ) {
            int zDir = Integer.compare(other.getZ(), corner.getZ());
            DirectionArrowRenderer.renderArrow(poseStack, camera,
                    corner.offset(0, 0, zDir), false, zDir);
        }
    }
}