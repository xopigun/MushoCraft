package fr.xopi.mushocraft.client.fx;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.magic.entity.SpellZone;
import fr.xopi.mushocraft.magic.entity.ZoneVisual;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * Deep Mist seen from inside, as in the anime (S2): a thick white fog where everything a few blocks away fades out.
 * While the eye is in a mist, the world's fog closes in and turns white; it thins at the mist's edge and as the mist
 * fades away.
 */
@EventBusSubscriber(modid = MushoCraft.MODID, value = Dist.CLIENT)
public final class ClientMist {
    /** How far one sees in the heart of the mist, in blocks. */
    private static final float SIGHT = 6.0F;
    private static final float[] WHITE = {0.86F, 0.87F, 0.9F};

    private ClientMist() {
    }

    /** How deep the eye is in a mist: 0 outside, 1 well inside a fully formed one. */
    public static float depth(Camera camera) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return 0.0F;
        }
        Vec3 eye = camera.getPosition();
        float deepest = 0.0F;
        for (SpellZone zone : minecraft.level.getEntitiesOfClass(SpellZone.class, new AABB(eye, eye).inflate(16.0),
                zone -> zone.visual() == ZoneVisual.MIST)) {
            double radius = zone.visualRadius();
            double across = Math.hypot(eye.x - zone.getX(), eye.z - zone.getZ());
            if (across > radius || eye.y < zone.getY() - 1.0 || eye.y > zone.getY() + 5.0) {
                continue;
            }
            float edge = (float) Mth.clamp((radius - across) / (radius * 0.25), 0.0, 1.0);
            float age = zone.tickCount;
            // the fog rolls in within half a second, and lifts over the last second and a half
            float formed = Mth.clamp(age / 10.0F, 0.0F, 1.0F) * Mth.clamp((zone.visualLifetime() - age) / 30.0F, 0.0F, 1.0F);
            deepest = Math.max(deepest, edge * formed);
        }
        return deepest;
    }

    @SubscribeEvent
    public static void onFog(ViewportEvent.RenderFog event) {
        float depth = depth(event.getCamera());
        if (depth <= 0.0F) {
            return;
        }
        // the sight shrinks fast as one goes in: the eased depth reaches the heart's few blocks well before the centre
        float eased = (float) Math.sqrt(depth);
        event.setNearPlaneDistance(Mth.lerp(eased, event.getNearPlaneDistance(), -2.0F));
        event.setFarPlaneDistance(Mth.lerp(eased, event.getFarPlaneDistance(), SIGHT));
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onFogColour(ViewportEvent.ComputeFogColor event) {
        float depth = depth(event.getCamera());
        if (depth <= 0.0F) {
            return;
        }
        float eased = (float) Math.sqrt(depth);
        event.setRed(Mth.lerp(eased, event.getRed(), WHITE[0]));
        event.setGreen(Mth.lerp(eased, event.getGreen(), WHITE[1]));
        event.setBlue(Mth.lerp(eased, event.getBlue(), WHITE[2]));
    }
}
