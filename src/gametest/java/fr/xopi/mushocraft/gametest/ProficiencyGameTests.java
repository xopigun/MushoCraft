package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.combat.move.MoveService;
import fr.xopi.mushocraft.core.MushoBlocks;
import fr.xopi.mushocraft.core.MushoMobEffects;
import fr.xopi.mushocraft.life.LifeService;
import fr.xopi.mushocraft.magic.entity.SpellProjectile;
import fr.xopi.mushocraft.magic.mana.ManaService;
import fr.xopi.mushocraft.magic.spell.MagicProficiency;
import fr.xopi.mushocraft.magic.spell.MagicService;
import fr.xopi.mushocraft.magic.spell.MushoSpells;
import fr.xopi.mushocraft.magic.spell.SpellCasting;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellRank;
import fr.xopi.mushocraft.magic.spell.SpellSchool;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;

@GameTestHolder(MushoCraft.MODID)
@PrefixGameTestTemplate(false)
public final class ProficiencyGameTests {
    private static final String EMPTY = "empty";

    private ProficiencyGameTests() {
    }

    /** An adult caster in the test area, looking straight down the test area (south) and slightly downward. */
    private static ServerPlayer caster(GameTestHelper helper, float pitch) {
        ServerPlayer player = TestPlayers.join(helper);
        Vec3 spot = helper.absoluteVec(new Vec3(1.5, 1.0, 0.5));
        player.moveTo(spot.x, spot.y, spot.z, 0.0F, pitch);
        LifeService.setAge(player, 20.0);
        ManaService.setTrained(player, 500.0);
        ManaService.refill(player);
        return player;
    }

    @GameTest(template = EMPTY)
    public static void schoolRankFollowsExperience(GameTestHelper helper) {
        MagicProficiency proficiency = MagicProficiency.none();
        helper.assertValueEqual(proficiency.rank(SpellSchool.WATER), SpellRank.BEGINNER, "start as Beginner");
        proficiency = proficiency.with(SpellSchool.WATER, MagicProficiency.threshold(SpellRank.INTERMEDIATE));
        helper.assertValueEqual(proficiency.rank(SpellSchool.WATER), SpellRank.INTERMEDIATE, "reach Intermediate");
        helper.assertValueEqual(proficiency.rank(SpellSchool.FIRE), SpellRank.BEGINNER, "schools are separate");
        TestPlayers.expectClose(helper, proficiency.power(MushoSpells.WATER_BALL.get()), 1.1, "stronger below one's rank");
        TestPlayers.expectClose(helper, proficiency.power(MushoSpells.WATER_CANNON.get()), 1.0, "normal at one's rank");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void intermediateSpellsNeedPractice(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 0.0F);
        helper.assertValueEqual(MagicService.study(player, MushoSpells.WATER_CANNON.get()), MagicService.StudyResult.NOT_READY,
                "a beginner cannot study Water Cannon");
        MagicService.setExperience(player, SpellSchool.WATER, MagicProficiency.threshold(SpellRank.INTERMEDIATE));
        helper.assertValueEqual(MagicService.study(player, MushoSpells.WATER_CANNON.get()), MagicService.StudyResult.LEARNED,
                "an intermediate water mage can");
        helper.assertValueEqual(MagicService.title(player), Optional.of(SpellRank.INTERMEDIATE), "title follows the best spell");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void castingGivesSchoolExperience(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 0.0F);
        MagicService.learn(player, MushoSpells.FIREBALL.get());
        MoveService.use(player, 0);
        helper.startSequence()
                .thenExecuteAfter(MushoSpells.FIREBALL.get().incantationTicks() + 1, () -> {
                    SpellCasting.tick(player);
                    TestPlayers.expectClose(helper, MagicService.proficiency(player).experience(SpellSchool.FIRE),
                            MushoSpells.FIREBALL.get().manaCost(), "fire experience equals the mana spent");
                })
                .thenExecute(() -> TestPlayers.leave(player))
                .thenSucceed();
    }

    @GameTest(template = EMPTY)
    public static void icePillarRisesWhereAimed(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 30.0F);
        Zombie target = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 1, 4));
        target.setNoAi(true);
        float before = target.getHealth();
        player.lookAt(EntityAnchorArgument.Anchor.EYES, target.position());
        MushoSpells.ICE_PILLAR.get().effect().cast(new SpellContext(player, MushoSpells.ICE_PILLAR.get(), 1.0));
        helper.assertTrue(target.getHealth() < before, "the creature on the spot is hurt");
        boolean pillar = false;
        for (int dy = 0; dy < 4; dy++) {
            pillar |= helper.getLevel().getBlockState(target.blockPosition().above(dy)).is(MushoBlocks.CONJURED_ICE.get());
        }
        helper.assertTrue(pillar, "an ice pillar stands where the creature was");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void earthPillarDropsABoulder(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 40.0F);
        MushoSpells.EARTH_PILLAR.get().effect().cast(new SpellContext(player, MushoSpells.EARTH_PILLAR.get(), 1.0));
        SpellProjectile boulder = helper.getLevel().getEntitiesOfClass(SpellProjectile.class,
                player.getBoundingBox().inflate(16.0)).stream().findFirst().orElse(null);
        helper.assertTrue(boulder != null, "a boulder was conjured");
        helper.assertTrue(boulder.getY() > player.getY() + 4.0, "the boulder starts high above the ground");
        helper.assertTrue(boulder.getDeltaMovement().y < 0.0, "and falls");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void physicalShieldWeakensBlows(GameTestHelper helper) {
        Zombie shielded = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 1, 1));
        Zombie bare = helper.spawn(EntityType.ZOMBIE, new BlockPos(5, 1, 5));
        shielded.addEffect(new MobEffectInstance(MushoMobEffects.PHYSICAL_SHIELD, 200));
        float shieldedBefore = shielded.getHealth();
        float bareBefore = bare.getHealth();
        DamageSource blowOnShielded = helper.getLevel().damageSources().mobAttack(bare);
        DamageSource blowOnBare = helper.getLevel().damageSources().mobAttack(shielded);
        shielded.hurt(blowOnShielded, 10.0F);
        bare.hurt(blowOnBare, 10.0F);
        double ratio = (shieldedBefore - shielded.getHealth()) / (bareBefore - bare.getHealth());
        TestPlayers.expectClose(helper, ratio, 0.4, "physical shield lets 40% of a blow through");
        helper.succeed();
    }
}
