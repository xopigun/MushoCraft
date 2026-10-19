package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * Wraps the caster in a lasting status (Magic Shield).
 */
public final class SelfStatusEffect implements SpellEffect {
    private final Holder<MobEffect> status;
    private final int durationTicks;
    private final int amplifier;
    private final SoundEvent sound;

    public SelfStatusEffect(Holder<MobEffect> status, int durationTicks, int amplifier, SoundEvent sound) {
        this.status = status;
        this.durationTicks = durationTicks;
        this.amplifier = amplifier;
        this.sound = sound;
    }

    @Override
    public boolean cast(SpellContext context) {
        context.caster().addEffect(new MobEffectInstance(status, (int) Math.round(durationTicks * context.power()), amplifier));
        context.level().playSound(null, context.caster().blockPosition(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }
}
