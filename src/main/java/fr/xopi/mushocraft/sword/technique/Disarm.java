package fr.xopi.mushocraft.sword.technique;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

/**
 * A struck or crushed arm that can no longer hold a weapon (Arm Chop, Light Reversal, Pecking): creatures drop what
 * they hold, everyone is weakened for a while.
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
}
