package fr.xopi.mushocraft.combat.move;

import com.mojang.serialization.Codec;
import fr.xopi.mushocraft.core.MushoRegistries;
import fr.xopi.mushocraft.magic.spell.MagicService;
import fr.xopi.mushocraft.sword.SwordService;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * The families of moves, each backed by its own typed registry and its own knowledge.
 */
public enum MoveKind implements StringRepresentable {
    SPELL {
        @Override
        public Optional<Move> resolve(ResourceLocation id) {
            return MushoRegistries.SPELL.getOptional(id).map(spell -> spell);
        }

        @Override
        public List<ResourceLocation> known(ServerPlayer player) {
            return MagicService.knownSpells(player);
        }
    },
    TECHNIQUE {
        @Override
        public Optional<Move> resolve(ResourceLocation id) {
            return MushoRegistries.TECHNIQUE.getOptional(id).map(technique -> technique);
        }

        @Override
        public List<ResourceLocation> known(ServerPlayer player) {
            return SwordService.knownTechniques(player);
        }
    };

    public static final Codec<MoveKind> CODEC = StringRepresentable.fromEnum(MoveKind::values);
    public static final StreamCodec<ByteBuf, MoveKind> STREAM_CODEC =
            ByteBufCodecs.idMapper(index -> values()[index], MoveKind::ordinal);

    public abstract Optional<Move> resolve(ResourceLocation id);

    /** Ids of the moves of this kind the player knows, in display order. */
    public abstract List<ResourceLocation> known(ServerPlayer player);

    public boolean isKnownBy(ServerPlayer player, ResourceLocation id) {
        return known(player).contains(id);
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
