package com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.client.renderer.rope;

import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.client.model.rope.RopeModelData;
import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.rope.core.RopeConnectionData;
import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.rope.core.RopeConstants;
import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.rope.core.RopeQuality;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public abstract class RopeRenderer {

    protected RopeQuality currentQuality = RopeConstants.DEFAULT_QUALITY;
    protected boolean useCache = true;
    protected int maxCachedModels = RopeConstants.MAX_CACHED_MODELS;

    public final Map<String, RopeConnectionData> connections = new LinkedHashMap<>();
    protected final Map<BakeKey, RopeModelData> modelCache = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<BakeKey, RopeModelData> eldest) {
            return size() > maxCachedModels;
        }
    };

    protected abstract RopeModelData buildModel(Vec3 start, Vec3 end, float width, float sagFactor, int segments);
    protected abstract ResourceLocation getDefaultTexture();

    public void registerRope(RopeConnectionData connection) {
        if (!connections.containsKey(connection.connectionId)) {
            connections.put(connection.connectionId, connection);
        }
    }

    public void unregisterRope(String connectionId) {
        connections.remove(connectionId);
    }

    public void tick(float partialTick) {
        connections.entrySet().removeIf(entry -> !entry.getValue().isValid());
        for (RopeConnectionData conn : connections.values()) {
            conn.updatePoints(partialTick);
        }
    }

    protected RopeModelData getOrCreateModel(RopeConnectionData conn) {
        BakeKey key = new BakeKey(conn.startPoint, conn.endPoint, currentQuality.width);

        if (useCache) {
            RopeModelData cached = modelCache.get(key);
            if (cached != null && !cached.isEmpty()) {
                return cached;
            }
        }

        RopeModelData model = buildModel(
                conn.startPoint, conn.endPoint,
                currentQuality.width, currentQuality.sagFactor, currentQuality.segments
        );

        if (useCache && model != null && !model.isEmpty() && !model.containsNaN()) {
            modelCache.put(key, model);
        } else if (model != null && model.containsNaN()) {
            clearCache();
        }

        return model;
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, float partialTick, Camera camera) {
        tick(partialTick);

        if (connections.isEmpty()) {
            return;
        }

        for (RopeConnectionData conn : connections.values()) {
            if (conn.startPoint == null || conn.endPoint == null) {
                continue;
            }
            if (!conn.isValid()) {
                continue;
            }

            RopeModelData model = getOrCreateModel(conn);
            if (model == null || model.isEmpty()) {
                continue;
            }

            renderModel(poseStack, buffer, model, conn.texture != null ? conn.texture : getDefaultTexture());
        }
    }

    protected void renderModel(PoseStack poseStack, MultiBufferSource buffer, RopeModelData model, ResourceLocation texture) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(texture));
        var pose = poseStack.last().pose();

        float[] vertices = model.getVertices();
        float[] uvs = model.getUvs();

        for (int i = 0; i < model.getVertexCount(); i++) {
            float x = vertices[i * 3];
            float y = vertices[i * 3 + 1];
            float z = vertices[i * 3 + 2];
            float u = uvs[i * 2];
            float v = uvs[i * 2 + 1];

            consumer.vertex(pose, x, y, z)
                    .color(1.0f, 1.0f, 1.0f, 1.0f)
                    .uv(u, v)
                    .overlayCoords(0)
                    .uv2(15728880)
                    .normal(0, 1, 0)
                    .endVertex();
        }
    }

    public void clearCache() {
        modelCache.clear();
    }

    public void setQuality(RopeQuality quality) {
        if (this.currentQuality != quality) {
            this.currentQuality = quality;
            clearCache();
        }
    }

    public RopeQuality getQuality() {
        return currentQuality;
    }

    protected static class BakeKey {
        private final Vec3 start;
        private final Vec3 end;
        private final float width;
        private final int hash;

        public BakeKey(Vec3 start, Vec3 end, float width) {
            this.start = quantize(start);
            this.end = quantize(end);
            this.width = Math.round(width * 1000) / 1000f;
            this.hash = Objects.hash(this.start, this.end, this.width);
        }

        private static Vec3 quantize(Vec3 v) {
            return new Vec3(
                    Math.round(v.x * 100) / 100.0,
                    Math.round(v.y * 100) / 100.0,
                    Math.round(v.z * 100) / 100.0
            );
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BakeKey that)) return false;
            return Float.compare(width, that.width) == 0
                    && start.equals(that.start)
                    && end.equals(that.end);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}