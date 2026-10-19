package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.magic.block.ConjuredBlock;
import fr.xopi.mushocraft.magic.entity.PillarKind;
import fr.xopi.mushocraft.magic.entity.SpellPillar;
import fr.xopi.mushocraft.magic.fx.CameraShake;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * A shelter raised around the caster (Earth Fortress): a ring of walls and a roof, with the caster inside.
 * In the novel it crumbles unless fed mana; here it lasts a set time, longer for a stronger caster.
 */
public final class FortressEffect implements SpellEffect {
    private final Supplier<? extends Block> block;
    private final int radius;
    private final int height;
    private final int durationTicks;
    private final SoundEvent sound;
    @Nullable
    private final PillarKind look;

    public FortressEffect(Supplier<? extends Block> block, int radius, int height, int durationTicks, SoundEvent sound) {
        this(block, radius, height, durationTicks, sound, null);
    }

    private FortressEffect(Supplier<? extends Block> block, int radius, int height, int durationTicks, SoundEvent sound,
                           @Nullable PillarKind look) {
        this.look = look;
        this.block = block;
        this.radius = radius;
        this.height = height;
        this.durationTicks = durationTicks;
        this.sound = sound;
    }

    /** The same shelter, its blocks hidden behind a model (Earth Fortress: walls and a roof of stone slabs). */
    public FortressEffect lookingLike(PillarKind model) {
        return new FortressEffect(block, radius, height, durationTicks, sound, model);
    }

    @Override
    public boolean cast(SpellContext context) {
        ServerLevel level = context.level();
        BlockPos center = context.caster().blockPosition();
        int duration = (int) Math.round(durationTicks * context.power());
        int placed = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                boolean wall = Math.max(Math.abs(dx), Math.abs(dz)) == radius;
                for (int up = 0; up <= height; up++) {
                    boolean roof = up == height;
                    if ((wall && !roof) || (roof && Math.abs(dx) + Math.abs(dz) < radius * 2)) {
                        placed += raise(level, center.offset(dx, up, dz), duration);
                    }
                }
            }
        }
        if (placed > 0) {
            level.playSound(null, center, sound, SoundSource.PLAYERS, 1.0F, 0.8F);
            if (look != null) {
                level.addFreshEntity(SpellPillar.create(level, look, Vec3.atBottomCenterOf(center), look.modelHeight(), duration,
                        0.0F, 0.0F));
                CameraShake.around(level, Vec3.atCenterOf(center), 0.7F, 10, 14.0);
            }
        }
        return placed > 0;
    }

    private int raise(ServerLevel level, BlockPos pos, int duration) {
        if (!level.getBlockState(pos).canBeReplaced()) {
            return 0;
        }
        level.setBlockAndUpdate(pos, look != null && block.get() instanceof ConjuredBlock conjured
                ? conjured.hiddenState() : block.get().defaultBlockState());
        level.scheduleTick(pos, block.get(), duration);
        return 1;
    }
}
