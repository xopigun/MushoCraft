package fr.xopi.mushocraft.village;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.life.LifeStage;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

/**
 * Which outfit a person wears: one skin per sex, age group and variant (textures/entity/townsfolk, drawn for
 * MushoCraft by tools/people/townsfolk_skins.py). A person keeps the same variant number all their life; as they
 * grow up the number picks among the outfits of their new age group.
 */
public final class Appearance {
    private Appearance() {
    }

    /** Age groups that share a set of outfits. */
    public enum Group {
        CHILD(3),
        ADULT(6),
        ELDER(2);

        private final int variants;

        Group(int variants) {
            this.variants = variants;
        }

        public int variants() {
            return variants;
        }

        public static Group of(LifeStage stage) {
            return switch (stage) {
                case BABY, CHILD -> CHILD;
                case ADOLESCENT, ADULT -> ADULT;
                case ELDER -> ELDER;
            };
        }
    }

    public static ResourceLocation skin(Sex sex, LifeStage stage, int variant) {
        Group group = Group.of(stage);
        return MushoCraft.id("textures/entity/townsfolk/%s_%s_%d.png".formatted(sex.getSerializedName(),
                group.name().toLowerCase(Locale.ROOT), Math.floorMod(variant, group.variants())));
    }
}
