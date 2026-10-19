package fr.xopi.mushocraft.body;

import com.mojang.serialization.Codec;
import fr.xopi.mushocraft.talent.Talent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * The four physical stats of the sword path. Each one is trained by its own activity and is backed by
 * the natural talent of the same name.
 */
public enum BodyStat implements StringRepresentable {
    STRENGTH(Talent.STRENGTH),
    VITALITY(Talent.VITALITY),
    SPEED(Talent.SPEED),
    RESISTANCE(Talent.RESISTANCE);

    public static final Codec<BodyStat> CODEC = StringRepresentable.fromEnum(BodyStat::values);

    private final Talent talent;

    BodyStat(Talent talent) {
        this.talent = talent;
    }

    public Talent talent() {
        return talent;
    }

    public Component displayName() {
        return Component.translatable("body_stat.mushocraft." + getSerializedName());
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
