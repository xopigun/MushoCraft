package fr.xopi.mushocraft.network;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.combat.move.MoveRef;
import fr.xopi.mushocraft.combat.move.MoveService;
import fr.xopi.mushocraft.magic.spell.MagicService;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

/**
 * Client → server: put a known move in a slot of the move bar, or empty the slot.
 */
public record SetMoveSlotPayload(int slot, Optional<MoveRef> move) implements CustomPacketPayload {
    public static final Type<SetMoveSlotPayload> TYPE = new Type<>(MushoCraft.id("set_move_slot"));

    public static final StreamCodec<ByteBuf, SetMoveSlotPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SetMoveSlotPayload::slot,
            ByteBufCodecs.optional(MoveRef.STREAM_CODEC), SetMoveSlotPayload::move,
            SetMoveSlotPayload::new);

    @Override
    public Type<SetMoveSlotPayload> type() {
        return TYPE;
    }

    void handle(IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player
                && move.map(ref -> MagicService.knows(player, ref)).orElse(true)) {
            MoveService.setSlot(player, slot, move);
        }
    }
}
