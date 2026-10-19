package fr.xopi.mushocraft.life;

import fr.xopi.mushocraft.core.MushoConfig;

/**
 * Converts game ticks to years of life. Game time keeps running while players sleep through the night,
 * so skipping the night does not make anyone older.
 */
public final class LifeClock {
    private static final long TICKS_PER_DAY = 24_000L;

    private final double ticksPerYear;

    public LifeClock(double daysPerYear) {
        this.ticksPerYear = daysPerYear * TICKS_PER_DAY;
    }

    public static LifeClock fromConfig() {
        return new LifeClock(MushoConfig.DAYS_PER_YEAR.get());
    }

    public double toYears(long ticks) {
        return ticks / ticksPerYear;
    }

    public long toTicks(double years) {
        return Math.round(years * ticksPerYear);
    }
}
