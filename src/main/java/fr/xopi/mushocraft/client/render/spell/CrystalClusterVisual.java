package fr.xopi.mushocraft.client.render.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.magic.entity.SpellProjectile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import javax.annotation.Nullable;

/**
 * Ice Smash, after the anime: a cluster of long jagged ice spikes radiating from a dense core, bursting into shape
 * then tumbling through the air. Modelled and animated in Blockbench (blockbench/ice_smash.bbmodel, from an anime
 * reference) and played by GeckoLib.
 */
public final class CrystalClusterVisual implements ProjectileVisual {
    private static final ResourceLocation MODEL = MushoCraft.id("geo/entity/ice_smash.geo.json");
    private static final ResourceLocation TEXTURE = MushoCraft.id("textures/entity/spell/ice_crystal.png");
    private static final ResourceLocation ANIMATIONS = MushoCraft.id("animations/entity/ice_smash.animation.json");
    /** Model units from the cluster's centre to its longest spike tip (in blocks, at scale 1). */
    private static final float MODEL_RADIUS = 1.4F;
    /** Height of the cluster's centre above the model's origin (in blocks, at scale 1). */
    private static final float MODEL_CENTRE = 10.0F / 16.0F;

    private final GeoEntityRenderer<SpellProjectile> renderer;

    public CrystalClusterVisual(EntityRendererProvider.Context context) {
        this.renderer = new GeoEntityRenderer<>(context, new Model()) {
            @Override
            public RenderType getRenderType(SpellProjectile animatable, ResourceLocation texture,
                                            @Nullable MultiBufferSource bufferSource, float partialTick) {
                return RenderType.entityTranslucent(texture);
            }
        };
    }

    @Override
    public void render(SpellProjectile projectile, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int light) {
        // The spikes reach about as far as the spell's size (its diameter) from the centre.
        float scale = projectile.visualSize() * 0.9F / MODEL_RADIUS;
        poseStack.pushPose();
        poseStack.translate(0.0, projectile.getBbHeight() / 2.0 - MODEL_CENTRE * scale, 0.0);
        poseStack.scale(scale, scale, scale);
        renderer.render(projectile, projectile.getYRot(), partialTicks, poseStack, buffers, light);
        poseStack.popPose();
    }

    private static final class Model extends GeoModel<SpellProjectile> {
        @Override
        public ResourceLocation getModelResource(SpellProjectile animatable) {
            return MODEL;
        }

        @Override
        public ResourceLocation getTextureResource(SpellProjectile animatable) {
            return TEXTURE;
        }

        @Override
        public ResourceLocation getAnimationResource(SpellProjectile animatable) {
            return ANIMATIONS;
        }
    }
}
