package fr.xopi.mushocraft.body;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Running and jumping momentum given by the speed stat (same principle as JujutsuCraft's physical ability):
 * <ul>
 *     <li>Running starts with no boost. A trained runner then gains Speed step by step while still running,
 *     up to a ceiling set by the speed stat; an untrained one never gets any.</li>
 *     <li>Crouching charges a jump step by step, up to a ceiling also set by the speed stat; running at full speed
 *     carries part of the momentum into the jump.</li>
 *     <li>Everything fades within half a second once the player stops.</li>
 * </ul>
 * The boosts are short, hidden effects, renewed while the momentum lasts. A longer effect (a potion) is never
 * touched: the stronger source of speed wins.
 */
public final class SprintMomentum {
    public static final int INTERVAL_TICKS = 2;
    /** Speed level reached by a fully trained adult runner (Speed VII). */
    static final int MAX_SPEED_LEVEL = 7;
    /** Jump Boost level reached by a fully trained adult (Jump Boost IV). */
    static final int MAX_JUMP_LEVEL = 4;
    private static final int BOOST_TICKS = 10;
    /** The boost climbs one level each time it is about to run out. */
    private static final int RENEW_BELOW_TICKS = 5;

    private SprintMomentum() {
    }

    /** Highest Speed amplifier the player's body allows (-1: none). */
    public static int speedCeiling(ServerPlayer player) {
        return ceiling(BodyService.effectiveShare(player, BodyStat.SPEED), MAX_SPEED_LEVEL);
    }

    /** Highest Jump Boost amplifier the player's body allows (-1: none). */
    public static int jumpCeiling(ServerPlayer player) {
        return ceiling(BodyService.effectiveShare(player, BodyStat.SPEED), MAX_JUMP_LEVEL);
    }

    private static int ceiling(double share, int maxLevel) {
        return (int) Math.round(Math.clamp(share, 0.0, 1.5) * maxLevel) - 1;
    }

    /** Runs every {@link #INTERVAL_TICKS} ticks for each player. */
    public static void tick(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        int speed = player.isSprinting() ? build(player, MobEffects.MOVEMENT_SPEED, speedCeiling(player), player.onGround()) : -1;
        if (player.isShiftKeyDown() && player.onGround()) {
            build(player, MobEffects.JUMP, jumpCeiling(player), true);
        } else if (speed >= 0) {
            hold(player, MobEffects.JUMP, Math.min(speed / 2, jumpCeiling(player)));
        }
    }

    /**
     * Starts the boost at level I, or raises it by one level when it is about to run out.
     *
     * @return the boost's amplifier now (-1: none)
     */
    private static int build(ServerPlayer player, Holder<MobEffect> effect, int ceiling, boolean canStart) {
        if (ceiling < 0) {
            return -1;
        }
        int current = current(player, effect);
        if (current < 0) {
            if (!canStart || isForeign(player, effect)) {
                return -1;
            }
            give(player, effect, 0);
            return 0;
        }
        if (player.getEffect(effect).getDuration() <= RENEW_BELOW_TICKS) {
            give(player, effect, Math.min(current + 1, ceiling));
        }
        return current(player, effect);
    }

    /** Keeps a boost at a given level while the momentum lasts. */
    private static void hold(ServerPlayer player, Holder<MobEffect> effect, int amplifier) {
        if (amplifier < 0 || isForeign(player, effect)) {
            return;
        }
        MobEffectInstance instance = player.getEffect(effect);
        if (instance == null || instance.getDuration() <= RENEW_BELOW_TICKS) {
            give(player, effect, amplifier);
        }
    }

    private static int current(ServerPlayer player, Holder<MobEffect> effect) {
        MobEffectInstance instance = player.getEffect(effect);
        return instance == null || isForeign(instance) ? -1 : instance.getAmplifier();
    }

    /** An effect that does not come from the momentum (a potion, a beacon): longer than any boost. */
    private static boolean isForeign(ServerPlayer player, Holder<MobEffect> effect) {
        MobEffectInstance instance = player.getEffect(effect);
        return instance != null && isForeign(instance);
    }

    private static boolean isForeign(MobEffectInstance instance) {
        return instance.isInfiniteDuration() || instance.getDuration() > BOOST_TICKS || instance.isVisible();
    }

    private static void give(ServerPlayer player, Holder<MobEffect> effect, int amplifier) {
        player.addEffect(new MobEffectInstance(effect, BOOST_TICKS, amplifier, false, false, false));
    }
}
