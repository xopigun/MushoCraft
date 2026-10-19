package fr.xopi.mushocraft.sword.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A North God trick carried in a pouch (spice bag, crimson ball): anyone can throw one by hand, like a snowball. The
 * techniques throw them without using any up.
 */
public class TrickItem extends Item {
    private static final int COOLDOWN_TICKS = 20;

    private final TrickKind kind;

    public TrickItem(TrickKind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.5F, 0.6F);
        if (!level.isClientSide) {
            ThrownTrick trick = ThrownTrick.create(level, player, kind);
            trick.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.3F, 1.0F);
            level.addFreshEntity(trick);
        }
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        stack.consume(1, player);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
