package fr.xopi.mushocraft.talent;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * Natural gifts rolled once at birth. A value of 1.0 is an ordinary human:
 * above makes the matching progression easier and its ceiling higher, below the opposite.
 */
public enum Talent implements StringRepresentable {
    /** Size of the mana reservoir at birth and how far it can grow. */
    MANA_POTENTIAL,
    STRENGTH,
    VITALITY,
    SPEED,
    RESISTANCE,
    /** How easily the character learns Battle Aura. */
    BATTLE_AURA_EASE;

    public static final Codec<Talent> CODEC = StringRepresentable.fromEnum(Talent::values);

    public Component displayName() {
        return Component.translatable("talent.mushocraft." + getSerializedName());
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
