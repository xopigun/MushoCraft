package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.core.MushoDamageTypes;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Something springs up from the ground where the caster aims: a shaft of ice (Ice Pillar), an earthen spike
 * (Earth Lance) or a column of fire (Flame Pillar). Creatures standing there are hurt and thrown upward.
 * Solid columns are conjured blocks that dissolve after a few seconds.
 */
public final class EruptionEffect implements SpellEffect {
    private static final int COLUMN_LIFETIME_TICKS = 80;
    private static final double HIT_RADIUS = 1.2;

    @Nullable
    private final Supplier<? extends Block> column;
    private final int height;
    private final float damage;
    private final double launch;
    private final int igniteSeconds;
    private final double range;
    private final ParticleOptions particle;
    private final SoundEvent sound;

    public EruptionEffect(@Nullable Supplier<? extends Block> column, int height, float damage, double launch,
                          int igniteSeconds, double range, ParticleOptions particle, SoundEvent sound) {
        this.column = column;
        this.height = height;
        this.damage = damage;
        this.launch = launch;
        this.igniteSeconds = igniteSeconds;
        this.range = range;
        this.particle = particle;
        this.sound = sound;
    }

    @Override
    public boolean cast(SpellContext context) {
        eruptAt(context, SpellTargeting.groundBelow(context, SpellTargeting.aimedPoint(context, range)));
        return true;
    }

    /** The eruption itself, at a given spot (also used by {@link ScatteredEruptionEffect}). */
    void eruptAt(SpellContext context, BlockPos base) {
        ServerLevel level = context.level();
        Vec3 center = Vec3.atBottomCenterOf(base);
        AABB area = new AABB(center.x - HIT_RADIUS, center.y, center.z - HIT_RADIUS,
                center.x + HIT_RADIUS, center.y + height, center.z + HIT_RADIUS);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, entity -> entity != context.caster())) {
            target.hurt(MushoDamageTypes.spell(level, context.caster(), context.caster()), (float) (damage * context.power()));
            target.push(0.0, launch, 0.0);
            target.hurtMarked = true;
            if (igniteSeconds > 0) {
                target.igniteForSeconds(igniteSeconds);
            }
        }
        if (column != null) {
            for (int up = 0; up < height; up++) {
                BlockPos pos = base.above(up);
                if (level.getBlockState(pos).canBeReplaced()) {
                    level.setBlockAndUpdate(pos, column.get().defaultBlockState());
                    level.scheduleTick(pos, column.get(), COLUMN_LIFETIME_TICKS);
                }
            }
        }
        for (int up = 0; up < height; up++) {
            level.sendParticles(particle, center.x, center.y + up + 0.5, center.z, 10, 0.35, 0.4, 0.35, 0.02);
        }
        level.playSound(null, base, sound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    double range() {
        return range;
    }
}
