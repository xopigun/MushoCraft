package fr.xopi.mushocraft.sword;

import fr.xopi.mushocraft.body.aura.AuraLevel;
import fr.xopi.mushocraft.core.Rank;

/**
 * Rules of progression in a sword style.
 * <ul>
 *     <li>Only a master grants a rank, one at a time, up to his own (decisions of 19/10/2026): he first asks for a
 *     trained body, then shows each technique of the rank, which must be mastered by repetition.</li>
 *     <li>Advanced requires conscious control of the Battle Aura, Saint and above its complete control
 *     (wiki: « A standard Advanced practitioner can self consciously use their Battle Aura »; a standard Sword Saint
 *     completely controls it).</li>
 *     <li>The cooldown shared by a style's techniques shrinks as the style is mastered.</li>
 * </ul>
 */
public final class StyleRules {
    private static final double COOLDOWN_CUT_PER_RANK = 0.08;

    private StyleRules() {
    }

    public static boolean auraReady(AuraLevel aura, Rank rank) {
        return aura.ordinal() >= requiredAura(rank).ordinal();
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
