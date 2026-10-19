package fr.xopi.mushocraft.client.render.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.core.MushoParticles;
import fr.xopi.mushocraft.magic.entity.PillarKind;
import fr.xopi.mushocraft.magic.entity.SpellPillar;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3d;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Draws a {@link SpellPillar}: its Blockbench model (GeckoLib), scaled to the pillar's height, turned and leaning as the
 * spell set it. Ice is translucent; rock is opaque. Frost is a mesh instead (see {@link FrostVisuals}).
 */
public class SpellPillarRenderer extends EntityRenderer<SpellPillar> {
    private final GeoEntityRenderer<SpellPillar> geo;
    private final Map<SpellPillar, Integer> lastEmitted = new WeakHashMap<>();

    public SpellPillarRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.geo = new GeoEntityRenderer<>(context, new Model()) {
            @Override
            public RenderType getRenderType(SpellPillar pillar, ResourceLocation texture, @Nullable MultiBufferSource buffers,
                                            float partialTick) {
                return pillar.kind().translucent() ? RenderType.entityTranslucent(texture) : RenderType.entityCutoutNoCull(texture);
            }

            @Override
            public void preRender(PoseStack poseStack, SpellPillar pillar, BakedGeoModel model, @Nullable MultiBufferSource buffers,
                                  @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight,
                                  int packedOverlay, int colour) {
                emitters(pillar).forEach(name -> model.getBone(name).ifPresent(bone -> bone.setTrackingMatrices(true)));
                super.preRender(poseStack, pillar, model, buffers, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
            }

            @Override
            public void renderFinal(PoseStack poseStack, SpellPillar pillar, BakedGeoModel model, MultiBufferSource buffers,
                                    @Nullable VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay, int colour) {
                super.renderFinal(poseStack, pillar, model, buffers, buffer, partialTick, packedLight, packedOverlay, colour);
                emit(pillar, model);
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

    /**
     * The bones the model sheds particles from (technique of the visual reference, docs/VISUELS_REFERENCE.md): frost
     * glints from the ice pillar's tip, dust trickling from the earthen spikes' shards.
     */
    private static List<String> emitters(SpellPillar pillar) {
        return switch (pillar.kind()) {
            case ICE_PILLAR -> List.of("tip");
            case EARTH_LANCE, EARTH_SPIKE -> List.of("shard_0", "shard_1", "shard_2");
            case EARTH_WALL, EARTH_FORTRESS, FROST_MOUND -> List.of();
        };
    }

    /** Once every few ticks per pillar, never twice for the same tick however many frames are drawn. */
    private void emit(SpellPillar pillar, BakedGeoModel model) {
        if (pillar.tickCount % 3 != 0 || Integer.valueOf(pillar.tickCount).equals(lastEmitted.get(pillar))) {
            return;
        }
        lastEmitted.put(pillar, pillar.tickCount);
        ParticleOptions particle = pillar.kind() == PillarKind.ICE_PILLAR
                ? ColorParticleOption.create(MushoParticles.MANA_MOTE.get(), 0xFFCCEEFF)
                : new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.COARSE_DIRT.defaultBlockState());
        for (String name : emitters(pillar)) {
            model.getBone(name).ifPresent(bone -> {
                Vector3d at = bone.getWorldPosition();
                pillar.level().addParticle(particle, at.x + (pillar.getRandom().nextDouble() - 0.5) * 0.3, at.y,
                        at.z + (pillar.getRandom().nextDouble() - 0.5) * 0.3, 0.0, 0.02, 0.0);
            });
        }
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
