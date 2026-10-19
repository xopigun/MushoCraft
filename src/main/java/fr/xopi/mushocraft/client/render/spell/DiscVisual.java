package fr.xopi.mushocraft.client.render.spell;

import com.mojang.math.Axis;
import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.client.render.mesh.LatheMesh;
import fr.xopi.mushocraft.client.render.mesh.Premultiplied;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Icicle Edge, after the anime (S2, Roxy's Icicle Edge): a thin disc of ice, lens-shaped so its rim is a sharp edge,
 * pale and crossed by white frosted streaks that whirl as it spins. It stands upright in the line of its flight and
 * spins like a saw blade, edge first; a faint glow rims it. The texture (textures/entity/spell/ice_disc.png) is drawn
 * for MushoCraft.
 */
public final class DiscVisual {
    private static final ResourceLocation ICE_DISC = MushoCraft.id("textures/entity/spell/ice_disc.png");
    private static final int GLOW = 0xF000F0;
    /** Thickness of the disc at its centre, as a share of its radius: thin, so the rim is sharp. */
    private static final float THICKNESS = 0.14F;
    private static final LatheMesh.Material ICE = new LatheMesh.Material(0.92F, 0.97F, 1.0F, 0.92F, 1.0F, 0.6F,
            1.0F, 1.0F, 0.0F, 0.0F);
    private static final LatheMesh.Material RIM_GLOW = new LatheMesh.Material(0.55F, 0.8F, 1.0F, 0.0F, 0.55F, 0.0F,
            3.0F, 1.0F, 0.0F, 0.0F);
    private static final LatheMesh.Deform STILL = (point, t, angle) -> {
    };

    private DiscVisual() {
    }

    public static final ProjectileVisual ICICLE_EDGE = (projectile, partialTicks, poseStack, buffers, light) -> {
        float time = projectile.tickCount + partialTicks;
        float radius = projectile.visualSize();
        poseStack.pushPose();
        poseStack.translate(0.0, projectile.getBbHeight() / 2.0, 0.0);
        // +Y ahead, +X to the side, +Z up; the disc's axis is turned to lie along +X, so it stands in the line of flight
        CrescentVisual.faceFlight(poseStack, projectile.getDeltaMovement());
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.YP.rotation(-time * 0.9F));
        poseStack.scale(radius, radius * THICKNESS, radius);
        LatheMesh.render(poseStack.last(), buffers.getBuffer(RenderType.entityTranslucentEmissive(ICE_DISC)), LatheMesh.SPHERE, STILL,
                ICE, 8, 28, time, GLOW);
        poseStack.scale(1.12F, 1.6F, 1.12F);
        LatheMesh.render(poseStack.last(), new Premultiplied(buffers.getBuffer(RenderType.eyes(ICE_DISC))), LatheMesh.SPHERE, STILL,
                RIM_GLOW, 6, 28, time, GLOW);
        poseStack.popPose();
    };
}
