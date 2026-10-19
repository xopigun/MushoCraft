package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.core.MushoBlocks;
import fr.xopi.mushocraft.life.LifeService;
import fr.xopi.mushocraft.magic.entity.SpellProjectile;
import fr.xopi.mushocraft.magic.spell.MushoSpells;
import fr.xopi.mushocraft.magic.spell.Spell;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(MushoCraft.MODID)
@PrefixGameTestTemplate(false)
public final class AdvancedSpellGameTests {
    private static final String EMPTY = "empty";
    private static final BlockPos MIDDLE = new BlockPos(4, 1, 4);

    private AdvancedSpellGameTests() {
    }

    /** A caster in the middle of the test area, looking south (+z). */
    private static ServerPlayer caster(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.join(helper);
        player.setGameMode(GameType.SURVIVAL);
        Vec3 spot = helper.absoluteVec(Vec3.atBottomCenterOf(MIDDLE));
        player.moveTo(spot.x, spot.y, spot.z, 0.0F, 0.0F);
        player.setYHeadRot(0.0F);
        LifeService.setAge(player, 20.0);
        return player;
    }

    private static boolean cast(ServerPlayer player, Spell spell) {
        return spell.effect().cast(new SpellContext(player, spell, 1.0));
    }

    private static Husk dummy(GameTestHelper helper, BlockPos pos) {
        Husk husk = helper.spawn(EntityType.HUSK, pos);
        husk.setNoAi(true);
        return husk;
    }

    private static boolean hurt(Husk husk) {
        return husk.getHealth() < husk.getMaxHealth();
    }

    @GameTest(template = EMPTY)
    public static void blizzardStormFlingsShardsAllAround(GameTestHelper helper) {
        ServerPlayer player = caster(helper);
        helper.assertTrue(cast(player, MushoSpells.BLIZZARD_STORM.get()), "cast");
        var shards = helper.getLevel().getEntitiesOfClass(SpellProjectile.class, player.getBoundingBox().inflate(3.0));
        helper.assertValueEqual(shards.size(), 16, "sixteen shards");
        helper.assertTrue(shards.stream().anyMatch(shard -> shard.getDeltaMovement().x < -0.5)
                && shards.stream().anyMatch(shard -> shard.getDeltaMovement().z > 0.5), "in every direction");
        shards.forEach(SpellProjectile::discard);
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void tornadoImpactBoresThroughEveryoneInLine(GameTestHelper helper) {
        ServerPlayer player = caster(helper);
        Husk near = dummy(helper, MIDDLE.south(1));
        Husk far = dummy(helper, MIDDLE.south(3));
        Husk aside = dummy(helper, MIDDLE.south(2).east(3));
        cast(player, MushoSpells.TORNADO_IMPACT.get());
        helper.assertTrue(hurt(near) && hurt(far), "both creatures on the line are hit");
        helper.assertFalse(hurt(aside), "a creature off the line is spared");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void sonicBlastHitsOnlyInFront(GameTestHelper helper) {
        ServerPlayer player = caster(helper);
        Husk front = dummy(helper, MIDDLE.south(3));
        Husk behind = dummy(helper, MIDDLE.north(3));
        cast(player, MushoSpells.SONIC_BLAST.get());
        helper.assertTrue(hurt(front), "the creature in front is hit");
        helper.assertTrue(front.getDeltaMovement().z > 0.0, "and thrown away from the caster");
        helper.assertFalse(hurt(behind), "the one behind is not");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void earthFortressEnclosesTheCaster(GameTestHelper helper) {
        ServerPlayer player = caster(helper);
        helper.assertTrue(cast(player, MushoSpells.EARTH_FORTRESS.get()), "cast");
        helper.assertBlockPresent(MushoBlocks.CONJURED_EARTH.get(), MIDDLE.east(2));
        helper.assertBlockPresent(MushoBlocks.CONJURED_EARTH.get(), MIDDLE.north(2).above(2));
        helper.assertBlockPresent(MushoBlocks.CONJURED_EARTH.get(), MIDDLE.above(3));
        helper.assertBlockNotPresent(MushoBlocks.CONJURED_EARTH.get(), MIDDLE);
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void earthHedgehogRaisesManySpikes(GameTestHelper helper) {
        ServerPlayer player = caster(helper);
        player.setXRot(60.0F);
        cast(player, MushoSpells.EARTH_HEDGEHOG.get());
        int spikes = 0;
        for (BlockPos pos : BlockPos.betweenClosed(helper.absolutePos(new BlockPos(0, 0, 0)), helper.absolutePos(new BlockPos(7, 3, 7)))) {
            if (helper.getLevel().getBlockState(pos).is(MushoBlocks.CONJURED_EARTH.get())) {
                spikes++;
            }
        }
        helper.assertTrue(spikes >= 6, "several spikes rose (" + spikes + " earth blocks)");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void dustStormLeavesABlindingCloud(GameTestHelper helper) {
        ServerPlayer player = caster(helper);
        player.setXRot(60.0F);
        cast(player, MushoSpells.DUST_STORM.get());
        var clouds = helper.getLevel().getEntitiesOfClass(AreaEffectCloud.class, player.getBoundingBox().inflate(20.0));
        helper.assertValueEqual(clouds.size(), 1, "one dust cloud");
        clouds.forEach(AreaEffectCloud::discard);
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void exodusFlameBurnsItsCaster(GameTestHelper helper) {
        ServerPlayer player = caster(helper);
        cast(player, MushoSpells.EXODUS_FLAME.get());
        helper.assertTrue(player.getRemainingFireTicks() > 0, "the caster is scorched by the heat");
        helper.getLevel().getEntitiesOfClass(SpellProjectile.class, player.getBoundingBox().inflate(6.0))
                .forEach(SpellProjectile::discard);
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void squallBringsRain(GameTestHelper helper) {
        ServerPlayer player = caster(helper);
        helper.getLevel().setWeatherParameters(6000, 0, false, false);
        helper.assertTrue(cast(player, MushoSpells.SQUALL.get()), "cast under a clear sky");
        helper.assertTrue(helper.getLevel().getLevelData().isRaining(), "it rains");
        helper.assertFalse(cast(player, MushoSpells.SQUALL.get()), "nothing more to do when it already rains");
        helper.getLevel().setWeatherParameters(6000, 0, false, false);
        TestPlayers.leave(player);
        helper.succeed();
    }
}
