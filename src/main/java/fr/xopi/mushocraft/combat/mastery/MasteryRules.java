package fr.xopi.mushocraft.combat.mastery;

/**
 * How repeating a move turns into mastery. Gameplay choices, not numbers from the novel (docs/ECARTS.md):
 * <ul>
 *     <li>A move is mastered after {@link #REPETITIONS_TO_MASTER} repetitions.</li>
 *     <li>A fresh move fails one time in two; the risk falls quickly with practice and vanishes once mastered.</li>
 *     <li>A fresh move has 60% of its strength and is a third slower (chant, swing); both reach 100% with mastery.</li>
 * </ul>
 */
public final class MasteryRules {
    public static final double REPETITIONS_TO_MASTER = 50.0;
    /** Repetition credited by a failed attempt: one learns from mistakes too, but less. */
    public static final double FAILED_ATTEMPT = 0.5;
    private static final double FRESH_FAILURE = 0.5;
    private static final double FRESH_POWER = 0.6;
    private static final double FRESH_SPEED = 0.75;

    private MasteryRules() {
    }

    /** Progress towards mastery, in [0, 1]. */
    public static double progress(double repetitions) {
        return Math.clamp(repetitions / REPETITIONS_TO_MASTER, 0.0, 1.0);
    }

    public static boolean isMastered(double repetitions) {
        return repetitions >= REPETITIONS_TO_MASTER;
    }

    public static double failureChance(double repetitions) {
        double missing = 1.0 - progress(repetitions);
        return FRESH_FAILURE * missing * missing;
    }

    public static double powerFactor(double repetitions) {
        return FRESH_POWER + (1.0 - FRESH_POWER) * progress(repetitions);
    }

    /** Speed of execution relative to a mastered move: below 1, the move takes longer. */
    public static double speedFactor(double repetitions) {
        return FRESH_SPEED + (1.0 - FRESH_SPEED) * progress(repetitions);
    }
}
