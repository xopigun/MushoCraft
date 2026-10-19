package fr.xopi.mushocraft.client.render.mesh;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * A free-form surface built at render time from a grid: every point (u, v) in [0, 1]² is placed by a function, so a
 * surface can ripple, bulge, or turn from a sphere into a sheet. Shaded like {@link LatheMesh}.
 */
public final class GridMesh {
    /** Where a point of the grid lies (in the mesh's own space). */
    @FunctionalInterface
    public interface Surface {
        Vector3f at(float u, float v);
    }

    private GridMesh() {
    }

    public static void render(PoseStack.Pose pose, VertexConsumer consumer, Surface surface, LatheMesh.Material material,
                              int columns, int rows, float time, int light) {
        Vector3f[][] points = new Vector3f[rows + 1][columns + 1];
        for (int i = 0; i <= rows; i++) {
            for (int j = 0; j <= columns; j++) {
                points[i][j] = surface.at((float) j / columns, (float) i / rows);
            }
        }
        Vector3f[][] normals = new Vector3f[rows + 1][columns + 1];
        for (int i = 0; i <= rows; i++) {
            for (int j = 0; j <= columns; j++) {
                Vector3f alongV = new Vector3f(points[Math.min(rows, i + 1)][j]).sub(points[Math.max(0, i - 1)][j]);
                Vector3f alongU = new Vector3f(points[i][Math.min(columns, j + 1)]).sub(points[i][Math.max(0, j - 1)]);
                Vector3f normal = alongU.cross(alongV);
                normals[i][j] = normal.lengthSquared() < 1.0E-10F ? new Vector3f(0.0F, 0.0F, 1.0F) : normal.normalize();
            }
        }
        Matrix4f matrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                LatheMesh.vertex(consumer, matrix, normalMatrix, points[i][j], normals[i][j], material, i, j, rows, columns, time, light);
                LatheMesh.vertex(consumer, matrix, normalMatrix, points[i][j + 1], normals[i][j + 1], material, i, j + 1, rows, columns, time, light);
                LatheMesh.vertex(consumer, matrix, normalMatrix, points[i + 1][j + 1], normals[i + 1][j + 1], material, i + 1, j + 1, rows, columns, time, light);
                LatheMesh.vertex(consumer, matrix, normalMatrix, points[i + 1][j], normals[i + 1][j], material, i + 1, j, rows, columns, time, light);
            }
        }
    }
}
