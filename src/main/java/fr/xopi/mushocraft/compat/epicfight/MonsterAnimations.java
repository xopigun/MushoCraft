package fr.xopi.mushocraft.compat.epicfight;

import fr.xopi.mushocraft.MushoCraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import yesman.epicfight.api.animation.AnimationManager.AnimationBuilder;
import yesman.epicfight.api.animation.AnimationManager.AnimationRegistryEvent;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.gameasset.Armatures;

/**
 * Skeletons and animations of MushoCraft's monsters, all made for the mod (tools/bestiary). Files live under
 * {@code assets/mushocraft/animmodels}: the skeleton and mesh in {@code entity/<species>.json}, the animations in
 * {@code animations/<species>/}.
 */
@EventBusSubscriber(modid = MushoCraft.MODID)
public final class MonsterAnimations {
    public static final AssetAccessor<Armature> ASSAULT_DOG_ARMATURE = armature("assault_dog");
    public static final AssetAccessor<Armature> PAX_COYOTE_ARMATURE = armature("pax_coyote");
    public static final AssetAccessor<Armature> TERMINATE_BOAR_ARMATURE = armature("terminate_boar");
    public static final AssetAccessor<Armature> LUSTER_GRIZZLY_ARMATURE = armature("luster_grizzly");
    public static final AssetAccessor<Armature> GOBLIN_ARMATURE = armature("goblin");
    public static final AssetAccessor<Armature> WRAITH_ARMATURE = armature("wraith");
    public static final AssetAccessor<Armature> ARMORED_WARRIOR_ARMATURE = armature("armored_warrior");
    public static final AssetAccessor<Armature> MAD_SKULL_ARMATURE = armature("mad_skull");
    public static final AssetAccessor<Armature> TARANTULA_DEATHLORD_ARMATURE = armature("tarantula_deathlord");
    public static final AssetAccessor<Armature> SNOW_DRAKE_ARMATURE = armature("snow_drake");
    public static final AssetAccessor<Armature> DEVOURING_DEVIL_ARMATURE = armature("devouring_devil");
    public static final AssetAccessor<Armature> IRON_CRAWLER_ARMATURE = armature("iron_crawler");

    public static CanineAnimations ASSAULT_DOG;
    public static CanineAnimations PAX_COYOTE;
    public static BoarAnimations TERMINATE_BOAR;
    public static BearAnimations LUSTER_GRIZZLY;
    public static GoblinAnimations GOBLIN;
    public static WraithAnimations WRAITH;
    public static ArmoredWarriorAnimations ARMORED_WARRIOR;
    public static MadSkullAnimations MAD_SKULL;
    public static TarantulaAnimations TARANTULA_DEATHLORD;
    public static SnowDrakeAnimations SNOW_DRAKE;
    public static DevouringDevilAnimations DEVOURING_DEVIL;
    public static IronCrawlerAnimations IRON_CRAWLER;

    private MonsterAnimations() {
    }

    private static AssetAccessor<Armature> armature(String species) {
        return Armatures.getOrCreate(MushoCraft.id("entity/" + species), Armature::new);
    }

    @SubscribeEvent
    public static void onRegisterAnimations(AnimationRegistryEvent event) {
        event.newBuilder(MushoCraft.MODID, MonsterAnimations::build);
    }

    private static void build(AnimationBuilder builder) {
        ASSAULT_DOG = CanineAnimations.register(builder, "assault_dog", ASSAULT_DOG_ARMATURE);
        PAX_COYOTE = CanineAnimations.register(builder, "pax_coyote", PAX_COYOTE_ARMATURE);
        TERMINATE_BOAR = BoarAnimations.register(builder, "terminate_boar", TERMINATE_BOAR_ARMATURE);
        LUSTER_GRIZZLY = BearAnimations.register(builder, "luster_grizzly", LUSTER_GRIZZLY_ARMATURE);
        GOBLIN = GoblinAnimations.register(builder, "goblin", GOBLIN_ARMATURE);
        WRAITH = WraithAnimations.register(builder, "wraith", WRAITH_ARMATURE);
        ARMORED_WARRIOR = ArmoredWarriorAnimations.register(builder, "armored_warrior", ARMORED_WARRIOR_ARMATURE);
        MAD_SKULL = MadSkullAnimations.register(builder, "mad_skull", MAD_SKULL_ARMATURE);
        TARANTULA_DEATHLORD = TarantulaAnimations.register(builder, "tarantula_deathlord", TARANTULA_DEATHLORD_ARMATURE);
        SNOW_DRAKE = SnowDrakeAnimations.register(builder, "snow_drake", SNOW_DRAKE_ARMATURE);
        DEVOURING_DEVIL = DevouringDevilAnimations.register(builder, "devouring_devil", DEVOURING_DEVIL_ARMATURE);
        IRON_CRAWLER = IronCrawlerAnimations.register(builder, "iron_crawler", IRON_CRAWLER_ARMATURE);
    }
}
