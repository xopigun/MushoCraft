package fr.xopi.mushocraft.combat.move;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * A move the player knows, with how many times they have repeated it (shown as mastery on the client).
 */
public record KnownMove(MoveRef move, float repetitions) {
    public static final StreamCodec<ByteBuf, KnownMove> STREAM_CODEC = StreamCodec.composite(
            MoveRef.STREAM_CODEC, KnownMove::move,
            ByteBufCodecs.FLOAT, KnownMove::repetitions,
            KnownMove::new);
}
