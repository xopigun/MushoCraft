package fr.xopi.mushocraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.magic.entity.SpellProjectile;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Rounded orb of conjured matter (Water Ball, Fireball...), one block across, centred on its origin.
 * Made in Blockbench: blockbench/spell_orb.bbmodel.
 */
public class SpellOrbModel extends EntityModel<SpellProjectile> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(MushoCraft.id("spell_orb"), "main");

    private final ModelPart orb;

    public SpellOrbModel(ModelPart root) {
        this.orb = root.getChild("orb");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("orb", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-6.0F, -6.0F, -6.0F, 12.0F, 12.0F, 12.0F, CubeDeformation.NONE)
                        .texOffs(0, 24).addBox(-8.0F, -4.0F, -4.0F, 16.0F, 8.0F, 8.0F, CubeDeformation.NONE)
                        .texOffs(0, 40).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 16.0F, 8.0F, CubeDeformation.NONE)
                        .texOffs(16, 24).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 16.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(SpellProjectile entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        orb.yRot = ageInTicks * 0.25F;
        orb.xRot = ageInTicks * 0.15F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        orb.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
