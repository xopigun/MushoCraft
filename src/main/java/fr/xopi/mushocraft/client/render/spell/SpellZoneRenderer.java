package fr.xopi.mushocraft.client.render.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.client.render.mesh.LatheMesh;
import fr.xopi.mushocraft.magic.entity.SpellZone;
import fr.xopi.mushocraft.magic.entity.ZoneVisual;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Draws lasting spells that have a body (the others are drawn by {@link ZoneBodies}). Tornado: a towering funnel, narrow at the ground and wide in the sky, made of
 * two layers of wind that wind round in opposite directions; the column sways, rises out of the ground when the spell
 * begins and thins out when it ends. The wind texture (textures/entity/spell/wind_streaks.png) is drawn for MushoCraft.
 */
public class SpellZoneRenderer extends EntityRenderer<SpellZone> {
    private static final ResourceLocation WIND = MushoCraft.id("textures/entity/spell/wind_streaks.png");
    private static final LatheMesh.Material OUTER_WIND = new LatheMesh.Material(0.86F, 0.9F, 0.94F, 0.18F, 0.62F, 0.3F,
            3.0F, 2.0F, 0.09F, -0.05F);
    private static final LatheMesh.Material INNER_WIND = new LatheMesh.Material(0.7F, 0.74F, 0.8F, 0.28F, 0.5F, 0.1F,
            2.0F, 3.0F, -0.14F, -0.08F);
    /** Height of the funnel, in zone radii. */
    private static final float HEIGHT_PER_RADIUS = 2.6F;
    private static final int RISE_TICKS = 20;
    private static final int FADE_TICKS = 20;

    public SpellZoneRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(SpellZone zone, Frustum frustum, double x, double y, double z) {
        if (zone.visual() == ZoneVisual.NONE) {
            return false;
        }
        if (zone.followed() != null || zone.visual() == ZoneVisual.STORM_CLOUD) {
            return true;
        }
        // the funnel reaches far beyond the zone's tiny hitbox
        float reach = zone.visualRadius() * 1.3F;
        return frustum.isVisible(new AABB(zone.getX() - reach, zone.getY(), zone.getZ() - reach,
                zone.getX() + reach, zone.getY() + reach * HEIGHT_PER_RADIUS, zone.getZ() + reach));
    }

    @Override
    public void render(SpellZone zone, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int light) {
        float age = zone.tickCount + partialTicks;
        poseStack.pushPose();
        Entity followed = zone.followed();
        if (followed != null) {
            // drawn on the entity it follows, which the client knows far better than the zone itself
            Vec3 offset = followed.getPosition(partialTicks).subtract(zone.getPosition(partialTicks));
            poseStack.translate(offset.x, offset.y, offset.z);
        }
        switch (zone.visual()) {
            case TORNADO -> renderTornado(zone, age, poseStack, buffers);
            case WARD_MAGIC -> ZoneBodies.ward(zone, age, partialTicks, poseStack, buffers, 0x7FD4FF);
            case WARD_PHYSICAL -> ZoneBodies.ward(zone, age, partialTicks, poseStack, buffers, 0xE0B070);
            case SEALING_DOME -> ZoneBodies.sealingDome(zone, age, poseStack, buffers);
            case CONFINEMENT_CELL -> ZoneBodies.confinementCell(zone, age, poseStack, buffers);
            case WATERFALL -> ZoneBodies.waterfall(zone, age, poseStack, buffers, light);
            case FLOOD -> ZoneBodies.flood(zone, age, poseStack, buffers, light);
            case MIST -> ZoneBodies.mist(zone, age, poseStack, buffers, light);
            case DUST_STORM -> ZoneBodies.dustStorm(zone, age, poseStack, buffers, light);
            case STORM_CLOUD -> ZoneBodies.stormCloud(zone, age, poseStack, buffers);
            case THUNDERHEAD -> ZoneBodies.thunderhead(zone, age, poseStack, buffers, 8.5F, 30.0F);
            case CHARGED_CLOUD -> ZoneBodies.chargedCloud(zone, age, poseStack, buffers);
            case SPLASH_CROWN -> ZoneBodies.splashCrown(zone, age, poseStack, buffers, light);
            case FLAME_JET -> {
                if (followed != null) {
                    FireVisuals.renderJet(zone, followed, age, partialTicks, poseStack, buffers);
                }
            }
            case NONE -> {
            }
        }
        poseStack.popPose();
        super.render(zone, yaw, partialTicks, poseStack, buffers, light);
    }

    private static void renderTornado(SpellZone zone, float age, PoseStack poseStack, MultiBufferSource buffers) {
        float radius = zone.visualRadius();
        float rise = smooth(Mth.clamp(age / RISE_TICKS, 0.0F, 1.0F));
        float fade = 1.0F - smooth(Mth.clamp((age - (zone.visualLifetime() - FADE_TICKS)) / FADE_TICKS, 0.0F, 1.0F));
        float height = radius * HEIGHT_PER_RADIUS * rise;
        if (height < 0.1F || fade <= 0.0F) {
            return;
        }
        poseStack.pushPose();
        poseStack.scale(1.0F, height / 2.0F, 1.0F);
        poseStack.translate(0.0, 1.0, 0.0);
        // funnel: t = 0 at the ground (narrow), 1 in the sky (wide)
        LatheMesh.Profile funnel = t -> new float[]{t * 2.0F - 1.0F, (0.12F + 0.88F * (float) Math.pow(t, 1.7)) * radius * fade};
        LatheMesh.Deform twist = (point, t, angle) -> {
            float turn = t * 3.0F + age * 0.18F;
            float cos = Mth.cos(turn);
            float sin = Mth.sin(turn);
            float x = point.x * cos - point.z * sin;
            float z = point.x * sin + point.z * cos;
            // the column sways, more at the top; its wall bulges in rolling bands
            float sway = t * t * radius * 0.25F;
            float band = 1.0F + 0.12F * Mth.sin(t * 22.0F - age * 0.6F + angle * 2.0F);
            point.set(x * band + Mth.sin(age * 0.07F + t * 2.0F) * sway, point.y, z * band + Mth.cos(age * 0.05F + t * 2.0F) * sway);
        };
        int glow = LightTexture.FULL_BRIGHT;
        LatheMesh.render(poseStack.last(), buffers.getBuffer(RenderType.entityTranslucent(WIND)), funnel, twist, INNER_WIND,
                24, 28, age, glow);
        poseStack.pushPose();
        poseStack.scale(1.15F, 1.0F, 1.15F);
        LatheMesh.render(poseStack.last(), buffers.getBuffer(RenderType.entityTranslucent(WIND)), funnel, twist, OUTER_WIND,
                24, 32, -age, glow);
        poseStack.popPose();
        poseStack.popPose();
    }

    private static float smooth(float t) {
        return t * t * (3.0F - 2.0F * t);
    }

    @Override
    public ResourceLocation getTextureLocation(SpellZone zone) {
        return WIND;
    }
}
