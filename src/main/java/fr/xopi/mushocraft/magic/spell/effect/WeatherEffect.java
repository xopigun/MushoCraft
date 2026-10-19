package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * Gathers clouds and brings down rain (Squall). Fails where the sky cannot rain, or when it already rains.
 */
public final class WeatherEffect implements SpellEffect {
    private final int rainTicks;

    public WeatherEffect(int rainTicks) {
        this.rainTicks = rainTicks;
    }

    @Override
    public boolean cast(SpellContext context) {
        ServerLevel level = context.level();
        if (!level.dimensionType().hasSkyLight() || level.dimensionType().hasCeiling() || level.getLevelData().isRaining()) {
            return false;
        }
        level.setWeatherParameters(0, (int) Math.round(rainTicks * context.power()), true, false);
        level.sendParticles(ParticleTypes.CLOUD, context.caster().getX(), context.caster().getY() + 3.0,
                context.caster().getZ(), 40, 2.0, 0.5, 2.0, 0.02);
        level.playSound(null, context.caster().blockPosition(), SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.WEATHER, 1.0F, 1.0F);
        return true;
    }
}
