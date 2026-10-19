package fr.xopi.mushocraft.core;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.magic.focus.MagicFocusItem;
import fr.xopi.mushocraft.magic.item.ConjuredSwordItem;
import fr.xopi.mushocraft.magic.item.SpellBookItem;
import fr.xopi.mushocraft.magic.spell.MushoSpells;
import fr.xopi.mushocraft.magic.spell.SpellSchool;
import fr.xopi.mushocraft.magic.summon.SummonRituals;
import fr.xopi.mushocraft.magic.summon.SummoningPaperItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Map;

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
    // ---- Wands and staffs (wiki, page Magic Tools and Magic Items): never required, they strengthen spells ----

    /** A plain wooden wand with a small magic stone. */
    public static final DeferredItem<MagicFocusItem> APPRENTICE_WAND = ITEMS.register("apprentice_wand",
            () -> new MagicFocusItem(new Item.Properties(), 0.10, Map.of()));

    /** A magician's staff of good wood, crowned with a larger magic stone. */
    public static final DeferredItem<MagicFocusItem> MAGE_STAFF = ITEMS.register("mage_staff",
            () -> new MagicFocusItem(new Item.Properties().rarity(Rarity.UNCOMMON), 0.20, Map.of()));

    /**
     * Aqua Heartia, the A-rank staff made for Rudeus: Elder Treant wood and a dragon's magic stone; it greatly improves
     * water magic and, to a lesser extent, earth, wind and fire (wiki). Unique: not craftable.
     */
    public static final DeferredItem<MagicFocusItem> AQUA_HEARTIA = ITEMS.register("aqua_heartia",
            () -> new MagicFocusItem(new Item.Properties().rarity(Rarity.EPIC), 0.15, Map.of(SpellSchool.WATER, 0.25)));

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

    // ---- Summoning papers (wiki, Summoning Magic): circles drawn in advance, spent when used ----

    public static final DeferredItem<SummoningPaperItem> LAMPLIGHT_SUMMONING_PAPER = ITEMS.register("lamplight_summoning_paper",
            () -> new SummoningPaperItem(new Item.Properties().stacksTo(16), SummonRituals.LAMPLIGHT));

    public static final DeferredItem<SummoningPaperItem> SPIRIT_SUMMONING_PAPER = ITEMS.register("spirit_summoning_paper",
            () -> new SummoningPaperItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON), SummonRituals.SPIRIT));

    public static final DeferredItem<SummoningPaperItem> BEAST_SUMMONING_PAPER = ITEMS.register("beast_summoning_paper",
            () -> new SummoningPaperItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON), SummonRituals.BEAST));

    public static final DeferredItem<DeferredSpawnEggItem> SWORD_MASTER_SPAWN_EGG = ITEMS.register("sword_master_spawn_egg",
            () -> new DeferredSpawnEggItem(MushoEntities.SWORD_MASTER, 0x2B2B33, 0xC9C9D6, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> ASSAULT_DOG_SPAWN_EGG = ITEMS.register("assault_dog_spawn_egg",
            () -> new DeferredSpawnEggItem(MushoEntities.ASSAULT_DOG, 0x1E1F28, 0xA8623A, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> MAGIC_TUTOR_SPAWN_EGG = ITEMS.register("magic_tutor_spawn_egg",
            () -> new DeferredSpawnEggItem(MushoEntities.MAGIC_TUTOR, 0x22407A, 0x8FC7FF, new Item.Properties()));

    private MushoItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
