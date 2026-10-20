package fr.xopi.mushocraft.monster;

import fr.xopi.mushocraft.compat.epicfight.EpicFightAnimations;
import fr.xopi.mushocraft.magic.ai.SpellcasterGoal;
import fr.xopi.mushocraft.magic.spell.ChantBroadcast;
import fr.xopi.mushocraft.magic.spell.MushoSpells;
import fr.xopi.mushocraft.monster.ai.MudSlingGoal;
import fr.xopi.mushocraft.monster.ai.PackHuntGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Mad Skull (wiki, page <i>Monsters</i>: Teleportation Labyrinth, floors 3-4, rank A): a headless giant of mud, slow and very sturdy, with
 * a human skull buried in its chest, its weak point. It shrugs off blows to its mud and hides the skull inside when it
 * senses danger; it slings mud, or casts a spell like Stone Cannon. Here the skull comes out of the mud only while it
 * attacks or chants: that is the moment to strike it, from the front.
 */
public class MadSkull extends MushoMonster {
    /** A blow to the mud, or to a hidden skull, does next to nothing. */
    private static final float MUD_SHARE = 0.1F;
    private static final float SKULL_MULTIPLIER = 1.5F;
    /** How long the skull stays out after an attack. */
    private static final int EXPOSED_AFTER = 30;

    private int exposedFor;

    public MadSkull(EntityType<? extends MadSkull> type, Level level) {
        super(type, level);
        xpReward = 25;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.MOVEMENT_SPEED, 0.19)
                .add(Attributes.ATTACK_DAMAGE, 11.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    public MonsterRank rank() {
        return MonsterRank.A;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new SpellcasterGoal(this, () -> List.of(MushoSpells.STONE_CANNON.get()), spell -> 1.2));
        goalSelector.addGoal(3, new MudSlingGoal(this, 5.0, 16.0, 90));
        goalSelector.addGoal(4, new PackHuntGoal(this, 1.1, 1.0, 4.0, 3.2));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** The skull is out while the giant attacks or chants, and a moment after. */
    public boolean isSkullExposed() {
        return exposedFor > 0 || ChantBroadcast.isChanting(this) || !EpicFightAnimations.canAct(this);
    }

    /** It is about to attack: the skull comes out. */
    public void expose() {
        exposedFor = Math.max(exposedFor, EXPOSED_AFTER);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if (hitsTheSkull(source)) {
                amount *= SKULL_MULTIPLIER;
                if (level() instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.CRIT, getX(), getY() + 1.9, getZ(), 8, 0.2, 0.2, 0.2, 0.1);
                }
            } else {
                amount *= MUD_SHARE;
                if (level() instanceof ServerLevel server) {
                    server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MUD.defaultBlockState()),
                            getX(), getY() + 1.4, getZ(), 16, 0.5, 0.6, 0.5, 0.1);
                }
                playSound(SoundEvents.MUD_HIT, 1.0F, 0.8F);
            }
        }
        return super.hurt(source, amount);
    }

    /** A blow reaches the skull only while it is out, and only from in front of the chest. */
    private boolean hitsTheSkull(DamageSource source) {
        Entity from = source.getDirectEntity() != null ? source.getDirectEntity() : source.getEntity();
        if (from == null || !isSkullExposed()) {
            return false;
        }
        Vec3 facing = Vec3.directionFromRotation(0.0F, getYRot());
        Vec3 towards = from.position().subtract(position()).multiply(1.0, 0.0, 1.0);
        return towards.lengthSqr() < 1.0E-4 || facing.dot(towards.normalize()) > 0.35;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (exposedFor > 0) {
            exposedFor--;
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.MUD_STEP;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.MUD_BREAK;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.MUD_BRICKS_BREAK;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.MUD_STEP, 1.0F, 0.6F);
    }
}
