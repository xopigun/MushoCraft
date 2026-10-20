package fr.xopi.mushocraft.monster;

import fr.xopi.mushocraft.compat.epicfight.EpicFightWeapons;
import fr.xopi.mushocraft.magic.entity.SpellProjectile;
import fr.xopi.mushocraft.magic.spell.SpellElement;
import fr.xopi.mushocraft.monster.ai.PackHuntGoal;
import fr.xopi.mushocraft.monster.ai.WaterGodGuardGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Armored Warrior (wiki, page <i>Monsters</i>: rank A, floors 4-5 of the Teleportation Labyrinth): an animated suit of
 * rusty armour with four arms, each holding a razor-sharp blade. Light weapons do little against it; it counters both
 * physical and magical attacks with the Water God Style; an undead, it is weak against earth and Divine Strike magic.
 */
public class ArmoredWarrior extends MushoMonster {
    /** Share of a light weapon's blow that gets through the plate. */
    private static final float LIGHT_WEAPON_SHARE = 0.25F;
    private static final float WEAKNESS_MULTIPLIER = 2.0F;

    public ArmoredWarrior(EntityType<? extends ArmoredWarrior> type, Level level) {
        super(type, level);
        xpReward = 30;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 80.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24)
                .add(Attributes.ATTACK_DAMAGE, 9.0)
                .add(Attributes.ARMOR, 14.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    public MonsterRank rank() {
        return MonsterRank.A;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new WaterGodGuardGoal(this, 3.5, 70, 18));
        goalSelector.addGoal(3, new PackHuntGoal(this, 1.15, 1.0, 4.0, 3.0));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** Light weapons glance off the plate; earth and Divine Strike magic hurt it twice as much. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof SpellProjectile projectile
                && (projectile.element() == SpellElement.EARTH || projectile.element() == SpellElement.LIGHT)) {
            amount *= WEAKNESS_MULTIPLIER;
        } else if (source.getEntity() instanceof LivingEntity attacker && source.getDirectEntity() == attacker
                && EpicFightWeapons.holdsLightWeapon(attacker)) {
            amount *= LIGHT_WEAPON_SHARE;
            if (level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.CRIT, getX(), getY() + 1.2, getZ(), 6, 0.3, 0.4, 0.3, 0.1);
            }
            playSound(SoundEvents.ANVIL_PLACE, 0.4F, 1.8F);
        }
        return super.hurt(source, amount);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ARMOR_EQUIP_IRON.value();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.ARMOR_EQUIP_CHAIN.value(), 0.5F, 0.7F);
    }
}
