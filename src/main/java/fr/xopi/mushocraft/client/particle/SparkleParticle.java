package fr.xopi.mushocraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.util.Mth;

/**
 * A twinkle of light, as the anime draws healing (S1 ep. 1): a four-pointed star that swells, flickers and fades as it
 * drifts up a little. Tinted by its colour, added to the light behind it, from a {@link SmoothSheet}.
 */
public final class SparkleParticle extends SingleQuadParticle {
    private static final int FULL_BRIGHT = 0xF000F0;
    private final float peakSize;
    private final float phase;

    private SparkleParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
        super(level, x, y, z);
        this.xd = dx * 0.2;
        this.yd = 0.01 + dy * 0.2;
        this.zd = dz * 0.2;
        this.friction = 0.9F;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.lifetime = 12 + random.nextInt(10);
        this.peakSize = 0.12F + random.nextFloat() * 0.14F;
        this.phase = random.nextFloat() * Mth.TWO_PI;
        this.quadSize = 0.0F;
    }

    @Override
    public void tick() {
        super.tick();
        float life = (float) age / lifetime;
        // swells fast, then shrinks away, flickering
        float swell = life < 0.25F ? life / 0.25F : 1.0F - (life - 0.25F) / 0.75F;
        quadSize = peakSize * swell * (0.85F + 0.15F * Mth.sin(age * 1.7F + phase));
    }

    @Override
    protected float getU0() {
        return SmoothSheet.SPARKLE.u0(0);
    }

    @Override
    protected float getU1() {
        return SmoothSheet.SPARKLE.u1(0);
    }

    @Override
    protected float getV0() {
        return 0.0F;
    }

    @Override
    protected float getV1() {
        return 1.0F;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return FULL_BRIGHT;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return SmoothSheet.SPARKLE.renderType();
    }

    public static final class Provider implements ParticleProvider<ColorParticleOption> {
        @Override
        public Particle createParticle(ColorParticleOption colour, ClientLevel level, double x, double y, double z,
                                       double dx, double dy, double dz) {
            SparkleParticle sparkle = new SparkleParticle(level, x, y, z, dx, dy, dz);
            sparkle.setColor(colour.getRed(), colour.getGreen(), colour.getBlue());
            return sparkle;
        }
    }
}
