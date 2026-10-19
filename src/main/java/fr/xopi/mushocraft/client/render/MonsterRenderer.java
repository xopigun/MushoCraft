package fr.xopi.mushocraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.monster.MushoMonster;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/**
 * Vanilla side of a monster's rendering: its shadow, name tag and texture. The body itself is drawn by Epic Fight from
 * the monster's own skinned mesh (see {@link EpicFightRenderers}); the vanilla model is empty.
 */
public class MonsterRenderer<T extends MushoMonster> extends MobRenderer<T, MonsterRenderer.Empty<T>> {
    public MonsterRenderer(EntityRendererProvider.Context context, float shadowRadius) {
        super(context, new Empty<>(), shadowRadius);
    }

    @Override
    public ResourceLocation getTextureLocation(T monster) {
        String species = BuiltInRegistries.ENTITY_TYPE.getKey(monster.getType()).getPath();
        return MushoCraft.id("textures/entity/monster/" + species + ".png");
    }

    /** No vanilla geometry: Epic Fight draws the body. */
    public static final class Empty<T extends MushoMonster> extends EntityModel<T> {
        @Override
        public void setupAnim(T monster, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        }

        @Override
        public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        }
    }
}
