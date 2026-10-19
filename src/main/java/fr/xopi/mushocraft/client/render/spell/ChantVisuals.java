package fr.xopi.mushocraft.client.render.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.client.ClientChants;
import fr.xopi.mushocraft.client.particle.ManaMote;
import fr.xopi.mushocraft.client.render.mesh.LatheMesh;
import fr.xopi.mushocraft.core.MushoParticles;
import fr.xopi.mushocraft.magic.spell.SpellSchool;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Vector3f;

import java.util.Map;

/**
 * A spell taking shape while it is chanted, as in the anime: motes of mana in the school's colour are drawn into the
 * caster's hand, where a glowing orb gathers and grows until the spell is released. Water and fire already look like
 * water and fire; other schools are a swirl of coloured mana. Drawn for every caster this client sees, players and
 * creatures alike (see {@link ClientChants}).
 */
@EventBusSubscriber(modid = MushoCraft.MODID, value = Dist.CLIENT)
public final class ChantVisuals {
    private static final ResourceLocation MANA = MushoCraft.id("textures/entity/spell/mana_surface.png");
    private static final float MAX_ORB_RADIUS = 0.2F;
    private static final int GLOW = 0xF000F0;

    private ChantVisuals() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            ClientChants.clear();
            return;
        }
        if (minecraft.isPaused()) {
            return;
        }
        RandomSource random = level.random;
        for (Map.Entry<Integer, ClientChants.Chant> entry : ClientChants.all().entrySet()) {
            Entity entity = level.getEntity(entry.getKey());
            if (!(entity instanceof LivingEntity caster) || !caster.isAlive()) {
                ClientChants.all().remove(entry.getKey());
                continue;
            }
            Vec3 hand = handPosition(caster, 1.0F, minecraft.gameRenderer.getMainCamera());
            float progress = entry.getValue().progress(level.getGameTime(), 0.0F);
            ColorParticleOption colour = ColorParticleOption.create(MushoParticles.MANA_MOTE.get(),
                    0xFF000000 | entry.getValue().school().colour());
            int motes = 1 + Math.round(progress * 2.0F);
            for (int i = 0; i < motes; i++) {
                // from a shell around the hand, inwards, arriving as the mote dies
                Vec3 offset = new Vec3(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5)
                        .normalize().scale(0.7 + random.nextDouble() * 0.5);
                Vec3 start = hand.add(offset);
                Vec3 speed = offset.scale(-1.0 / ManaMote.LIFETIME);
                level.addParticle(colour, start.x, start.y, start.z, speed.x, speed.y, speed.z);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES || ClientChants.all().isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Camera camera = event.getCamera();
        Vec3 eye = camera.getPosition();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        PoseStack poseStack = event.getPoseStack();
        for (Map.Entry<Integer, ClientChants.Chant> entry : ClientChants.all().entrySet()) {
            if (!(level.getEntity(entry.getKey()) instanceof LivingEntity caster)) {
                continue;
            }
            float progress = entry.getValue().progress(level.getGameTime(), partialTick);
            Vec3 hand = handPosition(caster, partialTick, camera);
            poseStack.pushPose();
            poseStack.translate(hand.x - eye.x, hand.y - eye.y, hand.z - eye.z);
            renderOrb(poseStack, buffers, entry.getValue().school(), progress, caster.tickCount + partialTick);
            poseStack.popPose();
        }
        buffers.endBatch();
    }

    /** The orb gathering in the hand: small and faint at first, full and bright when the spell is about to be released. */
    private static void renderOrb(PoseStack poseStack, MultiBufferSource buffers, SpellSchool school, float progress, float time) {
        float radius = MAX_ORB_RADIUS * (0.25F + 0.75F * progress) * (1.0F + 0.06F * Mth.sin(time * 0.8F));
        int rgb = school.colour();
        float red = (rgb >> 16 & 0xFF) / 255.0F;
        float green = (rgb >> 8 & 0xFF) / 255.0F;
        float blue = (rgb & 0xFF) / 255.0F;
        ResourceLocation texture = switch (school) {
            case WATER -> WaterVisuals.WATER;
            case FIRE -> FireVisuals.FIRE;
            default -> MANA;
        };
        LatheMesh.Deform swirl = (point, t, angle) -> {
            float wave = 1.0F + 0.1F * Mth.sin(angle * 4.0F + time * 0.7F + point.y * 3.0F);
            point.mul(wave, 1.0F, wave);
        };
        float glow = 0.35F + 0.6F * progress;
        poseStack.pushPose();
        poseStack.scale(radius, radius, radius);
        LatheMesh.render(poseStack.last(), buffers.getBuffer(RenderType.entityTranslucentEmissive(texture)), LatheMesh.SPHERE, swirl,
                new LatheMesh.Material(Math.min(1.0F, red + 0.35F), Math.min(1.0F, green + 0.35F), Math.min(1.0F, blue + 0.35F),
                        glow, glow * 0.4F, 0.3F, 2.0F, 1.0F, 0.03F, -0.05F), 10, 14, time, GLOW);
        // a faint halo of gathered mana around it
        poseStack.scale(1.6F, 1.6F, 1.6F);
        LatheMesh.render(poseStack.last(), buffers.getBuffer(RenderType.entityTranslucentEmissive(texture)), LatheMesh.SPHERE, swirl,
                new LatheMesh.Material(red, green, blue, glow * 0.35F, 0.0F, 0.0F, 3.0F, 1.0F, -0.04F, 0.06F), 10, 14, time, GLOW);
        poseStack.popPose();
    }

    /**
     * Where the spell forms: the caster's right hand held out before it. For the player seen in first person, a point
     * low and to the right of the view, where the hand is.
     */
    static Vec3 handPosition(LivingEntity caster, float partialTick, Camera camera) {
        Minecraft minecraft = Minecraft.getInstance();
        if (caster == minecraft.player && minecraft.options.getCameraType().isFirstPerson()) {
            Vector3f look = camera.getLookVector();
            Vector3f left = camera.getLeftVector();
            Vector3f up = camera.getUpVector();
            return camera.getPosition().add(look.x * 0.75 - left.x * 0.32 - up.x * 0.22,
                    look.y * 0.75 - left.y * 0.32 - up.y * 0.22, look.z * 0.75 - left.z * 0.32 - up.z * 0.22);
        }
        float yaw = Mth.lerp(partialTick, caster.yBodyRotO, caster.yBodyRot) * Mth.DEG_TO_RAD;
        Vec3 forward = new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
        Vec3 right = new Vec3(-Mth.cos(yaw), 0.0, -Mth.sin(yaw));
        return caster.getPosition(partialTick).add(0.0, caster.getBbHeight() * 0.62, 0.0)
                .add(forward.scale(0.5 * caster.getScale())).add(right.scale(0.3 * caster.getScale()));
    }
}
