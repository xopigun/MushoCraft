package fr.xopi.mushocraft.client.render;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.core.MushoEntities;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.client.renderer.patched.entity.PCustomEntityRenderer;
import yesman.epicfight.client.renderer.patched.entity.PHumanoidRenderer;

/**
 * Client side of the Epic Fight integration: teachers are drawn with Epic Fight's animated biped mesh, monsters with
 * their own meshes.
 * Must be initialised from the mod constructor, on the client only.
 */
public final class EpicFightRenderers {
    private EpicFightRenderers() {
    }

    /** A monster's own skinned mesh, made for MushoCraft (tools/bestiary). */
    private static AssetAccessor<SkinnedMesh> monsterMesh(String species) {
        return Meshes.getOrCreate(MushoCraft.id("entity/" + species), loader -> loader.loadSkinnedMesh(SkinnedMesh::new));
    }

    public static void register() {
        EpicFightClientEventHooks.Registry.ADD_PATCHED_ENTITY.registerEvent(event -> {
            event.addPatchedEntityRenderer(MushoEntities.SWORD_MASTER.get(),
                    type -> new PHumanoidRenderer<>(Meshes.BIPED, event.getContext(), type).initLayerLast(event.getContext(), type));
            event.addPatchedEntityRenderer(MushoEntities.TOWNSFOLK.get(),
                    type -> new PHumanoidRenderer<>(Meshes.BIPED, event.getContext(), type).initLayerLast(event.getContext(), type));
            event.addPatchedEntityRenderer(MushoEntities.MAGIC_TUTOR.get(),
                    type -> new PHumanoidRenderer<>(Meshes.BIPED, event.getContext(), type).initLayerLast(event.getContext(), type));
            event.addPatchedEntityRenderer(MushoEntities.ASSAULT_DOG.get(),
                    type -> new PCustomEntityRenderer(monsterMesh("assault_dog"), event.getContext()));
            event.addPatchedEntityRenderer(MushoEntities.PAX_COYOTE.get(),
                    type -> new PCustomEntityRenderer(monsterMesh("pax_coyote"), event.getContext()));
            event.addPatchedEntityRenderer(MushoEntities.TERMINATE_BOAR.get(),
                    type -> new PCustomEntityRenderer(monsterMesh("terminate_boar"), event.getContext()));
            event.addPatchedEntityRenderer(MushoEntities.LUSTER_GRIZZLY.get(),
                    type -> new PCustomEntityRenderer(monsterMesh("luster_grizzly"), event.getContext()));
            event.addPatchedEntityRenderer(MushoEntities.GOBLIN.get(),
                    type -> new PCustomEntityRenderer(monsterMesh("goblin"), event.getContext()));
            event.addPatchedEntityRenderer(MushoEntities.ARMORED_WARRIOR.get(),
                    type -> new PCustomEntityRenderer(monsterMesh("armored_warrior"), event.getContext()));
            event.addPatchedEntityRenderer(MushoEntities.MAD_SKULL.get(),
                    type -> new PCustomEntityRenderer(monsterMesh("mad_skull"), event.getContext()));
            event.addPatchedEntityRenderer(MushoEntities.TARANTULA_DEATHLORD.get(),
                    type -> new PCustomEntityRenderer(monsterMesh("tarantula_deathlord"), event.getContext()));
            event.addPatchedEntityRenderer(MushoEntities.SNOW_DRAKE.get(),
                    type -> new PCustomEntityRenderer(monsterMesh("snow_drake"), event.getContext()));
            event.addPatchedEntityRenderer(MushoEntities.DEVOURING_DEVIL.get(),
                    type -> new PCustomEntityRenderer(monsterMesh("devouring_devil"), event.getContext()));
            event.addPatchedEntityRenderer(MushoEntities.IRON_CRAWLER.get(),
                    type -> new PCustomEntityRenderer(monsterMesh("iron_crawler"), event.getContext()));
            event.addPatchedEntityRenderer(MushoEntities.EXECUTIONER.get(),
                    type -> new PHumanoidRenderer<>(Meshes.BIPED, event.getContext(), type).initLayerLast(event.getContext(), type));
            event.addPatchedEntityRenderer(MushoEntities.WRAITH.get(),
                    type -> new TranslucentMonsterRenderer(monsterMesh("wraith"), event.getContext(), 0.78F));
        });
    }
}
