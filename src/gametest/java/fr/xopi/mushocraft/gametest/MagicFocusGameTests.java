package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.core.MushoEntities;
import fr.xopi.mushocraft.core.MushoItems;
import fr.xopi.mushocraft.core.Rank;
import fr.xopi.mushocraft.magic.entity.SpellProjectile;
import fr.xopi.mushocraft.magic.focus.MagicFocus;
import fr.xopi.mushocraft.magic.spell.MushoSpells;
import fr.xopi.mushocraft.magic.spell.Spell;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellSchool;
import fr.xopi.mushocraft.npc.MagicTutorEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Wands and staffs: never needed to cast, they strengthen and enlarge spells.
 */
@GameTestHolder(MushoCraft.MODID)
@PrefixGameTestTemplate(false)
public final class MagicFocusGameTests {
    private static final String EMPTY = "empty";

    private MagicFocusGameTests() {
    }

    private static ServerPlayer caster(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.join(helper);
        Vec3 spot = helper.absoluteVec(new Vec3(1.5, 1.0, 1.5));
        player.moveTo(spot.x, spot.y, spot.z, 0.0F, 0.0F);
        return player;
    }

    /** Casts a water ball as the player and returns the size of the ball that took shape. */
    private static float waterBallSize(GameTestHelper helper, ServerPlayer player) {
        Spell waterBall = MushoSpells.WATER_BALL.get();
        waterBall.effect().cast(new SpellContext(player, waterBall, 1.0));
        SpellProjectile ball = helper.getLevel().getEntitiesOfClass(SpellProjectile.class, player.getBoundingBox().inflate(4.0)).getFirst();
        float size = ball.visualSize();
        ball.discard();
        return size;
    }

    @GameTest(template = EMPTY)
    public static void magicNeedsNoWand(GameTestHelper helper) {
        ServerPlayer player = caster(helper);
        TestPlayers.expectClose(helper, MagicFocus.multiplier(player, SpellSchool.WATER), 1.0, "empty hands: plain power");
        helper.assertTrue(waterBallSize(helper, player) > 0.0F, "and the spell still takes shape");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void aStaffMakesTheWaterBallStrongerAndBigger(GameTestHelper helper) {
        ServerPlayer player = caster(helper);
        float plain = waterBallSize(helper, player);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(MushoItems.MAGE_STAFF.get()));
        TestPlayers.expectClose(helper, MagicFocus.multiplier(player, SpellSchool.WATER), 1.2, "a staff: +20%");
        TestPlayers.expectClose(helper, waterBallSize(helper, player), plain * 1.2, "the water ball is 20% larger");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void aquaHeartiaFavoursWaterMagic(GameTestHelper helper) {
        ServerPlayer player = caster(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(MushoItems.AQUA_HEARTIA.get()));
        TestPlayers.expectClose(helper, MagicFocus.multiplier(player, SpellSchool.WATER), 1.4, "water above all");
        TestPlayers.expectClose(helper, MagicFocus.multiplier(player, SpellSchool.FIRE), 1.15, "the other schools less");
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(MushoItems.APPRENTICE_WAND.get()));
        TestPlayers.expectClose(helper, MagicFocus.multiplier(player, SpellSchool.WATER), 1.4, "only the best focus counts");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void tutorsCarryAFocusMatchingTheirRank(GameTestHelper helper) {
        MagicTutorEntity tutor = helper.spawn(MushoEntities.MAGIC_TUTOR.get(), new BlockPos(4, 1, 4));
        tutor.setRank(Rank.INTERMEDIATE);
        helper.assertTrue(tutor.getMainHandItem().is(MushoItems.APPRENTICE_WAND.get()), "an Intermediate holds a wand");
        tutor.setRank(Rank.ADVANCED);
        helper.assertTrue(tutor.getMainHandItem().is(MushoItems.MAGE_STAFF.get()), "an Advanced tutor holds a staff");
        tutor.discard();
        helper.succeed();
    }
}
