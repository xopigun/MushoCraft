package fr.xopi.mushocraft.client.render.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.client.render.mesh.Premultiplied;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A ribbon of light streaming behind a flying spell (technique of the visual reference, docs/VISUELS_REFERENCE.md):
 * a strip through the last positions of the projectile, always turned towards the eye, thinning and fading towards its
 * tail, with a second, wide and faint strip behind it for a glow. Remembered on the client only.
 */
public final class RibbonTrail {
    private static final ResourceLocation TEXTURE = MushoCraft.id("textures/entity/spell/trail.png");
    private static final int POINTS = 10;
    private static final Map<Entity, RibbonTrail> TRAILS = new WeakHashMap<>();

    private final Deque<Vec3> points = new ArrayDeque<>();
    private int lastTick = -1;

    private RibbonTrail() {
    }

    /**
     * Draws the trail of an entity, remembering where it has been. The pose stack is at the entity's rendered position.
     *
     * @param width half the strip's width at its head, in blocks
     */
    public static void render(Entity entity, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, float width, int rgb) {
        RibbonTrail trail = TRAILS.computeIfAbsent(entity, ignored -> new RibbonTrail());
        Vec3 head = entity.getPosition(partialTicks).add(0.0, entity.getBbHeight() / 2.0, 0.0);
        if (entity.tickCount != trail.lastTick) {
            trail.lastTick = entity.tickCount;
            trail.points.addFirst(entity.position().add(0.0, entity.getBbHeight() / 2.0, 0.0));
            while (trail.points.size() > POINTS) {
                trail.points.removeLast();
            }
        }
        List<Vec3> line = new ArrayList<>(POINTS + 1);
        line.add(head);
        line.addAll(trail.points);
        if (line.size() < 3) {
            return;
        }
        Vec3 origin = entity.getPosition(partialTicks);
        Vec3 eye = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        VertexConsumer consumer = new Premultiplied(buffers.getBuffer(RenderType.eyes(TEXTURE)));
        Matrix4f matrix = poseStack.last().pose();
        strip(consumer, matrix, line, origin, eye, width * 2.2F, rgb, 0.3F);
        strip(consumer, matrix, line, origin, eye, width, rgb, 1.0F);
    }

    private static void strip(VertexConsumer consumer, Matrix4f matrix, List<Vec3> line, Vec3 origin, Vec3 eye, float width,
                              int rgb, float strength) {
        int red = rgb >> 16 & 0xFF;
        int green = rgb >> 8 & 0xFF;
        int blue = rgb & 0xFF;
        int count = line.size();
        for (int i = 0; i < count - 1; i++) {
            Vec3 a = line.get(i);
            Vec3 b = line.get(i + 1);
            if (a.distanceToSqr(b) < 1.0E-6) {
                continue;
            }
            float fadeA = 1.0F - (float) i / (count - 1);
            float fadeB = 1.0F - (float) (i + 1) / (count - 1);
            Vec3 sideA = side(a, b, eye).scale(width * fadeA);
            Vec3 sideB = side(a, b, eye).scale(width * fadeB);
            int alphaA = Mth.clamp((int) (255 * strength * fadeA), 0, 255);
            int alphaB = Mth.clamp((int) (255 * strength * fadeB), 0, 255);
            vertex(consumer, matrix, a.add(sideA).subtract(origin), 0.0F, 0.0F, red, green, blue, alphaA);
            vertex(consumer, matrix, b.add(sideB).subtract(origin), 0.0F, 1.0F, red, green, blue, alphaB);
            vertex(consumer, matrix, b.subtract(sideB).subtract(origin), 1.0F, 1.0F, red, green, blue, alphaB);
            vertex(consumer, matrix, a.subtract(sideA).subtract(origin), 1.0F, 0.0F, red, green, blue, alphaA);
        }
    }

    /** The direction across the strip at a segment: square to the segment and to the eye. */
    private static Vec3 side(Vec3 a, Vec3 b, Vec3 eye) {
        Vec3 cross = b.subtract(a).cross(eye.subtract(a));
        return cross.lengthSqr() < 1.0E-10 ? Vec3.ZERO : cross.normalize();
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Vec3 point, float u, float v, int red, int green, int blue,
                               int alpha) {
        consumer.addVertex(matrix, (float) point.x, (float) point.y, (float) point.z).setColor(red, green, blue, alpha).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
    }
}
