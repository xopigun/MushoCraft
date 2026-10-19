package fr.xopi.mushocraft.network;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.client.ClientMoveState;
import fr.xopi.mushocraft.combat.move.CooldownView;
import fr.xopi.mushocraft.combat.move.KnownMove;
import fr.xopi.mushocraft.combat.move.MoveLoadout;
import fr.xopi.mushocraft.combat.move.MoveRef;
import fr.xopi.mushocraft.magic.spell.ManaControl;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

/**
 * Server → client: the move bar, its cooldowns and the moves the player knows, with their mastery.
 */
public record MovesSyncPayload(MoveLoadout loadout, List<CooldownView> cooldowns, List<KnownMove> known, double manaControl)
        implements CustomPacketPayload {
    public static final Type<MovesSyncPayload> TYPE = new Type<>(MushoCraft.id("moves_sync"));

    public static final StreamCodec<ByteBuf, MovesSyncPayload> STREAM_CODEC = StreamCodec.composite(
            MoveLoadout.STREAM_CODEC, MovesSyncPayload::loadout,
            CooldownView.STREAM_CODEC.apply(ByteBufCodecs.list()), MovesSyncPayload::cooldowns,
            KnownMove.STREAM_CODEC.apply(ByteBufCodecs.list()), MovesSyncPayload::known,
            ByteBufCodecs.DOUBLE, MovesSyncPayload::manaControl,
            MovesSyncPayload::new);

    @Override
    public Type<MovesSyncPayload> type() {
        return TYPE;
    }

    void handle(IPayloadContext context) {
        ClientMoveState.update(loadout, cooldowns, known, new ManaControl(manaControl));
    }
}
