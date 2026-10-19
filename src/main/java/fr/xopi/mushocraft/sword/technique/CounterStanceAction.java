package fr.xopi.mushocraft.sword.technique;

import fr.xopi.mushocraft.core.Rank;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * Takes a receiving stance: the next attack within the window is redirected and answered by a counter
 * (Water God Style, Flow). From Saint upward the stance also turns away projectiles: at high levels the style
 * can redirect even magical attacks (wiki, Water God Style).
 */
public final class CounterStanceAction implements TechniqueAction {
    private final int windowTicks;
    private final float counterMultiplier;
    private final Rank deflectsProjectilesFrom;

    public CounterStanceAction(int windowTicks, float counterMultiplier, Rank deflectsProjectilesFrom) {
        this.windowTicks = windowTicks;
        this.counterMultiplier = counterMultiplier;
        this.deflectsProjectilesFrom = deflectsProjectilesFrom;
    }

    @Override
    public boolean perform(TechniqueContext context) {
        CounterStances.enter(context.user(), windowTicks, (float) (counterMultiplier * context.power()),
                context.styleRank().isAtLeast(deflectsProjectilesFrom));
        context.level().sendParticles(ParticleTypes.BUBBLE_POP, context.user().getX(), context.user().getY() + 1.0,
                context.user().getZ(), 12, 0.4, 0.4, 0.4, 0.02);
        context.level().playSound(null, context.user().blockPosition(), SoundEvents.ARMOR_EQUIP_IRON.value(),
                SoundSource.PLAYERS, 1.0F, 1.3F);
        return true;
    }
}
