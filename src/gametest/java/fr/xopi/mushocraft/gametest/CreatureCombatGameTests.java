package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.core.MushoEntities;
import fr.xopi.mushocraft.core.Rank;
import fr.xopi.mushocraft.magic.spell.SpellSchool;
import fr.xopi.mushocraft.npc.MagicTutorEntity;
import fr.xopi.mushocraft.npc.SwordMasterEntity;
import fr.xopi.mushocraft.sword.SwordStyle;
import fr.xopi.mushocraft.sword.entity.ThrownWeapon;
import fr.xopi.mushocraft.sword.technique.CounterStances;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Creatures fight like players: spells chanted and released, sword techniques of their style.
 */
@GameTestHolder(MushoCraft.MODID)
@PrefixGameTestTemplate(false)
public final class CreatureCombatGameTests {
    private static final String EMPTY = "empty";

    private CreatureCombatGameTests() {
    }

    private static MagicTutorEntity tutor(GameTestHelper helper, SpellSchool school, Rank rank, BlockPos pos) {
        MagicTutorEntity tutor = helper.spawn(MushoEntities.MAGIC_TUTOR.get(), pos);
        tutor.setSchool(school);
        tutor.setRank(rank);
        return tutor;
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void aTutorFightsWithTheSpellsTheyTeach(GameTestHelper helper) {
        MagicTutorEntity tutor = tutor(helper, SpellSchool.FIRE, Rank.INTERMEDIATE, new BlockPos(1, 1, 1));
        Husk husk = helper.spawn(EntityType.HUSK, new BlockPos(6, 1, 6));
        husk.setNoAi(true);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            husk.setItemSlot(slot, ItemStack.EMPTY);
        }
        tutor.setTarget(husk);
        float before = husk.getHealth();
        helper.succeedWhen(() -> {
            helper.assertTrue(husk.getHealth() < before || !husk.isAlive(), "the target is hit by fire magic");
            tutor.discard();
            husk.discard();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void aHurtHealerHealsThemself(GameTestHelper helper) {
        MagicTutorEntity healer = tutor(helper, SpellSchool.HEALING, Rank.INTERMEDIATE, new BlockPos(4, 1, 4));
        healer.setHealth(healer.getMaxHealth() * 0.3F);
        float before = healer.getHealth();
        helper.succeedWhen(() -> {
            helper.assertTrue(healer.getHealth() > before + 3.0F, "healed by their own spell");
            healer.discard();
        });
    }

    private static Husk dummy(GameTestHelper helper, BlockPos pos) {
        Husk husk = helper.spawn(EntityType.HUSK, pos);
        husk.setNoAi(true);
        return husk;
    }

    private static SwordMasterEntity master(GameTestHelper helper, SwordStyle style, Rank rank, BlockPos pos) {
        SwordMasterEntity master = helper.spawn(MushoEntities.SWORD_MASTER.get(), pos);
        master.setStyle(style);
        master.setRank(rank);
        return master;
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void aNorthGodMasterThrowsTheirSwordAtADistantFoe(GameTestHelper helper) {
        SwordMasterEntity master = master(helper, SwordStyle.NORTH_GOD, Rank.KING, new BlockPos(0, 1, 0));
        Husk husk = dummy(helper, new BlockPos(6, 1, 6));
        master.setTarget(husk);
        helper.succeedWhen(() -> {
            helper.assertFalse(helper.getLevel().getEntitiesOfClass(ThrownWeapon.class, master.getBoundingBox().inflate(16.0)).isEmpty(),
                    "Sword Throwing at a foe out of reach");
            master.discard();
            husk.discard();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void aWaterGodMasterWaitsToCounter(GameTestHelper helper) {
        SwordMasterEntity master = master(helper, SwordStyle.WATER_GOD, Rank.SAINT, new BlockPos(3, 1, 3));
        Husk husk = dummy(helper, new BlockPos(3, 1, 5));
        master.setTarget(husk);
        helper.succeedWhen(() -> {
            helper.assertTrue(CounterStances.active(master).isPresent(), "Flow: the master awaits the blow");
            master.discard();
            husk.discard();
        });
    }
}
