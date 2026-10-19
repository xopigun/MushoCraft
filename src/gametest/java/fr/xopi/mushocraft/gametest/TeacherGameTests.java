package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.body.BodyService;
import fr.xopi.mushocraft.body.BodyStat;
import fr.xopi.mushocraft.body.aura.AuraLevel;
import fr.xopi.mushocraft.body.aura.BattleAuraService;
import fr.xopi.mushocraft.combat.mastery.MasteryRules;
import fr.xopi.mushocraft.combat.mastery.MasteryService;
import fr.xopi.mushocraft.combat.move.MoveRef;
import fr.xopi.mushocraft.core.MushoEntities;
import fr.xopi.mushocraft.core.Rank;
import fr.xopi.mushocraft.life.LifeService;
import fr.xopi.mushocraft.magic.mana.ManaService;
import fr.xopi.mushocraft.magic.spell.MagicService;
import fr.xopi.mushocraft.magic.spell.MushoSpells;
import fr.xopi.mushocraft.magic.spell.SpellSchool;
import fr.xopi.mushocraft.npc.MagicTutorEntity;
import fr.xopi.mushocraft.npc.SwordMasterEntity;
import fr.xopi.mushocraft.npc.TeacherEntity;
import fr.xopi.mushocraft.sword.StyleRules;
import fr.xopi.mushocraft.sword.SwordService;
import fr.xopi.mushocraft.sword.SwordStyle;
import fr.xopi.mushocraft.sword.technique.MushoTechniques;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.EnumSet;
import java.util.Optional;

@GameTestHolder(MushoCraft.MODID)
@PrefixGameTestTemplate(false)
public final class TeacherGameTests {
    private static final String EMPTY = "empty";
    private static final BlockPos TEACHER_POS = new BlockPos(4, 1, 4);

    private TeacherGameTests() {
    }

    private static ServerPlayer pupil(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.join(helper);
        player.setGameMode(GameType.SURVIVAL);
        Vec3 spot = helper.absoluteVec(new Vec3(2.5, 1.0, 2.5));
        player.moveTo(spot.x, spot.y, spot.z, 0.0F, 0.0F);
        LifeService.setAge(player, 20.0);
        return player;
    }

    private static SwordMasterEntity master(GameTestHelper helper, SwordStyle style, Rank rank) {
        SwordMasterEntity master = helper.spawn(MushoEntities.SWORD_MASTER.get(), TEACHER_POS);
        master.setStyle(style);
        master.setRank(rank);
        return master;
    }

    private static MagicTutorEntity tutor(GameTestHelper helper, SpellSchool school, Rank rank) {
        MagicTutorEntity tutor = helper.spawn(MushoEntities.MAGIC_TUTOR.get(), TEACHER_POS);
        tutor.setSchool(school);
        tutor.setRank(rank);
        return tutor;
    }

    /** A body trained enough for every rank a master may ask for. */
    private static void trainBody(ServerPlayer player) {
        for (BodyStat stat : BodyStat.values()) {
            BodyService.setExperience(player, stat, 1.0E7);
        }
    }

    /** The pupil repeats every move they were shown until it is mastered. */
    private static void masterEverythingShown(ServerPlayer player) {
        MagicService.knownSpells(player).forEach(id ->
                MasteryService.setRepetitions(player, MoveRef.spell(id), MasteryRules.REPETITIONS_TO_MASTER));
        SwordService.knownTechniques(player).forEach(id ->
                MasteryService.setRepetitions(player, MoveRef.technique(id), MasteryRules.REPETITIONS_TO_MASTER));
    }

    private static void lessons(TeacherEntity teacher, ServerPlayer player, int count) {
        for (int i = 0; i < count; i++) {
            masterEverythingShown(player);
            teacher.interact(player, InteractionHand.MAIN_HAND);
        }
    }

