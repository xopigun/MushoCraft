package fr.xopi.mushocraft.compat.epicfight;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
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

    private static CapabilityItem mainHand(ServerPlayer player) {
        return EpicFightCapabilities.getItemStackCapability(player.getItemInHand(InteractionHand.MAIN_HAND));
    }

    /** A sword of any kind: sword, longsword, tachi, uchigatana, greatsword or dagger. */
    public static boolean holdsBlade(ServerPlayer player) {
        return BLADES.contains(mainHand(player).getWeaponCategory());
    }

    /** Any real weapon: the North God Style does not insist on swords. */
    public static boolean holdsWeapon(ServerPlayer player) {
        return !NOT_WEAPONS.contains(mainHand(player).getWeaponCategory());
    }

    /** Whether the weapon is held with both hands, as the Longsword of Light requires. */
    public static boolean holdsWithBothHands(ServerPlayer player) {
        PlayerPatch<?> patch = EpicFightCapabilities.getPlayerPatch(player);
        return patch != null && mainHand(player).getStyle(patch) == CapabilityItem.Styles.TWO_HAND;
    }
}
