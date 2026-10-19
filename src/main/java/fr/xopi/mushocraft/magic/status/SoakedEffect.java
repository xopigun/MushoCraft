package fr.xopi.mushocraft.magic.status;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Soaked by water magic (Water Splash, Waterfall, Flood Flush): fire goes out at once, and freezing magic bites
 * deeper (wiki: Icicle Field is more effective against wet targets; Frost Nova soaks, then freezes).
 */
public class SoakedEffect extends MobEffect {
    public SoakedEffect(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        entity.clearFire();
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
