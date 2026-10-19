package fr.xopi.mushocraft.client.render.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.client.render.mesh.Premultiplied;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * A blade shaped like a crescent moon, flying face first (Icicle Edge, the wind and flame slices): a curved edge,
 * broad in the middle and thinning to two points that sweep back from the flight. It faces the way it flies, so the
 * one who threw it sees its whole curve. A bright blade inside a glow added to the light behind it (technique of the
 * visual reference, docs/VISUELS_REFERENCE.md). Each blade is tilted its own way, as a slash would be.
 */
public final class CrescentVisual {
    private static final int GLOW = 0xF000F0;
    /** Half the angle the blade's arc spans, its segments, and how far its points sweep back (in blade radii). */
    private static final float HALF_ARC = 1.15F;
    private static final int SEGMENTS = 20;
    private static final float SWEEP = 0.55F;
    /** The blade's width at its middle, in blade radii. */
    private static final float WIDTH = 0.32F;
    private static final float MAX_TILT = 35.0F * Mth.DEG_TO_RAD;

    public static final ProjectileVisual ICE = of(MushoCraft.id("textures/entity/spell/ice_crystal.png"),
            new float[]{0.88F, 0.96F, 1.0F, 0.9F}, new float[]{0.5F, 0.78F, 1.0F, 0.6F});
    public static final ProjectileVisual WIND = of(MushoCraft.id("textures/entity/spell/wind_streaks.png"),
            new float[]{0.92F, 1.0F, 0.96F, 0.75F}, new float[]{0.5F, 1.0F, 0.8F, 0.55F});
    public static final ProjectileVisual FIRE = of(FireVisuals.FIRE,
            new float[]{1.0F, 0.92F, 0.62F, 0.95F}, new float[]{1.0F, 0.45F, 0.12F, 0.7F});

    private CrescentVisual() {
    }

    private static ProjectileVisual of(ResourceLocation texture, float[] blade, float[] glow) {
        return (projectile, partialTicks, poseStack, buffers, light) -> {
            float time = projectile.tickCount + partialTicks;
            float radius = projectile.visualSize();
            float tilt = (Mth.sin(projectile.getId() * 12.9898F) * 0.5F + 0.5F) * 2.0F * MAX_TILT - MAX_TILT;
            poseStack.pushPose();
            poseStack.translate(0.0, projectile.getBbHeight() / 2.0, 0.0);
            faceFlight(poseStack, projectile.getDeltaMovement());
            // the blade lies across the flight; it turns a little about it, and quivers
            poseStack.mulPose(Axis.YP.rotation(tilt + 0.06F * Mth.sin(time * 0.9F)));
            poseStack.scale(radius, radius, radius);
            blade(poseStack.last(), buffers.getBuffer(RenderType.entityTranslucentEmissive(texture)), blade, 1.0F, time);
            blade(poseStack.last(), new Premultiplied(buffers.getBuffer(RenderType.eyes(texture))), glow, 1.35F, time);
            poseStack.popPose();
        };
    }

    /**
     * Turns the pose so +Y points the way the blade flies, +X to its right and +Z up (as level as the flight allows),
     * whatever the direction: the blade keeps the same bearing however it is thrown.
     */
    private static void faceFlight(PoseStack poseStack, Vec3 motion) {
        if (motion.lengthSqr() < 1.0E-6) {
            return;
        }
        Vector3f forward = new Vector3f((float) motion.x, (float) motion.y, (float) motion.z).normalize();
        Vector3f right = new Vector3f(forward).cross(0.0F, 1.0F, 0.0F);
        right = right.lengthSquared() < 1.0E-6F ? new Vector3f(1.0F, 0.0F, 0.0F) : right.normalize();
        Vector3f up = new Vector3f(right).cross(forward).normalize();
        poseStack.mulPose(new Quaternionf().setFromNormalized(new Matrix3f(right, forward, up)));
    }

    /** The blade, on both faces, scaled up by {@code swell} across its width for the glow around it. */
    private static void blade(PoseStack.Pose pose, VertexConsumer consumer, float[] colour, float swell, float time) {
        float scroll = time * 0.08F;
        for (int i = 0; i < SEGMENTS; i++) {
            float[] a = edge(i / (float) SEGMENTS, swell);
            float[] b = edge((i + 1) / (float) SEGMENTS, swell);
            float u0 = i / (float) SEGMENTS + scroll;
            float u1 = (i + 1) / (float) SEGMENTS + scroll;
            // outer and inner points of the two ends of this segment: x, sweep (y), z
            quad(pose, consumer, colour, a, b, u0, u1, false);
            quad(pose, consumer, colour, a, b, u0, u1, true);
        }
    }

    /**
     * The outer and inner points of the blade at a point along it (0 to 1, tip to tip): {outer x, y, z, inner x, y, z}.
     * The arc is part of a circle of radius 1 whose centre is set above the blade's middle, so the middle sits at the
     * origin and the points curve up; they also sweep back along the flight (-y).
     */
    private static float[] edge(float along, float swell) {
        float angle = (along * 2.0F - 1.0F) * HALF_ARC;
        float fromMiddle = angle / HALF_ARC;
        float width = WIDTH * swell * (float) Math.pow(Math.max(0.0F, 1.0F - fromMiddle * fromMiddle), 0.75);
        float sweep = -SWEEP * fromMiddle * fromMiddle;
        float outer = 1.0F + (swell - 1.0F) * 0.12F;
        float inner = outer - width;
        float sin = Mth.sin(angle);
        float cos = Mth.cos(angle);
        return new float[]{sin * outer, sweep, 1.0F - cos * outer, sin * inner, sweep, 1.0F - cos * inner};
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer consumer, float[] colour, float[] a, float[] b, float u0, float u1,
                             boolean back) {
        float normal = back ? -1.0F : 1.0F;
        if (back) {
            vertex(pose, consumer, colour, b[0], b[1], b[2], u1, 0.0F, normal);
            vertex(pose, consumer, colour, b[3], b[4], b[5], u1, 1.0F, normal);
            vertex(pose, consumer, colour, a[3], a[4], a[5], u0, 1.0F, normal);
            vertex(pose, consumer, colour, a[0], a[1], a[2], u0, 0.0F, normal);
        } else {
            vertex(pose, consumer, colour, a[0], a[1], a[2], u0, 0.0F, normal);
            vertex(pose, consumer, colour, a[3], a[4], a[5], u0, 1.0F, normal);
            vertex(pose, consumer, colour, b[3], b[4], b[5], u1, 1.0F, normal);
            vertex(pose, consumer, colour, b[0], b[1], b[2], u1, 0.0F, normal);
        }
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, float[] colour, float x, float y, float z, float u, float v,
                               float normal) {
        consumer.addVertex(pose, x, y, z).setColor(colour[0], colour[1], colour[2], colour[3]).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(GLOW).setNormal(pose, 0.0F, normal, 0.0F);
    }
}
