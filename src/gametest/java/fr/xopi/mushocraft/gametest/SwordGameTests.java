package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.body.aura.AuraLevel;
import fr.xopi.mushocraft.body.aura.BattleAuraService;
import fr.xopi.mushocraft.combat.move.CooldownService;
import fr.xopi.mushocraft.combat.move.MoveService;
import fr.xopi.mushocraft.core.Rank;
import fr.xopi.mushocraft.life.LifeService;
import fr.xopi.mushocraft.sword.StyleRules;
import fr.xopi.mushocraft.sword.SwordMastery;
import fr.xopi.mushocraft.sword.SwordService;
import fr.xopi.mushocraft.sword.SwordStyle;
import fr.xopi.mushocraft.sword.entity.ThrownWeapon;
import fr.xopi.mushocraft.sword.technique.CounterStances;
import fr.xopi.mushocraft.sword.technique.MushoTechniques;
import fr.xopi.mushocraft.sword.technique.TechniqueContext;
import fr.xopi.mushocraft.sword.technique.TechniqueStrikes;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;

@GameTestHolder(MushoCraft.MODID)
@PrefixGameTestTemplate(false)
public final class SwordGameTests {
    private static final String EMPTY = "empty";

    private SwordGameTests() {
    }

    private static ServerPlayer swordsman(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.join(helper);
        player.setGameMode(GameType.SURVIVAL);
        Vec3 spot = helper.absoluteVec(new Vec3(1.5, 1.0, 1.5));
        player.moveTo(spot.x, spot.y, spot.z, 0.0F, 0.0F);
        LifeService.setAge(player, 20.0);
        return player;
    }

