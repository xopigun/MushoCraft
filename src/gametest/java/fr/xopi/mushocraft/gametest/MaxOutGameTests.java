package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.body.BodyService;
import fr.xopi.mushocraft.body.aura.AuraLevel;
import fr.xopi.mushocraft.body.aura.BattleAuraService;
import fr.xopi.mushocraft.combat.mastery.MasteryService;
import fr.xopi.mushocraft.command.PlayerMaximizer;
import fr.xopi.mushocraft.core.MushoRegistries;
import fr.xopi.mushocraft.core.Rank;
import fr.xopi.mushocraft.life.LifeService;
import fr.xopi.mushocraft.life.LifeStage;
import fr.xopi.mushocraft.magic.mana.ManaSnapshot;
import fr.xopi.mushocraft.magic.mana.ManaService;
import fr.xopi.mushocraft.magic.spell.IncantationMode;
import fr.xopi.mushocraft.magic.spell.MagicService;
import fr.xopi.mushocraft.magic.spell.Spell;
import fr.xopi.mushocraft.magic.spell.SpellSchool;
import fr.xopi.mushocraft.sword.SwordService;
import fr.xopi.mushocraft.sword.SwordStyle;
import fr.xopi.mushocraft.sword.technique.Technique;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;

/**
 * {@code /mushocraft max}: a newborn comes out as strong as the mod allows in every discipline.
 */
@GameTestHolder(MushoCraft.MODID)
@PrefixGameTestTemplate(false)
public final class MaxOutGameTests {
    private static final String EMPTY = "empty";

    private MaxOutGameTests() {
    }

    @GameTest(template = EMPTY)
    public static void maxingOutMastersEveryDiscipline(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.join(helper);
        PlayerMaximizer.maxOut(player);

        helper.assertTrue(LifeService.ageInYears(player).orElse(0.0) >= LifeStage.ADULT.startAge(), "grown up");
        helper.assertTrue(BodyService.bodyLevel(player) > 1.0, "body trained past an ordinary talent's ceiling");
        helper.assertValueEqual(BattleAuraService.level(player), AuraLevel.COMPLETE, "Battle Aura");
        ManaSnapshot mana = ManaService.snapshot(player);
        TestPlayers.expectClose(helper, mana.capacity(), mana.ceiling(), "mana capacity");
        TestPlayers.expectClose(helper, mana.current(), mana.capacity(), "mana filled");

        for (SpellSchool school : SpellSchool.values()) {
            helper.assertValueEqual(MagicService.proficiency(player).rank(school), Rank.GOD, school + " rank");
        }
        for (Spell spell : MushoRegistries.SPELL) {
            helper.assertTrue(MagicService.knows(player, spell), spell.id() + " known");
            helper.assertTrue(MasteryService.isMastered(player, spell.ref()), spell.id() + " mastered");
            helper.assertValueEqual(MagicService.incantationMode(player, spell), IncantationMode.SILENT, spell.id() + " silent");
        }
        for (SwordStyle style : SwordStyle.values()) {
            helper.assertValueEqual(SwordService.rank(player, style), Optional.of(Rank.GOD), style + " rank");
        }
        for (Technique technique : MushoRegistries.TECHNIQUE) {
            helper.assertTrue(SwordService.knows(player, technique), technique.id() + " known");
            helper.assertTrue(MasteryService.isMastered(player, technique.ref()), technique.id() + " mastered");
        }
        TestPlayers.leave(player);
        helper.succeed();
    }
}
