package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.core.MushoDamageTypes;
import fr.xopi.mushocraft.core.MushoEntities;
import fr.xopi.mushocraft.magic.entity.SpellProjectile;
import fr.xopi.mushocraft.magic.spell.ChantBroadcast;
import fr.xopi.mushocraft.magic.spell.SpellElement;
import fr.xopi.mushocraft.magic.spell.effect.ProjectileProfile;
import fr.xopi.mushocraft.monster.Wraith;
import fr.xopi.mushocraft.monster.ai.RaiseTheDeadGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Skeleton;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Wraiths (wiki, page Monsters): impervious to ordinary blows, burnt away for a while by fire and magic, ended for good
 * only by Divine Strike; they raise skeletons, and the Deadly Wraith casts attack magic.
 */
@GameTestHolder(MushoCraft.MODID)
@PrefixGameTestTemplate(false)
public final class WraithGameTests {
    private static final String EMPTY = "empty";

    private WraithGameTests() {
    }

    private static Husk dummy(GameTestHelper helper, BlockPos pos) {
        Husk husk = helper.spawn(EntityType.HUSK, pos);
        husk.setNoAi(true);
        return husk;
    }

    @GameTest(template = EMPTY)
    public static void ordinaryBlowsPassThroughAWraith(GameTestHelper helper) {
        Wraith wraith = helper.spawn(MushoEntities.WRAITH.get(), new BlockPos(2, 1, 2));
        Husk husk = dummy(helper, new BlockPos(4, 1, 4));
        helper.assertFalse(wraith.hurt(wraith.damageSources().mobAttack(husk), 8.0F), "a sword passes through it");
        helper.assertTrue(wraith.getHealth() == wraith.getMaxHealth(), "untouched");
        wraith.discard();
        husk.discard();
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void fireAndMagicOnlyBurnItAwayForAWhile(GameTestHelper helper) {
        Wraith wraith = helper.spawn(MushoEntities.WRAITH.get(), new BlockPos(2, 1, 2));
        Husk husk = dummy(helper, new BlockPos(4, 1, 4));
        wraith.hurt(MushoDamageTypes.spell(helper.getLevel(), husk, husk), 4.0F);
        helper.assertTrue(wraith.getHealth() < wraith.getMaxHealth(), "magic hurts it");
        wraith.hurt(MushoDamageTypes.spell(helper.getLevel(), husk, husk), 999.0F);
        helper.assertTrue(wraith.isAlive() && wraith.isReforming() && wraith.isInvisible(), "burnt away, it forms again");
        helper.assertFalse(wraith.hurt(MushoDamageTypes.spell(helper.getLevel(), husk, husk), 999.0F), "out of reach while it forms");
        wraith.discard();
        husk.discard();
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void divineStrikeEndsAWraithForGood(GameTestHelper helper) {
        Wraith wraith = helper.spawn(MushoEntities.WRAITH.get(), new BlockPos(2, 1, 2));
        Wraith deadly = helper.spawn(MushoEntities.WRAITH.get(), new BlockPos(5, 1, 5));
        deadly.setDeadly(true);
        Husk caster = dummy(helper, new BlockPos(1, 1, 5));
        SpellProjectile light = SpellProjectile.create(helper.getLevel(), caster, ProjectileProfile.of(SpellElement.LIGHT, 3.0F).build());
        wraith.hurt(MushoDamageTypes.spell(helper.getLevel(), light, caster), 999.0F);
        helper.assertFalse(wraith.isAlive(), "Divine Strike ends it");
        deadly.hurt(MushoDamageTypes.spell(helper.getLevel(), light, caster), 999.0F);
        helper.assertTrue(deadly.isAlive() && deadly.isReforming(), "a beginner's Divine Strike only burns a Deadly Wraith away");
        deadly.discard();
        caster.discard();
        light.discard();
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 120)
    public static void aWraithRaisesSkeletonsAgainstItsPrey(GameTestHelper helper) {
        Wraith wraith = helper.spawn(MushoEntities.WRAITH.get(), new BlockPos(2, 1, 2));
        Husk husk = dummy(helper, new BlockPos(6, 1, 6));
        wraith.setTarget(husk);
        helper.succeedWhen(() -> {
            helper.assertTrue(RaiseTheDeadGoal.bound(wraith).size() == RaiseTheDeadGoal.army(wraith), "its skeletons have risen");
            helper.assertTrue(RaiseTheDeadGoal.bound(wraith).stream().allMatch(s -> s.getTarget() == husk), "against its prey");
            RaiseTheDeadGoal.bound(wraith).forEach(Skeleton::discard);
            wraith.discard();
            husk.discard();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void aDeadlyWraithCastsAttackMagic(GameTestHelper helper) {
        Wraith wraith = helper.spawn(MushoEntities.WRAITH.get(), new BlockPos(1, 1, 1));
        wraith.setDeadly(true);
        Husk husk = dummy(helper, new BlockPos(7, 1, 7));
        wraith.setTarget(husk);
        helper.succeedWhen(() -> {
            helper.assertTrue(ChantBroadcast.isChanting(wraith)
                    || !helper.getLevel().getEntitiesOfClass(SpellProjectile.class, wraith.getBoundingBox().inflate(16.0)).isEmpty(),
                    "a spell is chanted or flying");
            RaiseTheDeadGoal.bound(wraith).forEach(Skeleton::discard);
            wraith.discard();
            husk.discard();
        });
    }
}
