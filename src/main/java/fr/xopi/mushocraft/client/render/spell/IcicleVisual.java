package fr.xopi.mushocraft.client.render.spell;

import com.mojang.math.Axis;
import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.client.render.mesh.LatheMesh;
import fr.xopi.mushocraft.client.render.mesh.Premultiplied;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * An ice bolt (Icicle Break): a long, faceted icicle, point first, drawn with few sides so it reads as cut ice, inside
 * a thin glow added to the light behind it. It turns slowly about its length as it flies.
 */
public final class IcicleVisual {
    private static final ResourceLocation ICE = MushoCraft.id("textures/entity/spell/ice_crystal.png");
    private static final int GLOW = 0xF000F0;
    private static final LatheMesh.Material CRYSTAL = new LatheMesh.Material(0.86F, 0.95F, 1.0F, 0.9F, 1.0F, 0.55F,
            1.0F, 1.0F, 0.0F, 0.0F);
    private static final LatheMesh.Material HALO = new LatheMesh.Material(0.5F, 0.78F, 1.0F, 0.45F, 0.0F, 0.0F,
            2.0F, 1.0F, 0.0F, 0.0F);
    private static final LatheMesh.Deform STILL = (point, t, angle) -> {
    };

    private IcicleVisual() {
    }

    public static final ProjectileVisual ICICLE = (projectile, partialTicks, poseStack, buffers, light) -> {
        float time = projectile.tickCount + partialTicks;
        float thickness = projectile.visualSize() / 2.0F;
        float length = projectile.visualSize() * 5.0F;
        poseStack.pushPose();
        poseStack.translate(0.0, projectile.getBbHeight() / 2.0, 0.0);
        WaterVisuals.alignWithFlight(poseStack, projectile.getDeltaMovement());
        poseStack.mulPose(Axis.YP.rotation(time * 0.2F + projectile.getId()));
        poseStack.scale(thickness, length / 2.0F, thickness);
        // six sides: cut ice
        LatheMesh.render(poseStack.last(), buffers.getBuffer(RenderType.entityTranslucentEmissive(ICE)), WaterVisuals.BOLT, STILL,
                CRYSTAL, 12, 6, time, GLOW);
        poseStack.scale(1.6F, 1.08F, 1.6F);
        LatheMesh.render(poseStack.last(), new Premultiplied(buffers.getBuffer(RenderType.eyes(ICE))), WaterVisuals.BOLT, STILL,
                HALO, 10, 6, time, GLOW);
        poseStack.popPose();
    };
}
