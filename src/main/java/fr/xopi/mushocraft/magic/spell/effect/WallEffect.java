package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.magic.block.ConjuredBlock;
import fr.xopi.mushocraft.magic.entity.PillarKind;
import fr.xopi.mushocraft.magic.entity.SpellPillar;
import fr.xopi.mushocraft.magic.fx.CameraShake;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Raises a temporary wall facing the caster's look (Earth Wall). Only replaces air and plants,
 * and each block removes itself after the duration (see {@code ConjuredBlock}).
 */
public final class WallEffect implements SpellEffect {
    private final Supplier<? extends Block> block;
    private final int halfWidth;
    private final int height;
    private final int distance;
    private final int durationTicks;
    private final SoundEvent sound;
    @Nullable
    private final PillarKind look;

    public WallEffect(Supplier<? extends Block> block, int halfWidth, int height, int distance, int durationTicks,
                      SoundEvent sound) {
        this(block, halfWidth, height, distance, durationTicks, sound, null);
    }

    private WallEffect(Supplier<? extends Block> block, int halfWidth, int height, int distance, int durationTicks,
                       SoundEvent sound, @Nullable PillarKind look) {
        this.look = look;
        this.block = block;
        this.halfWidth = halfWidth;
        this.height = height;
        this.distance = distance;
        this.durationTicks = durationTicks;
        this.sound = sound;
    }

    /** The same wall, its blocks hidden behind a model (Earth Wall: a Blockbench wall of stone slabs). */
    public WallEffect lookingLike(PillarKind model) {
        return new WallEffect(block, halfWidth, height, distance, durationTicks, sound, model);
    }

    @Override
    public boolean cast(SpellContext context) {
        ServerLevel level = context.level();
        Direction facing = context.caster().getDirection();
        Direction side = facing.getClockWise();
        BlockPos center = context.caster().blockPosition().relative(facing, distance);
        int duration = (int) Math.round(durationTicks * context.power());
        int placed = 0;
        for (int across = -halfWidth; across <= halfWidth; across++) {
            for (int up = 0; up < height; up++) {
                BlockPos pos = center.relative(side, across).above(up);
                if (level.getBlockState(pos).canBeReplaced()) {
                    level.setBlockAndUpdate(pos, look != null && block.get() instanceof ConjuredBlock conjured
                            ? conjured.hiddenState() : block.get().defaultBlockState());
                    level.scheduleTick(pos, block.get(), duration);
                    placed++;
                }
            }
        }
        if (placed > 0) {
            level.playSound(null, center, sound, SoundSource.PLAYERS, 1.0F, 1.0F);
            if (look != null) {
                level.addFreshEntity(SpellPillar.create(level, look, Vec3.atBottomCenterOf(center), look.modelHeight(), duration,
                        facing.toYRot(), 0.0F));
                CameraShake.around(level, Vec3.atCenterOf(center), 0.5F, 8, 14.0);
            }
        }
        return placed > 0;
    }
}
