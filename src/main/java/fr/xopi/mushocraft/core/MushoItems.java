package fr.xopi.mushocraft.core;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.magic.item.ConjuredSwordItem;
import fr.xopi.mushocraft.magic.item.SpellBookItem;
import fr.xopi.mushocraft.magic.spell.MushoSpells;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/**
 * Items of the mod.
 */
public final class MushoItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MushoCraft.MODID);

    /** Sword of ice conjured by the Ice Blade spell. Melts after a minute. */
    public static final DeferredItem<ConjuredSwordItem> ICE_BLADE = ITEMS.register("ice_blade",
            () -> new ConjuredSwordItem(Tiers.IRON, new Item.Properties()
                    .attributes(SwordItem.createAttributes(Tiers.IRON, 3, -2.4F))
                    .stacksTo(1)));

    /** Beginner attack magic of the four elements, like the textbook Rudeus learns from (LN vol. 1 ch. 3). */
    public static final DeferredItem<SpellBookItem> MAGIC_TEXTBOOK = ITEMS.register("magic_textbook",
            () -> new SpellBookItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), () -> List.of(
                    MushoSpells.WATER_BALL.get(), MushoSpells.WATER_SHIELD.get(), MushoSpells.WATER_ARROW.get(),
                    MushoSpells.ICE_SMASH.get(), MushoSpells.ICE_BLADE.get(), MushoSpells.FIREBALL.get(),
                    MushoSpells.ROCK_BULLET.get())));

    /** Beginner healing and detoxification. */
    public static final DeferredItem<SpellBookItem> HEALING_PRIMER = ITEMS.register("healing_primer",
            () -> new SpellBookItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), () -> List.of(
                    MushoSpells.HEALING.get(), MushoSpells.DETOXIFICATION.get())));

    /** Beginner barrier and divine strike: the only rank of these schools Milis allows to be taught. */
    public static final DeferredItem<SpellBookItem> MILIS_PRAYER_BOOK = ITEMS.register("milis_prayer_book",
            () -> new SpellBookItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), () -> List.of(
                    MushoSpells.MAGIC_SHIELD.get(), MushoSpells.EXORCIST_LIGHT.get())));

    /** Intermediate attack magic of the four elements. Each spell needs its school at Intermediate. */
    public static final DeferredItem<SpellBookItem> INTERMEDIATE_ATTACK_TREATISE = ITEMS.register("intermediate_attack_treatise",
            () -> new SpellBookItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), () -> List.of(
                    MushoSpells.WATER_CANNON.get(), MushoSpells.ICE_PILLAR.get(), MushoSpells.FLAME_PILLAR.get(),
                    MushoSpells.EXPLOSION.get(), MushoSpells.EX_FLAME.get(), MushoSpells.SONIC_BOOM.get(),
                    MushoSpells.WIND_SLICE.get(), MushoSpells.STONE_CANNON.get(), MushoSpells.EARTH_PILLAR.get(),
                    MushoSpells.EARTH_LANCE.get())));

    /** Intermediate healing. */
    public static final DeferredItem<SpellBookItem> INTERMEDIATE_HEALING_TREATISE = ITEMS.register("intermediate_healing_treatise",
            () -> new SpellBookItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), () -> List.of(
                    MushoSpells.X_HEALING.get())));

    private MushoItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
