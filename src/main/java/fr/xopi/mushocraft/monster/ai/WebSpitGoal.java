package fr.xopi.mushocraft.monster.ai;

import fr.xopi.mushocraft.compat.epicfight.MonsterActions;
import fr.xopi.mushocraft.core.ServerTasks;
import fr.xopi.mushocraft.monster.MonsterAction;
import fr.xopi.mushocraft.monster.MushoMonster;
import fr.xopi.mushocraft.sword.entity.ThrownTrick;
import fr.xopi.mushocraft.sword.entity.TrickKind;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * A spider spits a web at a foe that holds a weapon, close or far, to tear it from the hand; at a foe that holds
 * nothing it only spits from afar, to hold it back as it comes (see {@link TrickKind#WEB}).
 */
public class WebSpitGoal extends Goal {
    /** When the web leaves the fangs, in the spit animation. */
    private static final int RELEASE_TICKS = 9;
    private static final float SPEED = 1.5F;
    /** Accurate enough to catch a blade (wiki). */
    private static final float INACCURACY = 0.4F;

    private final MushoMonster spider;
    private final double minRange;
    private final double maxRange;
    private final int cooldown;
    private long readyAt;

    public WebSpitGoal(MushoMonster spider, double minRange, double maxRange, int cooldown) {
        this.spider = spider;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.cooldown = cooldown;
        setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity foe = spider.getTarget();
        if (foe == null || spider.level().getGameTime() < readyAt || !spider.hasLineOfSight(foe)) {
            return false;
        }
        double distance = spider.distanceTo(foe);
        double from = foe.getMainHandItem().isEmpty() ? minRange * 2.0 : minRange;
        return distance >= from && distance <= maxRange;
    }

    @Override
    public void start() {
        readyAt = spider.level().getGameTime() + cooldown;
        spider.getNavigation().stop();
        MonsterActions.play(spider, MonsterAction.THROW);
        ServerTasks.later(spider, RELEASE_TICKS, self -> spit());
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    private void spit() {
        LivingEntity foe = spider.getTarget();
        if (foe == null || !spider.isAlive()) {
            return;
        }
        ThrownTrick web = ThrownTrick.create(spider.level(), spider, TrickKind.WEB);
        Vec3 from = spider.position().add(0.0, spider.getBbHeight() * 0.5, 0.0)
                .add(Vec3.directionFromRotation(0.0F, spider.getYRot()).scale(spider.getBbWidth() * 0.6));
        // at the weapon arm, a little ahead of a running foe
        Vec3 aim = foe.position().add(foe.getDeltaMovement().scale(6.0)).add(0.0, foe.getBbHeight() * 0.55, 0.0).subtract(from);
        web.setPos(from);
        web.shoot(aim.x, aim.y + aim.horizontalDistance() * 0.06, aim.z, SPEED, INACCURACY);
        spider.level().addFreshEntity(web);
        spider.playSound(SoundEvents.LLAMA_SPIT, 1.0F, 0.6F);
    }
}
