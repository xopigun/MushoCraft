package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.life.LifeService;
import fr.xopi.mushocraft.magic.entity.SpellZone;
import fr.xopi.mushocraft.magic.spell.MushoSpells;
import fr.xopi.mushocraft.magic.spell.Spell;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(MushoCraft.MODID)
@PrefixGameTestTemplate(false)
public final class SaintSpellGameTests {
    private static final String EMPTY = "empty";
    private static final BlockPos MIDDLE = new BlockPos(4, 1, 4);

    private SaintSpellGameTests() {
    }

    /** A caster in the middle of the area, looking down at the ground just ahead (south). */
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

    private static void clearZones(GameTestHelper helper, ServerPlayer player) {
        // Only this test's zones: neighbouring tests run at the same time.
        helper.getLevel().getEntitiesOfClass(SpellZone.class, player.getBoundingBox().inflate(40.0), zone -> zone.caster() == player)
                .forEach(SpellZone::discard);
    }

    @GameTest(template = EMPTY, timeoutTicks = 160)
    public static void cumulonimbusStrikesWithLightning(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 60.0F);
        Husk husk = dummy(helper, MIDDLE.south(2));
        float before = husk.getHealth();
        cast(player, MushoSpells.CUMULONIMBUS.get());
        helper.assertFalse(helper.getLevel().getEntitiesOfClass(SpellZone.class, player.getBoundingBox().inflate(20.0)).isEmpty(),
                "the storm takes shape");
        helper.succeedWhen(() -> {
            helper.assertTrue(husk.getHealth() < before || !husk.isAlive(), "lightning strikes inside the storm");
            clearZones(helper, player);
            helper.getLevel().setWeatherParameters(6000, 0, false, false);
            TestPlayers.leave(player);
        });
    }

    @GameTest(template = EMPTY)
    public static void flashOverSetsTheWholeAreaAblaze(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 60.0F);
        Husk near = dummy(helper, MIDDLE.south(2).east(3));
        Husk far = dummy(helper, MIDDLE.south(2).west(3));
        cast(player, MushoSpells.FLASH_OVER.get());
        helper.assertTrue(near.isOnFire() && far.isOnFire(), "everyone in the area burns");
        helper.assertTrue(near.getHealth() < near.getMaxHealth(), "and is hurt");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void violentStormBlowsAllAround(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 0.0F);
        Husk front = dummy(helper, MIDDLE.south(3));
        Husk behind = dummy(helper, MIDDLE.north(3));
        cast(player, MushoSpells.VIOLENT_STORM.get());
        helper.assertTrue(front.getHealth() < front.getMaxHealth() && behind.getHealth() < behind.getMaxHealth(),
                "the blast reaches every side");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 80)
    public static void tornadoBattersThoseCaughtInside(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 60.0F);
        Husk husk = dummy(helper, MIDDLE.south(3));
        float before = husk.getHealth();
        cast(player, MushoSpells.TORNADO.get());
        helper.succeedWhen(() -> {
            helper.assertTrue(husk.getHealth() < before, "battered by the whirlwind");
            clearZones(helper, player);
            TestPlayers.leave(player);
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void spiritSealingBarrierHoldsTheUndead(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 60.0F);
        Husk husk = dummy(helper, MIDDLE.south(2));
        cast(player, MushoSpells.SPIRIT_SEALING_BARRIER.get());
        helper.succeedWhen(() -> {
            helper.assertTrue(husk.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "the undead is held fast");
            clearZones(helper, player);
            TestPlayers.leave(player);
        });
    }

    @GameTest(template = EMPTY)
    public static void sandstormBlanketsAWideArea(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 60.0F);
        cast(player, MushoSpells.SANDSTORM.get());
        var clouds = helper.getLevel().getEntitiesOfClass(AreaEffectCloud.class, player.getBoundingBox().inflate(30.0));
        helper.assertTrue(clouds.size() == 1 && clouds.getFirst().getRadius() >= 8.9F, "a wide storm of sand");
        clouds.forEach(AreaEffectCloud::discard);
        TestPlayers.leave(player);
        helper.succeed();
    }
}
