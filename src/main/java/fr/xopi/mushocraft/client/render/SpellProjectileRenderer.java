package fr.xopi.mushocraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.xopi.mushocraft.client.render.spell.CrystalClusterVisual;
import fr.xopi.mushocraft.client.render.spell.FireVisuals;
import fr.xopi.mushocraft.client.render.spell.GlowShellVisuals;
import fr.xopi.mushocraft.client.render.spell.ProjectileVisual;
import fr.xopi.mushocraft.client.render.spell.RibbonTrail;
import fr.xopi.mushocraft.client.render.spell.RockVisuals;
import fr.xopi.mushocraft.client.render.spell.WaterVisuals;
import fr.xopi.mushocraft.magic.entity.SpellProjectile;
import fr.xopi.mushocraft.magic.spell.SpellElement;
import fr.xopi.mushocraft.magic.spell.effect.ProjectileShape;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * Draws a spell projectile as a spinning translucent orb of its element. Fire and light glow in the dark.
 */
public class SpellProjectileRenderer extends EntityRenderer<SpellProjectile> {
    private final SpellOrbModel model;
    private final CrystalClusterVisual crystalCluster;

    public SpellProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SpellOrbModel(context.bakeLayer(SpellOrbModel.LAYER));
        this.crystalCluster = new CrystalClusterVisual(context);
    }

    @Override
    public void render(SpellProjectile projectile, float yaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        trailColour(projectile).ifPresent(rgb -> RibbonTrail.render(projectile, partialTicks, poseStack, buffers,
                projectile.visualSize() * 0.35F, rgb));
        Optional<ProjectileVisual> visual = visualFor(projectile);
        if (visual.isPresent()) {
            visual.get().render(projectile, partialTicks, poseStack, buffers, packedLight);
            super.render(projectile, yaw, partialTicks, poseStack, buffers, packedLight);
            return;
        }
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

    /**
     * The colour of the ribbon of light a projectile leaves behind, if it leaves one. Water and earth leave none: water
     * sheds drops, rock is heavy.
     */
    private static Optional<Integer> trailColour(SpellProjectile projectile) {
        if (projectile.shape() == ProjectileShape.CRYSTAL_CLUSTER) {
            return Optional.empty();
        }
        return switch (projectile.element()) {
            case FIRE -> Optional.of(0xFF8A30);
            case WIND -> Optional.of(0xB8FFE4);
            case LIGHT -> Optional.of(0xFFE490);
            case ICE -> Optional.of(0xB8DCFF);
            case WATER, EARTH -> Optional.empty();
        };
    }

    /** The animated model of this projectile, if it has one yet; otherwise the plain orb of its element. */
    private Optional<ProjectileVisual> visualFor(SpellProjectile projectile) {
        if (projectile.shape() == ProjectileShape.CRYSTAL_CLUSTER) {
            return Optional.of(crystalCluster);
        }
        if (projectile.element() == SpellElement.FIRE) {
            return Optional.of(FireVisuals.FIREBALL);
        }
        if (projectile.element() == SpellElement.LIGHT) {
            return Optional.of(GlowShellVisuals.LIGHT);
        }
        if (projectile.element() == SpellElement.WIND) {
            return Optional.of(GlowShellVisuals.WIND);
        }
        if (projectile.element() == SpellElement.ICE) {
            return Optional.of(GlowShellVisuals.ICE);
        }
        if (projectile.element() == SpellElement.EARTH) {
            return Optional.of(RockVisuals.ROCK_BULLET);
        }
        if (projectile.element() == SpellElement.WATER) {
            return Optional.of(projectile.shape() == ProjectileShape.BOLT ? WaterVisuals.WATER_ARROW : WaterVisuals.WATER_BALL);
        }
        return Optional.empty();
    }

    @Override
    public ResourceLocation getTextureLocation(SpellProjectile projectile) {
        return projectile.element().texture();
    }
}
