package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.core.MushoDamageTypes;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Creates an explosion where the caster aims (Explosion). Hurts and pushes creatures, never breaks blocks.
 */
public final class ExplosionEffect implements SpellEffect {
    private final float radius;
    private final double range;

    public ExplosionEffect(float radius, double range) {
        this.radius = radius;
        this.range = range;
    }

    @Override
    public boolean cast(SpellContext context) {
        Vec3 point = SpellTargeting.aimedPoint(context, range);
        context.level().explode(context.caster(), MushoDamageTypes.spell(context.level(), context.caster(), context.caster()),
                null, point.x, point.y, point.z, (float) (radius * Math.sqrt(context.power())), false,
                Level.ExplosionInteraction.NONE);
        return true;
    }
}
