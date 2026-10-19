package fr.xopi.mushocraft.magic.mana;

import fr.xopi.mushocraft.core.MushoConfig;
import fr.xopi.mushocraft.core.Rank;
import fr.xopi.mushocraft.life.LifeStage;

/**
 * The numbers behind mana, kept free of any game object so they can be tested and tuned on their own.
 * <p>
 * Source (wiki, page <i>Mana</i>): capacity is believed fixed at birth, but Rudeus shows it grows a lot through
 * repeated use and exhaustion during early childhood; his body could hold an extraordinary amount of mana, yet
 * without training he would only have been somewhat above an ordinary person. Hence two quantities:
 * a natural capacity that only follows age, and a trained part that grows with use up to a ceiling set by talent.
 */
public record ManaRules(
        double newbornCapacity,
        double adultCapacity,
        double ceilingBase,
        double growthRate,
        double adultGrowthFactor,
        double exhaustionBonus,
        double regenPerSecond,
        double sleepRegenMultiplier,
        double trainingDrainPerSecond
) {
    /** Growth stays at full speed during early childhood, then slows down until adulthood. */
    private static final double EARLY_CHILDHOOD_END = 5.0;
    /** Spending that leaves less than this fraction of the capacity counts as training to exhaustion. */
    private static final double EXHAUSTION_THRESHOLD = 0.1;

    /** Mana capacity a tutor demands before teaching a rank's spells (gameplay choice, docs/ECARTS.md). */
    private static final double[] CAPACITY_BY_RANK = {0.0, 40.0, 120.0, 300.0, 800.0, 1_500.0, 3_000.0};

    public static double capacityToStudy(Rank rank) {
        return CAPACITY_BY_RANK[rank.ordinal()];
    }

    public static ManaRules fromConfig() {
        return new ManaRules(
                MushoConfig.MANA_NEWBORN_CAPACITY.get(),
                MushoConfig.MANA_ADULT_CAPACITY.get(),
                MushoConfig.MANA_CEILING.get(),
                MushoConfig.MANA_GROWTH_RATE.get(),
                MushoConfig.MANA_ADULT_GROWTH_FACTOR.get(),
                MushoConfig.MANA_EXHAUSTION_BONUS.get(),
                MushoConfig.MANA_REGEN_PER_SECOND.get(),
                MushoConfig.MANA_SLEEP_REGEN_MULTIPLIER.get(),
                MushoConfig.MANA_TRAINING_DRAIN_PER_SECOND.get());
    }

    public double naturalCapacity(double ageInYears, double potential) {
        double progress = Math.clamp(ageInYears / LifeStage.ADULT.startAge(), 0.0, 1.0);
        return (newbornCapacity + progress * (adultCapacity - newbornCapacity)) * potential;
    }

    public double ceiling(double potential) {
        return ceilingBase * potential * potential;
    }

    public double ageGrowthFactor(double ageInYears) {
        if (ageInYears <= EARLY_CHILDHOOD_END) {
            return 1.0;
        }
        double progress = Math.clamp(
                (ageInYears - EARLY_CHILDHOOD_END) / (LifeStage.ADULT.startAge() - EARLY_CHILDHOOD_END), 0.0, 1.0);
        return 1.0 + progress * (adultGrowthFactor - 1.0);
    }

    /**
     * Capacity earned by spending {@code spent} mana.
     *
     * @param remainingAfter mana left once the cost is paid, to reward training to exhaustion
     */
    public double growth(double spent, double ageInYears, double capacity, double ceiling, double remainingAfter) {
        if (spent <= 0.0 || capacity >= ceiling) {
            return 0.0;
        }
        double headroom = 1.0 - capacity / ceiling;
        double exhaustion = remainingAfter <= capacity * EXHAUSTION_THRESHOLD ? exhaustionBonus : 1.0;
        return growthRate * spent * ageGrowthFactor(ageInYears) * headroom * exhaustion;
    }

    public double regeneration(double capacity, double seconds, boolean sleeping) {
        return capacity * regenPerSecond * seconds * (sleeping ? sleepRegenMultiplier : 1.0);
    }
}
