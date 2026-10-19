package fr.xopi.mushocraft.life;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * Steps of a human life. Adulthood at 15 follows the Asura Kingdom custom (LN vol. 1 ch. 5);
 * the earlier thresholds are gameplay choices.
 */
public enum LifeStage implements StringRepresentable {
    BABY(0.0),
    CHILD(3.0),
    ADOLESCENT(12.0),
    ADULT(15.0);

    public static final Codec<LifeStage> CODEC = StringRepresentable.fromEnum(LifeStage::values);

    private final double startAge;

    LifeStage(double startAge) {
        this.startAge = startAge;
    }

    public static LifeStage at(double ageInYears) {
        LifeStage[] stages = values();
        for (int i = stages.length - 1; i > 0; i--) {
            if (ageInYears >= stages[i].startAge) {
                return stages[i];
            }
        }
        return BABY;
    }

    public double startAge() {
        return startAge;
    }

    public Component displayName() {
        return Component.translatable("life_stage.mushocraft." + getSerializedName());
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
