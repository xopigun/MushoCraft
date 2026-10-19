package fr.xopi.mushocraft.client.render.spell;

import com.mojang.math.Axis;
import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.client.render.mesh.LatheMesh;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * An ice bolt of Icicle Break, after the anime (S1 ep. 13, the ice bursting through a wall): a jagged chunk of ice,
 * pale and faceted, longer than it is wide and pointed ahead, each one broken its own way. Drawn with few sides and
 * uneven rings so it reads as cut ice; it tumbles a little as it flies.
 */
public final class IceShardVisual {
    private static final ResourceLocation ICE = MushoCraft.id("textures/entity/spell/ice_crystal.png");
    private static final LatheMesh.Material CRYSTAL = new LatheMesh.Material(0.9F, 0.97F, 1.0F, 0.88F, 1.0F, 0.55F,
            1.0F, 1.0F, 0.0F, 0.0F);
    /** A shard flying towards +Y: a short broken base, the widest part a third of the way, a long point ahead. */
    private static final LatheMesh.Profile SHARD = t -> {
        float radius = t < 0.3F ? 0.55F + 0.45F * t / 0.3F : (1.0F - t) / 0.7F;
        return new float[]{t * 2.0F - 1.0F, Math.max(0.0F, radius)};
    };

    private IceShardVisual() {
    }

    public static final ProjectileVisual SHARD_VISUAL = (projectile, partialTicks, poseStack, buffers, light) -> {
        float time = projectile.tickCount + partialTicks;
        float thickness = projectile.visualSize();
        float length = projectile.visualSize() * 3.2F;
        int seed = projectile.getId();
        // each shard broken its own way: rings pushed in and out, the facets uneven
        LatheMesh.Deform broken = (point, t, angle) -> {
            float jag = 1.0F + 0.28F * Mth.sin(angle * 2.0F + seed * 1.7F + t * 9.0F) * Mth.cos(t * 13.0F + seed);
            point.mul(jag, 1.0F, jag * (0.75F + 0.25F * Mth.sin(seed * 0.9F)));
        };
        poseStack.pushPose();
        poseStack.translate(0.0, projectile.getBbHeight() / 2.0, 0.0);
        WaterVisuals.alignWithFlight(poseStack, projectile.getDeltaMovement());
        poseStack.mulPose(Axis.YP.rotation(time * 0.15F + seed));
        poseStack.scale(thickness, length / 2.0F, thickness);
        LatheMesh.render(poseStack.last(), buffers.getBuffer(RenderType.entityTranslucentCull(ICE)), SHARD, broken, CRYSTAL,
                7, 5, time, light);
        poseStack.popPose();
    };
}
