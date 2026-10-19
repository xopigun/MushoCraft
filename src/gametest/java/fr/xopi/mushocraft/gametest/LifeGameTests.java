package fr.xopi.mushocraft.gametest;

import com.mojang.authlib.GameProfile;
import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.life.GrowthCurve;
import fr.xopi.mushocraft.life.LifeData;
import fr.xopi.mushocraft.life.LifeService;
import fr.xopi.mushocraft.life.LifeStage;
import fr.xopi.mushocraft.life.LifeTracker;
import fr.xopi.mushocraft.talent.TalentService;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

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
        expectStage(helper, 80.0, LifeStage.ADULT);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void growthCurveInterpolatesAndClamps(GameTestHelper helper) {
        GrowthCurve curve = GrowthCurve.HUMAN;
        expectClose(helper, curve.scaleAt(-1.0), 0.35, "scale before birth");
        expectClose(helper, curve.scaleAt(0.0), 0.35, "scale at birth");
        expectClose(helper, curve.scaleAt(1.5), 0.45, "scale halfway through babyhood");
        expectClose(helper, curve.scaleAt(15.0), 1.0, "scale at adulthood");
        expectClose(helper, curve.scaleAt(60.0), 1.0, "scale of an old adult");
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
        ServerPlayer player = joinMockPlayer(helper);
        helper.assertTrue(LifeService.isBorn(player), "player should be born on login");
        helper.assertValueEqual(LifeService.lifeData(player).orElseThrow().stage(), LifeStage.BABY, "stage at birth");
        helper.assertTrue(TalentService.talents(player).isPresent(), "talents should be rolled at birth");
        expectClose(helper, player.getAttributeValue(Attributes.SCALE), 0.35, "body scale at birth");
        leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void settingAgeUpdatesStageAndSize(GameTestHelper helper) {
        ServerPlayer player = joinMockPlayer(helper);

        LifeService.setAge(player, 7.0);
        helper.assertValueEqual(LifeService.lifeData(player).orElseThrow().stage(), LifeStage.CHILD, "stage at 7");
        expectClose(helper, LifeService.ageInYears(player).orElseThrow(), 7.0, "age after setage 7");
        expectClose(helper, player.getAttributeValue(Attributes.SCALE), GrowthCurve.HUMAN.scaleAt(7.0), "scale at 7");

        LifeService.setAge(player, 20.0);
        helper.assertValueEqual(LifeService.lifeData(player).orElseThrow().stage(), LifeStage.ADULT, "stage at 20");
        expectClose(helper, player.getAttributeValue(Attributes.SCALE), 1.0, "adult scale");

        leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void rebirthStartsANewLife(GameTestHelper helper) {
        ServerPlayer player = joinMockPlayer(helper);
        LifeService.setAge(player, 30.0);

        LifeService.rebirth(player);

        helper.assertValueEqual(LifeService.lifeData(player).orElseThrow().stage(), LifeStage.BABY, "stage after rebirth");
        helper.assertTrue(LifeService.ageInYears(player).orElseThrow() < EPSILON, "age after rebirth should be 0");
        helper.assertTrue(TalentService.talents(player).isPresent(), "talents after rebirth");
        leave(player);
        helper.succeed();
    }

    /**
     * A NeoForge fake player, then our login handler. Vanilla mock players cannot be used: they really join
     * the server, and Epic Fight then sends them packets their fake connection refuses.
     */
    private static ServerPlayer joinMockPlayer(GameTestHelper helper) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "musho_test"));
        LifeTracker.onLogin(new PlayerEvent.PlayerLoggedInEvent(player));
        return player;
    }

    private static void leave(ServerPlayer player) {
        player.discard();
    }

    private static void expectStage(GameTestHelper helper, double age, LifeStage expected) {
        helper.assertValueEqual(LifeStage.at(age), expected, "stage at age " + age);
    }

    private static void expectClose(GameTestHelper helper, double actual, double expected, String what) {
        helper.assertTrue(Math.abs(actual - expected) < EPSILON, what + ": expected " + expected + ", got " + actual);
    }
}
