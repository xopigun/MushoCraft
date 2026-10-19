package fr.xopi.mushocraft.monster.ai;

import fr.xopi.mushocraft.monster.EngagementTokens;
import fr.xopi.mushocraft.monster.MonsterPack;
import fr.xopi.mushocraft.monster.MushoMonster;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Courage in numbers: a monster that faces a person with too few of its kind around keeps out of reach, and comes
 * back when its gang gathers (goblins: harmless in small numbers, wiki page <i>Monsters</i>).
 */
public class FleeWhenOutnumberedGoal extends Goal {
    private static final double GANG_RANGE = 12.0;

    private final MushoMonster coward;
    private final int braveFrom;
    private final double speed;

    /**
     * @param braveFrom how many packmates around it the monster needs to dare fight
     */
    public FleeWhenOutnumberedGoal(MushoMonster coward, int braveFrom, double speed) {
        this.coward = coward;
        this.braveFrom = braveFrom;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity threat = coward.getTarget();
        return threat instanceof Player && threat.isAlive() && !brave();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse() && coward.distanceToSqr(coward.getTarget()) < 16.0 * 16.0;
    }

    @Override
    public void start() {
        EngagementTokens.release(coward);
        flee();
    }

    @Override
    public void tick() {
        LivingEntity threat = coward.getTarget();
        if (threat != null) {
            coward.getLookControl().setLookAt(threat, 30.0F, 30.0F);
        }
        if (coward.getNavigation().isDone() || coward.tickCount % 20 == 0) {
            flee();
        }
    }

    private boolean brave() {
        return MonsterPack.mates(coward).stream().filter(mate -> mate.distanceTo(coward) < GANG_RANGE).count() >= braveFrom;
    }

    private void flee() {
        LivingEntity threat = coward.getTarget();
        if (threat == null) {
            return;
        }
        Vec3 away = DefaultRandomPos.getPosAway(coward, 12, 4, threat.position());
        if (away == null) {
            Vec3 back = coward.position().subtract(threat.position()).multiply(1.0, 0.0, 1.0);
            away = coward.position().add(back.lengthSqr() < 1.0E-4 ? new Vec3(8.0, 0.0, 0.0) : back.normalize().scale(8.0));
        }
        coward.getNavigation().moveTo(away.x, away.y, away.z, speed);
    }
}
