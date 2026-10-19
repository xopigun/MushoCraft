package fr.xopi.mushocraft.sword.technique;

import fr.xopi.mushocraft.core.Rank;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * One use of a technique, by a player or a creature.
 *
 * @param styleRank the user's rank in the technique's style
 * @param power     multiplier on the technique's strength: rank above the technique's own, mastery
 * @param look      where the technique is aimed: where a player looks, or towards a creature's target
 */
public record TechniqueContext(LivingEntity user, Technique technique, Rank styleRank, double power, Vec3 look) {
    public TechniqueContext(LivingEntity user, Technique technique, Rank styleRank, double power) {
        this(user, technique, styleRank, power, user.getLookAngle());
    }

    public ServerLevel level() {
        return (ServerLevel) user.level();
    }

    /** Horizontal direction of the technique. */
    public Vec3 heading() {
        Vec3 flat = look.multiply(1.0, 0.0, 1.0);
        return flat.lengthSqr() < 1.0E-6 ? user.getLookAngle().multiply(1.0, 0.0, 1.0).normalize() : flat.normalize();
    }

    /** A melee blow from the user, whoever they are. */
    public DamageSource meleeSource() {
        return user instanceof Player player ? level().damageSources().playerAttack(player) : level().damageSources().mobAttack(user);
    }
}
