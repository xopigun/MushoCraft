package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.body.BodyService;
import fr.xopi.mushocraft.body.BodyStat;
import fr.xopi.mushocraft.body.SprintMomentum;
import fr.xopi.mushocraft.life.LifeService;
import fr.xopi.mushocraft.talent.Talent;
import fr.xopi.mushocraft.talent.TalentService;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import yesman.epicfight.registry.entries.EpicFightAttributes;

/**
 * Running and jumping momentum, and the speed stat's faster swings. Test players are not ticked by the server:
 * the tests run the momentum themselves and stand in for the boosts wearing off.
 */
@GameTestHolder(MushoCraft.MODID)
@PrefixGameTestTemplate(false)
public final class MomentumGameTests {
    private static final String EMPTY = "empty";
    private static final double FULLY_TRAINED = 1.0E7;

    private MomentumGameTests() {
    }

    private static ServerPlayer runner(GameTestHelper helper, double speedTraining) {
        ServerPlayer player = TestPlayers.join(helper);
        player.setGameMode(GameType.SURVIVAL);
        for (Talent talent : Talent.values()) {
            TalentService.set(player, talent, 1.0);
        }
        LifeService.setAge(player, 20.0);
        BodyService.setExperience(player, BodyStat.SPEED, speedTraining);
        BodyService.apply(player);
        player.setOnGround(true);
        return player;
    }

    private static int level(ServerPlayer player, Holder<MobEffect> effect) {
        MobEffectInstance instance = player.getEffect(effect);
        return instance == null ? -1 : instance.getAmplifier();
    }

    /** Stands in for a few ticks passing: the boost is about to run out. */
    private static void wearOff(ServerPlayer player, Holder<MobEffect> effect) {
        int amplifier = level(player, effect);
        player.removeEffect(effect);
        player.addEffect(new MobEffectInstance(effect, 4, amplifier, false, false, false));
    }

    @GameTest(template = EMPTY)
    public static void anUntrainedRunnerGetsNoSpeed(GameTestHelper helper) {
        ServerPlayer player = runner(helper, 0.0);
        player.setSprinting(true);
        SprintMomentum.tick(player);
        helper.assertValueEqual(level(player, MobEffects.MOVEMENT_SPEED), -1, "no boost for an ordinary body");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void aTrainedRunnerSpeedsUpStepByStep(GameTestHelper helper) {
        ServerPlayer player = runner(helper, FULLY_TRAINED);
        player.setSprinting(true);
        SprintMomentum.tick(player);
        helper.assertValueEqual(level(player, MobEffects.MOVEMENT_SPEED), 0, "the run starts at Speed I");
        SprintMomentum.tick(player);
        helper.assertValueEqual(level(player, MobEffects.MOVEMENT_SPEED), 0, "no jump while the boost is fresh");

        int ceiling = SprintMomentum.speedCeiling(player);
        for (int step = 1; step <= ceiling + 3; step++) {
            wearOff(player, MobEffects.MOVEMENT_SPEED);
            SprintMomentum.tick(player);
        }
        helper.assertValueEqual(level(player, MobEffects.MOVEMENT_SPEED), ceiling, "it climbs to the body's ceiling, no further");
        helper.assertTrue(ceiling >= 5, "a fully trained adult runs very fast (ceiling " + ceiling + ")");
        helper.assertTrue(level(player, MobEffects.JUMP) >= 0, "full speed carries into the jump");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void crouchingChargesAJump(GameTestHelper helper) {
        ServerPlayer player = runner(helper, FULLY_TRAINED);
        player.setShiftKeyDown(true);
        SprintMomentum.tick(player);
        for (int step = 0; step < 6; step++) {
            wearOff(player, MobEffects.JUMP);
            SprintMomentum.tick(player);
        }
        helper.assertValueEqual(level(player, MobEffects.JUMP), SprintMomentum.jumpCeiling(player), "fully charged jump");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void aPotionIsNeverOverridden(GameTestHelper helper) {
        ServerPlayer player = runner(helper, FULLY_TRAINED);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 1));
        player.setSprinting(true);
        for (int step = 0; step < 5; step++) {
            SprintMomentum.tick(player);
        }
        MobEffectInstance speed = player.getEffect(MobEffects.MOVEMENT_SPEED);
        helper.assertTrue(speed != null && speed.getAmplifier() == 1 && speed.getDuration() > 500, "the potion stays as it was");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void speedMakesSwingsFaster(GameTestHelper helper) {
        ServerPlayer player = runner(helper, 0.0);
        double before = player.getAttributeValue(Attributes.ATTACK_SPEED);
        double offhandBefore = player.getAttributeValue(EpicFightAttributes.OFFHAND_ATTACK_SPEED);
        BodyService.setExperience(player, BodyStat.SPEED, FULLY_TRAINED);
        BodyService.apply(player);
        helper.assertTrue(player.getAttributeValue(Attributes.ATTACK_SPEED) > before * 1.3, "faster swings (and animations)");
        helper.assertTrue(player.getAttributeValue(EpicFightAttributes.OFFHAND_ATTACK_SPEED) > offhandBefore * 1.3, "off hand too");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void vitalityAndResistanceShowOnTheBody(GameTestHelper helper) {
        ServerPlayer player = runner(helper, 0.0);
        BodyService.setExperience(player, BodyStat.VITALITY, FULLY_TRAINED);
        BodyService.setExperience(player, BodyStat.RESISTANCE, FULLY_TRAINED);
        BodyService.apply(player);
        helper.assertTrue(player.getMaxHealth() >= 39.0F, "ten more hearts (" + player.getMaxHealth() + ")");
        helper.assertTrue(player.getAttributeValue(Attributes.ARMOR) >= 9.9, "armor");
        helper.assertTrue(player.getAttributeValue(Attributes.ARMOR_TOUGHNESS) >= 5.9, "armor toughness");
        TestPlayers.leave(player);
        helper.succeed();
    }
}
