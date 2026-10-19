package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;

import java.util.List;

/**
 * Removes poisons and illnesses from the caster (Detoxification). Most conditions are curable at the lowest rank
 * (wiki, page <i>Detoxification Magic</i>); stronger poisons will need higher-ranked spells.
 */
public final class CureEffect implements SpellEffect {
    private final List<Holder<MobEffect>> cured;

    public CureEffect(List<Holder<MobEffect>> cured) {
        this.cured = List.copyOf(cured);
    }

    @Override
    public boolean cast(SpellContext context) {
        cured.forEach(context.caster()::removeEffect);
        context.level().sendParticles(ParticleTypes.EFFECT, context.caster().getX(), context.caster().getY() + 1.0,
                context.caster().getZ(), 12, 0.3, 0.5, 0.3, 0.0);
        context.level().playSound(null, context.caster().blockPosition(), SoundEvents.BREWING_STAND_BREW,
                SoundSource.PLAYERS, 0.8F, 1.4F);
        return true;
    }
}
