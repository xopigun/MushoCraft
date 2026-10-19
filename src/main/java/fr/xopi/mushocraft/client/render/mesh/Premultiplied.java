package fr.xopi.mushocraft.client.render.mesh;

import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Wraps a vertex consumer whose blending ignores alpha (additive render types such as {@code RenderType.eyes}): each
 * colour is multiplied by its alpha, so a mesh that fades out at its edges still does when added to the light.
 */
public record Premultiplied(VertexConsumer delegate) implements VertexConsumer {
    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        delegate.addVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        delegate.setColor(red * alpha / 255, green * alpha / 255, blue * alpha / 255, 255);
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        delegate.setUv(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        delegate.setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        delegate.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        delegate.setNormal(x, y, z);
        return this;
    }
}
