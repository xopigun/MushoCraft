package fr.xopi.mushocraft.magic.spell;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * Schools of magic (wiki, page <i>Magic</i>). Names follow the glossary.
 */
public enum SpellSchool implements StringRepresentable {
    WATER(0x5AB4FF),
    FIRE(0xFF8A2A),
    WIND(0x9CF2C8),
    EARTH(0xC8955A),
    HEALING(0x9CFF8A),
    DETOXIFICATION(0xC7FF6B),
    BARRIER(0xFFE680),
    DIVINE_STRIKE(0xFFF4C0),
    SUMMONING(0xC08CFF),
    MIXED(0xE0E0FF),
    /**
     * Inherent magic: arts born of a race's own body (Disturb Magic, Howling...). No incantation reproduces them; a
     * pupil of another race reproduces some after understanding their theory and long training (wiki, page
     * <i>Magic</i> § Inherent Magic). Taught only by the rare masters who know them.
     */
    INHERENT(0xFF8FB8);

    public static final Codec<SpellSchool> CODEC = StringRepresentable.fromEnum(SpellSchool::values);

    private final int colour;

    SpellSchool(int colour) {
        this.colour = colour;
    }

    /** The colour its mana takes as a spell of this school forms (RGB). */
    public int colour() {
        return colour;
    }

    public Component displayName() {
        return Component.translatable("spell_school.mushocraft." + getSerializedName());
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
