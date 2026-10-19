package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.magic.entity.PillarKind;
import fr.xopi.mushocraft.magic.entity.SpellFire;
import fr.xopi.mushocraft.magic.entity.SpellPillar;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/**
 * How an eruption is seen (Strategy pattern): what rises from the ground where the spell strikes.
 */
@FunctionalInterface
public interface EruptionVisual {
    /**
     * @param height   how tall the eruption is, in blocks (already enlarged by the caster's focus)
     * @param lifetime how long its solid column stands, in ticks
     */
    void raise(ServerLevel level, Vec3 base, float height, int lifetime, RandomSource random);

    /** Hidden blocks are the column's solidity; this look stands in for them. */
    default boolean hidesColumn() {
        return true;
    }

    /**
     * A Blockbench pillar (Ice Pillar, Earth Lance, Earth Hedgehog).
     *
     * @param maxTilt how far, in degrees, it may lean from upright (spikes of a hedgehog lean every way)
     */
    static EruptionVisual pillar(PillarKind kind, float maxTilt) {
        return (level, base, height, lifetime, random) -> level.addFreshEntity(SpellPillar.create(level, kind, base, height,
                lifetime, random.nextFloat() * 360.0F, maxTilt * random.nextFloat()));
    }

    /** A pillar of flame (Flame Pillar): it roars a moment and dies down, whatever the column would last. */
    static EruptionVisual fireColumn(float radius, int burnTicks) {
        return (level, base, height, lifetime, random) -> level.addFreshEntity(SpellFire.column(level, base, radius, height, burnTicks));
    }
}
