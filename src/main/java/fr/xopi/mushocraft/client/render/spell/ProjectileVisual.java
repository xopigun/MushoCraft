package fr.xopi.mushocraft.client.render.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.xopi.mushocraft.magic.entity.SpellProjectile;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * How one kind of spell projectile looks (Strategy pattern): each draws its own animated model.
 */
@FunctionalInterface
public interface ProjectileVisual {
    /** The pose stack is at the projectile's feet, in the renderer's usual space. */
    void render(SpellProjectile projectile, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int packedLight);
}
