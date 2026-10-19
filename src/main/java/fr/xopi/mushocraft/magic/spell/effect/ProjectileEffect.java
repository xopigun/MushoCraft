package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.magic.entity.SpellProjectile;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * Conjures matter in front of the caster and hurls it where they look (Water Ball, Fireball, Rock Bullet...).
 */
public final class ProjectileEffect implements SpellEffect {
    private final ProjectileProfile profile;
    private final SoundEvent sound;

    public ProjectileEffect(ProjectileProfile profile, SoundEvent sound) {
        this.profile = profile;
        this.sound = sound;
    }

    @Override
    public boolean cast(SpellContext context) {
        Vec3 look = context.look();
        Vec3 start = context.eyes().add(look.scale(0.6)).subtract(0.0, 0.2, 0.0);
        SpellProjectile projectile = SpellProjectile.create(context.level(), context.caster(), profile.scaled(context.power()));
        projectile.setPos(start);
        projectile.shoot(look.x, look.y, look.z, profile.speed(), 0.5F);
        context.level().addFreshEntity(projectile);
        context.level().playSound(null, context.caster().blockPosition(), sound, SoundSource.PLAYERS, 0.8F, 1.0F);
        return true;
    }
}
