package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.core.MushoDataComponents;
import fr.xopi.mushocraft.combat.move.CooldownService;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

/**
 * Shapes an object out of the element and puts it in the caster's hands (Ice Blade).
 * The object melts away after its duration: see {@code ConjuredItem}.
 */
public final class ConjureItemEffect implements SpellEffect {
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
        long expiry = CooldownService.now(context.caster()) + Math.round(durationTicks * context.power());
        stack.set(MushoDataComponents.CONJURED_UNTIL.get(), expiry);
        if (!context.caster().getInventory().add(stack)) {
            context.caster().drop(stack, false);
        }
        context.level().playSound(null, context.caster().blockPosition(), sound, SoundSource.PLAYERS, 1.0F, 1.2F);
        return true;
    }
}
