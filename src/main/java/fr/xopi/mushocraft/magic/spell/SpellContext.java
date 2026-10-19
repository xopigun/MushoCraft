package fr.xopi.mushocraft.magic.spell;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * Everything an effect needs to know about one cast.
 *
 * @param power multiplier on the effect's strength (1.0 = as written; later raised by mastery or silent casting)
 */
public record SpellContext(ServerPlayer caster, Spell spell, double power) {
    public ServerLevel level() {
        return caster.serverLevel();
    }

    public Vec3 look() {
        return caster.getLookAngle();
    }

    public Vec3 eyes() {
        return caster.getEyePosition();
    }
}
