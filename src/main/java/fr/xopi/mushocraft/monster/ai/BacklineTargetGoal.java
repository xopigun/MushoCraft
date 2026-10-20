package fr.xopi.mushocraft.monster.ai;

import fr.xopi.mushocraft.magic.spell.ChantBroadcast;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/**
 * A monster that gets into the middle of a party goes for its back line: whoever is chanting first, then whoever holds
 * no blade, and only then the fighters in front. It looks again from time to time, and switches if a weaker prey shows.
 */
public class BacklineTargetGoal extends TargetGoal {
    private static final int RETHINK_TICKS = 20;

    private final double range;
    private final TargetingConditions conditions;
    private int rethink;

    public BacklineTargetGoal(Mob mob, double range) {
        super(mob, true, false);
        this.range = range;
        this.conditions = TargetingConditions.forCombat().range(range);
        setFlags(EnumSet.of(Flag.TARGET));
    }

    /** Lower is better: a chanting mage, then someone without a blade, then a fighter. */
    public static int exposure(LivingEntity someone) {
        if (ChantBroadcast.isChanting(someone)) {
            return 0;
        }
        var held = someone.getMainHandItem().getItem();
        boolean blade = held instanceof SwordItem || held instanceof AxeItem || held instanceof TridentItem;
        return blade ? 2 : 1;
    }

    private LivingEntity pick() {
        List<Player> around = mob.level().getNearbyPlayers(conditions, mob, mob.getBoundingBox().inflate(range, 6.0, range));
        return around.stream()
                .min(Comparator.<Player>comparingInt(BacklineTargetGoal::exposure).thenComparingDouble(mob::distanceToSqr))
                .orElse(null);
    }

    @Override
    public boolean canUse() {
        if (--rethink > 0) {
            return false;
        }
        rethink = RETHINK_TICKS;
        LivingEntity best = pick();
        if (best == null || best == mob.getTarget()) {
            return false;
        }
        LivingEntity current = mob.getTarget();
        return current == null || exposure(best) < exposure(current);
    }

    @Override
    public void start() {
        targetMob = pick();
        mob.setTarget(targetMob);
        super.start();
    }
}
