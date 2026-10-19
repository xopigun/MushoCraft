package fr.xopi.mushocraft.magic.entity;

import fr.xopi.mushocraft.core.MushoParticles;
import fr.xopi.mushocraft.magic.fx.CameraShake;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * The explosions of spells: Minecraft's explosion does the harm and the push (never breaking blocks), and MushoCraft
 * draws it: its own flash in place of the vanilla puffs, and a {@link SpellFire} blast.
 */
public final class SpellBlasts {
    private SpellBlasts() {
    }

    public static void explode(ServerLevel level, @Nullable Entity source, DamageSource damage, Vec3 centre, float radius) {
        level.explode(source, damage, null, centre.x, centre.y, centre.z, radius, false, Level.ExplosionInteraction.NONE,
                MushoParticles.FLASH.get(), MushoParticles.FLASH.get(), SoundEvents.GENERIC_EXPLODE);
        level.addFreshEntity(SpellFire.blast(level, centre, radius));
        CameraShake.around(level, centre, 0.6F + radius * 0.35F, 10 + Math.round(radius * 4.0F), radius * 6.0 + 8.0);
    }
}
