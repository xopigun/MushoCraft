package fr.xopi.mushocraft.core;

import fr.xopi.mushocraft.MushoCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Armour materials of MushoCraft.
 */
public final class MushoArmorMaterials {
    private static final DeferredRegister<ArmorMaterial> MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, MushoCraft.MODID);

    /**
     * Snow Drake scales: they resist ice magic and hold heat, prized for shields and armour (wiki, page <i>Monsters</i>).
     * As sturdy as iron, a little tougher; the ice resistance itself is {@link fr.xopi.mushocraft.magic.spell.IceDamage}.
     */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> SNOW_DRAKE = MATERIALS.register("snow_drake",
            () -> new ArmorMaterial(defense(2, 5, 6, 2), 12, SoundEvents.ARMOR_EQUIP_IRON,
                    () -> Ingredient.of(MushoItems.SNOW_DRAKE_SCALE.get()),
                    List.of(new ArmorMaterial.Layer(MushoCraft.id("snow_drake"))), 1.0F, 0.0F));

    private MushoArmorMaterials() {
    }

    private static Map<ArmorItem.Type, Integer> defense(int boots, int leggings, int chestplate, int helmet) {
        Map<ArmorItem.Type, Integer> map = new EnumMap<>(ArmorItem.Type.class);
        map.put(ArmorItem.Type.BOOTS, boots);
        map.put(ArmorItem.Type.LEGGINGS, leggings);
        map.put(ArmorItem.Type.CHESTPLATE, chestplate);
        map.put(ArmorItem.Type.HELMET, helmet);
        map.put(ArmorItem.Type.BODY, chestplate);
        return map;
    }

    public static void register(IEventBus modEventBus) {
        MATERIALS.register(modEventBus);
    }
}
