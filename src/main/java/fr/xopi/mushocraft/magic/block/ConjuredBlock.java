package fr.xopi.mushocraft.magic.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Matter shaped by a spell. The spell schedules a tick when it places the block; on that tick the block
 * dissolves. Scheduled ticks are saved with the chunk, so walls never stay forever.
 * <p>
 * A {@link #HIDDEN} block is only the solid core of something the spell draws itself (the Blockbench model of an
 * ice pillar or an earthen spike): it stops what comes through but is not seen.
 */
public class ConjuredBlock extends Block {
    public static final BooleanProperty HIDDEN = BooleanProperty.create("hidden");

    private final ParticleOptions dissolveParticle;

    public ConjuredBlock(Properties properties, ParticleOptions dissolveParticle) {
        super(properties);
        this.dissolveParticle = dissolveParticle;
        registerDefaultState(stateDefinition.any().setValue(HIDDEN, false));
    }

    /** This block as the unseen core of a shape the spell draws itself. */
    public BlockState hiddenState() {
        return defaultBlockState().setValue(HIDDEN, true);
    }

    /**
     * A conjured block that cannot be seen: its spell draws its own animated look (Water Shield's water),
     * the block only stops what comes through.
     */
    public static class Invisible extends ConjuredBlock {
        public Invisible(Properties properties, ParticleOptions dissolveParticle) {
            super(properties, dissolveParticle);
        }

        @Override
        protected RenderShape getRenderShape(BlockState state) {
            return RenderShape.INVISIBLE;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HIDDEN);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return state.getValue(HIDDEN) ? RenderShape.INVISIBLE : super.getRenderShape(state);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
        if (!state.getValue(HIDDEN)) {
            level.sendParticles(dissolveParticle, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.02);
        }
    }
}
