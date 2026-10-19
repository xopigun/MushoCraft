package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.core.MushoBlocks;
import fr.xopi.mushocraft.life.LifeService;
import fr.xopi.mushocraft.magic.entity.PillarKind;
import fr.xopi.mushocraft.magic.entity.SpellPillar;
import fr.xopi.mushocraft.magic.entity.SpellProjectile;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/**
 * The spells added from the wiki's full list: Icicle Break, Icicle Edge, Ice Lance, Ice Fortress, Absolute Zero,
 * Flamethrower, Wind Blast, Electric and Antidote.
 */
@GameTestHolder(MushoCraft.MODID)
@PrefixGameTestTemplate(false)
public final class CatalogueSpellGameTests {
    private static final String EMPTY = "empty";
    private static final BlockPos MIDDLE = new BlockPos(4, 1, 4);

    private CatalogueSpellGameTests() {
    }

    /** A grown caster at the middle of the plot, looking south. */
    private static ServerPlayer caster(GameTestHelper helper, float pitch) {
        ServerPlayer player = TestPlayers.join(helper);
        player.setGameMode(GameType.SURVIVAL);
        Vec3 spot = helper.absoluteVec(Vec3.atBottomCenterOf(MIDDLE));
        player.moveTo(spot.x, spot.y, spot.z, 0.0F, pitch);
        LifeService.setAge(player, 20.0);
        return player;
    }

    private static boolean cast(ServerPlayer player, Spell spell) {
        return spell.effect().cast(new SpellContext(player, spell, 1.0));
    }

