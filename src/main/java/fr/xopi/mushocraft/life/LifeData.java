package fr.xopi.mushocraft.life;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * When a character was born, in overworld game ticks, and the life stage last applied to them.
 * The stage is stored so that a change that happened while offline is still noticed on the next login.
 */
public record LifeData(long birthTick, LifeStage stage) {
    public static final Codec<LifeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("birth_tick").forGetter(LifeData::birthTick),
            LifeStage.CODEC.fieldOf("stage").forGetter(LifeData::stage)
    ).apply(instance, LifeData::new));

    public static LifeData bornAt(long tick) {
        return new LifeData(tick, LifeStage.BABY);
    }

    /** Placeholder required by the attachment system; a player without life data has not been born yet. */
    public static LifeData unborn() {
        return bornAt(0L);
    }

    public LifeData withBirthTick(long tick) {
        return new LifeData(tick, stage);
    }

    public LifeData withStage(LifeStage newStage) {
        return new LifeData(birthTick, newStage);
    }
}
