package fr.xopi.mushocraft.client.render.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.magic.entity.SpellPillar;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import javax.annotation.Nullable;

/**
 * Draws a {@link SpellPillar}: its Blockbench model (GeckoLib), scaled to the pillar's height, turned and leaning as the
 * spell set it. Ice is translucent; rock is opaque. Frost is a mesh instead (see {@link FrostVisuals}).
 */
public class SpellPillarRenderer extends EntityRenderer<SpellPillar> {
    private final GeoEntityRenderer<SpellPillar> geo;

    public SpellPillarRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.geo = new GeoEntityRenderer<>(context, new Model()) {
            @Override
            public RenderType getRenderType(SpellPillar pillar, ResourceLocation texture, @Nullable MultiBufferSource buffers,
                                            float partialTick) {
                return pillar.kind().translucent() ? RenderType.entityTranslucent(texture) : RenderType.entityCutoutNoCull(texture);
            }
        };
    }

    @Override
    public boolean shouldRender(SpellPillar pillar, Frustum frustum, double x, double y, double z) {
        float reach = pillar.height();
        return frustum.isVisible(new AABB(pillar.getX() - reach, pillar.getY() - 1.0, pillar.getZ() - reach,
                pillar.getX() + reach, pillar.getY() + reach + 1.0, pillar.getZ() + reach));
    }

    @Override
    public void render(SpellPillar pillar, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int light) {
        if (!pillar.kind().isBlockbench()) {
            FrostVisuals.renderMound(pillar, partialTicks, poseStack, buffers, light);
            super.render(pillar, yaw, partialTicks, poseStack, buffers, light);
            return;
        }
        float scale = pillar.height() / pillar.kind().modelHeight();
        poseStack.pushPose();
        // lean first, then the model's own yaw is applied by the GeckoLib renderer
        poseStack.mulPose(Axis.YP.rotationDegrees(-pillar.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(pillar.getXRot()));
        poseStack.mulPose(Axis.YP.rotationDegrees(pillar.getYRot()));
        poseStack.scale(scale, scale, scale);
        geo.render(pillar, pillar.getYRot(), partialTicks, poseStack, buffers, light);
        poseStack.popPose();
        super.render(pillar, yaw, partialTicks, poseStack, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(SpellPillar pillar) {
        return MushoCraft.id("textures/entity/spell/" + pillar.kind().texture() + ".png");
    }

    private static final class Model extends GeoModel<SpellPillar> {
        @Override
        public ResourceLocation getModelResource(SpellPillar pillar) {
            return MushoCraft.id("geo/entity/" + pillar.kind().model() + ".geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(SpellPillar pillar) {
            return MushoCraft.id("textures/entity/spell/" + pillar.kind().texture() + ".png");
        }

        @Override
        public ResourceLocation getAnimationResource(SpellPillar pillar) {
            return MushoCraft.id("animations/entity/" + pillar.kind().model() + ".animation.json");
        }
    }
}
