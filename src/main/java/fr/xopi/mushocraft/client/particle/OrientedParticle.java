package fr.xopi.mushocraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.xopi.mushocraft.core.OrientedParticleOptions;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * A flat plane facing a set way, not the eye (technique of the visual reference, docs/VISUELS_REFERENCE.md): a shock
 * wave across a projectile's path, a slash in the plane of a swing. It plays its flipbook over its short life, grows a
 * little, and is drawn on both faces so it shows from either side. Its frames come from a {@link SmoothSheet}, so its
 * edges stay smooth however large and however slanted it is drawn.
 */
public class OrientedParticle extends Particle {
    private final SmoothSheet sheet;
    private final Quaternionf facing;
    private final float startSize;
    private final float growth;

    protected OrientedParticle(ClientLevel level, double x, double y, double z, OrientedParticleOptions options, SmoothSheet sheet,
                               int lifetime, float growth) {
        super(level, x, y, z);
        this.sheet = sheet;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.lifetime = lifetime;
        this.startSize = options.size();
        this.growth = growth;
        // the plane's normal points along the facing; then it turns within the plane
        this.facing = new Quaternionf().rotationY(-options.yaw() * Mth.DEG_TO_RAD)
                .rotateX(options.pitch() * Mth.DEG_TO_RAD)
                .rotateZ(options.roll() * Mth.DEG_TO_RAD);
        int rgb = options.rgb();
        setColor((rgb >> 16 & 0xFF) / 255.0F, (rgb >> 8 & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F);
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTicks) {
        float life = Math.min(1.0F, (age + partialTicks) / lifetime);
        float size = startSize * (1.0F + growth * life);
        int frame = sheet.frame(life);
        Vec3 eye = camera.getPosition();
        float x = (float) (Mth.lerp(partialTicks, xo, this.x) - eye.x);
        float y = (float) (Mth.lerp(partialTicks, yo, this.y) - eye.y);
        float z = (float) (Mth.lerp(partialTicks, zo, this.z) - eye.z);
        int light = getLightColor(partialTicks);
        face(consumer, x, y, z, size, frame, light, false);
        face(consumer, x, y, z, size, frame, light, true);
    }

    /** One face of the plane; the back face is mirrored so the picture reads the same from behind. */
    private void face(VertexConsumer consumer, float x, float y, float z, float size, int frame, int light, boolean back) {
        float u0 = back ? sheet.u1(frame) : sheet.u0(frame);
        float u1 = back ? sheet.u0(frame) : sheet.u1(frame);
        float[][] corners = back
                ? new float[][]{{1, -1, u1, 1}, {1, 1, u1, 0}, {-1, 1, u0, 0}, {-1, -1, u0, 1}}
                : new float[][]{{-1, -1, u0, 1}, {-1, 1, u0, 0}, {1, 1, u1, 0}, {1, -1, u1, 1}};
        for (float[] corner : corners) {
            Vector3f point = new Vector3f(corner[0], corner[1], 0.0F).rotate(facing).mul(size).add(x, y, z);
            consumer.addVertex(point.x, point.y, point.z).setUv(corner[2], corner[3]).setColor(rCol, gCol, bCol, alpha)
                    .setLight(light);
        }
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return sheet.renderType();
    }

    /** A shock wave of air: a ring that spreads and tears across the way it faces. Translucent, pale. */
    public static final class ShockWaveProvider implements ParticleProvider<OrientedParticleOptions> {
        @Override
        public Particle createParticle(OrientedParticleOptions options, ClientLevel level, double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new OrientedParticle(level, x, y, z, options, SmoothSheet.SHOCK_WAVE, 7 + level.random.nextInt(4), 0.6F);
        }
    }

    /** A slash: a crescent that sweeps and dissolves in a few ticks. Additive, it glows. */
    public static final class SlashProvider implements ParticleProvider<OrientedParticleOptions> {
        @Override
        public Particle createParticle(OrientedParticleOptions options, ClientLevel level, double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new OrientedParticle(level, x, y, z, options, SmoothSheet.SLASH, 5, 0.15F);
        }
    }
}
