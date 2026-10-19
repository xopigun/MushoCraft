package fr.xopi.mushocraft.core;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.magic.block.ConjuredBlock;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Blocks of the mod.
 */
public final class MushoBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MushoCraft.MODID);

    /** Wall raised by Water Shield: stops creatures and projectiles, dissolves after a while. */
    public static final DeferredBlock<ConjuredBlock> WATER_WALL = BLOCKS.register("water_wall",
            () -> new ConjuredBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WATER)
                    .strength(0.5F)
                    .sound(SoundType.WET_SPONGE)
                    .noOcclusion()
                    .noLootTable()
                    .pushReaction(PushReaction.DESTROY)
                    .isViewBlocking((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false),
                    ParticleTypes.SPLASH));

    private MushoBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
