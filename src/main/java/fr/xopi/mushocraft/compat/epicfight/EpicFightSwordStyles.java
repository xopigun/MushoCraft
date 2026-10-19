package fr.xopi.mushocraft.compat.epicfight;

import fr.xopi.mushocraft.sword.SwordStyle;
import yesman.epicfight.world.capabilities.item.Style;

/**
 * MushoCraft's sword styles as Epic Fight wield styles, so each one can have its own moveset.
 * Names are prefixed: Epic Fight's style names are global to every mod.
 */
public enum EpicFightSwordStyles implements Style {
    MUSHO_SWORD_GOD(SwordStyle.SWORD_GOD),
    MUSHO_WATER_GOD(SwordStyle.WATER_GOD),
    MUSHO_NORTH_GOD(SwordStyle.NORTH_GOD),
    /** The North God Style with a weapon in each hand (Auber's two swords, Hazy Cross). */
    MUSHO_NORTH_GOD_DUAL(SwordStyle.NORTH_GOD);

    private final SwordStyle style;
    private final int id;

    EpicFightSwordStyles(SwordStyle style) {
        this.style = style;
        this.id = Style.ENUM_MANAGER.assign(this);
    }

    public SwordStyle style() {
        return style;
    }

    @Override
    public int universalOrdinal() {
        return id;
    }

    /** Only the weapon hand matters: styles never change what the off hand may hold. */
    @Override
    public boolean canUseOffhand() {
        return true;
    }
}
