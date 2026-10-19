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

    public static CanineAnimations ASSAULT_DOG;

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
    }
}
