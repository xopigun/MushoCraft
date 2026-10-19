package fr.xopi.mushocraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * An electric arc crackling at a point (technique of the visual reference, docs/VISUELS_REFERENCE.md): a few bolts
 * made of jagged segments, each a strip turned towards the eye, drawn again at random every two ticks and fading in a
 * few ticks. Blue-white, added to the light. Its reach is given by the x speed of the particle.
 */
public class ArcParticle extends TextureSheetParticle {
    private static final int BOLTS = 3;
    private static final int SEGMENTS = 5;

    private final float reach;
    private final Vec3[][] bolts = new Vec3[BOLTS][SEGMENTS + 1];

    ArcParticle(ClientLevel level, double x, double y, double z, double reach, SpriteSet sprites) {
        super(level, x, y, z);
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.lifetime = 4 + random.nextInt(4);
        this.reach = (float) Math.max(0.3, reach);
        pickSprite(sprites);
        boolean yellow = random.nextFloat() < 0.15F;
        setColor(yellow ? 1.0F : 0.75F, yellow ? 0.95F : 0.88F, yellow ? 0.55F : 1.0F);
        reshape();
    }

    /** Draws new random walks for the bolts. */
    private void reshape() {
        RandomSource rand = random;
        for (Vec3[] bolt : bolts) {
            Vec3 point = Vec3.ZERO;
            Vec3 heading = new Vec3(rand.nextDouble() - 0.5, rand.nextDouble() - 0.5, rand.nextDouble() - 0.5).normalize();
            double step = reach / SEGMENTS;
            bolt[0] = point;
            for (int i = 1; i <= SEGMENTS; i++) {
                Vec3 jitter = new Vec3(rand.nextDouble() - 0.5, rand.nextDouble() - 0.5, rand.nextDouble() - 0.5).scale(step * 1.2);
                point = point.add(heading.scale(step)).add(jitter);
                bolt[i] = point;
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!removed && age % 2 == 0) {
            reshape();
        }
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTicks) {
        float life = Math.min(1.0F, (age + partialTicks) / lifetime);
        float strength = 1.0F - life;
        Vec3 eye = camera.getPosition();
        Vec3 origin = new Vec3(x, y, z);
        float width = 0.035F * (0.5F + reach * 0.3F);
        float u0 = getU0();
        float u1 = getU1();
        float v0 = getV0();
        float v1 = getV1();
        int light = 0xF000F0;
        for (Vec3[] bolt : bolts) {
            for (int i = 0; i < SEGMENTS; i++) {
                Vec3 a = origin.add(bolt[i]);
                Vec3 b = origin.add(bolt[i + 1]);
                // a strip from a to b, as wide as the bolt, turned towards the eye
                Vec3 along = b.subtract(a);
                Vec3 toEye = eye.subtract(a);
                Vec3 side = along.cross(toEye);
                if (side.lengthSqr() < 1.0E-8) {
                    continue;
                }
                side = side.normalize().scale(width * (1.0F - 0.5F * i / SEGMENTS));
                corner(consumer, a.add(side), eye, u0, v1, strength, light);
                corner(consumer, b.add(side), eye, u0, v0, strength, light);
                corner(consumer, b.subtract(side), eye, u1, v0, strength, light);
                corner(consumer, a.subtract(side), eye, u1, v1, strength, light);
            }
        }
    }

    private void corner(VertexConsumer consumer, Vec3 point, Vec3 eye, float u, float v, float strength, int light) {
        Vector3f relative = new Vector3f((float) (point.x - eye.x), (float) (point.y - eye.y), (float) (point.z - eye.z));
        consumer.addVertex(relative.x, relative.y, relative.z).setUv(u, v)
                .setColor(rCol * strength, gCol * strength, bCol * strength, 1.0F).setLight(light);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return FireParticles.ADDITIVE;
    }

    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double reach, double dy, double dz) {
            return new ArcParticle(level, x, y, z, reach, sprites);
        }
    }
}
