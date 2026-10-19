package fr.xopi.mushocraft.monster.ai;

import fr.xopi.mushocraft.monster.EngagementTokens;
import fr.xopi.mushocraft.monster.MushoMonster;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * How a pack hunter moves in a fight. If it holds a place against the prey (see {@link EngagementTokens}), it rushes in
 * and stops at striking range, facing the prey; its species' Epic Fight attacks then strike. Otherwise it circles the
 * prey at a distance, each member on its own side, ready to cut off a retreat, and asks again for a place every
 * half second. The attacks themselves are not here: Epic Fight plays them when the prey is in range.
 */
public class PackHuntGoal extends Goal {
    private static final int RETHINK_TICKS = 10;

    private final MushoMonster hunter;
    private final double chaseSpeed;
    private final double circleSpeed;
    private final double circleRadius;
    private final double strikeRange;
    private int rethink;
    private boolean engaged;
    private float orbitSign;

    public PackHuntGoal(MushoMonster hunter, double chaseSpeed, double circleSpeed, double circleRadius, double strikeRange) {
        this.hunter = hunter;
        this.chaseSpeed = chaseSpeed;
        this.circleSpeed = circleSpeed;
        this.circleRadius = circleRadius;
        this.strikeRange = strikeRange;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = hunter.getTarget();
        return target != null && target.isAlive() && hunter.canAttack(target);
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        rethink = 0;
        orbitSign = (hunter.getUUID().getLeastSignificantBits() & 1) == 0 ? 1.0F : -1.0F;
    }

    @Override
    public void stop() {
        EngagementTokens.release(hunter);
        hunter.getNavigation().stop();
        engaged = false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = hunter.getTarget();
        if (target == null) {
            return;
        }
        hunter.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (--rethink <= 0) {
            rethink = RETHINK_TICKS + hunter.getRandom().nextInt(5);
            engaged = EngagementTokens.tryEngage(hunter, target, hunter.engagementPlaces());
            if (engaged) {
                rush(target);
            } else {
                circle(target);
            }
        }
        if (engaged && hunter.distanceToSqr(target) <= strikeRange * strikeRange) {
            hunter.getNavigation().stop();
        }
    }

    private void rush(LivingEntity target) {
        if (hunter.distanceToSqr(target) > strikeRange * strikeRange) {
            hunter.getNavigation().moveTo(target, chaseSpeed);
        }
    }

    /** Takes the next point on a circle around the prey, on this member's own side. */
    private void circle(LivingEntity target) {
        Vec3 from = hunter.position().subtract(target.position());
        double angle = Math.atan2(from.z, from.x) + orbitSign * 0.6;
        double radius = circleRadius + (hunter.getRandom().nextDouble() - 0.5);
        if (hunter.getRandom().nextInt(12) == 0) {
            orbitSign = -orbitSign;
        }
        double x = target.getX() + Math.cos(angle) * radius;
        double z = target.getZ() + Math.sin(angle) * radius;
        double distance = Math.sqrt(from.x * from.x + from.z * from.z);
        double speed = distance < circleRadius - 1.5 ? chaseSpeed : circleSpeed;
        hunter.getNavigation().moveTo(x, target.getY(), z, Mth.clamp(speed, 0.6, 2.0));
    }
}
