package fr.xopi.mushocraft.compat.epicfight;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.sword.ActiveStyles;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.ex_cap.data.Moveset;
import yesman.epicfight.api.ex_cap.data.modifier.WeaponModifier;
import yesman.epicfight.api.ex_cap.provider.ProviderConditional;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.registry.deferred.ModifierRegister;
import yesman.epicfight.registry.deferred.holders.DeferredMoveset;
import yesman.epicfight.registry.deferred.holders.DeferredWeapon;
import yesman.epicfight.registry.entries.EpicFightItemCapabilityPresets;
import yesman.epicfight.registry.entries.EpicFightMovesets;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

/**
 * Basic attacks by sword style: on a weapon held alone, a player fighting in a MushoCraft style gets that style's
 * combo instead of the weapon's default one. Only Epic Fight's own animations are used, by reference. Guard, innate
 * skill and stances still come from the weapon (the style's moveset is a child of the weapon's).
 * <ul>
 *   <li>Sword God: speed and offence, a long chain of quick cuts.</li>
 *   <li>Water God: few, measured cuts; its strength is the counter (technique <i>Flow</i>).</li>
 *   <li>North God: unorthodox, mixing the cuts of several weapons.</li>
 * </ul>
 * Swords of every kind (sword, tachi, longsword, uchigatana, greatsword, dagger) take all three styles. Axes and
 * spears take only the North God Style, whose branches fight with axes, staves and other weapons rather than a sword
 * alone (wiki, page <i>North God Style</i>); the other two styles are styles of the sword, and leave them their own
 * moveset.
 * Dual wielding and sword-and-shield keep Epic Fight's own movesets: a style only applies with a free off hand.
 */
public final class StyleMovesets {
    public static final ModifierRegister REGISTRY = ModifierRegister.create(MushoCraft.MODID);

    /** Combo cuts, then the dash attack, then the air attack (Epic Fight's moveset layout). */
    private record Combo(List<AnimationAccessor<? extends AttackAnimation>> cuts,
                         AnimationAccessor<? extends AttackAnimation> dash,
                         AnimationAccessor<? extends AttackAnimation> air) {
        @SuppressWarnings("unchecked")
        Moveset.Builder over(DeferredMoveset weaponMoveset) {
            AnimationAccessor<? extends AttackAnimation>[] attacks = new AnimationAccessor[cuts.size() + 2];
            for (int i = 0; i < cuts.size(); i++) {
                attacks[i] = cuts.get(i);
            }
            attacks[cuts.size()] = dash;
            attacks[cuts.size() + 1] = air;
            return Moveset.builder().parent(weaponMoveset).addComboAttacks(attacks);
        }
    }

    /** The styles' combos on one weapon type; a style without a combo leaves the weapon its own moveset. */
    private record WeaponCombos(DeferredWeapon weapon, WeaponCategory category, DeferredMoveset weaponMoveset,
                                @Nullable Combo swordGod, @Nullable Combo waterGod, Combo northGod) {
        /** A weapon only the North God Style fights with. */
        static WeaponCombos northGodOnly(DeferredWeapon weapon, WeaponCategory category, DeferredMoveset weaponMoveset, Combo northGod) {
            return new WeaponCombos(weapon, category, weaponMoveset, null, null, northGod);
        }

        Optional<Combo> of(EpicFightSwordStyles style) {
            return Optional.ofNullable(switch (style) {
                case MUSHO_SWORD_GOD -> swordGod;
                case MUSHO_WATER_GOD -> waterGod;
                case MUSHO_NORTH_GOD -> northGod;
            });
        }

        WeaponModifier.Builder modifier() {
            WeaponModifier.Builder modifier = WeaponModifier.builder().target(weapon);
            for (EpicFightSwordStyles style : EpicFightSwordStyles.values()) {
                of(style).ifPresent(combo -> {
                    modifier.addConditionalModifier(ProviderConditional.createCustom(style, patch -> fightsIn(patch, style),
                            !isTwoHanded(category)));
                    modifier.addMovesetModifier(style, combo.over(weaponMoveset));
                });
            }
            return modifier;
        }
    }

