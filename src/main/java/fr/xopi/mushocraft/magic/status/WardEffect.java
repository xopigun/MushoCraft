package fr.xopi.mushocraft.magic.status;

import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * A protective status given by barrier magic: damage of the warded kinds is reduced while it lasts.
 * Magic Shield wards off magic, heat and cold; Physical Shield wards off blows and missiles
 * (wiki, page <i>Magic Spells</i>, Barrier). The reduction is applied by {@link WardHandler}.
 */
public class WardEffect extends MobEffect {
    /** Fraction of the damage that still gets through, at amplifier 0. */
    public static final float DAMAGE_TAKEN = 0.4F;

    private final TagKey<DamageType> warded;

    public WardEffect(int color, TagKey<DamageType> warded) {
        super(MobEffectCategory.BENEFICIAL, color);
        this.warded = warded;
    }

    public TagKey<DamageType> warded() {
        return warded;
    }

    public static float damageTaken(int amplifier) {
        return (float) Math.pow(DAMAGE_TAKEN, amplifier + 1);
    }
}
