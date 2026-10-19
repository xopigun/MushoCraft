package fr.xopi.mushocraft.network;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.body.aura.BattleAuraService;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client → server: the aura key was pressed.
 */
public record ToggleAuraPayload() implements CustomPacketPayload {
    public static final Type<ToggleAuraPayload> TYPE = new Type<>(MushoCraft.id("toggle_aura"));

    public static final StreamCodec<ByteBuf, ToggleAuraPayload> STREAM_CODEC = StreamCodec.unit(new ToggleAuraPayload());

    @Override
    public Type<ToggleAuraPayload> type() {
        return TYPE;
    }

    void handle(IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            BattleAuraService.toggle(player);
        }
    }
}
