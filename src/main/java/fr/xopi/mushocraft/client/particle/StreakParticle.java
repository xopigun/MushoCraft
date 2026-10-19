package fr.xopi.mushocraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * A line of rushing air, as wind is drawn in the anime: a soft white stroke stretched along the way it moves, longer
 * the faster it goes, turned about its length to face the eye. It slows, shortens and fades away. Added to the light
 * behind it, from a {@link SmoothSheet} so it stays smooth.
 */
public final class StreakParticle extends Particle {
    private static final int FULL_BRIGHT = 0xF000F0;

    private final float width;
    private final float stretch;
    /** The way it moves, kept once it has slowed down to nothing. */
    private final Vector3f heading = new Vector3f(0.0F, 0.0F, 1.0F);

    private StreakParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
        super(level, x, y, z);
        this.xd = dx;
        this.yd = dy;
        this.zd = dz;
        this.friction = 0.9F;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.lifetime = 7 + random.nextInt(5);
        this.width = 0.05F + random.nextFloat() * 0.05F;
        this.stretch = 3.5F + random.nextFloat() * 2.0F;
        float tint = 0.85F + random.nextFloat() * 0.15F;
        setColor(tint, 1.0F, tint * 0.97F);
        aim();
    }

    private void aim() {
        if (xd * xd + yd * yd + zd * zd > 1.0E-6) {
            heading.set((float) xd, (float) yd, (float) zd).normalize();
        }
    }

    @Override
    public void tick() {
        super.tick();
        aim();
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTicks) {
        float life = Math.min(1.0F, (age + partialTicks) / lifetime);
        float speed = (float) Math.sqrt(xd * xd + yd * yd + zd * zd);
        float length = 0.25F + speed * stretch;
        float fade = (1.0F - life) * (1.0F - life);
        Vec3 eye = camera.getPosition();
        Vector3f centre = new Vector3f((float) (Mth.lerp(partialTicks, xo, x) - eye.x), (float) (Mth.lerp(partialTicks, yo, y) - eye.y),
                (float) (Mth.lerp(partialTicks, zo, z) - eye.z));
        // turned about its length to face the eye
        Vector3f side = new Vector3f(heading).cross(new Vector3f(centre).negate());
        if (side.lengthSquared() < 1.0E-8F) {
            return;
        }
        side.normalize().mul(width * 0.5F);
        Vector3f half = new Vector3f(heading).mul(length * 0.5F);
        Vector3f head = new Vector3f(centre).add(half);
        Vector3f tail = new Vector3f(centre).sub(half);
        float alpha = 0.85F * fade;
        // the sheet is drawn tail (u = 0) to head (u = 1); both windings, so no face is ever culled
        vertex(consumer, new Vector3f(tail).sub(side), 0.0F, 1.0F, alpha);
        vertex(consumer, new Vector3f(tail).add(side), 0.0F, 0.0F, alpha);
        vertex(consumer, new Vector3f(head).add(side), 1.0F, 0.0F, alpha);
        vertex(consumer, new Vector3f(head).sub(side), 1.0F, 1.0F, alpha);
        vertex(consumer, new Vector3f(head).sub(side), 1.0F, 1.0F, alpha);
        vertex(consumer, new Vector3f(head).add(side), 1.0F, 0.0F, alpha);
        vertex(consumer, new Vector3f(tail).add(side), 0.0F, 0.0F, alpha);
        vertex(consumer, new Vector3f(tail).sub(side), 0.0F, 1.0F, alpha);
    }

    private void vertex(VertexConsumer consumer, Vector3f point, float u, float v, float alpha) {
        consumer.addVertex(point.x, point.y, point.z).setUv(u, v).setColor(rCol, gCol, bCol, alpha).setLight(FULL_BRIGHT);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return SmoothSheet.WIND_STREAK.renderType();
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new StreakParticle(level, x, y, z, dx, dy, dz);
        }
    }
}
