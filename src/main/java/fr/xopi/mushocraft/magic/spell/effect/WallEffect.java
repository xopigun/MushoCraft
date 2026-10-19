package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

/**
 * Raises a temporary wall facing the caster's look (Water Shield). Only replaces air and plants,
 * and each block removes itself after the duration (see {@code ConjuredBlock}).
 */
public final class WallEffect implements SpellEffect {
    private final Supplier<? extends Block> block;
    private final int halfWidth;
    private final int height;
    private final int distance;
    private final int durationTicks;
    private final SoundEvent sound;

    public WallEffect(Supplier<? extends Block> block, int halfWidth, int height, int distance, int durationTicks,
                      SoundEvent sound) {
        this.block = block;
        this.halfWidth = halfWidth;
        this.height = height;
        this.distance = distance;
        this.durationTicks = durationTicks;
        this.sound = sound;
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
                    level.setBlockAndUpdate(pos, block.get().defaultBlockState());
                    level.scheduleTick(pos, block.get(), duration);
                    placed++;
                }
            }
        }
        if (placed > 0) {
            level.playSound(null, center, sound, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return placed > 0;
    }
}
