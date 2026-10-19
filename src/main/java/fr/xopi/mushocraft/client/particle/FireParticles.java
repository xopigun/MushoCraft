package fr.xopi.mushocraft.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import fr.xopi.mushocraft.core.MushoParticles;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * MushoCraft's fire particles. Fire is drawn additively: it glows, brightens what lies behind it, and a dying flame
 * darkens rather than fades. Each flame takes one of two tints at random (a pale heart, a deeper orange), which bands the
 * fire with colour for free; a dark enough flame hands over to a puff of smoke. Smoke is plain translucent and lit by
 * the world. Techniques noted in docs/VISUELS_REFERENCE.md.
 */
public final class FireParticles {
    private static final int FULL_BRIGHT = 0xF000F0;

    /** Particles added to the light behind them, without hiding what is behind: glow and fire. */
    public static final ParticleRenderType ADDITIVE = new ParticleRenderType() {
        @Override
        public BufferBuilder begin(Tesselator tesselator, TextureManager textures) {
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            RenderSystem.depthMask(false);
            return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public String toString() {
            return "MUSHOCRAFT_ADDITIVE";
        }
    };

    /** The two tints of ordinary fire: the pale heart and the deep orange. */
    private static final float[][] FIRE_TINTS = {{1.0F, 0.68F, 0.22F}, {0.95F, 0.3F, 0.07F}};

    private FireParticles() {
    }

    /**
     * A tongue of flame: plays its flicker frames over its life, rises, and darkens away; when dark enough, some leave a
     * puff of smoke in their place.
     */
    public static final class Flame extends TextureSheetParticle {
        private final SpriteSet sprites;
        private final float startSize;
        private final float[] tint;
        private final boolean smokes;

        Flame(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, SpriteSet sprites) {
            super(level, x, y, z, dx, dy, dz);
            this.sprites = sprites;
            this.xd = dx + (random.nextDouble() - 0.5) * 0.02;
            this.yd = dy + 0.02 + random.nextDouble() * 0.03;
            this.zd = dz + (random.nextDouble() - 0.5) * 0.02;
            this.friction = 0.9F;
            this.gravity = -0.015F;
            this.lifetime = 11 + random.nextInt(10);
            this.startSize = 0.22F + random.nextFloat() * 0.2F;
            this.quadSize = startSize;
            this.roll = (random.nextFloat() - 0.5F) * 0.3F;
            this.oRoll = roll;
            this.tint = FIRE_TINTS[random.nextInt(FIRE_TINTS.length)];
            this.smokes = random.nextFloat() < 0.15F;
            setColor(tint[0], tint[1], tint[2]);
            setSpriteFromAge(sprites);
        }

        @Override
        public void tick() {
            super.tick();
            if (removed) {
                return;
            }
            setSpriteFromAge(sprites);
            float life = (float) age / lifetime;
            quadSize = startSize * (life < 0.2F ? 0.6F + life * 2.0F : 1.0F - (life - 0.2F) * 0.7F);
            // additive: darker is fainter; a flash of brightness first, then it dies down
            float glow = Math.max(0.08F, 1.0F - life) * (1.0F + 0.5F * life);
            setColor(tint[0] * glow, tint[1] * glow, tint[2] * glow);
            if (smokes && glow < 0.3F) {
                level.addParticle(MushoParticles.SMOKE.get(), x, y, z, xd * 0.5, 0.01, zd * 0.5);
                remove();
            }
        }

        @Override
        protected int getLightColor(float partialTick) {
            return FULL_BRIGHT;
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ADDITIVE;
        }
    }

    /** A spark: tiny, bright, thrown out, then it falls and dies out. */
    public static final class Ember extends TextureSheetParticle {
        private final SpriteSet sprites;
        private final float[] tint;

        Ember(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, SpriteSet sprites) {
            super(level, x, y, z, dx, dy, dz);
            this.sprites = sprites;
            this.xd = dx + (random.nextDouble() - 0.5) * 0.15;
            this.yd = dy + random.nextDouble() * 0.12;
            this.zd = dz + (random.nextDouble() - 0.5) * 0.15;
            this.friction = 0.94F;
            this.gravity = 0.25F;
            this.lifetime = 16 + random.nextInt(20);
            this.quadSize = 0.05F + random.nextFloat() * 0.04F;
            this.tint = FIRE_TINTS[random.nextInt(FIRE_TINTS.length)];
            setColor(tint[0], tint[1], tint[2]);
            setSprite(sprites.get(0, 1));
        }

        @Override
        public void tick() {
            super.tick();
            if (!removed) {
                // embers twinkle as they cool
                setSprite(sprites.get(random.nextInt(3) == 0 ? 1 : 0, 1));
                float life = (float) age / lifetime;
                float glow = life > 0.5F ? 1.0F - (life - 0.5F) * 2.0F : 1.0F;
                setColor(tint[0] * glow, tint[1] * glow, tint[2] * glow);
            }
        }

        @Override
        protected int getLightColor(float partialTick) {
            return FULL_BRIGHT;
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ADDITIVE;
        }
    }

    /** A puff of smoke: swells and rises slowly, then thins away. */
    public static final class Smoke extends TextureSheetParticle {
        private final float startSize;

        Smoke(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, SpriteSet sprites) {
            super(level, x, y, z, dx, dy, dz);
            this.xd = dx + (random.nextDouble() - 0.5) * 0.03;
            this.yd = dy + 0.01 + random.nextDouble() * 0.02;
            this.zd = dz + (random.nextDouble() - 0.5) * 0.03;
            this.friction = 0.92F;
            this.gravity = -0.004F;
            this.lifetime = 28 + random.nextInt(24);
            this.startSize = 0.35F + random.nextFloat() * 0.3F;
            this.quadSize = startSize;
            this.roll = random.nextFloat() * Mth.TWO_PI;
            this.oRoll = roll;
            pickSprite(sprites);
        }

        @Override
        public void tick() {
            super.tick();
            if (!removed) {
                float life = (float) age / lifetime;
                quadSize = startSize * (1.0F + life * 1.6F);
                alpha = life < 0.1F ? life * 10.0F * 0.85F : 0.85F * (1.0F - (life - 0.1F) / 0.9F);
                oRoll = roll;
                roll += 0.01F;
            }
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }
    }

    /** The white-hot flash of a blast: huge, a few ticks long, swelling as it fades. */
    public static final class Flash extends TextureSheetParticle {
        private final float startSize;

        Flash(ClientLevel level, double x, double y, double z, double size, SpriteSet sprites) {
            super(level, x, y, z);
            this.xd = 0.0;
            this.yd = 0.0;
            this.zd = 0.0;
            this.gravity = 0.0F;
            this.lifetime = 5;
            this.startSize = (float) Math.max(0.5, size);
            this.quadSize = startSize;
            this.roll = random.nextFloat() * Mth.TWO_PI;
            this.oRoll = roll;
            pickSprite(sprites);
        }

        @Override
        public void tick() {
            super.tick();
            float life = (float) age / lifetime;
            quadSize = startSize * (1.0F + life * 0.6F);
            // a short, warm flash: additive light piles up fast, so it stays well below white
            float glow = 0.35F * (1.0F - life * life);
            setColor(glow, glow * 0.9F, glow * 0.7F);
        }

        @Override
        protected int getLightColor(float partialTick) {
            return FULL_BRIGHT;
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ADDITIVE;
        }
    }

    /** A ring of air racing out flat along the ground, drawn lying down rather than facing the eye. */
    public static final class ShockRing extends TextureSheetParticle {
        private final float finalRadius;

        ShockRing(ClientLevel level, double x, double y, double z, double radius, SpriteSet sprites) {
            super(level, x, y, z);
            this.xd = 0.0;
            this.yd = 0.0;
            this.zd = 0.0;
            this.gravity = 0.0F;
            this.lifetime = 9;
            this.finalRadius = (float) Math.max(1.0, radius);
            pickSprite(sprites);
        }

        @Override
        public void tick() {
            super.tick();
            alpha = 1.0F - (float) age / lifetime;
        }

        @Override
        public void render(VertexConsumer consumer, Camera camera, float partialTicks) {
            float life = Math.min(1.0F, (age + partialTicks) / lifetime);
            // fast at first, slowing as the air spreads
            quadSize = finalRadius * (1.0F - (1.0F - life) * (1.0F - life));
            Vec3 eye = camera.getPosition();
            float x = (float) (Mth.lerp(partialTicks, xo, this.x) - eye.x);
            float y = (float) (Mth.lerp(partialTicks, yo, this.y) - eye.y);
            float z = (float) (Mth.lerp(partialTicks, zo, this.z) - eye.z);
            Quaternionf flat = new Quaternionf().rotationX(-Mth.HALF_PI);
            int light = getLightColor(partialTicks);
            // both faces, so it shows from above and below
            renderFlat(consumer, flat, x, y, z, light);
            renderFlat(consumer, new Quaternionf().rotationX(Mth.HALF_PI), x, y, z, light);
        }

        private void renderFlat(VertexConsumer consumer, Quaternionf rotation, float x, float y, float z, int light) {
            float[][] corners = {{-1, -1, getU1(), getV1()}, {-1, 1, getU1(), getV0()}, {1, 1, getU0(), getV0()}, {1, -1, getU0(), getV1()}};
            for (float[] corner : corners) {
                Vector3f point = new Vector3f(corner[0], corner[1], 0.0F).rotate(rotation).mul(quadSize).add(x, y, z);
                consumer.addVertex(point.x, point.y, point.z).setUv(corner[2], corner[3]).setColor(rCol, gCol, bCol, alpha)
                        .setLight(light);
            }
        }

        @Override
        protected int getLightColor(float partialTick) {
            return FULL_BRIGHT;
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }
    }

    public record FlameProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new Flame(level, x, y, z, dx, dy, dz, sprites);
        }
    }

    public record EmberProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new Ember(level, x, y, z, dx, dy, dz, sprites);
        }
    }

    public record SmokeProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new Smoke(level, x, y, z, dx, dy, dz, sprites);
        }
    }

    public record FlashProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double size, double dy, double dz) {
            return new Flash(level, x, y, z, size, sprites);
        }
    }

    public record ShockRingProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double radius, double dy, double dz) {
            return new ShockRing(level, x, y, z, radius, sprites);
        }
    }
}
