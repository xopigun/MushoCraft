package fr.xopi.mushocraft.magic.mana;

import fr.xopi.mushocraft.core.MushoAttachments;
import fr.xopi.mushocraft.life.LifeService;
import fr.xopi.mushocraft.talent.Talent;
import fr.xopi.mushocraft.talent.TalentService;
import fr.xopi.mushocraft.talent.Talents;
import net.minecraft.server.level.ServerPlayer;

/**
 * Single entry point to read and change a character's mana. Every change is pushed to the player's client.
 */
public final class ManaService {
    private ManaService() {
    }

    public static ManaSnapshot snapshot(ServerPlayer player) {
        ManaRules rules = ManaRules.fromConfig();
        ManaData data = player.getData(MushoAttachments.MANA);
        double capacity = capacity(player, rules, data);
        return new ManaSnapshot(Math.min(data.current(), capacity), capacity, ceiling(player, rules));
    }

    /**
     * Pays {@code amount} mana if the player has it. Spending is also what makes the reservoir grow.
     *
     * @return false, with nothing spent, when the player lacks mana
     */
    public static boolean trySpend(ServerPlayer player, double amount) {
        ManaRules rules = ManaRules.fromConfig();
        ManaData data = player.getData(MushoAttachments.MANA);
        double capacity = capacity(player, rules, data);
        double current = Math.min(data.current(), capacity);
        if (amount < 0.0 || current < amount) {
            return false;
        }
        double remaining = current - amount;
        double growth = rules.growth(amount, age(player), capacity, ceiling(player, rules), remaining);
        player.setData(MushoAttachments.MANA, new ManaData(data.trained() + growth, remaining));
        ManaSync.push(player);
        return true;
    }

    public static void restore(ServerPlayer player, double amount) {
        ManaSnapshot snapshot = snapshot(player);
        setCurrent(player, Math.min(snapshot.capacity(), snapshot.current() + amount));
    }

    public static void refill(ServerPlayer player) {
        setCurrent(player, snapshot(player).capacity());
    }

    public static void setCurrent(ServerPlayer player, double value) {
        player.setData(MushoAttachments.MANA, player.getData(MushoAttachments.MANA).withCurrent(value));
        ManaSync.push(player);
    }

    public static void setTrained(ServerPlayer player, double value) {
        player.setData(MushoAttachments.MANA, player.getData(MushoAttachments.MANA).withTrained(value));
        ManaSync.push(player);
    }

    /** A new life starts with an untrained, full reservoir. */
    public static void resetForNewLife(ServerPlayer player) {
        player.setData(MushoAttachments.MANA, ManaData.empty());
        refill(player);
    }

    public static void regenerate(ServerPlayer player, double seconds) {
        ManaSnapshot snapshot = snapshot(player);
        if (snapshot.current() < snapshot.capacity()) {
            restore(player, ManaRules.fromConfig().regeneration(snapshot.capacity(), seconds, player.isSleeping()));
        }
    }

    private static double capacity(ServerPlayer player, ManaRules rules, ManaData data) {
        return rules.naturalCapacity(age(player), potential(player)) + data.trained();
    }

    private static double ceiling(ServerPlayer player, ManaRules rules) {
        return rules.ceiling(potential(player));
    }

    private static double potential(ServerPlayer player) {
        return TalentService.talents(player).map(talents -> talents.get(Talent.MANA_POTENTIAL)).orElse(Talents.NEUTRAL);
    }

    private static double age(ServerPlayer player) {
        return LifeService.ageInYears(player).orElse(0.0);
    }
}
