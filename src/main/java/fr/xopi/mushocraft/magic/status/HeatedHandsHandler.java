package fr.xopi.mushocraft.magic.status;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.core.MushoMobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * Heat Hand: hands hot enough to melt stone (wiki) set alight whoever they strike in melee.
 */
@EventBusSubscriber(modid = MushoCraft.MODID)
public final class HeatedHandsHandler {
    private static final float BURN_SECONDS = 3.0F;

    private HeatedHandsHandler() {
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
        if (event.getSource().getDirectEntity() instanceof LivingEntity attacker && attacker == event.getSource().getEntity()
                && attacker.hasEffect(MushoMobEffects.HEATED_HANDS)) {
            event.getEntity().igniteForSeconds(BURN_SECONDS);
        }
    }
}
