package fr.xopi.mushocraft.magic.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Matter shaped by a spell. The spell schedules a tick when it places the block; on that tick the block
 * dissolves. Scheduled ticks are saved with the chunk, so walls never stay forever.
 */
public class ConjuredBlock extends Block {
    private final ParticleOptions dissolveParticle;

    public ConjuredBlock(Properties properties, ParticleOptions dissolveParticle) {
        super(properties);
        this.dissolveParticle = dissolveParticle;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
        level.sendParticles(dissolveParticle, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.02);
    }
}
