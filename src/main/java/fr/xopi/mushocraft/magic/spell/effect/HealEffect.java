package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.magic.fx.SpellFx;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Heals the creature the caster is looking at, or the caster when nobody is in reach.
 */
public final class HealEffect implements SpellEffect {
    /** Soft green-gold: the light of healing magic. */
    private static final int HEALING_LIGHT = 0xB8FF9A;

    private final float amount;
    private final double reach;

    public HealEffect(float amount, double reach) {
        this.amount = amount;
        this.reach = reach;
    }

    @Override
    public boolean cast(SpellContext context) {
        LivingEntity target = lookedAt(context);
        target.heal((float) (amount * context.power()));
        SpellFx.healingLight(context.level(), target, HEALING_LIGHT);
        context.level().playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE,
                SoundSource.PLAYERS, 1.0F, 1.5F);
        return true;
    }

    private LivingEntity lookedAt(SpellContext context) {
        Vec3 start = context.eyes();
        Vec3 end = start.add(context.look().scale(reach));
        AABB area = context.caster().getBoundingBox().expandTowards(context.look().scale(reach)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(context.caster(), start, end, area,
                entity -> entity instanceof LivingEntity && entity.isAlive(), reach * reach);
        return hit != null && hit.getEntity() instanceof LivingEntity living ? living : context.caster();
    }
}
