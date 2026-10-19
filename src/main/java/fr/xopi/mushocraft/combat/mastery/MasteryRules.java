package fr.xopi.mushocraft.combat.mastery;

/**
 * How repeating a move turns into mastery, for the moves that follow the {@link MasteryCurve#STANDARD standard curve}.
 * Gameplay choices, not numbers from the novel (docs/ECARTS.md):
 * <ul>
 *     <li>A move is mastered after {@link #REPETITIONS_TO_MASTER} repetitions.</li>
 *     <li>A fresh move fails one time in two; the risk falls quickly with practice and vanishes once mastered.</li>
 *     <li>A fresh move has 60% of its strength and is a third slower (chant, swing); both reach 100% with mastery.</li>
 * </ul>
 * A move may follow another curve ({@link fr.xopi.mushocraft.combat.move.Move#masteryCurve()}): read it from the move.
 */
public final class MasteryRules {
    public static final double REPETITIONS_TO_MASTER = MasteryCurve.STANDARD.repetitionsToMaster();
    /** Repetition credited by a failed attempt: one learns from mistakes too, but less. */
    public static final double FAILED_ATTEMPT = 0.5;

    private MasteryRules() {
    }

    /** Progress towards mastery, in [0, 1]. */
    public static double progress(double repetitions) {
        return MasteryCurve.STANDARD.progress(repetitions);
    }

    public static boolean isMastered(double repetitions) {
        return MasteryCurve.STANDARD.isMastered(repetitions);
    }

    public static double failureChance(double repetitions) {
        return MasteryCurve.STANDARD.failureChance(repetitions);
    }

    public static double powerFactor(double repetitions) {
        return MasteryCurve.STANDARD.powerFactor(repetitions);
    }

    /** Speed of execution relative to a mastered move: below 1, the move takes longer. */
    public static double speedFactor(double repetitions) {
        return MasteryCurve.STANDARD.speedFactor(repetitions);
    }
}
