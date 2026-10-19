package fr.xopi.mushocraft.core;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.magic.spell.Spell;
import fr.xopi.mushocraft.sword.technique.Technique;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

/**
 * The mod's own typed registries. They are synced, so clients know every move to draw the move bar.
 */
@EventBusSubscriber(modid = MushoCraft.MODID)
public final class MushoRegistries {
    public static final ResourceKey<Registry<Spell>> SPELL_KEY = ResourceKey.createRegistryKey(MushoCraft.id("spell"));

    public static final Registry<Spell> SPELL = new RegistryBuilder<>(SPELL_KEY).sync(true).create();

    public static final ResourceKey<Registry<Technique>> TECHNIQUE_KEY = ResourceKey.createRegistryKey(MushoCraft.id("technique"));

    public static final Registry<Technique> TECHNIQUE = new RegistryBuilder<>(TECHNIQUE_KEY).sync(true).create();

    private MushoRegistries() {
    }

    @SubscribeEvent
    public static void onNewRegistry(NewRegistryEvent event) {
        event.register(SPELL);
        event.register(TECHNIQUE);
    }
}
