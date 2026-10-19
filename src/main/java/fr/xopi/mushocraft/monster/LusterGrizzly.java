package fr.xopi.mushocraft.monster;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.monster.ai.CallThePackGoal;
import fr.xopi.mushocraft.monster.ai.FollowPackLeaderGoal;
import fr.xopi.mushocraft.monster.ai.PackHuntGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Luster Grizzly (wiki, page <i>Monsters</i>: rank B, Lake Cuculu in the north of the Central Continent): a large bear
 * with a white coat and a single black stripe down its middle. Grizzlies move in sizeable packs; they cannot see well in
 * the dark and move sluggishly at night, which is when adventurers fall upon them. In a fight a grizzly mauls, or rears
 * up and brings both forepaws crashing down; the leader stands and roars to rally the pack.
 */
public class LusterGrizzly extends MushoMonster {
    private static final ResourceLocation NIGHT_SLOWNESS = MushoCraft.id("monster/night_slowness");
    private static final ResourceLocation NIGHT_BLINDNESS = MushoCraft.id("monster/night_blindness");
    /** At night: a third slower, and sees only a third as far. */
    private static final double NIGHT_SPEED = -0.35;
    private static final double NIGHT_SIGHT = -0.65;

    public LusterGrizzly(EntityType<? extends LusterGrizzly> type, Level level) {
        super(type, level);
        xpReward = 25;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 90.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27)
                .add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    public MonsterRank rank() {
        return MonsterRank.B;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new CallThePackGoal(this, SoundEvents.POLAR_BEAR_WARNING, 44));
        goalSelector.addGoal(3, new PackHuntGoal(this, 1.35, 1.0, 5.5, 2.8));
        goalSelector.addGoal(5, new FollowPackLeaderGoal(this, 1.0, 5.0, 11.0));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
    }

    public boolean isSluggish() {
        return !level().isDay() && level().dimensionType().hasSkyLight();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide() && tickCount % 40 == 0) {
            boolean night = isSluggish();
            modify(getAttribute(Attributes.MOVEMENT_SPEED), NIGHT_SLOWNESS, NIGHT_SPEED, night);
            modify(getAttribute(Attributes.FOLLOW_RANGE), NIGHT_BLINDNESS, NIGHT_SIGHT, night);
        }
    }

    private static void modify(AttributeInstance attribute, ResourceLocation id, double share, boolean active) {
        if (attribute == null) {
            return;
        }
        if (active) {
            attribute.addOrUpdateTransientModifier(new AttributeModifier(id, share, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else {
            attribute.removeModifier(id);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.POLAR_BEAR_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.POLAR_BEAR_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.POLAR_BEAR_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.POLAR_BEAR_STEP, 0.3F, 0.9F);
    }
}
