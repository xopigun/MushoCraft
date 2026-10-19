package fr.xopi.mushocraft.client.render.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.client.render.mesh.LatheMesh;
import fr.xopi.mushocraft.client.render.mesh.Premultiplied;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Projectiles of light, wind and plain ice: a bright core inside a shell half again as large that is added to the
 * light behind it, a crisp silhouette with a glow and no shader (technique of the visual reference,
 * docs/VISUELS_REFERENCE.md). The core turns, the shell breathes.
 */
public final class GlowShellVisuals {
    private static final int GLOW = 0xF000F0;

    private GlowShellVisuals() {
    }

    public static final ProjectileVisual LIGHT = of(ManaOrb.MANA,
            new LatheMesh.Material(1.0F, 1.0F, 0.92F, 1.0F, 0.85F, 0.4F, 2.0F, 1.0F, 0.04F, -0.06F),
            new LatheMesh.Material(1.0F, 0.82F, 0.38F, 0.7F, 0.0F, 0.0F, 3.0F, 1.0F, -0.03F, 0.05F));

    public static final ProjectileVisual WIND = of(MushoCraft.id("textures/entity/spell/wind_streaks.png"),
            new LatheMesh.Material(0.9F, 1.0F, 0.96F, 0.35F, 0.8F, 0.6F, 2.0F, 1.0F, 0.12F, 0.0F),
            new LatheMesh.Material(0.55F, 1.0F, 0.85F, 0.6F, 0.0F, 0.0F, 2.0F, 1.0F, 0.2F, 0.02F));

    public static final ProjectileVisual ICE = of(MushoCraft.id("textures/entity/spell/ice_crystal.png"),
            new LatheMesh.Material(0.86F, 0.95F, 1.0F, 0.85F, 1.0F, 0.5F, 1.0F, 1.0F, 0.0F, 0.0F),
            new LatheMesh.Material(0.55F, 0.82F, 1.0F, 0.55F, 0.0F, 0.0F, 2.0F, 1.0F, 0.02F, -0.02F));

    private static ProjectileVisual of(ResourceLocation texture, LatheMesh.Material core, LatheMesh.Material shell) {
        return (projectile, partialTicks, poseStack, buffers, light) -> {
            float time = projectile.tickCount + partialTicks;
            float radius = projectile.visualSize() / 2.0F;
            LatheMesh.Deform swirl = (point, t, angle) -> {
                float wave = 1.0F + 0.08F * Mth.sin(angle * 4.0F + time * 0.6F + point.y * 3.0F);
                point.mul(wave, 1.0F, wave);
            };
            poseStack.pushPose();
            poseStack.translate(0.0, projectile.getBbHeight() / 2.0, 0.0);
            WaterVisuals.alignWithFlight(poseStack, projectile.getDeltaMovement());
            poseStack.mulPose(Axis.YP.rotation(time * 0.25F));
            poseStack.scale(radius, radius, radius);
            LatheMesh.render(poseStack.last(), buffers.getBuffer(RenderType.entityTranslucentEmissive(texture)), LatheMesh.SPHERE,
                    swirl, core, 12, 16, time, GLOW);
            float breathe = 1.5F + 0.08F * Mth.sin(time * 0.5F);
            poseStack.scale(breathe, breathe, breathe);
            LatheMesh.render(poseStack.last(), new Premultiplied(buffers.getBuffer(RenderType.eyes(texture))), LatheMesh.SPHERE,
                    swirl, shell, 12, 16, time + 5.0F, GLOW);
            poseStack.popPose();
        };
    }
}
