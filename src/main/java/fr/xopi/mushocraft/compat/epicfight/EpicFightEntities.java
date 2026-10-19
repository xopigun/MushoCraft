package fr.xopi.mushocraft.compat.epicfight;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.core.MushoEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.gameasset.Armatures;

/**
 * Makes MushoCraft's humanoids Epic Fight entities: their patch (AI and animations) and their skeleton.
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
        });
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Armatures.registerEntityTypeArmature(MushoEntities.SWORD_MASTER.get(), Armatures.BIPED);
            Armatures.registerEntityTypeArmature(MushoEntities.MAGIC_TUTOR.get(), Armatures.BIPED);
        });
    }
}
