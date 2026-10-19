package fr.xopi.mushocraft.network;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.magic.mana.ManaTraining;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client → server: the player starts or stops circulating mana (the training key is held or released).
 */
public record ManaCirculationPayload(boolean active) implements CustomPacketPayload {
    public static final Type<ManaCirculationPayload> TYPE = new Type<>(MushoCraft.id("mana_circulation"));

    public static final StreamCodec<ByteBuf, ManaCirculationPayload> STREAM_CODEC =
            ByteBufCodecs.BOOL.map(ManaCirculationPayload::new, ManaCirculationPayload::active);

    @Override
    public Type<ManaCirculationPayload> type() {
        return TYPE;
    }

    void handle(IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            ManaTraining.setCirculating(player, active);
        }
    }
}
