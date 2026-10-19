package fr.xopi.mushocraft.sword.technique;

import fr.xopi.mushocraft.compat.epicfight.EpicFightWeapons;
import net.minecraft.world.entity.LivingEntity;

import java.util.Locale;
import java.util.function.Predicate;

/**
 * What a technique needs in hand.
 */
public enum WeaponRequirement {
    /** Any sword (sword, longsword, tachi, uchigatana, greatsword, dagger). */
    BLADE(EpicFightWeapons::holdsBlade),
    /** A sword held with both hands: the Longsword of Light demands it (wiki, Sword God Style). */
    TWO_HANDED_BLADE(player -> EpicFightWeapons.holdsBlade(player) && EpicFightWeapons.holdsWithBothHands(player)),
    /** Any weapon: « there is no need to stick to the sword » (Kalman II, North God Style). */
    ANY_WEAPON(EpicFightWeapons::holdsWeapon),
    /** A weapon in each hand (Hazy Cross). */
    TWO_WEAPONS(EpicFightWeapons::holdsTwoWeapons),
    /** Nothing at all: a thrown trick, a bare-handed counter, the aura itself. */
    NONE(fighter -> true);

    private final Predicate<LivingEntity> check;

    WeaponRequirement(Predicate<LivingEntity> check) {
        this.check = check;
    }

    public boolean isMetBy(LivingEntity fighter) {
        return check.test(fighter);
    }

    public String failureKey() {
        return "message.mushocraft.technique.weapon." + name().toLowerCase(Locale.ROOT);
    }
}
