package fr.xopi.mushocraft.client.render.mesh;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

/**
 * A heap of round, cauliflower-like billows: frost welling up from the ground, a cloud of smoke. Each billow is a
 * lumpy sphere ({@link LatheMesh}) that swells into place after a delay, so the heap grows from its foot to its top.
 * The heap is about one unit tall and centred on the origin, its foot at y = 0.
 */
public final class BillowCluster {
    /** One billow: where it sits, how big it is, and when it starts to swell (0 to 1 of the growth). */
    public record Billow(float x, float y, float z, float radius, float delay, float phase) {
    }

    private BillowCluster() {
    }

    /**
     * A heap shaped like a dome: a ring of large billows at the foot, smaller ones above, a crown on top.
     *
     * @param seed the same seed always gives the same heap
     */
    public static List<Billow> dome(long seed) {
        RandomSource random = RandomSource.create(seed);
        List<Billow> billows = new ArrayList<>();
        ring(billows, random, 6, 0.42F, 0.2F, 0.27F, 0.33F, 0.0F);
        ring(billows, random, 4, 0.24F, 0.5F, 0.22F, 0.27F, 0.25F);
        ring(billows, random, 2, 0.1F, 0.74F, 0.18F, 0.21F, 0.45F);
        billows.add(new Billow(0.0F, 0.88F, 0.0F, 0.15F, 0.62F, random.nextFloat() * Mth.TWO_PI));
        return billows;
    }

    private static void ring(List<Billow> billows, RandomSource random, int count, float distance, float height,
                             float minRadius, float maxRadius, float delay) {
        float turn = random.nextFloat() * Mth.TWO_PI;
        for (int i = 0; i < count; i++) {
            float angle = turn + i * Mth.TWO_PI / count + (random.nextFloat() - 0.5F) * 0.5F;
            float reach = distance * (0.85F + random.nextFloat() * 0.3F);
            billows.add(new Billow(Mth.cos(angle) * reach, height + (random.nextFloat() - 0.5F) * 0.08F, Mth.sin(angle) * reach,
                    minRadius + random.nextFloat() * (maxRadius - minRadius), delay + random.nextFloat() * 0.12F,
                    random.nextFloat() * Mth.TWO_PI));
        }
    }

    /**
     * @param growth  0 when nothing has swollen yet, 1 when every billow is at its full size
     * @param breathe how much the billows slowly swell and settle (0 for none)
     */
    public static void render(PoseStack poseStack, VertexConsumer consumer, List<Billow> billows, float growth, float breathe,
                              LatheMesh.Material material, float time, int light) {
        for (Billow billow : billows) {
            float own = Mth.clamp((growth - billow.delay()) / (1.0F - billow.delay() * 0.9F), 0.0F, 1.0F);
            if (own <= 0.0F) {
                continue;
            }
            // swells past its size, then settles
            float size = billow.radius() * overshoot(own) * (1.0F + breathe * Mth.sin(time * 0.08F + billow.phase()));
            poseStack.pushPose();
            poseStack.translate(billow.x(), billow.y(), billow.z());
            poseStack.scale(size, size, size);
            float phase = billow.phase();
            LatheMesh.Deform cauliflower = (point, t, angle) -> {
                float bump = 1.0F + 0.11F * Mth.sin(angle * 5.0F + phase) * Mth.sin(t * Mth.PI * 4.0F + phase)
                        + 0.05F * Mth.sin(angle * 9.0F - t * 11.0F + phase * 2.0F);
                point.mul(bump);
                if (point.y < -0.45F) {
                    // sits on what is below it
                    point.y = -0.45F + (point.y + 0.45F) * 0.35F;
                }
            };
            LatheMesh.render(poseStack.last(), consumer, LatheMesh.SPHERE, cauliflower, material, 12, 16, time, light);
            poseStack.popPose();
        }
    }

    private static float overshoot(float x) {
        float c = 1.9F;
        float back = x - 1.0F;
        return 1.0F + (c + 1.0F) * back * back * back + c * back * back;
    }
}
