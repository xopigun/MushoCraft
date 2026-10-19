package fr.xopi.mushocraft.core;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server (per-world) configuration. Gameplay tuning belongs to the world, so it is synced to clients
 * and can differ between servers.
 */
public final class MushoConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.DoubleValue DAYS_PER_YEAR;

    public static final ModConfigSpec.DoubleValue TALENT_MEAN;
    public static final ModConfigSpec.DoubleValue TALENT_STD_DEV;
    public static final ModConfigSpec.DoubleValue TALENT_MIN;
    public static final ModConfigSpec.DoubleValue TALENT_MAX;

    public static final ModConfigSpec.DoubleValue MANA_NEWBORN_CAPACITY;
    public static final ModConfigSpec.DoubleValue MANA_ADULT_CAPACITY;
    public static final ModConfigSpec.DoubleValue MANA_CEILING;
    public static final ModConfigSpec.DoubleValue MANA_GROWTH_RATE;
    public static final ModConfigSpec.DoubleValue MANA_ADULT_GROWTH_FACTOR;
    public static final ModConfigSpec.DoubleValue MANA_EXHAUSTION_BONUS;
    public static final ModConfigSpec.DoubleValue MANA_REGEN_PER_SECOND;
    public static final ModConfigSpec.DoubleValue MANA_SLEEP_REGEN_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue MANA_TRAINING_DRAIN_PER_SECOND;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("life");
        DAYS_PER_YEAR = builder
                .comment("Minecraft days (20 min each) that make one year of a character's life.",
                        "Changing it also changes the current age of every character.")
                .defineInRange("daysPerYear", 2.0, 0.05, 365.0);
        builder.pop();

        builder.push("talents");
        TALENT_MEAN = builder
                .comment("Average natural talent rolled at birth. 1.0 is an ordinary human.")
                .defineInRange("mean", 1.0, 0.0, 10.0);
        TALENT_STD_DEV = builder
                .comment("Spread of natural talents around the mean (standard deviation).")
                .defineInRange("standardDeviation", 0.15, 0.0, 5.0);
        TALENT_MIN = builder
                .comment("Lowest talent a character can be born with.")
                .defineInRange("min", 0.5, 0.0, 10.0);
        TALENT_MAX = builder
                .comment("Highest talent a character can be born with.")
                .defineInRange("max", 1.5, 0.0, 10.0);
        builder.pop();

        builder.push("mana");
        MANA_NEWBORN_CAPACITY = builder
                .comment("Natural mana capacity of a newborn with an ordinary mana potential (1.0).")
                .defineInRange("newbornCapacity", 10.0, 0.0, 1.0E6);
        MANA_ADULT_CAPACITY = builder
                .comment("Natural mana capacity of an untrained adult with an ordinary mana potential.",
                        "Natural capacity grows linearly from the newborn value to this one until adulthood.")
                .defineInRange("adultCapacity", 100.0, 0.0, 1.0E6);
        MANA_CEILING = builder
                .comment("Highest capacity a character with an ordinary potential can ever reach through training.",
                        "The real ceiling is this value times the square of the mana potential talent.")
                .defineInRange("ceiling", 2000.0, 1.0, 1.0E7);
        MANA_GROWTH_RATE = builder
                .comment("Capacity gained per point of mana spent, for a young child far from their ceiling.")
                .defineInRange("growthRate", 0.05, 0.0, 10.0);
        MANA_ADULT_GROWTH_FACTOR = builder
                .comment("Multiplier on growth once adult (growth slows from age 5 to this value at 15).")
                .defineInRange("adultGrowthFactor", 0.05, 0.0, 1.0);
        MANA_EXHAUSTION_BONUS = builder
                .comment("Growth multiplier when spending leaves the reservoir almost empty (training to exhaustion).")
                .defineInRange("exhaustionBonus", 2.0, 1.0, 10.0);
        MANA_REGEN_PER_SECOND = builder
                .comment("Fraction of the capacity regenerated each second.")
                .defineInRange("regenPerSecond", 0.01, 0.0, 1.0);
        MANA_SLEEP_REGEN_MULTIPLIER = builder
                .comment("Regeneration multiplier while sleeping.")
                .defineInRange("sleepRegenMultiplier", 3.0, 1.0, 100.0);
        MANA_TRAINING_DRAIN_PER_SECOND = builder
                .comment("Fraction of the capacity spent each second while circulating mana to train it.")
                .defineInRange("trainingDrainPerSecond", 0.05, 0.0, 1.0);
        builder.pop();

        SPEC = builder.build();
    }

    private MushoConfig() {
    }
}