    // Built on demand: Epic Fight's animation constants are only set once its animations are registered.
    private static WeaponCombos sword() {
        return new WeaponCombos(EpicFightItemCapabilityPresets.SWORD, CapabilityItem.WeaponCategories.SWORD, EpicFightMovesets.SWORD_1H,
                new Combo(List.of(Animations.UCHIGATANA_AUTO1, Animations.UCHIGATANA_AUTO2, Animations.UCHIGATANA_AUTO3, Animations.SWORD_AUTO3),
                        Animations.UCHIGATANA_DASH, Animations.SWORD_AIR_SLASH),
                new Combo(List.of(Animations.SWORD_AUTO1, Animations.SWORD_AUTO2),
                        Animations.SWORD_DASH, Animations.SWORD_AIR_SLASH),
                new Combo(List.of(Animations.SWORD_AUTO1, Animations.DAGGER_AUTO3, Animations.LONGSWORD_AUTO2, Animations.SWORD_AUTO3),
                        Animations.DAGGER_DASH, Animations.SWORD_AIR_SLASH));
    }

    private static WeaponCombos tachi() {
        return new WeaponCombos(EpicFightItemCapabilityPresets.TACHI, CapabilityItem.WeaponCategories.TACHI, EpicFightMovesets.TACHI_2H,
                new Combo(List.of(Animations.UCHIGATANA_AUTO1, Animations.UCHIGATANA_AUTO2, Animations.UCHIGATANA_AUTO3, Animations.TACHI_AUTO3),
                        Animations.UCHIGATANA_DASH, Animations.LONGSWORD_AIR_SLASH),
                new Combo(List.of(Animations.TACHI_AUTO1, Animations.TACHI_AUTO2),
                        Animations.TACHI_DASH, Animations.LONGSWORD_AIR_SLASH),
                new Combo(List.of(Animations.TACHI_AUTO1, Animations.LONGSWORD_AUTO2, Animations.SWORD_AUTO3, Animations.TACHI_AUTO3),
                        Animations.LONGSWORD_DASH, Animations.LONGSWORD_AIR_SLASH));
    }

    private static WeaponCombos longsword() {
        return new WeaponCombos(EpicFightItemCapabilityPresets.LONGSWORD, CapabilityItem.WeaponCategories.LONGSWORD,
                EpicFightMovesets.LONGSWORD_2H,
                new Combo(List.of(Animations.UCHIGATANA_AUTO1, Animations.UCHIGATANA_AUTO2, Animations.LONGSWORD_AUTO2,
                        Animations.UCHIGATANA_AUTO3, Animations.LONGSWORD_AUTO3), Animations.UCHIGATANA_DASH, Animations.LONGSWORD_AIR_SLASH),
                new Combo(List.of(Animations.LONGSWORD_AUTO1, Animations.LONGSWORD_AUTO2),
                        Animations.LONGSWORD_DASH, Animations.LONGSWORD_AIR_SLASH),
                new Combo(List.of(Animations.LONGSWORD_AUTO1, Animations.DAGGER_AUTO3, Animations.TACHI_AUTO2, Animations.LONGSWORD_AUTO3),
                        Animations.DAGGER_DASH, Animations.LONGSWORD_AIR_SLASH));
    }

    private static WeaponCombos uchigatana() {
        return new WeaponCombos(EpicFightItemCapabilityPresets.UCHIGATANA, CapabilityItem.WeaponCategories.UCHIGATANA,
                EpicFightMovesets.UCHIGATANA_BASE,
                new Combo(List.of(Animations.UCHIGATANA_AUTO1, Animations.UCHIGATANA_AUTO2, Animations.TACHI_AUTO2,
                        Animations.UCHIGATANA_AUTO3), Animations.UCHIGATANA_DASH, Animations.UCHIGATANA_AIR_SLASH),
                new Combo(List.of(Animations.UCHIGATANA_AUTO1, Animations.UCHIGATANA_AUTO3),
                        Animations.UCHIGATANA_DASH, Animations.UCHIGATANA_AIR_SLASH),
                new Combo(List.of(Animations.UCHIGATANA_AUTO1, Animations.DAGGER_AUTO2, Animations.LONGSWORD_AUTO2,
                        Animations.UCHIGATANA_AUTO3), Animations.DAGGER_DASH, Animations.UCHIGATANA_AIR_SLASH));
    }

    private static WeaponCombos greatsword() {
        return new WeaponCombos(EpicFightItemCapabilityPresets.GREATSWORD, CapabilityItem.WeaponCategories.GREATSWORD,
                EpicFightMovesets.GREATSWORD_2H,
                new Combo(List.of(Animations.GREATSWORD_AUTO1, Animations.LONGSWORD_AUTO2, Animations.GREATSWORD_AUTO2),
                        Animations.GREATSWORD_DASH, Animations.GREATSWORD_AIR_SLASH),
                new Combo(List.of(Animations.GREATSWORD_AUTO1),
                        Animations.GREATSWORD_DASH, Animations.GREATSWORD_AIR_SLASH),
                new Combo(List.of(Animations.GREATSWORD_AUTO1, Animations.AXE_AUTO2, Animations.GREATSWORD_AUTO2),
                        Animations.AXE_DASH, Animations.GREATSWORD_AIR_SLASH));
    }

