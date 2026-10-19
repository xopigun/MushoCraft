package fr.xopi.mushocraft.magic.status;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Status given by the Magic Shield spell: a translucent barrier that blocks magical attacks and isolates
 * heat and cold (wiki, page <i>Magic Spells</i>, Barrier). The reduction itself is applied by {@link MagicShieldHandler}.
 */
public class MagicShieldEffect extends MobEffect {
    /** Fraction of the damage that still gets through, at amplifier 0. */
    public static final float DAMAGE_TAKEN = 0.4F;

    public MagicShieldEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x7FD4FF);
    }

    public static float damageTaken(int amplifier) {
        return (float) Math.pow(DAMAGE_TAKEN, amplifier + 1);
    }
}
