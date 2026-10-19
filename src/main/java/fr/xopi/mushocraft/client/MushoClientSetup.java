package fr.xopi.mushocraft.client;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.client.hud.ManaBarLayer;
import fr.xopi.mushocraft.client.hud.MoveBarLayer;
import fr.xopi.mushocraft.client.input.MushoKeyMappings;
import fr.xopi.mushocraft.client.particle.ArcParticle;
import fr.xopi.mushocraft.client.particle.FireParticles;
import fr.xopi.mushocraft.client.particle.FlipbookParticle;
import fr.xopi.mushocraft.client.particle.ManaMote;
import fr.xopi.mushocraft.client.particle.OrientedParticle;
import fr.xopi.mushocraft.client.particle.RisingLight;
import fr.xopi.mushocraft.client.particle.SparkleParticle;
import fr.xopi.mushocraft.client.particle.StreakParticle;
import fr.xopi.mushocraft.client.render.SpellOrbModel;
import fr.xopi.mushocraft.client.render.SpellProjectileRenderer;
import fr.xopi.mushocraft.client.render.TeacherRenderer;
import fr.xopi.mushocraft.client.render.ThrownWeaponRenderer;
import fr.xopi.mushocraft.client.render.spell.SpellBeamRenderer;
import fr.xopi.mushocraft.client.render.spell.SpellFireRenderer;
import fr.xopi.mushocraft.client.render.spell.SpellPillarRenderer;
import fr.xopi.mushocraft.client.render.spell.SpellZoneRenderer;
import fr.xopi.mushocraft.client.render.spell.SpiritRenderers;
import fr.xopi.mushocraft.client.render.spell.WaterSheetRenderer;
import fr.xopi.mushocraft.client.screen.MoveIcons;
import fr.xopi.mushocraft.core.MushoEntities;
import fr.xopi.mushocraft.core.MushoParticles;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import fr.xopi.mushocraft.client.render.MonsterRenderer;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Client-only registrations: HUD layers, key mappings, particles, entity renderers and models.
 */
@EventBusSubscriber(modid = MushoCraft.MODID, value = Dist.CLIENT)
public final class MushoClientSetup {
    private MushoClientSetup() {
    }

    /** Move icons are smoothed once; a resource reload makes their textures anew, so they are smoothed again. */
    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) manager -> MoveIcons.reset());
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.FOOD_LEVEL, MushoCraft.id("mana_bar"), new ManaBarLayer());
        event.registerAbove(VanillaGuiLayers.HOTBAR, MushoCraft.id("move_bar"), new MoveBarLayer());
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MushoEntities.SPELL_PROJECTILE.get(), SpellProjectileRenderer::new);
        event.registerEntityRenderer(MushoEntities.THROWN_WEAPON.get(), ThrownWeaponRenderer::new);
        event.registerEntityRenderer(MushoEntities.SPELL_ZONE.get(), SpellZoneRenderer::new);
        event.registerEntityRenderer(MushoEntities.WATER_SHEET.get(), WaterSheetRenderer::new);
        event.registerEntityRenderer(MushoEntities.SPELL_PILLAR.get(), SpellPillarRenderer::new);
        event.registerEntityRenderer(MushoEntities.SPELL_FIRE.get(), SpellFireRenderer::new);
        event.registerEntityRenderer(MushoEntities.SPELL_BEAM.get(), SpellBeamRenderer::new);
        event.registerEntityRenderer(MushoEntities.LAMPLIGHT_SPIRIT.get(), SpiritRenderers.Lamplight::new);
        event.registerEntityRenderer(MushoEntities.SUMMONED_SPIRIT.get(), SpiritRenderers.Fighting::new);
        event.registerEntityRenderer(MushoEntities.SWORD_MASTER.get(), TeacherRenderer::new);
        event.registerEntityRenderer(MushoEntities.MAGIC_TUTOR.get(), TeacherRenderer::new);
        event.registerEntityRenderer(MushoEntities.ASSAULT_DOG.get(), context -> new MonsterRenderer<>(context, 0.5F));
    }

    @SubscribeEvent
    public static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(MushoParticles.FLAME.get(), FireParticles.FlameProvider::new);
        event.registerSpriteSet(MushoParticles.EMBER.get(), FireParticles.EmberProvider::new);
        event.registerSpecial(MushoParticles.SMOKE.get(), new FireParticles.SmokeProvider());
        event.registerSpriteSet(MushoParticles.FLASH.get(), FireParticles.FlashProvider::new);
        event.registerSpriteSet(MushoParticles.SHOCK_RING.get(), FireParticles.ShockRingProvider::new);
        event.registerSpecial(MushoParticles.MANA_MOTE.get(), new ManaMote.Provider());
        event.registerSpecial(MushoParticles.SHOCK_WAVE.get(), new OrientedParticle.ShockWaveProvider());
        event.registerSpecial(MushoParticles.SLASH.get(), new OrientedParticle.SlashProvider());
        event.registerSpriteSet(MushoParticles.ARC.get(), ArcParticle.Provider::new);
        event.registerSpecial(MushoParticles.RISING_LIGHT.get(), new RisingLight.Provider());
        event.registerSpecial(MushoParticles.IMPACT.get(), new FlipbookParticle.ImpactProvider());
        event.registerSpecial(MushoParticles.BLAST.get(), new FlipbookParticle.BlastProvider());
        event.registerSpecial(MushoParticles.WIND_STREAK.get(), new StreakParticle.Provider());
        event.registerSpecial(MushoParticles.SPARKLE.get(), new SparkleParticle.Provider());
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SpellOrbModel.LAYER, SpellOrbModel::createLayer);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(MushoKeyMappings.CIRCULATE_MANA);
        event.register(MushoKeyMappings.OPEN_MOVES);
        event.register(MushoKeyMappings.TOGGLE_AURA);
    }
}
