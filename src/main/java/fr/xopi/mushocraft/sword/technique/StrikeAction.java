package fr.xopi.mushocraft.sword.technique;

import fr.xopi.mushocraft.compat.epicfight.EpicFightAnimations;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Plays an Epic Fight attack animation whose hits are multiplied, with an optional effect on whoever is hit
 * (Arm Chop, Longsword of Silence, Longsword of Light). An optional lunge throws the user forward first
 * (Four Legged Form).
 */
public final class StrikeAction implements TechniqueAction {
    private static final int STRIKE_WINDOW_TICKS = 40;

    private final Supplier<? extends AssetAccessor<? extends StaticAnimation>> animation;
    private final float damageMultiplier;
    private final double lunge;
    @Nullable
    private final Consumer<LivingEntity> onHit;
    @Nullable
    private final SoundEvent sound;

    private StrikeAction(Builder builder) {
        this.animation = builder.animation;
        this.damageMultiplier = builder.damageMultiplier;
        this.lunge = builder.lunge;
        this.onHit = builder.onHit;
        this.sound = builder.sound;
    }

    public static Builder of(Supplier<? extends AssetAccessor<? extends StaticAnimation>> animation, float damageMultiplier) {
        return new Builder(animation, damageMultiplier);
    }

    /** A lunge carries the blow further. */
    @Override
    public double maxRange() {
        return 3.5 + lunge * 3.0;
    }

    @Override
    public boolean perform(TechniqueContext context) {
        if (!EpicFightAnimations.canAct(context.user())) {
            return false;
        }
        AssetAccessor<? extends StaticAnimation> accessor = animation.get();
        TechniqueStrikes.arm(context.user(), accessor.registryName(), (float) (damageMultiplier * context.power()),
                STRIKE_WINDOW_TICKS, onHit);
        if (lunge > 0.0) {
            Vec3 look = context.heading();
            context.user().push(look.x * lunge, 0.15, look.z * lunge);
            context.user().hurtMarked = true;
        }
        if (sound != null) {
            context.level().playSound(null, context.user().blockPosition(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return EpicFightAnimations.play(context.user(), accessor);
    }

    public static final class Builder {
        private final Supplier<? extends AssetAccessor<? extends StaticAnimation>> animation;
        private final float damageMultiplier;
        private double lunge;
        private Consumer<LivingEntity> onHit;
        private SoundEvent sound;

        private Builder(Supplier<? extends AssetAccessor<? extends StaticAnimation>> animation, float damageMultiplier) {
            this.animation = animation;
            this.damageMultiplier = damageMultiplier;
        }

        public Builder lunge(double speed) {
            this.lunge = speed;
            return this;
        }

        public Builder onHit(Consumer<LivingEntity> effect) {
            this.onHit = effect;
            return this;
        }

        public Builder sound(SoundEvent value) {
            this.sound = value;
            return this;
        }

        public StrikeAction build() {
            return new StrikeAction(this);
        }
    }
}
