package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.magic.entity.SpellProjectile;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * A barrage at one target, e.g. the ice bolts of Icicle Break: the projectiles take shape in an arc above and around
 * the caster's head, then all fly at the aimed point, a little scattered.
 */
public final class VolleyEffect implements SpellEffect {
    /** How far the arc of projectiles spreads to each side, and how high it stands over the eyes. */
    private static final double ARC_WIDTH = 1.4;
    private static final double ARC_HEIGHT = 0.6;
    private static final double RANGE = 32.0;
    private static final float SCATTER = 3.0F;

    private final ProjectileProfile profile;
    private final int count;
    private final SoundEvent sound;

    public VolleyEffect(ProjectileProfile profile, int count, SoundEvent sound) {
        this.profile = profile;
        this.count = count;
        this.sound = sound;
    }

    @Override
    public boolean cast(SpellContext context) {
        Vec3 look = context.look();
        Vec3 target = SpellTargeting.aimedPoint(context, RANGE);
        Vec3 side = look.cross(new Vec3(0.0, 1.0, 0.0));
        side = side.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : side.normalize();
        ProjectileProfile scaled = profile.scaled(context.power()).enlarged(context.scale());
        for (int i = 0; i < count; i++) {
            // spread from one side to the other, highest in the middle
            double across = count == 1 ? 0.0 : (double) i / (count - 1) * 2.0 - 1.0;
            Vec3 start = context.eyes().add(look.scale(0.8)).add(side.scale(across * ARC_WIDTH))
                    .add(0.0, ARC_HEIGHT * (1.0 - across * across), 0.0);
            Vec3 direction = target.subtract(start).normalize();
            SpellProjectile bolt = SpellProjectile.create(context.level(), context.caster(), scaled);
            bolt.setPos(start);
            bolt.shoot(direction.x, direction.y, direction.z, profile.speed(), SCATTER);
            context.level().addFreshEntity(bolt);
        }
        context.level().playSound(null, context.caster().blockPosition(), sound, SoundSource.PLAYERS, 1.0F, 1.2F);
        return true;
    }
}
