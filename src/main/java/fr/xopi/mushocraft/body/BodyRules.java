package fr.xopi.mushocraft.body;

import fr.xopi.mushocraft.core.Rank;
import fr.xopi.mushocraft.life.LifeStage;

/**
 * How training turns into a bonus. No numbers come from the novel: these are gameplay choices (docs/ECARTS.md, E8).
 * <ul>
 *     <li>Every human starts from the same base; the natural talent shifts the start a little (born strong or frail).</li>
 *     <li>Training gives diminishing returns towards a ceiling, and the talent scales that ceiling:
 *     the gift at birth stays decisive.</li>
 *     <li>A child's body cannot use its whole potential: the bonus grows with the body until adulthood.</li>
 * </ul>
 */
public final class BodyRules {
    /** Training experience at which a stat has reached about 63% of its ceiling. */
    private static final double TRAINING_SCALE = 2_000.0;
    /** Share of the ceiling given by the talent alone, before any training (positive or negative). */
    private static final double TALENT_HEAD_START = 0.25;
    /** Strength of a newborn's body relative to an adult's. */
    private static final double NEWBORN_BODY = 0.3;

    private BodyRules() {
    }

    /**
     * Fraction of the stat's maximal bonus reached, in [-0.25, talent]: negative for a frail, untrained character.
     */
    public static double progress(double experience, double talent) {
        double trained = 1.0 - Math.exp(-experience / TRAINING_SCALE);
        double headStart = (talent - 1.0) * TALENT_HEAD_START;
        return headStart + trained * talent;
    }

    /** How much of its potential the body can use at this age: from 30% at birth to 100% at adulthood. */
    public static double bodyMaturity(double ageInYears) {
        double progress = Math.clamp(ageInYears / LifeStage.ADULT.startAge(), 0.0, 1.0);
        return NEWBORN_BODY + progress * (1.0 - NEWBORN_BODY);
    }

    /** Body level a master demands before teaching a rank (average share of the four stats; gameplay choice). */
    private static final double[] BODY_LEVEL_BY_RANK = {0.0, 0.15, 0.3, 0.5, 0.7, 0.85, 1.0};

    public static double requiredBodyLevel(Rank rank) {
        return BODY_LEVEL_BY_RANK[rank.ordinal()];
    }

    /** Children learn their bodies faster: training gains are doubled before adolescence. */
    public static double trainingSpeed(double ageInYears) {
        return ageInYears < LifeStage.ADOLESCENT.startAge() ? 2.0 : 1.0;
    }
}
