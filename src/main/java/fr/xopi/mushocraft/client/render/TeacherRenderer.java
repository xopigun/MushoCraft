package fr.xopi.mushocraft.client.render;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.npc.MagicTutorEntity;
import fr.xopi.mushocraft.npc.SwordMasterEntity;
import fr.xopi.mushocraft.npc.TeacherEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Vanilla renderer of the teachers (Epic Fight replaces it with its own animated mesh, using the same skin).
 * Each style and each school has its own outfit: textures/entity/teacher/*.png, drawn for MushoCraft.
 */
public class TeacherRenderer<T extends TeacherEntity> extends HumanoidMobRenderer<T, PlayerModel<T>> {
    public TeacherRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(T teacher) {
        String outfit = switch (teacher) {
            case SwordMasterEntity master -> master.style().getSerializedName();
            case MagicTutorEntity tutor -> tutor.isMilisKnight() ? "milis_knight" : "mage";
            default -> "mage";
        };
        return MushoCraft.id("textures/entity/teacher/" + outfit + ".png");
    }
}
