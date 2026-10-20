package fr.xopi.mushocraft.monster;

import fr.xopi.mushocraft.core.MushoItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;

/**
 * A shield of Snow Drake scales (wiki, page <i>Monsters</i>: the scales are prized for shields and armour): it guards
 * as a shield does, and while raised it stops ice magic and the cold entirely (see
 * {@link fr.xopi.mushocraft.magic.spell.IceDamage}). Being a {@link ShieldItem}, Epic Fight treats it as a shield.
 */
public class DrakeScaleShieldItem extends ShieldItem {
    public DrakeScaleShieldItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isValidRepairItem(ItemStack shield, ItemStack repair) {
        return repair.is(MushoItems.SNOW_DRAKE_SCALE.get());
    }
}
