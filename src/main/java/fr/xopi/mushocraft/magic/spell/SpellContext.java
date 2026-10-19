package fr.xopi.mushocraft.magic.spell;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Everything an effect needs to know about one cast. The caster may be a player or any creature that knows magic.
 *
 * @param power multiplier on the effect's strength (1.0 = as written; raised by proficiency, lowered by a lack of mastery)
 * @param look  direction the spell is aimed in: where a player looks, or towards a creature's target
 */
public record SpellContext(LivingEntity caster, Spell spell, double power, Vec3 look) {
    /** A cast aimed where the caster looks. */
    public SpellContext(LivingEntity caster, Spell spell, double power) {
        this(caster, spell, power, caster.getLookAngle());
    }

    /** A cast aimed at a point, as a creature aims at its target. */
    public static SpellContext aimedAt(LivingEntity caster, Spell spell, double power, Vec3 point) {
        Vec3 direction = point.subtract(caster.getEyePosition());
        return new SpellContext(caster, spell, power, direction.lengthSqr() < 1.0E-6 ? caster.getLookAngle() : direction.normalize());
    }

    public ServerLevel level() {
        return (ServerLevel) caster.level();
    }

    public Vec3 eyes() {
        return caster.getEyePosition();
    }

    public long gameTime() {
        return level().getServer().overworld().getGameTime();
    }
}
