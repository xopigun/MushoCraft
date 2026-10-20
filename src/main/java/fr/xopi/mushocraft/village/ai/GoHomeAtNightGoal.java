package fr.xopi.mushocraft.village.ai;

import fr.xopi.mushocraft.village.TownsfolkEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Villagers go back indoors when night falls, and stay close to the hearth until morning.
 */
public class GoHomeAtNightGoal extends Goal {
    private static final double CLOSE_ENOUGH = 2.5;

    private final TownsfolkEntity person;
    private final double speed;

    public GoHomeAtNightGoal(TownsfolkEntity person, double speed) {
        this.person = person;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return person.level().isNight() && person.home()
                .map(home -> person.distanceToSqr(home.getCenter()) > CLOSE_ENOUGH * CLOSE_ENOUGH)
                .orElse(false);
    }

    @Override
    public void start() {
        person.home().ifPresent(this::walkTo);
    }

    @Override
    public void tick() {
        if (person.getNavigation().isDone()) {
            person.home().ifPresent(this::walkTo);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    private void walkTo(BlockPos home) {
        person.getNavigation().moveTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, speed);
    }
}
