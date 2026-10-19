package fr.xopi.mushocraft.client.render.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.client.render.mesh.BillowCluster;
import fr.xopi.mushocraft.client.render.mesh.LatheMesh;
import fr.xopi.mushocraft.client.render.mesh.Premultiplied;
import fr.xopi.mushocraft.magic.entity.SpellFire;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Conjured fire, after the anime: a white-hot heart wrapped in layers of cel-shaded yellow, orange and red flame
 * that lick backwards and flicker. The surface texture is a scrolling pattern of flame streaks
 * (textures/entity/spell/fire_surface.png, drawn for MushoCraft). Fire is never lit by the world: it glows.
 * <ul>
 *     <li>Fireball: a burning sphere whose flames stream behind it, longer the faster it flies.</li>
 *     <li>Blast: a flash, then a lumpy fireball that swells, darkens to red and turns to smoke.</li>
 *     <li>Column: a pillar of flame roaring up, its tongues climbing.</li>
 * </ul>
 */
public final class FireVisuals {
    static final ResourceLocation FIRE = MushoCraft.id("textures/entity/spell/fire_surface.png");
    private static final int GLOW = 0xF000F0;

    /** Opaque where it faces the eye, thinning at its edges: flames have soft borders. */
    private static final LatheMesh.Material HEART = new LatheMesh.Material(1.0F, 0.98F, 0.86F, 1.0F, 0.6F, 0.0F,
            1.0F, 1.0F, 0.02F, -0.08F);
    private static final LatheMesh.Material FLAME = new LatheMesh.Material(1.0F, 0.72F, 0.3F, 0.9F, 0.25F, 0.0F,
            3.0F, 1.0F, 0.01F, -0.12F);
    private static final LatheMesh.Material HALO = new LatheMesh.Material(0.95F, 0.36F, 0.14F, 0.55F, 0.05F, 0.0F,
            4.0F, 1.0F, -0.01F, -0.16F);

    private FireVisuals() {
    }

    public static final ProjectileVisual FIREBALL = (projectile, partialTicks, poseStack, buffers, light) -> {
        float time = projectile.tickCount + partialTicks;
        float radius = projectile.visualSize() / 2.0F;
        Vec3 motion = projectile.getDeltaMovement();
        // the tail streams back at most two blocks, whatever the fireball's size
        float tail = Math.min(0.8F + Math.min(2.2F, (float) motion.length() * 1.4F), 2.0F / Math.max(radius, 0.1F));
        // glowing flames seen from inside would flood the view: they fade as the eye comes into them
        Vec3 eye = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        float distance = (float) eye.distanceTo(projectile.getPosition(partialTicks).add(0.0, projectile.getBbHeight() / 2.0, 0.0));
        float nearFade = Mth.clamp((distance - radius * 0.8F) / (radius * 2.0F + 1.0F), 0.15F, 1.0F);

        poseStack.pushPose();
        poseStack.translate(0.0, projectile.getBbHeight() / 2.0, 0.0);
        WaterVisuals.alignWithFlight(poseStack, motion);
        poseStack.scale(radius, radius, radius);
        // the heart, then flames that stream back from it, then a thin red halo
        poseStack.pushPose();
        poseStack.scale(0.62F, 0.62F, 0.62F);
        LatheMesh.render(poseStack.last(), buffers.getBuffer(RenderType.entityTranslucentEmissive(FIRE)), LatheMesh.SPHERE,
                licking(time, 0.08F, 0.0F), HEART, 10, 14, time, GLOW);
        poseStack.popPose();
        LatheMesh.render(poseStack.last(), glow(buffers), LatheMesh.SPHERE, licking(time, 0.22F, tail), withAlpha(FLAME, nearFade),
                16, 22, time, GLOW);
        poseStack.scale(1.25F, 1.25F, 1.25F);
        LatheMesh.render(poseStack.last(), glow(buffers), LatheMesh.SPHERE, licking(time + 7.0F, 0.3F, tail * 1.3F),
                withAlpha(HALO, nearFade), 14, 20, time, GLOW);
        poseStack.popPose();
    };

