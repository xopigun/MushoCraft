package fr.xopi.mushocraft.client.render.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.client.render.mesh.LatheMesh;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * A glowing ball of mana: a bright swirling core in a faint halo, tinted with a colour. A spell forming in the hand and
 * a summoned spirit are both drawn with it. Mana glows: it is never lit by the world.
 */
public final class ManaOrb {
    static final ResourceLocation MANA = MushoCraft.id("textures/entity/spell/mana_surface.png");
    private static final int GLOW = 0xF000F0;

    private ManaOrb() {
    }

    /**
     * @param texture the surface (mana swirl, water, fire)
     * @param glow    from faint (0) to full (1)
     */
    public static void render(PoseStack poseStack, MultiBufferSource buffers, ResourceLocation texture, int rgb, float radius,
                              float glow, float time) {
        float red = (rgb >> 16 & 0xFF) / 255.0F;
        float green = (rgb >> 8 & 0xFF) / 255.0F;
        float blue = (rgb & 0xFF) / 255.0F;
        LatheMesh.Deform swirl = (point, t, angle) -> {
            float wave = 1.0F + 0.1F * Mth.sin(angle * 4.0F + time * 0.7F + point.y * 3.0F);
            point.mul(wave, 1.0F, wave);
        };
        poseStack.pushPose();
        poseStack.scale(radius, radius, radius);
        LatheMesh.render(poseStack.last(), buffers.getBuffer(RenderType.entityTranslucentEmissive(texture)), LatheMesh.SPHERE, swirl,
                new LatheMesh.Material(Math.min(1.0F, red + 0.35F), Math.min(1.0F, green + 0.35F), Math.min(1.0F, blue + 0.35F),
                        glow, glow * 0.4F, 0.3F, 2.0F, 1.0F, 0.03F, -0.05F), 10, 14, time, GLOW);
        // a faint halo of gathered mana around it
        poseStack.scale(1.6F, 1.6F, 1.6F);
        LatheMesh.render(poseStack.last(), buffers.getBuffer(RenderType.entityTranslucentEmissive(texture)), LatheMesh.SPHERE, swirl,
                new LatheMesh.Material(red, green, blue, glow * 0.35F, 0.0F, 0.0F, 3.0F, 1.0F, -0.04F, 0.06F), 10, 14, time, GLOW);
        poseStack.popPose();
    }
}
