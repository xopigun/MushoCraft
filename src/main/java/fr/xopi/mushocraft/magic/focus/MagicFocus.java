package fr.xopi.mushocraft.magic.focus;

import fr.xopi.mushocraft.magic.spell.SpellSchool;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

/**
 * The wand or staff a caster holds, if any: the best one in either hand counts.
 */
public final class MagicFocus {
    private MagicFocus() {
    }

    /** Multiplier on the power, and on the size, of a spell of this school: 1.0 with empty hands. */
    public static double multiplier(LivingEntity caster, SpellSchool school) {
        double best = 0.0;
        for (InteractionHand hand : InteractionHand.values()) {
            if (caster.getItemInHand(hand).getItem() instanceof MagicFocusItem focus) {
                best = Math.max(best, focus.bonus(school));
            }
        }
        return 1.0 + best;
    }
}
