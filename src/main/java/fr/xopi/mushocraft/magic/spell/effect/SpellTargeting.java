package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.magic.spell.SpellContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Where a spell aimed at a spot lands: the creature looked at, else the block looked at, else the end of reach.
 */
final class SpellTargeting {
    private SpellTargeting() {
    }

    static Vec3 aimedPoint(SpellContext context, double range) {
        Vec3 start = context.eyes();
        Vec3 end = start.add(context.look().scale(range));
        BlockHitResult block = context.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, context.caster()));
        Vec3 blockEnd = block.getType() == HitResult.Type.MISS ? end : block.getLocation();
        AABB area = context.caster().getBoundingBox().expandTowards(blockEnd.subtract(start)).inflate(1.0);
        EntityHitResult entity = ProjectileUtil.getEntityHitResult(context.caster(), start, blockEnd, area,
                target -> target.isAlive() && target.isPickable(), start.distanceToSqr(blockEnd));
        return entity != null ? entity.getEntity().position() : blockEnd;
    }

    /** The creature looked at within reach, if any (never the caster). */
    static Optional<LivingEntity> aimedCreature(SpellContext context, double range) {
        Vec3 start = context.eyes();
        Vec3 end = start.add(context.look().scale(range));
        BlockHitResult block = context.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, context.caster()));
        Vec3 blockEnd = block.getType() == HitResult.Type.MISS ? end : block.getLocation();
        AABB area = context.caster().getBoundingBox().expandTowards(blockEnd.subtract(start)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(context.caster(), start, blockEnd, area,
                target -> target instanceof LivingEntity && target.isAlive() && target != context.caster(), start.distanceToSqr(blockEnd));
        return hit == null ? Optional.empty() : Optional.of((LivingEntity) hit.getEntity());
    }

    /** First block position at or below the point that stands on solid ground, searching a few blocks down. */
    static BlockPos groundBelow(SpellContext context, Vec3 point) {
        BlockPos pos = BlockPos.containing(point);
        for (int i = 0; i < 8; i++) {
            if (!context.level().getBlockState(pos.below()).isAir()) {
                return pos;
            }
            pos = pos.below();
        }
        return BlockPos.containing(point);
    }
}
