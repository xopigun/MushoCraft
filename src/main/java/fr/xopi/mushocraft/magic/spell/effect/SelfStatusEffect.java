package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.magic.entity.SpellZone;
import fr.xopi.mushocraft.magic.entity.ZoneVisual;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import fr.xopi.mushocraft.magic.spell.zone.ZoneBehaviors;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * Wraps the caster in a lasting status (Magic Shield). The status can be shown as a body around the caster (a ward of
 * hexagons) that lasts as long as it does.
 */
public final class SelfStatusEffect implements SpellEffect {
    private static final double WARD_RADIUS = 1.15;

    private final Holder<MobEffect> status;
    private final int durationTicks;
    private final int amplifier;
    private final SoundEvent sound;
    private final ZoneVisual shownAs;

    public SelfStatusEffect(Holder<MobEffect> status, int durationTicks, int amplifier, SoundEvent sound) {
        this(status, durationTicks, amplifier, sound, ZoneVisual.NONE);
    }

    private SelfStatusEffect(Holder<MobEffect> status, int durationTicks, int amplifier, SoundEvent sound, ZoneVisual shownAs) {
        this.status = status;
        this.durationTicks = durationTicks;
        this.amplifier = amplifier;
        this.sound = sound;
        this.shownAs = shownAs;
    }

    /** The same status, seen around the caster as long as it lasts. */
    public SelfStatusEffect shownAs(ZoneVisual visual) {
        return new SelfStatusEffect(status, durationTicks, amplifier, sound, visual);
    }

    @Override
    public boolean cast(SpellContext context) {
        int duration = (int) Math.round(durationTicks * context.power());
        context.caster().addEffect(new MobEffectInstance(status, duration, amplifier));
        context.level().playSound(null, context.caster().blockPosition(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (shownAs != ZoneVisual.NONE) {
            SpellZone ward = SpellZone.create(context.level(), context.caster(), ZoneBehaviors.ward(status), shownAs, WARD_RADIUS,
                    context.power(), duration).following(context.caster());
            ward.setPos(context.caster().position());
            context.level().addFreshEntity(ward);
        }
        return true;
    }
}
