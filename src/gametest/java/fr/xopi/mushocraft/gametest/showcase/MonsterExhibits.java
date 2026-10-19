package fr.xopi.mushocraft.gametest.showcase;

import fr.xopi.mushocraft.compat.epicfight.CanineAnimations;
import fr.xopi.mushocraft.compat.epicfight.EpicFightAnimations;
import fr.xopi.mushocraft.compat.epicfight.MonsterAnimations;
import fr.xopi.mushocraft.core.MushoEntities;
import fr.xopi.mushocraft.gametest.showcase.GalleryDirector.Exhibit;
import fr.xopi.mushocraft.monster.MushoMonster;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Husk;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The monsters in the gallery: each one posed side-on, every animation played in turn, then a pack hunting the dummy.
 * Run alone with {@code -Dmushocraft.gallery.only=monster_}.
 */
final class MonsterExhibits {
    private static final String[] CANINE_CLIPS = {"walk", "run", "bite", "pounce", "howl", "hit", "death"};
    private static final List<Function<CanineAnimations, AnimationAccessor<? extends StaticAnimation>>> CANINE = List.of(
            CanineAnimations::walk, CanineAnimations::run, CanineAnimations::bite, CanineAnimations::pounce,
            CanineAnimations::howl, CanineAnimations::hit, CanineAnimations::death);

    private MonsterExhibits() {
    }

    static void add(List<Exhibit> all) {
        canine(all, "assault_dog", MushoEntities.ASSAULT_DOG.get(), () -> MonsterAnimations.ASSAULT_DOG);
    }

    private static <T extends MushoMonster> void canine(List<Exhibit> all, String species, EntityType<T> type,
                                                        Supplier<CanineAnimations> animations) {
        String prefix = "monster_" + species + "_";
        all.add(new Exhibit(prefix + "pose", player -> posed(player, type, null), 30, new int[]{20}));
        for (int i = 0; i < CANINE.size(); i++) {
            Function<CanineAnimations, AnimationAccessor<? extends StaticAnimation>> clip = CANINE.get(i);
            all.add(new Exhibit(prefix + CANINE_CLIPS[i], player -> posed(player, type, clip.apply(animations.get())),
                    40, new int[]{4, 9, 14, 22}));
        }
        all.add(new Exhibit(prefix + "hunt", player -> hunt(player, type), 200, new int[]{20, 50, 80, 110, 140, 170}));
    }

    /** One monster side-on, a few blocks ahead, held in place but animated. */
    private static <T extends MushoMonster> void posed(ServerPlayer player, EntityType<T> type,
                                                       AnimationAccessor<? extends StaticAnimation> clip) {
        ServerLevel level = player.serverLevel();
        T monster = type.create(level);
        if (monster == null) {
            return;
        }
        monster.moveTo(player.getX() - 1.8, player.getY(), player.getZ() + 3.6, 90.0F, 0.0F);
        monster.setYBodyRot(90.0F);
        monster.setYHeadRot(90.0F);
        monster.setNoAi(true);
        level.addFreshEntity(monster);
        if (clip != null) {
            EpicFightAnimations.play(monster, clip);
        }
    }

    /** A pack of four spawned together (one leader) sets upon the dummy. */
    private static <T extends MushoMonster> void hunt(ServerPlayer player, EntityType<T> type) {
        ServerLevel level = player.serverLevel();
        Husk prey = level.getEntitiesOfClass(Husk.class, player.getBoundingBox().inflate(20.0)).stream().findFirst().orElse(null);
        if (prey == null) {
            return;
        }
        prey.moveTo(player.getX(), player.getY(), player.getZ() + 6.0, 180.0F, 0.0F);
        SpawnGroupData group = null;
        for (int i = 0; i < 4; i++) {
            T monster = type.create(level);
            if (monster == null) {
                return;
            }
            monster.moveTo(player.getX() - 6 + i * 4, player.getY(), player.getZ() + 16, 180.0F, 0.0F);
            group = monster.finalizeSpawn(level, level.getCurrentDifficultyAt(monster.blockPosition()), MobSpawnType.EVENT, group);
            level.addFreshEntity(monster);
            monster.setTarget(prey);
        }
    }
}
