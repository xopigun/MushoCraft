package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.core.MushoEntities;
import fr.xopi.mushocraft.monster.AssaultDog;
import fr.xopi.mushocraft.monster.EngagementTokens;
import fr.xopi.mushocraft.monster.MonsterPack;
import fr.xopi.mushocraft.monster.MushoMonster;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Husk;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Monsters and their pack AI: the leader's call, a few engaging while the rest circle, the wounded backing off, the
 * pack choosing a new leader, and the bites themselves.
 */
@GameTestHolder(MushoCraft.MODID)
@PrefixGameTestTemplate(false)
public final class MonsterGameTests {
    private static final String EMPTY = "empty";

    private MonsterGameTests() {
    }

    /** A pack spawned together, as natural spawning makes it: the first one leads. */
    private static List<AssaultDog> pack(GameTestHelper helper, int size, BlockPos first) {
        List<AssaultDog> dogs = new ArrayList<>();
        SpawnGroupData group = null;
        for (int i = 0; i < size; i++) {
            AssaultDog dog = helper.spawn(MushoEntities.ASSAULT_DOG.get(), first.offset(i, 0, 0));
            group = dog.finalizeSpawn(helper.getLevel(), helper.getLevel().getCurrentDifficultyAt(dog.blockPosition()),
                    MobSpawnType.EVENT, group);
            dogs.add(dog);
        }
        return dogs;
    }

    private static Husk prey(GameTestHelper helper, BlockPos pos) {
        Husk husk = helper.spawn(EntityType.HUSK, pos);
        husk.setNoAi(true);
        return husk;
    }

    private static void clear(List<? extends MushoMonster> monsters, Husk prey) {
        monsters.forEach(MushoMonster::discard);
        prey.discard();
    }

    @GameTest(template = EMPTY)
    public static void aSpawnedGroupFormsOnePackWithOneLeader(GameTestHelper helper) {
        List<AssaultDog> dogs = pack(helper, 4, new BlockPos(1, 1, 1));
        helper.assertTrue(dogs.stream().filter(MushoMonster::isPackLeader).count() == 1, "exactly one leader");
        helper.assertTrue(dogs.stream().allMatch(dog -> dog.inSamePack(dogs.get(0))), "all in the same pack");
        helper.assertFalse(dogs.get(0).isAlliedTo(helper.spawn(MushoEntities.ASSAULT_DOG.get(), new BlockPos(6, 1, 6))),
                "a dog of another pack is no ally");
        dogs.forEach(MushoMonster::discard);
        helper.killAllEntitiesOfClass(AssaultDog.class);
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 120)
    public static void theLeadersCallTurnsThePackOnItsPrey(GameTestHelper helper) {
        List<AssaultDog> dogs = pack(helper, 4, new BlockPos(1, 1, 1));
        Husk husk = prey(helper, new BlockPos(6, 1, 6));
        AssaultDog leader = dogs.stream().filter(MushoMonster::isPackLeader).findFirst().orElseThrow();
        leader.setTarget(husk);
        helper.succeedWhen(() -> {
            helper.assertTrue(dogs.stream().allMatch(dog -> dog.getTarget() == husk), "the whole pack hunts the prey");
            clear(dogs, husk);
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 120)
    public static void onlyTwoDogsCloseInAtOnce(GameTestHelper helper) {
        List<AssaultDog> dogs = pack(helper, 4, new BlockPos(1, 1, 1));
        Husk husk = prey(helper, new BlockPos(6, 1, 6));
        dogs.forEach(dog -> dog.setTarget(husk));
        helper.runAtTickTime(60, () -> {
            long engaged = dogs.stream().filter(dog -> EngagementTokens.isEngaged(dog, husk)).count();
            helper.assertTrue(engaged == 2, "two dogs engage, the others circle: " + engaged);
            clear(dogs, husk);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void theDogsBiteTheirPrey(GameTestHelper helper) {
        List<AssaultDog> dogs = pack(helper, 2, new BlockPos(1, 1, 1));
        Husk husk = prey(helper, new BlockPos(4, 1, 4));
        float before = husk.getHealth();
        dogs.forEach(dog -> dog.setTarget(husk));
        helper.succeedWhen(() -> {
            helper.assertTrue(husk.getHealth() < before || !husk.isAlive(), "bitten");
            clear(dogs, husk);
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 120)
    public static void aBadlyWoundedDogBacksOff(GameTestHelper helper) {
        List<AssaultDog> dogs = pack(helper, 3, new BlockPos(2, 1, 2));
        Husk husk = prey(helper, new BlockPos(4, 1, 4));
        AssaultDog wounded = dogs.get(1);
        dogs.forEach(dog -> dog.setTarget(husk));
        wounded.setHealth(wounded.getMaxHealth() * 0.2F);
        double start = wounded.distanceTo(husk);
        helper.runAtTickTime(50, () -> {
            helper.assertTrue(wounded.distanceTo(husk) > start + 2.0, "the wounded dog withdrew");
            helper.assertFalse(EngagementTokens.isEngaged(wounded, husk), "and gave up its place");
            clear(dogs, husk);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void whenTheLeaderFallsTheStrongestLeads(GameTestHelper helper) {
        List<AssaultDog> dogs = pack(helper, 3, new BlockPos(1, 1, 1));
        AssaultDog leader = dogs.stream().filter(MushoMonster::isPackLeader).findFirst().orElseThrow();
        dogs.stream().filter(dog -> dog != leader).findFirst().orElseThrow().setHealth(5.0F);
        leader.kill();
        List<AssaultDog> left = dogs.stream().filter(dog -> dog != leader).toList();
        helper.assertTrue(left.stream().filter(MushoMonster::isPackLeader).count() == 1, "one new leader");
        helper.assertTrue(MonsterPack.leader(left.get(0)).map(l -> l.getHealth() > 5.0F).orElse(false), "the strongest one");
        left.forEach(MushoMonster::discard);
        helper.succeed();
    }
}
