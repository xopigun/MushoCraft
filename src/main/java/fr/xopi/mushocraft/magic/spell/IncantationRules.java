package fr.xopi.mushocraft.magic.spell;

import fr.xopi.mushocraft.combat.mastery.MasteryRules;

/**
 * Which incantation a caster may use (wiki, page <i>Magic</i>). Numbers are gameplay choices (docs/ECARTS.md):
 * <ul>
 *     <li>A spell repeated far beyond mastery ({@link #ABBREVIATED_AT} times) can be cast with a truncated
 *     incantation, half as long.</li>
 *     <li>Silent spell-casting relies on moulding mana by imagination: it is built by circulating mana, much faster
 *     as a small child (Rudeus learned it as a toddler; Roxy's paper says it is easier to learn young). Once the
 *     control is there, every mastered spell can be cast without any incantation.</li>
 * </ul>
 */
public final class IncantationRules {
    public static final double ABBREVIATED_AT = 3.0 * MasteryRules.REPETITIONS_TO_MASTER;
    /** Seconds of mana circulation (as an older child) needed to cast silently. */
    public static final double SILENT_CASTING_CONTROL = 900.0;
    private static final double EARLY_CHILDHOOD_END = 6.0;
    private static final double CHILDHOOD_END = 15.0;

    private IncantationRules() {
    }

    /** Control gained by circulating mana for this long, at this age: x4 before 6, x1 until 15, x0.25 after. */
    public static double controlGain(double seconds, double ageInYears) {
        if (ageInYears < EARLY_CHILDHOOD_END) {
            return seconds * 4.0;
        }
        return ageInYears < CHILDHOOD_END ? seconds : seconds * 0.25;
    }

    public static boolean canCastSilently(ManaControl control) {
        return control.practice() >= SILENT_CASTING_CONTROL;
    }

    /** The shortest incantation allowed for a spell repeated this many times, by a caster with this control. */
    public static IncantationMode mode(double repetitions, ManaControl control) {
        if (MasteryRules.isMastered(repetitions) && canCastSilently(control)) {
            return IncantationMode.SILENT;
        }
        return repetitions >= ABBREVIATED_AT ? IncantationMode.ABBREVIATED : IncantationMode.FULL;
    }
}
