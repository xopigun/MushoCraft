package fr.xopi.mushocraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.ColorParticleOption;

/**
 * A mote of mana drawn into a caster's hand as the spell takes shape. Its speed is aimed so that it reaches the hand
 * at the end of its short life; it brightens as it arrives. Tinted with its spell school's colour.
 */
public final class ManaMote extends SingleQuadParticle {
    public static final int LIFETIME = 10;
    private static final SmoothSheet SHEET = SmoothSheet.MOTE;

    ManaMote(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, ColorParticleOption colour) {
        super(level, x, y, z);
        this.xd = dx;
        this.yd = dy;
        this.zd = dz;
        this.friction = 1.0F;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.lifetime = LIFETIME;
        this.quadSize = 0.05F + random.nextFloat() * 0.03F;
        setColor(colour.getRed(), colour.getGreen(), colour.getBlue());
        this.alpha = 0.0F;
    }

    @Override
    protected float getU0() {
        return SHEET.u0(0);
    }

    @Override
    protected float getU1() {
        return SHEET.u1(0);
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
    public void tick() {
        super.tick();
        float life = (float) age / lifetime;
        alpha = Math.min(1.0F, life * 3.0F);
        quadSize *= 0.97F;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return SHEET.renderType();
    }

    public static final class Provider implements ParticleProvider<ColorParticleOption> {
        @Override
        public Particle createParticle(ColorParticleOption colour, ClientLevel level, double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new ManaMote(level, x, y, z, dx, dy, dz, colour);
        }
    }
}
