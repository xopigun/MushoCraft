package fr.xopi.mushocraft.life;

import fr.xopi.mushocraft.MushoCraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Applies the size given by the growth curve through the vanilla {@code scale} attribute.
 * The modifier is transient and re-applied regularly, so it never goes stale after a respawn or a config change.
 */
public final class BodyGrowth {
    private static final ResourceLocation MODIFIER_ID = MushoCraft.id("life/growth");
    private static final double TOLERANCE = 1.0E-3;

    private final GrowthCurve curve;

    public BodyGrowth(GrowthCurve curve) {
        this.curve = curve;
    }

    public void apply(ServerPlayer player, double ageInYears) {
        AttributeInstance scale = player.getAttribute(Attributes.SCALE);
        if (scale == null) {
            return;
        }
        double target = curve.scaleAt(ageInYears) - 1.0;
        AttributeModifier current = scale.getModifier(MODIFIER_ID);
        if (Math.abs(target) < TOLERANCE) {
            if (current != null) {
                scale.removeModifier(MODIFIER_ID);
            }
            return;
        }
        if (current == null || Math.abs(current.amount() - target) >= TOLERANCE) {
            scale.addOrUpdateTransientModifier(
                    new AttributeModifier(MODIFIER_ID, target, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }
}
