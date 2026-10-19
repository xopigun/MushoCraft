package fr.xopi.mushocraft.client.render.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.client.render.mesh.BillowCluster;
import fr.xopi.mushocraft.client.render.mesh.LatheMesh;
import fr.xopi.mushocraft.magic.entity.SpellPillar;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.List;

/**
 * The frost of Icicle Field, after the anime: round billows of white frost, cel-lined in pale blue, that well up from
 * the ground in an instant, swallow what they catch, breathe a little, then slump and melt away. Each mound is a
 * {@link BillowCluster} wrapped in a faint cold mist. The texture is textures/entity/spell/frost.png, drawn for
 * MushoCraft.
 */
public final class FrostVisuals {
    private static final ResourceLocation FROST = MushoCraft.id("textures/entity/spell/frost.png");
    private static final int RISE_TICKS = 10;
    private static final int MELT_TICKS = 10;

    private static final LatheMesh.Material BODY = new LatheMesh.Material(0.93F, 0.97F, 1.0F, 0.96F, 1.0F, 0.6F,
            1.0F, 1.0F, 0.002F, 0.004F);
    private static final LatheMesh.Material MIST = new LatheMesh.Material(0.86F, 0.94F, 1.0F, 0.22F, 0.0F, 0.0F,
            1.0F, 1.0F, 0.01F, 0.02F);

    private FrostVisuals() {
    }

    static void renderMound(SpellPillar mound, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int light) {
        float age = mound.tickCount + partialTicks;
        float growth = Math.min(1.0F, age / RISE_TICKS);
        float melt = Mth.clamp((mound.lifetime() - age) / MELT_TICKS, 0.0F, 1.0F);
        float height = mound.height();
        float width = height * 1.15F;
        List<BillowCluster.Billow> billows = BillowCluster.dome(mound.getId() * 31L + 7L);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-mound.getYRot()));
        // melting: the heap slumps and spreads before it vanishes
        poseStack.scale(width * (1.0F + (1.0F - melt) * 0.15F), height * (0.25F + 0.75F * melt), width * (1.0F + (1.0F - melt) * 0.15F));
        float swollen = growth * (0.4F + 0.6F * melt);
        BillowCluster.render(poseStack, buffers.getBuffer(RenderType.entityTranslucentCull(FROST)), billows, swollen, 0.03F,
                BODY, age, light);
        poseStack.scale(1.12F, 1.08F, 1.12F);
        BillowCluster.render(poseStack, buffers.getBuffer(RenderType.entityTranslucent(FROST)), billows, swollen, 0.05F,
                MIST, age + 20.0F, light);
        poseStack.popPose();
    }
}
