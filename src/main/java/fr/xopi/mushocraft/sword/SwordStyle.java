package fr.xopi.mushocraft.sword;

import com.mojang.serialization.Codec;
import fr.xopi.mushocraft.MushoCraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * The three great schools of swordsmanship (wiki, category <i>Swordstyle</i>).
 * <ul>
 *     <li>Sword God: « victory goes to he who moves first », speed and a single decisive blow.</li>
 *     <li>Water God: defence and counter-attacks, reading the opponent.</li>
 *     <li>North God: practical and adaptable, any weapon, any trick.</li>
 * </ul>
 */
public enum SwordStyle implements StringRepresentable {
    SWORD_GOD,
    WATER_GOD,
    NORTH_GOD;

    public static final Codec<SwordStyle> CODEC = StringRepresentable.fromEnum(SwordStyle::values);

    /** Key of the cooldown shared by every technique of the style. */
    public ResourceLocation cooldownKey() {
        return MushoCraft.id("style/" + getSerializedName());
    }

    public Component displayName() {
        return Component.translatable("sword_style.mushocraft." + getSerializedName());
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
