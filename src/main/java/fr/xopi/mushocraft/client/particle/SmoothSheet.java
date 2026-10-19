package fr.xopi.mushocraft.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import fr.xopi.mushocraft.MushoCraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * A flipbook kept out of the particle atlas, in a texture of its own, sampled smoothly: linear filtering, no mipmaps.
 * The atlas is read texel by texel with mipmaps; a large plane seen edge-on drops to a small mipmap, and its edges
 * turn to stairs. Big oriented planes (shock waves, slashes) use a sheet instead. The frames sit side by side, left to
 * right, in one strip.
 */
public final class SmoothSheet {
    /** Shock waves: rings of air that spread and tear (textures/effect/shock_wave.png, drawn for MushoCraft). */
    public static final SmoothSheet SHOCK_WAVE = new SmoothSheet("shock_wave", 6, false);
    /** Slashes: crescents that sweep and dissolve (textures/effect/slash.png, drawn for MushoCraft). */
    public static final SmoothSheet SLASH = new SmoothSheet("slash", 8, true);
    /** Hit sparks: rays and a torn ring, tinted by the element (textures/effect/impact.png, drawn for MushoCraft). */
    public static final SmoothSheet IMPACT = new SmoothSheet("impact", 8, false);
    /** Explosions: a white-hot ball, an orange ball that tears open, smoke (textures/effect/blast.png, drawn for MushoCraft). */
    public static final SmoothSheet BLAST = new SmoothSheet("blast", 12, false);

    private final ResourceLocation texture;
    private final int frames;
    private final ParticleRenderType renderType;

    private SmoothSheet(String name, int frames, boolean additive) {
        this.texture = MushoCraft.id("textures/effect/" + name + ".png");
        this.frames = frames;
        this.renderType = new ParticleRenderType() {
            @Override
            public BufferBuilder begin(Tesselator tesselator, TextureManager textures) {
                RenderSystem.setShader(GameRenderer::getParticleShader);
                AbstractTexture sheet = textures.getTexture(texture);
                sheet.setFilter(true, false);
                RenderSystem.setShaderTexture(0, sheet.getId());
                RenderSystem.enableBlend();
                if (additive) {
                    // added to the light behind, as FireParticles.ADDITIVE
                    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
                    RenderSystem.depthMask(false);
                } else {
                    // as the vanilla translucent particle sheet
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.depthMask(true);
                }
                return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
            }

            @Override
            public String toString() {
                return "MUSHOCRAFT_SHEET_" + name;
            }
        };
    }

    public ParticleRenderType renderType() {
        return renderType;
    }

    /** The frame shown at this point of a life (0 to 1). */
    public int frame(float life) {
        return Mth.clamp((int) (life * frames), 0, frames - 1);
    }

    public float u0(int frame) {
        return (float) frame / frames;
    }

    public float u1(int frame) {
        return (frame + 1.0F) / frames;
    }
}
