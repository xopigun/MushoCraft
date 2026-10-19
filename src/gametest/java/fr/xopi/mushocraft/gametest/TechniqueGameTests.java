package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.body.aura.BattleAuraService;
import fr.xopi.mushocraft.core.Rank;
import fr.xopi.mushocraft.life.LifeService;
import fr.xopi.mushocraft.magic.spell.ChantBroadcast;
import fr.xopi.mushocraft.magic.spell.MushoSpells;
import fr.xopi.mushocraft.sword.entity.ThrownWeapon;
import fr.xopi.mushocraft.sword.entity.TrickKind;
import fr.xopi.mushocraft.sword.technique.AuraShiftAction;
import fr.xopi.mushocraft.sword.technique.CounterKind;
import fr.xopi.mushocraft.sword.technique.CounterStances;
import fr.xopi.mushocraft.sword.technique.MushoTechniques;
import fr.xopi.mushocraft.sword.technique.SwordDomains;
import fr.xopi.mushocraft.sword.technique.Technique;
import fr.xopi.mushocraft.sword.technique.TechniqueContext;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The sword techniques added from the wiki pages of the three styles: Light Reversal, Deprivation Sword Kingdom, and
 * the North God's Hazy Cross, Teardrop Bullet, Crimson Ink, Battle Aura shifting and Pecking.
 */
@GameTestHolder(MushoCraft.MODID)
@PrefixGameTestTemplate(false)
public final class TechniqueGameTests {
    private static final String EMPTY = "empty";

    private TechniqueGameTests() {
    }

    private static ServerPlayer fighter(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.join(helper);
        player.setGameMode(GameType.SURVIVAL);
        Vec3 spot = helper.absoluteVec(new Vec3(1.5, 1.0, 1.5));
        player.moveTo(spot.x, spot.y, spot.z, 0.0F, 0.0F);
        LifeService.setAge(player, 20.0);
        return player;
    }

    private static Husk foe(GameTestHelper helper, BlockPos pos) {
        Husk husk = helper.spawn(EntityType.HUSK, pos);
        husk.setNoAi(true);
        return husk;
    }

    private static boolean perform(Technique technique, ServerPlayer player, Rank rank) {
        return technique.action().perform(new TechniqueContext(player, technique, rank, 1.0));
    }

    /** Fake players cannot be hurt at all: the counters are tried by a creature, which takes the stances just the same. */
    private static boolean performAs(Technique technique, Husk user, Rank rank) {
        return technique.action().perform(new TechniqueContext(user, technique, rank, 1.0));
    }

