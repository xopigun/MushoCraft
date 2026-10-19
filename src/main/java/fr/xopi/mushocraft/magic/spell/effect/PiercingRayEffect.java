package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.core.MushoDamageTypes;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A spinning drill that bores straight ahead (Tornado Impact): it goes through every creature on its line
 * until it meets a solid block.
 */
public final class PiercingRayEffect implements SpellEffect {
    private final double range;
    private final double width;
    private final float damage;
    private final ParticleOptions particle;
    private final SoundEvent sound;

    public PiercingRayEffect(double range, double width, float damage, ParticleOptions particle, SoundEvent sound) {
        this.range = range;
        this.width = width;
        this.damage = damage;
        this.particle = particle;
        this.sound = sound;
    }

    @Override
    public boolean cast(SpellContext context) {
        ServerLevel level = context.level();
        Vec3 start = context.eyes();
        Vec3 wanted = start.add(context.look().scale(range));
        HitResult wall = level.clip(new ClipContext(start, wanted, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                context.caster()));
        Vec3 end = wall.getType() == HitResult.Type.MISS ? wanted : wall.getLocation();

        AABB swept = new AABB(start, end).inflate(width);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, swept, entity -> entity != context.caster())) {
            if (target.getBoundingBox().inflate(width).clip(start, end).isPresent()) {
                target.hurt(MushoDamageTypes.spell(level, context.caster(), context.caster()), (float) (damage * context.power()));
            }
        }
        double length = end.distanceTo(start);
        for (double step = 0.5; step < length; step += 0.5) {
            Vec3 point = start.add(context.look().scale(step));
            level.sendParticles(particle, point.x, point.y, point.z, 3, width * 0.3, width * 0.3, width * 0.3, 0.1);
        }
        level.playSound(null, context.caster().blockPosition(), sound, SoundSource.PLAYERS, 1.0F, 1.4F);
        return true;
    }
}
