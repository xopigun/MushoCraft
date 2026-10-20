package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.magic.spell.SpellSchool;
import fr.xopi.mushocraft.npc.MagicTutorEntity;
import fr.xopi.mushocraft.npc.SwordMasterEntity;
import fr.xopi.mushocraft.npc.TeacherEntity;
import fr.xopi.mushocraft.sword.SwordStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The places of the mod (tools/structures): a dojo has a resident master of its style, the church of Milis a healing and
 * a divine strike tutor. Each template is placed high above the test plot, out of the other tests' way.
 */
@GameTestHolder(MushoCraft.MODID)
@PrefixGameTestTemplate(false)
public final class PlaceGameTests {
    private static final String EMPTY = "empty";
    private static final int ABOVE = 60;

    private PlaceGameTests() {
    }

    private static <T extends TeacherEntity> List<T> place(GameTestHelper helper, String name, Class<T> resident) {
        StructureTemplate template = helper.getLevel().getStructureManager().get(MushoCraft.id(name)).orElseThrow();
        BlockPos corner = helper.absolutePos(new BlockPos(0, ABOVE, 0));
        template.placeInWorld(helper.getLevel(), corner, corner, new StructurePlaceSettings().setFinalizeEntities(true),
                helper.getLevel().getRandom(), 2);
        AABB area = new AABB(corner).expandTowards(template.getSize().getX(), template.getSize().getY(), template.getSize().getZ());
        return helper.getLevel().getEntitiesOfClass(resident, area.inflate(2.0));
    }

    private static void clear(GameTestHelper helper, String name) {
        BlockPos corner = helper.absolutePos(new BlockPos(0, ABOVE, 0));
        helper.getLevel().getEntitiesOfClass(Entity.class, new AABB(corner).inflate(40.0)).forEach(Entity::discard);
    }

    @GameTest(template = EMPTY)
    public static void aDojoHasAResidentMasterOfItsStyle(GameTestHelper helper) {
        List<SwordMasterEntity> masters = place(helper, "water_god_dojo", SwordMasterEntity.class);
        helper.assertTrue(masters.size() == 1, "one master in the dojo: " + masters.size());
        SwordMasterEntity master = masters.getFirst();
        helper.assertValueEqual(master.style(), SwordStyle.WATER_GOD, "the dojo's style");
        helper.assertTrue(master.isResident() && master.hasRestriction(), "a resident, staying at its post");
        clear(helper, "water_god_dojo");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void theChurchOfMilisHasItsHealingAndDivineTutors(GameTestHelper helper) {
        List<MagicTutorEntity> tutors = place(helper, "milis_church", MagicTutorEntity.class);
        Set<SpellSchool> schools = tutors.stream().map(MagicTutorEntity::school).collect(Collectors.toSet());
        helper.assertValueEqual(schools, Set.of(SpellSchool.HEALING, SpellSchool.DIVINE_STRIKE), "the church's schools");
        helper.assertTrue(tutors.stream().allMatch(TeacherEntity::isResident), "residents");
        clear(helper, "milis_church");
        helper.succeed();
    }
}
