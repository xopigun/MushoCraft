package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.core.MushoDamageTypes;
import fr.xopi.mushocraft.magic.entity.SpellBeam;
import fr.xopi.mushocraft.magic.fx.SpellFx;
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
 * until it meets a solid block. It is seen as a twisting {@link SpellBeam}, with rings of torn air along it.
 */
public final class PiercingRayEffect implements SpellEffect {
    private static final int SHEATH_COLOUR = 0xA8F0D8;
    private static final int CORE_COLOUR = 0xF4FFF8;
    private static final int BEAM_TICKS = 12;

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
        Vec3 from = start.add(context.look().scale(0.8)).add(0.0, -0.2, 0.0);
        level.addFreshEntity(SpellBeam.create(level, from, end, (float) (width * context.scale()), SHEATH_COLOUR, CORE_COLOUR,
                BEAM_TICKS, true));
        SpellFx.shockTunnel(level, from, context.look(), length, 2.5, (float) (width * 2.2), SHEATH_COLOUR);
        for (double step = 1.0; step < length; step += 1.0) {
            Vec3 point = start.add(context.look().scale(step));
            level.sendParticles(particle, point.x, point.y, point.z, 1, width * 0.3, width * 0.3, width * 0.3, 0.1);
        }
        level.playSound(null, context.caster().blockPosition(), sound, SoundSource.PLAYERS, 1.0F, 1.4F);
        return true;
    }
}
