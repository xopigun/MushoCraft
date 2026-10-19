package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.magic.entity.SpellZone;
import fr.xopi.mushocraft.magic.entity.ZoneVisual;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;

/**
 * A lasting spell bound to the creature the caster aims at (Lightning strikes it, Confinement Barrier shuts it in):
 * a {@link SpellZone} set on that creature, whose behaviour knows its target.
 */
public final class TargetedZoneEffect implements SpellEffect {
    private final Function<LivingEntity, SpellZone.Behavior> behavior;
    private final double radius;
    private final int durationTicks;
    private final double range;
    private final boolean needsTarget;
    private final SoundEvent sound;

    /**
     * @param behavior    builds the zone's behaviour for its target (null when the spell can fall on a bare spot)
     * @param needsTarget whether the spell fizzles when no creature is aimed at; otherwise it falls on the aimed spot
     */
    public TargetedZoneEffect(Function<LivingEntity, SpellZone.Behavior> behavior, double radius, int durationTicks, double range,
                              boolean needsTarget, SoundEvent sound) {
        this.behavior = behavior;
        this.radius = radius;
        this.durationTicks = durationTicks;
        this.range = range;
        this.needsTarget = needsTarget;
        this.sound = sound;
    }

    @Override
    public boolean cast(SpellContext context) {
        Optional<LivingEntity> target = SpellTargeting.aimedCreature(context, range);
        if (target.isEmpty() && needsTarget) {
            return false;
        }
        Vec3 spot = target.map(LivingEntity::position).orElseGet(() ->
                Vec3.atBottomCenterOf(SpellTargeting.groundBelow(context, SpellTargeting.aimedPoint(context, range))));
        @Nullable LivingEntity aimed = target.orElse(null);
        SpellZone zone = SpellZone.create(context.level(), context.caster(), behavior.apply(aimed), ZoneVisual.NONE,
                radius * context.scale(), context.power(), durationTicks);
        zone.setPos(spot);
        context.level().addFreshEntity(zone);
        context.level().playSound(null, zone.blockPosition(), sound, SoundSource.PLAYERS, 1.5F, 1.0F);
        return true;
    }
}
