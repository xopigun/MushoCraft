package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.body.BodyRules;
import fr.xopi.mushocraft.body.BodyService;
import fr.xopi.mushocraft.body.BodyStat;
import fr.xopi.mushocraft.body.aura.AuraLevel;
import fr.xopi.mushocraft.body.aura.BattleAuraService;
import fr.xopi.mushocraft.life.LifeService;
import fr.xopi.mushocraft.magic.mana.ManaService;
import fr.xopi.mushocraft.talent.Talent;
import fr.xopi.mushocraft.talent.TalentService;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(MushoCraft.MODID)
@PrefixGameTestTemplate(false)
public final class BodyGameTests {
    private static final String EMPTY = "empty";

    private BodyGameTests() {
    }

    /** An ordinary adult in survival, with neutral talents. */
    private static ServerPlayer adult(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.join(helper);
        player.setGameMode(GameType.SURVIVAL);
        for (Talent talent : Talent.values()) {
            TalentService.set(player, talent, 1.0);
        }
        LifeService.setAge(player, 20.0);
        BodyService.apply(player);
        return player;
    }

    @GameTest(template = EMPTY)
    public static void trainingHasDiminishingReturnsAndTalentMatters(GameTestHelper helper) {
        TestPlayers.expectClose(helper, BodyRules.progress(0.0, 1.0), 0.0, "untrained ordinary human");
        double half = BodyRules.progress(2_000.0, 1.0);
        double more = BodyRules.progress(4_000.0, 1.0);
        helper.assertTrue(more > half && more - half < half, "each extra hour of training is worth less");
        helper.assertTrue(BodyRules.progress(1.0E9, 1.0) <= 1.0, "an ordinary human never passes the ceiling");
        helper.assertTrue(BodyRules.progress(1.0E9, 1.4) > 1.3, "the gifted reach further");
        helper.assertTrue(BodyRules.progress(0.0, 0.7) < 0.0, "the frail start below the common base");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void bodyMaturesWithAge(GameTestHelper helper) {
        TestPlayers.expectClose(helper, BodyRules.bodyMaturity(0.0), 0.3, "newborn");
        TestPlayers.expectClose(helper, BodyRules.bodyMaturity(30.0), 1.0, "adult");
        helper.assertTrue(BodyRules.trainingSpeed(6.0) > BodyRules.trainingSpeed(30.0), "children learn faster");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void strengthTrainingRaisesAttackDamage(GameTestHelper helper) {
        ServerPlayer player = adult(helper);
        double before = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        BodyService.train(player, BodyStat.STRENGTH, 3_000.0);
        BodyService.apply(player);
        helper.assertTrue(player.getAttributeValue(Attributes.ATTACK_DAMAGE) > before + 2.0, "trained arms hit harder");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void babiesAreFrail(GameTestHelper helper) {
        ServerPlayer player = adult(helper);
        double adultHealth = player.getMaxHealth();
        LifeService.setAge(player, 0.0);
        BodyService.apply(player);
        TestPlayers.expectClose(helper, player.getMaxHealth(), adultHealth - 7.0, "a newborn has 7 less health");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void creativePlayersDoNotTrain(GameTestHelper helper) {
        ServerPlayer player = adult(helper);
        player.setGameMode(GameType.CREATIVE);
        BodyService.train(player, BodyStat.SPEED, 100.0);
        TestPlayers.expectClose(helper, BodyService.training(player).experience(BodyStat.SPEED), 0.0, "no training in creative");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void physicalTrainingAwakensTheAura(GameTestHelper helper) {
        ServerPlayer player = adult(helper);
        helper.assertValueEqual(BattleAuraService.level(player), AuraLevel.NONE, "no aura at first");
        BodyService.train(player, BodyStat.STRENGTH, AuraLevel.UNCONSCIOUS.mastery() * 2.0 + 1.0);
        helper.assertValueEqual(BattleAuraService.level(player), AuraLevel.UNCONSCIOUS, "training awakens the aura");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void raisedAuraStrengthensAndBurnsMana(GameTestHelper helper) {
        ServerPlayer player = adult(helper);
        ManaService.refill(player);
        BattleAuraService.setMastery(player, AuraLevel.CONSCIOUS.mastery());
        double attackBefore = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        double manaBefore = ManaService.snapshot(player).current();

        BattleAuraService.toggle(player);
        helper.assertTrue(BattleAuraService.isUp(player), "aura raised at will");
        helper.assertTrue(player.getAttributeValue(Attributes.ATTACK_DAMAGE) > attackBefore, "aura strengthens the body");

        BattleAuraService.tick(player, 1.0);
        helper.assertTrue(ManaService.snapshot(player).current() < manaBefore, "aura burns mana");

        ManaService.setCurrent(player, 0.0);
        BattleAuraService.tick(player, 1.0);
        helper.assertFalse(BattleAuraService.isUp(player), "aura falls when mana runs out");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void unconsciousAuraCannotBeRaisedAtWill(GameTestHelper helper) {
        ServerPlayer player = adult(helper);
        BattleAuraService.setMastery(player, AuraLevel.UNCONSCIOUS.mastery());
        BattleAuraService.toggle(player);
        helper.assertFalse(BattleAuraService.data(player).raised(), "not under control yet");
        TestPlayers.leave(player);
        helper.succeed();
    }
}