    private static WeaponCombos dagger() {
        return new WeaponCombos(EpicFightItemCapabilityPresets.DAGGER, CapabilityItem.WeaponCategories.DAGGER,
                EpicFightMovesets.DAGGER_1H,
                new Combo(List.of(Animations.DAGGER_AUTO1, Animations.DAGGER_AUTO2, Animations.SWORD_AUTO2, Animations.DAGGER_AUTO3),
                        Animations.DAGGER_DASH, Animations.DAGGER_AIR_SLASH),
                new Combo(List.of(Animations.DAGGER_AUTO1, Animations.DAGGER_AUTO3),
                        Animations.DAGGER_DASH, Animations.DAGGER_AIR_SLASH),
                new Combo(List.of(Animations.DAGGER_AUTO1, Animations.AXE_AUTO1, Animations.DAGGER_AUTO3, Animations.SWORD_AUTO3),
                        Animations.DAGGER_DASH, Animations.DAGGER_AIR_SLASH));
    }

    private static WeaponCombos axe() {
        return WeaponCombos.northGodOnly(EpicFightItemCapabilityPresets.AXE, CapabilityItem.WeaponCategories.AXE, EpicFightMovesets.AXE_1H,
                new Combo(List.of(Animations.AXE_AUTO1, Animations.SWORD_AUTO2, Animations.AXE_AUTO2, Animations.DAGGER_AUTO3),
                        Animations.AXE_DASH, Animations.AXE_AIRSLASH));
    }

    private static WeaponCombos spear() {
        return WeaponCombos.northGodOnly(EpicFightItemCapabilityPresets.SPEAR, CapabilityItem.WeaponCategories.SPEAR,
                EpicFightMovesets.SPEAR_2H,
                new Combo(List.of(Animations.SPEAR_TWOHAND_AUTO1, Animations.SPEAR_TWOHAND_AUTO2, Animations.LONGSWORD_AUTO2,
                        Animations.SPEAR_TWOHAND_AUTO1), Animations.SPEAR_DASH, Animations.SPEAR_TWOHAND_AIR_SLASH));
    }

    static {
        REGISTRY.registerModifier("sword_styles", () -> sword().modifier());
        REGISTRY.registerModifier("tachi_styles", () -> tachi().modifier());
        REGISTRY.registerModifier("longsword_styles", () -> longsword().modifier());
        REGISTRY.registerModifier("uchigatana_styles", () -> uchigatana().modifier());
        REGISTRY.registerModifier("greatsword_styles", () -> greatsword().modifier());
        REGISTRY.registerModifier("dagger_styles", () -> dagger().modifier());
        REGISTRY.registerModifier("axe_styles", () -> axe().modifier());
        REGISTRY.registerModifier("spear_styles", () -> spear().modifier());
    }

    private StyleMovesets() {
    }

    /** From the mod constructor: Epic Fight loads extensible enums and modifiers right after construction. */
    public static void register(IEventBus modEventBus) {
        Style.ENUM_MANAGER.registerEnumCls(MushoCraft.MODID, EpicFightSwordStyles.class);
        REGISTRY.register(modEventBus);
    }

    /**
     * Whether a weapon restyled here is wielded with both hands. Needed because a MushoCraft style hides
     * Epic Fight's own wield style (TWO_HAND) on the weapons it applies to.
     */
    public static boolean heldWithBothHands(CapabilityItem weapon) {
        return isTwoHanded(weapon.getWeaponCategory());
    }

    /** The weapons Epic Fight wields with both hands by default, and so does a style. */
    private static final Set<WeaponCategory> TWO_HANDED = Set.of(CapabilityItem.WeaponCategories.TACHI,
            CapabilityItem.WeaponCategories.LONGSWORD, CapabilityItem.WeaponCategories.UCHIGATANA,
            CapabilityItem.WeaponCategories.GREATSWORD, CapabilityItem.WeaponCategories.SPEAR);

    private static boolean isTwoHanded(WeaponCategory category) {
        return TWO_HANDED.contains(category);
    }

    /** A player fighting in this style, weapon alone in hand. */
    private static boolean fightsIn(LivingEntityPatch<?> patch, EpicFightSwordStyles style) {
        return patch.getOriginal() instanceof Player player
                && player.getOffhandItem().isEmpty()
                && ActiveStyles.of(player).filter(style.style()::equals).isPresent();
    }
}
