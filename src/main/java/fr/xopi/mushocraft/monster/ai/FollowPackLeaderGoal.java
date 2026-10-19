package fr.xopi.mushocraft.monster.ai;

import fr.xopi.mushocraft.monster.MonsterPack;
import fr.xopi.mushocraft.monster.MushoMonster;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.Optional;

/**
 * Out of a fight, a pack keeps together: members trail their leader and close up when it wanders too far. A pack
 * found leaderless picks a new leader here.
 */
public class FollowPackLeaderGoal extends Goal {
    private final MushoMonster member;
    private final double speed;
    private final double closeEnough;
    private final double tooFar;
    private MushoMonster leader;
    private int recheck;

    public FollowPackLeaderGoal(MushoMonster member, double speed, double closeEnough, double tooFar) {
        this.member = member;
        this.speed = speed;
        this.closeEnough = closeEnough;
        this.tooFar = tooFar;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (member.getTarget() != null || member.isPackLeader() || --recheck > 0) {
            return false;
        }
        recheck = 20 + member.getRandom().nextInt(20);
        MonsterPack.ensureLeader(member);
        Optional<MushoMonster> found = MonsterPack.leader(member).filter(l -> l != member);
        if (found.isEmpty() || found.get().distanceToSqr(member) < tooFar * tooFar) {
            return false;
        }
        leader = found.get();
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return leader != null && leader.isAlive() && member.getTarget() == null
                && member.distanceToSqr(leader) > closeEnough * closeEnough;
    }

    @Override
    public void tick() {
        if (member.tickCount % 10 == 0) {
            member.getNavigation().moveTo(leader, speed);
        }
        member.getLookControl().setLookAt(leader, 10.0F, member.getMaxHeadXRot());
    }

    @Override
    public void stop() {
        leader = null;
        member.getNavigation().stop();
    }
}
