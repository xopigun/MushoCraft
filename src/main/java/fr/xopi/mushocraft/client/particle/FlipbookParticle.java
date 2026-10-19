package fr.xopi.mushocraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

/**
 * A drawn impact played as a flipbook (technique of the visual reference, docs/VISUELS_REFERENCE.md): a few frames of
 * a strong, hard-edged drawing over a very short life, turned at random, glowing. A hit spark (tinted by its element)
 * or an explosion (drawn in its own colours). Its size is given by the x speed of the particle.
 */
public final class FlipbookParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    private FlipbookParticle(ClientLevel level, double x, double y, double z, double size, int lifetime, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.lifetime = lifetime;
        this.quadSize = (float) Math.max(0.2, size);
        this.roll = random.nextFloat() * Mth.TWO_PI;
        this.oRoll = roll;
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
    protected int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    /** A hit spark, tinted by the element that struck. */
    public record ImpactProvider(SpriteSet sprites) implements ParticleProvider<ColorParticleOption> {
        @Override
        public Particle createParticle(ColorParticleOption colour, ClientLevel level, double x, double y, double z,
                                       double size, double dy, double dz) {
            FlipbookParticle spark = new FlipbookParticle(level, x, y, z, size, 6, sprites);
            spark.setColor(colour.getRed(), colour.getGreen(), colour.getBlue());
            return spark;
        }
    }

    /** An explosion, in its own colours. */
    public record BlastProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double size, double dy, double dz) {
            return new FlipbookParticle(level, x, y, z, size, 14, sprites);
        }
    }
}
