package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;
import fr.xopi.mushocraft.sword.technique.CounterStances;
import fr.xopi.mushocraft.magic.spell.effect.ProjectileProfile;
import fr.xopi.mushocraft.magic.spell.SpellElement;
import fr.xopi.mushocraft.magic.entity.SpellProjectile;
import fr.xopi.mushocraft.core.MushoDamageTypes;
import fr.xopi.mushocraft.core.MushoEntities;
import fr.xopi.mushocraft.monster.ArmoredWarrior;
import fr.xopi.mushocraft.monster.AssaultDog;
import fr.xopi.mushocraft.monster.EngagementTokens;
import fr.xopi.mushocraft.monster.Goblin;
import fr.xopi.mushocraft.monster.MadSkull;
import fr.xopi.mushocraft.monster.LusterGrizzly;
import fr.xopi.mushocraft.monster.MonsterPack;
import fr.xopi.mushocraft.monster.MushoMonster;
import fr.xopi.mushocraft.monster.PaxCoyote;
import fr.xopi.mushocraft.monster.TarantulaDeathlord;
import fr.xopi.mushocraft.monster.TerminateBoar;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.phys.Vec3;
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

    private static List<AssaultDog> pack(GameTestHelper helper, int size, BlockPos first) {
        return pack(helper, MushoEntities.ASSAULT_DOG.get(), size, first);
    }

    /** A pack spawned together, as natural spawning makes it: the first one leads. */
    private static <T extends MushoMonster> List<T> pack(GameTestHelper helper, EntityType<T> type, int size, BlockPos first) {
        List<T> dogs = new ArrayList<>();
        SpawnGroupData group = null;
        for (int i = 0; i < size; i++) {
            T dog = helper.spawn(type, first.offset(i, 0, 0));
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
        helper.succeedWhen(() -> {
            helper.assertTrue(wounded.distanceTo(husk) > start + 2.0, "the wounded dog withdrew");
            helper.assertFalse(EngagementTokens.isEngaged(wounded, husk), "and gave up its place");
            clear(dogs, husk);
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

    @GameTest(template = EMPTY, timeoutTicks = 120)
    public static void paxCoyotesCloseInThreeAtATime(GameTestHelper helper) {
        List<PaxCoyote> coyotes = pack(helper, MushoEntities.PAX_COYOTE.get(), 5, new BlockPos(1, 1, 1));
        Husk husk = prey(helper, new BlockPos(6, 1, 6));
        coyotes.forEach(coyote -> coyote.setTarget(husk));
        helper.runAtTickTime(60, () -> {
            long engaged = coyotes.stream().filter(coyote -> EngagementTokens.isEngaged(coyote, husk)).count();
            helper.assertTrue(engaged == 3, "three coyotes engage: " + engaged);
            clear(coyotes, husk);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void aPaxCoyoteLeaderDividesIntoItsOwnGroup(GameTestHelper helper) {
        List<PaxCoyote> coyotes = pack(helper, MushoEntities.PAX_COYOTE.get(), 2, new BlockPos(1, 1, 1));
        PaxCoyote leader = coyotes.stream().filter(MushoMonster::isPackLeader).findFirst().orElseThrow();
        MushoMonster offspring = leader.divide().orElseThrow();
        helper.assertTrue(offspring.inSamePack(leader) && !offspring.isPackLeader(), "the new coyote obeys the leader");
        for (int i = 0; i < PaxCoyote.GROUP_CAP; i++) {
            leader.divide();
        }
        helper.assertTrue(MonsterPack.mates(leader).size() + 1 == PaxCoyote.GROUP_CAP, "a group stops growing at its cap");
        helper.killAllEntitiesOfClass(PaxCoyote.class);
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void woundingOneTurnsThePackOnTheAttacker(GameTestHelper helper) {
        List<AssaultDog> dogs = pack(helper, 3, new BlockPos(1, 1, 1));
        Husk attacker = prey(helper, new BlockPos(5, 1, 5));
        dogs.get(2).hurt(helper.getLevel().damageSources().mobAttack(attacker), 2.0F);
        helper.assertTrue(dogs.stream().allMatch(dog -> dog.getTarget() == attacker || dog == dogs.get(2)),
                "the mates turn on whoever wounded one of them");
        clear(dogs, attacker);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void aTerminateBoarComesWithItsDogs(GameTestHelper helper) {
        TerminateBoar boar = helper.spawn(MushoEntities.TERMINATE_BOAR.get(), new BlockPos(4, 1, 4));
        boar.finalizeSpawn(helper.getLevel(), helper.getLevel().getCurrentDifficultyAt(boar.blockPosition()), MobSpawnType.NATURAL, null);
        List<MushoMonster> dogs = MonsterPack.mates(boar);
        helper.assertTrue(boar.isPackLeader(), "the boar leads");
        helper.assertTrue(dogs.size() >= 3 && dogs.stream().allMatch(AssaultDog.class::isInstance), "three or four Assault Dogs follow it");
        dogs.forEach(MushoMonster::discard);
        boar.discard();
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void aTerminateBoarStrikesItsPrey(GameTestHelper helper) {
        TerminateBoar boar = helper.spawn(MushoEntities.TERMINATE_BOAR.get(), new BlockPos(1, 1, 1));
        Husk husk = prey(helper, new BlockPos(4, 1, 4));
        float before = husk.getHealth();
        boar.setTarget(husk);
        helper.succeedWhen(() -> {
            helper.assertTrue(husk.getHealth() < before || !husk.isAlive(), "struck");
            clear(List.of(boar), husk);
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 120)
    public static void aLusterGrizzlyIsSluggishAndShortSightedAtNight(GameTestHelper helper) {
        LusterGrizzly grizzly = helper.spawn(MushoEntities.LUSTER_GRIZZLY.get(), new BlockPos(3, 1, 3));
        double daySpeed = grizzly.getAttributeValue(Attributes.MOVEMENT_SPEED);
        double daySight = grizzly.getAttributeValue(Attributes.FOLLOW_RANGE);
        long time = helper.getLevel().getDayTime();
        helper.getLevel().setDayTime(18000);
        helper.runAtTickTime(50, () -> {
            helper.assertTrue(grizzly.getAttributeValue(Attributes.MOVEMENT_SPEED) < daySpeed * 0.8, "slower at night");
            helper.assertTrue(grizzly.getAttributeValue(Attributes.FOLLOW_RANGE) < daySight * 0.5, "short-sighted at night");
            helper.getLevel().setDayTime(time);
            grizzly.discard();
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void aLoneGoblinKeepsAwayFromAPerson(GameTestHelper helper) {
        Goblin goblin = helper.spawn(MushoEntities.GOBLIN.get(), new BlockPos(4, 1, 4));
        goblin.finalizeSpawn(helper.getLevel(), helper.getLevel().getCurrentDifficultyAt(goblin.blockPosition()), MobSpawnType.EVENT, null);
        ServerPlayer person = TestPlayers.join(helper);
        person.moveTo(helper.absoluteVec(new Vec3(2.5, 1, 4.5)));
        goblin.setTarget(person);
        double start = goblin.distanceTo(person);
        helper.succeedWhen(() -> {
            helper.assertTrue(goblin.distanceTo(person) > start + 2.5, "alone, it backs away");
            goblin.discard();
            TestPlayers.leave(person);
        });
    }

    @GameTest(template = EMPTY)
    public static void aGoblinGangBreedsUpToItsSize(GameTestHelper helper) {
        List<Goblin> gang = pack(helper, MushoEntities.GOBLIN.get(), 3, new BlockPos(1, 1, 1));
        Goblin leader = gang.stream().filter(MushoMonster::isPackLeader).findFirst().orElseThrow();
        for (int i = 0; i < Goblin.GANG_CAP + 2; i++) {
            leader.breed();
        }
        helper.assertTrue(MonsterPack.mates(leader).size() + 1 == Goblin.GANG_CAP, "the gang grew to its size and stopped");
        helper.killAllEntitiesOfClass(Goblin.class);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void lightWeaponsGlanceOffAnArmoredWarrior(GameTestHelper helper) {
        ArmoredWarrior fists = helper.spawn(MushoEntities.ARMORED_WARRIOR.get(), new BlockPos(1, 1, 1));
        ArmoredWarrior axe = helper.spawn(MushoEntities.ARMORED_WARRIOR.get(), new BlockPos(6, 1, 6));
        Husk brawler = prey(helper, new BlockPos(1, 1, 4));
        Husk axeman = prey(helper, new BlockPos(6, 1, 3));
        axeman.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
        fists.hurt(fists.damageSources().mobAttack(brawler), 20.0F);
        axe.hurt(axe.damageSources().mobAttack(axeman), 20.0F);
        float lightLoss = fists.getMaxHealth() - fists.getHealth();
        float heavyLoss = axe.getMaxHealth() - axe.getHealth();
        helper.assertTrue(lightLoss < heavyLoss * 0.5F, "fists barely dent the plate: " + lightLoss + " vs " + heavyLoss);
        clear(List.of(fists, axe), brawler);
        axeman.discard();
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void earthMagicHurtsAnArmoredWarriorTwiceAsMuch(GameTestHelper helper) {
        ArmoredWarrior a = helper.spawn(MushoEntities.ARMORED_WARRIOR.get(), new BlockPos(1, 1, 1));
        ArmoredWarrior b = helper.spawn(MushoEntities.ARMORED_WARRIOR.get(), new BlockPos(6, 1, 6));
        Husk mage = prey(helper, new BlockPos(3, 1, 3));
        SpellProjectile rock = SpellProjectile.create(helper.getLevel(), mage, ProjectileProfile.of(SpellElement.EARTH, 6.0F).build());
        SpellProjectile water = SpellProjectile.create(helper.getLevel(), mage, ProjectileProfile.of(SpellElement.WATER, 6.0F).build());
        a.hurt(MushoDamageTypes.spell(helper.getLevel(), rock, mage), 10.0F);
        b.hurt(MushoDamageTypes.spell(helper.getLevel(), water, mage), 10.0F);
        helper.assertTrue(a.getMaxHealth() - a.getHealth() > (b.getMaxHealth() - b.getHealth()) * 1.5F, "weak against earth");
        clear(List.of(a, b), mage);
        rock.discard();
        water.discard();
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void anArmoredWarriorCountersWithTheWaterGodStyle(GameTestHelper helper) {
        ArmoredWarrior warrior = helper.spawn(MushoEntities.ARMORED_WARRIOR.get(), new BlockPos(2, 1, 2));
        Husk foe = prey(helper, new BlockPos(3, 1, 3));
        warrior.setTarget(foe);
        helper.succeedWhen(() -> {
            helper.assertTrue(CounterStances.active(warrior).isPresent(), "it takes Flow's stance");
            float health = warrior.getHealth();
            warrior.hurt(warrior.damageSources().mobAttack(foe), 6.0F);
            helper.assertTrue(warrior.getHealth() == health && foe.getHealth() < foe.getMaxHealth(), "the blow is turned and answered");
            clear(List.of(warrior), foe);
        });
    }

    @GameTest(template = EMPTY)
    public static void onlyAMadSkullsExposedSkullTakesBlows(GameTestHelper helper) {
        MadSkull hidden = helper.spawn(MushoEntities.MAD_SKULL.get(), new BlockPos(2, 1, 2));
        MadSkull exposed = helper.spawn(MushoEntities.MAD_SKULL.get(), new BlockPos(7, 1, 2));
        MadSkull behind = helper.spawn(MushoEntities.MAD_SKULL.get(), new BlockPos(2, 1, 7));
        for (MadSkull giant : List.of(hidden, exposed, behind)) {
            giant.setYRot(0.0F);
            giant.setYBodyRot(0.0F);
        }
        // facing south (+z): a foe two blocks south is in front, two blocks north is behind
        Husk front = prey(helper, new BlockPos(2, 1, 4));
        Husk front2 = prey(helper, new BlockPos(7, 1, 4));
        Husk back = prey(helper, new BlockPos(2, 1, 5));
        exposed.expose();
        behind.expose();
        hidden.hurt(hidden.damageSources().mobAttack(front), 10.0F);
        exposed.hurt(exposed.damageSources().mobAttack(front2), 10.0F);
        behind.hurt(behind.damageSources().mobAttack(back), 10.0F);
        helper.assertTrue(hidden.getMaxHealth() - hidden.getHealth() <= 1.0F, "the mud shrugs the blow off");
        helper.assertTrue(exposed.getMaxHealth() - exposed.getHealth() >= 10.0F, "the skull, out of the mud, is hit hard");
        helper.assertTrue(behind.getMaxHealth() - behind.getHealth() <= 1.0F, "but only from in front of the chest");
        List.of(hidden, exposed, behind).forEach(MushoMonster::discard);
        List.of(front, front2, back).forEach(Husk::discard);
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 120)
    public static void aMadSkullSlingsMudAtAFarFoe(GameTestHelper helper) {
        MadSkull giant = helper.spawn(MushoEntities.MAD_SKULL.get(), new BlockPos(1, 1, 1));
        Husk foe = prey(helper, new BlockPos(7, 1, 7));
        giant.setTarget(foe);
        helper.succeedWhen(() -> {
            helper.assertFalse(helper.getLevel().getEntitiesOfClass(SpellProjectile.class, giant.getBoundingBox().inflate(16.0),
                    ball -> ball.element() == SpellElement.EARTH).isEmpty(), "a ball of mud or a stone flies");
            clear(List.of(giant), foe);
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 160)
    public static void aTarantulasWebTearsTheWeaponFromTheHand(GameTestHelper helper) {
        TarantulaDeathlord spider = helper.spawn(MushoEntities.TARANTULA_DEATHLORD.get(), new BlockPos(1, 1, 1));
        Husk foe = prey(helper, new BlockPos(6, 1, 6));
        foe.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        spider.setTarget(foe);
        helper.succeedWhen(() -> {
            helper.assertTrue(foe.getMainHandItem().isEmpty(), "the web catches the blade");
            helper.assertFalse(helper.getLevel().getEntitiesOfClass(ItemEntity.class, foe.getBoundingBox().inflate(3.0),
                    item -> item.getItem().is(Items.IRON_SWORD)).isEmpty(), "and the sword falls at its feet");
            clear(List.of(spider), foe);
        });
    }

    @GameTest(template = EMPTY)
    public static void aTarantulasBitePoisonsButNotItself(GameTestHelper helper) {
        TarantulaDeathlord spider = helper.spawn(MushoEntities.TARANTULA_DEATHLORD.get(), new BlockPos(1, 1, 1));
        Pig prey = helper.spawn(EntityType.PIG, new BlockPos(2, 1, 2));
        prey.setNoAi(true);
        spider.doHurtTarget(prey);
        helper.assertTrue(prey.hasEffect(MobEffects.POISON), "the bite carries venom");
        spider.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
        helper.assertFalse(spider.hasEffect(MobEffects.POISON), "its own venom does nothing to it");
        spider.discard();
        prey.discard();
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 120)
    public static void aTarantulaClimbsWalls(GameTestHelper helper) {
        for (int x = 0; x < 8; x++) {
            for (int y = 1; y < 6; y++) {
                helper.setBlock(new BlockPos(x, y, 5), Blocks.STONE);
            }
        }
        TarantulaDeathlord spider = helper.spawn(MushoEntities.TARANTULA_DEATHLORD.get(), new BlockPos(3, 1, 3));
        double ground = spider.getY();
        helper.onEachTick(() -> spider.getMoveControl().setWantedPosition(spider.getX(), spider.getY(), spider.getZ() + 3.0, 1.0));
        helper.succeedWhen(() -> {
            helper.assertTrue(spider.getY() > ground + 2.0, "it goes up the wall");
            spider.discard();
        });
    }
}
