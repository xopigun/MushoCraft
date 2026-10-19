package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.magic.entity.WaterSheet;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Water Shield: a wall of water curving round the caster like a screen, ahead of them. Its blocks (invisible) stop
 * what comes through; its animated water is a {@link WaterSheet} that swells from a bubble into the curved wall.
 */
public final class CurvedWallEffect implements SpellEffect {
    private final Supplier<? extends Block> block;
    private final double radius;
    private final double arcLength;
    private final int height;
    private final int durationTicks;
    private final SoundEvent sound;

    /**
     * @param radius    distance from the caster to the wall
     * @param arcLength length of the wall along its curve, in blocks
     */
    public CurvedWallEffect(Supplier<? extends Block> block, double radius, double arcLength, int height, int durationTicks,
                            SoundEvent sound) {
        this.block = block;
        this.radius = radius;
        this.arcLength = arcLength;
        this.height = height;
        this.durationTicks = durationTicks;
        this.sound = sound;
    }

    @Override
    public boolean cast(SpellContext context) {
        ServerLevel level = context.level();
        double scale = context.scale();
        double wallRadius = radius * scale;
        double span = arcLength * scale / wallRadius;
        int wallHeight = (int) Math.ceil(height * scale);
        float yaw = context.caster().getYRot();
        Vec3 forward = Vec3.directionFromRotation(0.0F, yaw);
        Vec3 right = new Vec3(-forward.z, 0.0, forward.x);
        Vec3 centre = context.caster().position();
        int duration = (int) Math.round(durationTicks * context.power());

        Set<BlockPos> columns = new LinkedHashSet<>();
        int steps = (int) Math.ceil(arcLength * scale * 2.0);
        for (int i = 0; i <= steps; i++) {
            double angle = (i / (double) steps - 0.5) * span;
            Vec3 point = centre.add(forward.scale(Math.cos(angle) * wallRadius)).add(right.scale(Math.sin(angle) * wallRadius));
            columns.add(BlockPos.containing(point.x, centre.y, point.z));
        }
        int placed = 0;
        for (BlockPos column : columns) {
            for (int up = 0; up < wallHeight; up++) {
                BlockPos pos = column.above(up);
                if (level.getBlockState(pos).canBeReplaced()) {
                    level.setBlockAndUpdate(pos, block.get().defaultBlockState());
                    level.scheduleTick(pos, block.get(), duration);
                    placed++;
                }
            }
        }
        if (placed == 0) {
            return false;
        }
        level.addFreshEntity(WaterSheet.create(level, centre, yaw, (float) (arcLength * scale), (float) (height * scale), duration,
                (float) wallRadius));
        level.playSound(null, BlockPos.containing(centre), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }
}
