package fr.xopi.mushocraft.magic.spell;

import fr.xopi.mushocraft.core.Rank;

/**
 * Length of a full incantation. The wiki notes that incantations grow longer with the spell's rank:
 * a few sentences for a Beginner water spell, a long recitation for a God-rank one.
 */
public final class Incantation {
    private static final int[] TICKS_BY_RANK = {20, 30, 45, 70, 100, 140, 200};

    private Incantation() {
    }

    public static int defaultTicks(Rank rank) {
        return TICKS_BY_RANK[rank.ordinal()];
    }
}
