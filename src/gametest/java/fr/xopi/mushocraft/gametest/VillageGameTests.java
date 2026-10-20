package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.core.MushoEntities;
import fr.xopi.mushocraft.life.LifeStage;
import fr.xopi.mushocraft.village.Household;
import fr.xopi.mushocraft.village.TownsfolkEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The village folk: households that hang together, people who age and grow like players.
 */
@GameTestHolder(MushoCraft.MODID)
@PrefixGameTestTemplate(false)
public final class VillageGameTests {
    private static final String EMPTY = "empty";

    private VillageGameTests() {
    }

    private static TownsfolkEntity person(GameTestHelper helper, BlockPos pos) {
        return helper.spawn(MushoEntities.TOWNSFOLK.get(), pos);
    }

    @GameTest(template = EMPTY)
    public static void aHouseholdHangsTogether(GameTestHelper helper) {
        for (int round = 0; round < 12; round++) {
            List<TownsfolkEntity> family = Household.found(person(helper, new BlockPos(3, 1, 3)));
            Map<UUID, TownsfolkEntity> byId = family.stream().collect(Collectors.toMap(TownsfolkEntity::getUUID, Function.identity()));
            TownsfolkEntity founder = family.getFirst();
            for (TownsfolkEntity member : family) {
                helper.assertTrue(!member.givenName().isEmpty() && member.familyName().equals(founder.familyName()),
                        "everyone is named, with the family's name");
                helper.assertTrue(member.home().equals(founder.home()), "everyone lives in the same house");
                member.spouse().map(byId::get).ifPresent(spouse -> {
                    helper.assertTrue(spouse.spouse().orElseThrow().equals(member.getUUID()), "spouses are married to each other");
                    helper.assertTrue(spouse.sex() != member.sex() && spouse.isAdult() && member.isAdult(), "a man and a woman, both grown");
                });
                for (UUID childId : member.children()) {
                    TownsfolkEntity child = byId.get(childId);
                    if (child != null) {
                        helper.assertTrue(child.parents().contains(member.getUUID()), "a child knows its parents");
                        helper.assertTrue(member.ageInYears() - child.ageInYears() >= LifeStage.ADULT.startAge(),
                                "parents had their children as adults");
                    }
                }
            }
            family.forEach(TownsfolkEntity::discard);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void townsfolkGrowLikePlayers(GameTestHelper helper) {
        TownsfolkEntity baby = person(helper, new BlockPos(1, 1, 1));
        TownsfolkEntity adult = person(helper, new BlockPos(4, 1, 1));
        TownsfolkEntity elder = person(helper, new BlockPos(7, 1, 1));
        baby.setAge(0.5);
        adult.setAge(30.0);
        elder.setAge(65.0);
        helper.assertTrue(baby.stage() == LifeStage.BABY && adult.stage() == LifeStage.ADULT && elder.stage() == LifeStage.ELDER,
                "each has the stage of their age");
        helper.assertTrue(baby.getScale() < 0.5F && Math.abs(adult.getScale() - 1.0F) < 1.0E-3F, "a baby is small, an adult full size");
        List.of(baby, adult, elder).forEach(TownsfolkEntity::discard);
        helper.succeed();
    }
}
