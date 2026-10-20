package fr.xopi.mushocraft.village;

import fr.xopi.mushocraft.core.MushoEntities;
import fr.xopi.mushocraft.life.LifeStage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Raises the family that lives in a village house, around its founder (Factory). Households of the countryside are
 * mostly a couple and their children, now and then with a grandparent, an old couple whose children have left, or a
 * widow or widower living alone. Everyone shares the founder's home and family name.
 */
public final class Household {
    private static final double ADULT = LifeStage.ADULT.startAge();
    private static final double ELDER = LifeStage.ELDER.startAge();

    private Household() {
    }

    /** What a household is made of. */
    enum Kind {
        FAMILY(70),
        OLD_COUPLE(18),
        ALONE(12);

        private final int weight;

        Kind(int weight) {
            this.weight = weight;
        }

        static Kind roll(RandomSource random) {
            int total = 0;
            for (Kind kind : values()) {
                total += kind.weight;
            }
            int pick = random.nextInt(total);
            for (Kind kind : values()) {
                pick -= kind.weight;
                if (pick < 0) {
                    return kind;
                }
            }
            return FAMILY;
        }
    }

    /** Raises the household of {@code founder} in its house, and returns everyone in it (the founder first). */
    public static List<TownsfolkEntity> found(TownsfolkEntity founder) {
        RandomSource random = founder.getRandom();
        String family = PersonNames.family(random);
        BlockPos home = founder.blockPosition();
        List<TownsfolkEntity> members = new ArrayList<>();
        switch (Kind.roll(random)) {
            case ALONE -> {
                Sex sex = Sex.random(random);
                founder.becomePerson(sex, PersonNames.given(sex, random), family, between(random, ELDER + 5, ELDER + 25));
                founder.settleAt(home);
                members.add(founder);
            }
            case OLD_COUPLE -> {
                double age = between(random, ELDER + 2, ELDER + 22);
                founder.becomePerson(Sex.MALE, PersonNames.given(Sex.MALE, random), family, age);
                founder.settleAt(home);
                members.add(founder);
                members.add(spouseOf(founder, home, family, Mth.clamp(age + between(random, -6, 3), ELDER, ELDER + 30)));
            }
            case FAMILY -> {
                double fatherAge = between(random, ADULT + 7, ADULT + 30);
                founder.becomePerson(Sex.MALE, PersonNames.given(Sex.MALE, random), family, fatherAge);
                founder.settleAt(home);
                members.add(founder);
                TownsfolkEntity mother = spouseOf(founder, home, family, Math.max(ADULT + 3, fatherAge + between(random, -5, 2)));
                members.add(mother);
                double eldestPossible = Math.min(mother.ageInYears() - (ADULT + 2), ADULT - 1);
                int children = eldestPossible <= 0 ? 0 : random.nextInt(4);
                for (int i = 0; i < children; i++) {
                    members.add(childOf(founder, mother, home, family, between(random, 0.0, eldestPossible)));
                }
                if (random.nextInt(4) == 0) {
                    Sex sex = Sex.random(random);
                    TownsfolkEntity grandparent = newcomer(founder, home);
                    grandparent.becomePerson(sex, PersonNames.given(sex, random), family, Math.max(ELDER + 3, fatherAge + between(random, 22, 32)));
                    grandparent.addChild(founder.getUUID());
                    founder.addParent(grandparent.getUUID());
                    members.add(grandparent);
                }
            }
        }
        return members;
    }

    private static TownsfolkEntity spouseOf(TownsfolkEntity partner, BlockPos home, String family, double age) {
        RandomSource random = partner.getRandom();
        Sex sex = partner.sex().opposite();
        TownsfolkEntity spouse = newcomer(partner, home);
        spouse.becomePerson(sex, PersonNames.given(sex, random), family, age);
        spouse.marry(partner.getUUID());
        partner.marry(spouse.getUUID());
        return spouse;
    }

    private static TownsfolkEntity childOf(TownsfolkEntity father, TownsfolkEntity mother, BlockPos home, String family, double age) {
        RandomSource random = father.getRandom();
        Sex sex = Sex.random(random);
        TownsfolkEntity child = newcomer(father, home);
        child.becomePerson(sex, PersonNames.given(sex, random), family, age);
        for (TownsfolkEntity parent : List.of(father, mother)) {
            child.addParent(parent.getUUID());
            parent.addChild(child.getUUID());
        }
        return child;
    }

    /** A new member of the household, standing at home. */
    private static TownsfolkEntity newcomer(TownsfolkEntity founder, BlockPos home) {
        ServerLevel level = (ServerLevel) founder.level();
        TownsfolkEntity person = MushoEntities.TOWNSFOLK.get().create(level);
        if (person == null) {
            throw new IllegalStateException("Townsfolk could not be created");
        }
        person.moveTo(home.getX() + 0.5 + founder.getRandom().nextGaussian() * 0.6, home.getY(),
                home.getZ() + 0.5 + founder.getRandom().nextGaussian() * 0.6, founder.getRandom().nextFloat() * 360.0F, 0.0F);
        person.settleAt(home);
        level.addFreshEntity(person);
        return person;
    }

    private static double between(RandomSource random, double low, double high) {
        return low + random.nextDouble() * (high - low);
    }
}
