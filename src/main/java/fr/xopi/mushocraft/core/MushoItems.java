package fr.xopi.mushocraft.core;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.magic.focus.MagicFocusItem;
import fr.xopi.mushocraft.magic.item.ConjuredSwordItem;
import fr.xopi.mushocraft.magic.item.SpellBookItem;
import fr.xopi.mushocraft.magic.spell.MushoSpells;
import fr.xopi.mushocraft.magic.spell.SpellSchool;
import fr.xopi.mushocraft.magic.summon.SummonRituals;
import fr.xopi.mushocraft.magic.summon.SummoningPaperItem;
import fr.xopi.mushocraft.monster.DrakeScaleShieldItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.neoforged.bus.api.IEventBus;
import fr.xopi.mushocraft.sword.entity.TrickItem;
import fr.xopi.mushocraft.sword.entity.TrickKind;
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

    /** Teardrop Bullet's bag of powdered spices (North God Style); can also be thrown by hand. */
    public static final DeferredItem<TrickItem> SPICE_POUCH = ITEMS.register("spice_pouch",
            () -> new TrickItem(TrickKind.SPICE, new Item.Properties().stacksTo(16)));

    /** Crimson Ink's red ball of instant adhesive (North God Style); can also be thrown by hand. */
    public static final DeferredItem<TrickItem> CRIMSON_BALL = ITEMS.register("crimson_ball",
            () -> new TrickItem(TrickKind.CRIMSON_INK, new Item.Properties().stacksTo(16)));

    /** A Snow Drake's white scale: it resists ice magic and holds heat, prized for shields and armour (wiki). */
    public static final DeferredItem<Item> SNOW_DRAKE_SCALE = ITEMS.register("snow_drake_scale",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<ArmorItem> SNOW_DRAKE_HELMET = drakeArmour("snow_drake_helmet", ArmorItem.Type.HELMET);
    public static final DeferredItem<ArmorItem> SNOW_DRAKE_CHESTPLATE = drakeArmour("snow_drake_chestplate", ArmorItem.Type.CHESTPLATE);
    public static final DeferredItem<ArmorItem> SNOW_DRAKE_LEGGINGS = drakeArmour("snow_drake_leggings", ArmorItem.Type.LEGGINGS);
    public static final DeferredItem<ArmorItem> SNOW_DRAKE_BOOTS = drakeArmour("snow_drake_boots", ArmorItem.Type.BOOTS);

    /** A shield of Snow Drake scales: it stops ice magic while raised. */
    public static final DeferredItem<DrakeScaleShieldItem> SNOW_DRAKE_SHIELD = ITEMS.register("snow_drake_shield",
            () -> new DrakeScaleShieldItem(new Item.Properties().durability(420).rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<DeferredSpawnEggItem> SWORD_MASTER_SPAWN_EGG = ITEMS.register("sword_master_spawn_egg",
            () -> new DeferredSpawnEggItem(MushoEntities.SWORD_MASTER, 0x2B2B33, 0xC9C9D6, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> ASSAULT_DOG_SPAWN_EGG = ITEMS.register("assault_dog_spawn_egg",
            () -> new DeferredSpawnEggItem(MushoEntities.ASSAULT_DOG, 0x1E1F28, 0xA8623A, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> PAX_COYOTE_SPAWN_EGG = ITEMS.register("pax_coyote_spawn_egg",
            () -> new DeferredSpawnEggItem(MushoEntities.PAX_COYOTE, 0x5E5A7C, 0xB2ACCC, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> TERMINATE_BOAR_SPAWN_EGG = ITEMS.register("terminate_boar_spawn_egg",
            () -> new DeferredSpawnEggItem(MushoEntities.TERMINATE_BOAR, 0x2E2420, 0xE6DCC4, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> LUSTER_GRIZZLY_SPAWN_EGG = ITEMS.register("luster_grizzly_spawn_egg",
            () -> new DeferredSpawnEggItem(MushoEntities.LUSTER_GRIZZLY, 0xE8EAF0, 0x1E1E24, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> GOBLIN_SPAWN_EGG = ITEMS.register("goblin_spawn_egg",
            () -> new DeferredSpawnEggItem(MushoEntities.GOBLIN, 0x8C7F5E, 0x5E4C68, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> WRAITH_SPAWN_EGG = ITEMS.register("wraith_spawn_egg",
            () -> new DeferredSpawnEggItem(MushoEntities.WRAITH, 0x3C4258, 0xDCF2FF, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> ARMORED_WARRIOR_SPAWN_EGG = ITEMS.register("armored_warrior_spawn_egg",
            () -> new DeferredSpawnEggItem(MushoEntities.ARMORED_WARRIOR, 0x8C929E, 0x8A4E2C, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> MAD_SKULL_SPAWN_EGG = ITEMS.register("mad_skull_spawn_egg",
            () -> new DeferredSpawnEggItem(MushoEntities.MAD_SKULL, 0x7C8490, 0xE2DED0, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> TARANTULA_DEATHLORD_SPAWN_EGG = ITEMS.register("tarantula_deathlord_spawn_egg",
            () -> new DeferredSpawnEggItem(MushoEntities.TARANTULA_DEATHLORD, 0x1E202A, 0xB02C28, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> SNOW_DRAKE_SPAWN_EGG = ITEMS.register("snow_drake_spawn_egg",
            () -> new DeferredSpawnEggItem(MushoEntities.SNOW_DRAKE, 0xECECF2, 0x404896, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> DEVOURING_DEVIL_SPAWN_EGG = ITEMS.register("devouring_devil_spawn_egg",
            () -> new DeferredSpawnEggItem(MushoEntities.DEVOURING_DEVIL, 0x22223A, 0x76548C, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> EXECUTIONER_SPAWN_EGG = ITEMS.register("executioner_spawn_egg",
            () -> new DeferredSpawnEggItem(MushoEntities.EXECUTIONER, 0x9C9EA6, 0x423A38, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> MAGIC_TUTOR_SPAWN_EGG = ITEMS.register("magic_tutor_spawn_egg",
            () -> new DeferredSpawnEggItem(MushoEntities.MAGIC_TUTOR, 0x22407A, 0x8FC7FF, new Item.Properties()));

    private MushoItems() {
    }

    /** A piece of Snow Drake scale armour: as sturdy as iron, it resists ice magic (see {@code IceDamage}). */
    private static DeferredItem<ArmorItem> drakeArmour(String name, ArmorItem.Type type) {
        return ITEMS.register(name, () -> new ArmorItem(MushoArmorMaterials.SNOW_DRAKE, type,
                new Item.Properties().durability(type.getDurability(18)).rarity(Rarity.UNCOMMON)));
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
