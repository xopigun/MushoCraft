package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.magic.fx.CameraShake;
import fr.xopi.mushocraft.magic.fx.SpellFx;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Many eruptions at once, scattered around the aimed spot: the earthen spikes of Earth Hedgehog.
 * The first one always rises at the spot itself.
 */
public final class ScatteredEruptionEffect implements SpellEffect {
    private final EruptionEffect eruption;
    private final int count;
    private final double spread;

    public ScatteredEruptionEffect(EruptionEffect eruption, int count, double spread) {
        this.eruption = eruption;
        this.count = count;
        this.spread = spread;
    }

    @Override
    public boolean cast(SpellContext context) {
        Vec3 aimed = SpellTargeting.aimedPoint(context, eruption.range());
        RandomSource random = context.caster().getRandom();
        for (int i = 0; i < count; i++) {
            Vec3 spot = i == 0 ? aimed : aimed.add((random.nextDouble() * 2.0 - 1.0) * spread, 1.0,
                    (random.nextDouble() * 2.0 - 1.0) * spread);
            eruption.eruptAt(context, SpellTargeting.groundBelow(context, spot));
        }
        CameraShake.around(context.level(), aimed, 1.0F, 14, 20.0);
        SpellFx.groundRing(context.level(), context.caster(), aimed, 4.5, 8,
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COARSE_DIRT.defaultBlockState()));
        return true;
    }
}
