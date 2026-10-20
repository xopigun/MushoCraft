package fr.xopi.mushocraft.monster;

import fr.xopi.mushocraft.MushoCraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * Tells a monster when one of its own blows lands (a bite, a claw: not a missile), whatever dealt it, vanilla melee or
 * an Epic Fight attack animation, so that it can add what its species carries (see {@link MushoMonster#onStrike}).
 */
@EventBusSubscriber(modid = MushoCraft.MODID)
public final class MonsterStrikes {
    private MonsterStrikes() {
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
        if (event.getSource().getDirectEntity() instanceof MushoMonster monster && monster == event.getSource().getEntity()
                && event.getNewDamage() > 0.0F && !monster.level().isClientSide) {
            monster.onStrike(event.getEntity());
        }
    }
}
