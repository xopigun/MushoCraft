package fr.xopi.mushocraft.client.render.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.xopi.mushocraft.magic.entity.SpellFire;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

/**
 * Draws a {@link SpellFire}: an explosion or a pillar of flame (see {@link FireVisuals}).
 */
public class SpellFireRenderer extends EntityRenderer<SpellFire> {
    public SpellFireRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(SpellFire fire, Frustum frustum, double x, double y, double z) {
        float reach = Math.max(fire.radius(), 1.0F) * 1.5F;
        return frustum.isVisible(new AABB(fire.getX() - reach, fire.getY() - reach, fire.getZ() - reach,
                fire.getX() + reach, fire.getY() + Math.max(reach, fire.height()) + 1.0, fire.getZ() + reach));
    }

    @Override
    public void render(SpellFire fire, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int light) {
        if (fire.kind() == SpellFire.Kind.BLAST) {
            FireVisuals.renderBlast(fire, partialTicks, poseStack, buffers, light);
        } else {
            FireVisuals.renderColumn(fire, partialTicks, poseStack, buffers);
        }
        super.render(fire, yaw, partialTicks, poseStack, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(SpellFire fire) {
        return FireVisuals.FIRE;
    }
}