    @GameTest(template = EMPTY)
    public static void lightReversalCutsTheWristThatStrikes(GameTestHelper helper) {
        Husk defender = foe(helper, new BlockPos(1, 1, 1));
        Husk attacker = foe(helper, new BlockPos(2, 1, 2));
        performAs(MushoTechniques.LIGHT_REVERSAL.get(), defender, Rank.KING);
        helper.assertTrue(CounterStances.active(defender).map(s -> s.kind() == CounterKind.LIGHT_REVERSAL).orElse(false), "in the stance");
        float health = defender.getHealth();
        defender.hurt(defender.damageSources().mobAttack(attacker), 6.0F);
        helper.assertTrue(defender.getHealth() == health, "the blow never lands");
        helper.assertTrue(attacker.hasEffect(MobEffects.WEAKNESS) && attacker.getHealth() < attacker.getMaxHealth(), "the striking arm is cut");
        defender.discard();
        attacker.discard();
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void peckingCrushesTheFistAtTheCostOfFingers(GameTestHelper helper) {
        Husk defender = foe(helper, new BlockPos(1, 1, 1));
        Husk attacker = foe(helper, new BlockPos(2, 1, 2));
        performAs(MushoTechniques.PECKING.get(), defender, Rank.KING);
        float health = defender.getHealth();
        defender.hurt(defender.damageSources().mobAttack(attacker), 10.0F);
        float lost = health - defender.getHealth();
        helper.assertTrue(lost > 0.0F && lost < 5.0F, "the fingers pay a part of the blow: " + lost);
        helper.assertTrue(attacker.hasEffect(MobEffects.DIG_SLOWDOWN) && attacker.hasEffect(MobEffects.WEAKNESS), "the fist is crushed");
        defender.discard();
        attacker.discard();
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void theSwordKingdomAnswersBlowsAndChants(GameTestHelper helper) {
        ServerPlayer player = fighter(helper);
        Husk brute = foe(helper, new BlockPos(3, 1, 3));
        Husk mage = foe(helper, new BlockPos(4, 1, 1));
        Pig pig = helper.spawn(EntityType.PIG, new BlockPos(3, 1, 1));
        perform(MushoTechniques.DEPRIVATION_SWORD_KINGDOM.get(), player, Rank.GOD);
        helper.assertTrue(SwordDomains.of(player).isPresent(), "the field is open");
        pig.hurt(pig.damageSources().mobAttack(brute), 1.0F);
        helper.assertTrue(brute.getHealth() < brute.getMaxHealth(), "whoever attacks inside it is cut down");
        ChantBroadcast.started(mage, MushoSpells.WATER_BALL.get(), 40);
        helper.runAtTickTime(12, () -> {
            helper.assertTrue(mage.getHealth() < mage.getMaxHealth(), "a chant inside it is answered");
            helper.assertFalse(ChantBroadcast.isChanting(mage), "and broken off");
            float idle = pig.getHealth();
            helper.assertTrue(idle > 0.0F, "it strikes no one who does nothing");
            brute.discard();
            mage.discard();
            pig.discard();
            TestPlayers.leave(player);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void teardropBulletBlindsAndCrimsonInkSticks(GameTestHelper helper) {
        Husk blinded = foe(helper, new BlockPos(1, 1, 1));
        Husk stuck = foe(helper, new BlockPos(6, 1, 6));
        TrickKind.SPICE.burstAt(helper.getLevel(), blinded.position(), null);
        TrickKind.CRIMSON_INK.burstAt(helper.getLevel(), stuck.position(), null);
        helper.assertTrue(blinded.hasEffect(MobEffects.BLINDNESS) && blinded.hasEffect(MobEffects.CONFUSION), "spice in the eyes");
        helper.assertFalse(stuck.hasEffect(MobEffects.BLINDNESS), "the spice cloud stays where it burst");
        helper.assertTrue(stuck.getEffect(MobEffects.MOVEMENT_SLOWDOWN) != null
                && stuck.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getAmplifier() >= 9, "glued in place");
        blinded.discard();
        stuck.discard();
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void auraShiftingTradesPowerForSpeed(GameTestHelper helper) {
        ServerPlayer player = fighter(helper);
        Technique shift = MushoTechniques.AURA_SHIFTING.get();
        helper.assertFalse(perform(shift, player, Rank.ADVANCED), "no shift without a raised aura");
        BattleAuraService.setMastery(player, 2500.0);
        BattleAuraService.toggle(player);
        double speed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
        double damage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        helper.assertTrue(perform(shift, player, Rank.ADVANCED), "shifted");
        helper.assertTrue(player.getAttributeValue(Attributes.MOVEMENT_SPEED) > speed, "faster");
        helper.assertTrue(player.getAttributeValue(Attributes.ATTACK_DAMAGE) < damage, "weaker blows");
        perform(shift, player, Rank.ADVANCED);
        helper.assertFalse(AuraShiftAction.isShifted(player), "the second use sets it back");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void hazyCrossThrowsTheSecondSword(GameTestHelper helper) {
        ServerPlayer player = fighter(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.IRON_SWORD));
        helper.assertTrue(MushoTechniques.HAZY_CROSS.get().weapon().isMetBy(player), "two swords in hand");
        helper.assertTrue(perform(MushoTechniques.HAZY_CROSS.get(), player, Rank.SAINT), "the overhead blow");
        helper.runAtTickTime(11, () -> {
            helper.assertTrue(player.getItemInHand(InteractionHand.OFF_HAND).isEmpty(), "the second sword has left the hand");
            helper.assertFalse(helper.getLevel().getEntitiesOfClass(ThrownWeapon.class, player.getBoundingBox().inflate(16.0)).isEmpty(),
                    "and flies at the foe");
            TestPlayers.leave(player);
            helper.succeed();
        });
    }
}
