package fr.xopi.mushocraft;

import com.mojang.logging.LogUtils;
import fr.xopi.mushocraft.core.MushoAttachments;
import fr.xopi.mushocraft.core.MushoConfig;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * Entry point of MushoCraft. Only wires registries and configuration:
 * gameplay lives in its own packages and subscribes to events by itself.
 */
@Mod(MushoCraft.MODID)
public final class MushoCraft {
    public static final String MODID = "mushocraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MushoCraft(IEventBus modEventBus, ModContainer modContainer) {
        MushoAttachments.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.SERVER, MushoConfig.SPEC);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
