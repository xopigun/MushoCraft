package fr.xopi.mushocraft.sword.technique;

import fr.xopi.mushocraft.magic.fx.SpellFx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * Enters the stance of Deprivation Sword Kingdom: for a while, a field around the user answers at once any attack and
 * any incantation made inside it (see {@link SwordDomains}).
 */
public final class DomainAction implements TechniqueAction {
    private final double radius;
    private final int ticks;
    private final float multiplier;

    public DomainAction(double radius, int ticks, float multiplier) {
        this.radius = radius;
        this.ticks = ticks;
        this.multiplier = multiplier;
    }

    /** Taken when foes are near enough to enter the field. */
    @Override
    public double maxRange() {
        return radius;
    }

    @Override
    public boolean perform(TechniqueContext context) {
        SwordDomains.open(context.user(), radius, ticks, (float) (multiplier * context.power()));
        SpellFx.groundRing(context.level(), context.user(), context.user().position(), radius, 12, ParticleTypes.SPLASH);
        context.level().playSound(null, context.user().blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.5F, 0.7F);
        return true;
    }
}
