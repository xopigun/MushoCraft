package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.core.MushoBlocks;
import fr.xopi.mushocraft.core.MushoMobEffects;
import fr.xopi.mushocraft.life.LifeService;
import fr.xopi.mushocraft.magic.entity.SpellZone;
import fr.xopi.mushocraft.magic.spell.MushoSpells;
import fr.xopi.mushocraft.magic.spell.Spell;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The spells the wiki gives no rank: water that soaks, ice that bites the wet harder, fire, wind and sand.
 */
@GameTestHolder(MushoCraft.MODID)
@PrefixGameTestTemplate(false)
public final class ElementalSpellGameTests {
    private static final String EMPTY = "empty";
    private static final BlockPos MIDDLE = new BlockPos(4, 1, 4);

    private ElementalSpellGameTests() {
    }

    private static ServerPlayer caster(GameTestHelper helper, float pitch) {
        ServerPlayer player = TestPlayers.join(helper);
        player.setGameMode(GameType.SURVIVAL);
        Vec3 spot = helper.absoluteVec(Vec3.atBottomCenterOf(MIDDLE));
        player.moveTo(spot.x, spot.y, spot.z, 0.0F, pitch);
        LifeService.setAge(player, 20.0);
        return player;
    }

    private static void cast(ServerPlayer player, Spell spell) {
        spell.effect().cast(new SpellContext(player, spell, 1.0));
    }

    private static Husk dummy(GameTestHelper helper, BlockPos pos) {
        Husk husk = helper.spawn(EntityType.HUSK, pos);
        husk.setNoAi(true);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            husk.setItemSlot(slot, ItemStack.EMPTY);
        }
        return husk;
    }

    @GameTest(template = EMPTY)
    public static void iceBitesTheSoakedHarder(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 60.0F);
        Husk dry = dummy(helper, MIDDLE.south(2).east(2));
        Husk wet = dummy(helper, MIDDLE.south(2).west(2));
        wet.addEffect(new MobEffectInstance(MushoMobEffects.SOAKED, 200, 0));
        cast(player, MushoSpells.ICICLE_FIELD.get());
        float dryLoss = dry.getMaxHealth() - dry.getHealth();
        float wetLoss = wet.getMaxHealth() - wet.getHealth();
        helper.assertTrue(dryLoss > 0.0F && wetLoss > dryLoss * 1.5F, "the soaked freeze harder (" + wetLoss + " vs " + dryLoss + ")");
        helper.assertTrue(wet.getTicksFrozen() > dry.getTicksFrozen(), "and stay frozen longer");
        helper.assertFalse(wet.hasEffect(MushoMobEffects.SOAKED), "their water turned to ice");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void waterSplashSoaksEveryone(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 60.0F);
        Husk husk = dummy(helper, MIDDLE.south(3));
        husk.igniteForSeconds(5.0F);
        cast(player, MushoSpells.WATER_SPLASH.get());
        helper.assertTrue(husk.hasEffect(MushoMobEffects.SOAKED), "soaked");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void heatHandSetsFoesAlight(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 0.0F);
        Husk husk = dummy(helper, MIDDLE.south(1));
        cast(player, MushoSpells.HEAT_HAND.get());
        helper.assertTrue(player.hasEffect(MushoMobEffects.HEATED_HANDS), "hot hands");
        player.attack(husk);
        helper.assertTrue(husk.isOnFire(), "the blow sets the foe alight");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void windBindTiesTheFoeDown(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 0.0F);
        Husk husk = dummy(helper, MIDDLE.south(3));
        player.lookAt(EntityAnchorArgument.Anchor.EYES, husk.getEyePosition());
        cast(player, MushoSpells.WIND_BIND.get());
        helper.succeedWhen(() -> {
            var slow = husk.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
            helper.assertTrue(slow != null && slow.getAmplifier() >= 9, "held fast by the wind");
            TestPlayers.leave(player);
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 80)
    public static void sandCloudBlindsAroundItsImpact(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 0.0F);
        Husk target = dummy(helper, MIDDLE.south(3));
        Husk beside = dummy(helper, MIDDLE.south(3).east(2));
        player.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
        cast(player, MushoSpells.SAND_CLOUD.get());
        helper.succeedWhen(() -> {
            helper.assertTrue(target.hasEffect(MobEffects.BLINDNESS) && beside.hasEffect(MobEffects.BLINDNESS),
                    "the dust blinds all around the impact");
            TestPlayers.leave(player);
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void burningPlaceEngulfsTheClose(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 0.0F);
        Husk close = dummy(helper, MIDDLE.south(2));
        cast(player, MushoSpells.BURNING_PLACE.get());
        helper.succeedWhen(() -> {
            helper.assertTrue(close.isOnFire(), "the wreath of fire engulfs whoever is close");
            helper.assertFalse(player.isOnFire(), "but spares its caster");
            helper.getLevel().getEntitiesOfClass(SpellZone.class, player.getBoundingBox().inflate(10.0), zone -> zone.caster() == player)
                    .forEach(SpellZone::discard);
            TestPlayers.leave(player);
        });
    }

    @GameTest(template = EMPTY)
    public static void earthWallRisesAhead(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 0.0F);
        cast(player, MushoSpells.EARTH_WALL.get());
        helper.assertBlockPresent(MushoBlocks.CONJURED_EARTH.get(), MIDDLE.south(3));
        helper.assertBlockPresent(MushoBlocks.CONJURED_EARTH.get(), MIDDLE.south(3).east(2).above(2));
        TestPlayers.leave(player);
        helper.succeed();
    }
}
