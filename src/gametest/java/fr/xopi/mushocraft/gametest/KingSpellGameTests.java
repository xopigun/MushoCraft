package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.core.MushoMobEffects;
import fr.xopi.mushocraft.life.LifeService;
import fr.xopi.mushocraft.magic.entity.SpellZone;
import fr.xopi.mushocraft.magic.mana.ManaService;
import fr.xopi.mushocraft.magic.spell.MagicService;
import fr.xopi.mushocraft.magic.spell.MushoSpells;
import fr.xopi.mushocraft.magic.spell.Spell;
import fr.xopi.mushocraft.magic.spell.SpellCasting;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The King-rank spells the wiki names: Lightning and Confinement Barrier.
 */
@GameTestHolder(MushoCraft.MODID)
@PrefixGameTestTemplate(false)
public final class KingSpellGameTests {
    private static final String EMPTY = "empty";
    private static final BlockPos MIDDLE = new BlockPos(4, 1, 4);

    private KingSpellGameTests() {
    }

    private static ServerPlayer caster(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.join(helper);
        player.setGameMode(GameType.SURVIVAL);
        Vec3 spot = helper.absoluteVec(Vec3.atBottomCenterOf(MIDDLE));
        // looking south, slightly down, at the dummy three blocks away
        player.moveTo(spot.x, spot.y, spot.z, 0.0F, 15.0F);
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

    private static void clearZones(GameTestHelper helper, ServerPlayer player) {
        helper.getLevel().getEntitiesOfClass(SpellZone.class, player.getBoundingBox().inflate(30.0), zone -> zone.caster() == player)
                .forEach(SpellZone::discard);
    }

    @GameTest(template = EMPTY, timeoutTicks = 80)
    public static void lightningGathersThenStrikesItsTarget(GameTestHelper helper) {
        ServerPlayer player = caster(helper);
        Husk target = dummy(helper, MIDDLE.south(3));
        float health = target.getHealth();
        helper.assertTrue(cast(player, MushoSpells.LIGHTNING.get()), "the storm gathers over the target");
        helper.runAfterDelay(10, () -> helper.assertTrue(target.getHealth() == health, "nothing falls while the cloud gathers"));
        helper.succeedWhen(() -> {
            helper.assertTrue(!target.isAlive() || target.getHealth() < health - 15.0F, "then the bolt strikes hard");
            clearZones(helper, player);
            TestPlayers.leave(player);
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void confinementBarrierHoldsItsPrisoner(GameTestHelper helper) {
        ServerPlayer player = caster(helper);
        Husk prisoner = dummy(helper, MIDDLE.south(3));
        Vec3 cell = prisoner.position();
        helper.assertTrue(cast(player, MushoSpells.CONFINEMENT_BARRIER.get()), "the barrier closes on the aimed creature");
        helper.runAfterDelay(2, () -> prisoner.teleportTo(cell.x + 4.0, cell.y, cell.z));
        helper.runAfterDelay(6, () -> {
            helper.assertTrue(prisoner.position().distanceTo(cell) < 2.0, "it cannot leave the barrier");
            helper.assertTrue(prisoner.hasEffect(MushoMobEffects.CONFINED), "and is confined");
            clearZones(helper, player);
            TestPlayers.leave(player);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void confinementBarrierFizzlesWithoutTarget(GameTestHelper helper) {
        ServerPlayer player = caster(helper);
        helper.assertFalse(cast(player, MushoSpells.CONFINEMENT_BARRIER.get()), "nothing to shut in: the spell fizzles");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void noMagicInsideAConfinementBarrier(GameTestHelper helper) {
        ServerPlayer player = caster(helper);
        MagicService.learn(player, MushoSpells.WATER_BALL.get());
        ManaService.refill(player);
        player.addEffect(new MobEffectInstance(MushoMobEffects.CONFINED, 100));
        helper.assertFalse(SpellCasting.begin(player, MushoSpells.WATER_BALL.get()).success(), "magic cannot be used inside");
        TestPlayers.leave(player);
        helper.succeed();
    }
}
