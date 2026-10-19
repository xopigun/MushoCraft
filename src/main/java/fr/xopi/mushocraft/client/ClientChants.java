package fr.xopi.mushocraft.client;

import fr.xopi.mushocraft.magic.spell.SpellSchool;
import net.minecraft.client.Minecraft;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The chants under way that this client can see, by entity id: which school, since when, for how long.
 */
public final class ClientChants {
    public record Chant(SpellSchool school, long startTick, int ticks) {
        /** From 0 when the chant starts to 1 when the spell takes shape. */
        public float progress(long gameTime, float partialTick) {
            return ticks <= 0 ? 1.0F : Math.min(1.0F, (gameTime - startTick + partialTick) / ticks);
        }
    }

    private static final Map<Integer, Chant> CHANTS = new ConcurrentHashMap<>();

    private ClientChants() {
    }

    public static void update(int entityId, int school, int ticks) {
        if (school < 0 || school >= SpellSchool.values().length || Minecraft.getInstance().level == null) {
            CHANTS.remove(entityId);
            return;
        }
        CHANTS.put(entityId, new Chant(SpellSchool.values()[school], Minecraft.getInstance().level.getGameTime(), ticks));
    }

    public static Map<Integer, Chant> all() {
        return CHANTS;
    }

    public static void clear() {
        CHANTS.clear();
    }
}
