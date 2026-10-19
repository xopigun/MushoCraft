package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.body.aura.AuraLevel;
import fr.xopi.mushocraft.body.aura.BattleAuraService;
import fr.xopi.mushocraft.core.MushoEntities;
import fr.xopi.mushocraft.core.Rank;
import fr.xopi.mushocraft.life.LifeService;
import fr.xopi.mushocraft.magic.spell.MagicService;
import fr.xopi.mushocraft.magic.spell.MushoSpells;
import fr.xopi.mushocraft.magic.spell.SpellSchool;
import fr.xopi.mushocraft.npc.MagicTutorEntity;
import fr.xopi.mushocraft.npc.SwordMasterEntity;
import fr.xopi.mushocraft.sword.StyleRules;
import fr.xopi.mushocraft.sword.SwordService;
import fr.xopi.mushocraft.sword.SwordStyle;
import fr.xopi.mushocraft.sword.technique.MushoTechniques;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

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

    @GameTest(template = EMPTY)
    public static void masterTakesADiscipleAndTeachesWhatTheyCanLearn(GameTestHelper helper) {
        ServerPlayer player = pupil(helper);
        SwordMasterEntity master = master(helper, SwordStyle.SWORD_GOD, Rank.SAINT);

        master.interact(player, InteractionHand.MAIN_HAND);

        helper.assertValueEqual(SwordService.activeStyle(player), Optional.of(SwordStyle.SWORD_GOD), "disciple of the master's style");
        helper.assertTrue(SwordService.knows(player, MushoTechniques.ARM_CHOP.get()), "Beginner technique taught at once");
        helper.assertFalse(SwordService.knows(player, MushoTechniques.LONGSWORD_OF_SILENCE.get()),
                "an Advanced technique waits for the Advanced rank");
        master.discard();
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void masterRaisesUpToTheirOwnRankOnly(GameTestHelper helper) {
        ServerPlayer player = pupil(helper);
        SwordService.adopt(player, SwordStyle.WATER_GOD);
        SwordService.gainExperience(player, SwordStyle.WATER_GOD, StyleRules.threshold(Rank.KING));
        BattleAuraService.setMastery(player, AuraLevel.COMPLETE.mastery());

        SwordMasterEntity advanced = master(helper, SwordStyle.WATER_GOD, Rank.ADVANCED);
        advanced.interact(player, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(SwordService.rank(player, SwordStyle.WATER_GOD), Optional.of(Rank.ADVANCED),
                "an Advanced master stops their pupil at Advanced");

        SwordMasterEntity saint = master(helper, SwordStyle.WATER_GOD, Rank.SAINT);
        saint.interact(player, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(SwordService.rank(player, SwordStyle.WATER_GOD), Optional.of(Rank.SAINT),
                "a Saint takes them on to Saint");
        advanced.discard();
        saint.discard();
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
    public static void tutorTeachesTheirSchoolUpToTheirRank(GameTestHelper helper) {
        ServerPlayer player = pupil(helper);
        MagicTutorEntity tutor = tutor(helper, SpellSchool.WATER, Rank.INTERMEDIATE);

        tutor.interact(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(MagicService.knows(player, MushoSpells.WATER_BALL.get()), "Beginner water spell taught");
        helper.assertFalse(MagicService.knows(player, MushoSpells.WATER_CANNON.get()), "Intermediate spell needs practice first");
        helper.assertFalse(MagicService.knows(player, MushoSpells.FIREBALL.get()), "a water tutor teaches no fire magic");

        MagicService.setExperience(player, SpellSchool.WATER, 150.0);
        tutor.interact(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(MagicService.knows(player, MushoSpells.WATER_CANNON.get()), "Intermediate spell taught once ready");
        tutor.discard();
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
}
