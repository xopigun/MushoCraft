package fr.xopi.mushocraft.magic.focus;

import fr.xopi.mushocraft.magic.spell.SpellSchool;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A wand or a staff (wiki, page <i>Magic Tools and Magic Items</i>): it channels mana and makes magic more efficient,
 * its quality set by its wood and its magic stone. Magic never needs one; held in either hand, it raises the power of
 * spells by a share, and some favour particular schools (Aqua Heartia: water above all).
 */
public class MagicFocusItem extends Item {
    private final double bonus;
    private final Map<SpellSchool, Double> affinities;

    /**
     * @param bonus      extra power for every school (0.2 = +20%)
     * @param affinities extra power for some schools, on top of the general bonus
     */
    public MagicFocusItem(Properties properties, double bonus, Map<SpellSchool, Double> affinities) {
        super(properties.stacksTo(1));
        this.bonus = bonus;
        this.affinities = affinities.isEmpty() ? Map.of() : new EnumMap<>(affinities);
    }

    /** Extra power given to a spell of this school (0.2 = +20%). */
    public double bonus(SpellSchool school) {
        return bonus + affinities.getOrDefault(school, 0.0);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (bonus > 0.0) {
            tooltip.add(Component.translatable("item.mushocraft.magic_focus.bonus", percent(bonus)).withStyle(ChatFormatting.BLUE));
        }
        affinities.forEach((school, extra) -> tooltip.add(Component.translatable("item.mushocraft.magic_focus.affinity",
                percent(bonus + extra), school.displayName()).withStyle(ChatFormatting.BLUE)));
    }

    private static String percent(double share) {
        return String.format(Locale.ROOT, "%.0f", share * 100.0);
    }
}
