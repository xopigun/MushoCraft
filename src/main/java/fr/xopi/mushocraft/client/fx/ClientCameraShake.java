package fr.xopi.mushocraft.client.fx;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.client.MushoClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * The shakes the view is going through. Each one rolls and jolts the camera, sinusoidally, and fades out as it runs out;
 * several add up. The client setting {@code cameraShake} scales them all (0 turns them off).
 */
@EventBusSubscriber(modid = MushoCraft.MODID, value = Dist.CLIENT)
public final class ClientCameraShake {
    /** Degrees of roll and of jolt for a shake of intensity 1. */
    private static final float ROLL_DEGREES = 1.6F;
    private static final float JOLT_DEGREES = 0.9F;
    private static final float MAX_INTENSITY = 4.0F;

    private static final List<Shake> SHAKES = new ArrayList<>();

    private ClientCameraShake() {
    }

    public static void add(float intensity, int ticks) {
        if (ticks > 0 && intensity > 0.0F) {
            SHAKES.add(new Shake(Math.min(intensity, MAX_INTENSITY), ticks));
        }
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().isPaused()) {
            return;
        }
        SHAKES.removeIf(Shake::tick);
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        double scale = MushoClientConfig.CAMERA_SHAKE.get();
        if (SHAKES.isEmpty() || scale <= 0.0) {
            return;
        }
        float partial = (float) event.getPartialTick();
        float roll = 0.0F;
        float yaw = 0.0F;
        float pitch = 0.0F;
        for (Shake shake : SHAKES) {
            float strength = shake.strength(partial) * (float) scale;
            float t = shake.age + partial;
            roll += Mth.sin(t * 2.5F) * ROLL_DEGREES * strength;
            yaw += Mth.cos(t * 5.0F) * JOLT_DEGREES * strength;
            pitch += Mth.sin(t * 5.0F + 1.3F) * JOLT_DEGREES * strength;
        }
        event.setRoll(event.getRoll() + roll);
        event.setYaw(event.getYaw() + yaw);
        event.setPitch(event.getPitch() + pitch);
    }

    private static final class Shake {
        private final float intensity;
        private final int duration;
        private int age;

        private Shake(float intensity, int duration) {
            this.intensity = intensity;
            this.duration = duration;
        }

        /** @return true once over */
        private boolean tick() {
            return ++age >= duration;
        }

        /** Strong at first, fading to nothing. */
        private float strength(float partial) {
            float left = 1.0F - Math.min(1.0F, (age + partial) / duration);
            return intensity * left * left;
        }
    }
}
