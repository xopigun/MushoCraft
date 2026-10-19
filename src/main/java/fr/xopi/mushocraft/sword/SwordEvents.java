package fr.xopi.mushocraft.sword;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.compat.epicfight.EpicFightModes;
import fr.xopi.mushocraft.compat.epicfight.EpicFightWeapons;
import fr.xopi.mushocraft.core.MushoAttachments;
import fr.xopi.mushocraft.life.event.PlayerBornEvent;
import fr.xopi.mushocraft.sword.technique.AuraShiftAction;
import fr.xopi.mushocraft.sword.technique.CounterStances;
import fr.xopi.mushocraft.sword.technique.TechniqueStrikes;
import fr.xopi.mushocraft.sword.technique.SwordDomains;
import fr.xopi.mushocraft.sword.technique.TechniqueTempo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.entity.DealDamageEvent;

/**
 * Server-side glue between Epic Fight's combat and the sword styles: stronger hits during techniques, style practice
 * from ordinary hits, Water God counters, and a clean slate for a new life.
 */
@EventBusSubscriber(modid = MushoCraft.MODID)
public final class SwordEvents {
    private SwordEvents() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        EpicFightEventHooks.Entity.DELIVER_DAMAGE_PRE.registerEvent(SwordEvents::onEpicFightHit);
        EpicFightEventHooks.Entity.DELIVER_DAMAGE_POST.registerEvent(SwordEvents::onEpicFightHitDealt);
    }

    /** A hit dealt by the animation of an armed technique is multiplied and carries the technique's effect. */
    private static void onEpicFightHit(DealDamageEvent.Pre event) {
        LivingEntity fighter = event.getEntityPatch().getOriginal();
        if (fighter.level().isClientSide || event.getDamageSource().getAnimation() == null) {
            return;
        }
        TechniqueStrikes.matching(fighter, event.getDamageSource().getAnimation().registryName()).ifPresent(strike -> {
            event.setModifiedDamage(event.getModifiedDamage() * strike.damageMultiplier());
            strike.applyTo(event.getTarget());
        });
    }

    /** Fighting in battle mode with a weapon trains the style the player fights in. */
    private static void onEpicFightHitDealt(DealDamageEvent.Post event) {
        if (event.getEntityPatch().getOriginal() instanceof ServerPlayer player && EpicFightModes.isBattleMode(player)
                && EpicFightWeapons.holdsWeapon(player)) {
            SwordService.activeStyle(player).ifPresent(style ->
                    SwordService.gainExperience(player, style, event.getModifiedDamage()));
        }
    }

    /** A receiving stance (Flow, Light Reversal, Pecking): the awaited attack is answered. */
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity player = event.getEntity();
        if (player.level().isClientSide) {
            return;
        }
        CounterStances.active(player).ifPresent(stance -> {
            DamageSource source = event.getSource();
            boolean melee = source.getEntity() instanceof LivingEntity && source.getDirectEntity() == source.getEntity();
            boolean missile = source.getDirectEntity() instanceof Projectile;
            if (melee && source.getEntity() instanceof LivingEntity attacker) {
                CounterStances.clear(player);
                stance.kind().answer(player, attacker, event, stance.counterMultiplier());
            } else if (missile && stance.deflectsProjectiles()) {
                event.setCanceled(true);
                CounterStances.clear(player);
                player.level().playSound(null, player.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0F, 1.2F);
            }
        });
    }

    @SubscribeEvent
    public static void onBorn(PlayerBornEvent event) {
        ServerPlayer player = event.getEntity();
        clear(player);
        player.setData(MushoAttachments.SWORD_MASTERY, SwordMastery.none());
        StyleSync.push(player);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            clear(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            clear(player);
        }
    }

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TechniqueTempo.tick(player);
            AuraShiftAction.tick(player);
        }
    }

    private static void clear(ServerPlayer player) {
        TechniqueStrikes.clear(player);
        CounterStances.clear(player);
        TechniqueTempo.clear(player);
        AuraShiftAction.settle(player);
        SwordDomains.close(player);
    }
}
