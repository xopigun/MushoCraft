package fr.xopi.mushocraft.magic.spell;

import com.mojang.serialization.Codec;
import fr.xopi.mushocraft.core.Rank;

import java.util.EnumMap;
import java.util.Map;

/**
 * Experience gathered in each school of magic by casting its spells. The school's rank follows from it:
 * a magician can only study spells of a rank they have reached in that school, and casts spells below
 * their rank a little more strongly.
 */
public record MagicProficiency(Map<SpellSchool, Double> experience) {
    public static final Codec<MagicProficiency> CODEC = Codec.unboundedMap(SpellSchool.CODEC, Codec.DOUBLE)
            .xmap(MagicProficiency::new, MagicProficiency::experience);

    /** Experience needed to reach each rank, indexed by {@link Rank#ordinal()}. */
    private static final double[] THRESHOLDS = {0.0, 150.0, 800.0, 4_000.0, 15_000.0, 50_000.0, 150_000.0};
    /** Extra strength per rank of the school above the spell's own rank. */
    private static final double POWER_PER_RANK = 0.1;

    public MagicProficiency {
        experience = Map.copyOf(experience);
    }

    public static MagicProficiency none() {
        return new MagicProficiency(Map.of());
    }

    public double experience(SpellSchool school) {
        return experience.getOrDefault(school, 0.0);
    }

    public Rank rank(SpellSchool school) {
        double xp = experience(school);
        Rank reached = Rank.BEGINNER;
        for (Rank rank : Rank.values()) {
            if (xp >= THRESHOLDS[rank.ordinal()]) {
                reached = rank;
            }
        }
        return reached;
    }

    public static double threshold(Rank rank) {
        return THRESHOLDS[rank.ordinal()];
    }

    public boolean canStudy(Spell spell) {
        return rank(spell.school()).ordinal() >= spell.rank().ordinal();
    }

    public double power(Spell spell) {
        return 1.0 + POWER_PER_RANK * Math.max(0, rank(spell.school()).ordinal() - spell.rank().ordinal());
    }

    public MagicProficiency with(SpellSchool school, double xp) {
        Map<SpellSchool, Double> copy = new EnumMap<>(SpellSchool.class);
        copy.putAll(experience);
        copy.put(school, Math.max(0.0, xp));
        return new MagicProficiency(copy);
    }
}
