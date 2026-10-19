package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.core.MushoBlocks;
import fr.xopi.mushocraft.core.MushoItems;
import fr.xopi.mushocraft.life.LifeService;
import fr.xopi.mushocraft.magic.mana.ManaService;
import fr.xopi.mushocraft.magic.summon.LamplightSpirit;
import fr.xopi.mushocraft.magic.summon.SummonBonds;
import fr.xopi.mushocraft.magic.summon.SummonRules;
import fr.xopi.mushocraft.magic.summon.SummonedSpirit;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/**
 * Summoning magic: papers spend their mana and themselves, lamplight spirits shed real light, fighting spirits live on
 * their master's mana, and bound creatures never turn on their summoner.
 */
@GameTestHolder(MushoCraft.MODID)
@PrefixGameTestTemplate(false)
public final class SummoningGameTests {
    private static final String EMPTY = "empty";
    private static final BlockPos MIDDLE = new BlockPos(4, 1, 4);

    private SummoningGameTests() {
    }

    private static ServerPlayer summoner(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.join(helper);
        player.setGameMode(GameType.SURVIVAL);
        Vec3 spot = helper.absoluteVec(Vec3.atBottomCenterOf(MIDDLE));
        player.moveTo(spot.x, spot.y, spot.z, 0.0F, 0.0F);
        LifeService.setAge(player, 20.0);
        ManaService.setTrained(player, 500.0);
        ManaService.refill(player);
        return player;
    }

    private static void usePaper(ServerPlayer player, ItemStack paper) {
        player.setItemInHand(InteractionHand.MAIN_HAND, paper);
        paper.getItem().use(player.level(), player, InteractionHand.MAIN_HAND);
    }

    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void lamplightSpiritsCircleAndShedLight(GameTestHelper helper) {
        ServerPlayer player = summoner(helper);
        double before = ManaService.snapshot(player).current();
        ItemStack paper = new ItemStack(MushoItems.LAMPLIGHT_SUMMONING_PAPER.get());
        usePaper(player, paper);
        helper.assertTrue(paper.isEmpty(), "the paper is spent");
        helper.assertTrue(ManaService.snapshot(player).current() <= before - SummonRules.LAMPLIGHT_COST + 0.01, "its mana is paid");
        helper.succeedWhen(() -> {
            List<LamplightSpirit> spirits = helper.getLevel().getEntitiesOfClass(LamplightSpirit.class, player.getBoundingBox().inflate(5.0));
            helper.assertTrue(spirits.size() == SummonRules.LAMPLIGHT_SPIRITS, "three spirits circle the caster");
            helper.assertTrue(helper.getLevel().getBlockState(spirits.get(0).blockPosition()).is(MushoBlocks.SPIRIT_LIGHT.get()),
                    "and light the place where they float");
            spirits.forEach(LamplightSpirit::discard);
            TestPlayers.leave(player);
        });
    }

    @GameTest(template = EMPTY)
    public static void summoningNeedsTheMana(GameTestHelper helper) {
        ServerPlayer player = summoner(helper);
        ManaService.setCurrent(player, 1.0);
        ItemStack paper = new ItemStack(MushoItems.SPIRIT_SUMMONING_PAPER.get());
        usePaper(player, paper);
        helper.assertFalse(paper.isEmpty(), "without the mana, the paper is kept");
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(SummonedSpirit.class, player.getBoundingBox().inflate(5.0)).isEmpty(),
                "and nothing is summoned");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 80)
    public static void aSpiritLivesOnItsMastersMana(GameTestHelper helper) {
        ServerPlayer player = summoner(helper);
        usePaper(player, new ItemStack(MushoItems.SPIRIT_SUMMONING_PAPER.get()));
        List<SummonedSpirit> spirits = helper.getLevel().getEntitiesOfClass(SummonedSpirit.class, player.getBoundingBox().inflate(5.0));
        helper.assertTrue(spirits.size() == 1, "a spirit is summoned");
        SummonedSpirit spirit = spirits.get(0);
        helper.assertTrue(SummonBonds.summoner(spirit).orElse(null) == player, "bound to its summoner");
        helper.assertTrue(spirit.getMaxHealth() > 10.0F, "and as strong as its master's reservoir");
        ManaService.setCurrent(player, 0.0);
        helper.succeedWhen(() -> {
            helper.assertTrue(spirit.isRemoved(), "without mana to feed on, it ceases to exist");
            TestPlayers.leave(player);
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void aBoundCreatureNeverTurnsOnItsSummoner(GameTestHelper helper) {
        ServerPlayer player = summoner(helper);
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, MIDDLE.south(3));
        SummonBonds.bind(zombie, player, 200);
        Pig pig = helper.spawn(EntityType.PIG, MIDDLE.north(2));
        zombie.setTarget(player);
        helper.assertTrue(zombie.getTarget() == null, "it will not target its summoner");
        helper.assertTrue(SummonBonds.allied(zombie, player), "they are allies");
        player.setLastHurtMob(pig);
        helper.succeedWhen(() -> {
            helper.assertTrue(zombie.getTarget() == pig, "it strikes what its master strikes");
            pig.discard();
            zombie.discard();
            TestPlayers.leave(player);
        });
    }

    @GameTest(template = EMPTY)
    public static void tooStrongACreatureMayBreakLoose(GameTestHelper helper) {
        helper.assertTrue(SummonRules.lossOfControlChance(10.0, 500.0) == 0.0, "a weak creature always obeys");
        helper.assertTrue(SummonRules.lossOfControlChance(100.0, 250.0) > 0.5, "one with far more mana likely breaks loose");
        helper.assertTrue(SummonRules.lossOfControlChance(1000.0, 10.0) <= 0.9, "never certain");
        helper.succeed();
    }
}
