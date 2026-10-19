package fr.xopi.mushocraft.magic.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Light shed by a lamplight spirit. Placed where the spirit floats, it removes itself a few ticks later unless the
 * spirit placed it again, so the light follows the spirit and never outlives it.
 */
public class SpiritLightBlock extends Block {
    public static final int LINGER_TICKS = 6;

    public SpiritLightBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
    }
}
