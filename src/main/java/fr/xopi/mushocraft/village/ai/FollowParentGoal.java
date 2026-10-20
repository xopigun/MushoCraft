package fr.xopi.mushocraft.village.ai;

import fr.xopi.mushocraft.village.TownsfolkEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * A young child keeps close to a parent, as long as one is around.
 */
public class FollowParentGoal extends Goal {
    private static final double TOO_FAR = 6.0;
    private static final double CLOSE = 3.0;
    private static final double SEARCH = 16.0;

    private final TownsfolkEntity child;
    private final double speed;
    @Nullable
    private LivingEntity parent;

    public FollowParentGoal(TownsfolkEntity child, double speed) {
        this.child = child;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (child.isAdult() || child.parents().isEmpty()) {
            return false;
        }
        parent = child.level().getEntitiesOfClass(LivingEntity.class, child.getBoundingBox().inflate(SEARCH),
                        other -> child.parents().contains(other.getUUID())).stream()
                .min((a, b) -> Double.compare(child.distanceToSqr(a), child.distanceToSqr(b))).orElse(null);
        return parent != null && child.distanceToSqr(parent) > TOO_FAR * TOO_FAR;
    }

    @Override
    public boolean canContinueToUse() {
        return parent != null && parent.isAlive() && child.distanceToSqr(parent) > CLOSE * CLOSE;
    }

    @Override
    public void tick() {
        if (parent != null && child.getNavigation().isDone()) {
            child.getNavigation().moveTo(parent, speed);
        }
    }

    @Override
    public void stop() {
        parent = null;
    }
}
