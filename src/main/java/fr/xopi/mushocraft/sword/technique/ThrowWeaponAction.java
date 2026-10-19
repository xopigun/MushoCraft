package fr.xopi.mushocraft.sword.technique;

import fr.xopi.mushocraft.sword.entity.ThrownWeapon;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Flings the weapon in hand at high speed, blade whirling (North God Style, Sword Throwing Techniques: enough force
 * to cut through a Fireball). The weapon leaves the hand and lands where it hits.
 */
public final class ThrowWeaponAction implements TechniqueAction {
    private final float damageMultiplier;
    private final float speed;

    public ThrowWeaponAction(float damageMultiplier, float speed) {
        this.damageMultiplier = damageMultiplier;
        this.speed = speed;
    }

    /** Thrown at an enemy out of sword's reach. */
    @Override
    public double minRange() {
        return 5.0;
    }

    @Override
    public double maxRange() {
        return 16.0;
    }

    @Override
    public boolean perform(TechniqueContext context) {
        ItemStack weapon = context.user().getItemInHand(InteractionHand.MAIN_HAND);
        if (weapon.isEmpty()) {
            return false;
        }
        float damage = (float) (context.user().getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier * context.power());
        ThrownWeapon thrown = ThrownWeapon.create(context.level(), context.user(), weapon, damage);
        Vec3 look = context.look();
        thrown.setPos(context.user().getEyePosition().add(look.scale(0.8)).subtract(0.0, 0.3, 0.0));
        thrown.shoot(look.x, look.y, look.z, speed, 0.5F);
        context.level().addFreshEntity(thrown);
        context.user().setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        context.level().playSound(null, context.user().blockPosition(), SoundEvents.TRIDENT_THROW.value(),
                SoundSource.PLAYERS, 1.0F, 0.8F);
        return true;
    }
}
