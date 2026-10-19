package fr.xopi.mushocraft.gametest.showcase;

import fr.xopi.mushocraft.compat.epicfight.BearAnimations;
import fr.xopi.mushocraft.compat.epicfight.BoarAnimations;
import fr.xopi.mushocraft.compat.epicfight.CanineAnimations;
import fr.xopi.mushocraft.compat.epicfight.EpicFightAnimations;
import fr.xopi.mushocraft.compat.epicfight.GoblinAnimations;
import fr.xopi.mushocraft.compat.epicfight.MonsterAnimations;
import fr.xopi.mushocraft.core.MushoEntities;
import fr.xopi.mushocraft.gametest.showcase.GalleryDirector.Exhibit;
import fr.xopi.mushocraft.monster.MushoMonster;
import fr.xopi.mushocraft.monster.TerminateBoar;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Husk;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The monsters in the gallery: each one posed side-on, every animation played in turn, then hunting the dummy.
 * Run alone with {@code -Dmushocraft.gallery.only=monster_}.
 */
final class MonsterExhibits {
    private MonsterExhibits() {
    }

    static void add(List<Exhibit> all) {
        canine(all, "assault_dog", MushoEntities.ASSAULT_DOG.get(), () -> MonsterAnimations.ASSAULT_DOG);
        canine(all, "pax_coyote", MushoEntities.PAX_COYOTE.get(), () -> MonsterAnimations.PAX_COYOTE);
        Supplier<BoarAnimations> boar = () -> MonsterAnimations.TERMINATE_BOAR;
        Map<String, Supplier<AnimationAccessor<? extends StaticAnimation>>> boarClips = new LinkedHashMap<>();
        boarClips.put("walk", () -> boar.get().walk());
        boarClips.put("run", () -> boar.get().run());
        boarClips.put("swipe_r", () -> boar.get().swipeRight());
        boarClips.put("swipe_l", () -> boar.get().swipeLeft());
        boarClips.put("smash", () -> boar.get().smash());
        boarClips.put("gore", () -> boar.get().gore());
        boarClips.put("roar", () -> boar.get().roar());
        boarClips.put("hit", () -> boar.get().hit());
        boarClips.put("death", () -> boar.get().death());
        species(all, "terminate_boar", MushoEntities.TERMINATE_BOAR.get(), boarClips, 6.0, MonsterExhibits::boarHunt);
        Supplier<BearAnimations> bear = () -> MonsterAnimations.LUSTER_GRIZZLY;
        Map<String, Supplier<AnimationAccessor<? extends StaticAnimation>>> bearClips = new LinkedHashMap<>();
        bearClips.put("walk", () -> bear.get().walk());
        bearClips.put("run", () -> bear.get().run());
        bearClips.put("maul", () -> bear.get().maul());
        bearClips.put("swipe", () -> bear.get().swipe());
        bearClips.put("roar", () -> bear.get().roar());
        bearClips.put("hit", () -> bear.get().hit());
        bearClips.put("death", () -> bear.get().death());
        species(all, "luster_grizzly", MushoEntities.LUSTER_GRIZZLY.get(), bearClips, 5.0,
                player -> packHunt(player, MushoEntities.LUSTER_GRIZZLY.get()));
        Supplier<GoblinAnimations> goblin = () -> MonsterAnimations.GOBLIN;
        Map<String, Supplier<AnimationAccessor<? extends StaticAnimation>>> goblinClips = new LinkedHashMap<>();
        goblinClips.put("walk", () -> goblin.get().walk());
        goblinClips.put("run", () -> goblin.get().run());
        goblinClips.put("swing", () -> goblin.get().swing());
        goblinClips.put("overhead", () -> goblin.get().overhead());
        goblinClips.put("shout", () -> goblin.get().shout());
        goblinClips.put("hit", () -> goblin.get().hit());
        goblinClips.put("death", () -> goblin.get().death());
        species(all, "goblin", MushoEntities.GOBLIN.get(), goblinClips, 3.2, player -> packHunt(player, MushoEntities.GOBLIN.get()));
    }

    private static <T extends MushoMonster> void canine(List<Exhibit> all, String name, EntityType<T> type,
                                                        Supplier<CanineAnimations> animations) {
        Map<String, Supplier<AnimationAccessor<? extends StaticAnimation>>> clips = new LinkedHashMap<>();
        clips.put("walk", () -> animations.get().walk());
        clips.put("run", () -> animations.get().run());
        clips.put("bite", () -> animations.get().bite());
        clips.put("pounce", () -> animations.get().pounce());
        clips.put("howl", () -> animations.get().howl());
        clips.put("hit", () -> animations.get().hit());
        clips.put("death", () -> animations.get().death());
        species(all, name, type, clips, 3.6, player -> packHunt(player, type));
    }

    private static <T extends MushoMonster> void species(List<Exhibit> all, String name, EntityType<T> type,
                                                         Map<String, Supplier<AnimationAccessor<? extends StaticAnimation>>> clips,
                                                         double distance, Consumer<ServerPlayer> hunt) {
        String prefix = "monster_" + name + "_";
        all.add(new Exhibit(prefix + "pose", player -> posed(player, type, null, distance), 30, new int[]{20}));
        clips.forEach((clip, accessor) -> all.add(new Exhibit(prefix + clip,
                player -> posed(player, type, accessor.get(), distance), 44, new int[]{4, 10, 16, 26})));
        all.add(new Exhibit(prefix + "hunt", hunt, 200, new int[]{20, 50, 80, 110, 140, 170}));
    }

    /** One monster side-on, a few blocks ahead and off the crosshair, held in place but animated. */
    private static <T extends MushoMonster> void posed(ServerPlayer player, EntityType<T> type,
                                                       AnimationAccessor<? extends StaticAnimation> clip, double distance) {
        ServerLevel level = player.serverLevel();
        T monster = type.create(level);
        if (monster == null) {
            return;
        }
        monster.moveTo(player.getX() - distance * 0.5, player.getY(), player.getZ() + distance, 90.0F, 0.0F);
        monster.setYBodyRot(90.0F);
        monster.setYHeadRot(90.0F);
        monster.setNoAi(true);
        level.addFreshEntity(monster);
        if (clip != null) {
            EpicFightAnimations.play(monster, clip);
        }
    }

    private static Husk prey(ServerPlayer player) {
        Husk prey = player.serverLevel().getEntitiesOfClass(Husk.class, player.getBoundingBox().inflate(20.0)).stream()
                .findFirst().orElse(null);
        if (prey != null) {
            prey.moveTo(player.getX(), player.getY(), player.getZ() + 7.0, 180.0F, 0.0F);
        }
        return prey;
    }

    /** A pack of four spawned together (one leader) sets upon the dummy. */
    private static <T extends MushoMonster> void packHunt(ServerPlayer player, EntityType<T> type) {
        ServerLevel level = player.serverLevel();
        Husk prey = prey(player);
        if (prey == null) {
            return;
        }
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

    /** A Terminate Boar and the dogs it comes with, as met in the wild, set upon the dummy. */
    private static void boarHunt(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Husk prey = prey(player);
        TerminateBoar boar = MushoEntities.TERMINATE_BOAR.get().create(level);
        if (prey == null || boar == null) {
            return;
        }
        boar.moveTo(player.getX(), player.getY(), player.getZ() + 18, 180.0F, 0.0F);
        boar.finalizeSpawn(level, level.getCurrentDifficultyAt(boar.blockPosition()), MobSpawnType.SPAWN_EGG, null);
        level.addFreshEntity(boar);
        boar.setTarget(prey);
    }
}
