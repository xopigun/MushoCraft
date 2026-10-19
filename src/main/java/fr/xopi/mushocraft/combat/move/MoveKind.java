package fr.xopi.mushocraft.combat.move;

import com.mojang.serialization.Codec;
import fr.xopi.mushocraft.core.MushoRegistries;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;
import java.util.Optional;

/**
 * The families of moves, each backed by its own typed registry.
 */
public enum MoveKind implements StringRepresentable {
    SPELL {
        @Override
        public Optional<Move> resolve(ResourceLocation id) {
            return MushoRegistries.SPELL.getOptional(id).map(spell -> spell);
        }
    };

    public static final Codec<MoveKind> CODEC = StringRepresentable.fromEnum(MoveKind::values);
    public static final StreamCodec<ByteBuf, MoveKind> STREAM_CODEC =
            ByteBufCodecs.idMapper(index -> values()[index], MoveKind::ordinal);

    public abstract Optional<Move> resolve(ResourceLocation id);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
