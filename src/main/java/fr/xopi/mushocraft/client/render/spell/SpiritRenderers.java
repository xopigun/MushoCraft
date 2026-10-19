package fr.xopi.mushocraft.client.render.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.xopi.mushocraft.magic.spell.SpellSchool;
import fr.xopi.mushocraft.magic.summon.LamplightSpirit;
import fr.xopi.mushocraft.magic.summon.SummonedSpirit;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Summoned spirits are made of mana, and drawn as such ({@link ManaOrb}): lamplight spirits as small warm lights,
 * fighting spirits as larger orbs in the colour of summoning magic that pulse as they move.
 */
public final class SpiritRenderers {
    private static final int LAMPLIGHT_COLOUR = 0xFFF2C0;

    private SpiritRenderers() {
    }

    public static final class Lamplight extends EntityRenderer<LamplightSpirit> {
        public Lamplight(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public void render(LamplightSpirit spirit, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers,
                           int light) {
            float time = spirit.tickCount + partialTicks;
            poseStack.pushPose();
            poseStack.translate(0.0, 0.15, 0.0);
            ManaOrb.render(poseStack, buffers, ManaOrb.MANA, LAMPLIGHT_COLOUR, 0.12F * (1.0F + 0.1F * Mth.sin(time * 0.5F)), 1.0F, time);
            poseStack.popPose();
            super.render(spirit, yaw, partialTicks, poseStack, buffers, light);
        }

        @Override
        public ResourceLocation getTextureLocation(LamplightSpirit spirit) {
            return ManaOrb.MANA;
        }
    }

    public static final class Fighting extends EntityRenderer<SummonedSpirit> {
        public Fighting(EntityRendererProvider.Context context) {
            super(context);
            shadowRadius = 0.2F;
        }

        @Override
        public void render(SummonedSpirit spirit, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers,
                           int light) {
            float time = spirit.tickCount + partialTicks;
            float hurt = spirit.hurtTime > 0 ? 1.15F : 1.0F;
            poseStack.pushPose();
            poseStack.translate(0.0, spirit.getBbHeight() / 2.0, 0.0);
            ManaOrb.render(poseStack, buffers, ManaOrb.MANA, SpellSchool.SUMMONING.colour(),
                    0.3F * hurt * (1.0F + 0.08F * Mth.sin(time * 0.3F)), 0.9F, time);
            poseStack.popPose();
            super.render(spirit, yaw, partialTicks, poseStack, buffers, light);
        }

        @Override
        public ResourceLocation getTextureLocation(SummonedSpirit spirit) {
            return ManaOrb.MANA;
        }
    }
}
