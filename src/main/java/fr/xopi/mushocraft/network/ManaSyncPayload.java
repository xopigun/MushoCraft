package fr.xopi.mushocraft.network;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.client.ClientManaState;
import fr.xopi.mushocraft.magic.mana.ManaSnapshot;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server → client: the owner's mana gauge.
 */
public record ManaSyncPayload(ManaSnapshot snapshot) implements CustomPacketPayload {
    public static final Type<ManaSyncPayload> TYPE = new Type<>(MushoCraft.id("mana_sync"));

    public static final StreamCodec<ByteBuf, ManaSyncPayload> STREAM_CODEC =
            ManaSnapshot.STREAM_CODEC.map(ManaSyncPayload::new, ManaSyncPayload::snapshot);

    @Override
    public Type<ManaSyncPayload> type() {
        return TYPE;
    }

    void handle(IPayloadContext context) {
        ClientManaState.set(snapshot);
    }
}
