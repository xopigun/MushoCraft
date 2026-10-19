package fr.xopi.mushocraft.sword.technique;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.compat.epicfight.EpicFightAnimations;
import fr.xopi.mushocraft.magic.fx.SpellFx;
import fr.xopi.mushocraft.magic.spell.ChantBroadcast;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import yesman.epicfight.gameasset.Animations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Water God Style, Deprivation Sword Kingdom (wiki): in her stance, the Water God holds a space around her, in every
 * direction and as large as a hall, in which she answers at once any movement against her, and even any working of
 * mana. It strikes no one who does nothing. Here: whoever attacks inside the field, or begins an incantation there,
 * is cut down at once by the field's holder.
 */
@EventBusSubscriber(modid = MushoCraft.MODID)
public final class SwordDomains {
    /** A field answers the same foe at most this often. */
    private static final int ANSWER_TICKS = 10;

    private static final Map<UUID, Domain> OPEN = new ConcurrentHashMap<>();

    private SwordDomains() {
    }

    public static void open(LivingEntity holder, double radius, int ticks, float multiplier) {
        OPEN.put(holder.getUUID(), new Domain(holder, radius, holder.level().getGameTime() + ticks, multiplier, new HashMap<>()));
    }

    public static Optional<Domain> of(LivingEntity holder) {
        Domain domain = OPEN.get(holder.getUUID());
        return domain != null && domain.isOpen() ? Optional.of(domain) : Optional.empty();
    }

    public static void close(LivingEntity holder) {
        OPEN.remove(holder.getUUID());
    }

    /** An attack made inside a field is answered; aimed at the holder, it never lands. */
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide || !(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }
        for (Domain domain : OPEN.values()) {
            if (domain.isOpen() && domain.holder != attacker && domain.contains(attacker) && domain.answer(attacker)) {
                if (event.getEntity() == domain.holder) {
                    event.setCanceled(true);
                }
            }
        }
    }

    /** Fields close in time; a working of mana inside one is answered. */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        Iterator<Domain> it = OPEN.values().iterator();
        while (it.hasNext()) {
            Domain domain = it.next();
            if (!domain.isOpen()) {
                it.remove();
                continue;
            }
            if (domain.holder.level() instanceof ServerLevel level && level.getGameTime() % 5 == 0) {
                for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, domain.box(),
                        e -> e != domain.holder && domain.contains(e) && ChantBroadcast.isChanting(e))) {
                    if (domain.answer(other)) {
                        ChantBroadcast.stopped(other);
                    }
                }
                if (level.getGameTime() % 10 == 0) {
                    domain.drawEdge(level);
                }
            }
        }
    }

    public static final class Domain {
        private final LivingEntity holder;
        private final double radius;
        private final long closesAt;
        private final float multiplier;
        private final Map<UUID, Long> lastAnswer;

        private Domain(LivingEntity holder, double radius, long closesAt, float multiplier, Map<UUID, Long> lastAnswer) {
            this.holder = holder;
            this.radius = radius;
            this.closesAt = closesAt;
            this.multiplier = multiplier;
            this.lastAnswer = lastAnswer;
        }

        boolean isOpen() {
            return holder.isAlive() && !holder.isRemoved() && holder.level().getGameTime() < closesAt;
        }

        public boolean contains(Entity entity) {
            return entity.position().distanceToSqr(holder.position()) <= radius * radius;
        }

        AABB box() {
            return holder.getBoundingBox().inflate(radius);
        }

        /** Cuts the foe down at once, unless it was answered a moment ago. */
        boolean answer(LivingEntity foe) {
            long now = holder.level().getGameTime();
            Long last = lastAnswer.get(foe.getUUID());
            if (last != null && now - last < ANSWER_TICKS) {
                return false;
            }
            lastAnswer.put(foe.getUUID(), now);
            Vec3 towards = foe.position().subtract(holder.position()).normalize();
            holder.setYRot((float) Math.toDegrees(Math.atan2(-towards.x, towards.z)));
            EpicFightAnimations.play(holder, Animations.UCHIGATANA_AUTO2);
            foe.hurt(CounterKind.blow(holder), CounterKind.strength(holder) * multiplier);
            if (holder.level() instanceof ServerLevel level) {
                SpellFx.slash(level, holder, towards, 1.5F, 0xCFE8FF);
            }
            holder.level().playSound(null, foe.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.5F);
            return true;
        }

        /** A faint ring of falling droplets marks the field's edge. */
        void drawEdge(ServerLevel level) {
            for (int i = 0; i < 24; i++) {
                double a = 2 * Math.PI * i / 24;
                level.sendParticles(ParticleTypes.FALLING_WATER, holder.getX() + Math.cos(a) * radius, holder.getY() + 0.2,
                        holder.getZ() + Math.sin(a) * radius, 1, 0.0, 0.2, 0.0, 0.0);
            }
        }
    }
}
