package fr.xopi.mushocraft.sword.entity;

import fr.xopi.mushocraft.core.MushoItems;
import fr.xopi.mushocraft.sword.technique.Disarm;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.function.Supplier;

/**
 * The North God Style's thrown tricks (wiki), and the webs some monsters spit: what each one is and what it does where
 * it bursts.
 */
public enum TrickKind {
    /**
     * Teardrop Bullet: a bag full of powdered spices thrown at the enemy. Here it bursts into a burning cloud that blinds,
     * makes the eyes stream and the head spin.
     */
    SPICE(() -> MushoItems.SPICE_POUCH.get(), 2.5) {
        @Override
        void affect(LivingEntity victim) {
            victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
            victim.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 140, 0));
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
        }

        @Override
        void burst(ServerLevel level, Vec3 at) {
            level.sendParticles(new DustParticleOptions(new Vector3f(0.86F, 0.42F, 0.12F), 1.6F), at.x, at.y + 0.6, at.z, 70, 1.1, 0.7, 1.1, 0.02);
            level.sendParticles(new DustParticleOptions(new Vector3f(0.95F, 0.75F, 0.25F), 1.2F), at.x, at.y + 0.6, at.z, 40, 0.9, 0.6, 0.9, 0.02);
            level.playSound(null, at.x, at.y, at.z, SoundEvents.PANDA_SNEEZE, SoundSource.PLAYERS, 1.2F, 0.8F);
        }
    },
    /**
     * Crimson Ink: a red ball that bursts on contact and spreads a strong instant adhesive. Whoever is caught in it is
     * stuck in place for a few seconds.
     */
    CRIMSON_INK(() -> MushoItems.CRIMSON_BALL.get(), 2.0) {
        @Override
        void affect(LivingEntity victim) {
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 9));
            victim.addEffect(new MobEffectInstance(MobEffects.JUMP, 80, 128));
            victim.setDeltaMovement(Vec3.ZERO);
            victim.hurtMarked = true;
        }

        @Override
        void burst(ServerLevel level, Vec3 at) {
            level.sendParticles(new DustParticleOptions(new Vector3f(0.78F, 0.05F, 0.1F), 1.8F), at.x, at.y + 0.2, at.z, 60, 0.9, 0.25, 0.9, 0.0);
            level.sendParticles(new DustParticleOptions(new Vector3f(0.5F, 0.02F, 0.06F), 1.3F), at.x, at.y + 0.1, at.z, 30, 0.7, 0.1, 0.7, 0.0);
            level.playSound(null, at.x, at.y, at.z, SoundEvents.SLIME_BLOCK_BREAK, SoundSource.PLAYERS, 1.2F, 0.7F);
            level.playSound(null, at.x, at.y, at.z, SoundEvents.HONEY_BLOCK_SLIDE, SoundSource.PLAYERS, 1.0F, 0.6F);
        }
    },
    /**
     * A Tarantula Deathlord's web, spat so accurately that it catches the weapon and tears it from the hand (wiki, page
     * <i>Monsters</i>); its threads also hold the victim back for a moment.
     */
    WEB(() -> Items.COBWEB, 1.1) {
        @Override
        void affect(LivingEntity victim) {
            Disarm.tearAway(victim, 50);
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
        }

        @Override
        void burst(ServerLevel level, Vec3 at) {
            level.sendParticles(new DustParticleOptions(new Vector3f(0.92F, 0.92F, 0.95F), 1.1F), at.x, at.y + 0.3, at.z, 40, 0.5, 0.4, 0.5, 0.0);
            level.playSound(null, at.x, at.y, at.z, SoundEvents.WOOL_BREAK, SoundSource.HOSTILE, 1.0F, 0.6F);
        }
    };

    private final Supplier<Item> item;
    private final double radius;

    TrickKind(Supplier<Item> item, double radius) {
        this.item = item;
        this.radius = radius;
    }

    public Item item() {
        return item.get();
    }

    /** Bursts at a point: everyone within reach but the thrower is caught. */
    public void burstAt(ServerLevel level, Vec3 at, Entity thrower) {
        burst(level, at);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(radius),
                victim -> victim != thrower && victim.position().distanceTo(at) <= radius + victim.getBbWidth())) {
            affect(victim);
        }
    }

    abstract void affect(LivingEntity victim);

    abstract void burst(ServerLevel level, Vec3 at);
}
