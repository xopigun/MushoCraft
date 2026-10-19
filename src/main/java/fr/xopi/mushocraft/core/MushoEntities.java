package fr.xopi.mushocraft.core;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.magic.entity.SpellFire;
import fr.xopi.mushocraft.magic.entity.SpellPillar;
import fr.xopi.mushocraft.magic.entity.SpellProjectile;
import fr.xopi.mushocraft.magic.entity.SpellZone;
import fr.xopi.mushocraft.magic.entity.WaterSheet;
import fr.xopi.mushocraft.magic.summon.LamplightSpirit;
import fr.xopi.mushocraft.magic.summon.SummonedSpirit;
import fr.xopi.mushocraft.npc.MagicTutorEntity;
import fr.xopi.mushocraft.npc.SwordMasterEntity;
import fr.xopi.mushocraft.npc.TeacherEntity;
import fr.xopi.mushocraft.sword.entity.ThrownWeapon;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/**
 * Entity types of the mod.
 */
public final class MushoEntities {
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, MushoCraft.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<SpellProjectile>> SPELL_PROJECTILE =
            ENTITY_TYPES.register("spell_projectile", () -> EntityType.Builder
                    .<SpellProjectile>of(SpellProjectile::new, MobCategory.MISC)
                    .sized(0.4F, 0.4F)
                    .clientTrackingRange(6)
                    .updateInterval(5)
                    .build(MushoCraft.id("spell_projectile").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<ThrownWeapon>> THROWN_WEAPON =
            ENTITY_TYPES.register("thrown_weapon", () -> EntityType.Builder
                    .<ThrownWeapon>of(ThrownWeapon::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(6)
                    .updateInterval(5)
                    .build(MushoCraft.id("thrown_weapon").toString()));

    /** Invisible place of a lasting spell (storm, tornado, sealing barrier). */
    public static final DeferredHolder<EntityType<?>, EntityType<SpellZone>> SPELL_ZONE =
            ENTITY_TYPES.register("spell_zone", () -> EntityType.Builder
                    .<SpellZone>of(SpellZone::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .noSummon()
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build(MushoCraft.id("spell_zone").toString()));

    /** The visible water of Water Shield. */
    public static final DeferredHolder<EntityType<?>, EntityType<WaterSheet>> WATER_SHEET =
            ENTITY_TYPES.register("water_sheet", () -> EntityType.Builder
                    .<WaterSheet>of(WaterSheet::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .noSummon()
                    .clientTrackingRange(8)
                    .updateInterval(40)
                    .build(MushoCraft.id("water_sheet").toString()));

    /** The visible pillar or spike of Ice Pillar, Earth Lance and Earth Hedgehog (a Blockbench model). */
    public static final DeferredHolder<EntityType<?>, EntityType<SpellPillar>> SPELL_PILLAR =
            ENTITY_TYPES.register("spell_pillar", () -> EntityType.Builder
                    .<SpellPillar>of(SpellPillar::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .noSummon()
                    .clientTrackingRange(8)
                    .updateInterval(40)
                    .build(MushoCraft.id("spell_pillar").toString()));

    /** Conjured fire with a body: an explosion or a pillar of flame. */
    public static final DeferredHolder<EntityType<?>, EntityType<SpellFire>> SPELL_FIRE =
            ENTITY_TYPES.register("spell_fire", () -> EntityType.Builder
                    .<SpellFire>of(SpellFire::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .noSummon()
                    .clientTrackingRange(8)
                    .updateInterval(40)
                    .build(MushoCraft.id("spell_fire").toString()));

    /** A lamplight spirit circling its summoner. */
    public static final DeferredHolder<EntityType<?>, EntityType<LamplightSpirit>> LAMPLIGHT_SPIRIT =
            ENTITY_TYPES.register("lamplight_spirit", () -> EntityType.Builder
                    .<LamplightSpirit>of(LamplightSpirit::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F)
                    .noSummon()
                    .clientTrackingRange(8)
                    .updateInterval(2)
                    .build(MushoCraft.id("lamplight_spirit").toString()));

    /** A fighting spirit made of its summoner's mana. */
    public static final DeferredHolder<EntityType<?>, EntityType<SummonedSpirit>> SUMMONED_SPIRIT =
            ENTITY_TYPES.register("summoned_spirit", () -> EntityType.Builder
                    .<SummonedSpirit>of(SummonedSpirit::new, MobCategory.MISC)
                    .sized(0.6F, 0.6F)
                    .noSummon()
                    .clientTrackingRange(8)
                    .build(MushoCraft.id("summoned_spirit").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<SwordMasterEntity>> SWORD_MASTER =
            ENTITY_TYPES.register("sword_master", () -> EntityType.Builder
                    .<SwordMasterEntity>of(SwordMasterEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(10)
                    .build(MushoCraft.id("sword_master").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<MagicTutorEntity>> MAGIC_TUTOR =
            ENTITY_TYPES.register("magic_tutor", () -> EntityType.Builder
                    .<MagicTutorEntity>of(MagicTutorEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(10)
                    .build(MushoCraft.id("magic_tutor").toString()));

    private MushoEntities() {
    }

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(MushoEntities::onCreateAttributes);
        modEventBus.addListener(MushoEntities::onRegisterSpawnPlacements);
    }

    private static void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        for (EntityType<? extends TeacherEntity> teacher : List.of(SWORD_MASTER.get(), MAGIC_TUTOR.get())) {
            event.register(teacher, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    TeacherEntity::checkTeacherSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
    }

    private static void onCreateAttributes(EntityAttributeCreationEvent event) {
        event.put(SWORD_MASTER.get(), SwordMasterEntity.createAttributes().build());
        event.put(MAGIC_TUTOR.get(), MagicTutorEntity.createAttributes().build());
        event.put(SUMMONED_SPIRIT.get(), SummonedSpirit.createAttributes().build());
    }
}
