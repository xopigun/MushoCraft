package fr.xopi.mushocraft.magic.spell;

import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * How a spell is cast (wiki, page <i>Magic</i>): the full incantation, a truncated one, or no incantation at all.
 */
public enum IncantationMode {
    /** The whole incantation: the spell's full casting time. */
    FULL(1.0),
    /** A truncated incantation, as Roxy and Rudeus use: cast times drastically improved. */
    ABBREVIATED(0.5),
    /** Silent spell-casting: the caster moulds mana by imagination alone, no incantation at all. */
    SILENT(0.0);

    private final double timeFactor;

    IncantationMode(double timeFactor) {
        this.timeFactor = timeFactor;
    }

    /** Share of the full incantation time this mode takes. */
    public double timeFactor() {
        return timeFactor;
    }

    public Component displayName() {
        return Component.translatable("incantation.mushocraft." + name().toLowerCase(Locale.ROOT));
    }
}
