package fr.xopi.mushocraft.magic.status;

import fr.xopi.mushocraft.MushoCraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Applies every active ward of the hurt creature to the incoming damage.
 */
@EventBusSubscriber(modid = MushoCraft.MODID)
public final class WardHandler {
    private WardHandler() {
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        for (MobEffectInstance instance : event.getEntity().getActiveEffects()) {
            if (instance.getEffect().value() instanceof WardEffect ward && event.getSource().is(ward.warded())) {
                event.setAmount(event.getAmount() * WardEffect.damageTaken(instance.getAmplifier()));
            }
        }
    }
}
