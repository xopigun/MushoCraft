package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.core.MushoEntities;
import fr.xopi.mushocraft.core.Rank;
import fr.xopi.mushocraft.magic.spell.SpellSchool;
import fr.xopi.mushocraft.npc.MagicTutorEntity;
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
 * Creatures that know magic cast real spells, chanting like players.
 */
@GameTestHolder(MushoCraft.MODID)
@PrefixGameTestTemplate(false)
public final class SpellcasterGameTests {
    private static final String EMPTY = "empty";

    private SpellcasterGameTests() {
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
}
