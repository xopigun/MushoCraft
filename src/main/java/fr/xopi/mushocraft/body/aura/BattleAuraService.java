package fr.xopi.mushocraft.body.aura;

import fr.xopi.mushocraft.body.BodyService;
import fr.xopi.mushocraft.compat.epicfight.EpicFightModes;
import fr.xopi.mushocraft.core.MushoAttachments;
import fr.xopi.mushocraft.magic.mana.ManaService;
import fr.xopi.mushocraft.magic.mana.ManaSnapshot;
import fr.xopi.mushocraft.network.AuraSyncPayload;
import fr.xopi.mushocraft.talent.Talent;
import fr.xopi.mushocraft.talent.TalentService;
import fr.xopi.mushocraft.talent.Talents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Battle Aura: mana wrapped around and compressed into the body to strengthen it (wiki, page <i>Battle Aura</i>,
 * WN ch. 80). It is learned through physical training, not spells, and burns mana from the shared reservoir
 * (decision of 19/10/2026). Everyone can learn it, more or less easily (talent): docs/ECARTS.md, E2.
 */
public final class BattleAuraService {
    /** Share of every physical training gain that also trains the aura, for an ordinary talent. */
    private static final double MASTERY_PER_TRAINING = 0.5;

    private BattleAuraService() {
    }

    public static BattleAuraData data(ServerPlayer player) {
        return player.getData(MushoAttachments.BATTLE_AURA);
    }

    public static AuraLevel level(ServerPlayer player) {
        return data(player).level();
    }

    /** The aura is up when raised on purpose, or by itself in battle for those who do not control it yet. */
    public static boolean isUp(ServerPlayer player) {
        AuraLevel level = level(player);
        if (level == AuraLevel.NONE) {
            return false;
        }
        return level.manual() ? data(player).raised() : EpicFightModes.isBattleMode(player);
    }

    /** Extra share of the maximal stat bonus currently given by the aura. */
    public static double bonus(ServerPlayer player) {
        return isUp(player) ? level(player).bonus() : 0.0;
    }

    public static void onTraining(ServerPlayer player, double trainingGained) {
        double ease = TalentService.talents(player).map(talents -> talents.get(Talent.BATTLE_AURA_EASE)).orElse(Talents.NEUTRAL);
        setMastery(player, data(player).mastery() + trainingGained * MASTERY_PER_TRAINING * ease);
    }

    public static void setMastery(ServerPlayer player, double mastery) {
        AuraLevel before = level(player);
        player.setData(MushoAttachments.BATTLE_AURA, data(player).withMastery(mastery));
        AuraLevel after = level(player);
        if (after != before) {
            player.sendSystemMessage(Component.translatable("message.mushocraft.aura.level_up", after.displayName()));
            refresh(player);
        }
    }

    /** Raises or lowers the aura at will; only possible once it is consciously controlled. */
    public static void toggle(ServerPlayer player) {
        if (!level(player).manual()) {
            player.displayClientMessage(Component.translatable("message.mushocraft.aura.cannot_control"), true);
            return;
        }
        boolean raised = !data(player).raised();
        if (raised && ManaService.snapshot(player).current() <= 0.0) {
            player.displayClientMessage(Component.translatable("message.mushocraft.aura.no_mana"), true);
            return;
        }
        player.setData(MushoAttachments.BATTLE_AURA, data(player).withRaised(raised));
        player.displayClientMessage(Component.translatable(raised ? "message.mushocraft.aura.raised"
                : "message.mushocraft.aura.lowered"), true);
        refresh(player);
    }

    /** Burns this second's mana while the aura is up; drops it when the reservoir runs dry. */
    public static void tick(ServerPlayer player, double seconds) {
        if (!isUp(player)) {
            return;
        }
        ManaSnapshot mana = ManaService.snapshot(player);
        double cost = Math.min(mana.capacity() * level(player).drainPerSecond() * seconds, mana.current());
        ManaService.trySpend(player, cost);
        if (ManaService.snapshot(player).current() <= 0.0) {
            player.setData(MushoAttachments.BATTLE_AURA, data(player).withRaised(false));
            player.displayClientMessage(Component.translatable("message.mushocraft.aura.no_mana"), true);
            refresh(player);
            return;
        }
        player.serverLevel().sendParticles(ParticleTypes.WHITE_ASH, player.getX(), player.getY() + player.getBbHeight() * 0.5,
                player.getZ(), 6, 0.35, 0.5, 0.35, 0.01);
    }

    /** Re-applies the body bonuses and tells the client, after any change of the aura's state. */
    public static void refresh(ServerPlayer player) {
        BodyService.apply(player);
        if (player.connection != null) {
            PacketDistributor.sendToPlayer(player, new AuraSyncPayload(level(player).ordinal(), isUp(player)));
        }
    }
}
