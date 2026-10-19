package fr.xopi.mushocraft.network;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.client.ClientAuraState;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server → client: the owner's Battle Aura level and whether it is up.
 */
public record AuraSyncPayload(int level, boolean up) implements CustomPacketPayload {
    public static final Type<AuraSyncPayload> TYPE = new Type<>(MushoCraft.id("aura_sync"));

    public static final StreamCodec<ByteBuf, AuraSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AuraSyncPayload::level,
            ByteBufCodecs.BOOL, AuraSyncPayload::up,
            AuraSyncPayload::new);

    @Override
    public Type<AuraSyncPayload> type() {
        return TYPE;
    }

    void handle(IPayloadContext context) {
        ClientAuraState.set(level, up);
    }
}
