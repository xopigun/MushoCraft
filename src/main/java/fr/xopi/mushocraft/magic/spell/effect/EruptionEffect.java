package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.core.MushoDamageTypes;
import fr.xopi.mushocraft.magic.block.ConjuredBlock;
import fr.xopi.mushocraft.magic.fx.CameraShake;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Something springs up from the ground where the caster aims: a shaft of ice (Ice Pillar), an earthen spike
 * (Earth Lance) or a column of fire (Flame Pillar). Creatures standing there are hurt and thrown upward.
 * Solid columns are conjured blocks that dissolve after a few seconds. When the eruption has a {@link EruptionVisual look}
 * (an animated Blockbench pillar, a mesh of flame), those blocks are hidden and the look is what is seen.
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
    @Nullable
    private final EruptionVisual look;

    public EruptionEffect(@Nullable Supplier<? extends Block> column, int height, float damage, double launch,
                          int igniteSeconds, double range, ParticleOptions particle, SoundEvent sound) {
        this(column, height, damage, launch, igniteSeconds, range, particle, sound, null);
    }

    private EruptionEffect(@Nullable Supplier<? extends Block> column, int height, float damage, double launch,
                           int igniteSeconds, double range, ParticleOptions particle, SoundEvent sound,
                           @Nullable EruptionVisual look) {
        this.column = column;
        this.height = height;
        this.damage = damage;
        this.launch = launch;
        this.igniteSeconds = igniteSeconds;
        this.range = range;
        this.particle = particle;
        this.sound = sound;
        this.look = look;
    }

    /** The same eruption, seen as an animated model. */
    public EruptionEffect lookingLike(EruptionVisual look) {
        return new EruptionEffect(column, height, damage, launch, igniteSeconds, range, particle, sound, look);
    }

    @Override
    public boolean cast(SpellContext context) {
        BlockPos base = SpellTargeting.groundBelow(context, SpellTargeting.aimedPoint(context, range));
        eruptAt(context, base);
        CameraShake.around(context.level(), Vec3.atCenterOf(base), 0.5F, 8, 14.0);
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
                    level.setBlockAndUpdate(pos, columnState());
                    level.scheduleTick(pos, column.get(), COLUMN_LIFETIME_TICKS);
                }
            }
        }
        if (look != null) {
            look.raise(level, center, (float) (height * context.scale()), COLUMN_LIFETIME_TICKS, context.caster().getRandom());
        }
        for (int up = 0; up < height; up++) {
            level.sendParticles(particle, center.x, center.y + up + 0.5, center.z, 10, 0.35, 0.4, 0.35, 0.02);
        }
        level.playSound(null, base, sound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private BlockState columnState() {
        Block block = column.get();
        return look != null && look.hidesColumn() && block instanceof ConjuredBlock conjured ? conjured.hiddenState() : block.defaultBlockState();
    }

    double range() {
        return range;
    }
}
