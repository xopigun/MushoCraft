package fr.xopi.mushocraft.gametest;

import com.mojang.authlib.GameProfile;
import fr.xopi.mushocraft.life.LifeTracker;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.UUID;

/**
 * Players for gametests. Vanilla mock players cannot be used: they really join the server,
 * and Epic Fight then sends them packets their fake connection refuses.
 */
final class TestPlayers {
    private TestPlayers() {
    }

    /** A fresh NeoForge fake player that went through our login handling, so it is already born. */
    static ServerPlayer join(GameTestHelper helper) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "musho_test"));
        LifeTracker.onLogin(new PlayerEvent.PlayerLoggedInEvent(player));
        return player;
    }

    static void leave(ServerPlayer player) {
        NeoForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedOutEvent(player));
        player.discard();
    }

    static void expectClose(GameTestHelper helper, double actual, double expected, String what) {
        helper.assertTrue(Math.abs(actual - expected) < 1.0E-3, what + ": expected " + expected + ", got " + actual);
    }
}
