package fr.xopi.mushocraft.magic.spell;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.combat.move.CooldownService;
import fr.xopi.mushocraft.combat.move.MoveLoadout;
import fr.xopi.mushocraft.combat.move.MovesSync;
import fr.xopi.mushocraft.core.MushoAttachments;
import fr.xopi.mushocraft.life.event.PlayerBornEvent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Server-side glue for casting: chant progress every tick, broken chants, and move bar resyncs.
 */
@EventBusSubscriber(modid = MushoCraft.MODID)
public final class MagicEvents {
    private MagicEvents() {
    }

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SpellCasting.tick(player);
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getNewDamage() > 0.0F) {
            SpellCasting.interrupt(player);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SpellCasting.clear(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SpellCasting.clear(player);
        }
    }

    /** A new life starts without any knowledge of magic. */
    @SubscribeEvent
    public static void onBorn(PlayerBornEvent event) {
        ServerPlayer player = event.getEntity();
        SpellCasting.clear(player);
        player.setData(MushoAttachments.LOADOUT, MoveLoadout.empty());
        CooldownService.clear(player);
        MagicService.forgetAll(player);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MovesSync.push(player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MovesSync.push(player);
        }
    }
}
