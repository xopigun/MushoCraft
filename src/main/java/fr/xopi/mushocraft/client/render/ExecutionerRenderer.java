package fr.xopi.mushocraft.client.render;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.monster.Executioner;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Vanilla renderer of the Executioner (Epic Fight replaces it with its animated biped mesh, using the same skin): a full
 * suit of plate drawn for MushoCraft, textures/entity/monster/executioner.png.
 */
public class ExecutionerRenderer extends HumanoidMobRenderer<Executioner, PlayerModel<Executioner>> {
    private static final ResourceLocation SKIN = MushoCraft.id("textures/entity/monster/executioner.png");

    public ExecutionerRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.6F);
    }

    @Override
    public ResourceLocation getTextureLocation(Executioner executioner) {
        return SKIN;
    }
}
