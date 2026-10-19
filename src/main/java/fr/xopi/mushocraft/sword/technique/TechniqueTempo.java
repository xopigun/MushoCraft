package fr.xopi.mushocraft.sword.technique;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.combat.move.CooldownService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
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
    public static void apply(LivingEntity fighter, double speedFactor, int durationTicks) {
        AttributeInstance speed = fighter.getAttribute(Attributes.ATTACK_SPEED);
        if (speed == null) {
            return;
        }
        if (speedFactor >= 1.0) {
            clear(fighter);
            return;
        }
        speed.addOrUpdateTransientModifier(new AttributeModifier(MODIFIER, speedFactor - 1.0,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        ENDS_AT.put(fighter.getUUID(), CooldownService.now(fighter) + durationTicks);
    }

    /** Called every tick: restores the normal pace once the technique is over. */
    public static void tick(LivingEntity fighter) {
        Long end = ENDS_AT.get(fighter.getUUID());
        if (end != null && CooldownService.now(fighter) >= end) {
            clear(fighter);
        }
    }

    public static void clear(LivingEntity fighter) {
        ENDS_AT.remove(fighter.getUUID());
        AttributeInstance speed = fighter.getAttribute(Attributes.ATTACK_SPEED);
        if (speed != null) {
            speed.removeModifier(MODIFIER);
        }
    }
}
