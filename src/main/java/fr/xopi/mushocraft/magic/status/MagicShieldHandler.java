package fr.xopi.mushocraft.magic.status;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.core.MushoDamageTypes;
import fr.xopi.mushocraft.core.MushoMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Weakens magical, fire and cold damage taken by creatures under Magic Shield.
 */
@EventBusSubscriber(modid = MushoCraft.MODID)
public final class MagicShieldHandler {
    private MagicShieldHandler() {
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        MobEffectInstance shield = event.getEntity().getEffect(MushoMobEffects.MAGIC_SHIELD);
        if (shield != null && event.getSource().is(MushoDamageTypes.BLOCKED_BY_MAGIC_SHIELD)) {
            event.setAmount(event.getAmount() * MagicShieldEffect.damageTaken(shield.getAmplifier()));
        }
    }
}
