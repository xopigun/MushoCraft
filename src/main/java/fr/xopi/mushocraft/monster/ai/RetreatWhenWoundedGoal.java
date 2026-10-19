package fr.xopi.mushocraft.monster.ai;

import fr.xopi.mushocraft.monster.EngagementTokens;
import fr.xopi.mushocraft.monster.MonsterPack;
import fr.xopi.mushocraft.monster.MushoMonster;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * A badly wounded beast backs off for a few seconds and lets a packmate take its place, then comes back to the fight.
 * One left alone does not withdraw: cornered, it fights to the end.
 */
public class RetreatWhenWoundedGoal extends Goal {
    private static final int RETREAT_TICKS = 70;
    private static final int COOLDOWN_TICKS = 200;

    private final MushoMonster beast;
    private final float healthShare;
    private final double speed;
    private int retreating;
    private long readyAt;

    public RetreatWhenWoundedGoal(MushoMonster beast, float healthShare, double speed) {
        this.beast = beast;
        this.healthShare = healthShare;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity threat = beast.getTarget();
        return threat != null && beast.level().getGameTime() >= readyAt
                && beast.getHealth() < beast.getMaxHealth() * healthShare
                && !MonsterPack.mates(beast).isEmpty();
    }

    @Override
    public void start() {
        retreating = RETREAT_TICKS;
        EngagementTokens.release(beast);
        flee();
    }

    @Override
    public boolean canContinueToUse() {
        return retreating > 0 && beast.getTarget() != null;
    }

    @Override
    public void tick() {
        retreating--;
        if (beast.getNavigation().isDone()) {
            flee();
        }
    }

    @Override
    public void stop() {
        readyAt = beast.level().getGameTime() + COOLDOWN_TICKS;
    }

    private void flee() {
        LivingEntity threat = beast.getTarget();
        if (threat == null) {
            return;
        }
        Vec3 away = DefaultRandomPos.getPosAway(beast, 10, 4, threat.position());
        if (away == null) {
            // no open ground found at random: straight back, away from the threat
            Vec3 back = beast.position().subtract(threat.position()).multiply(1.0, 0.0, 1.0);
            away = beast.position().add(back.lengthSqr() < 1.0E-4 ? new Vec3(8.0, 0.0, 0.0) : back.normalize().scale(8.0));
        }
        beast.getNavigation().moveTo(away.x, away.y, away.z, speed);
    }
}
