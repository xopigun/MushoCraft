package fr.xopi.mushocraft.client.render.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.client.fx.ClientMist;
import fr.xopi.mushocraft.client.render.mesh.BillowCluster;
import fr.xopi.mushocraft.client.render.mesh.LatheMesh;
import fr.xopi.mushocraft.client.render.mesh.Premultiplied;
import fr.xopi.mushocraft.magic.entity.SpellZone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

import java.util.List;

/**
 * The bodies of lasting spells, drawn by {@link SpellZoneRenderer}: wards and barriers of glowing hexagons, water
 * pouring or spreading, banks of fog. Barriers glow (added to the light, brightest at their edges); water and fog are
 * lit by the world. Textures drawn for MushoCraft.
 */
final class ZoneBodies {
    private static final ResourceLocation HEX = MushoCraft.id("textures/entity/spell/ward_hex.png");
    private static final ResourceLocation FOG = MushoCraft.id("textures/entity/spell/mana_surface.png");
    private static final int GLOW = 0xF000F0;
    private static final int APPEAR_TICKS = 6;
    private static final int FADE_TICKS = 10;

    private ZoneBodies() {
    }

    /** How present the body is: grows in, holds, fades at the end. */
    private static float presence(SpellZone zone, float age) {
        float in = Mth.clamp(age / APPEAR_TICKS, 0.0F, 1.0F);
        float out = Mth.clamp((zone.visualLifetime() - age) / FADE_TICKS, 0.0F, 1.0F);
        return Math.min(in, out);
    }

