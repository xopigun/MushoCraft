package fr.xopi.mushocraft.client;

import fr.xopi.mushocraft.magic.mana.ManaSnapshot;

/**
 * Last mana gauge received from the server. Holds plain data only, so packet handlers
 * can write it without pulling client classes onto a dedicated server.
 */
public final class ClientManaState {
    private static volatile ManaSnapshot snapshot = ManaSnapshot.NONE;

    private ClientManaState() {
    }

    public static ManaSnapshot get() {
        return snapshot;
    }

    public static void set(ManaSnapshot value) {
        snapshot = value;
    }
}
