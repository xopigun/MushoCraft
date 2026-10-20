package fr.xopi.mushocraft.compat.epicfight;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

import java.util.Set;

/**
 * Façade over Epic Fight's view of the weapon in hand: what kind of weapon it is and how it is held.
 */
public final class EpicFightWeapons {
    private static final Set<WeaponCategory> BLADES = Set.of(
            CapabilityItem.WeaponCategories.SWORD,
            CapabilityItem.WeaponCategories.LONGSWORD,
            CapabilityItem.WeaponCategories.TACHI,
            CapabilityItem.WeaponCategories.UCHIGATANA,
            CapabilityItem.WeaponCategories.GREATSWORD,
            CapabilityItem.WeaponCategories.DAGGER);

    private static final Set<WeaponCategory> NOT_WEAPONS = Set.of(
            CapabilityItem.WeaponCategories.NOT_WEAPON,
            CapabilityItem.WeaponCategories.FIST);

    private EpicFightWeapons() {
    }

    private static CapabilityItem mainHand(LivingEntity fighter) {
        return EpicFightCapabilities.getItemStackCapability(fighter.getItemInHand(InteractionHand.MAIN_HAND));
    }

    /** A sword of any kind: sword, longsword, tachi, uchigatana, greatsword or dagger. */
    public static boolean holdsBlade(LivingEntity fighter) {
        return BLADES.contains(mainHand(fighter).getWeaponCategory());
    }

    /** Any real weapon: the North God Style does not insist on swords. */
    public static boolean holdsWeapon(LivingEntity fighter) {
        return !NOT_WEAPONS.contains(mainHand(fighter).getWeaponCategory());
    }

    /**
     * Bare fists or a light blade: what glances off heavy plate. Heavy are the axes, greatswords, longswords, tachis,
     * spears and maces, by Epic Fight's category or, for items it does not class in a creature's hand, by Minecraft's tags.
     */
    public static boolean holdsLightWeapon(LivingEntity fighter) {
        ItemStack stack = fighter.getItemInHand(InteractionHand.MAIN_HAND);
        if (stack.isEmpty()) {
            return true;
        }
        boolean heavy = HEAVY.contains(mainHand(fighter).getWeaponCategory()) || stack.is(ItemTags.AXES)
                || stack.is(Items.MACE) || stack.is(Items.TRIDENT);
        return !heavy;
    }

    private static final Set<WeaponCategory> HEAVY = Set.of(CapabilityItem.WeaponCategories.AXE,
            CapabilityItem.WeaponCategories.GREATSWORD, CapabilityItem.WeaponCategories.LONGSWORD,
            CapabilityItem.WeaponCategories.TACHI, CapabilityItem.WeaponCategories.SPEAR, CapabilityItem.WeaponCategories.TRIDENT);

    /** A real weapon in each hand, as the North God's two-sword techniques need. */
    public static boolean holdsTwoWeapons(LivingEntity fighter) {
        CapabilityItem offhand = EpicFightCapabilities.getItemStackCapability(fighter.getItemInHand(InteractionHand.OFF_HAND));
        return holdsWeapon(fighter) && !NOT_WEAPONS.contains(offhand.getWeaponCategory());
    }

    /** Whether the weapon is held with both hands, as the Longsword of Light requires. */
    public static boolean holdsWithBothHands(LivingEntity fighter) {
        LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(fighter, LivingEntityPatch.class);
        if (patch == null) {
            return false;
        }
        CapabilityItem weapon = mainHand(fighter);
        Style style = weapon.getStyle(patch);
        return style == CapabilityItem.Styles.TWO_HAND
                || (style instanceof EpicFightSwordStyles && StyleMovesets.heldWithBothHands(weapon));
    }
}
