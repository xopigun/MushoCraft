package fr.xopi.mushocraft.gametest.showcase;

import fr.xopi.mushocraft.core.MushoEntities;
import fr.xopi.mushocraft.gametest.showcase.GalleryDirector.Exhibit;
import fr.xopi.mushocraft.village.Household;
import fr.xopi.mushocraft.village.TownsfolkEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * The village folk in the gallery: households raised in front of the camera, every age side by side.
 */
final class VillageExhibits {
    private VillageExhibits() {
    }

    static void add(List<Exhibit> all) {
        for (int i = 0; i < 3; i++) {
            all.add(new Exhibit("village_household_" + i, VillageExhibits::household, 40, new int[]{30}));
        }
    }

    /** A household gathered a few blocks ahead, standing still so everyone can be seen. */
    private static void household(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        level.getEntitiesOfClass(TownsfolkEntity.class, player.getBoundingBox().inflate(32.0)).forEach(TownsfolkEntity::discard);
        TownsfolkEntity founder = MushoEntities.TOWNSFOLK.get().create(level);
        if (founder == null) {
            return;
        }
        founder.moveTo(player.getX(), player.getY(), player.getZ() + 5.0, 180.0F, 0.0F);
        level.addFreshEntity(founder);
        List<TownsfolkEntity> family = Household.found(founder);
        for (int i = 0; i < family.size(); i++) {
            TownsfolkEntity member = family.get(i);
            member.moveTo(player.getX() + (i - (family.size() - 1) / 2.0) * 1.3, player.getY(), player.getZ() + 5.0, 180.0F, 0.0F);
            member.setYHeadRot(180.0F);
            member.setYBodyRot(180.0F);
            member.setNoAi(true);
        }
    }
}
