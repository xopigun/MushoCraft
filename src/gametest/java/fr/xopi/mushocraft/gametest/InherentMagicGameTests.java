package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.combat.mastery.MasteryCurve;
import fr.xopi.mushocraft.combat.mastery.MasteryService;
import fr.xopi.mushocraft.core.MushoEntities;
import fr.xopi.mushocraft.core.MushoMobEffects;
import fr.xopi.mushocraft.core.Rank;
import fr.xopi.mushocraft.life.LifeService;
import fr.xopi.mushocraft.magic.mana.ManaService;
import fr.xopi.mushocraft.magic.spell.ChantBroadcast;
import fr.xopi.mushocraft.magic.spell.MagicProficiency;
import fr.xopi.mushocraft.magic.spell.MagicService;
import fr.xopi.mushocraft.magic.spell.MushoSpells;
import fr.xopi.mushocraft.magic.spell.Spell;
import fr.xopi.mushocraft.magic.spell.SpellCasting;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellSchool;
import fr.xopi.mushocraft.npc.MagicTutorEntity;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
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
 * Inherent magic (wiki, page Magic § Inherent Magic): arts no incantation reproduces, taught by the rare masters who
 * know them and mastered by long training. Disturb Magic and Howling.
 */
@GameTestHolder(MushoCraft.MODID)
@PrefixGameTestTemplate(false)
public final class InherentMagicGameTests {
    private static final String EMPTY = "empty";
    private static final BlockPos MIDDLE = new BlockPos(4, 1, 4);

    private InherentMagicGameTests() {
    }

