package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.core.MushoDataComponents;
import fr.xopi.mushocraft.magic.fx.SpellFx;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

/**
 * Shapes an object out of the element and puts it in the caster's hands (Ice Blade).
 * The object melts away after its duration: see {@code ConjuredItem}.
 */
public final class ConjureItemEffect implements SpellEffect {
    /** The pale blue light of the ice taking form in the hand. */
    private static final int ICE_LIGHT = 0xBFE8FF;

    private final Supplier<? extends Item> item;
    private final int durationTicks;
    private final SoundEvent sound;

    public ConjureItemEffect(Supplier<? extends Item> item, int durationTicks, SoundEvent sound) {
        this.item = item;
        this.durationTicks = durationTicks;
        this.sound = sound;
    }

    @Override
    public boolean cast(SpellContext context) {
        ItemStack stack = new ItemStack(item.get());
        stack.set(MushoDataComponents.CONJURED_UNTIL.get(), context.gameTime() + Math.round(durationTicks * context.power()));
        if (context.caster() instanceof Player player) {
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        } else if (context.caster().getMainHandItem().isEmpty()) {
            // A creature arms itself with the conjured weapon.
            context.caster().setItemSlot(EquipmentSlot.MAINHAND, stack);
        } else {
            return false;
        }
        context.level().playSound(null, context.caster().blockPosition(), sound, SoundSource.PLAYERS, 1.0F, 1.2F);
        SpellFx.conjureInHand(context.level(), context.caster(), ICE_LIGHT);
        return true;
    }
}
