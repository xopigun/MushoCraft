package fr.xopi.mushocraft.magic.spell.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.xopi.mushocraft.magic.spell.SpellElement;

/**
 * Parameters of a conjured projectile. Saved with the entity, so a projectile in flight survives a chunk reload.
 *
 * @param speed          blocks per tick at launch
 * @param gravity        downward acceleration per tick
 * @param size           visual diameter in blocks
 * @param knockback      horizontal push on hit
 * @param igniteSeconds  seconds the target burns (fire)
 * @param slowTicks      ticks of slowness on hit (ice)
 * @param undeadMultiplier damage multiplier against undead (divine strike)
 * @param explosionRadius  radius of the blast on impact, 0 for none; never breaks blocks
 * @param bindTicks        ticks the creature hit is held fast (Wind Bind), 0 for none
 * @param blindRadius      radius of the blinding dust burst on impact (Sand Cloud), 0 for none
 * @param shape            the form the client draws
 */
public record ProjectileProfile(
        SpellElement element,
        float damage,
        float speed,
        float gravity,
        float size,
        float knockback,
        int igniteSeconds,
        int slowTicks,
        float undeadMultiplier,
        float explosionRadius,
        int bindTicks,
        float blindRadius,
        ProjectileShape shape
) {
    public static final Codec<ProjectileProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SpellElement.CODEC.fieldOf("element").forGetter(ProjectileProfile::element),
            Codec.FLOAT.fieldOf("damage").forGetter(ProjectileProfile::damage),
            Codec.FLOAT.fieldOf("speed").forGetter(ProjectileProfile::speed),
            Codec.FLOAT.fieldOf("gravity").forGetter(ProjectileProfile::gravity),
            Codec.FLOAT.fieldOf("size").forGetter(ProjectileProfile::size),
            Codec.FLOAT.fieldOf("knockback").forGetter(ProjectileProfile::knockback),
            Codec.INT.fieldOf("ignite_seconds").forGetter(ProjectileProfile::igniteSeconds),
            Codec.INT.fieldOf("slow_ticks").forGetter(ProjectileProfile::slowTicks),
            Codec.FLOAT.fieldOf("undead_multiplier").forGetter(ProjectileProfile::undeadMultiplier),
            Codec.FLOAT.optionalFieldOf("explosion_radius", 0.0F).forGetter(ProjectileProfile::explosionRadius),
            Codec.INT.optionalFieldOf("bind_ticks", 0).forGetter(ProjectileProfile::bindTicks),
            Codec.FLOAT.optionalFieldOf("blind_radius", 0.0F).forGetter(ProjectileProfile::blindRadius),
            ProjectileShape.CODEC.optionalFieldOf("shape", ProjectileShape.ORB).forGetter(ProjectileProfile::shape)
    ).apply(instance, ProjectileProfile::new));

    public static Builder of(SpellElement element, float damage) {
        return new Builder(element, damage);
    }

    public ProjectileProfile scaled(double power) {
        return new ProjectileProfile(element, (float) (damage * power), speed, gravity, size, knockback,
                igniteSeconds, slowTicks, undeadMultiplier, explosionRadius, bindTicks, blindRadius, shape);
    }

    /** The same projectile, larger (and with a wider blast) by this factor. */
    public ProjectileProfile enlarged(double factor) {
        return new ProjectileProfile(element, damage, speed, gravity, (float) (size * factor), knockback,
                igniteSeconds, slowTicks, undeadMultiplier, (float) (explosionRadius * factor), bindTicks,
                (float) (blindRadius * factor), shape);
    }

    public static final class Builder {
        private final SpellElement element;
        private final float damage;
        private float speed = 1.5F;
        private float gravity = 0.0F;
        private float size = 0.5F;
        private float knockback = 0.0F;
        private int igniteSeconds;
        private int slowTicks;
        private float undeadMultiplier = 1.0F;
        private float explosionRadius;
        private int bindTicks;
        private float blindRadius;
        private ProjectileShape shape = ProjectileShape.ORB;

        private Builder(SpellElement element, float damage) {
            this.element = element;
            this.damage = damage;
        }

        public Builder speed(float value) {
            this.speed = value;
            return this;
        }

        public Builder gravity(float value) {
            this.gravity = value;
            return this;
        }

        public Builder size(float value) {
            this.size = value;
            return this;
        }

        public Builder knockback(float value) {
            this.knockback = value;
            return this;
        }

        public Builder ignite(int seconds) {
            this.igniteSeconds = seconds;
            return this;
        }

        public Builder slow(int ticks) {
            this.slowTicks = ticks;
            return this;
        }

        public Builder undeadMultiplier(float value) {
            this.undeadMultiplier = value;
            return this;
        }

        public Builder explode(float radius) {
            this.explosionRadius = radius;
            return this;
        }

        public Builder bind(int ticks) {
            this.bindTicks = ticks;
            return this;
        }

        public Builder blindingDust(float radius) {
            this.blindRadius = radius;
            return this;
        }

        public Builder shape(ProjectileShape value) {
            this.shape = value;
            return this;
        }

        public ProjectileProfile build() {
            return new ProjectileProfile(element, damage, speed, gravity, size, knockback, igniteSeconds, slowTicks,
                    undeadMultiplier, explosionRadius, bindTicks, blindRadius, shape);
        }
    }
}
