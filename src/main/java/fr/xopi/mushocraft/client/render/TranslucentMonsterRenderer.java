package fr.xopi.mushocraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.client.renderer.patched.entity.PCustomEntityRenderer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

/**
 * Draws a monster's skinned mesh see-through (a wraith: a translucent figure, wiki page <i>Monsters</i>). Same posing as
 * Epic Fight's custom renderer, with a translucent render type and an overall opacity.
 */
public class TranslucentMonsterRenderer extends PCustomEntityRenderer {
    private final float opacity;

    public TranslucentMonsterRenderer(AssetAccessor<SkinnedMesh> mesh, EntityRendererProvider.Context context, float opacity) {
        super(mesh, context);
        this.opacity = opacity;
    }

    @Override
    public void render(LivingEntity entity, LivingEntityPatch<LivingEntity> patch, EntityRenderer<LivingEntity> renderer,
                       MultiBufferSource buffer, PoseStack poseStack, int packedLight, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        ResourceLocation texture = renderer.getTextureLocation(entity);
        RenderType type = minecraft.shouldEntityAppearGlowing(entity) ? RenderType.outline(texture) : RenderType.entityTranslucent(texture);
        Armature armature = patch.getArmature();
        poseStack.pushPose();
        mulPoseStack(poseStack, armature, entity, patch, partialTicks);
        setArmaturePose(patch, armature, partialTicks);
        float alpha = entity.isInvisibleTo(minecraft.player) ? 0.0F : opacity;
        if (alpha > 0.0F) {
            getDefaultMesh().get().draw(poseStack, buffer, type, packedLight, 1.0F, 1.0F, 1.0F, alpha,
                    getOverlayCoord(entity, patch, partialTicks), armature, armature.getPoseMatrices());
        }
        poseStack.popPose();
    }
}
