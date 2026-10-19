package fr.xopi.mushocraft.command;

import fr.xopi.mushocraft.body.BodyService;
import fr.xopi.mushocraft.body.BodyStat;
import fr.xopi.mushocraft.body.aura.AuraLevel;
import fr.xopi.mushocraft.body.aura.BattleAuraService;
import fr.xopi.mushocraft.combat.mastery.MasteryService;
import fr.xopi.mushocraft.combat.move.MoveRef;
import fr.xopi.mushocraft.core.MushoAttachments;
import fr.xopi.mushocraft.core.MushoConfig;
import fr.xopi.mushocraft.core.MushoRegistries;
import fr.xopi.mushocraft.core.Rank;
import fr.xopi.mushocraft.life.LifeService;
import fr.xopi.mushocraft.life.LifeStage;
import fr.xopi.mushocraft.magic.mana.ManaService;
import fr.xopi.mushocraft.magic.mana.ManaSnapshot;
import fr.xopi.mushocraft.magic.spell.IncantationRules;
import fr.xopi.mushocraft.magic.spell.MagicProficiency;
import fr.xopi.mushocraft.magic.spell.MagicService;
import fr.xopi.mushocraft.magic.spell.ManaControl;
import fr.xopi.mushocraft.magic.spell.Spell;
import fr.xopi.mushocraft.magic.spell.SpellSchool;
import fr.xopi.mushocraft.sword.StyleSync;
import fr.xopi.mushocraft.sword.SwordMastery.StyleProgress;
import fr.xopi.mushocraft.sword.SwordService;
import fr.xopi.mushocraft.sword.SwordStyle;
import fr.xopi.mushocraft.sword.technique.Technique;
import fr.xopi.mushocraft.talent.Talent;
import fr.xopi.mushocraft.talent.TalentService;
import net.minecraft.server.level.ServerPlayer;

/**
 * Makes a player as strong as the mod allows in every discipline at once (operators, tests): an adult with the best
 * talents, a fully trained body and a complete Battle Aura, the largest mana reservoir and silent casting, every
 * spell and technique known and mastered, every school of magic and every sword style at the God rank.
 * Each discipline is set through its own service, so the usual announcements and syncs happen.
 */
public final class PlayerMaximizer {
    /** Body training past which a stat no longer grows in any visible way (99.99% of its ceiling). */
    private static final double BODY_EXPERIENCE = 20_000.0;

    private PlayerMaximizer() {
    }

    public static void maxOut(ServerPlayer player) {
        growUp(player);
        maxTalents(player);
        maxBody(player);
        maxMana(player);
        maxMagic(player);
        maxSwordsmanship(player);
    }

    /** A child's body and reservoir are held back by its age: the rest only counts fully in an adult. */
    private static void growUp(ServerPlayer player) {
        double age = LifeService.ageInYears(player).orElse(0.0);
        if (age < LifeStage.ADULT.startAge()) {
            LifeService.setAge(player, LifeStage.ADULT.startAge());
        }
    }

    private static void maxTalents(ServerPlayer player) {
        double best = MushoConfig.TALENT_MAX.get();
        for (Talent talent : Talent.values()) {
            TalentService.set(player, talent, best);
        }
    }

    private static void maxBody(ServerPlayer player) {
        for (BodyStat stat : BodyStat.values()) {
            BodyService.setExperience(player, stat, BODY_EXPERIENCE);
        }
        BattleAuraService.setMastery(player, AuraLevel.COMPLETE.mastery());
        BodyService.apply(player);
    }

    /** The trained part of the reservoir is raised until the whole reaches the talent's ceiling, then filled. */
    private static void maxMana(ServerPlayer player) {
        ManaSnapshot snapshot = ManaService.snapshot(player);
        double trained = player.getData(MushoAttachments.MANA).trained();
        ManaService.setTrained(player, trained + Math.max(0.0, snapshot.ceiling() - snapshot.capacity()));
        ManaService.refill(player);
    }

    private static void maxMagic(ServerPlayer player) {
        for (SpellSchool school : SpellSchool.values()) {
            MagicService.setExperience(player, school, MagicProficiency.threshold(Rank.GOD));
        }
        MagicService.setControl(player, new ManaControl(IncantationRules.SILENT_CASTING_CONTROL));
        for (Spell spell : MushoRegistries.SPELL) {
            MagicService.learn(player, spell);
        }
        for (Spell spell : MushoRegistries.SPELL) {
            master(player, spell.ref());
        }
    }

    private static void maxSwordsmanship(ServerPlayer player) {
        for (SwordStyle style : SwordStyle.values()) {
            StyleProgress progress = SwordService.mastery(player).progress(style).orElseGet(StyleProgress::beginner);
            SwordService.setProgress(player, style, progress.withRank(Rank.GOD));
        }
        for (Technique technique : MushoRegistries.TECHNIQUE) {
            if (!SwordService.knows(player, technique)) {
                SwordService.learn(player, technique);
            }
            master(player, technique.ref());
        }
        StyleSync.push(player);
    }

    /** Mastered by its own curve (inherent magic takes longer), and repeated enough to be cast in short. */
    private static void master(ServerPlayer player, MoveRef move) {
        double needed = Math.max(MasteryService.curve(move).repetitionsToMaster(), IncantationRules.ABBREVIATED_AT);
        if (MasteryService.repetitions(player, move) < needed) {
            MasteryService.setRepetitions(player, move, needed);
        }
    }
}
