package fr.xopi.mushocraft.network;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.combat.move.MoveLoadout;
import fr.xopi.mushocraft.combat.move.MoveService;
import fr.xopi.mushocraft.compat.epicfight.EpicFightModes;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client → server: a move bar slot was pressed. Ignored outside battle mode, where 1-9 select hotbar items.
 */
public record UseMovePayload(int slot) implements CustomPacketPayload {
    public static final Type<UseMovePayload> TYPE = new Type<>(MushoCraft.id("use_move"));

    public static final StreamCodec<ByteBuf, UseMovePayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(UseMovePayload::new, UseMovePayload::slot);

    @Override
    public Type<UseMovePayload> type() {
        return TYPE;
    }

    void handle(IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player && slot >= 0 && slot < MoveLoadout.SIZE
                && EpicFightModes.isBattleMode(player)) {
            MoveService.useAndReport(player, slot);
        }
    }
}
