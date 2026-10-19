package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

/**
 * A shelter raised around the caster (Earth Fortress): a ring of walls and a roof, with the caster inside.
 * In the novel it crumbles unless fed mana; here it lasts a set time, longer for a stronger caster.
 */
public final class FortressEffect implements SpellEffect {
    private final Supplier<? extends Block> block;
    private final int radius;
    private final int height;
    private final int durationTicks;
    private final SoundEvent sound;

    public FortressEffect(Supplier<? extends Block> block, int radius, int height, int durationTicks, SoundEvent sound) {
        this.block = block;
        this.radius = radius;
        this.height = height;
        this.durationTicks = durationTicks;
        this.sound = sound;
    }

    @Override
    public boolean cast(SpellContext context) {
        ServerLevel level = context.level();
        BlockPos center = context.caster().blockPosition();
        int duration = (int) Math.round(durationTicks * context.power());
        int placed = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                boolean wall = Math.max(Math.abs(dx), Math.abs(dz)) == radius;
                for (int up = 0; up <= height; up++) {
                    boolean roof = up == height;
                    if ((wall && !roof) || (roof && Math.abs(dx) + Math.abs(dz) < radius * 2)) {
                        placed += raise(level, center.offset(dx, up, dz), duration);
                    }
                }
            }
        }
        if (placed > 0) {
            level.playSound(null, center, sound, SoundSource.PLAYERS, 1.0F, 0.8F);
        }
        return placed > 0;
    }

    private int raise(ServerLevel level, BlockPos pos, int duration) {
        if (!level.getBlockState(pos).canBeReplaced()) {
            return 0;
        }
        level.setBlockAndUpdate(pos, block.get().defaultBlockState());
        level.scheduleTick(pos, block.get(), duration);
        return 1;
    }
}
