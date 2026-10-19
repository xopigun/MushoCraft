package fr.xopi.mushocraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.xopi.mushocraft.magic.entity.SpellProjectile;
import fr.xopi.mushocraft.magic.spell.SpellElement;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Draws a spell projectile as a spinning translucent orb of its element. Fire and light glow in the dark.
 */
public class SpellProjectileRenderer extends EntityRenderer<SpellProjectile> {
    private final SpellOrbModel model;

    public SpellProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SpellOrbModel(context.bakeLayer(SpellOrbModel.LAYER));
    }

    @Override
    public void render(SpellProjectile projectile, float yaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        SpellElement element = projectile.element();
        float size = projectile.visualSize();
        int light = element == SpellElement.FIRE || element == SpellElement.LIGHT ? LightTexture.FULL_BRIGHT : packedLight;

        poseStack.pushPose();
        poseStack.translate(0.0, projectile.getBbHeight() / 2.0, 0.0);
        poseStack.scale(size, size, size);
        model.setupAnim(projectile, 0.0F, 0.0F, projectile.tickCount + partialTicks, 0.0F, 0.0F);
        model.renderToBuffer(poseStack, buffers.getBuffer(RenderType.entityTranslucent(getTextureLocation(projectile))),
                light, OverlayTexture.NO_OVERLAY, -1);
        poseStack.popPose();
        super.render(projectile, yaw, partialTicks, poseStack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(SpellProjectile projectile) {
        return projectile.element().texture();
    }
}
