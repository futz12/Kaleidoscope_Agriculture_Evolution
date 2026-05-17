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

        System.out.println("[ClientEventHandler] onRenderLevel called, partialTick = " + partialTick);

        // 更新绳子渲染器
        RopeClientEventHandler.tick(partialTick);

        // 渲染绳子
        RopeRenderer ropeRenderer = RopeClientEventHandler.getRopeRenderer();
        if (ropeRenderer != null) {
            System.out.println("[ClientEventHandler] RopeRenderer exists, connections = " + ropeRenderer.connections.size());

            poseStack.pushPose();
            Vec3 cameraPos = camera.getPosition();
            System.out.println("[ClientEventHandler] Camera position = " + cameraPos);
            poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            ropeRenderer.render(poseStack, bufferSource, partialTick, camera);
            poseStack.popPose();
        } else {
            System.out.println("[ClientEventHandler] RopeRenderer is null!");
        }

        // 调试球体（显示端点位置）
        renderDebugSpheres(ropeRenderer, poseStack, bufferSource);

        renderWhipSelections(player, poseStack, bufferSource, camera);
        renderOxRelated(player, mc, poseStack, bufferSource, camera, partialTick);

        bufferSource.endBatch();
    }

    /**
     * 调试用：渲染端点球体
     */
    private static void renderDebugSpheres(RopeRenderer ropeRenderer, PoseStack poseStack, MultiBufferSource buffer) {
        if (ropeRenderer == null || ropeRenderer.connections == null) return;

        System.out.println("[ClientEventHandler] renderDebugSpheres, connections = " + ropeRenderer.connections.size());

        for (var conn : ropeRenderer.connections.values()) {
            if (conn.startPoint != null && conn.endPoint != null) {
                System.out.println("[ClientEventHandler] Debug sphere: start=" + conn.startPoint + ", end=" + conn.endPoint);
                // 起点 - 红色球体
                renderSolidSphere(poseStack, buffer, conn.startPoint, 0.3f, 1.0f, 0.0f, 0.0f);
                // 终点 - 绿色球体
                renderSolidSphere(poseStack, buffer, conn.endPoint, 0.3f, 0.0f, 1.0f, 0.0f);
                // 中点 - 蓝色球体
                Vec3 mid = conn.startPoint.lerp(conn.endPoint, 0.5);
                renderSolidSphere(poseStack, buffer, mid, 0.3f, 0.0f, 0.0f, 1.0f);
            }
        }
    }

    private static void renderSolidSphere(PoseStack poseStack, MultiBufferSource buffer, Vec3 pos, float size, float r, float g, float b) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.entitySolid(new net.minecraft.resources.ResourceLocation("minecraft", "textures/block/red_concrete.png")));
        var pose = poseStack.last().pose();

        int light = LightTexture.pack(15, 15);

        float x = (float) pos.x;
        float y = (float) pos.y;
        float z = (float) pos.z;
        float s = size;

        float[][] verts = {
                {x - s, y - s, z - s}, {x + s, y - s, z - s},
                {x + s, y - s, z + s}, {x - s, y - s, z + s},
                {x - s, y + s, z - s}, {x + s, y + s, z - s},
                {x + s, y + s, z + s}, {x - s, y + s, z + s}
        };

        int[][] faces = {
                {0,1,2,3}, {4,7,6,5}, {0,4,5,1},
                {2,6,7,3}, {0,3,7,4}, {1,5,6,2}
        };

        for (int[] face : faces) {
            consumer.vertex(pose, verts[face[0]][0], verts[face[0]][1], verts[face[0]][2])
                    .color(r, g, b, 1.0f).uv(0, 0).overlayCoords(0).uv2(light).normal(0, 1, 0).endVertex();
            consumer.vertex(pose, verts[face[1]][0], verts[face[1]][1], verts[face[1]][2])
                    .color(r, g, b, 1.0f).uv(0, 0).overlayCoords(0).uv2(light).normal(0, 1, 0).endVertex();
            consumer.vertex(pose, verts[face[2]][0], verts[face[2]][1], verts[face[2]][2])
                    .color(r, g, b, 1.0f).uv(0, 0).overlayCoords(0).uv2(light).normal(0, 1, 0).endVertex();
            consumer.vertex(pose, verts[face[3]][0], verts[face[3]][1], verts[face[3]][2])
                    .color(r, g, b, 1.0f).uv(0, 0).overlayCoords(0).uv2(light).normal(0, 1, 0).endVertex();
        }
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