package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.combat.move.CooldownService;
import fr.xopi.mushocraft.combat.move.MoveLoadout;
import fr.xopi.mushocraft.combat.move.MoveRef;
import fr.xopi.mushocraft.combat.move.MoveService;
import fr.xopi.mushocraft.core.MushoBlocks;
import fr.xopi.mushocraft.core.MushoDamageTypes;
import fr.xopi.mushocraft.core.MushoDataComponents;
import fr.xopi.mushocraft.core.MushoItems;
import fr.xopi.mushocraft.core.MushoMobEffects;
import fr.xopi.mushocraft.life.LifeService;
import fr.xopi.mushocraft.magic.entity.SpellProjectile;
import fr.xopi.mushocraft.magic.mana.ManaService;
import fr.xopi.mushocraft.magic.spell.MagicService;
import fr.xopi.mushocraft.magic.spell.MushoSpells;
import fr.xopi.mushocraft.magic.spell.Spell;
import fr.xopi.mushocraft.magic.spell.SpellCasting;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellElement;
import fr.xopi.mushocraft.magic.spell.effect.ProjectileProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;

@GameTestHolder(MushoCraft.MODID)
@PrefixGameTestTemplate(false)
public final class SpellGameTests {
    private static final String EMPTY = "empty";

    private SpellGameTests() {
    }

    /** A born fake player standing in the test area, with a full and comfortable mana reservoir. */
    private static ServerPlayer caster(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.join(helper);
        Vec3 spot = helper.absoluteVec(new Vec3(1.5, 1.0, 1.5));
        player.moveTo(spot.x, spot.y, spot.z, 0.0F, 0.0F);
        LifeService.setAge(player, 20.0);
        ManaService.setTrained(player, 500.0);
        ManaService.refill(player);
        return player;
    }

