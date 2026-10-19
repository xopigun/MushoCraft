package fr.xopi.mushocraft.magic.summon;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.EnumSet;
import java.util.Optional;

/**
 * What a bound creature does for its summoner, as AI goals added to any mob. Each goal checks the covenant itself, so
 * once it runs out the creature simply goes back to its own ways.
 */
public final class SummonerGoals {
    private SummonerGoals() {
    }

    /** Adds the goals once (they are not saved: they are added again when the creature is loaded). */
    public static void addTo(Mob creature) {
        boolean present = creature.goalSelector.getAvailableGoals().stream().anyMatch(wrapped -> wrapped.getGoal() instanceof Follow);
        if (present) {
            return;
        }
        creature.goalSelector.addGoal(2, new Follow(creature));
        creature.targetSelector.addGoal(0, new Defend(creature));
        creature.targetSelector.addGoal(1, new Assist(creature));
    }

    /** Keeps near its master; comes back to it from afar. */
    static final class Follow extends Goal {
        private static final double START = 8.0;
        private static final double STOP = 3.0;
        private static final double TELEPORT = 24.0;

        private final Mob creature;
        private ServerPlayer summoner;

        Follow(Mob creature) {
            this.creature = creature;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            Optional<ServerPlayer> master = SummonBonds.summoner(creature);
            if (master.isEmpty() || creature.getTarget() != null || creature.distanceToSqr(master.get()) < START * START) {
                return false;
            }
            summoner = master.get();
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return SummonBonds.isBound(creature) && creature.getTarget() == null && summoner != null && summoner.isAlive()
                    && creature.distanceToSqr(summoner) > STOP * STOP;
        }

        @Override
        public void tick() {
            if (creature.distanceToSqr(summoner) > TELEPORT * TELEPORT) {
                creature.teleportTo(summoner.getX(), summoner.getY(), summoner.getZ());
                creature.getNavigation().stop();
                return;
            }
            creature.getLookControl().setLookAt(summoner, 10.0F, creature.getMaxHeadXRot());
            creature.getNavigation().moveTo(summoner, 1.2);
        }

        @Override
        public void stop() {
            summoner = null;
            creature.getNavigation().stop();
        }
    }

    /** Turns on whoever hurt its master. */
    static final class Defend extends TargetGoal {
        private LivingEntity attacker;

        Defend(Mob creature) {
            super(creature, false);
            setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            Optional<ServerPlayer> master = SummonBonds.summoner(mob);
            if (master.isEmpty()) {
                return false;
            }
            attacker = master.get().getLastHurtByMob();
            return attacker != null && !SummonBonds.allied(mob, attacker)
                    && canAttack(attacker, TargetingConditions.DEFAULT);
        }

        @Override
        public void start() {
            mob.setTarget(attacker);
            super.start();
        }
    }

    /** Strikes what its master strikes. */
    static final class Assist extends TargetGoal {
        private LivingEntity prey;

        Assist(Mob creature) {
            super(creature, false);
            setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            Optional<ServerPlayer> master = SummonBonds.summoner(mob);
            if (master.isEmpty()) {
                return false;
            }
            prey = master.get().getLastHurtMob();
            return prey != null && !SummonBonds.allied(mob, prey) && canAttack(prey, TargetingConditions.DEFAULT);
        }

        @Override
        public void start() {
            mob.setTarget(prey);
            super.start();
        }
    }
}
