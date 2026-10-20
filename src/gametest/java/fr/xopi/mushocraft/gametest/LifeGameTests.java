package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.life.GrowthCurve;
import fr.xopi.mushocraft.life.LifeData;
import fr.xopi.mushocraft.life.LifeService;
import fr.xopi.mushocraft.life.LifeStage;
import fr.xopi.mushocraft.talent.TalentService;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(MushoCraft.MODID)
@PrefixGameTestTemplate(false)
public final class LifeGameTests {
    private static final String EMPTY = "empty";
    private static final double EPSILON = 1.0E-3;

    private LifeGameTests() {
    }

    @GameTest(template = EMPTY)
    public static void stageThresholds(GameTestHelper helper) {
        expectStage(helper, 0.0, LifeStage.BABY);
        expectStage(helper, 2.99, LifeStage.BABY);
        expectStage(helper, 3.0, LifeStage.CHILD);
        expectStage(helper, 11.99, LifeStage.CHILD);
        expectStage(helper, 12.0, LifeStage.ADOLESCENT);
        expectStage(helper, 14.99, LifeStage.ADOLESCENT);
        expectStage(helper, 15.0, LifeStage.ADULT);
        expectStage(helper, 49.0, LifeStage.ADULT);
        expectStage(helper, 50.0, LifeStage.ELDER);
        expectStage(helper, 80.0, LifeStage.ELDER);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void growthCurveInterpolatesAndClamps(GameTestHelper helper) {
        GrowthCurve curve = GrowthCurve.HUMAN;
        TestPlayers.expectClose(helper, curve.scaleAt(-1.0), 0.35, "scale before birth");
        TestPlayers.expectClose(helper, curve.scaleAt(0.0), 0.35, "scale at birth");
        TestPlayers.expectClose(helper, curve.scaleAt(1.5), 0.45, "scale halfway through babyhood");
        TestPlayers.expectClose(helper, curve.scaleAt(15.0), 1.0, "scale at adulthood");
        TestPlayers.expectClose(helper, curve.scaleAt(60.0), 1.0, "scale of an old adult");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void lifeDataSurvivesSerialization(GameTestHelper helper) {
        LifeData original = new LifeData(123_456L, LifeStage.ADOLESCENT);
        Tag encoded = LifeData.CODEC.encodeStart(NbtOps.INSTANCE, original).getOrThrow();
        LifeData decoded = LifeData.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow();
        helper.assertValueEqual(decoded, original, "life data after a save and load");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void playerIsBornAsBabyOnFirstLogin(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.join(helper);
        helper.assertTrue(LifeService.isBorn(player), "player should be born on login");
        helper.assertValueEqual(LifeService.lifeData(player).orElseThrow().stage(), LifeStage.BABY, "stage at birth");
        helper.assertTrue(TalentService.talents(player).isPresent(), "talents should be rolled at birth");
        TestPlayers.expectClose(helper, player.getAttributeValue(Attributes.SCALE), 0.35, "body scale at birth");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void settingAgeUpdatesStageAndSize(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.join(helper);

        LifeService.setAge(player, 7.0);
        helper.assertValueEqual(LifeService.lifeData(player).orElseThrow().stage(), LifeStage.CHILD, "stage at 7");
        TestPlayers.expectClose(helper, LifeService.ageInYears(player).orElseThrow(), 7.0, "age after setage 7");
        TestPlayers.expectClose(helper, player.getAttributeValue(Attributes.SCALE), GrowthCurve.HUMAN.scaleAt(7.0), "scale at 7");

        LifeService.setAge(player, 20.0);
        helper.assertValueEqual(LifeService.lifeData(player).orElseThrow().stage(), LifeStage.ADULT, "stage at 20");
        TestPlayers.expectClose(helper, player.getAttributeValue(Attributes.SCALE), 1.0, "adult scale");

        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void rebirthStartsANewLife(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.join(helper);
        LifeService.setAge(player, 30.0);

        LifeService.rebirth(player);

        helper.assertValueEqual(LifeService.lifeData(player).orElseThrow().stage(), LifeStage.BABY, "stage after rebirth");
        helper.assertTrue(LifeService.ageInYears(player).orElseThrow() < EPSILON, "age after rebirth should be 0");
        helper.assertTrue(TalentService.talents(player).isPresent(), "talents after rebirth");
        TestPlayers.leave(player);
        helper.succeed();
    }

    private static void expectStage(GameTestHelper helper, double age, LifeStage expected) {
        helper.assertValueEqual(LifeStage.at(age), expected, "stage at age " + age);
    }
}
