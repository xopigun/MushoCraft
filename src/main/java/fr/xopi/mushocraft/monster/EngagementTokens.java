package fr.xopi.mushocraft.monster;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Who may close in on a target right now. A pack does not pile onto its prey all at once: a few members engage while
 * the others circle at a distance, snarling, and take their turn when one withdraws. The number of places is set by
 * each species. Server side only; places are held by monster UUID and dropped as soon as the monster lets go.
 */
public final class EngagementTokens {
    private static final Map<LivingEntity, Set<UUID>> ENGAGED = new WeakHashMap<>();

    private EngagementTokens() {
    }

    /** Takes a place against the target if one is free (or already held). */
    public static boolean tryEngage(Mob attacker, LivingEntity target, int places) {
        Set<UUID> engaged = ENGAGED.computeIfAbsent(target, t -> new HashSet<>());
        engaged.removeIf(id -> !stillEngaged(attacker, target, id));
        if (engaged.contains(attacker.getUUID())) {
            return true;
        }
        if (engaged.size() >= places) {
            return false;
        }
        engaged.add(attacker.getUUID());
        return true;
    }

    public static boolean isEngaged(Mob attacker, LivingEntity target) {
        Set<UUID> engaged = ENGAGED.get(target);
        return engaged != null && engaged.contains(attacker.getUUID());
    }

    public static void release(Mob attacker) {
        ENGAGED.values().forEach(engaged -> engaged.remove(attacker.getUUID()));
    }

    /** A place is freed when its holder died, left, or turned to another target. */
    private static boolean stillEngaged(Mob asker, LivingEntity target, UUID holder) {
        if (holder.equals(asker.getUUID())) {
            return true;
        }
        if (!(asker.level() instanceof ServerLevel level)) {
            return false;
        }
        return level.getEntity(holder) instanceof Mob mob && mob.isAlive() && mob.getTarget() == target;
    }
}
