package fr.xopi.mushocraft;

import com.mojang.logging.LogUtils;
import fr.xopi.mushocraft.client.render.EpicFightRenderers;
import fr.xopi.mushocraft.compat.epicfight.EpicFightEntities;
import fr.xopi.mushocraft.compat.epicfight.StyleMovesets;
import fr.xopi.mushocraft.core.MushoAttachments;
import fr.xopi.mushocraft.core.MushoBlocks;
import fr.xopi.mushocraft.client.MushoClientConfig;
import fr.xopi.mushocraft.core.MushoConfig;
import fr.xopi.mushocraft.core.MushoCreativeTabs;
import fr.xopi.mushocraft.core.MushoDataComponents;
import fr.xopi.mushocraft.core.MushoEntities;
import fr.xopi.mushocraft.core.MushoItems;
import fr.xopi.mushocraft.core.MushoMobEffects;
import fr.xopi.mushocraft.core.MushoParticles;
import fr.xopi.mushocraft.magic.spell.MushoSpells;
import fr.xopi.mushocraft.sword.technique.MushoTechniques;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
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
        MushoDataComponents.register(modEventBus);
        MushoBlocks.register(modEventBus);
        MushoItems.register(modEventBus);
        MushoEntities.register(modEventBus);
        MushoMobEffects.register(modEventBus);
        MushoParticles.register(modEventBus);
        MushoCreativeTabs.register(modEventBus);
        MushoSpells.register(modEventBus);
        MushoTechniques.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.SERVER, MushoConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, MushoClientConfig.SPEC);
        EpicFightEntities.registerPatches();
        StyleMovesets.register(modEventBus);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            EpicFightRenderers.register();
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
