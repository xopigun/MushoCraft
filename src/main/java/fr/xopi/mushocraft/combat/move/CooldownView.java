package fr.xopi.mushocraft.combat.move;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * A cooldown as the client sees it: relative times, since client and server clocks differ.
 */
public record CooldownView(ResourceLocation key, int remainingTicks, int totalTicks) {
    public static final StreamCodec<ByteBuf, CooldownView> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, CooldownView::key,
            ByteBufCodecs.VAR_INT, CooldownView::remainingTicks,
            ByteBufCodecs.VAR_INT, CooldownView::totalTicks,
            CooldownView::new);
}
