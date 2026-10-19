package fr.xopi.mushocraft.sword.technique;

import fr.xopi.mushocraft.compat.epicfight.EpicFightAnimations;
import fr.xopi.mushocraft.magic.fx.SpellFx;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import yesman.epicfight.gameasset.Animations;

/**
 * How a receiving stance answers the blow it awaited (Strategy). Each style's counter does it its own way.
 */
public enum CounterKind {
    /**
     * Water God Style, Flow: the blow is caught with the weapon, its force redirected, and answered.
     */
    FLOW {
        @Override
        public void answer(LivingEntity defender, LivingEntity attacker, LivingIncomingDamageEvent event, float multiplier) {
            event.setCanceled(true);
            EpicFightAnimations.play(defender, Animations.LONGSWORD_GUARD_ACTIVE_HIT1);
            attacker.hurt(blow(defender), strength(defender) * multiplier);
            defender.level().playSound(null, defender.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.6F, 1.6F);
        }
    },
    /**
     * Sword God Style, Light Reversal: before the enemy's stroke reaches full speed, one's own Longsword of Light cuts
     * off the wrist that swings it (wiki). The blow never lands, the attacker loses the use of its arm.
     */
    LIGHT_REVERSAL {
        @Override
        public void answer(LivingEntity defender, LivingEntity attacker, LivingIncomingDamageEvent event, float multiplier) {
            event.setCanceled(true);
            EpicFightAnimations.play(defender, Animations.BATTOJUTSU);
            attacker.hurt(blow(defender), strength(defender) * multiplier);
            Disarm.apply(attacker, 160, 2);
            if (defender.level() instanceof ServerLevel level) {
                SpellFx.slash(level, defender, attacker.position().subtract(defender.position()).normalize(), 1.4F, 0xFFF6D8);
            }
            defender.level().playSound(null, defender.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.4F);
        }
    },
    /**
     * North God Style, Pecking: a secret counter that crushes the opponent's fist at the cost of one's own fingers
     * (wiki). The defender still takes a part of the blow; the attacker's hand is ruined.
     */
    PECKING {
        @Override
        public void answer(LivingEntity defender, LivingEntity attacker, LivingIncomingDamageEvent event, float multiplier) {
            event.setAmount(event.getAmount() * 0.35F);
            attacker.hurt(blow(defender), strength(defender) * multiplier);
            Disarm.apply(attacker, 300, 3);
            attacker.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 300, 2));
            defender.level().playSound(null, defender.blockPosition(), SoundEvents.SKELETON_HURT, SoundSource.PLAYERS, 1.0F, 0.6F);
            defender.level().playSound(null, defender.blockPosition(), SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.PLAYERS, 0.5F, 1.6F);
        }
    };

    /** Answers the awaited blow from `attacker`; the event is the incoming damage on the defender. */
    public abstract void answer(LivingEntity defender, LivingEntity attacker, LivingIncomingDamageEvent event, float multiplier);

    static DamageSource blow(LivingEntity defender) {
        return defender instanceof Player player ? defender.damageSources().playerAttack(player) : defender.damageSources().mobAttack(defender);
    }

    static float strength(LivingEntity defender) {
        return (float) defender.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }
}