    @GameTest(template = EMPTY)
    public static void learningPutsTheSpellInTheBar(GameTestHelper helper) {
        ServerPlayer player = caster(helper);
        Spell waterBall = MushoSpells.WATER_BALL.get();
        helper.assertTrue(MagicService.learn(player, waterBall), "first learning succeeds");
        helper.assertFalse(MagicService.learn(player, waterBall), "learning twice does nothing");
        helper.assertValueEqual(MoveService.loadout(player).get(0), Optional.of(waterBall.ref()), "spell placed in slot 1");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void unknownSpellCannotBeCast(GameTestHelper helper) {
        ServerPlayer player = caster(helper);
        MoveService.setSlot(player, 0, Optional.of(MushoSpells.FIREBALL.get().ref()));
        helper.assertFalse(MoveService.use(player, 0).success(), "casting an unknown spell fails");
        helper.assertFalse(MoveService.use(player, 5).success(), "an empty slot does nothing");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void chantThenCastSpendsManaAndStartsCooldown(GameTestHelper helper) {
        ServerPlayer player = caster(helper);
        Spell waterBall = MushoSpells.WATER_BALL.get();
        MagicService.learn(player, waterBall);
        double manaBefore = ManaService.snapshot(player).current();

        helper.assertTrue(MoveService.use(player, 0).success(), "chant starts");
        helper.assertTrue(SpellCasting.chanting(player).isPresent(), "player is chanting");
        TestPlayers.expectClose(helper, ManaService.snapshot(player).current(), manaBefore, "no mana spent while chanting");

        helper.startSequence()
                .thenExecuteAfter(waterBall.incantationTicks() + 1, () -> {
                    SpellCasting.tick(player);
                    helper.assertTrue(SpellCasting.chanting(player).isEmpty(), "chant is over");
                    TestPlayers.expectClose(helper, ManaService.snapshot(player).current(),
                            manaBefore - waterBall.manaCost(), "mana paid once the spell takes shape");
                    helper.assertTrue(CooldownService.active(player, waterBall.cooldownKey()).isPresent(), "cooldown started");
                    helper.assertFalse(MoveService.use(player, 0).success(), "cannot recast during the cooldown");
                    helper.assertFalse(helper.getLevel().getEntitiesOfClass(SpellProjectile.class,
                            player.getBoundingBox().inflate(4.0)).isEmpty(), "a water ball was thrown");
                })
                .thenExecute(() -> TestPlayers.leave(player))
                .thenSucceed();
    }

    @GameTest(template = EMPTY)
    public static void brokenChantCostsNothing(GameTestHelper helper) {
        ServerPlayer player = caster(helper);
        MagicService.learn(player, MushoSpells.FIREBALL.get());
        double manaBefore = ManaService.snapshot(player).current();
        MoveService.use(player, 0);
        SpellCasting.interrupt(player);
        helper.assertTrue(SpellCasting.chanting(player).isEmpty(), "chant broken");
        TestPlayers.expectClose(helper, ManaService.snapshot(player).current(), manaBefore, "no mana lost");
        helper.assertTrue(CooldownService.active(player, MushoSpells.FIREBALL.get().cooldownKey()).isEmpty(), "no cooldown");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void notEnoughManaRefusesTheChant(GameTestHelper helper) {
        ServerPlayer player = caster(helper);
        MagicService.learn(player, MushoSpells.WATER_BALL.get());
        ManaService.setCurrent(player, 1.0);
        helper.assertFalse(MoveService.use(player, 0).success(), "chant refused without mana");
        helper.assertTrue(SpellCasting.chanting(player).isEmpty(), "no chant started");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void waterShieldRaisesAWall(GameTestHelper helper) {
        ServerPlayer player = caster(helper);
        boolean cast = MushoSpells.WATER_SHIELD.get().effect().cast(
                new SpellContext(player, MushoSpells.WATER_SHIELD.get(), 1.0));
        helper.assertTrue(cast, "wall raised");
        BlockPos front = player.blockPosition().relative(player.getDirection(), 2);
        helper.assertTrue(helper.getLevel().getBlockState(front).is(MushoBlocks.WATER_WALL.get()), "water wall in front of the caster");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void iceBladeConjuresAMeltingSword(GameTestHelper helper) {
        ServerPlayer player = caster(helper);
        MushoSpells.ICE_BLADE.get().effect().cast(
                new SpellContext(player, MushoSpells.ICE_BLADE.get(), 1.0));
        ItemStack blade = player.getInventory().items.stream()
                .filter(stack -> stack.is(MushoItems.ICE_BLADE.get())).findFirst().orElse(ItemStack.EMPTY);
        helper.assertFalse(blade.isEmpty(), "ice blade in the inventory");
        helper.assertTrue(blade.has(MushoDataComponents.CONJURED_UNTIL.get()), "the blade knows when it melts");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void magicShieldWeakensSpells(GameTestHelper helper) {
        Zombie shielded = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 1, 1));
        Zombie bare = helper.spawn(EntityType.ZOMBIE, new BlockPos(5, 1, 5));
        shielded.addEffect(new MobEffectInstance(MushoMobEffects.MAGIC_SHIELD, 200));
        float shieldedBefore = shielded.getHealth();
        float bareBefore = bare.getHealth();
        shielded.hurt(MushoDamageTypes.spell(helper.getLevel(), shielded, null), 10.0F);
        bare.hurt(MushoDamageTypes.spell(helper.getLevel(), bare, null), 10.0F);
        double ratio = (shieldedBefore - shielded.getHealth()) / (bareBefore - bare.getHealth());
        TestPlayers.expectClose(helper, ratio, 0.4, "shield lets 40% of the spell through");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void exorcistLightBurnsTheUndead(GameTestHelper helper) {
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 1, 3));
        zombie.setNoAi(true);
        float before = zombie.getHealth();
        SpellProjectile light = SpellProjectile.create(helper.getLevel(), zombie,
                ProjectileProfile.of(SpellElement.LIGHT, 3.0F).speed(0.8F).undeadMultiplier(3.0F).build());
        light.setOwner(null);
        Vec3 start = helper.absoluteVec(new Vec3(1.5, 1.8, 0.6));
        light.setPos(start);
        light.shoot(0.0, 0.0, 1.0, 0.8F, 0.0F);
        helper.getLevel().addFreshEntity(light);
        helper.succeedWhen(() -> helper.assertTrue(before - zombie.getHealth() >= 8.9F,
                "undead take triple damage, lost " + (before - zombie.getHealth())));
    }

    @GameTest(template = EMPTY)
    public static void rebirthForgetsMagic(GameTestHelper helper) {
        ServerPlayer player = caster(helper);
        MagicService.learn(player, MushoSpells.HEALING.get());
        LifeService.rebirth(player);
        helper.assertTrue(MagicService.knownSpells(player).isEmpty(), "no spell known after rebirth");
        helper.assertValueEqual(MoveService.loadout(player), MoveLoadout.empty(), "empty move bar after rebirth");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void loadoutSurvivesSerialization(GameTestHelper helper) {
        MoveLoadout original = MoveLoadout.empty()
                .with(0, Optional.of(MoveRef.spell(MushoSpells.WATER_BALL.getId())))
                .with(8, Optional.of(MoveRef.spell(MushoSpells.HEALING.getId())));
        Tag encoded = MoveLoadout.CODEC.encodeStart(NbtOps.INSTANCE, original).getOrThrow();
        MoveLoadout decoded = MoveLoadout.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow();
        helper.assertValueEqual(decoded, original, "loadout after a save and load");
        helper.succeed();
    }
}
