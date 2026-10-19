package fr.xopi.mushocraft.network;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.sword.ActiveStyles;
import fr.xopi.mushocraft.sword.SwordStyle;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

/**
 * Server → client: the sword style a player fights in (empty: none). Sent to the player and to everyone
 * who sees them, since Epic Fight picks the attack moveset on the client too.
 */
public record StyleSyncPayload(int entityId, Optional<SwordStyle> style) implements CustomPacketPayload {
    public static final Type<StyleSyncPayload> TYPE = new Type<>(MushoCraft.id("style_sync"));

    private static final StreamCodec<ByteBuf, SwordStyle> STYLE_CODEC =
            ByteBufCodecs.idMapper(id -> SwordStyle.values()[id], SwordStyle::ordinal);

    public static final StreamCodec<ByteBuf, StyleSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, StyleSyncPayload::entityId,
            ByteBufCodecs.optional(STYLE_CODEC), StyleSyncPayload::style,
            StyleSyncPayload::new);

    @Override
    public Type<StyleSyncPayload> type() {
        return TYPE;
    }

    void handle(IPayloadContext context) {
        ActiveStyles.acceptFromServer(entityId, style);
    }
}
