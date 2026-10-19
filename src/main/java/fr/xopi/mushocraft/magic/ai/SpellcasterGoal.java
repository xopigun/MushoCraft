package fr.xopi.mushocraft.magic.ai;

import fr.xopi.mushocraft.magic.focus.MagicFocus;
import fr.xopi.mushocraft.magic.spell.Spell;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellIntent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

/**
 * A creature casts the spells it knows the way a player does: it stops, chants for the spell's incantation time,
 * can be interrupted by a blow, then releases the very same spell a player would. It picks what fits the fight:
 * <ul>
 *     <li>badly hurt: a support spell on itself (healing);</li>
 *     <li>an enemy close by: a defensive spell (wall, barrier, fortress);</li>
 *     <li>otherwise: an attack spell aimed at its target.</li>
 * </ul>
 * Creatures have no mana gauge: each spell's cooldown, plus a pause between two spells, paces them.
 */
public class SpellcasterGoal extends Goal {
    private static final double CAST_RANGE = 20.0;
    private static final double CLOSE_RANGE = 4.0;
    private static final float HURT_SHARE = 0.5F;
    private static final int PAUSE_BETWEEN_SPELLS = 30;

    private final PathfinderMob caster;
    private final Supplier<List<Spell>> repertoire;
    private final ToDoubleFunction<Spell> power;
    private final Map<Spell, Long> readyAt = new HashMap<>();
    private long nextSpellAt;

    private Spell chanting;
    private int chantLeft;
    private float healthAtStart;

    /**
     * @param repertoire the spells the creature knows (read again at every choice: it may learn or change school)
     * @param power      strength of each spell in this creature's hands
     */
    public SpellcasterGoal(PathfinderMob caster, Supplier<List<Spell>> repertoire, ToDoubleFunction<Spell> power) {
        this.caster = caster;
        this.repertoire = repertoire;
        this.power = power;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (now() < nextSpellAt) {
            return false;
        }
        Optional<Spell> choice = choose();
        choice.ifPresent(spell -> chanting = spell);
        return choice.isPresent();
    }

    @Override
    public boolean canContinueToUse() {
        return chanting != null && chantLeft > 0 && caster.isAlive();
    }

    @Override
    public void start() {
        chantLeft = Math.max(1, chanting.incantationTicks());
        healthAtStart = caster.getHealth();
        caster.getNavigation().stop();
        caster.level().playSound(null, caster.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.HOSTILE, 0.6F, 1.4F);
    }

    @Override
    public void tick() {
        // Minecraft only re-checks canContinueToUse every other tick: the tick after a release may still come here.
        if (chanting == null) {
            return;
        }
        LivingEntity target = caster.getTarget();
        if (target != null) {
            caster.getLookControl().setLookAt(target);
        }
        if (caster.getHealth() < healthAtStart) {
            // A blow breaks the chant, as it does for a player.
            stopChanting(PAUSE_BETWEEN_SPELLS);
            return;
        }
        if (caster.tickCount % 4 == 0 && caster.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.ENCHANT, caster.getX(), caster.getY() + caster.getBbHeight() * 0.7, caster.getZ(),
                    4, 0.4, 0.4, 0.4, 0.5);
        }
        if (--chantLeft <= 0) {
            release(target);
        }
    }

    @Override
    public void stop() {
        chanting = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private void release(LivingEntity target) {
        Spell spell = chanting;
        double strength = power.applyAsDouble(spell) * MagicFocus.multiplier(caster, spell.school());
        SpellContext context = switch (spell.intent()) {
            case ATTACK -> target == null ? null : SpellContext.aimedAt(caster, spell, strength, target.getEyePosition());
            // Healing and barriers are aimed at the caster itself: looking straight up, no one else is in reach.
            case SUPPORT -> new SpellContext(caster, spell, strength, new Vec3(0.0, 1.0, 0.0));
            case DEFEND, UTILITY -> new SpellContext(caster, spell, strength);
        };
        if (context != null && spell.effect().cast(context)) {
            readyAt.put(spell, now() + spell.cooldownTicks());
        }
        stopChanting(PAUSE_BETWEEN_SPELLS);
    }

    private void stopChanting(int pause) {
        chanting = null;
        chantLeft = 0;
        nextSpellAt = now() + pause;
    }

    /** The spell that fits the fight right now, if one is ready. */
    private Optional<Spell> choose() {
        LivingEntity target = caster.getTarget();
        boolean fighting = target != null && target.isAlive();
        if (caster.getHealth() < caster.getMaxHealth() * HURT_SHARE) {
            Optional<Spell> support = ready(SpellIntent.SUPPORT);
            if (support.isPresent()) {
                return support;
            }
        }
        if (!fighting) {
            return Optional.empty();
        }
        double distance = caster.distanceTo(target);
        if (distance < CLOSE_RANGE) {
            Optional<Spell> defence = ready(SpellIntent.DEFEND);
            if (defence.isPresent()) {
                return defence;
            }
        }
        if (distance < CAST_RANGE && caster.hasLineOfSight(target)) {
            return ready(SpellIntent.ATTACK);
        }
        return Optional.empty();
    }

    private Optional<Spell> ready(SpellIntent intent) {
        List<Spell> candidates = repertoire.get().stream()
                .filter(spell -> spell.intent() == intent && readyAt.getOrDefault(spell, 0L) <= now())
                .toList();
        return candidates.isEmpty() ? Optional.empty()
                : Optional.of(candidates.get(caster.getRandom().nextInt(candidates.size())));
    }

    private long now() {
        return caster.level().getGameTime();
    }
}
