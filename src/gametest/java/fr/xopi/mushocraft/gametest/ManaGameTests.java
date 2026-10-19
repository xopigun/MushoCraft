package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.core.MushoAttachments;
import fr.xopi.mushocraft.life.LifeService;
import fr.xopi.mushocraft.magic.mana.ManaRules;
import fr.xopi.mushocraft.magic.mana.ManaService;
import fr.xopi.mushocraft.magic.mana.ManaSnapshot;
import fr.xopi.mushocraft.magic.mana.ManaTraining;
import fr.xopi.mushocraft.talent.Talent;
import fr.xopi.mushocraft.talent.TalentService;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(MushoCraft.MODID)
@PrefixGameTestTemplate(false)
public final class ManaGameTests {
    private static final String EMPTY = "empty";

    /** Fixed numbers so the tests do not depend on the world's config. */
    private static final ManaRules RULES = new ManaRules(10.0, 100.0, 2000.0, 0.05, 0.05, 2.0, 0.01, 3.0, 0.05);

    private ManaGameTests() {
    }

    @GameTest(template = EMPTY)
    public static void naturalCapacityFollowsAgeAndPotential(GameTestHelper helper) {
        TestPlayers.expectClose(helper, RULES.naturalCapacity(0.0, 1.0), 10.0, "newborn");
        TestPlayers.expectClose(helper, RULES.naturalCapacity(7.5, 1.0), 55.0, "halfway to adulthood");
        TestPlayers.expectClose(helper, RULES.naturalCapacity(40.0, 1.0), 100.0, "adult");
        TestPlayers.expectClose(helper, RULES.naturalCapacity(40.0, 1.5), 150.0, "gifted adult");
        TestPlayers.expectClose(helper, RULES.ceiling(1.5), 4500.0, "gifted ceiling");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void growthIsFastestInEarlyChildhood(GameTestHelper helper) {
        TestPlayers.expectClose(helper, RULES.ageGrowthFactor(2.0), 1.0, "toddler");
        TestPlayers.expectClose(helper, RULES.ageGrowthFactor(10.0), 0.525, "ten years old");
        TestPlayers.expectClose(helper, RULES.ageGrowthFactor(30.0), 0.05, "adult");
        double child = RULES.growth(10.0, 2.0, 100.0, 2000.0, 50.0);
        double adult = RULES.growth(10.0, 30.0, 100.0, 2000.0, 50.0);
        helper.assertTrue(child > adult * 10.0, "a child should grow far faster than an adult");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void growthStopsAtCeilingAndRewardsExhaustion(GameTestHelper helper) {
        TestPlayers.expectClose(helper, RULES.growth(10.0, 2.0, 2000.0, 2000.0, 0.0), 0.0, "at the ceiling");
        double partial = RULES.growth(10.0, 2.0, 100.0, 2000.0, 80.0);
        double exhausted = RULES.growth(10.0, 2.0, 100.0, 2000.0, 5.0);
        TestPlayers.expectClose(helper, exhausted, partial * 2.0, "exhaustion bonus");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void newbornStartsWithAFullReservoir(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.join(helper);
        ManaSnapshot mana = ManaService.snapshot(player);
        helper.assertTrue(mana.capacity() > 0.0, "a newborn has some mana");
        TestPlayers.expectClose(helper, mana.current(), mana.capacity(), "full at birth");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void spendingNeedsManaAndMakesItGrow(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.join(helper);
        TalentService.set(player, Talent.MANA_POTENTIAL, 1.0);
        LifeService.setAge(player, 2.0);
        ManaService.refill(player);
        ManaSnapshot before = ManaService.snapshot(player);

        helper.assertFalse(ManaService.trySpend(player, before.capacity() + 1.0), "cannot spend more than you have");
        TestPlayers.expectClose(helper, ManaService.snapshot(player).current(), before.current(), "nothing spent on failure");

        helper.assertTrue(ManaService.trySpend(player, before.capacity()), "can spend the whole reservoir");
        ManaSnapshot after = ManaService.snapshot(player);
        TestPlayers.expectClose(helper, after.current(), 0.0, "empty after spending everything");
        helper.assertTrue(after.capacity() > before.capacity(), "spending to exhaustion grows the reservoir");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void regenerationNeverOverfills(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.join(helper);
        ManaService.setCurrent(player, 0.0);
        ManaService.regenerate(player, 1.0);
        double afterOneSecond = ManaService.snapshot(player).current();
        helper.assertTrue(afterOneSecond > 0.0, "mana comes back over time");

        ManaService.regenerate(player, 100_000.0);
        ManaSnapshot full = ManaService.snapshot(player);
        TestPlayers.expectClose(helper, full.current(), full.capacity(), "regeneration stops at capacity");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void circulationTrainsUntilEmpty(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.join(helper);
        ManaService.refill(player);
        ManaTraining.setCirculating(player, true);
        for (int second = 0; second < 40 && ManaTraining.isCirculating(player); second++) {
            ManaTraining.tick(player, 1.0);
        }
        helper.assertFalse(ManaTraining.isCirculating(player), "circulation stops once the reservoir is empty");
        helper.assertTrue(player.getData(MushoAttachments.MANA).trained() > 0.0, "circulating trained the reservoir");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void rebirthResetsTheReservoir(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.join(helper);
        ManaService.setTrained(player, 500.0);
        LifeService.rebirth(player);
        TestPlayers.expectClose(helper, player.getData(MushoAttachments.MANA).trained(), 0.0, "trained part after rebirth");
        ManaSnapshot mana = ManaService.snapshot(player);
        TestPlayers.expectClose(helper, mana.current(), mana.capacity(), "full after rebirth");
        TestPlayers.leave(player);
        helper.succeed();
    }
}
