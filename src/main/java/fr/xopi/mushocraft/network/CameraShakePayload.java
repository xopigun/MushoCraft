package fr.xopi.mushocraft.network;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.client.fx.ClientCameraShake;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server → client: shake the view this hard for this many ticks.
 */
public record CameraShakePayload(float intensity, int ticks) implements CustomPacketPayload {
    public static final Type<CameraShakePayload> TYPE = new Type<>(MushoCraft.id("camera_shake"));

    public static final StreamCodec<ByteBuf, CameraShakePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, CameraShakePayload::intensity,
            ByteBufCodecs.VAR_INT, CameraShakePayload::ticks,
            CameraShakePayload::new);

    @Override
    public Type<CameraShakePayload> type() {
        return TYPE;
    }

    void handle(IPayloadContext context) {
        ClientCameraShake.add(intensity, ticks);
    }
}
