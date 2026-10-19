package fr.xopi.mushocraft.client;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.client.hud.ManaBarLayer;
import fr.xopi.mushocraft.client.hud.MoveBarLayer;
import fr.xopi.mushocraft.client.input.MushoKeyMappings;
import fr.xopi.mushocraft.client.render.SpellOrbModel;
import fr.xopi.mushocraft.client.render.SpellProjectileRenderer;
import fr.xopi.mushocraft.core.MushoEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Client-only registrations: HUD layers, key mappings, entity renderers and models.
 */
@EventBusSubscriber(modid = MushoCraft.MODID, value = Dist.CLIENT)
public final class MushoClientSetup {
    private MushoClientSetup() {
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.FOOD_LEVEL, MushoCraft.id("mana_bar"), new ManaBarLayer());
        event.registerAbove(VanillaGuiLayers.HOTBAR, MushoCraft.id("move_bar"), new MoveBarLayer());
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MushoEntities.SPELL_PROJECTILE.get(), SpellProjectileRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SpellOrbModel.LAYER, SpellOrbModel::createLayer);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(MushoKeyMappings.CIRCULATE_MANA);
        event.register(MushoKeyMappings.OPEN_MOVES);
    }
}
