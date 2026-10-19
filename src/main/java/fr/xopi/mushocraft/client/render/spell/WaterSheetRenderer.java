package fr.xopi.mushocraft.client.render.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fr.xopi.mushocraft.client.render.mesh.GridMesh;
import fr.xopi.mushocraft.client.render.mesh.LatheMesh;
import fr.xopi.mushocraft.magic.entity.WaterSheet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

/**
 * Draws Water Shield's water: one grid that is first a swelling sphere, then morphs into a broad sheet whose surface
 * ripples, whose rim bristles with splashes (as in the anime) and whose top spills; at the end it slumps and falls.
 */
public class WaterSheetRenderer extends EntityRenderer<WaterSheet> {
    private static final LatheMesh.Material WALL = new LatheMesh.Material(0.55F, 0.82F, 1.0F, 0.42F, 0.88F, 0.5F,
            3.0F, 3.0F, 0.01F, -0.06F);
    private static final float BUBBLE_RADIUS = 0.75F;

    public WaterSheetRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    /** The wall stands far beyond the entity's tiny box: cull against the whole wall instead. */
    @Override
    public boolean shouldRender(WaterSheet sheet, Frustum frustum, double x, double y, double z) {
        float reach = Math.max(sheet.width(), sheet.height());
        return frustum.isVisible(new AABB(sheet.getX() - reach, sheet.getY(), sheet.getZ() - reach,
                sheet.getX() + reach, sheet.getY() + sheet.height() + 1.0, sheet.getZ() + reach));
    }

    @Override
    public void render(WaterSheet sheet, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int light) {
        float age = sheet.tickCount + partialTicks;
        float width = sheet.width();
        float height = sheet.height();
        float bubble = Mth.clamp(age / WaterSheet.BUBBLE_TICKS, 0.0F, 1.0F);
        // morph: 0 = sphere, 1 = wall
        float morph = smooth(Mth.clamp((age - WaterSheet.BUBBLE_TICKS) / WaterSheet.SPREAD_TICKS, 0.0F, 1.0F));
        float collapse = smooth(Mth.clamp((age - (sheet.lifetime() - WaterSheet.COLLAPSE_TICKS)) / WaterSheet.COLLAPSE_TICKS, 0.0F, 1.0F));
        float sphereCentre = height * 0.45F;

        GridMesh.Surface surface = (u, v) -> {
            // sphere: u around, v from bottom to top
            double theta = u * Math.PI * 2.0;
            double phi = v * Math.PI;
            float radius = BUBBLE_RADIUS * bubble * (1.0F + 0.06F * Mth.sin(age * 0.8F + u * 12.0F));
            Vector3f sphere = new Vector3f((float) (Math.cos(theta) * Math.sin(phi)) * radius,
                    sphereCentre - (float) Math.cos(phi) * radius, (float) (Math.sin(theta) * Math.sin(phi)) * radius);
            // wall: u across, v up; ripples travel across it, the rim bristles outwards
            float across = (u - 0.5F) * width;
            float up = v * height;
            float ripple = 0.08F * Mth.sin(across * 2.4F + age * 0.35F) + 0.06F * Mth.sin(up * 3.1F - age * 0.5F);
            float rim = Math.max(Math.abs(u - 0.5F) * 2.0F, Math.abs(v - 0.5F) * 2.0F);
            float bristle = rim > 0.85F ? (rim - 0.85F) * 6.0F * (0.5F + 0.5F * Mth.sin(u * 47.0F + v * 31.0F + age * 0.9F)) : 0.0F;
            float spread = 1.0F + bristle * 0.18F;
            Vector3f wall = new Vector3f(across * spread, sphereCentre + (up - sphereCentre) * spread, ripple);
            // collapse: the water slumps down and spreads at the foot of the wall
            wall.y = wall.y * (1.0F - collapse) ;
            wall.z += collapse * (v - 0.5F) * 0.6F;
            return sphere.lerp(wall, morph);
        };

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-sheet.getYRot()));
        GridMesh.render(poseStack.last(), buffers.getBuffer(RenderType.entityTranslucent(WaterVisuals.WATER)), surface, WALL,
                28, 22, age, light);
        poseStack.popPose();
        super.render(sheet, yaw, partialTicks, poseStack, buffers, light);
    }

    private static float smooth(float t) {
        return t * t * (3.0F - 2.0F * t);
    }

    @Override
    public ResourceLocation getTextureLocation(WaterSheet sheet) {
        return WaterVisuals.WATER;
    }
}
