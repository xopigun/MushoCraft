package fr.xopi.mushocraft.core;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * The seven ranks shared by magic (wiki, page <i>Magic</i>, « Magic Rank Title ») and by the three sword styles
 * (wiki, pages of each style, « Ranks »): Beginner, Intermediate, Advanced, Saint, King, Emperor, God.
 */
public enum Rank implements StringRepresentable {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    SAINT,
    KING,
    EMPEROR,
    GOD;

    public static final Codec<Rank> CODEC = StringRepresentable.fromEnum(Rank::values);

    public boolean isAtLeast(Rank other) {
        return ordinal() >= other.ordinal();
    }

    public Component displayName() {
        return Component.translatable("rank.mushocraft." + getSerializedName());
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
