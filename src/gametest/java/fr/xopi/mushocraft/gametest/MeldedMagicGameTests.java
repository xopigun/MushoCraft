package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.combat.mastery.MasteryRules;
import fr.xopi.mushocraft.combat.mastery.MasteryService;
import fr.xopi.mushocraft.life.LifeService;
import fr.xopi.mushocraft.magic.entity.SpellPillar;
import fr.xopi.mushocraft.magic.entity.SpellZone;
import fr.xopi.mushocraft.magic.spell.MagicService;
import fr.xopi.mushocraft.magic.spell.MushoSpells;
import fr.xopi.mushocraft.magic.spell.Spell;
import fr.xopi.mushocraft.magic.spell.SpellContext;
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

import java.util.function.Supplier;

/**
 * Mixed magic: discovered by mastering every part, never by merely knowing them; and what each melded spell does.
 */
@GameTestHolder(MushoCraft.MODID)
@PrefixGameTestTemplate(false)
public final class MeldedMagicGameTests {
    private static final String EMPTY = "empty";
    private static final BlockPos MIDDLE = new BlockPos(4, 1, 4);

    private MeldedMagicGameTests() {
    }

    private static ServerPlayer caster(GameTestHelper helper, float pitch) {
        ServerPlayer player = TestPlayers.join(helper);
        player.setGameMode(GameType.SURVIVAL);
        Vec3 spot = helper.absoluteVec(Vec3.atBottomCenterOf(MIDDLE));
        player.moveTo(spot.x, spot.y, spot.z, 0.0F, pitch);
        LifeService.setAge(player, 20.0);
        return player;
    }

    private static void master(ServerPlayer player, Supplier<Spell> spell) {
        MagicService.learn(player, spell.get());
        MasteryService.setRepetitions(player, spell.get().ref(), MasteryRules.REPETITIONS_TO_MASTER);
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
        helper.getLevel().getEntitiesOfClass(SpellZone.class, player.getBoundingBox().inflate(30.0), zone -> zone.caster() == player)
                .forEach(SpellZone::discard);
    }

    @GameTest(template = EMPTY)
    public static void masteringAWaterAndAnEarthSpellRevealsQuagmire(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 0.0F);
        master(player, MushoSpells.WATER_BALL);
        helper.assertFalse(MagicService.knows(player, MushoSpells.QUAGMIRE.get()), "one school is not enough");
        master(player, MushoSpells.EARTH_WALL);
        helper.assertTrue(MagicService.knows(player, MushoSpells.QUAGMIRE.get()), "water + earth mastered: Quagmire is found");
        helper.assertTrue(MagicService.knows(player, MushoSpells.MUD_RAIN.get()), "and Mud Rain, of the same parts");
        helper.assertFalse(MagicService.knows(player, MushoSpells.DEEP_MIST.get()), "Deep Mist needs its own three spells");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void knowingThePartsIsNotEnough(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 0.0F);
        MagicService.learn(player, MushoSpells.WATER_SPLASH.get());
        MagicService.learn(player, MushoSpells.ICICLE_FIELD.get());
        MasteryService.setRepetitions(player, MushoSpells.WATER_SPLASH.get().ref(), MasteryRules.REPETITIONS_TO_MASTER - 1);
        MasteryService.setRepetitions(player, MushoSpells.ICICLE_FIELD.get().ref(), MasteryRules.REPETITIONS_TO_MASTER);
        helper.assertFalse(MagicService.knows(player, MushoSpells.FROST_NOVA.get()), "Water Splash is not mastered yet");
        MasteryService.succeeded(player, MushoSpells.WATER_SPLASH.get().ref());
        helper.assertTrue(MagicService.knows(player, MushoSpells.FROST_NOVA.get()), "one more repetition, and Frost Nova is found");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void boraBlastWaitsForBlastToBeMastered(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 0.0F);
        master(player, MushoSpells.HEAT_HAND);
        master(player, MushoSpells.WIND_BIND);
        master(player, MushoSpells.ICICLE_FIELD);
        helper.assertTrue(MagicService.knows(player, MushoSpells.BLAST.get()), "fire + wind: Blast");
        helper.assertFalse(MagicService.knows(player, MushoSpells.BORA_BLAST.get()), "Blast is only just known");
        MasteryService.setRepetitions(player, MushoSpells.BLAST.get().ref(), MasteryRules.REPETITIONS_TO_MASTER);
        helper.assertTrue(MagicService.knows(player, MushoSpells.BORA_BLAST.get()), "Blast mastered + Icicle Field: Bora Blast");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void quagmireHoldsFast(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 30.0F);
        Husk stuck = dummy(helper, MIDDLE.south(3));
        cast(player, MushoSpells.QUAGMIRE.get());
        helper.succeedWhen(() -> {
            MobEffectInstance slowness = stuck.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
            helper.assertTrue(slowness != null && slowness.getAmplifier() >= 5, "the mud holds the foe fast");
            clearZones(helper, player);
            TestPlayers.leave(player);
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void deepMistHidesTheCasterFromItsHunters(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 0.0F);
        Husk hunter = dummy(helper, MIDDLE.south(4));
        hunter.setTarget(player);
        cast(player, MushoSpells.DEEP_MIST.get());
        helper.succeedWhen(() -> {
            helper.assertTrue(hunter.getTarget() == null, "the hunter loses its prey in the mist");
            helper.assertTrue(hunter.hasEffect(MobEffects.BLINDNESS), "and cannot see inside it");
            clearZones(helper, player);
            TestPlayers.leave(player);
        });
    }

    @GameTest(template = EMPTY)
    public static void frostNovaSoaksThenFreezesThrough(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 30.0F);
        Husk target = dummy(helper, MIDDLE.south(3));
        cast(player, MushoSpells.FROST_NOVA.get());
        // soaked by the first part, the second freezes twice as long
        helper.assertTrue(target.getTicksFrozen() >= target.getTicksRequiredToFreeze() + 280,
                "frozen through: " + target.getTicksFrozen());
        helper.getLevel().getEntitiesOfClass(SpellPillar.class, player.getBoundingBox().inflate(25.0)).forEach(SpellPillar::discard);
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void blastScorchesAndBoraBlastFreezes(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 0.0F);
        Husk scorched = dummy(helper, MIDDLE.south(3));
        cast(player, MushoSpells.BLAST.get());
        helper.assertTrue(scorched.isOnFire(), "the hot wind sets alight");
        scorched.discard();
        Husk frozen = dummy(helper, MIDDLE.south(3));
        cast(player, MushoSpells.BORA_BLAST.get());
        helper.assertTrue(frozen.getTicksFrozen() > frozen.getTicksRequiredToFreeze(), "the icy gust freezes");
        helper.assertTrue(frozen.hasEffect(MobEffects.DIG_SLOWDOWN), "and numbs the arms");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void magmaGushLeavesTheGroundMolten(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 30.0F);
        Husk target = dummy(helper, MIDDLE.south(3));
        cast(player, MushoSpells.MAGMA_GUSH.get());
        helper.succeedWhen(() -> {
            helper.assertTrue(target.isOnFire(), "the lava sets alight");
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(SpellZone.class, player.getBoundingBox().inflate(25.0),
                    zone -> zone.caster() == player).size() == 1, "and the ground stays molten");
            clearZones(helper, player);
            TestPlayers.leave(player);
        });
    }
}
