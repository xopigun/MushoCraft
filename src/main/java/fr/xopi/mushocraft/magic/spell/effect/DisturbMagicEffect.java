package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.core.MushoMobEffects;
import fr.xopi.mushocraft.core.MushoParticles;
import fr.xopi.mushocraft.magic.entity.SpellZone;
import fr.xopi.mushocraft.magic.entity.ZoneVisual;
import fr.xopi.mushocraft.magic.fx.SpellFx;
import fr.xopi.mushocraft.magic.spell.ChantBroadcast;
import fr.xopi.mushocraft.magic.spell.Spell;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Disturb Magic (wiki, page <i>Disturb Magic</i>): interfering mana sent into magic still taking shape, so that it
 * loses its structure and scatters. Economical, but it must reach the magic: a limited range, and the spot where the
 * magic forms must be seen (a Deep Mist hides it). What it can disturb, in this order:
 * <ol>
 *     <li>a spell the aimed creature is chanting: it scatters, and no new one takes shape until the mana settles;</li>
 *     <li>magic that keeps drawing on mana: the shields on the aimed creature, or a barrier it stands in;</li>
 *     <li>nothing in reach: the user practises on their own mana, forming a wisp in one hand and scattering it with
 *     the other, as Rudeus trained.</li>
 * </ol>
 * It never seals a body part or forbids magic for long: a skilled caster simply forms the spell again.
 */
public final class DisturbMagicEffect implements SpellEffect {
    private static final double RANGE = 10.0;
    private static final int SCATTER_TICKS = 10;
    /** The user's own mana, felt at the outstretched hand. */
    private static final int COLOUR = 0xFF8FB8;
    private static final int MAGIC_SHIELD_COLOUR = 0x7FD4FF;
    private static final int PHYSICAL_SHIELD_COLOUR = 0xE0B070;
    private static final int BARRIER_COLOUR = 0xFFE680;
    private static final Set<ZoneVisual> BARRIERS = Set.of(ZoneVisual.SEALING_DOME, ZoneVisual.CONFINEMENT_CELL);

    @Override
    public boolean cast(SpellContext context) {
        ServerLevel level = context.level();
        LivingEntity user = context.caster();
        Optional<LivingEntity> aimed = SpellTargeting.aimedCreature(context, RANGE)
                .filter(target -> user.hasLineOfSight(target) && !hiddenByMist(level, target));
        if (aimed.isPresent()) {
            LivingEntity target = aimed.get();
            Optional<Spell> forming = ChantBroadcast.chanted(target);
            if (forming.isPresent()) {
                target.addEffect(new MobEffectInstance(MushoMobEffects.DISTURBED, SCATTER_TICKS, 0, false, true), user);
                scatter(level, user, SpellFx.hand(target, true), forming.get().school().colour(), 1.0);
                return true;
            }
            if (target.hasEffect(MushoMobEffects.MAGIC_SHIELD) || target.hasEffect(MushoMobEffects.PHYSICAL_SHIELD)) {
                int colour = target.hasEffect(MushoMobEffects.MAGIC_SHIELD) ? MAGIC_SHIELD_COLOUR : PHYSICAL_SHIELD_COLOUR;
                target.removeEffect(MushoMobEffects.MAGIC_SHIELD);
                target.removeEffect(MushoMobEffects.PHYSICAL_SHIELD);
                scatter(level, user, target.getBoundingBox().getCenter(), colour, 1.4);
                return true;
            }
            Optional<SpellZone> barrier = barrierAround(level, target, user);
            if (barrier.isPresent()) {
                scatter(level, user, barrier.get().position().add(0.0, 1.0, 0.0), BARRIER_COLOUR, 2.0);
                barrier.get().discard();
                return true;
            }
        }
        practise(level, user);
        return true;
    }

    /** Whether the creature stands inside someone's Deep Mist, where the forming magic cannot be seen. */
    private static boolean hiddenByMist(ServerLevel level, LivingEntity target) {
        return zonesAround(level, target).stream().anyMatch(zone -> zone.visual() == ZoneVisual.MIST);
    }

    /** A barrier, not the user's own, that holds the creature. */
    private static Optional<SpellZone> barrierAround(ServerLevel level, LivingEntity target, LivingEntity user) {
        return zonesAround(level, target).stream()
                .filter(zone -> BARRIERS.contains(zone.visual()) && zone.caster() != user)
                .findFirst();
    }

    private static List<SpellZone> zonesAround(ServerLevel level, LivingEntity target) {
        return level.getEntitiesOfClass(SpellZone.class, target.getBoundingBox().inflate(16.0),
                zone -> zone.distanceToSqr(target.getX(), zone.getY(), target.getZ()) <= zone.radius() * zone.radius());
    }

    /** Practice alone: a wisp formed between the hands, held out before the eyes, scattered by the other hand. */
    private static void practise(ServerLevel level, LivingEntity user) {
        Vec3 wisp = user.getEyePosition().add(user.getLookAngle().scale(1.1)).subtract(0.0, 0.35, 0.0);
        scatter(level, user, wisp, COLOUR, 0.5);
    }

    /**
     * As in the anime (S1 ep. 21, Orsted against Rudeus): no beam is seen, only the user's outstretched hand; the magic
     * itself tears apart into shreds of its own colour, flung out and fading, with a pale spark.
     */
    private static void scatter(ServerLevel level, LivingEntity user, Vec3 at, int colour, double size) {
        Vec3 hand = SpellFx.hand(user, true);
        level.sendParticles(ColorParticleOption.create(MushoParticles.MANA_MOTE.get(), 0xFF000000 | COLOUR), hand.x, hand.y, hand.z,
                3, 0.05, 0.05, 0.05, 0.01);
        ColorParticleOption shred = ColorParticleOption.create(MushoParticles.MANA_MOTE.get(), 0xFF000000 | colour);
        level.sendParticles(shred, at.x, at.y, at.z, (int) (18 * size), 0.15 * size, 0.15 * size, 0.15 * size, 0.16 * size);
        level.sendParticles(ColorParticleOption.create(MushoParticles.IMPACT.get(), 0xFF000000 | colour), at.x, at.y, at.z, 0,
                0.5 * size, 0.0, 0.0, 1.0);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 0.9F, 1.6F);
    }
}
