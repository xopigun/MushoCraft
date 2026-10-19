package fr.xopi.mushocraft.magic.item;

import fr.xopi.mushocraft.core.MushoDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A sword shaped out of an element by a spell (Ice Blade). It melts once its time is up,
 * whether held, stored in the inventory or lying on the ground. Epic Fight treats it as a sword
 * through {@code data/mushocraft/capabilities/weapons/ice_blade.json}.
 */
public class ConjuredSwordItem extends SwordItem {
    public ConjuredSwordItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    public static boolean hasMelted(ItemStack stack, Level level) {
        Long until = stack.get(MushoDataComponents.CONJURED_UNTIL.get());
        return until != null && level.getServer() != null && level.getServer().overworld().getGameTime() >= until;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity holder, int slot, boolean selected) {
        if (level instanceof ServerLevel server && hasMelted(stack, level)) {
            stack.setCount(0);
            server.playSound(null, holder.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.6F, 1.6F);
        }
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (!entity.level().isClientSide && hasMelted(stack, entity.level())) {
            entity.discard();
            return true;
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.mushocraft.conjured.tooltip").withStyle(style -> style.withColor(0x8FD8FF)));
    }
}
