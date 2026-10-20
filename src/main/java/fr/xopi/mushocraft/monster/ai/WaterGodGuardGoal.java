package fr.xopi.mushocraft.monster.ai;

import fr.xopi.mushocraft.compat.epicfight.MonsterActions;
import fr.xopi.mushocraft.monster.MonsterAction;
import fr.xopi.mushocraft.monster.MushoMonster;
import fr.xopi.mushocraft.sword.technique.CounterKind;
import fr.xopi.mushocraft.sword.technique.CounterStances;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;

/**
 * A fighter of the Water God Style waits for the blow: when its foe is within reach, or a missile or spell flies at it,
 * it takes Flow's receiving stance, and the attack is turned aside and answered (see {@link CounterStances}). It does
 * not stand still waiting for ever: the stance is brief and comes back after a while.
 */
public class WaterGodGuardGoal extends Goal {
    private final MushoMonster fighter;
    private final double reach;
    private final int everyTicks;
    private final int windowTicks;
    private long readyAt;

    public WaterGodGuardGoal(MushoMonster fighter, double reach, int everyTicks, int windowTicks) {
        this.fighter = fighter;
        this.reach = reach;
        this.everyTicks = everyTicks;
        this.windowTicks = windowTicks;
        setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        LivingEntity foe = fighter.getTarget();
        if (foe == null || fighter.level().getGameTime() < readyAt || CounterStances.active(fighter).isPresent()) {
            return false;
        }
        return fighter.distanceTo(foe) <= reach || incoming();
    }

    /** A missile or a spell on its way, close by. */
    private boolean incoming() {
        AABB around = fighter.getBoundingBox().inflate(6.0);
        return !fighter.level().getEntitiesOfClass(Projectile.class, around,
                p -> p.getOwner() != fighter && p.getDeltaMovement().lengthSqr() > 0.04
                        && p.getDeltaMovement().normalize().dot(fighter.position().subtract(p.position()).normalize()) > 0.7).isEmpty();
    }

    @Override
    public void start() {
        CounterStances.enter(fighter, windowTicks, 1.6F, true, CounterKind.FLOW);
        MonsterActions.play(fighter, MonsterAction.COUNTER);
        readyAt = fighter.level().getGameTime() + everyTicks;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }
}
