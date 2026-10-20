package fr.xopi.mushocraft.monster.ai;

import fr.xopi.mushocraft.compat.epicfight.MonsterActions;
import fr.xopi.mushocraft.core.ServerTasks;
import fr.xopi.mushocraft.magic.entity.SpellProjectile;
import fr.xopi.mushocraft.magic.spell.SpellElement;
import fr.xopi.mushocraft.magic.spell.effect.ProjectileProfile;
import fr.xopi.mushocraft.monster.MadSkull;
import fr.xopi.mushocraft.monster.MonsterAction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * A Mad Skull scoops mud from its own body and slings it at a foe out of reach: a heavy ball that slows whoever it
 * hits. The skull comes out of the mud as it throws.
 */
public class MudSlingGoal extends Goal {
    /** When the ball leaves the hand, in the throw animation. */
    private static final int RELEASE_TICKS = 12;
    private static final ProjectileProfile MUD = ProjectileProfile.of(SpellElement.EARTH, 6.0F)
            .speed(1.1F).size(0.9F).slow(60).build();

    private final MadSkull giant;
    private final double minRange;
    private final double maxRange;
    private final int cooldown;
    private long readyAt;

    public MudSlingGoal(MadSkull giant, double minRange, double maxRange, int cooldown) {
        this.giant = giant;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.cooldown = cooldown;
        setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity foe = giant.getTarget();
        if (foe == null || giant.level().getGameTime() < readyAt || !giant.hasLineOfSight(foe)) {
            return false;
        }
        double distance = giant.distanceTo(foe);
        return distance >= minRange && distance <= maxRange;
    }

    @Override
    public void start() {
        readyAt = giant.level().getGameTime() + cooldown;
        giant.expose();
        giant.getNavigation().stop();
        MonsterActions.play(giant, MonsterAction.THROW);
        ServerTasks.later(giant, RELEASE_TICKS, self -> sling());
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    private void sling() {
        LivingEntity foe = giant.getTarget();
        if (foe == null) {
            return;
        }
        SpellProjectile ball = SpellProjectile.create(giant.level(), giant, MUD);
        Vec3 from = giant.position().add(0.0, 2.6, 0.0);
        Vec3 aim = foe.getEyePosition().subtract(from);
        ball.setPos(from);
        ball.shoot(aim.x, aim.y + aim.horizontalDistance() * 0.08, aim.z, MUD.speed(), 2.0F);
        giant.level().addFreshEntity(ball);
        giant.playSound(SoundEvents.MUD_BREAK, 1.2F, 0.6F);
    }
}