    /**
     * Flickering flame on a sphere flying towards +Y: its surface ripples in tongues, and its back half is dragged into
     * a ragged tail.
     */
    private static LatheMesh.Deform licking(float time, float flicker, float tail) {
        return (point, t, angle) -> {
            float tongues = Mth.sin(angle * 5.0F + time * 0.9F - point.y * 3.0F) * Mth.cos(angle * 3.0F - time * 0.6F);
            float swell = 1.0F + flicker * tongues;
            point.mul(swell, 1.0F, swell);
            if (point.y < 0.0F) {
                float back = 1.0F - t * 2.0F;
                float ragged = 1.0F + 0.45F * Mth.sin(angle * 4.0F + time * 1.3F);
                point.y *= 1.0F + tail * back * ragged;
                // the tail narrows to points
                float narrow = 1.0F - 0.55F * back * Math.min(1.0F, tail);
                point.mul(narrow, 1.0F, narrow);
            }
        };
    }

    /** Burning layers of a blast, from the outside in: orange flame, yellow, the white-hot heart. */
    private static final LatheMesh.Material BLAST_OUTER = new LatheMesh.Material(1.0F, 0.55F, 0.18F, 0.95F, 0.55F, 0.0F,
            3.0F, 1.0F, 0.01F, -0.05F);
    private static final LatheMesh.Material BLAST_MIDDLE = new LatheMesh.Material(1.0F, 0.84F, 0.36F, 0.95F, 0.6F, 0.0F,
            2.0F, 1.0F, -0.02F, -0.07F);
    /** What is left: a pale, lit cloud of smoke. */
    private static final LatheMesh.Material SMOKE = new LatheMesh.Material(0.62F, 0.58F, 0.66F, 0.85F, 0.3F, 0.25F,
            2.0F, 1.0F, 0.005F, -0.01F);
    static final ResourceLocation SMOKE_SURFACE = MushoCraft.id("textures/entity/spell/mana_surface.png");

    /**
     * An explosion at the pose's origin: the fireball swells fast; its heart cools from white to yellow to orange, and
     * once the flame has burnt out a cloud of smoke rises in its place and thins away.
     */
    static void renderBlast(SpellFire fire, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int light) {
        float age = fire.tickCount + partialTicks;
        float life = Mth.clamp(age / fire.lifetime(), 0.0F, 1.0F);
        float radius = fire.radius();
        float grow = 1.0F - (float) Math.pow(1.0F - Math.min(1.0F, life / 0.3F), 3.0);
        float size = radius * (0.3F + 0.8F * grow);
        // heat: 1 while the flame is young, 0 when it has burnt out
        float heat = 1.0F - Mth.clamp((life - 0.2F) / 0.45F, 0.0F, 1.0F);
        LatheMesh.Deform lumps = (point, t, angle) -> {
            float lump = 1.0F + 0.14F * Mth.sin(angle * 5.0F + point.y * 4.0F + age * 0.25F)
                    * Mth.cos(angle * 3.0F - point.y * 3.0F + age * 0.18F);
            point.mul(lump);
        };

        poseStack.pushPose();
        poseStack.scale(size, size, size);
        if (heat > 0.02F) {
            // the flame shrinks inwards as it burns out, layer by layer
            // a crisp orange outside (opaque enough to read in daylight), the glow inside it, the white heart
            renderLayer(poseStack, buffers.getBuffer(RenderType.entityTranslucentEmissive(FIRE)), BLAST_OUTER, heat,
                    1.0F * (0.6F + 0.4F * heat), lumps, age, 16, 24);
            renderLayer(poseStack, glow(buffers), BLAST_MIDDLE, heat, 0.78F * heat, lumps, age + 3.0F, 14, 20);
            renderLayer(poseStack, buffers.getBuffer(RenderType.entityTranslucentEmissive(FIRE)), HEART, heat, 0.5F * heat * heat,
                    lumps, age + 6.0F, 10, 16);
        }
        poseStack.popPose();
        float smoke = Mth.clamp((life - 0.35F) / 0.25F, 0.0F, 1.0F) * (life < 0.75F ? 1.0F : 1.0F - (life - 0.75F) / 0.25F);
        if (smoke > 0.02F) {
            // what is left rises as a heap of billowing smoke
            float rise = radius * 0.7F * Math.max(0.0F, life - 0.35F);
            float puff = size * 1.6F * (0.8F + 0.4F * life);
            List<BillowCluster.Billow> billows = BillowCluster.dome(fire.getId() * 17L + 3L);
            poseStack.pushPose();
            poseStack.translate(0.0, rise - size * 0.6F, 0.0);
            poseStack.scale(puff, puff, puff);
            BillowCluster.render(poseStack, buffers.getBuffer(RenderType.entityTranslucent(SMOKE_SURFACE)), billows,
                    Mth.clamp((life - 0.3F) / 0.3F, 0.0F, 1.0F), 0.04F, withAlpha(SMOKE, smoke), age, light);
            poseStack.popPose();
        }
    }

