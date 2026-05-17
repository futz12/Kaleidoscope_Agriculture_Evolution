package com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.client.model.rope;

/**
 * 绳子模型数据容器
 * 存储预生成的顶点和UV数据
 */
public class RopeModelData {

    private final float[] vertices;
    private final float[] uvs;
    private final int vertexCount;

    public RopeModelData(float[] vertices, float[] uvs) {
        this.vertices = vertices;
        this.uvs = uvs;
        this.vertexCount = vertices.length / 3;
    }

    public float[] getVertices() {
        return vertices;
    }

    public float[] getUvs() {
        return uvs;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public boolean isEmpty() {
        return vertexCount == 0;
    }
}