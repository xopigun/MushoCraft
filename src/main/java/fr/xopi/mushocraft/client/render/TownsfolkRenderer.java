package fr.xopi.mushocraft.client.render;

import fr.xopi.mushocraft.village.Appearance;
import fr.xopi.mushocraft.village.TownsfolkEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Vanilla renderer of the village folk (Epic Fight replaces it with its animated biped mesh, using the same skin):
 * the outfit follows the person's sex, age group and variant ({@link Appearance}).
 */
public class TownsfolkRenderer extends HumanoidMobRenderer<TownsfolkEntity, PlayerModel<TownsfolkEntity>> {
    public TownsfolkRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(TownsfolkEntity person) {
        return Appearance.skin(person.sex(), person.stage(), person.variant());
    }
}
