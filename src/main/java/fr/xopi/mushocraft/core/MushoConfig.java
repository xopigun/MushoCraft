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

        SPEC = builder.build();
    }

    private MushoConfig() {
    }
}
