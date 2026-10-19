package fr.xopi.mushocraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.util.Mth;

/**
 * A mote of soft light drifting up and twinkling, as healing and holy magic gather on a body. Tinted by its option,
 * added to the light behind it, it swells, then shrinks away.
 */
public final class RisingLight extends TextureSheetParticle {
    private final float startSize;
    private final float red;
    private final float green;
    private final float blue;

    RisingLight(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, ColorParticleOption colour,
                SpriteSet sprites) {
        super(level, x, y, z);
        this.xd = dx + (random.nextDouble() - 0.5) * 0.01;
        this.yd = 0.02 + random.nextDouble() * 0.03 + dy;
        this.zd = dz + (random.nextDouble() - 0.5) * 0.01;
        this.friction = 0.96F;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.lifetime = 18 + random.nextInt(14);
        this.startSize = 0.06F + random.nextFloat() * 0.08F;
        this.quadSize = startSize;
        this.red = colour.getRed();
        this.green = colour.getGreen();
        this.blue = colour.getBlue();
        pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (removed) {
            return;
        }
        float life = (float) age / lifetime;
        quadSize = startSize * (life < 0.3F ? 0.5F + life / 0.6F : 1.0F - (life - 0.3F) / 0.7F * 0.8F);
        float twinkle = 0.7F + 0.3F * Mth.sin(age * 1.3F + startSize * 50.0F);
        float glow = (life < 0.15F ? life / 0.15F : 1.0F - Math.max(0.0F, life - 0.6F) / 0.4F) * twinkle;
        setColor(red * glow, green * glow, blue * glow);
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return FireParticles.ADDITIVE;
    }

    public record Provider(SpriteSet sprites) implements ParticleProvider<ColorParticleOption> {
        @Override
        public Particle createParticle(ColorParticleOption colour, ClientLevel level, double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new RisingLight(level, x, y, z, dx, dy, dz, colour, sprites);
        }
    }
}
