package fr.xopi.mushocraft.combat.mastery;

/**
 * How repeating one move turns into mastery: how many repetitions master it, how often it fails, how weak and how
 * slow it is when fresh; all three fade with practice. Most moves follow {@link #STANDARD}. Inherent magic, which a
 * pupil reproduces only after understanding its theory and long, specialised training (wiki, page <i>Magic</i> §
 * Inherent Magic), follows {@link #LONG_TRAINING}: it almost always fails at first and takes six times the practice.
 * Gameplay choices, not numbers from the novel (docs/ECARTS.md).
 *
 * @param repetitionsToMaster repetitions after which the move is mastered
 * @param freshFailure        chance that a fresh move fails
 * @param freshPower          strength of a fresh move, as a share of a mastered one
 * @param freshSpeed          speed of a fresh move, as a share of a mastered one
 */
public record MasteryCurve(double repetitionsToMaster, double freshFailure, double freshPower, double freshSpeed) {
    public static final MasteryCurve STANDARD = new MasteryCurve(50.0, 0.5, 0.6, 0.75);
    public static final MasteryCurve LONG_TRAINING = new MasteryCurve(300.0, 0.9, 0.4, 0.75);

    /** Progress towards mastery, in [0, 1]. */
    public double progress(double repetitions) {
        return Math.clamp(repetitions / repetitionsToMaster, 0.0, 1.0);
    }

    public boolean isMastered(double repetitions) {
        return repetitions >= repetitionsToMaster;
    }

    /** The risk falls quickly with practice and vanishes once mastered. */
    public double failureChance(double repetitions) {
        double missing = 1.0 - progress(repetitions);
        return freshFailure * missing * missing;
    }

    public double powerFactor(double repetitions) {
        return freshPower + (1.0 - freshPower) * progress(repetitions);
    }

    /** Speed of execution relative to a mastered move: below 1, the move takes longer. */
    public double speedFactor(double repetitions) {
        return freshSpeed + (1.0 - freshSpeed) * progress(repetitions);
    }
}
