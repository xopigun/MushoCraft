package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.magic.entity.SpellProjectile;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * Conjures a mass above the aimed spot and lets it fall on whoever stands there (Earth Pillar: a boulder).
 */
public final class FallingProjectileEffect implements SpellEffect {
    private final ProjectileProfile profile;
    private final double height;
    private final double range;
    private final SoundEvent sound;

    public FallingProjectileEffect(ProjectileProfile profile, double height, double range, SoundEvent sound) {
        this.profile = profile;
        this.height = height;
        this.range = range;
        this.sound = sound;
    }

    @Override
    public boolean cast(SpellContext context) {
        Vec3 target = SpellTargeting.aimedPoint(context, range);
        SpellProjectile boulder = SpellProjectile.create(context.level(), context.caster(), profile.scaled(context.power()).enlarged(context.scale()));
        boulder.setPos(target.x, target.y + height, target.z);
        boulder.setDeltaMovement(0.0, -profile.speed(), 0.0);
        context.level().addFreshEntity(boulder);
        context.level().playSound(null, boulder.blockPosition(), sound, SoundSource.PLAYERS, 1.0F, 0.8F);
        return true;
    }
}
