package fr.xopi.mushocraft.monster.ai;

import fr.xopi.mushocraft.compat.epicfight.MonsterActions;
import fr.xopi.mushocraft.monster.MonsterAction;
import fr.xopi.mushocraft.monster.MonsterPack;
import fr.xopi.mushocraft.monster.MushoMonster;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * When the pack leader spots prey, it stops to give the call (a howl, a roar) and the whole pack within earshot turns
 * on that prey. Only once per prey; a new prey is called again.
 */
public class CallThePackGoal extends Goal {
    private final MushoMonster caller;
    private final SoundEvent call;
    private final int duration;
    private LivingEntity called;
    private int remaining;

    public CallThePackGoal(MushoMonster caller, SoundEvent call, int duration) {
        this.caller = caller;
        this.call = call;
        this.duration = duration;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        LivingEntity prey = caller.getTarget();
        return prey != null && prey != called && caller.isPackLeader() && !MonsterPack.mates(caller).isEmpty()
                && caller.hasLineOfSight(prey);
    }

    @Override
    public void start() {
        called = caller.getTarget();
        remaining = duration;
        caller.getNavigation().stop();
        MonsterActions.play(caller, MonsterAction.CALL);
        caller.playSound(call, 2.0F, 0.9F + caller.getRandom().nextFloat() * 0.2F);
        MonsterPack.callToHunt(caller, called);
    }

    @Override
    public boolean canContinueToUse() {
        return remaining > 0;
    }

    @Override
    public void tick() {
        remaining--;
        if (called != null) {
            caller.getLookControl().setLookAt(called, 10.0F, 10.0F);
        }
    }
}
