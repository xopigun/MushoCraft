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
            event.registerEntityPatch(MushoEntities.TERMINATE_BOAR.get(), boar -> new BoarPatch<>(boar, () -> MonsterAnimations.TERMINATE_BOAR));
            event.registerEntityPatch(MushoEntities.LUSTER_GRIZZLY.get(), monster -> new BearPatch<>(monster, () -> MonsterAnimations.LUSTER_GRIZZLY));
            event.registerEntityPatch(MushoEntities.GOBLIN.get(), monster -> new GoblinPatch<>(monster, () -> MonsterAnimations.GOBLIN));
            event.registerEntityPatch(MushoEntities.WRAITH.get(), monster -> new WraithPatch<>(monster, () -> MonsterAnimations.WRAITH));
            event.registerEntityPatch(MushoEntities.ARMORED_WARRIOR.get(), monster -> new ArmoredWarriorPatch<>(monster, () -> MonsterAnimations.ARMORED_WARRIOR));
        });
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Armatures.registerEntityTypeArmature(MushoEntities.SWORD_MASTER.get(), Armatures.BIPED);
            Armatures.registerEntityTypeArmature(MushoEntities.MAGIC_TUTOR.get(), Armatures.BIPED);
            Armatures.registerEntityTypeArmature(MushoEntities.ASSAULT_DOG.get(), MonsterAnimations.ASSAULT_DOG_ARMATURE);
            Armatures.registerEntityTypeArmature(MushoEntities.PAX_COYOTE.get(), MonsterAnimations.PAX_COYOTE_ARMATURE);
            Armatures.registerEntityTypeArmature(MushoEntities.TERMINATE_BOAR.get(), MonsterAnimations.TERMINATE_BOAR_ARMATURE);
            Armatures.registerEntityTypeArmature(MushoEntities.LUSTER_GRIZZLY.get(), MonsterAnimations.LUSTER_GRIZZLY_ARMATURE);
            Armatures.registerEntityTypeArmature(MushoEntities.GOBLIN.get(), MonsterAnimations.GOBLIN_ARMATURE);
            Armatures.registerEntityTypeArmature(MushoEntities.WRAITH.get(), MonsterAnimations.WRAITH_ARMATURE);
            Armatures.registerEntityTypeArmature(MushoEntities.ARMORED_WARRIOR.get(), MonsterAnimations.ARMORED_WARRIOR_ARMATURE);
        });
    }
}
