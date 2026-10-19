package fr.xopi.mushocraft.compat.epicfight;

import fr.xopi.mushocraft.monster.MushoMonster;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.entity.ai.goal.AnimatedAttackGoal;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

/**
 * Epic Fight patch of a MushoCraft monster: its species' animations and attacks. Moving in a fight is the monster's own
 * AI (circling, rushing in, backing off); this patch only adds Epic Fight's attack goal, which strikes when the prey is
 * in reach and the monster holds a place to strike (see {@link MushoMonster#mayStrike()}).
 */
public abstract class MonsterPatch<T extends MushoMonster> extends MobPatch<T> {
    protected MonsterPatch(T monster) {
        super(monster, Factions.NEUTRAL);
    }

    public abstract MonsterAnimationSet animations();

    protected abstract CombatBehaviors.Builder<MonsterPatch<?>> combatBehaviors();

    public boolean mayStrike() {
        return original.mayStrike();
    }

    @Override
    protected void initAI() {
        super.initAI();
        original.goalSelector.addGoal(0, new AnimatedAttackGoal<MonsterPatch<?>>(this, combatBehaviors().build(this)));
    }

    @Override
    public void updateMotion(boolean considerInaction) {
        super.commonAggressiveMobUpdateMotion(considerInaction);
    }

    @Override
    public LivingEntity getTarget() {
        return original.getTarget();
    }
}
