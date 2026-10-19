package fr.xopi.mushocraft.client;

/**
 * Last Battle Aura state received from the server. Plain data only (written by a packet handler).
 */
public final class ClientAuraState {
    private static volatile int level;
    private static volatile boolean up;

    private ClientAuraState() {
    }

    public static void set(int newLevel, boolean newUp) {
        level = newLevel;
        up = newUp;
    }

    public static int level() {
        return level;
    }

    public static boolean isUp() {
        return up;
    }
}
