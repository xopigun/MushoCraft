package fr.xopi.mushocraft.life;

import fr.xopi.mushocraft.core.MushoAttachments;
import fr.xopi.mushocraft.life.event.LifeStageChangedEvent;
import fr.xopi.mushocraft.life.event.PlayerBornEvent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Optional;

/**
 * Single entry point to read and change a character's life: birth, age and life stage.
 * Other systems react to {@link PlayerBornEvent} and {@link LifeStageChangedEvent} instead of being called from here.
 */
public final class LifeService {
    private static final BodyGrowth BODY_GROWTH = new BodyGrowth(GrowthCurve.HUMAN);

    private LifeService() {
    }

    public static boolean isBorn(ServerPlayer player) {
        return player.hasData(MushoAttachments.LIFE);
    }

    public static Optional<LifeData> lifeData(ServerPlayer player) {
        return isBorn(player) ? Optional.of(player.getData(MushoAttachments.LIFE)) : Optional.empty();
    }

    public static void giveBirth(ServerPlayer player) {
        player.setData(MushoAttachments.LIFE, LifeData.bornAt(now(player)));
        NeoForge.EVENT_BUS.post(new PlayerBornEvent(player));
        refresh(player);
    }

    /** Starts a brand new life: age 0 and everything rolled at birth is rolled again. */
    public static void rebirth(ServerPlayer player) {
        player.removeData(MushoAttachments.LIFE);
        giveBirth(player);
    }

    public static Optional<Double> ageInYears(ServerPlayer player) {
        return lifeData(player).map(data -> LifeClock.fromConfig().toYears(now(player) - data.birthTick()));
    }

    public static void setAge(ServerPlayer player, double years) {
        long birthTick = now(player) - LifeClock.fromConfig().toTicks(years);
        player.setData(MushoAttachments.LIFE, lifeData(player)
                .map(data -> data.withBirthTick(birthTick))
                .orElseGet(() -> LifeData.bornAt(birthTick)));
        refresh(player);
    }

    /**
     * Brings the stored stage and the body size up to date with the current age.
     * Safe to call often: nothing happens when nothing changed.
     */
    public static void refresh(ServerPlayer player) {
        Optional<LifeData> stored = lifeData(player);
        Optional<Double> age = ageInYears(player);
        if (stored.isEmpty() || age.isEmpty()) {
            return;
        }
        LifeStage previous = stored.get().stage();
        LifeStage current = LifeStage.at(age.get());
        if (current != previous) {
            player.setData(MushoAttachments.LIFE, stored.get().withStage(current));
            NeoForge.EVENT_BUS.post(new LifeStageChangedEvent(player, previous, current));
        }
        BODY_GROWTH.apply(player, age.get());
    }

    /** Overworld game time is shared by every dimension and never goes backwards. */
    private static long now(ServerPlayer player) {
        return player.server.overworld().getGameTime();
    }
}
