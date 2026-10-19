package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.combat.mastery.MasteryRules;
import fr.xopi.mushocraft.combat.mastery.MasteryService;
import fr.xopi.mushocraft.combat.move.MoveRef;
import fr.xopi.mushocraft.magic.spell.MushoSpells;
import fr.xopi.mushocraft.sword.technique.MushoTechniques;
import fr.xopi.mushocraft.sword.technique.TechniqueTempo;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(MushoCraft.MODID)
@PrefixGameTestTemplate(false)
public final class MasteryGameTests {
    private static final String EMPTY = "empty";

    private MasteryGameTests() {
    }

    @GameTest(template = EMPTY)
    public static void masteryGrowsWithRepetition(GameTestHelper helper) {
        TestPlayers.expectClose(helper, MasteryRules.failureChance(0.0), 0.5, "a fresh move fails one time in two");
        TestPlayers.expectClose(helper, MasteryRules.failureChance(MasteryRules.REPETITIONS_TO_MASTER), 0.0, "never once mastered");
        helper.assertTrue(MasteryRules.failureChance(10.0) < MasteryRules.failureChance(5.0), "practice lowers the risk");
        TestPlayers.expectClose(helper, MasteryRules.powerFactor(0.0), 0.6, "a fresh move is weaker");
        TestPlayers.expectClose(helper, MasteryRules.powerFactor(500.0), 1.0, "full strength once mastered");
        helper.assertTrue(MasteryRules.speedFactor(0.0) < MasteryRules.speedFactor(25.0), "and slower at first");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void freshMovesSometimesFailButStillTeach(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.join(helper);
        MoveRef move = MushoSpells.WATER_BALL.get().ref();
        int botched = 0;
        for (int attempt = 0; attempt < 40; attempt++) {
            botched += MasteryService.botches(player, move) ? 1 : 0;
        }
        helper.assertTrue(botched >= 3 && botched <= 30, "some attempts fail (" + botched + "/40)");
        TestPlayers.expectClose(helper, MasteryService.repetitions(player, move), botched * MasteryRules.FAILED_ATTEMPT,
                "each failure still counts for half a repetition");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void nothingFailsInCreativeMode(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.join(helper);
        player.setGameMode(GameType.CREATIVE);
        MoveRef move = MushoSpells.DISTURB_MAGIC.get().ref();
        for (int attempt = 0; attempt < 100; attempt++) {
            helper.assertFalse(MasteryService.botches(player, move), "in creative mode even a fresh, hard move succeeds");
        }
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void aMasteredMoveNeverFails(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.join(helper);
        MoveRef move = MushoTechniques.ARM_CHOP.get().ref();
        for (int i = 0; i < 50; i++) {
            MasteryService.succeeded(player, move);
        }
        helper.assertTrue(MasteryService.isMastered(player, move), "fifty repetitions master a move");
        for (int attempt = 0; attempt < 100; attempt++) {
            helper.assertFalse(MasteryService.botches(player, move), "a mastered move does not fail");
        }
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void anUnmasteredTechniqueIsSwungSlower(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.join(helper);
        double normal = player.getAttributeValue(Attributes.ATTACK_SPEED);
        TechniqueTempo.apply(player, MasteryRules.speedFactor(0.0), 40);
        helper.assertTrue(player.getAttributeValue(Attributes.ATTACK_SPEED) < normal * 0.8, "slower swing (Epic Fight animation)");
        TechniqueTempo.clear(player);
        TestPlayers.expectClose(helper, player.getAttributeValue(Attributes.ATTACK_SPEED), normal, "normal pace afterwards");
        TestPlayers.leave(player);
        helper.succeed();
    }
}
