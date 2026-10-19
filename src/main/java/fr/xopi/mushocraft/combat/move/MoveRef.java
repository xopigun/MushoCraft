package fr.xopi.mushocraft.combat.move;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * Serializable pointer to a move: which registry, which entry. Stored in loadouts and sent over the network.
 */
public record MoveRef(MoveKind kind, ResourceLocation id) {
    public static final Codec<MoveRef> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MoveKind.CODEC.fieldOf("kind").forGetter(MoveRef::kind),
            ResourceLocation.CODEC.fieldOf("id").forGetter(MoveRef::id)
    ).apply(instance, MoveRef::new));

    public static final StreamCodec<ByteBuf, MoveRef> STREAM_CODEC = StreamCodec.composite(
            MoveKind.STREAM_CODEC, MoveRef::kind,
            ResourceLocation.STREAM_CODEC, MoveRef::id,
            MoveRef::new);

    public static MoveRef spell(ResourceLocation id) {
        return new MoveRef(MoveKind.SPELL, id);
    }

    public static MoveRef technique(ResourceLocation id) {
        return new MoveRef(MoveKind.TECHNIQUE, id);
    }

    public Optional<Move> resolve() {
        return kind.resolve(id);
    }
}
