package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.core.MushoParticles;
import fr.xopi.mushocraft.magic.entity.SpellZone;
import fr.xopi.mushocraft.magic.entity.ZoneVisual;
import fr.xopi.mushocraft.magic.fx.SpellFx;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import fr.xopi.mushocraft.magic.spell.zone.ZoneBehaviors;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;

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
            // the ward closes round the caster: a ring of its light spreads on the ground, twinkles rise (anime S1: the
            // magenta circle of Physical Shield)
            int colour = shownAs == ZoneVisual.WARD_PHYSICAL ? 0xFF78D8 : 0x7FD4FF;
            SpellFx.shockWave(context.level(), context.caster().position().add(0.0, 0.1, 0.0), new Vec3(0.0, 1.0, 0.0), 1.6F, colour);
            context.level().sendParticles(ColorParticleOption.create(MushoParticles.SPARKLE.get(), 0xFF000000 | colour),
                    context.caster().getX(), context.caster().getY() + 1.0, context.caster().getZ(), 16, 0.7, 0.6, 0.7, 0.02);
        }
        return true;
    }
}
