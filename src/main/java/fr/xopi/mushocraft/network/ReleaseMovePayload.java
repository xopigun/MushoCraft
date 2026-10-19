package fr.xopi.mushocraft.network;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.combat.move.MoveLoadout;
import fr.xopi.mushocraft.magic.spell.SpellCasting;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client → server: the key of a move bar slot was let go. A spell is chanted only while its key is held:
 * letting go before the end abandons the chant.
 */
public record ReleaseMovePayload(int slot) implements CustomPacketPayload {
    public static final Type<ReleaseMovePayload> TYPE = new Type<>(MushoCraft.id("release_move"));

    public static final StreamCodec<ByteBuf, ReleaseMovePayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(ReleaseMovePayload::new, ReleaseMovePayload::slot);

    @Override
    public Type<ReleaseMovePayload> type() {
        return TYPE;
    }

    void handle(IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player && slot >= 0 && slot < MoveLoadout.SIZE) {
            SpellCasting.abandon(player, slot);
        }
    }
}
