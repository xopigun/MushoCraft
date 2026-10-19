package fr.xopi.mushocraft.compat.epicfight;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.core.MushoEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.gameasset.Armatures;

/**
 * Makes MushoCraft's humanoids and monsters Epic Fight entities: their patch (AI and animations) and their skeleton.
 */
@EventBusSubscriber(modid = MushoCraft.MODID)
public final class EpicFightEntities {
    private EpicFightEntities() {
    }

    /**
     * Must run from the mod constructor: Epic Fight posts its patch registry during common setup.
     */
    public static void registerPatches() {
        EpicFightEventHooks.Registry.ENTITY_PATCH.registerEvent(event -> {
            event.registerEntityPatch(MushoEntities.SWORD_MASTER.get(), TeacherPatch::new);
            event.registerEntityPatch(MushoEntities.MAGIC_TUTOR.get(), TeacherPatch::new);
            event.registerEntityPatch(MushoEntities.ASSAULT_DOG.get(), dog -> new CaninePatch<>(dog, () -> MonsterAnimations.ASSAULT_DOG));
            event.registerEntityPatch(MushoEntities.PAX_COYOTE.get(), coyote -> new CaninePatch<>(coyote, () -> MonsterAnimations.PAX_COYOTE));
        });
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Armatures.registerEntityTypeArmature(MushoEntities.SWORD_MASTER.get(), Armatures.BIPED);
            Armatures.registerEntityTypeArmature(MushoEntities.MAGIC_TUTOR.get(), Armatures.BIPED);
            Armatures.registerEntityTypeArmature(MushoEntities.ASSAULT_DOG.get(), MonsterAnimations.ASSAULT_DOG_ARMATURE);
            Armatures.registerEntityTypeArmature(MushoEntities.PAX_COYOTE.get(), MonsterAnimations.PAX_COYOTE_ARMATURE);
        });
    }
}
