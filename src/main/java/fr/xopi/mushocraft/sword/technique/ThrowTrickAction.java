package fr.xopi.mushocraft.sword.technique;

import fr.xopi.mushocraft.sword.entity.ThrownTrick;
import fr.xopi.mushocraft.sword.entity.TrickKind;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * Throws one of the North God Style's tricks at the foe (Teardrop Bullet, Crimson Ink): no weapon needed, nothing used
 * up; the trick bursts where it lands.
 */
public final class ThrowTrickAction implements TechniqueAction {
    private final TrickKind kind;
    private final float speed;

    public ThrowTrickAction(TrickKind kind, float speed) {
        this.kind = kind;
        this.speed = speed;
    }

    @Override
    public double minRange() {
        return 2.0;
    }

    @Override
    public double maxRange() {
        return 14.0;
    }

    @Override
    public boolean perform(TechniqueContext context) {
        ThrownTrick trick = ThrownTrick.create(context.level(), context.user(), kind);
        Vec3 look = context.look();
        trick.shoot(look.x, look.y + 0.1, look.z, speed, 1.0F);
        context.level().addFreshEntity(trick);
        context.level().playSound(null, context.user().blockPosition(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 1.0F, 0.6F);
        return true;
    }
}
