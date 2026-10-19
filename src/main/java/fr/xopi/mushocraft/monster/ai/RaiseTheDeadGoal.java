package fr.xopi.mushocraft.monster.ai;

import fr.xopi.mushocraft.compat.epicfight.MonsterActions;
import fr.xopi.mushocraft.monster.MonsterAction;
import fr.xopi.mushocraft.monster.Wraith;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

/**
 * A wraith animates skeletons to fight for it (wiki, page <i>Monsters</i>): in a fight it raises the dead around its
 * prey, armed as they were in life, and raises new ones as those fall; they are immortal as long as it remains, and
 * crumble with it. Skeletons it raised carry its mark.
 */
public class RaiseTheDeadGoal extends Goal {
    public static final String BOUND_TO = "mushocraft:wraith";
    private static final int COOLDOWN_TICKS = 200;
    private static final int RAISE_TICKS = 30;
    private static final List<Item> WEAPONS = List.of(Items.IRON_SWORD, Items.STONE_SWORD, Items.IRON_AXE, Items.STONE_AXE);

    private final Wraith wraith;
    private long readyAt;
    private int raising;

    public RaiseTheDeadGoal(Wraith wraith) {
        this.wraith = wraith;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    /** The skeletons this wraith raised, within reach. */
    public static List<Skeleton> bound(Wraith wraith) {
        String id = wraith.getUUID().toString();
        return wraith.level().getEntitiesOfClass(Skeleton.class, new AABB(wraith.blockPosition()).inflate(48.0),
                skeleton -> skeleton.isAlive() && id.equals(skeleton.getPersistentData().getString(BOUND_TO)));
    }

    public static int army(Wraith wraith) {
        return wraith.isDeadly() ? 3 : 2;
    }

    @Override
    public boolean canUse() {
        LivingEntity prey = wraith.getTarget();
        return prey != null && prey.isAlive() && !wraith.isReforming() && wraith.level().getGameTime() >= readyAt
                && bound(wraith).size() < army(wraith);
    }

    @Override
    public boolean canContinueToUse() {
        return raising > 0 && !wraith.isReforming();
    }

    @Override
    public void start() {
        raising = RAISE_TICKS;
        wraith.getNavigation().stop();
        MonsterActions.play(wraith, MonsterAction.CALL);
        wraith.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 1.2F, 0.6F);
    }

    @Override
    public void tick() {
        if (wraith.getTarget() != null) {
            wraith.getLookControl().setLookAt(wraith.getTarget());
        }
        if (--raising == 0) {
            raise();
        }
    }

    @Override
    public void stop() {
        raising = 0;
        readyAt = wraith.level().getGameTime() + COOLDOWN_TICKS;
    }

    /** The dead rise from the ground between the wraith and its prey, up to the size of its army. */
    public void raise() {
        if (!(wraith.level() instanceof ServerLevel level)) {
            return;
        }
        LivingEntity prey = wraith.getTarget();
        int missing = army(wraith) - bound(wraith).size();
        for (int i = 0; i < missing; i++) {
            double angle = wraith.getRandom().nextDouble() * Math.PI * 2;
            double x = wraith.getX() + Math.cos(angle) * 2.5;
            double z = wraith.getZ() + Math.sin(angle) * 2.5;
            BlockPos ground = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BlockPos.containing(x, wraith.getY(), z));
            if (Math.abs(ground.getY() - wraith.getY()) > 4) {
                ground = wraith.blockPosition();
            }
            Skeleton skeleton = EntityType.SKELETON.create(level);
            if (skeleton == null) {
                return;
            }
            skeleton.moveTo(x, ground.getY(), z, wraith.getRandom().nextFloat() * 360.0F, 0.0F);
            skeleton.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(WEAPONS.get(wraith.getRandom().nextInt(WEAPONS.size()))));
            skeleton.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                skeleton.setDropChance(slot, 0.0F);
            }
            skeleton.getPersistentData().putString(BOUND_TO, wraith.getUUID().toString());
            level.addFreshEntity(skeleton);
            if (prey != null) {
                skeleton.setTarget(prey);
            }
            level.sendParticles(ParticleTypes.SOUL, x, ground.getY() + 0.3, z, 16, 0.3, 0.2, 0.3, 0.02);
            level.playSound(null, skeleton.blockPosition(), SoundEvents.SKELETON_AMBIENT, SoundSource.HOSTILE, 1.0F, 0.6F);
        }
    }
}
