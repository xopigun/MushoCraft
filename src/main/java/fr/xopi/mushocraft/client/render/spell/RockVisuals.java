package fr.xopi.mushocraft.client.render.spell;

import com.mojang.math.Axis;
import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.client.render.mesh.LatheMesh;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Conjured rock (Rock Bullet, Stone Cannon, Earth Pillar): a lumpy, faceted stone, each one its own shape, tumbling as
 * it flies. Drawn with few facets on purpose, so it reads as hewn rock; lit by the world like any stone. The texture
 * (textures/entity/spell/earth_rock.png) is drawn for MushoCraft.
 */
public final class RockVisuals {
    private static final ResourceLocation ROCK = MushoCraft.id("textures/entity/spell/earth_rock.png");
    private static final LatheMesh.Material STONE = new LatheMesh.Material(0.95F, 0.92F, 0.88F, 1.0F, 1.0F, 0.0F,
            2.0F, 1.0F, 0.0F, 0.0F);

    private RockVisuals() {
    }

    public static final ProjectileVisual ROCK_BULLET = (projectile, partialTicks, poseStack, buffers, light) -> {
        float time = projectile.tickCount + partialTicks;
        float radius = projectile.visualSize() / 2.0F;
        int seed = projectile.getId();
        // each rock gets its own lumps, from its id
        LatheMesh.Deform hewn = (point, t, angle) -> {
            float lump = 1.0F + 0.22F * Mth.sin(angle * 3.0F + seed) * Mth.cos(t * 5.0F + seed * 0.7F)
                    + 0.12F * Mth.sin(angle * 5.0F - t * 7.0F + seed * 1.3F);
            point.mul(lump, lump * 0.85F, lump);
        };
        poseStack.pushPose();
        poseStack.translate(0.0, projectile.getBbHeight() / 2.0, 0.0);
        poseStack.mulPose(Axis.XP.rotation(time * 0.35F + seed));
        poseStack.mulPose(Axis.ZP.rotation(time * 0.22F));
        poseStack.scale(radius, radius, radius);
        LatheMesh.render(poseStack.last(), buffers.getBuffer(RenderType.entityCutoutNoCull(ROCK)), LatheMesh.SPHERE, hewn, STONE,
                5, 7, 0.0F, light);
        poseStack.popPose();
    };
}
