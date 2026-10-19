package fr.xopi.mushocraft.client.render;

import fr.xopi.mushocraft.core.MushoEntities;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.client.renderer.patched.entity.PHumanoidRenderer;

/**
 * Client side of the Epic Fight integration: teachers are drawn with Epic Fight's animated biped mesh.
 * Must be initialised from the mod constructor, on the client only.
 */
public final class EpicFightRenderers {
    private EpicFightRenderers() {
    }

    public static void register() {
        EpicFightClientEventHooks.Registry.ADD_PATCHED_ENTITY.registerEvent(event -> {
            event.addPatchedEntityRenderer(MushoEntities.SWORD_MASTER.get(),
                    type -> new PHumanoidRenderer<>(Meshes.BIPED, event.getContext(), type).initLayerLast(event.getContext(), type));
            event.addPatchedEntityRenderer(MushoEntities.MAGIC_TUTOR.get(),
                    type -> new PHumanoidRenderer<>(Meshes.BIPED, event.getContext(), type).initLayerLast(event.getContext(), type));
        });
    }
}
