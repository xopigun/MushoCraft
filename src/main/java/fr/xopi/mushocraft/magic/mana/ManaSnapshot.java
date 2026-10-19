package fr.xopi.mushocraft.magic.mana;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Everything the player needs to see about their mana, computed on the server.
 */
public record ManaSnapshot(double current, double capacity, double ceiling) {
    public static final ManaSnapshot NONE = new ManaSnapshot(0.0, 0.0, 0.0);

    public static final StreamCodec<ByteBuf, ManaSnapshot> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, ManaSnapshot::current,
            ByteBufCodecs.DOUBLE, ManaSnapshot::capacity,
            ByteBufCodecs.DOUBLE, ManaSnapshot::ceiling,
            ManaSnapshot::new);

    public double fill() {
        return capacity <= 0.0 ? 0.0 : Math.clamp(current / capacity, 0.0, 1.0);
    }
}
