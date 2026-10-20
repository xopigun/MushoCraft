package fr.xopi.mushocraft.magic.spell;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.core.MushoArmorMaterials;
import fr.xopi.mushocraft.magic.entity.SpellProjectile;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.List;

/**
 * What counts as ice: a spell of ice or the bite of the cold. Snow Drake scales resist it (wiki): each piece of their
 * armour takes off a share of it, and any piece keeps the wearer from freezing, as leather does
 * (freeze_immune_wearables tag).
 */
@EventBusSubscriber(modid = MushoCraft.MODID)
public final class IceDamage {
    /** Share of ice damage each piece of Snow Drake armour takes off. */
    private static final float PER_PIECE = 0.15F;
    private static final List<EquipmentSlot> ARMOUR = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
            EquipmentSlot.FEET);

    private IceDamage() {
    }

    public static boolean isIce(DamageSource source) {
        return source.is(DamageTypeTags.IS_FREEZING)
                || source.getDirectEntity() instanceof SpellProjectile projectile && projectile.element() == SpellElement.ICE;
    }

    public static int drakeScalePieces(LivingEntity wearer) {
        return (int) ARMOUR.stream()
                .filter(slot -> wearer.getItemBySlot(slot).getItem() instanceof ArmorItem armour
                        && armour.getMaterial().is(MushoArmorMaterials.SNOW_DRAKE))
                .count();
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (isIce(event.getSource())) {
            int pieces = drakeScalePieces(event.getEntity());
            if (pieces > 0) {
                event.setAmount(event.getAmount() * (1.0F - PER_PIECE * pieces));
            }
        }
    }
}