    @GameTest(template = EMPTY)
    public static void masterTakesADiscipleAndShowsOneTechniqueAtATime(GameTestHelper helper) {
        ServerPlayer player = pupil(helper);
        SwordMasterEntity master = master(helper, SwordStyle.SWORD_GOD, Rank.SAINT);

        master.interact(player, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(SwordService.activeStyle(player), Optional.of(SwordStyle.SWORD_GOD), "disciple of the master's style");
        helper.assertTrue(SwordService.knows(player, MushoTechniques.ARM_CHOP.get()), "the first technique is shown");

        master.interact(player, InteractionHand.MAIN_HAND);
        helper.assertFalse(SwordService.knows(player, MushoTechniques.LONGSWORD_OF_SILENCE.get()),
                "nothing new before the first technique is mastered");
        helper.assertValueEqual(SwordService.rank(player, SwordStyle.SWORD_GOD), Optional.of(Rank.BEGINNER), "no rank given");
        master.discard();
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void masterDemandsATrainedBodyBeforeTheNextRank(GameTestHelper helper) {
        ServerPlayer player = pupil(helper);
        SwordMasterEntity master = master(helper, SwordStyle.WATER_GOD, Rank.SAINT);
        lessons(master, player, 4);
        helper.assertTrue(MasteryService.isMastered(player, MushoTechniques.FLOW.get().ref()), "Flow shown and mastered");
        helper.assertValueEqual(SwordService.rank(player, SwordStyle.WATER_GOD), Optional.of(Rank.BEGINNER),
                "an untrained body stays Beginner");
        master.discard();
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void masterLeadsRankByRankUpToTheirOwn(GameTestHelper helper) {
        ServerPlayer player = pupil(helper);
        trainBody(player);
        BattleAuraService.setMastery(player, AuraLevel.COMPLETE.mastery());
        SwordMasterEntity advanced = master(helper, SwordStyle.NORTH_GOD, Rank.ADVANCED);

        lessons(advanced, player, 12);
        helper.assertTrue(SwordService.knows(player, MushoTechniques.FOUR_LEGGED_FORM.get()), "Intermediate technique shown");
        helper.assertValueEqual(SwordService.rank(player, SwordStyle.NORTH_GOD), Optional.of(Rank.ADVANCED),
                "an Advanced master stops their pupil at Advanced");
        helper.assertFalse(SwordService.knows(player, MushoTechniques.MOONLIGHT_CLEAVE.get()), "a King technique is beyond him");

        SwordMasterEntity king = master(helper, SwordStyle.NORTH_GOD, Rank.KING);
        lessons(king, player, 12);
        helper.assertValueEqual(SwordService.rank(player, SwordStyle.NORTH_GOD), Optional.of(Rank.KING), "a King leads on to King");
        helper.assertTrue(MasteryService.isMastered(player, MushoTechniques.MOONLIGHT_CLEAVE.get().ref()),
                "after mastering Moonlight Cleave");
        advanced.discard();
        king.discard();
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void rankWaitsForTheBattleAura(GameTestHelper helper) {
        ServerPlayer player = pupil(helper);
        trainBody(player);
        SwordMasterEntity master = master(helper, SwordStyle.WATER_GOD, Rank.SAINT);
        lessons(master, player, 8);
        helper.assertValueEqual(SwordService.rank(player, SwordStyle.WATER_GOD), Optional.of(Rank.INTERMEDIATE),
                "Advanced demands a conscious Battle Aura (wiki)");
        master.discard();
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void masterOfAnotherStyleTeachesNothingOfOurs(GameTestHelper helper) {
        ServerPlayer player = pupil(helper);
        SwordMasterEntity northGod = master(helper, SwordStyle.NORTH_GOD, Rank.KING);

        northGod.interact(player, InteractionHand.MAIN_HAND);

        helper.assertFalse(SwordService.knows(player, MushoTechniques.ARM_CHOP.get()), "Arm Chop belongs to the Sword God Style");
        helper.assertValueEqual(SwordService.activeStyle(player), Optional.of(SwordStyle.NORTH_GOD), "disciple of the North God Style");
        northGod.discard();
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void tutorTeachesOneSpellAtATimeUpToTheirRank(GameTestHelper helper) {
        ServerPlayer player = pupil(helper);
        MagicTutorEntity tutor = tutor(helper, SpellSchool.WATER, Rank.INTERMEDIATE);

        tutor.interact(player, InteractionHand.MAIN_HAND);
        tutor.interact(player, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(MagicService.knownSpells(player).size(), 1, "one spell, to master before the next");
        helper.assertFalse(MagicService.knows(player, MushoSpells.FIREBALL.get()), "a water tutor teaches no fire magic");

        lessons(tutor, player, 16);
        helper.assertTrue(MagicService.knows(player, MushoSpells.WATER_CANNON.get()), "Intermediate spells shown in turn");
        helper.assertValueEqual(MagicService.proficiency(player).rank(SpellSchool.WATER), Rank.INTERMEDIATE,
                "every Intermediate spell mastered: Intermediate in water magic");
        helper.assertFalse(MagicService.knows(player, MushoSpells.BLIZZARD_STORM.get()), "nothing above the tutor's rank");
        tutor.discard();
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void tutorRanksStayWithinWhatTheirSchoolTeaches(GameTestHelper helper) {
        RandomSource random = RandomSource.create(7L);
        EnumSet<Rank> water = EnumSet.noneOf(Rank.class);
        EnumSet<Rank> healing = EnumSet.noneOf(Rank.class);
        for (int i = 0; i < 4000; i++) {
            water.add(MagicTutorEntity.randomRank(SpellSchool.WATER, random));
            healing.add(MagicTutorEntity.randomRank(SpellSchool.HEALING, random));
        }
        helper.assertTrue(water.contains(Rank.SAINT) && water.contains(Rank.KING) && water.contains(Rank.EMPEROR),
                "water tutors may be Saints, Kings, even an Emperor (Absolute Zero): " + water);
        helper.assertFalse(healing.contains(Rank.SAINT), "no Saint healing spell is named: no Saint healing tutor (" + healing + ")");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void aWaterKingLeadsOnToLightning(GameTestHelper helper) {
        ServerPlayer player = pupil(helper);
        ManaService.setTrained(player, 1.0E7);
        MagicTutorEntity king = tutor(helper, SpellSchool.WATER, Rank.KING);
        lessons(king, player, 40);
        helper.assertTrue(MagicService.knows(player, MushoSpells.CUMULONIMBUS.get()), "the Saint spells are shown");
        helper.assertTrue(MagicService.knows(player, MushoSpells.LIGHTNING.get()), "then Lightning");
        helper.assertValueEqual(MagicService.proficiency(player).rank(SpellSchool.WATER), Rank.KING, "a King leads on to King");
        helper.assertFalse(MagicService.knows(player, MushoSpells.ABSOLUTE_ZERO.get()), "the Emperor spell is beyond a King");
        king.discard();
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void teacherKeepsRankAndStyleWhenSaved(GameTestHelper helper) {
        SwordMasterEntity master = master(helper, SwordStyle.NORTH_GOD, Rank.KING);
        CompoundTag tag = new CompoundTag();
        master.saveWithoutId(tag);
        SwordMasterEntity copy = MushoEntities.SWORD_MASTER.get().create(helper.getLevel());
        helper.assertTrue(copy != null, "entity type creates a master");
        copy.load(tag);
        helper.assertValueEqual(copy.style(), SwordStyle.NORTH_GOD, "style saved");
        helper.assertValueEqual(copy.rank(), Rank.KING, "rank saved");
        master.discard();
        helper.succeed();
    }

    /** Its own batch: the rule looks 128 blocks around, where other tests' teachers would stand. */
    @GameTest(template = EMPTY, batch = "teacher_spawn")
    public static void teachersNeverSpawnNextToAnother(GameTestHelper helper) {
        helper.setBlock(new BlockPos(2, 0, 2), Blocks.GRASS_BLOCK);
        BlockPos spot = helper.absolutePos(new BlockPos(2, 1, 2));
        RandomSource random = RandomSource.create(42L);

        boolean aloneCanSpawn = false;
        for (int attempt = 0; attempt < 64 && !aloneCanSpawn; attempt++) {
            aloneCanSpawn = TeacherEntity.checkTeacherSpawnRules(MushoEntities.SWORD_MASTER.get(), helper.getLevel(),
                    MobSpawnType.NATURAL, spot, random);
        }
        helper.assertTrue(aloneCanSpawn, "a teacher can appear where there is none");

        MagicTutorEntity tutor = tutor(helper, SpellSchool.FIRE, Rank.INTERMEDIATE);
        for (int attempt = 0; attempt < 64; attempt++) {
            helper.assertFalse(TeacherEntity.checkTeacherSpawnRules(MushoEntities.SWORD_MASTER.get(), helper.getLevel(),
                    MobSpawnType.NATURAL, spot, random), "never two teachers close together");
        }
        tutor.discard();
        helper.succeed();
    }
}
