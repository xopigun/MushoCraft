package fr.xopi.mushocraft.monster.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * A creature that hates bright light leaves it for the nearest shadow, whatever else it was doing, even a hunt.
 */
public class ShunTheLightGoal extends Goal {
    private final PathfinderMob creature;
    private final int bright;
    private final int dark;
    private final double speed;
    @Nullable
    private Vec3 shade;

    /** `bright`: the light level it cannot bear; `dark`: the level it looks for. */
    public ShunTheLightGoal(PathfinderMob creature, int bright, int dark, double speed) {
        this.creature = creature;
        this.bright = bright;
        this.dark = dark;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    public static boolean isBright(PathfinderMob creature, BlockPos pos, int level) {
        return creature.level().getMaxLocalRawBrightness(pos) >= level;
    }

    @Override
    public boolean canUse() {
        if (!isBright(creature, creature.blockPosition(), bright)) {
            return false;
        }
        shade = findShade();
        return shade != null;
    }

    @Nullable
    private Vec3 findShade() {
        for (int i = 0; i < 12; i++) {
            Vec3 pos = DefaultRandomPos.getPos(creature, 12, 6);
            if (pos != null && !isBright(creature, BlockPos.containing(pos), dark + 1)) {
                return pos;
            }
        }
        return null;
    }

    @Override
    public void start() {
        creature.getNavigation().moveTo(shade.x, shade.y, shade.z, speed);
    }

    @Override
    public boolean canContinueToUse() {
        return !creature.getNavigation().isDone();
    }
}
