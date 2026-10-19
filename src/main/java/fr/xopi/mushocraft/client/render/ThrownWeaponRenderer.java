package fr.xopi.mushocraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fr.xopi.mushocraft.sword.entity.ThrownWeapon;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * Draws a thrown weapon with its own item model, whirling end over end along its flight.
 */
public class ThrownWeaponRenderer extends EntityRenderer<ThrownWeapon> {
    private static final float SPIN_DEGREES_PER_TICK = 50.0F;

    private final ItemRenderer itemRenderer;

    public ThrownWeaponRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ThrownWeapon weapon, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers,
                       int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0, weapon.getBbHeight() / 2.0, 0.0);
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, weapon.yRotO, weapon.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-(weapon.tickCount + partialTicks) * SPIN_DEGREES_PER_TICK));
        poseStack.scale(1.2F, 1.2F, 1.2F);
        itemRenderer.renderStatic(weapon.weapon(), ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY,
                poseStack, buffers, weapon.level(), weapon.getId());
        poseStack.popPose();
        super.render(weapon, yaw, partialTicks, poseStack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownWeapon weapon) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
