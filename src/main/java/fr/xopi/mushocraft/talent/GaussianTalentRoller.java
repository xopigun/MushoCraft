package fr.xopi.mushocraft.talent;

import fr.xopi.mushocraft.core.MushoConfig;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.EnumMap;
import java.util.Map;

/**
 * Rolls every talent independently on a normal distribution, clamped to [min, max].
 * Most characters end up close to the mean; exceptional gifts are rare.
 */
public final class GaussianTalentRoller implements TalentRoller {
    private final double mean;
    private final double standardDeviation;
    private final double min;
    private final double max;

    public GaussianTalentRoller(double mean, double standardDeviation, double min, double max) {
        this.mean = mean;
        this.standardDeviation = standardDeviation;
        this.min = Math.min(min, max);
        this.max = Math.max(min, max);
    }

    public static GaussianTalentRoller fromConfig() {
        return new GaussianTalentRoller(
                MushoConfig.TALENT_MEAN.get(),
                MushoConfig.TALENT_STD_DEV.get(),
                MushoConfig.TALENT_MIN.get(),
                MushoConfig.TALENT_MAX.get());
    }

    @Override
    public Talents roll(RandomSource random) {
        Map<Talent, Double> values = new EnumMap<>(Talent.class);
        for (Talent talent : Talent.values()) {
            values.put(talent, Mth.clamp(mean + random.nextGaussian() * standardDeviation, min, max));
        }
        return new Talents(values);
    }
}
