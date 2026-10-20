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
            event.registerEntityPatch(MushoEntities.TOWNSFOLK.get(), TownsfolkPatch::new);
            event.registerEntityPatch(MushoEntities.MAGIC_TUTOR.get(), TeacherPatch::new);
            event.registerEntityPatch(MushoEntities.ASSAULT_DOG.get(), dog -> new CaninePatch<>(dog, () -> MonsterAnimations.ASSAULT_DOG));
            event.registerEntityPatch(MushoEntities.PAX_COYOTE.get(), coyote -> new CaninePatch<>(coyote, () -> MonsterAnimations.PAX_COYOTE));
            event.registerEntityPatch(MushoEntities.TERMINATE_BOAR.get(), boar -> new BoarPatch<>(boar, () -> MonsterAnimations.TERMINATE_BOAR));
            event.registerEntityPatch(MushoEntities.LUSTER_GRIZZLY.get(), monster -> new BearPatch<>(monster, () -> MonsterAnimations.LUSTER_GRIZZLY));
            event.registerEntityPatch(MushoEntities.GOBLIN.get(), monster -> new GoblinPatch<>(monster, () -> MonsterAnimations.GOBLIN));
            event.registerEntityPatch(MushoEntities.WRAITH.get(), monster -> new WraithPatch<>(monster, () -> MonsterAnimations.WRAITH));
            event.registerEntityPatch(MushoEntities.ARMORED_WARRIOR.get(), monster -> new ArmoredWarriorPatch<>(monster, () -> MonsterAnimations.ARMORED_WARRIOR));
            event.registerEntityPatch(MushoEntities.MAD_SKULL.get(), monster -> new MadSkullPatch<>(monster, () -> MonsterAnimations.MAD_SKULL));
            event.registerEntityPatch(MushoEntities.TARANTULA_DEATHLORD.get(), monster -> new TarantulaPatch<>(monster, () -> MonsterAnimations.TARANTULA_DEATHLORD));
            event.registerEntityPatch(MushoEntities.SNOW_DRAKE.get(), monster -> new SnowDrakePatch<>(monster, () -> MonsterAnimations.SNOW_DRAKE));
            event.registerEntityPatch(MushoEntities.DEVOURING_DEVIL.get(), monster -> new DevouringDevilPatch<>(monster, () -> MonsterAnimations.DEVOURING_DEVIL));
            event.registerEntityPatch(MushoEntities.EXECUTIONER.get(), ExecutionerPatch::new);
            event.registerEntityPatch(MushoEntities.IRON_CRAWLER.get(), monster -> new IronCrawlerPatch<>(monster, () -> MonsterAnimations.IRON_CRAWLER));
        });
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Armatures.registerEntityTypeArmature(MushoEntities.SWORD_MASTER.get(), Armatures.BIPED);
            Armatures.registerEntityTypeArmature(MushoEntities.TOWNSFOLK.get(), Armatures.BIPED);
            Armatures.registerEntityTypeArmature(MushoEntities.MAGIC_TUTOR.get(), Armatures.BIPED);
            Armatures.registerEntityTypeArmature(MushoEntities.ASSAULT_DOG.get(), MonsterAnimations.ASSAULT_DOG_ARMATURE);
            Armatures.registerEntityTypeArmature(MushoEntities.PAX_COYOTE.get(), MonsterAnimations.PAX_COYOTE_ARMATURE);
            Armatures.registerEntityTypeArmature(MushoEntities.TERMINATE_BOAR.get(), MonsterAnimations.TERMINATE_BOAR_ARMATURE);
            Armatures.registerEntityTypeArmature(MushoEntities.LUSTER_GRIZZLY.get(), MonsterAnimations.LUSTER_GRIZZLY_ARMATURE);
            Armatures.registerEntityTypeArmature(MushoEntities.GOBLIN.get(), MonsterAnimations.GOBLIN_ARMATURE);
            Armatures.registerEntityTypeArmature(MushoEntities.WRAITH.get(), MonsterAnimations.WRAITH_ARMATURE);
            Armatures.registerEntityTypeArmature(MushoEntities.ARMORED_WARRIOR.get(), MonsterAnimations.ARMORED_WARRIOR_ARMATURE);
            Armatures.registerEntityTypeArmature(MushoEntities.MAD_SKULL.get(), MonsterAnimations.MAD_SKULL_ARMATURE);
            Armatures.registerEntityTypeArmature(MushoEntities.TARANTULA_DEATHLORD.get(), MonsterAnimations.TARANTULA_DEATHLORD_ARMATURE);
            Armatures.registerEntityTypeArmature(MushoEntities.SNOW_DRAKE.get(), MonsterAnimations.SNOW_DRAKE_ARMATURE);
            Armatures.registerEntityTypeArmature(MushoEntities.DEVOURING_DEVIL.get(), MonsterAnimations.DEVOURING_DEVIL_ARMATURE);
            Armatures.registerEntityTypeArmature(MushoEntities.EXECUTIONER.get(), Armatures.BIPED);
            Armatures.registerEntityTypeArmature(MushoEntities.IRON_CRAWLER.get(), MonsterAnimations.IRON_CRAWLER_ARMATURE);
        });
    }
}
