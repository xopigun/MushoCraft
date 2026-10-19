package fr.xopi.mushocraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.xopi.mushocraft.core.OrientedParticleOptions;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * A flat plane facing a set way, not the eye (technique of the visual reference, docs/VISUELS_REFERENCE.md): a shock
 * wave across a projectile's path, a slash in the plane of a swing. It plays its flipbook over its short life, grows a
 * little, and is drawn on both faces so it shows from either side.
 */
public class OrientedParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final Quaternionf facing;
    private final float startSize;
    private final float growth;
    private final ParticleRenderType renderType;

    protected OrientedParticle(ClientLevel level, double x, double y, double z, OrientedParticleOptions options, SpriteSet sprites,
                               int lifetime, float growth, ParticleRenderType renderType) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.lifetime = lifetime;
        this.startSize = options.size();
        this.quadSize = startSize;
        this.growth = growth;
        this.renderType = renderType;
        // the plane's normal points along the facing; then it turns within the plane
        this.facing = new Quaternionf().rotationY(-options.yaw() * Mth.DEG_TO_RAD)
                .rotateX(options.pitch() * Mth.DEG_TO_RAD)
                .rotateZ(options.roll() * Mth.DEG_TO_RAD);
        int rgb = options.rgb();
        setColor((rgb >> 16 & 0xFF) / 255.0F, (rgb >> 8 & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F);
        setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (!removed) {
            setSpriteFromAge(sprites);
        }
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTicks) {
        float life = Math.min(1.0F, (age + partialTicks) / lifetime);
        quadSize = startSize * (1.0F + growth * life);
        Vec3 eye = camera.getPosition();
        float x = (float) (Mth.lerp(partialTicks, xo, this.x) - eye.x);
        float y = (float) (Mth.lerp(partialTicks, yo, this.y) - eye.y);
        float z = (float) (Mth.lerp(partialTicks, zo, this.z) - eye.z);
        int light = getLightColor(partialTicks);
        face(consumer, x, y, z, light, false);
        face(consumer, x, y, z, light, true);
    }

    /** One face of the plane; the back face is mirrored so the picture reads the same from behind. */
    private void face(VertexConsumer consumer, float x, float y, float z, int light, boolean back) {
        float u0 = back ? getU1() : getU0();
        float u1 = back ? getU0() : getU1();
        float[][] corners = back
                ? new float[][]{{1, -1, u1, getV1()}, {1, 1, u1, getV0()}, {-1, 1, u0, getV0()}, {-1, -1, u0, getV1()}}
                : new float[][]{{-1, -1, u0, getV1()}, {-1, 1, u0, getV0()}, {1, 1, u1, getV0()}, {1, -1, u1, getV1()}};
        for (float[] corner : corners) {
            Vector3f point = new Vector3f(corner[0], corner[1], 0.0F).rotate(facing).mul(quadSize).add(x, y, z);
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
        return renderType;
    }

    /** A shock wave of air: a ring that spreads and tears across the way it faces. Translucent, pale. */
    public record ShockWaveProvider(SpriteSet sprites) implements ParticleProvider<OrientedParticleOptions> {
        @Override
        public Particle createParticle(OrientedParticleOptions options, ClientLevel level, double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new OrientedParticle(level, x, y, z, options, sprites, 7 + level.random.nextInt(4), 0.6F,
                    ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT);
        }
    }

    /** A slash: a crescent that sweeps and dissolves in a few ticks. Additive, it glows. */
    public record SlashProvider(SpriteSet sprites) implements ParticleProvider<OrientedParticleOptions> {
        @Override
        public Particle createParticle(OrientedParticleOptions options, ClientLevel level, double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new OrientedParticle(level, x, y, z, options, sprites, 5, 0.15F, FireParticles.ADDITIVE);
        }
    }
}
