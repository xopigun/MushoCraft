package fr.xopi.mushocraft.village;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * A person's sex, which decides their names, their look and who they may marry.
 */
public enum Sex implements StringRepresentable {
    MALE,
    FEMALE;

    public static final Codec<Sex> CODEC = StringRepresentable.fromEnum(Sex::values);

    public static Sex random(RandomSource random) {
        return random.nextBoolean() ? MALE : FEMALE;
    }

    public Sex opposite() {
        return this == MALE ? FEMALE : MALE;
    }

    public Component displayName() {
        return Component.translatable("sex.mushocraft." + getSerializedName());
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
