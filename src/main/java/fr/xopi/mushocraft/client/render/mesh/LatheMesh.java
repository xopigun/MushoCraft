package fr.xopi.mushocraft.client.render.mesh;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * A surface of revolution built at render time: a profile turned around the Y axis (a sphere, a drop, a bolt, a
 * funnel), deformed point by point (ripples, stretching), and shaded like water or glass: more transparent where it
 * faces the eye, more opaque and paler at its edges (a Fresnel look). Quads are emitted directly.
 */
public final class LatheMesh {
    /** Radius and height of the profile at a point along it, t from 0 (bottom) to 1 (top). */
    @FunctionalInterface
    public interface Profile {
        /** @return {y, radius} */
        float[] at(float t);
    }

    /** Moves a point of the surface (in the mesh's own space) to animate it. */
    @FunctionalInterface
    public interface Deform {
        void apply(Vector3f point, float t, float angle);
    }

    /** How the surface looks. Alpha goes from {@code alphaFacing} where the surface faces the eye to {@code alphaRim}. */
    public record Material(float red, float green, float blue, float alphaFacing, float alphaRim, float rimWhiten,
                           float uRepeat, float vRepeat, float uScroll, float vScroll) {
    }

    public static final Profile SPHERE = t -> {
        double angle = Math.PI * t;
        return new float[]{(float) -Math.cos(angle), (float) Math.sin(angle)};
    };

    private LatheMesh() {
    }

    public static void render(PoseStack.Pose pose, VertexConsumer consumer, Profile profile, Deform deform, Material material,
                              int rings, int segments, float time, int light) {
        Vector3f[][] points = new Vector3f[rings + 1][segments + 1];
        for (int i = 0; i <= rings; i++) {
            float t = (float) i / rings;
            float[] yr = profile.at(t);
            for (int j = 0; j <= segments; j++) {
                float angle = (float) (2.0 * Math.PI * j / segments);
                Vector3f point = new Vector3f((float) Math.cos(angle) * yr[1], yr[0], (float) Math.sin(angle) * yr[1]);
                deform.apply(point, t, angle);
                points[i][j] = point;
            }
        }
        Matrix4f matrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        Vector3f[][] normals = new Vector3f[rings + 1][segments + 1];
        for (int i = 0; i <= rings; i++) {
            for (int j = 0; j <= segments; j++) {
                Vector3f along = new Vector3f(points[Math.min(rings, i + 1)][j]).sub(points[Math.max(0, i - 1)][j]);
                Vector3f around = new Vector3f(points[i][(j + 1) % segments]).sub(points[i][(j + segments - 1) % segments]);
                Vector3f normal = around.cross(along);
                if (normal.lengthSquared() < 1.0E-10F) {
                    normal.set(points[i][j]);
                }
                normals[i][j] = normal.normalize();
            }
        }
        for (int i = 0; i < rings; i++) {
            for (int j = 0; j < segments; j++) {
                vertex(consumer, matrix, normalMatrix, points[i][j], normals[i][j], material, i, j, rings, segments, time, light);
                vertex(consumer, matrix, normalMatrix, points[i][j + 1], normals[i][j + 1], material, i, j + 1, rings, segments, time, light);
                vertex(consumer, matrix, normalMatrix, points[i + 1][j + 1], normals[i + 1][j + 1], material, i + 1, j + 1, rings, segments, time, light);
                vertex(consumer, matrix, normalMatrix, points[i + 1][j], normals[i + 1][j], material, i + 1, j, rings, segments, time, light);
            }
        }
    }

    /** One shaded vertex: Fresnel transparency and paling, scrolling UVs. Shared with {@link GridMesh}. */
    static void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normalMatrix, Vector3f point, Vector3f normal,
                               Material material, int i, int j, int rings, int segments, float time, int light) {
        Vector3f eyeToPoint = matrix.transformPosition(new Vector3f(point));
        Vector3f worldNormal = normalMatrix.transform(new Vector3f(normal)).normalize();
        float facing = eyeToPoint.lengthSquared() < 1.0E-8F ? 1.0F : Math.abs(eyeToPoint.normalize().dot(worldNormal));
        float rim = (float) Math.pow(1.0F - facing, 1.5);
        float alpha = material.alphaFacing() + (material.alphaRim() - material.alphaFacing()) * rim;
        float whiten = material.rimWhiten() * rim;
        float red = material.red() + (1.0F - material.red()) * whiten;
        float green = material.green() + (1.0F - material.green()) * whiten;
        float blue = material.blue() + (1.0F - material.blue()) * whiten;
        float u = (float) j / segments * material.uRepeat() + time * material.uScroll();
        float v = (float) i / rings * material.vRepeat() + time * material.vScroll();
        consumer.addVertex(matrix, point.x, point.y, point.z)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(worldNormal.x, worldNormal.y, worldNormal.z);
    }
}
