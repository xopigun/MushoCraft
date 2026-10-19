package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.magic.entity.SpellProjectile;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * Projectiles flung in every direction around the caster, e.g. the ice shards of Blizzard Storm.
 * They leave in a horizontal ring, slightly fanned upward and downward, starting from chest height.
 */
public final class BurstEffect implements SpellEffect {
    private static final double START_DISTANCE = 1.0;
    private static final double FAN = 0.15;

    private final ProjectileProfile profile;
    private final int count;
    private final SoundEvent sound;

    public BurstEffect(ProjectileProfile profile, int count, SoundEvent sound) {
        this.profile = profile;
        this.count = count;
        this.sound = sound;
    }

    @Override
    public boolean cast(SpellContext context) {
        Vec3 chest = context.caster().position().add(0.0, context.caster().getBbHeight() * 0.6, 0.0);
        ProjectileProfile scaled = profile.scaled(context.power()).enlarged(context.scale());
        for (int i = 0; i < count; i++) {
            double angle = 2.0 * Math.PI * i / count;
            Vec3 direction = new Vec3(Math.cos(angle), (i % 3 - 1) * FAN, Math.sin(angle)).normalize();
            SpellProjectile shard = SpellProjectile.create(context.level(), context.caster(), scaled);
            shard.setPos(chest.add(direction.scale(START_DISTANCE)));
            shard.shoot(direction.x, direction.y, direction.z, profile.speed(), 1.0F);
            context.level().addFreshEntity(shard);
        }
        context.level().playSound(null, context.caster().blockPosition(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }
}