    /** A glowing barrier surface of hexagons, brighter at its edges, tinted. */
    private static LatheMesh.Material barrier(int rgb, float strength) {
        return new LatheMesh.Material((rgb >> 16 & 0xFF) / 255.0F, (rgb >> 8 & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F,
                0.1F * strength, 0.85F * strength, 0.35F, 6.0F, 3.0F, 0.004F, 0.008F);
    }

    /** A ward around its caster: an egg of hexagons that flares as it closes, then holds faintly, flickering. */
    static void ward(SpellZone zone, float age, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int rgb) {
        Entity wearer = zone.followed();
        float height = wearer == null ? 1.8F : wearer.getBbHeight();
        float presence = presence(zone, age);
        float flare = age < APPEAR_TICKS * 2 ? 1.0F + 0.8F * (1.0F - age / (APPEAR_TICKS * 2.0F)) : 1.0F;
        float flicker = 0.85F + 0.15F * Mth.sin(age * 0.9F) * Mth.sin(age * 0.37F);
        float size = zone.visualRadius() * (0.7F + 0.3F * presence);
        // seen from inside (one's own ward in first person) it would tint the whole view: only a faint trace then
        Minecraft minecraft = Minecraft.getInstance();
        float seenFromInside = wearer == minecraft.getCameraEntity() && minecraft.options.getCameraType().isFirstPerson() ? 0.2F : 1.0F;
        poseStack.pushPose();
        poseStack.translate(0.0, height * 0.5, 0.0);
        poseStack.scale(size, height * 0.62F * (0.7F + 0.3F * presence), size);
        LatheMesh.render(poseStack.last(), new Premultiplied(buffers.getBuffer(RenderType.eyes(HEX))), LatheMesh.SPHERE,
                (point, t, angle) -> { }, barrier(rgb, presence * flicker * flare * seenFromInside), 14, 20, age, GLOW);
        poseStack.popPose();
    }

    /** Spirit-Sealing Barrier: a great golden dome rising out of the ground. */
    static void sealingDome(SpellZone zone, float age, PoseStack poseStack, MultiBufferSource buffers) {
        float presence = presence(zone, age);
        float radius = zone.visualRadius();
        float rise = Mth.clamp(age / (APPEAR_TICKS * 2.0F), 0.0F, 1.0F);
        LatheMesh.Profile dome = t -> {
            double angle = Math.PI / 2.0 * t;
            return new float[]{(float) Math.sin(angle) * rise, (float) Math.cos(angle)};
        };
        poseStack.pushPose();
        poseStack.scale(radius, radius, radius);
        LatheMesh.render(poseStack.last(), new Premultiplied(buffers.getBuffer(RenderType.eyes(HEX))), dome,
                (point, t, angle) -> { }, barrier(0xFFDC7A, presence * (0.85F + 0.15F * Mth.sin(age * 0.2F))), 12, 36, age, GLOW);
        poseStack.popPose();
    }

    /** Confinement Barrier: a narrow golden cell round its prisoner, its bands sliding up. */
    static void confinementCell(SpellZone zone, float age, PoseStack poseStack, MultiBufferSource buffers) {
        float presence = presence(zone, age);
        float radius = zone.visualRadius();
        LatheMesh.Profile cell = t -> new float[]{t * 2.6F, 1.0F};
        poseStack.pushPose();
        poseStack.scale(radius, 1.0F, radius);
        LatheMesh.render(poseStack.last(), new Premultiplied(buffers.getBuffer(RenderType.eyes(HEX))), cell,
                (point, t, angle) -> { }, barrier(0xFFE8A0, presence), 8, 20, age, GLOW);
        poseStack.popPose();
    }

    private static final LatheMesh.Material FALLING_WATER = new LatheMesh.Material(0.6F, 0.85F, 1.0F, 0.45F, 0.9F, 0.5F,
            3.0F, 4.0F, 0.0F, 0.18F);
    private static final LatheMesh.Material FOAM = new LatheMesh.Material(0.92F, 0.97F, 1.0F, 0.75F, 0.95F, 0.6F,
            4.0F, 1.0F, 0.06F, 0.0F);

    /** Waterfall: a column of water pouring down from high above, foaming where it lands. */
    static void waterfall(SpellZone zone, float age, PoseStack poseStack, MultiBufferSource buffers, int light) {
        float presence = presence(zone, age);
        float radius = zone.visualRadius() * 0.55F;
        float height = 7.0F;
        // the water reaches down from the top as the spell begins
        float reach = Mth.clamp(age / 8.0F, 0.0F, 1.0F);
        LatheMesh.Profile column = t -> new float[]{height * (1.0F - t * reach), 1.0F};
        LatheMesh.Deform ripple = (point, t, angle) -> {
            float wave = 1.0F + 0.08F * Mth.sin(angle * 5.0F + point.y * 2.0F + age * 0.8F);
            point.mul(wave, 1.0F, wave);
        };
        poseStack.pushPose();
        poseStack.scale(radius * presence, 1.0F, radius * presence);
        LatheMesh.render(poseStack.last(), buffers.getBuffer(RenderType.entityTranslucent(WaterVisuals.WATER)), column, ripple,
                FALLING_WATER, 10, 20, age, light);
        poseStack.popPose();
        if (reach >= 1.0F) {
            LatheMesh.Profile foam = t -> new float[]{0.45F * (float) Math.sin(Math.PI * t), 0.6F + 0.9F * t};
            poseStack.pushPose();
            poseStack.scale(radius * presence * (1.0F + 0.06F * Mth.sin(age * 0.6F)), 1.0F, radius * presence);
            LatheMesh.render(poseStack.last(), buffers.getBuffer(RenderType.entityTranslucent(WaterVisuals.WATER)), foam, ripple,
                    FOAM, 6, 20, age, light);
            poseStack.popPose();
        }
    }

    private static final LatheMesh.Material FLOOD_WATER = new LatheMesh.Material(0.55F, 0.82F, 1.0F, 0.55F, 0.85F, 0.4F,
            4.0F, 3.0F, 0.01F, -0.05F);

    /** Flood Flush: a sheet of water spreading out over the ground, its ripples racing outwards. */
    static void flood(SpellZone zone, float age, PoseStack poseStack, MultiBufferSource buffers, int light) {
        float presence = presence(zone, age);
        float spread = zone.visualRadius() * Mth.clamp(age / 16.0F, 0.0F, 1.0F);
        LatheMesh.Profile sheet = t -> new float[]{0.12F, t};
        LatheMesh.Deform ripples = (point, t, angle) -> point.y += 0.08F * Mth.sin(t * 14.0F - age * 0.5F) * (1.0F - t * 0.5F)
                - 0.1F * t * t;
        poseStack.pushPose();
        poseStack.scale(spread, presence, spread);
        LatheMesh.render(poseStack.last(), buffers.getBuffer(RenderType.entityTranslucent(WaterVisuals.WATER)), sheet, ripples,
                FLOOD_WATER, 12, 28, age, light);
        poseStack.popPose();
    }

    private static final ResourceLocation MUD = MushoCraft.id("textures/entity/spell/mud.png");
    private static final LatheMesh.Material MUD_SURFACE = new LatheMesh.Material(1.0F, 1.0F, 1.0F, 0.96F, 1.0F, 0.08F,
            3.0F, 2.0F, 0.002F, 0.0F);

    /** Quagmire, after the anime (S1): the ground turned into a sheet of green-brown mud, slowly churning. */
    static void mud(SpellZone zone, float age, PoseStack poseStack, MultiBufferSource buffers, int light) {
        float presence = presence(zone, age);
        float spread = zone.visualRadius() * Mth.clamp(age / 10.0F, 0.0F, 1.0F);
        LatheMesh.Profile sheet = t -> new float[]{0.06F, t};
        LatheMesh.Deform churn = (point, t, angle) -> point.y += 0.05F * Mth.sin(t * 9.0F + angle * 3.0F - age * 0.15F) - 0.06F * t * t;
        poseStack.pushPose();
        poseStack.scale(spread, presence, spread);
        LatheMesh.render(poseStack.last(), buffers.getBuffer(RenderType.entityTranslucent(MUD)), sheet, churn,
                MUD_SURFACE, 10, 28, age, light);
        poseStack.popPose();
    }

    private static final LatheMesh.Material FOG_BANK = new LatheMesh.Material(1.0F, 1.0F, 1.0F, 0.38F, 0.12F, 0.6F,
            1.0F, 1.0F, 0.003F, 0.002F);

    /** Deep Mist: low banks of thick fog rolling slowly round the caster. */
    static void mist(SpellZone zone, float age, PoseStack poseStack, MultiBufferSource buffers, int light) {
        // from inside, the screen's white fog takes over (ClientMist): the banks, whose inner faces would show dark, fade
        float inside = ClientMist.depth(Minecraft.getInstance().gameRenderer.getMainCamera());
        banks(zone, age, poseStack, buffers, 1.0F, 1.0F, 1.0F, 0.004F, 0.42F, -0.2F, 8, 1.05F, 2.0F * (1.0F - inside));
    }

    /** Dust Storm, Sandstorm: banks of sand-coloured dust whirling round fast, taller than fog. */
    static void dustStorm(SpellZone zone, float age, PoseStack poseStack, MultiBufferSource buffers, int light) {
        banks(zone, age, poseStack, buffers, 0.86F, 0.74F, 0.52F, 0.05F, 0.55F, -0.2F, 6, 1.0F);
    }

    /** Cumulonimbus: a wide, dark thundercloud hanging high over the storm, slowly turning. */
    static void stormCloud(SpellZone zone, float age, PoseStack poseStack, MultiBufferSource buffers) {
        banks(zone, age, poseStack, buffers, 0.42F, 0.43F, 0.5F, 0.002F, 0.22F, 11.0F, 9, 1.4F);
    }

    /**
     * Lightning: a dark cloud gathering over the target, tighter as the energy condenses, lit from within by flickers
     * just before the bolt falls.
     *
     * @param height      how high above the target the cloud floats
     * @param gatherTicks how long it takes to condense
     */
    static void thunderhead(SpellZone zone, float age, PoseStack poseStack, MultiBufferSource buffers, float height, float gatherTicks) {
        float gather = Mth.clamp(age / gatherTicks, 0.0F, 1.0F);
        float flash = gather > 0.75F && Mth.sin(age * 3.1F) > 0.3F ? 1.0F : 0.0F;
        float shade = 0.36F + 0.5F * flash;
        banks(zone, age, poseStack, buffers, shade, shade, shade + 0.08F, 0.03F, 0.6F, height, 4, 1.6F - 0.8F * gather);
    }

    /**
     * Electric, after the anime (S3 ep. 5, Rudeus' Electric): the storm cloud compressed into a small ball, near black,
     * churning fast, lit from within by violet flickers as it charges.
     */
    static void chargedCloud(SpellZone zone, float age, PoseStack poseStack, MultiBufferSource buffers) {
        float gather = Mth.clamp(age / 12.0F, 0.0F, 1.0F);
        boolean flicker = Mth.sin(age * 4.3F) > 0.2F;
        float shade = 0.1F + (flicker ? 0.12F * gather : 0.0F);
        banks(zone, age, poseStack, buffers, shade + (flicker ? 0.1F : 0.0F), shade, shade + (flicker ? 0.22F : 0.04F), 0.12F,
                0.55F, 3.2F, 5, 0.75F - 0.35F * gather);
    }

    /** Water Splash: a crown of water leaping up from the ground, ragged at its rim, then falling back. */
    static void splashCrown(SpellZone zone, float age, PoseStack poseStack, MultiBufferSource buffers, int light) {
        float life = Mth.clamp(age / zone.visualLifetime(), 0.0F, 1.0F);
        float radius = zone.visualRadius() * (0.35F + 0.65F * life);
        float height = 1.6F * Mth.sin(Mth.PI * Math.min(1.0F, life * 1.2F));
        if (height < 0.02F) {
            return;
        }
        LatheMesh.Profile crown = t -> new float[]{t * height, 1.0F + 0.25F * t};
        LatheMesh.Deform ragged = (point, t, angle) -> point.y *= 1.0F + 0.35F * t * Mth.sin(angle * 9.0F + age * 0.3F);
        LatheMesh.Material water = new LatheMesh.Material(0.62F, 0.86F, 1.0F, 0.4F * (1.0F - life * 0.6F),
                0.9F * (1.0F - life * 0.6F), 0.6F, 4.0F, 1.0F, 0.0F, -0.1F);
        poseStack.pushPose();
        poseStack.scale(radius, 1.0F, radius);
        LatheMesh.render(poseStack.last(), buffers.getBuffer(RenderType.entityTranslucent(WaterVisuals.WATER)), crown, ragged, water,
                6, 32, age, light);
        poseStack.popPose();
    }

    /**
     * Heaps of billows set round the zone's centre: fog, dust, clouds.
     *
     * @param tallness how tall each heap is, in zone radii
     * @param height   how high above the ground they float
     * @param spread   how far out they stand, in zone radii
     */
    private static void banks(SpellZone zone, float age, PoseStack poseStack, MultiBufferSource buffers, float red, float green,
                              float blue, float spin, float tallness, float height, int banks, float spread) {
        banks(zone, age, poseStack, buffers, red, green, blue, spin, tallness, height, banks, spread, 1.0F);
    }

    /** @param opacity how much more opaque than the usual fog bank (Deep Mist: thick) */
    private static void banks(SpellZone zone, float age, PoseStack poseStack, MultiBufferSource buffers, float red, float green,
                              float blue, float spin, float tallness, float height, int banks, float spread, float opacity) {
        float presence = presence(zone, age);
        float radius = zone.visualRadius() * spread;
        for (int i = 0; i < banks; i++) {
            float angle = i * Mth.TWO_PI / banks + age * spin;
            float distance = radius * (i % 2 == 0 ? 0.45F : 0.75F);
            List<BillowCluster.Billow> billows = BillowCluster.dome(zone.getId() * 13L + i);
            poseStack.pushPose();
            poseStack.translate(Mth.cos(angle) * distance, height, Mth.sin(angle) * distance);
            poseStack.scale(radius * 0.7F, radius * tallness, radius * 0.7F);
            BillowCluster.render(poseStack, buffers.getBuffer(RenderType.entityTranslucent(FOG)), billows,
                    Mth.clamp(age / 20.0F, 0.0F, 1.0F), 0.05F,
                    new LatheMesh.Material(red, green, blue, Math.min(1.0F, FOG_BANK.alphaFacing() * presence * opacity),
                            Math.min(1.0F, FOG_BANK.alphaRim() * presence * opacity), FOG_BANK.rimWhiten(), 1.0F, 1.0F, 0.003F, 0.002F),
                    age + i * 7.0F, LightTexture.FULL_BRIGHT);
            poseStack.popPose();
        }
    }
}
