package fr.xopi.mushocraft.sword.ai;

import fr.xopi.mushocraft.compat.epicfight.EpicFightAnimations;
import fr.xopi.mushocraft.core.Rank;
import fr.xopi.mushocraft.sword.StyleRules;
import fr.xopi.mushocraft.sword.technique.Technique;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A creature uses the sword techniques it knows, the very ones players learn. Between techniques, Epic Fight's own
 * combat behaviour drives its ordinary attacks; this goal only steps in when a technique fits the distance to the
 * target (each action says its range) and is ready again. Cooldowns are those of a player of the same rank.
 */
public class TechniqueGoal extends Goal {
    private static final int PAUSE_BETWEEN_TECHNIQUES = 40;

    private final PathfinderMob fighter;
    private final Supplier<List<Technique>> repertoire;
    private final Supplier<Rank> rank;
    private final Map<Technique, Long> readyAt = new HashMap<>();
    private long nextTechniqueAt;
    private Technique chosen;
    /** A technique that could not be performed (wrong stance, nothing in hand) is left aside this long. */
    private static final int RETRY_AFTER_FAILURE = 60;

    public TechniqueGoal(PathfinderMob fighter, Supplier<List<Technique>> repertoire, Supplier<Rank> rank) {
        this.fighter = fighter;
        this.repertoire = repertoire;
        this.rank = rank;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = fighter.getTarget();
        if (target == null || !target.isAlive() || now() < nextTechniqueAt || !fighter.hasLineOfSight(target)
                || !EpicFightAnimations.canAct(fighter)) {
            return false;
        }
        Optional<Technique> fitting = fitting(fighter.distanceTo(target));
        fitting.ifPresent(technique -> chosen = technique);
        return fitting.isPresent();
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        LivingEntity target = fighter.getTarget();
        if (target == null || chosen == null) {
            return;
        }
        fighter.getLookControl().setLookAt(target);
        fighter.lookAt(target, 360.0F, 360.0F);
        if (chosen.performAs(fighter, rank.get(), target.getEyePosition())) {
            readyAt.put(chosen, now() + Math.round(chosen.cooldownTicks() * StyleRules.cooldownFactor(rank.get())));
        } else {
            readyAt.put(chosen, now() + RETRY_AFTER_FAILURE);
        }
        nextTechniqueAt = now() + PAUSE_BETWEEN_TECHNIQUES;
        chosen = null;
    }

    private Optional<Technique> fitting(double distance) {
        List<Technique> candidates = repertoire.get().stream()
                .filter(technique -> readyAt.getOrDefault(technique, 0L) <= now())
                .filter(technique -> technique.action().usableBy(fighter))
                .filter(technique -> distance >= technique.action().minRange() && distance <= technique.action().maxRange())
                .toList();
        return candidates.isEmpty() ? Optional.empty()
                : Optional.of(candidates.get(fighter.getRandom().nextInt(candidates.size())));
    }

    private long now() {
        return fighter.level().getGameTime();
    }
}
