package fr.xopi.mushocraft.sword.technique;

import fr.xopi.mushocraft.compat.epicfight.EpicFightAnimations;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;

import java.util.function.Supplier;

/**
 * A swing so violent that its shockwave sweeps away everyone in front of the user (North God Style, Moonlight Cleave).
 */
public final class ShockwaveAction implements TechniqueAction {
    /** Only creatures roughly in front of the user (within about 70 degrees of the look direction) are caught. */
    private static final double FRONT_CONE_COS = 0.35;

    private final Supplier<? extends AssetAccessor<? extends StaticAnimation>> animation;
    private final double radius;
    private final float damageMultiplier;
    private final double launch;

    public ShockwaveAction(Supplier<? extends AssetAccessor<? extends StaticAnimation>> animation, double radius,
                           float damageMultiplier, double launch) {
        this.animation = animation;
        this.radius = radius;
        this.damageMultiplier = damageMultiplier;
        this.launch = launch;
    }

    @Override
    public double maxRange() {
        return radius;
    }

    @Override
    public boolean perform(TechniqueContext context) {
        if (!EpicFightAnimations.canAct(context.user())) {
            return false;
        }
        EpicFightAnimations.play(context.user(), animation.get());
        Vec3 origin = context.user().position();
        Vec3 look = context.heading();
        float damage = (float) (context.user().getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier * context.power());
        for (LivingEntity target : context.level().getEntitiesOfClass(LivingEntity.class,
                context.user().getBoundingBox().inflate(radius), entity -> entity != context.user())) {
            Vec3 toTarget = target.position().subtract(origin).multiply(1.0, 0.0, 1.0);
            if (toTarget.lengthSqr() > radius * radius || toTarget.normalize().dot(look) < FRONT_CONE_COS) {
                continue;
            }
            target.hurt(context.meleeSource(), damage);
            Vec3 push = toTarget.normalize().scale(launch);
            target.push(push.x, launch * 0.6, push.z);
            target.hurtMarked = true;
        }
        for (int step = 1; step <= (int) radius; step++) {
            Vec3 point = origin.add(look.scale(step)).add(0.0, 0.8, 0.0);
            context.level().sendParticles(ParticleTypes.SWEEP_ATTACK, point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
            context.level().sendParticles(ParticleTypes.CLOUD, point.x, point.y, point.z, 4, 0.3, 0.3, 0.3, 0.05);
        }
        context.level().playSound(null, context.user().blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.PLAYERS, 1.2F, 0.6F);
        return true;
    }
}
