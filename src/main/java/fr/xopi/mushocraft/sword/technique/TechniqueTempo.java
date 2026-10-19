package fr.xopi.mushocraft.sword.technique;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.combat.move.CooldownService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * How fast a technique is executed: a technique not yet mastered is slower. Epic Fight plays its attack animations
 * at the attack speed, so a short-lived attack speed modifier slows (or restores) the swing while the technique
 * lasts. Attribute changes reach the client on their own, so both sides play the animation at the same pace.
 */
public final class TechniqueTempo {
    private static final ResourceLocation MODIFIER = MushoCraft.id("technique/mastery_tempo");
    private static final Map<UUID, Long> ENDS_AT = new ConcurrentHashMap<>();

    private TechniqueTempo() {
    }

    /**
     * @param speedFactor 1.0 for a mastered technique, below 1.0 for a slower one
     */
    public static void apply(ServerPlayer player, double speedFactor, int durationTicks) {
        AttributeInstance speed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (speed == null) {
            return;
        }
        if (speedFactor >= 1.0) {
            clear(player);
            return;
        }
        speed.addOrUpdateTransientModifier(new AttributeModifier(MODIFIER, speedFactor - 1.0,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        ENDS_AT.put(player.getUUID(), CooldownService.now(player) + durationTicks);
    }

    /** Called every tick: restores the normal pace once the technique is over. */
    public static void tick(ServerPlayer player) {
        Long end = ENDS_AT.get(player.getUUID());
        if (end != null && CooldownService.now(player) >= end) {
            clear(player);
        }
    }

    public static void clear(ServerPlayer player) {
        ENDS_AT.remove(player.getUUID());
        AttributeInstance speed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (speed != null) {
            speed.removeModifier(MODIFIER);
        }
    }
}
