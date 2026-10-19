package fr.xopi.mushocraft.client;

import fr.xopi.mushocraft.combat.move.CooldownView;
import fr.xopi.mushocraft.combat.move.MoveLoadout;
import fr.xopi.mushocraft.combat.move.MoveRef;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Last move bar state received from the server. Cooldowns are kept in wall-clock time from reception,
 * since the client has no shared clock with the server. Plain data only (written by a packet handler).
 */
public final class ClientMoveState {
    private static final double MILLIS_PER_TICK = 50.0;

    private static volatile MoveLoadout loadout = MoveLoadout.empty();
    private static volatile List<MoveRef> known = List.of();
    private static volatile Map<ResourceLocation, ClientCooldown> cooldowns = Map.of();

    private ClientMoveState() {
    }

    public static void update(MoveLoadout newLoadout, List<CooldownView> newCooldowns, List<MoveRef> newKnown) {
        long now = System.currentTimeMillis();
        Map<ResourceLocation, ClientCooldown> map = new HashMap<>();
        newCooldowns.forEach(view -> map.put(view.key(),
                new ClientCooldown(now + (long) (view.remainingTicks() * MILLIS_PER_TICK), view.totalTicks())));
        loadout = newLoadout;
        known = List.copyOf(newKnown);
        cooldowns = Map.copyOf(map);
    }

    public static MoveLoadout loadout() {
        return loadout;
    }

    public static List<MoveRef> known() {
        return known;
    }

    /** Fraction of the cooldown still to wait, from 1 (just started) to 0 (ready). */
    public static float cooldownFraction(ResourceLocation key) {
        ClientCooldown cooldown = cooldowns.get(key);
        if (cooldown == null || cooldown.totalTicks() <= 0) {
            return 0.0F;
        }
        double remainingTicks = (cooldown.endMillis() - System.currentTimeMillis()) / MILLIS_PER_TICK;
        return (float) Math.clamp(remainingTicks / cooldown.totalTicks(), 0.0, 1.0);
    }

    private record ClientCooldown(long endMillis, int totalTicks) {
    }
}
