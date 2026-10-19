package fr.xopi.mushocraft.magic.spell;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * The seven ranks of magic (wiki, page <i>Magic</i>, table « Magic Rank Title »). Higher ranks take longer
 * to chant: the wiki notes that incantations grow longer with rank.
 */
public enum SpellRank implements StringRepresentable {
    BEGINNER(20),
    INTERMEDIATE(30),
    ADVANCED(45),
    SAINT(70),
    KING(100),
    EMPEROR(140),
    GOD(200);

    public static final Codec<SpellRank> CODEC = StringRepresentable.fromEnum(SpellRank::values);

    private final int incantationTicks;

    SpellRank(int incantationTicks) {
        this.incantationTicks = incantationTicks;
    }

    public int incantationTicks() {
        return incantationTicks;
    }

    public Component displayName() {
        return Component.translatable("rank.mushocraft." + getSerializedName());
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
