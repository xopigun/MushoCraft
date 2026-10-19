package fr.xopi.mushocraft.client.render.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.client.render.mesh.LatheMesh;
import fr.xopi.mushocraft.magic.entity.SpellProjectile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Conjured water, after the anime: a clear, bright, light-blue mass with shimmering highlights.
 * <ul>
 *     <li>Water Ball: a wobbling sphere that stretches into a drop along its flight, the faster the longer, with a
 *     deeper core and a glint.</li>
 *     <li>Water Arrow: a slender pointed bolt whose surface ripples backwards as it flies.</li>
 * </ul>
 * The surface texture is a scrolling pattern of caustics (textures/entity/spell/water_surface.png, drawn for MushoCraft).
 */
public final class WaterVisuals {
    static final ResourceLocation WATER = MushoCraft.id("textures/entity/spell/water_surface.png");
    static final ResourceLocation GLINT = MushoCraft.id("textures/entity/spell/glint.png");

    private static final LatheMesh.Material SHELL = new LatheMesh.Material(0.62F, 0.86F, 1.0F, 0.32F, 0.9F, 0.55F,
            2.0F, 1.0F, 0.015F, 0.04F);
    private static final LatheMesh.Material CORE = new LatheMesh.Material(0.25F, 0.55F, 1.0F, 0.22F, 0.45F, 0.1F,
            1.0F, 1.0F, -0.02F, -0.03F);

    private WaterVisuals() {
    }

    public static final ProjectileVisual WATER_BALL = (projectile, partialTicks, poseStack, buffers, light) -> {
        float time = projectile.tickCount + partialTicks;
        float radius = projectile.visualSize() / 2.0F;
        Vec3 motion = projectile.getDeltaMovement();
        float speed = (float) motion.length();
        // The faster it flies, the longer the drop; its volume stays the same.
        float stretch = 1.0F + Math.min(1.6F, speed * 0.7F);
        float tail = (stretch - 1.0F) * 0.8F;

        poseStack.pushPose();
        poseStack.translate(0.0, projectile.getBbHeight() / 2.0, 0.0);
        alignWithFlight(poseStack, motion);
        float across = radius / (float) Math.sqrt(stretch);
        poseStack.scale(across, radius * stretch, across);

        LatheMesh.Deform wobble = (point, t, angle) -> {
            float ripple = 1.0F + 0.07F * Mth.sin(time * 0.55F + point.y * 4.0F + angle * 3.0F)
                    * Mth.cos(time * 0.4F + angle * 2.0F);
            point.mul(ripple, 1.0F, ripple);
            if (point.y < 0.0F) {
                // the back of the drop trails behind
                point.y *= 1.0F + tail * (1.0F - t);
            }
        };
        VertexConsumer core = buffers.getBuffer(RenderType.entityTranslucentCull(WATER));
        poseStack.pushPose();
        poseStack.scale(0.55F, 0.55F, 0.55F);
        LatheMesh.render(poseStack.last(), core, LatheMesh.SPHERE, wobble, CORE, 12, 16, time, light);
        poseStack.popPose();
        VertexConsumer shell = buffers.getBuffer(RenderType.entityTranslucentCull(WATER));
        LatheMesh.render(poseStack.last(), shell, LatheMesh.SPHERE, wobble, SHELL, 16, 24, time, light);
        poseStack.popPose();

        renderGlint(projectile, poseStack, buffers, radius, time);
    };

    /** A slim drop flying towards +Y: pointed tip ahead (top), a long tapering tail behind. */
    static final LatheMesh.Profile BOLT = t -> {
        float y = t * 2.0F - 1.0F;
        float radius = t > 0.78F ? (1.0F - t) / 0.22F : (float) Math.pow(t / 0.78F, 0.6);
        return new float[]{y, radius};
    };

    public static final ProjectileVisual WATER_ARROW = (projectile, partialTicks, poseStack, buffers, light) -> {
        float time = projectile.tickCount + partialTicks;
        float thickness = projectile.visualSize() / 2.0F;
        float length = projectile.visualSize() * 4.0F;

        poseStack.pushPose();
        poseStack.translate(0.0, projectile.getBbHeight() / 2.0, 0.0);
        alignWithFlight(poseStack, projectile.getDeltaMovement());
        poseStack.scale(thickness, length / 2.0F, thickness);
        LatheMesh.Deform ripple = (point, t, angle) -> {
            float wave = 1.0F + 0.12F * Mth.sin(t * 18.0F + time * 1.2F);
            point.mul(wave, 1.0F, wave);
        };
        LatheMesh.render(poseStack.last(), buffers.getBuffer(RenderType.entityTranslucentCull(WATER)), BOLT, ripple, SHELL,
                14, 12, time, light);
        poseStack.popPose();
    };

    /** Turns the mesh so its +Y axis points the way the projectile flies. */
    static void alignWithFlight(PoseStack poseStack, Vec3 motion) {
        if (motion.lengthSqr() > 1.0E-6) {
            Vector3f direction = new Vector3f((float) motion.x, (float) motion.y, (float) motion.z).normalize();
            poseStack.mulPose(new Quaternionf().rotationTo(new Vector3f(0.0F, 1.0F, 0.0F), direction));
        }
    }

    /**
     * A soft glint facing the eye, slightly off-centre, as light catches the water. It is drawn on both sides of the
     * ball, double-faced: only the one on the eye's side shows, the other is hidden behind the water.
     */
    static void renderGlint(SpellProjectile projectile, PoseStack poseStack, MultiBufferSource buffers, float radius, float time) {
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucentEmissive(GLINT));
        for (float side : new float[]{1.05F, -1.05F}) {
            poseStack.pushPose();
            poseStack.translate(0.0, projectile.getBbHeight() / 2.0, 0.0);
            poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
            poseStack.translate(-radius * 0.38F, radius * 0.38F, radius * side);
            quad(consumer, poseStack.last(), radius * (0.42F + 0.06F * Mth.sin(time * 0.9F)), 0.95F);
            poseStack.translate(radius * 0.6F, -radius * 0.55F, 0.0F);
            quad(consumer, poseStack.last(), radius * 0.16F, 0.7F);
            poseStack.popPose();
        }
    }

    /** A square facing +Z, drawn with both windings so it shows whichever way it is turned. */
    private static void quad(VertexConsumer consumer, PoseStack.Pose pose, float half, float alpha) {
        float[][] corners = {{-half, -half, 0, 1}, {half, -half, 1, 1}, {half, half, 1, 0}, {-half, half, 0, 0}};
        for (int pass = 0; pass < 2; pass++) {
            for (int k = 0; k < 4; k++) {
                float[] corner = corners[pass == 0 ? k : 3 - k];
                consumer.addVertex(pose.pose(), corner[0], corner[1], 0.0F).setColor(1.0F, 1.0F, 1.0F, alpha)
                        .setUv(corner[2], corner[3]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0)
                        .setNormal(pose, 0.0F, 0.0F, pass == 0 ? 1.0F : -1.0F);
            }
        }
    }
}