    private static Husk dummy(GameTestHelper helper, BlockPos pos) {
        Husk husk = helper.spawn(EntityType.HUSK, pos);
        husk.setNoAi(true);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            husk.setItemSlot(slot, ItemStack.EMPTY);
        }
        return husk;
    }

    private static <T extends Entity> List<T> around(GameTestHelper helper, ServerPlayer player, Class<T> type) {
        return helper.getLevel().getEntitiesOfClass(type, player.getBoundingBox().inflate(24.0));
    }

    /** Only this caster's zones: the tests next door have their own. */
    private static void clearZones(GameTestHelper helper, ServerPlayer player) {
        helper.getLevel().getEntitiesOfClass(SpellZone.class, player.getBoundingBox().inflate(30.0), zone -> zone.caster() == player)
                .forEach(SpellZone::discard);
    }

    /** The pillars of one kind around the caster: no other test raises ice lances or ice fortresses. */
    private static void clearPillars(GameTestHelper helper, ServerPlayer player, PillarKind kind) {
        around(helper, player, SpellPillar.class).stream().filter(pillar -> pillar.kind() == kind).forEach(SpellPillar::discard);
    }

    @GameTest(template = EMPTY)
    public static void icicleBreakFiresAVolley(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 0.0F);
        helper.assertTrue(cast(player, MushoSpells.ICICLE_BREAK.get()), "cast");
        long bolts = around(helper, player, SpellProjectile.class).stream().filter(bolt -> bolt.getOwner() == player).count();
        helper.assertValueEqual(bolts, 6L, "ice bolts in the barrage");
        around(helper, player, SpellProjectile.class).forEach(SpellProjectile::discard);
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void iceLanceImpalesFromBelow(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 15.0F);
        Husk husk = dummy(helper, MIDDLE.south(3));
        player.lookAt(EntityAnchorArgument.Anchor.EYES, husk.position());
        helper.assertTrue(cast(player, MushoSpells.ICE_LANCE.get()), "cast");
        helper.assertTrue(husk.getHealth() < husk.getMaxHealth(), "the spike impales the foe");
        helper.assertTrue(around(helper, player, SpellPillar.class).stream().anyMatch(pillar -> pillar.kind() == PillarKind.ICE_LANCE),
                "an ice spike stands where it struck");
        clearPillars(helper, player, PillarKind.ICE_LANCE);
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void iceFortressRaisesARampartAhead(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 0.0F);
        helper.assertTrue(cast(player, MushoSpells.ICE_FORTRESS.get()), "cast");
        helper.assertTrue(around(helper, player, SpellPillar.class).stream().anyMatch(pillar -> pillar.kind() == PillarKind.ICE_FORTRESS),
                "a rampart of ice rises");
        boolean iceWall = false;
        for (BlockPos pos : BlockPos.betweenClosed(player.blockPosition().offset(-3, 0, -3), player.blockPosition().offset(3, 3, 3))) {
            iceWall |= helper.getLevel().getBlockState(pos).is(MushoBlocks.CONJURED_ICE.get());
        }
        helper.assertTrue(iceWall, "they hold: conjured ice under the model");
        clearPillars(helper, player, PillarKind.ICE_FORTRESS);
        TestPlayers.leave(player);
        helper.succeed();
    }

    /** In a batch of its own: the field reaches far beyond the plot, into the tests that would run beside it. */
    @GameTest(template = EMPTY, batch = "absolute_zero")
    public static void absoluteZeroFreezesAllAround(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 0.0F);
        Husk ahead = dummy(helper, MIDDLE.south(3));
        Husk behind = dummy(helper, MIDDLE.north(3));
        BlockPos pond = MIDDLE.east(3);
        helper.setBlock(pond, Blocks.WATER);
        helper.assertTrue(cast(player, MushoSpells.ABSOLUTE_ZERO.get()), "cast");
        helper.assertTrue(ahead.getTicksFrozen() > 0 && behind.getTicksFrozen() > 0, "everything around freezes, ahead and behind");
        helper.assertTrue(player.getTicksFrozen() == 0, "but the caster");
        helper.assertBlockPresent(Blocks.FROSTED_ICE, pond);
        clearPillars(helper, player, PillarKind.FROST_MOUND);
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void flamethrowerBurnsWhatIsAhead(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 10.0F);
        Husk inFront = dummy(helper, MIDDLE.south(3));
        Husk aside = dummy(helper, MIDDLE.east(3));
        player.lookAt(EntityAnchorArgument.Anchor.EYES, inFront.getEyePosition());
        helper.assertTrue(cast(player, MushoSpells.FLAMETHROWER.get()), "the jet starts pouring");
        helper.runAfterDelay(4, () -> {
            helper.assertTrue(inFront.isOnFire() && inFront.getHealth() < inFront.getMaxHealth(), "the jet burns what is ahead");
            helper.assertFalse(aside.isOnFire(), "not what is beside");
            clearZones(helper, player);
            TestPlayers.leave(player);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void windBlastPushesWithoutHarm(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 0.0F);
        Husk husk = dummy(helper, MIDDLE.south(2));
        player.lookAt(EntityAnchorArgument.Anchor.EYES, husk.getEyePosition());
        helper.assertTrue(cast(player, MushoSpells.WIND_BLAST.get()), "cast");
        helper.assertValueEqual(husk.getHealth(), husk.getMaxHealth(), "a gust does not hurt");
        helper.assertTrue(husk.getDeltaMovement().z > 0.5, "it pushes away (" + husk.getDeltaMovement() + ")");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void electricShocksThroughArmourAndParalyses(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 15.0F);
        Husk knight = dummy(helper, MIDDLE.south(3));
        knight.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
        knight.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
        knight.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
        knight.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
        player.lookAt(EntityAnchorArgument.Anchor.EYES, knight.getEyePosition());
        float health = knight.getHealth();
        helper.assertTrue(cast(player, MushoSpells.ELECTRIC.get()), "a small cloud gathers over the target");
        helper.runAfterDelay(6, () -> helper.assertTrue(knight.getHealth() == health, "nothing strikes while it gathers"));
        helper.succeedWhen(() -> {
            // 14 damage at power 1: diamond armour would take more than half of it; electricity goes through
            helper.assertTrue(knight.getHealth() <= health - 13.5F, "the shock goes through the armour (" + knight.getHealth() + ")");
            MobEffectInstance slowed = knight.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
            helper.assertTrue(slowed != null && slowed.getAmplifier() >= 9, "and paralyses");
            clearZones(helper, player);
            TestPlayers.leave(player);
        });
    }

    @GameTest(template = EMPTY)
    public static void antidoteCuresWhatDetoxificationCannot(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 0.0F);
        player.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0));
        cast(player, MushoSpells.DETOXIFICATION.get());
        helper.assertTrue(player.hasEffect(MobEffects.WITHER), "Detoxification cannot cure the wither");
        cast(player, MushoSpells.ANTIDOTE.get());
        helper.assertFalse(player.hasEffect(MobEffects.WITHER), "Antidote can");
        TestPlayers.leave(player);
        helper.succeed();
    }
}
