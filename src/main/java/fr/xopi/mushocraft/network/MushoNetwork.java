package fr.xopi.mushocraft.network;

import fr.xopi.mushocraft.MushoCraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Declares every packet of the mod. Handlers run on the main thread (NeoForge default).
 */
@EventBusSubscriber(modid = MushoCraft.MODID)
public final class MushoNetwork {
    private static final String PROTOCOL_VERSION = "1";

    private MushoNetwork() {
    }

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(ManaSyncPayload.TYPE, ManaSyncPayload.STREAM_CODEC, ManaSyncPayload::handle);
        registrar.playToServer(ManaCirculationPayload.TYPE, ManaCirculationPayload.STREAM_CODEC,
                ManaCirculationPayload::handle);
        registrar.playToClient(MovesSyncPayload.TYPE, MovesSyncPayload.STREAM_CODEC, MovesSyncPayload::handle);
        registrar.playToServer(UseMovePayload.TYPE, UseMovePayload.STREAM_CODEC, UseMovePayload::handle);
        registrar.playToServer(SetMoveSlotPayload.TYPE, SetMoveSlotPayload.STREAM_CODEC, SetMoveSlotPayload::handle);
        registrar.playToClient(AuraSyncPayload.TYPE, AuraSyncPayload.STREAM_CODEC, AuraSyncPayload::handle);
        registrar.playToClient(StyleSyncPayload.TYPE, StyleSyncPayload.STREAM_CODEC, StyleSyncPayload::handle);
        registrar.playToServer(ToggleAuraPayload.TYPE, ToggleAuraPayload.STREAM_CODEC, ToggleAuraPayload::handle);
    }
}
