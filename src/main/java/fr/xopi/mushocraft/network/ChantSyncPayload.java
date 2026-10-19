package fr.xopi.mushocraft.network;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.client.ClientChants;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server → clients watching a caster: it started chanting a spell of a school for some ticks, or it stopped
 * ({@code school} −1). Lets every client draw the spell taking shape in the caster's hand.
 */
public record ChantSyncPayload(int entityId, int school, int ticks) implements CustomPacketPayload {
    public static final Type<ChantSyncPayload> TYPE = new Type<>(MushoCraft.id("chant_sync"));

    public static final StreamCodec<ByteBuf, ChantSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ChantSyncPayload::entityId,
            ByteBufCodecs.VAR_INT, ChantSyncPayload::school,
            ByteBufCodecs.VAR_INT, ChantSyncPayload::ticks,
            ChantSyncPayload::new);

    @Override
    public Type<ChantSyncPayload> type() {
        return TYPE;
    }

    void handle(IPayloadContext context) {
        ClientChants.update(entityId, school, ticks);
    }
}
