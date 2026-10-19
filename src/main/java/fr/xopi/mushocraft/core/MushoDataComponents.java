package fr.xopi.mushocraft.core;

import com.mojang.serialization.Codec;
import fr.xopi.mushocraft.MushoCraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Item data components of the mod.
 */
public final class MushoDataComponents {
    private static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MushoCraft.MODID);

    /** Overworld game tick at which a conjured item melts away. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> CONJURED_UNTIL =
            DATA_COMPONENTS.registerComponentType("conjured_until",
                    builder -> builder.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));

    private MushoDataComponents() {
    }

    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }
}
