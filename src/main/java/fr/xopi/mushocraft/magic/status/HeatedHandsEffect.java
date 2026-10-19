package fr.xopi.mushocraft.magic.status;

import fr.xopi.mushocraft.magic.fx.SpellFx;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Heat Hand: hands so hot that blows set the target alight (see {@link HeatedHandsHandler}). While it lasts the hands
 * glow, small flames and sparks curling up from them.
 */
public class HeatedHandsEffect extends MobEffect {
    private static final int GLOW_INTERVAL = 3;

    public HeatedHandsEffect(int colour) {
        super(MobEffectCategory.BENEFICIAL, colour);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % GLOW_INTERVAL == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level() instanceof ServerLevel level) {
            SpellFx.heatedHands(level, entity);
        }
        return true;
    }
}
