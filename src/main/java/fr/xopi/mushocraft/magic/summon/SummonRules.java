package fr.xopi.mushocraft.magic.summon;

/**
 * The numbers of summoning. The wiki gives the principles (summons measure their master by its mana, spirits live on
 * it, a creature with more mana than its summoner may break loose); the figures are chosen for the game
 * (docs/ECARTS.md, E24).
 */
public final class SummonRules {
    public static final double LAMPLIGHT_COST = 5.0;
    public static final int LAMPLIGHT_SPIRITS = 3;
    public static final int LAMPLIGHT_TICKS = 6000;

    public static final double SPIRIT_COST = 40.0;
    /** Mana a fighting spirit draws from its master each second; it vanishes when there is none left. */
    public static final double SPIRIT_UPKEEP_PER_SECOND = 1.0;
    public static final int SPIRIT_MAX_TICKS = 3600;

    public static final double BEAST_COST = 30.0;
    public static final int BEAST_BOND_TICKS = 6000;
    /** A creature's « mana », to compare with its summoner's: its health times this. */
    public static final double MANA_PER_HEALTH = 5.0;

    private SummonRules() {
    }

    /** A fighting spirit's health: built from its master's reservoir. */
    public static double spiritHealth(double summonerCapacity) {
        return Math.min(60.0, 10.0 + summonerCapacity / 30.0);
    }

    public static double spiritDamage(double summonerCapacity) {
        return Math.min(15.0, 3.0 + summonerCapacity / 150.0);
    }

    /**
     * The chance that a summoned creature breaks loose and turns on its summoner: none while its mana is below the
     * summoner's, then rising with the gap, at most 90 %.
     */
    public static double lossOfControlChance(double creatureMaxHealth, double summonerCapacity) {
        double creatureMana = creatureMaxHealth * MANA_PER_HEALTH;
        if (summonerCapacity <= 0.0) {
            return 0.9;
        }
        return Math.max(0.0, Math.min(0.9, creatureMana / summonerCapacity - 1.0));
    }
}
