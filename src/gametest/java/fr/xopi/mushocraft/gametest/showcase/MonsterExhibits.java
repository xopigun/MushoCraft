package fr.xopi.mushocraft.gametest.showcase;

import fr.xopi.mushocraft.compat.epicfight.ArmoredWarriorAnimations;
import fr.xopi.mushocraft.compat.epicfight.BearAnimations;
import fr.xopi.mushocraft.compat.epicfight.BoarAnimations;
import fr.xopi.mushocraft.compat.epicfight.CanineAnimations;
import fr.xopi.mushocraft.compat.epicfight.EpicFightAnimations;
import fr.xopi.mushocraft.compat.epicfight.GoblinAnimations;
import fr.xopi.mushocraft.compat.epicfight.MadSkullAnimations;
import fr.xopi.mushocraft.compat.epicfight.DevouringDevilAnimations;
import fr.xopi.mushocraft.compat.epicfight.SnowDrakeAnimations;
import fr.xopi.mushocraft.compat.epicfight.TarantulaAnimations;
import fr.xopi.mushocraft.compat.epicfight.WraithAnimations;
import fr.xopi.mushocraft.compat.epicfight.MonsterAnimations;
import fr.xopi.mushocraft.core.MushoEntities;
import fr.xopi.mushocraft.gametest.showcase.GalleryDirector.Exhibit;
import fr.xopi.mushocraft.monster.MushoMonster;
import fr.xopi.mushocraft.monster.TerminateBoar;
import fr.xopi.mushocraft.monster.Wraith;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
        Supplier<WraithAnimations> wraith = () -> MonsterAnimations.WRAITH;
        Map<String, Supplier<AnimationAccessor<? extends StaticAnimation>>> wraithClips = new LinkedHashMap<>();
        wraithClips.put("walk", () -> wraith.get().walk());
        wraithClips.put("claw", () -> wraith.get().claw());
        wraithClips.put("cast", () -> wraith.get().cast());
        wraithClips.put("raise_dead", () -> wraith.get().raiseDead());
        wraithClips.put("death", () -> wraith.get().death());
        species(all, "wraith", MushoEntities.WRAITH.get(), wraithClips, 3.6, MonsterExhibits::wraithHunt);
        Supplier<ArmoredWarriorAnimations> warrior = () -> MonsterAnimations.ARMORED_WARRIOR;
        Map<String, Supplier<AnimationAccessor<? extends StaticAnimation>>> warriorClips = new LinkedHashMap<>();
        warriorClips.put("walk", () -> warrior.get().walk());
        warriorClips.put("cross_slash", () -> warrior.get().crossSlash());
        warriorClips.put("overhead", () -> warrior.get().overhead());
        warriorClips.put("parry", () -> warrior.get().parry());
        warriorClips.put("death", () -> warrior.get().death());
        species(all, "armored_warrior", MushoEntities.ARMORED_WARRIOR.get(), warriorClips, 3.8,
                player -> packHunt(player, MushoEntities.ARMORED_WARRIOR.get()));
        Supplier<MadSkullAnimations> skull = () -> MonsterAnimations.MAD_SKULL;
        Map<String, Supplier<AnimationAccessor<? extends StaticAnimation>>> skullClips = new LinkedHashMap<>();
        skullClips.put("walk", () -> skull.get().walk());
        skullClips.put("slam", () -> skull.get().slam());
        skullClips.put("throw", () -> skull.get().throwMud());
        skullClips.put("cast", () -> skull.get().cast());
        skullClips.put("death", () -> skull.get().death());
        species(all, "mad_skull", MushoEntities.MAD_SKULL.get(), skullClips, 6.0, player -> soloHunt(player, MushoEntities.MAD_SKULL.get()));
        Supplier<TarantulaAnimations> spider = () -> MonsterAnimations.TARANTULA_DEATHLORD;
        Map<String, Supplier<AnimationAccessor<? extends StaticAnimation>>> spiderClips = new LinkedHashMap<>();
        spiderClips.put("walk", () -> spider.get().walk());
        spiderClips.put("bite", () -> spider.get().bite());
        spiderClips.put("spit", () -> spider.get().spit());
        spiderClips.put("death", () -> spider.get().death());
        species(all, "tarantula_deathlord", MushoEntities.TARANTULA_DEATHLORD.get(), spiderClips, 4.5,
                player -> soloHunt(player, MushoEntities.TARANTULA_DEATHLORD.get()));
        Supplier<SnowDrakeAnimations> drake = () -> MonsterAnimations.SNOW_DRAKE;
        Map<String, Supplier<AnimationAccessor<? extends StaticAnimation>>> drakeClips = new LinkedHashMap<>();
        drakeClips.put("walk", () -> drake.get().walk());
        drakeClips.put("bite", () -> drake.get().bite());
        drakeClips.put("tail_sweep", () -> drake.get().tailSweep());
        drakeClips.put("rear", () -> drake.get().rear());
        drakeClips.put("death", () -> drake.get().death());
        species(all, "snow_drake", MushoEntities.SNOW_DRAKE.get(), drakeClips, 5.0, player -> packHunt(player, MushoEntities.SNOW_DRAKE.get()));
        Supplier<DevouringDevilAnimations> devil = () -> MonsterAnimations.DEVOURING_DEVIL;
        Map<String, Supplier<AnimationAccessor<? extends StaticAnimation>>> devilClips = new LinkedHashMap<>();
        devilClips.put("run", () -> devil.get().run());
        devilClips.put("claw", () -> devil.get().claw());
        devilClips.put("pounce", () -> devil.get().pounce());
        devilClips.put("bite", () -> devil.get().bite());
        devilClips.put("death", () -> devil.get().death());
        species(all, "devouring_devil", MushoEntities.DEVOURING_DEVIL.get(), devilClips, 3.8,
                player -> packHunt(player, MushoEntities.DEVOURING_DEVIL.get()));
        all.add(new Exhibit("monster_executioner_pose", player -> executionerPose(player), 40, new int[]{30}));
        all.add(new Exhibit("monster_executioner_hunt", player -> soloHunt(player, MushoEntities.EXECUTIONER.get()), 200,
                new int[]{20, 50, 80, 110, 140, 170}));
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
            // monsters only hunt what they can hurt: the dummy stays standing thanks to a huge health pool instead
            prey.setInvulnerable(false);
            prey.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);
            prey.setHealth(prey.getMaxHealth());
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

    /** The Executioner, armed, standing beside the camera's axis (Epic Fight's own animations, nothing to show clip by clip). */
    private static void executionerPose(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        var executioner = MushoEntities.EXECUTIONER.get().create(level);
        if (executioner == null) {
            return;
        }
        executioner.moveTo(player.getX() + 1.6, player.getY(), player.getZ() + 4.0, 200.0F, 0.0F);
        executioner.setYHeadRot(200.0F);
        executioner.setYBodyRot(200.0F);
        executioner.arm();
        executioner.setNoAi(true);
        level.addFreshEntity(executioner);
    }

    /** One monster sets upon the dummy from afar. */
    private static <T extends MushoMonster> void soloHunt(ServerPlayer player, EntityType<T> type) {
        ServerLevel level = player.serverLevel();
        Husk prey = prey(player);
        T monster = type.create(level);
        if (prey == null || monster == null) {
            return;
        }
        monster.moveTo(player.getX() + 2, player.getY(), player.getZ() + 18, 180.0F, 0.0F);
        monster.finalizeSpawn(level, level.getCurrentDifficultyAt(monster.blockPosition()), MobSpawnType.EVENT, null);
        level.addFreshEntity(monster);
        monster.setTarget(prey);
    }

    /** A Deadly Wraith sets upon the dummy: it raises its skeletons and casts at it. */
    private static void wraithHunt(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Husk prey = prey(player);
        Wraith wraith = MushoEntities.WRAITH.get().create(level);
        if (prey == null || wraith == null) {
            return;
        }
        wraith.moveTo(player.getX() + 3, player.getY(), player.getZ() + 14, 180.0F, 0.0F);
        wraith.setDeadly(true);
        level.addFreshEntity(wraith);
        wraith.setTarget(prey);
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
