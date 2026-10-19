package fr.xopi.mushocraft.talent;

import fr.xopi.mushocraft.core.MushoAttachments;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/**
 * Single entry point to read and change a character's talents.
 */
public final class TalentService {
    private TalentService() {
    }

    public static Optional<Talents> talents(ServerPlayer player) {
        return player.hasData(MushoAttachments.TALENTS)
                ? Optional.of(player.getData(MushoAttachments.TALENTS))
                : Optional.empty();
    }

    public static Talents roll(ServerPlayer player) {
        Talents talents = GaussianTalentRoller.fromConfig().roll(player.getRandom());
        player.setData(MushoAttachments.TALENTS, talents);
        return talents;
    }

    public static void set(ServerPlayer player, Talent talent, double value) {
        Talents current = talents(player).orElseGet(Talents::neutral);
        player.setData(MushoAttachments.TALENTS, current.with(talent, value));
    }
}
