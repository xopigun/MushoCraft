package fr.xopi.mushocraft.client.render.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.client.render.mesh.Premultiplied;
import fr.xopi.mushocraft.magic.entity.SpellBeam;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Draws a {@link SpellBeam}: two prisms one inside the other along the ray, both added to the light (a coloured sheath,
 * a hot core), their streaked texture flowing from the caster to the end. It flares on, holds, then thins away; a drill
 * twists about itself.
 */
public class SpellBeamRenderer extends EntityRenderer<SpellBeam> {
    private static final ResourceLocation STREAKS = MushoCraft.id("textures/entity/spell/beam_streaks.png");
    private static final int SIDES = 6;

    public SpellBeamRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(SpellBeam beam, Frustum frustum, double x, double y, double z) {
        return frustum.isVisible(new AABB(beam.position(), beam.end()).inflate(beam.width() + 1.0));
    }

    @Override
    public void render(SpellBeam beam, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int light) {
        float age = beam.tickCount + partialTicks;
        float life = Mth.clamp(age / beam.lifetime(), 0.0F, 1.0F);
        // flares on fast, holds, then thins away
        float strength = life < 0.15F ? life / 0.15F : 1.0F - Math.max(0.0F, (life - 0.5F) / 0.5F);
        Vec3 along = beam.end().subtract(beam.position());
        float length = (float) along.length();
        if (length < 0.01F || strength <= 0.0F) {
            return;
        }
        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotationTo(new Vector3f(0.0F, 1.0F, 0.0F),
                new Vector3f((float) along.x, (float) along.y, (float) along.z).normalize()));
        if (beam.spins()) {
            poseStack.mulPose(new Quaternionf().rotationY(age * 0.6F));
        }
        VertexConsumer consumer = new Premultiplied(buffers.getBuffer(RenderType.eyes(STREAKS)));
        float width = beam.width() * 0.6F * (0.6F + 0.4F * strength);
        // a light sheath one can see through, around a thin, bright core
        prism(poseStack, consumer, length, width, beam.sheathRgb(), 0.45F * strength, age * 0.12F, 1.0F);
        prism(poseStack, consumer, length, width * 0.35F, beam.coreRgb(), 0.9F * strength, age * 0.2F, 0.0F);
        poseStack.popPose();
        super.render(beam, yaw, partialTicks, poseStack, buffers, light);
    }

    /** A hexagonal prism along +Y; its texture scrolls along the length. */
    private static void prism(PoseStack poseStack, VertexConsumer consumer, float length, float radius, int rgb, float alpha,
                              float scroll, float twist) {
        Matrix4f matrix = poseStack.last().pose();
        int red = rgb >> 16 & 0xFF;
        int green = rgb >> 8 & 0xFF;
        int blue = rgb & 0xFF;
        int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
        float vEnd = length / 2.0F;
        for (int i = 0; i < SIDES; i++) {
            float a0 = Mth.TWO_PI * i / SIDES;
            float a1 = Mth.TWO_PI * (i + 1) / SIDES;
            float x0 = Mth.cos(a0) * radius;
            float z0 = Mth.sin(a0) * radius;
            float x1 = Mth.cos(a1) * radius;
            float z1 = Mth.sin(a1) * radius;
            // the far end turns a little against the near one, for a twisted drill
            float t0 = a0 + twist;
            float t1 = a1 + twist;
            float fx0 = Mth.cos(t0) * radius;
            float fz0 = Mth.sin(t0) * radius;
            float fx1 = Mth.cos(t1) * radius;
            float fz1 = Mth.sin(t1) * radius;
            float u0 = (float) i / SIDES;
            float u1 = (float) (i + 1) / SIDES;
            vertex(consumer, matrix, x0, 0.0F, z0, u0, -scroll, red, green, blue, a);
            vertex(consumer, matrix, x1, 0.0F, z1, u1, -scroll, red, green, blue, a);
            vertex(consumer, matrix, fx1, length, fz1, u1, vEnd - scroll, red, green, blue, a);
            vertex(consumer, matrix, fx0, length, fz0, u0, vEnd - scroll, red, green, blue, a);
            // and the inner face, so it shows from inside and from every side
            vertex(consumer, matrix, fx0, length, fz0, u0, vEnd - scroll, red, green, blue, a);
            vertex(consumer, matrix, fx1, length, fz1, u1, vEnd - scroll, red, green, blue, a);
            vertex(consumer, matrix, x1, 0.0F, z1, u1, -scroll, red, green, blue, a);
            vertex(consumer, matrix, x0, 0.0F, z0, u0, -scroll, red, green, blue, a);
        }
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, float u, float v,
                               int red, int green, int blue, int alpha) {
        consumer.addVertex(matrix, x, y, z).setColor(red, green, blue, alpha).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(SpellBeam beam) {
        return STREAKS;
    }
}
