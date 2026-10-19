package fr.xopi.mushocraft.sword.technique;

import fr.xopi.mushocraft.compat.epicfight.EpicFightAnimations;
import fr.xopi.mushocraft.core.ServerTasks;
import fr.xopi.mushocraft.magic.fx.SpellFx;
import fr.xopi.mushocraft.sword.entity.ThrownWeapon;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.gameasset.Animations;

/**
 * North God Style, Hazy Cross (wiki, Auber Corvette): an overhead blow with both swords, made to be blocked; the moment
 * both are caught, one sword is thrown and the user strikes across the undefended torso, the two cuts drawing a cross.
 * Here: the overhead blow with both swords, then the off-hand sword flung at the foe and a horizontal slash, stronger.
 */
public final class HazyCrossAction implements TechniqueAction {
    private static final int STRIKE_WINDOW_TICKS = 40;
    /** Ticks between the overhead blow and the throw with the crossing slash. */
    private static final int CROSS_DELAY = 9;

    private final float overheadMultiplier;
    private final float crossMultiplier;

    public HazyCrossAction(float overheadMultiplier, float crossMultiplier) {
        this.overheadMultiplier = overheadMultiplier;
        this.crossMultiplier = crossMultiplier;
    }

    @Override
    public double maxRange() {
        return 3.2;
    }

    @Override
    public boolean perform(TechniqueContext context) {
        LivingEntity user = context.user();
        if (!EpicFightAnimations.canAct(user) || user.getItemInHand(InteractionHand.OFF_HAND).isEmpty()) {
            return false;
        }
        var overhead = Animations.SWORD_DUAL_AUTO3;
        TechniqueStrikes.arm(user, overhead.registryName(), (float) (overheadMultiplier * context.power()), STRIKE_WINDOW_TICKS, null);
        EpicFightAnimations.play(user, overhead);
        Vec3 heading = context.heading();
        float cross = (float) (crossMultiplier * context.power());
        ServerTasks.later(user, CROSS_DELAY, fighter -> cross(fighter, heading, cross));
        return true;
    }

    /** The off-hand sword flies at the foe while the main hand cuts across. */
    private static void cross(LivingEntity user, Vec3 heading, float multiplier) {
        if (!(user.level() instanceof ServerLevel level)) {
            return;
        }
        ItemStack offhand = user.getItemInHand(InteractionHand.OFF_HAND);
        if (!offhand.isEmpty()) {
            float damage = (float) (user.getAttributeValue(Attributes.ATTACK_DAMAGE) * multiplier * 0.6F);
            ThrownWeapon thrown = ThrownWeapon.create(level, user, offhand, damage);
            thrown.setPos(user.getEyePosition().add(heading.scale(0.8)).subtract(0.0, 0.4, 0.0));
            thrown.shoot(heading.x, -0.05, heading.z, 1.8F, 0.3F);
            level.addFreshEntity(thrown);
            user.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            level.playSound(null, user.blockPosition(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0F, 1.1F);
        }
        var slash = Animations.SWORD_AUTO3;
        TechniqueStrikes.arm(user, slash.registryName(), multiplier, STRIKE_WINDOW_TICKS, null);
        EpicFightAnimations.play(user, slash);
        SpellFx.slash(level, user, heading, 1.5F, 0xE8E4FF);
    }
}
