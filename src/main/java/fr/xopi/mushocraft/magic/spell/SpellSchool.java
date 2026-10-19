package fr.xopi.mushocraft.magic.spell;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * Schools of magic (wiki, page <i>Magic</i>). Names follow the glossary.
 */
public enum SpellSchool implements StringRepresentable {
    WATER,
    FIRE,
    WIND,
    EARTH,
    HEALING,
    DETOXIFICATION,
    BARRIER,
    DIVINE_STRIKE,
    SUMMONING,
    MIXED;

    public static final Codec<SpellSchool> CODEC = StringRepresentable.fromEnum(SpellSchool::values);

    public Component displayName() {
        return Component.translatable("spell_school.mushocraft." + getSerializedName());
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
