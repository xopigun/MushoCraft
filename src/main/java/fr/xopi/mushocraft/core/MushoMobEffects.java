package fr.xopi.mushocraft.core;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.magic.status.SoakedEffect;
import fr.xopi.mushocraft.magic.status.WardEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
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

    /** Soaked by water magic: fire goes out, freezing magic bites deeper. */
    public static final DeferredHolder<MobEffect, SoakedEffect> SOAKED = MOB_EFFECTS.register("soaked",
            () -> new SoakedEffect(0x3F76E4));

    /** Heat Hand: hands so hot that blows set the target alight. */
    public static final DeferredHolder<MobEffect, MobEffect> HEATED_HANDS = MOB_EFFECTS.register("heated_hands",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xFF7A2A) {
            });

    /** Confinement Barrier: shut in a barrier where magic cannot be used (wiki, Magic Spells). */
    public static final DeferredHolder<MobEffect, MobEffect> CONFINED = MOB_EFFECTS.register("confined",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0xE8D88A) {
            });

    private MushoMobEffects() {
    }

    public static void register(IEventBus modEventBus) {
        MOB_EFFECTS.register(modEventBus);
    }
}
