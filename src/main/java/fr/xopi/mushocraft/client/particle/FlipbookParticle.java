package fr.xopi.mushocraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

/**
 * A drawn impact played as a flipbook (technique of the visual reference, docs/VISUELS_REFERENCE.md): a few frames of
 * a strong, hard-edged drawing over a very short life, turned at random, glowing. A hit spark (tinted by its element)
 * or an explosion (drawn in its own colours). Its size is given by the x speed of the particle. Its frames come from a
 * {@link SmoothSheet}, so its outline stays clean when it is drawn large.
 */
public final class FlipbookParticle extends SingleQuadParticle {
    private final SmoothSheet sheet;
    private int frame;

    private FlipbookParticle(ClientLevel level, double x, double y, double z, double size, int lifetime, SmoothSheet sheet) {
        super(level, x, y, z);
        this.sheet = sheet;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.lifetime = lifetime;
        this.quadSize = (float) Math.max(0.2, size);
        this.roll = random.nextFloat() * Mth.TWO_PI;
        this.oRoll = roll;
    }

    @Override
    public void tick() {
        super.tick();
        frame = sheet.frame((float) age / lifetime);
    }

    @Override
    protected float getU0() {
        return sheet.u0(frame);
    }

    @Override
    protected float getU1() {
        return sheet.u1(frame);
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
        return 0xF000F0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return sheet.renderType();
    }

    /** A hit spark, tinted by the element that struck. */
    public static final class ImpactProvider implements ParticleProvider<ColorParticleOption> {
        @Override
        public Particle createParticle(ColorParticleOption colour, ClientLevel level, double x, double y, double z,
                                       double size, double dy, double dz) {
            FlipbookParticle spark = new FlipbookParticle(level, x, y, z, size, 6, SmoothSheet.IMPACT);
            spark.setColor(colour.getRed(), colour.getGreen(), colour.getBlue());
            return spark;
        }
    }

    /** An explosion, in its own colours. */
    public static final class BlastProvider implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double size, double dy, double dz) {
            return new FlipbookParticle(level, x, y, z, size, 14, SmoothSheet.BLAST);
        }
    }
}
