package fr.xopi.mushocraft.sword;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The style each player fights in, readable on both sides. The server reads it from the player's mastery;
 * a client only knows what {@link StyleSync} told it (its own player and the players it can see).
 * Epic Fight asks on both sides which moveset to use, hence this single entry point.
 */
public final class ActiveStyles {
    private static final Map<Integer, SwordStyle> KNOWN_BY_CLIENT = new ConcurrentHashMap<>();

    private ActiveStyles() {
    }

    public static Optional<SwordStyle> of(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            return SwordService.activeStyle(player);
        }
        return entity.level().isClientSide ? Optional.ofNullable(KNOWN_BY_CLIENT.get(entity.getId())) : Optional.empty();
    }

    /** Client side: what the server says about an entity's style. */
    public static void acceptFromServer(int entityId, Optional<SwordStyle> style) {
        style.ifPresentOrElse(known -> KNOWN_BY_CLIENT.put(entityId, known), () -> KNOWN_BY_CLIENT.remove(entityId));
    }

    /** Client side: forgets everything when leaving a world (entity ids are only valid in one). */
    public static void clearClientView() {
        KNOWN_BY_CLIENT.clear();
    }
}
