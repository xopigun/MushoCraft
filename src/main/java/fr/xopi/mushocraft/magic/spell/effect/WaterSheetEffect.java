package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.magic.entity.WaterSheet;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/**
 * The living water of Water Shield, set where its wall stands (same place as the {@link WallEffect} it goes with):
 * see {@link WaterSheet}.
 */
public final class WaterSheetEffect implements SpellEffect {
    private final int halfWidth;
    private final int height;
    private final int distance;
    private final int durationTicks;

    public WaterSheetEffect(int halfWidth, int height, int distance, int durationTicks) {
        this.halfWidth = halfWidth;
        this.height = height;
        this.distance = distance;
        this.durationTicks = durationTicks;
    }

    @Override
    public boolean cast(SpellContext context) {
        Direction facing = context.caster().getDirection();
        BlockPos center = context.caster().blockPosition().relative(facing, distance);
        Vec3 base = Vec3.atBottomCenterOf(center);
        int lifetime = (int) Math.round(durationTicks * context.power());
        context.level().addFreshEntity(WaterSheet.create(context.level(), base, facing.toYRot(),
                (float) ((halfWidth * 2 + 1) * context.scale()), (float) (height * context.scale()), lifetime));
        return true;
    }
}
