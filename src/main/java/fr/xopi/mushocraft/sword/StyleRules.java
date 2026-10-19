package fr.xopi.mushocraft.sword;

import fr.xopi.mushocraft.body.aura.AuraLevel;
import fr.xopi.mushocraft.core.Rank;

/**
 * Rules of progression in a sword style.
 * <ul>
 *     <li>Experience thresholds per rank: gameplay choices, the same scale as magic.</li>
 *     <li>Practice alone leads up to Intermediate; above, a master of at least that rank must grant the rank
 *     (decision of 19/10/2026).</li>
 *     <li>Advanced requires conscious control of the Battle Aura, Saint and above its complete control
 *     (wiki: « A standard Advanced practitioner can self consciously use their Battle Aura »; a standard Sword Saint
 *     completely controls it).</li>
 *     <li>The cooldown shared by a style's techniques shrinks as the style is mastered.</li>
 * </ul>
 */
public final class StyleRules {
    private static final double[] THRESHOLDS = {0.0, 150.0, 800.0, 4_000.0, 15_000.0, 50_000.0, 150_000.0};
    private static final double COOLDOWN_CUT_PER_RANK = 0.08;
    /** Practice alone leads up to this rank; beyond, a master must grant each rank. */
    public static final Rank HIGHEST_SELF_TAUGHT = Rank.INTERMEDIATE;

    private StyleRules() {
    }

    public static double threshold(Rank rank) {
        return THRESHOLDS[rank.ordinal()];
    }

    public static AuraLevel requiredAura(Rank rank) {
        if (rank.isAtLeast(Rank.SAINT)) {
            return AuraLevel.COMPLETE;
        }
        return rank.isAtLeast(Rank.ADVANCED) ? AuraLevel.CONSCIOUS : AuraLevel.NONE;
    }

    public static double cooldownFactor(Rank styleRank) {
        return Math.max(0.4, 1.0 - COOLDOWN_CUT_PER_RANK * styleRank.ordinal());
    }
}
