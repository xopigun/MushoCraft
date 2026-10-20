package fr.xopi.mushocraft.sword.technique;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * A struck or crushed arm that can no longer hold a weapon (Arm Chop, Light Reversal, Pecking): creatures drop what
 * they hold, everyone is weakened for a while. A weapon can also be torn from the hand ({@link #tearAway}).
 */
public final class Disarm {
    private Disarm() {
    }

    public static void apply(LivingEntity target, int weaknessTicks, int weaknessLevel) {
        if (target instanceof Mob mob && !mob.getMainHandItem().isEmpty()) {
            mob.spawnAtLocation(mob.getMainHandItem().copy());
            mob.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, weaknessTicks, weaknessLevel));
    }

    /**
     * Tears the weapon out of the hand (a web that catches the blade, Tarantula Deathlord): it falls at the target's
     * feet and cannot be picked up again for a moment. Players in creative keep theirs.
     */
    public static void tearAway(LivingEntity target, int pickupDelay) {
        ItemStack held = target.getMainHandItem();
        if (held.isEmpty() || target instanceof Player player && player.getAbilities().instabuild) {
            return;
        }
        target.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        ItemEntity dropped = new ItemEntity(target.level(), target.getX(), target.getY() + 0.3, target.getZ(), held.copy());
        dropped.setPickUpDelay(pickupDelay);
        dropped.setDeltaMovement(target.getRandom().nextGaussian() * 0.05, 0.2, target.getRandom().nextGaussian() * 0.05);
        target.level().addFreshEntity(dropped);
    }
}
