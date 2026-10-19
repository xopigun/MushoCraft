package fr.xopi.mushocraft.magic.summon;

import fr.xopi.mushocraft.core.MushoEntities;
import fr.xopi.mushocraft.magic.mana.ManaService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;

/**
 * A spirit made of its summoner's mana (wiki, <i>Magic Spells</i> § Summoning Magic: Spirit Summon). It does what it
 * was made for, like a program: here, it guards its master and fights what its master fights. Its strength is
 * measured on its master's reservoir; it feeds on that mana every second and ceases to exist when none is left. Not
 * saved: a spirit does not outlast the world being reloaded.
 */
public class SummonedSpirit extends PathfinderMob {
    public SummonedSpirit(EntityType<? extends SummonedSpirit> type, Level level) {
        super(type, level);
        moveControl = new FlyingMoveControl(this, 20, true);
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.FLYING_SPEED, 0.6)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    /** A spirit whose strength follows its summoner's reservoir. */
    public static SummonedSpirit create(ServerPlayer summoner) {
        SummonedSpirit spirit = new SummonedSpirit(MushoEntities.SUMMONED_SPIRIT.get(), summoner.level());
        double capacity = ManaService.snapshot(summoner).capacity();
        spirit.getAttribute(Attributes.MAX_HEALTH).setBaseValue(SummonRules.spiritHealth(capacity));
        spirit.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(SummonRules.spiritDamage(capacity));
        spirit.setHealth(spirit.getMaxHealth());
        spirit.moveTo(summoner.getX(), summoner.getY() + 1.5, summoner.getZ(), summoner.getYRot(), 0.0F);
        return spirit;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, true));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        return navigation;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel level) || tickCount % 20 != 0) {
            return;
        }
        ServerPlayer master = SummonBonds.summoner(this).orElse(null);
        // fed by its master's mana; without it, the spirit ceases to exist
        if (master == null || tickCount > SummonRules.SPIRIT_MAX_TICKS
                || !ManaService.trySpend(master, SummonRules.SPIRIT_UPKEEP_PER_SECOND)) {
            level.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 0.4, getZ(), 12, 0.2, 0.2, 0.2, 0.05);
            level.playSound(null, blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.0F, 0.6F);
            discard();
        }
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
}
