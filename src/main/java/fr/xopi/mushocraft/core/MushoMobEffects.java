package fr.xopi.mushocraft.core;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.magic.status.WardEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Status effects of the mod.
 */
public final class MushoMobEffects {
    private static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, MushoCraft.MODID);

    public static final DeferredHolder<MobEffect, WardEffect> MAGIC_SHIELD = MOB_EFFECTS.register("magic_shield",
            () -> new WardEffect(0x7FD4FF, MushoDamageTypes.BLOCKED_BY_MAGIC_SHIELD));

    public static final DeferredHolder<MobEffect, WardEffect> PHYSICAL_SHIELD = MOB_EFFECTS.register("physical_shield",
            () -> new WardEffect(0xC9A86A, MushoDamageTypes.BLOCKED_BY_PHYSICAL_SHIELD));

    private MushoMobEffects() {
    }

    public static void register(IEventBus modEventBus) {
        MOB_EFFECTS.register(modEventBus);
    }
}
