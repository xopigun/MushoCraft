package fr.xopi.mushocraft.sword;

import fr.xopi.mushocraft.body.aura.BattleAuraService;
import fr.xopi.mushocraft.combat.move.MoveService;
import fr.xopi.mushocraft.combat.move.MovesSync;
import fr.xopi.mushocraft.core.MushoAttachments;
import fr.xopi.mushocraft.core.MushoRegistries;
import fr.xopi.mushocraft.core.Rank;
import fr.xopi.mushocraft.sword.SwordMastery.StyleProgress;
import fr.xopi.mushocraft.sword.technique.Technique;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Single entry point for swordsmanship: adopting a style, progressing in it, being promoted, learning techniques.
 */
public final class SwordService {
    public enum TeachResult {
        LEARNED,
        ALREADY_KNOWN,
        /** The pupil does not practise the technique's style, or not at the technique's rank yet. */
        NOT_READY
    }

    private SwordService() {
    }

    public static SwordMastery mastery(ServerPlayer player) {
        return player.getData(MushoAttachments.SWORD_MASTERY);
    }

    public static Optional<Rank> rank(ServerPlayer player, SwordStyle style) {
        return mastery(player).progress(style).map(StyleProgress::rank);
    }

    public static Optional<SwordStyle> activeStyle(ServerPlayer player) {
        return mastery(player).active();
    }

    /** Starts practising a style (as a Beginner if new to it) and fights in it from now on. */
    public static void adopt(ServerPlayer player, SwordStyle style) {
        SwordMastery mastery = mastery(player);
        if (!mastery.practises(style)) {
            mastery = mastery.with(style, StyleProgress.beginner());
        }
        player.setData(MushoAttachments.SWORD_MASTERY, mastery.withActive(style));
        player.displayClientMessage(Component.translatable("message.mushocraft.style.adopted", style.displayName()), true);
    }

    /** Practice in a style: experience, and promotion up to Intermediate without a master. */
    public static void gainExperience(ServerPlayer player, SwordStyle style, double amount) {
        Optional<StyleProgress> progress = mastery(player).progress(style);
        if (progress.isEmpty() || amount <= 0.0) {
            return;
        }
        setProgress(player, style, progress.get().withExperience(progress.get().experience() + amount));
        promote(player, style, StyleRules.HIGHEST_SELF_TAUGHT);
    }

    public static void setProgress(ServerPlayer player, SwordStyle style, StyleProgress progress) {
        player.setData(MushoAttachments.SWORD_MASTERY, mastery(player).with(style, progress));
    }

    /**
     * Raises the rank in a style as far as experience, Battle Aura and the teacher allow.
     *
     * @param highestGrantable the highest rank this promotion can give: the master's own rank, or Intermediate alone
     * @return the rank after promotion, if the style is practised
     */
    public static Optional<Rank> promote(ServerPlayer player, SwordStyle style, Rank highestGrantable) {
        Optional<StyleProgress> progress = mastery(player).progress(style);
        if (progress.isEmpty()) {
            return Optional.empty();
        }
        StyleProgress current = progress.get();
        while (current.rank() != Rank.GOD) {
            Rank next = Rank.values()[current.rank().ordinal() + 1];
            boolean grantable = highestGrantable.isAtLeast(next);
            boolean experienced = current.experience() >= StyleRules.threshold(next);
            boolean auraReady = BattleAuraService.level(player).ordinal() >= StyleRules.requiredAura(next).ordinal();
            if (!grantable || !experienced || !auraReady) {
                break;
            }
            current = current.withRank(next);
            player.sendSystemMessage(Component.translatable("message.mushocraft.style.promoted",
                    style.displayName(), next.displayName()));
        }
        setProgress(player, style, current);
        return Optional.of(current.rank());
    }

    public static boolean knows(ServerPlayer player, Technique technique) {
        return mastery(player).techniques().contains(technique.id());
    }

    /** Teaching by a master (or a scripted reward): needs the style practised at the technique's rank. */
    public static TeachResult teach(ServerPlayer player, Technique technique) {
        if (knows(player, technique)) {
            return TeachResult.ALREADY_KNOWN;
        }
        boolean ready = rank(player, technique.style()).map(rank -> rank.isAtLeast(technique.rank())).orElse(false);
        if (!ready) {
            return TeachResult.NOT_READY;
        }
        learn(player, technique);
        return TeachResult.LEARNED;
    }

    /**
     * Learns a technique unconditionally (commands, tests) and puts it in the move bar if a slot is free.
     * The technique's style becomes practised (as a Beginner) if it was not already.
     */
    public static void learn(ServerPlayer player, Technique technique) {
        SwordMastery mastery = mastery(player);
        if (!mastery.practises(technique.style())) {
            mastery = mastery.with(technique.style(), StyleProgress.beginner());
        }
        player.setData(MushoAttachments.SWORD_MASTERY, mastery.withTechnique(technique.id()));
        player.sendSystemMessage(Component.translatable("message.mushocraft.style.technique_learned", technique.displayName()));
        MoveService.offer(player, technique.ref());
        MovesSync.push(player);
    }

    /** Known techniques in a stable order (style, rank, name). Removed techniques are skipped. */
    public static List<ResourceLocation> knownTechniques(ServerPlayer player) {
        return mastery(player).techniques().stream()
                .map(MushoRegistries.TECHNIQUE::get)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Technique::style).thenComparing(Technique::rank)
                        .thenComparing(technique -> technique.id().toString()))
                .map(Technique::id)
                .toList();
    }

    public static void forgetAll(ServerPlayer player) {
        player.setData(MushoAttachments.SWORD_MASTERY, SwordMastery.none());
        MovesSync.push(player);
    }
}