    private static void renderLayer(PoseStack poseStack, VertexConsumer consumer, LatheMesh.Material material, float alpha,
                                    float scale, LatheMesh.Deform deform, float time, int rings, int segments) {
        if (scale < 0.02F) {
            return;
        }
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        LatheMesh.render(poseStack.last(), consumer, LatheMesh.SPHERE, deform, withAlpha(material, alpha), rings, segments, time, GLOW);
        poseStack.popPose();
    }

    /**
     * Flame drawn additively (after the reference techniques, docs/VISUELS_REFERENCE.md): it adds its light to what is
     * behind it, so layered flames glow brighter where they overlap.
     */
    private static VertexConsumer glow(MultiBufferSource buffers) {
        return new Premultiplied(buffers.getBuffer(RenderType.eyes(FIRE)));
    }

    /** A pillar of flame standing at the pose's origin. */
    static void renderColumn(SpellFire fire, float partialTicks, PoseStack poseStack, MultiBufferSource buffers) {
        float age = fire.tickCount + partialTicks;
        float rise = Mth.clamp(age / 5.0F, 0.0F, 1.0F);
        float sink = Mth.clamp((fire.lifetime() - age) / 8.0F, 0.0F, 1.0F);
        float height = fire.height() * rise * (0.4F + 0.6F * sink);
        float radius = fire.radius() * (0.5F + 0.5F * sink);
        if (height < 0.05F) {
            return;
        }
        // wide at the foot, narrowing into ragged points
        LatheMesh.Profile tower = t -> new float[]{t, (float) Math.pow(1.0F - t, 0.7) * (0.75F + 0.25F * (1.0F - t))};
        LatheMesh.Deform tongues = (point, t, angle) -> {
            float lick = 1.0F + 0.35F * t * Mth.sin(angle * 6.0F + age * 0.8F - t * 9.0F);
            float sway = 0.08F * t * Mth.sin(age * 0.3F + t * 4.0F);
            point.mul(lick, 1.0F, lick);
            point.add(sway, 0.0F, sway * 0.5F);
        };
        poseStack.pushPose();
        poseStack.scale(radius, height, radius);
        poseStack.pushPose();
        poseStack.scale(0.5F, 0.85F, 0.5F);
        LatheMesh.render(poseStack.last(), buffers.getBuffer(RenderType.entityTranslucentEmissive(FIRE)), tower, tongues,
                HEART, 10, 14, age, GLOW);
        poseStack.popPose();
        LatheMesh.render(poseStack.last(), buffers.getBuffer(RenderType.entityTranslucentEmissive(FIRE)), tower, tongues,
                FLAME, 14, 20, age, GLOW);
        poseStack.scale(1.2F, 1.05F, 1.2F);
        LatheMesh.render(poseStack.last(), glow(buffers), tower, tongues, HALO, 12, 18, age + 5.0F, GLOW);
        poseStack.popPose();
    }

    private static LatheMesh.Material withAlpha(LatheMesh.Material material, float factor) {
        return new LatheMesh.Material(material.red(), material.green(), material.blue(), material.alphaFacing() * factor,
                material.alphaRim() * factor, material.rimWhiten(), material.uRepeat(), material.vRepeat(),
                material.uScroll(), material.vScroll());
    }
}
