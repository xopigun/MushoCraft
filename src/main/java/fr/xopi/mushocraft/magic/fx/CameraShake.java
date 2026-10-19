package fr.xopi.mushocraft.magic.fx;

import fr.xopi.mushocraft.network.CameraShakePayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Shakes the view of those near a violent spell (an explosion, a lightning bolt, the earth bursting open). The closer
 * they stand, the harder it shakes; the shake fades out by itself.
 */
public final class CameraShake {
    private CameraShake() {
    }

    /**
     * @param intensity how hard it shakes at the centre (1: a solid blast; 3: a cataclysm)
     * @param ticks     how long it lasts at the centre
     * @param radius    beyond this distance nothing is felt
     */
    public static void around(ServerLevel level, Vec3 centre, float intensity, int ticks, double radius) {
        for (ServerPlayer player : level.players()) {
            double distance = player.position().distanceTo(centre);
            if (distance >= radius) {
                continue;
            }
            float felt = (float) (intensity * (1.0 - distance / radius));
            if (felt > 0.05F) {
                PacketDistributor.sendToPlayer(player, new CameraShakePayload(felt, ticks));
            }
        }
    }
}
