package fr.xopi.mushocraft.magic.spell;

import fr.xopi.mushocraft.combat.mastery.MasteryService;
import fr.xopi.mushocraft.combat.move.MoveKind;
import fr.xopi.mushocraft.combat.move.MoveRef;
import fr.xopi.mushocraft.combat.move.MoveService;
import fr.xopi.mushocraft.combat.move.MovesSync;
import fr.xopi.mushocraft.core.MushoAttachments;
import fr.xopi.mushocraft.core.MushoRegistries;
import fr.xopi.mushocraft.core.Rank;
import fr.xopi.mushocraft.life.LifeService;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Single entry point for what a character knows of magic.
 */
public final class MagicService {
    private MagicService() {
    }

    public static boolean knows(ServerPlayer player, Spell spell) {
        return player.getData(MushoAttachments.MAGIC_KNOWLEDGE).knows(spell.id());
    }

    public static boolean knows(ServerPlayer player, MoveRef move) {
        return move.kind() == MoveKind.SPELL && player.getData(MushoAttachments.MAGIC_KNOWLEDGE).knows(move.id());
    }

    /** Known spells in a stable order (school, then rank, then name), for display. Removed spells are skipped. */
    public static List<ResourceLocation> knownSpells(ServerPlayer player) {
        return player.getData(MushoAttachments.MAGIC_KNOWLEDGE).spells().stream()
                .map(MushoRegistries.SPELL::get)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Spell::school)
                        .thenComparing(Spell::rank)
                        .thenComparing(spell -> spell.id().toString()))
                .map(Spell::id)
                .toList();
    }

    public enum StudyResult {
        LEARNED,
        ALREADY_KNOWN,
        /** The school is not at the spell's rank yet. */
        NOT_READY
    }

    public static MagicProficiency proficiency(ServerPlayer player) {
        return player.getData(MushoAttachments.MAGIC_PROFICIENCY);
    }

    /**
     * Studies a spell from a book or a teacher: only possible once the school has reached the spell's rank.
     */
    public static StudyResult study(ServerPlayer player, Spell spell) {
        if (knows(player, spell)) {
            return StudyResult.ALREADY_KNOWN;
        }
        if (!proficiency(player).canStudy(spell)) {
            return StudyResult.NOT_READY;
        }
        learn(player, spell);
        return StudyResult.LEARNED;
    }

    /**
     * Adds experience to a school, announcing a new rank when one is reached.
     */
    public static void gainExperience(ServerPlayer player, SpellSchool school, double amount) {
        MagicProficiency before = proficiency(player);
        MagicProficiency after = before.with(school, before.experience(school) + amount);
        player.setData(MushoAttachments.MAGIC_PROFICIENCY, after);
        if (after.rank(school) != before.rank(school)) {
            player.sendSystemMessage(Component.translatable("message.mushocraft.magic.rank_up",
                    school.displayName(), after.rank(school).displayName()));
        }
    }

    public static void setExperience(ServerPlayer player, SpellSchool school, double xp) {
        player.setData(MushoAttachments.MAGIC_PROFICIENCY, proficiency(player).with(school, xp));
    }

    /**
     * The magician's title: the rank of the most advanced spell they know (wiki, page <i>Magic</i>).
     */
    public static Optional<Rank> title(ServerPlayer player) {
        return knownSpells(player).stream()
                .map(MushoRegistries.SPELL::get)
                .filter(Objects::nonNull)
                .map(Spell::rank)
                .max(Comparator.naturalOrder());
    }

    /**
     * Teaches a spell unconditionally (commands, tests, scripted rewards) and puts it in the move bar
     * if a slot is free.
     *
     * @return false if the spell was already known
     */
    public static boolean learn(ServerPlayer player, Spell spell) {
        if (knows(player, spell)) {
            return false;
        }
        player.setData(MushoAttachments.MAGIC_KNOWLEDGE, player.getData(MushoAttachments.MAGIC_KNOWLEDGE).with(spell.id()));
        player.sendSystemMessage(Component.translatable("message.mushocraft.magic.learned", spell.displayName()));
        MoveService.offer(player, spell.ref());
        MovesSync.push(player);
        return true;
    }

    public static ManaControl manaControl(ServerPlayer player) {
        return player.getData(MushoAttachments.MANA_CONTROL);
    }

    /** Sets the control of mana outright (commands, tests); reaching silent casting is not announced. */
    public static void setControl(ServerPlayer player, ManaControl control) {
        player.setData(MushoAttachments.MANA_CONTROL, control);
    }

    /** Circulating mana refines its control, the way to silent spell-casting; announced when it is reached. */
    public static void trainControl(ServerPlayer player, double seconds) {
        ManaControl before = manaControl(player);
        ManaControl after = before.plus(IncantationRules.controlGain(seconds, LifeService.ageInYears(player).orElse(0.0)));
        player.setData(MushoAttachments.MANA_CONTROL, after);
        if (!IncantationRules.canCastSilently(before) && IncantationRules.canCastSilently(after)) {
            player.sendSystemMessage(Component.translatable("message.mushocraft.magic.silent_unlocked"));
        }
    }

    /** The shortest incantation this player may use for a spell. */
    public static IncantationMode incantationMode(ServerPlayer player, Spell spell) {
        return IncantationRules.mode(MasteryService.repetitions(player, spell.ref()), manaControl(player));
    }

    public static void forgetAll(ServerPlayer player) {
        player.setData(MushoAttachments.MAGIC_KNOWLEDGE, MagicKnowledge.none());
        player.setData(MushoAttachments.MAGIC_PROFICIENCY, MagicProficiency.none());
        MovesSync.push(player);
    }
}