    private static ServerPlayer player(GameTestHelper helper, BlockPos pos, float yaw) {
        ServerPlayer player = TestPlayers.join(helper);
        player.setGameMode(GameType.SURVIVAL);
        Vec3 spot = helper.absoluteVec(Vec3.atBottomCenterOf(pos));
        player.moveTo(spot.x, spot.y, spot.z, yaw, 0.0F);
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

    @GameTest(template = EMPTY)
    public static void inherentMagicTakesLongTraining(GameTestHelper helper) {
        Spell disturb = MushoSpells.DISTURB_MAGIC.get();
        helper.assertValueEqual(disturb.masteryCurve(), MasteryCurve.LONG_TRAINING, "Disturb Magic is mastered by long training");
        helper.assertValueEqual(disturb.incantationTicks(), 0, "and released with no chant");
        MasteryCurve curve = MasteryCurve.LONG_TRAINING;
        helper.assertTrue(curve.failureChance(0.0) > MasteryCurve.STANDARD.failureChance(0.0),
                "at first it fails far more often than a spell");
        helper.assertFalse(curve.isMastered(MasteryCurve.STANDARD.repetitionsToMaster()),
                "and what masters a spell is far from enough");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void disturbMagicScattersASpellTakingShape(GameTestHelper helper) {
        ServerPlayer disturber = player(helper, MIDDLE, 0.0F);
        Husk chanter = dummy(helper, MIDDLE.south(3));
        ChantBroadcast.started(chanter, MushoSpells.WATER_CANNON.get(), 40);
        disturber.lookAt(EntityAnchorArgument.Anchor.EYES, chanter.getEyePosition());
        helper.assertTrue(cast(disturber, MushoSpells.DISTURB_MAGIC.get()), "interfering mana reaches the forming spell");
        helper.assertTrue(chanter.hasEffect(MushoMobEffects.DISTURBED), "and scatters its mana");
        ChantBroadcast.stopped(chanter);
        TestPlayers.leave(disturber);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void scatteredManaBreaksTheChant(GameTestHelper helper) {
        ServerPlayer chanter = player(helper, MIDDLE, 0.0F);
        Spell cannon = MushoSpells.WATER_CANNON.get();
        MagicService.learn(chanter, cannon);
        ManaService.setTrained(chanter, 5000.0);
        ManaService.refill(chanter);
        helper.assertTrue(SpellCasting.begin(chanter, cannon).success() && SpellCasting.chanting(chanter).isPresent(),
                "a spell takes shape");
        chanter.addEffect(new MobEffectInstance(MushoMobEffects.DISTURBED, 10, 0));
        SpellCasting.tick(chanter);
        helper.assertTrue(SpellCasting.chanting(chanter).isEmpty(), "the spell scatters before taking shape");
        helper.assertFalse(SpellCasting.begin(chanter, cannon).success(), "nothing takes shape until the mana settles");
        SpellCasting.clear(chanter);
        TestPlayers.leave(chanter);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void disturbMagicUndoesShields(GameTestHelper helper) {
        ServerPlayer disturber = player(helper, MIDDLE, 0.0F);
        Husk warded = dummy(helper, MIDDLE.south(3));
        warded.addEffect(new MobEffectInstance(MushoMobEffects.MAGIC_SHIELD, 300, 0));
        disturber.lookAt(EntityAnchorArgument.Anchor.EYES, warded.getEyePosition());
        cast(disturber, MushoSpells.DISTURB_MAGIC.get());
        helper.assertFalse(warded.hasEffect(MushoMobEffects.MAGIC_SHIELD), "the mana holding the shield scatters");
        TestPlayers.leave(disturber);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void disturbMagicCanBePractisedAlone(GameTestHelper helper) {
        ServerPlayer player = player(helper, MIDDLE, 0.0F);
        helper.assertTrue(cast(player, MushoSpells.DISTURB_MAGIC.get()), "no one to disturb: one practises on one's own mana");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void howlingStunsThoseCloseAround(GameTestHelper helper) {
        ServerPlayer player = player(helper, MIDDLE, 0.0F);
        Husk near = dummy(helper, MIDDLE.south(2));
        Husk behind = dummy(helper, MIDDLE.north(3));
        Husk far = dummy(helper, MIDDLE.east(4).south(4));
        helper.assertTrue(cast(player, MushoSpells.HOWLING.get()), "cast");
        helper.assertTrue(near.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && behind.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
                "whoever hears it, all around, is stunned");
        helper.assertFalse(far.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "in a human's mouth it does not carry far");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void inherentMasterAsksForASureControlOfMana(GameTestHelper helper) {
        ServerPlayer pupil = player(helper, new BlockPos(2, 1, 2), 0.0F);
        ManaService.setTrained(pupil, 5000.0);
        MagicTutorEntity master = helper.spawn(MushoEntities.MAGIC_TUTOR.get(), MIDDLE);
        master.setSchool(SpellSchool.INHERENT);
        master.setRank(Rank.INTERMEDIATE);

        master.interact(pupil, InteractionHand.MAIN_HAND);
        helper.assertFalse(MagicService.knows(pupil, MushoSpells.DISTURB_MAGIC.get()), "no school at Intermediate: nothing taught");

        MagicService.gainExperience(pupil, SpellSchool.WATER, MagicProficiency.threshold(Rank.INTERMEDIATE));
        master.interact(pupil, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(MagicService.knownSpells(pupil).size(), 1, "then the master shows one art");
        Spell shown = MushoSpells.DISTURB_MAGIC.get();
        helper.assertTrue(MagicService.knows(pupil, shown), "Disturb Magic first (in order)");

        MasteryService.setRepetitions(pupil, shown.ref(), MasteryCurve.STANDARD.repetitionsToMaster());
        master.interact(pupil, InteractionHand.MAIN_HAND);
        helper.assertFalse(MagicService.knows(pupil, MushoSpells.HOWLING.get()), "fifty repetitions do not master an inherent art");

        MasteryService.setRepetitions(pupil, shown.ref(), MasteryCurve.LONG_TRAINING.repetitionsToMaster());
        master.interact(pupil, InteractionHand.MAIN_HAND);
        helper.assertTrue(MagicService.knows(pupil, MushoSpells.HOWLING.get()), "long training done: the next art");
        master.discard();
        TestPlayers.leave(pupil);
        helper.succeed();
    }
}