    @GameTest(template = EMPTY)
    public static void practiceAloneStopsAtIntermediate(GameTestHelper helper) {
        ServerPlayer player = swordsman(helper);
        SwordService.adopt(player, SwordStyle.SWORD_GOD);
        SwordService.gainExperience(player, SwordStyle.SWORD_GOD, StyleRules.threshold(Rank.SAINT));
        helper.assertValueEqual(SwordService.rank(player, SwordStyle.SWORD_GOD), Optional.of(Rank.INTERMEDIATE),
                "no master: stuck at Intermediate");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void advancedNeedsConsciousAuraAndAMaster(GameTestHelper helper) {
        ServerPlayer player = swordsman(helper);
        SwordService.adopt(player, SwordStyle.WATER_GOD);
        SwordService.gainExperience(player, SwordStyle.WATER_GOD, StyleRules.threshold(Rank.SAINT));

        SwordService.promote(player, SwordStyle.WATER_GOD, Rank.GOD);
        helper.assertValueEqual(SwordService.rank(player, SwordStyle.WATER_GOD), Optional.of(Rank.INTERMEDIATE),
                "without conscious aura, even a Water God cannot make you Advanced");

        BattleAuraService.setMastery(player, AuraLevel.CONSCIOUS.mastery());
        SwordService.promote(player, SwordStyle.WATER_GOD, Rank.INTERMEDIATE);
        helper.assertValueEqual(SwordService.rank(player, SwordStyle.WATER_GOD), Optional.of(Rank.INTERMEDIATE),
                "an Intermediate master cannot raise above Intermediate");

        SwordService.promote(player, SwordStyle.WATER_GOD, Rank.GOD);
        helper.assertValueEqual(SwordService.rank(player, SwordStyle.WATER_GOD), Optional.of(Rank.ADVANCED),
                "conscious aura: Advanced, but Saint needs complete control");

        BattleAuraService.setMastery(player, AuraLevel.COMPLETE.mastery());
        SwordService.promote(player, SwordStyle.WATER_GOD, Rank.GOD);
        helper.assertValueEqual(SwordService.rank(player, SwordStyle.WATER_GOD), Optional.of(Rank.SAINT),
                "complete aura and enough experience: Saint");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void techniquesNeedTheirRank(GameTestHelper helper) {
        ServerPlayer player = swordsman(helper);
        helper.assertValueEqual(SwordService.teach(player, MushoTechniques.ARM_CHOP.get()), SwordService.TeachResult.NOT_READY,
                "a style must be practised first");
        SwordService.adopt(player, SwordStyle.SWORD_GOD);
        helper.assertValueEqual(SwordService.teach(player, MushoTechniques.ARM_CHOP.get()), SwordService.TeachResult.LEARNED,
                "Arm Chop is a Beginner technique");
        helper.assertValueEqual(SwordService.teach(player, MushoTechniques.LONGSWORD_OF_LIGHT.get()),
                SwordService.TeachResult.NOT_READY, "the Longsword of Light waits for Saint");
        helper.assertValueEqual(MoveService.loadout(player).get(0), Optional.of(MushoTechniques.ARM_CHOP.get().ref()),
                "the technique goes into the move bar");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void techniquesOfAStyleShareOneCooldown(GameTestHelper helper) {
        ServerPlayer player = swordsman(helper);
        SwordService.adopt(player, SwordStyle.SWORD_GOD);
        SwordService.learn(player, MushoTechniques.ARM_CHOP.get());
        helper.assertValueEqual(MushoTechniques.ARM_CHOP.get().cooldownKey(), MushoTechniques.LONGSWORD_OF_LIGHT.get().cooldownKey(),
                "same style, same cooldown");
        helper.assertFalse(MushoTechniques.ARM_CHOP.get().cooldownKey().equals(MushoTechniques.FLOW.get().cooldownKey()),
                "other styles have their own");
        CooldownService.start(player, SwordStyle.SWORD_GOD.cooldownKey(), 100);
        helper.assertFalse(MoveService.use(player, 0).success(), "Arm Chop waits for the style cooldown");
        helper.assertTrue(StyleRules.cooldownFactor(Rank.KING) < StyleRules.cooldownFactor(Rank.BEGINNER),
                "mastery shortens the style cooldown");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void throwingSendsTheWeaponFlying(GameTestHelper helper) {
        ServerPlayer player = swordsman(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
        SwordService.adopt(player, SwordStyle.NORTH_GOD);
        boolean thrown = MushoTechniques.SWORD_THROWING.get().action().perform(
                new TechniqueContext(player, MushoTechniques.SWORD_THROWING.get(), Rank.BEGINNER, 1.0));
        helper.assertTrue(thrown, "the sword is thrown");
        helper.assertTrue(player.getMainHandItem().isEmpty(), "the hand is empty");
        ThrownWeapon flying = helper.getLevel().getEntitiesOfClass(ThrownWeapon.class, player.getBoundingBox().inflate(4.0))
                .stream().findFirst().orElse(null);
        helper.assertTrue(flying != null && flying.weapon().is(Items.IRON_SWORD), "the iron sword is in the air");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void flowWaitsForTheNextAttack(GameTestHelper helper) {
        ServerPlayer player = swordsman(helper);
        MushoTechniques.FLOW.get().action().perform(new TechniqueContext(player, MushoTechniques.FLOW.get(), Rank.BEGINNER, 1.0));
        helper.assertTrue(CounterStances.active(player).isPresent(), "in the receiving stance");
        helper.assertFalse(CounterStances.active(player).orElseThrow().deflectsProjectiles(), "a Beginner cannot turn missiles");
        MushoTechniques.FLOW.get().action().perform(new TechniqueContext(player, MushoTechniques.FLOW.get(), Rank.SAINT, 1.0));
        helper.assertTrue(CounterStances.active(player).orElseThrow().deflectsProjectiles(), "a Saint turns missiles away");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void armedStrikeMatchesOnlyItsAnimation(GameTestHelper helper) {
        ServerPlayer player = swordsman(helper);
        ResourceLocation animation = ResourceLocation.fromNamespaceAndPath("epicfight", "biped/combat/battojutsu");
        TechniqueStrikes.arm(player, animation, 2.0F, 40, null);
        helper.assertTrue(TechniqueStrikes.matching(player, animation).isPresent(), "the technique's own hit is boosted");
        helper.assertTrue(TechniqueStrikes.matching(player,
                ResourceLocation.fromNamespaceAndPath("epicfight", "biped/combat/sword_auto1")).isEmpty(), "other hits are not");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void masterySurvivesSerializationAndRebirthClearsIt(GameTestHelper helper) {
        ServerPlayer player = swordsman(helper);
        SwordService.adopt(player, SwordStyle.NORTH_GOD);
        SwordService.learn(player, MushoTechniques.SWORD_THROWING.get());
        SwordMastery original = SwordService.mastery(player);
        Tag encoded = SwordMastery.CODEC.encodeStart(NbtOps.INSTANCE, original).getOrThrow();
        helper.assertValueEqual(SwordMastery.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow(), original, "save and load");
        LifeService.rebirth(player);
        helper.assertValueEqual(SwordService.mastery(player), SwordMastery.none(), "a new life starts with no sword training");
        TestPlayers.leave(player);
        helper.succeed();
    }
}
