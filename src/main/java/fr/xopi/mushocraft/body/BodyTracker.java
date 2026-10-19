package fr.xopi.mushocraft.body;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.body.aura.BattleAuraService;
import fr.xopi.mushocraft.core.MushoAttachments;
import fr.xopi.mushocraft.body.aura.BattleAuraData;
import fr.xopi.mushocraft.life.event.LifeStageChangedEvent;
import fr.xopi.mushocraft.life.event.PlayerBornEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import yesman.epicfight.api.event.EpicFightEventHooks;

/**
 * Where physical training comes from, and when the body bonuses are refreshed.
 * <ul>
 *     <li>Strength: melee damage dealt.</li>
 *     <li>Vitality: damage endured and running.</li>
 *     <li>Speed: running and dodging attacks (Epic Fight dodge).</li>
 *     <li>Resistance: damage endured and attacks blocked with a shield.</li>
 * </ul>
 */
@EventBusSubscriber(modid = MushoCraft.MODID)
public final class BodyTracker {
    private static final int REFRESH_INTERVAL_TICKS = 20;
    private static final double SPRINT_SPEED_PER_TICK = 0.03;
    private static final double SPRINT_VITALITY_PER_TICK = 0.02;
    private static final double DODGE_SPEED = 5.0;
    private static final double ENDURED_RESISTANCE_FACTOR = 1.5;

    private BodyTracker() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        EpicFightEventHooks.Entity.ON_DODGE.registerEvent(dodge -> {
            if (dodge.getEntityPatch().getOriginal() instanceof ServerPlayer player) {
                BodyService.train(player, BodyStat.SPEED, DODGE_SPEED);
            }
        });
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
        float damage = event.getNewDamage();
        if (damage <= 0.0F) {
            return;
        }
        if (event.getSource().getDirectEntity() instanceof ServerPlayer attacker && event.getSource().getEntity() == attacker) {
            BodyService.train(attacker, BodyStat.STRENGTH, damage);
        }
        if (event.getEntity() instanceof ServerPlayer victim) {
            BodyService.train(victim, BodyStat.VITALITY, damage);
            BodyService.train(victim, BodyStat.RESISTANCE, damage * ENDURED_RESISTANCE_FACTOR);
        }
    }

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getBlocked()) {
            BodyService.train(player, BodyStat.RESISTANCE, event.getBlockedDamage());
        }
    }

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.isSprinting() && player.onGround()) {
            BodyService.train(player, BodyStat.SPEED, SPRINT_SPEED_PER_TICK);
            BodyService.train(player, BodyStat.VITALITY, SPRINT_VITALITY_PER_TICK);
        }
        if (player.tickCount % SprintMomentum.INTERVAL_TICKS == 0) {
            SprintMomentum.tick(player);
        }
        if (player.tickCount % REFRESH_INTERVAL_TICKS == 0) {
            BattleAuraService.tick(player, REFRESH_INTERVAL_TICKS / 20.0);
            BodyService.apply(player);
        }
    }

    /** A new life starts with an untrained body and no aura. */
    @SubscribeEvent
    public static void onBorn(PlayerBornEvent event) {
        ServerPlayer player = event.getEntity();
        player.setData(MushoAttachments.BODY_TRAINING, BodyTraining.none());
        player.setData(MushoAttachments.BATTLE_AURA, BattleAuraData.none());
        BattleAuraService.refresh(player);
    }

    @SubscribeEvent
    public static void onStageChanged(LifeStageChangedEvent event) {
        BodyService.apply(event.getEntity());
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        refresh(event.getEntity());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        refresh(event.getEntity());
    }

    private static void refresh(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            BattleAuraService.refresh(serverPlayer);
        }
    }
}
