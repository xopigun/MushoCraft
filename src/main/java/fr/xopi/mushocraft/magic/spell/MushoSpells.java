package fr.xopi.mushocraft.magic.spell;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.core.MushoBlocks;
import fr.xopi.mushocraft.core.MushoItems;
import fr.xopi.mushocraft.core.MushoMobEffects;
import fr.xopi.mushocraft.core.MushoParticles;
import fr.xopi.mushocraft.core.MushoRegistries;
import fr.xopi.mushocraft.core.Rank;
import fr.xopi.mushocraft.magic.entity.PillarKind;
import fr.xopi.mushocraft.magic.entity.ZoneVisual;
import fr.xopi.mushocraft.magic.spell.effect.AreaBurstEffect;
import fr.xopi.mushocraft.magic.spell.effect.BurstEffect;
import fr.xopi.mushocraft.magic.spell.effect.CloudEffect;
import fr.xopi.mushocraft.magic.spell.effect.CompositeEffect;
import fr.xopi.mushocraft.magic.spell.effect.ConeBlastEffect;
import fr.xopi.mushocraft.magic.spell.effect.ConjureItemEffect;
import fr.xopi.mushocraft.magic.spell.effect.CureEffect;
import fr.xopi.mushocraft.magic.spell.effect.CurvedWallEffect;
import fr.xopi.mushocraft.magic.spell.effect.EruptionEffect;
import fr.xopi.mushocraft.magic.spell.effect.EruptionVisual;
import fr.xopi.mushocraft.magic.spell.effect.ExplosionEffect;
import fr.xopi.mushocraft.magic.spell.effect.FallingProjectileEffect;
import fr.xopi.mushocraft.magic.spell.effect.FortressEffect;
import fr.xopi.mushocraft.magic.spell.effect.FreezeFieldEffect;
import fr.xopi.mushocraft.magic.spell.effect.HealEffect;
import fr.xopi.mushocraft.magic.spell.effect.PiercingRayEffect;
import fr.xopi.mushocraft.magic.spell.effect.ProjectileEffect;
import fr.xopi.mushocraft.magic.spell.effect.ProjectileProfile;
import fr.xopi.mushocraft.magic.spell.effect.ProjectileShape;
import fr.xopi.mushocraft.magic.spell.effect.ScatteredEruptionEffect;
import fr.xopi.mushocraft.magic.spell.effect.ScorchCasterEffect;
import fr.xopi.mushocraft.magic.spell.effect.SelfStatusEffect;
import fr.xopi.mushocraft.magic.spell.effect.TargetedZoneEffect;
import fr.xopi.mushocraft.magic.spell.effect.VolleyEffect;
import fr.xopi.mushocraft.magic.spell.effect.WallEffect;
import fr.xopi.mushocraft.magic.spell.effect.WeatherEffect;
import fr.xopi.mushocraft.magic.spell.effect.ZoneEffect;
import fr.xopi.mushocraft.magic.spell.meld.MeldIngredient;
import fr.xopi.mushocraft.magic.spell.zone.ZoneBehaviors;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/**
 * Every spell of the mod. Names, schools and ranks follow docs/GLOSSAIRE.md; each line cites where the wiki
 * places the spell. Numbers (cost, damage, cooldown) are gameplay choices, open to balancing.
 */
public final class MushoSpells {
    private static final DeferredRegister<Spell> SPELLS = DeferredRegister.create(MushoRegistries.SPELL, MushoCraft.MODID);

    // ---- Water, Beginner (LN vol. 1 ch. 3) ----

    /** Materializes and hurls a mass of water. */
    public static final DeferredHolder<Spell, Spell> WATER_BALL = SPELLS.register("water_ball",
            () -> Spell.builder(SpellSchool.WATER, Rank.BEGINNER).manaCost(5).cooldownTicks(20)
                    .effect(new ProjectileEffect(ProjectileProfile.of(SpellElement.WATER, 4.0F)
                            .speed(1.4F).gravity(0.01F).size(0.6F).knockback(0.8F).build(), SoundEvents.BUCKET_EMPTY))
                    .build());

    /** Creates a spout of water from the ground forming a defensive wall. */
    public static final DeferredHolder<Spell, Spell> WATER_SHIELD = SPELLS.register("water_shield",
            () -> Spell.builder(SpellSchool.WATER, Rank.BEGINNER).intent(SpellIntent.DEFEND).manaCost(8).cooldownTicks(100)
                    .effect(new CurvedWallEffect(MushoBlocks.WATER_WALL, 2.5, 6.0, 4, 200, SoundEvents.BUCKET_EMPTY))
                    .build());

    /** Launches a bolt of water, roughly twenty centimeters long. */
    public static final DeferredHolder<Spell, Spell> WATER_ARROW = SPELLS.register("water_arrow",
            () -> Spell.builder(SpellSchool.WATER, Rank.BEGINNER).manaCost(4).cooldownTicks(15)
                    .effect(new ProjectileEffect(ProjectileProfile.of(SpellElement.WATER, 3.0F)
                            .speed(2.4F).size(0.3F).knockback(0.2F).shape(ProjectileShape.BOLT).build(),
                            SoundEvents.TRIDENT_THROW.value()))
                    .build());

    /** Strikes an opponent with a mound of ice. */
    public static final DeferredHolder<Spell, Spell> ICE_SMASH = SPELLS.register("ice_smash",
            () -> Spell.builder(SpellSchool.WATER, Rank.BEGINNER).manaCost(6).cooldownTicks(30)
                    .effect(new ProjectileEffect(ProjectileProfile.of(SpellElement.ICE, 5.0F)
                            .speed(1.6F).gravity(0.02F).size(0.7F).knockback(0.4F).slow(60)
                            .shape(ProjectileShape.CRYSTAL_CLUSTER).build(),
                            SoundEvents.GLASS_PLACE))
                    .build());

    /** Creates a sword made out of ice. */
    public static final DeferredHolder<Spell, Spell> ICE_BLADE = SPELLS.register("ice_blade",
            () -> Spell.builder(SpellSchool.WATER, Rank.BEGINNER).intent(SpellIntent.UTILITY).manaCost(10).cooldownTicks(200)
                    .effect(new ConjureItemEffect(MushoItems.ICE_BLADE, 1200, SoundEvents.GLASS_PLACE))
                    .build());

    // ---- Fire, Beginner (LN vol. 2 ch. 2) ----

    /** Materializes and launches a spherule of fire. */
    public static final DeferredHolder<Spell, Spell> FIREBALL = SPELLS.register("fireball",
            () -> Spell.builder(SpellSchool.FIRE, Rank.BEGINNER).manaCost(6).cooldownTicks(25)
                    .effect(new ProjectileEffect(ProjectileProfile.of(SpellElement.FIRE, 5.0F)
                            .speed(1.4F).size(0.6F).ignite(4).build(), SoundEvents.BLAZE_SHOOT))
                    .build());

    // ---- Earth, Beginner (LN vol. 8 ch. 7) ----

    /** Materializes a bullet-sized rock to hit a target. */
    public static final DeferredHolder<Spell, Spell> ROCK_BULLET = SPELLS.register("rock_bullet",
            () -> Spell.builder(SpellSchool.EARTH, Rank.BEGINNER).manaCost(5).cooldownTicks(20)
                    .effect(new ProjectileEffect(ProjectileProfile.of(SpellElement.EARTH, 6.0F)
                            .speed(2.0F).gravity(0.02F).size(0.35F).knockback(0.3F).build(), SoundEvents.STONE_PLACE))
                    .build());

    // ---- Healing and detoxification ----

    /** Heals minor cuts (Beginner, anime S1 ep. 1 per the wiki). */
    public static final DeferredHolder<Spell, Spell> HEALING = SPELLS.register("healing",
            () -> Spell.builder(SpellSchool.HEALING, Rank.BEGINNER).intent(SpellIntent.SUPPORT).manaCost(8).cooldownTicks(40)
                    .effect(new HealEffect(4.0F, 5.0))
                    .build());

    /**
     * Cures poison and sickness. The wiki gives no rank for this spell but says most conditions are curable
     * at the lowest rank: classed Beginner (docs/ECARTS.md, choice to validate).
     */
    public static final DeferredHolder<Spell, Spell> DETOXIFICATION = SPELLS.register("detoxification",
            () -> Spell.builder(SpellSchool.DETOXIFICATION, Rank.BEGINNER).intent(SpellIntent.SUPPORT).manaCost(8).cooldownTicks(60)
                    .effect(new CureEffect(List.of(MobEffects.POISON, MobEffects.HUNGER, MobEffects.CONFUSION,
                            MobEffects.WEAKNESS)))
                    .build());

    // ---- Barrier and divine strike, Beginner (the only rank Milis lets others teach) ----

    /** Semi-transparent barrier that blocks magical attacks and isolates heat and cold (LN vol. 8 ch. 4). */
    public static final DeferredHolder<Spell, Spell> MAGIC_SHIELD = SPELLS.register("magic_shield",
            () -> Spell.builder(SpellSchool.BARRIER, Rank.BEGINNER).intent(SpellIntent.DEFEND).manaCost(10).cooldownTicks(300)
                    .effect(new SelfStatusEffect(MushoMobEffects.MAGIC_SHIELD, 300, 0, SoundEvents.BEACON_ACTIVATE)
                            .shownAs(ZoneVisual.WARD_MAGIC))
                    .build());

    /** A bolt of white light that can harm a spirit (WN vol. 10 ch. 93). Strong against the undead. */
    public static final DeferredHolder<Spell, Spell> EXORCIST_LIGHT = SPELLS.register("exorcist_light",
            () -> Spell.builder(SpellSchool.DIVINE_STRIKE, Rank.BEGINNER).manaCost(6).cooldownTicks(30)
                    .effect(new ProjectileEffect(ProjectileProfile.of(SpellElement.LIGHT, 3.0F)
                            .speed(2.0F).size(0.5F).undeadMultiplier(3.0F).build(), SoundEvents.AMETHYST_BLOCK_CHIME))
                    .build());

    // ---- Intermediate (school must have reached Intermediate to study them) ----

    /** Launches forth a large concentration of water (LN vol. 4 ch. 4). */
    public static final DeferredHolder<Spell, Spell> WATER_CANNON = SPELLS.register("water_cannon",
            () -> Spell.builder(SpellSchool.WATER, Rank.INTERMEDIATE).manaCost(15).cooldownTicks(60)
                    .effect(new ProjectileEffect(ProjectileProfile.of(SpellElement.WATER, 8.0F)
                            .speed(1.6F).gravity(0.01F).size(1.2F).knockback(2.0F).build(), SoundEvents.GENERIC_SPLASH))
                    .build());

    /** A shaft of ice springing up from the ground (LN vol. 1 ch. 8). */
    public static final DeferredHolder<Spell, Spell> ICE_PILLAR = SPELLS.register("ice_pillar",
            () -> Spell.builder(SpellSchool.WATER, Rank.INTERMEDIATE).manaCost(15).cooldownTicks(80)
                    .effect(new EruptionEffect(MushoBlocks.CONJURED_ICE, 3, 7.0F, 0.8, 0, 16.0,
                            ParticleTypes.SNOWFLAKE, SoundEvents.GLASS_PLACE).lookingLike(EruptionVisual.pillar(PillarKind.ICE_PILLAR, 0.0F)))
                    .build());

    /** Materializes a pillar of fire (WN ch. 184). */
    public static final DeferredHolder<Spell, Spell> FLAME_PILLAR = SPELLS.register("flame_pillar",
            () -> Spell.builder(SpellSchool.FIRE, Rank.INTERMEDIATE).manaCost(16).cooldownTicks(80)
                    .effect(new EruptionEffect(null, 4, 7.0F, 0.4, 5, 16.0, MushoParticles.EMBER.get(), SoundEvents.BLAZE_SHOOT)
                            .lookingLike(EruptionVisual.fireColumn(1.1F, 30)))
                    .build());

    /** Creates an explosion (no source cited by the wiki). Never breaks blocks. */
    public static final DeferredHolder<Spell, Spell> EXPLOSION = SPELLS.register("explosion",
            () -> Spell.builder(SpellSchool.FIRE, Rank.INTERMEDIATE).manaCost(20).cooldownTicks(100)
                    .effect(new ExplosionEffect(2.5F, 20.0))
                    .build());

    /** Launches a large fireball about two meters across (LN vol. 7 ch. 2). */
    public static final DeferredHolder<Spell, Spell> EX_FLAME = SPELLS.register("ex_flame",
            () -> Spell.builder(SpellSchool.FIRE, Rank.INTERMEDIATE).manaCost(22).cooldownTicks(120)
                    .effect(new ProjectileEffect(ProjectileProfile.of(SpellElement.FIRE, 8.0F)
                            .speed(1.0F).size(2.0F).ignite(5).explode(1.5F).build(), SoundEvents.BLAZE_SHOOT))
                    .build());

    /** Launches a slicing wind blade that throws the target away (LN vol. 2 ch. 2). */
    public static final DeferredHolder<Spell, Spell> SONIC_BOOM = SPELLS.register("sonic_boom",
            () -> Spell.builder(SpellSchool.WIND, Rank.INTERMEDIATE).manaCost(12).cooldownTicks(60)
                    .effect(new ProjectileEffect(ProjectileProfile.of(SpellElement.WIND, 5.0F)
                            .speed(2.2F).size(0.6F).knockback(2.5F).shape(ProjectileShape.CRESCENT).build(), SoundEvents.BREEZE_SHOOT))
                    .build());

    /** Shoots a gust of cutting wind (no source cited by the wiki). */
    public static final DeferredHolder<Spell, Spell> WIND_SLICE = SPELLS.register("wind_slice",
            () -> Spell.builder(SpellSchool.WIND, Rank.INTERMEDIATE).manaCost(12).cooldownTicks(40)
                    .effect(new ProjectileEffect(ProjectileProfile.of(SpellElement.WIND, 7.0F)
                            .speed(2.6F).size(0.5F).shape(ProjectileShape.CRESCENT).build(), SoundEvents.BREEZE_SHOOT))
                    .build());

    /** Rudeus' signature: a bullet-shaped rock fired at high speed (LN vol. 6). */
    public static final DeferredHolder<Spell, Spell> STONE_CANNON = SPELLS.register("stone_cannon",
            () -> Spell.builder(SpellSchool.EARTH, Rank.INTERMEDIATE).manaCost(18).cooldownTicks(60)
                    .effect(new ProjectileEffect(ProjectileProfile.of(SpellElement.EARTH, 12.0F)
                            .speed(3.2F).gravity(0.01F).size(0.5F).knockback(1.0F).build(), SoundEvents.STONE_PLACE))
                    .build());

    /** Summons a boulder above the target to drop on it (LN vol. 12 ch. 8). */
    public static final DeferredHolder<Spell, Spell> EARTH_PILLAR = SPELLS.register("earth_pillar",
            () -> Spell.builder(SpellSchool.EARTH, Rank.INTERMEDIATE).manaCost(16).cooldownTicks(80)
                    .effect(new FallingProjectileEffect(ProjectileProfile.of(SpellElement.EARTH, 10.0F)
                            .speed(0.6F).gravity(0.08F).size(1.4F).knockback(0.6F).build(), 8.0, 16.0,
                            SoundEvents.STONE_PLACE))
                    .build());

    /** An earthen spike rising from the ground (LN vol. 6 ch. 3). */
    public static final DeferredHolder<Spell, Spell> EARTH_LANCE = SPELLS.register("earth_lance",
            () -> Spell.builder(SpellSchool.EARTH, Rank.INTERMEDIATE).manaCost(15).cooldownTicks(80)
                    .effect(new EruptionEffect(MushoBlocks.CONJURED_EARTH, 3, 8.0F, 1.0, 0, 16.0,
                            ParticleTypes.ASH, SoundEvents.ROOTED_DIRT_PLACE).lookingLike(EruptionVisual.pillar(PillarKind.EARTH_LANCE, 6.0F)))
                    .build());

    /** Heals wounds as severe as broken bones (LN vol. 2 ch. 2). */
    public static final DeferredHolder<Spell, Spell> X_HEALING = SPELLS.register("x_healing",
            () -> Spell.builder(SpellSchool.HEALING, Rank.INTERMEDIATE).intent(SpellIntent.SUPPORT).manaCost(18).cooldownTicks(100)
                    .effect(new HealEffect(10.0F, 5.0))
                    .build());

    /**
     * Barrier that blocks physical attacks (LN vol. 4 ch. 6). Above Beginner, only the Holy Knights of Milis may
     * teach barrier magic (WN ch. 69): no book teaches it, a teacher will.
     */
    public static final DeferredHolder<Spell, Spell> PHYSICAL_SHIELD = SPELLS.register("physical_shield",
            () -> Spell.builder(SpellSchool.BARRIER, Rank.INTERMEDIATE).intent(SpellIntent.DEFEND).manaCost(20).cooldownTicks(400)
                    .effect(new SelfStatusEffect(MushoMobEffects.PHYSICAL_SHIELD, 300, 0, SoundEvents.ANVIL_PLACE)
                            .shownAs(ZoneVisual.WARD_PHYSICAL))
                    .build());

    // ---- Advanced (school must have reached Advanced; wiki, page Magic Spells) ----

    /** Shoots ice shards in all directions (LN vol. 7 ch. 2). */
    public static final DeferredHolder<Spell, Spell> BLIZZARD_STORM = SPELLS.register("blizzard_storm",
            () -> Spell.builder(SpellSchool.WATER, Rank.ADVANCED).manaCost(40).cooldownTicks(200)
                    .effect(new BurstEffect(ProjectileProfile.of(SpellElement.ICE, 6.0F)
                            .speed(1.6F).gravity(0.01F).size(0.35F).slow(60).build(), 16, SoundEvents.GLASS_BREAK))
                    .build());

    /** Manipulates clouds to bring down rain (anime, season 1; the wiki cites no volume). */
    public static final DeferredHolder<Spell, Spell> SQUALL = SPELLS.register("squall",
            () -> Spell.builder(SpellSchool.WATER, Rank.ADVANCED).intent(SpellIntent.UTILITY).manaCost(60).cooldownTicks(2400)
                    .effect(new WeatherEffect(6000))
                    .build());

    /**
     * Gathers a great wave of flame and releases it; it explodes on impact, and its heat burns the caster who
     * channels it at close range (LN vol. 5, interlude).
     */
    public static final DeferredHolder<Spell, Spell> EXODUS_FLAME = SPELLS.register("exodus_flame",
            () -> Spell.builder(SpellSchool.FIRE, Rank.ADVANCED).manaCost(45).cooldownTicks(240)
                    .effect(new CompositeEffect(
                            new ProjectileEffect(ProjectileProfile.of(SpellElement.FIRE, 14.0F)
                                    .speed(1.1F).size(2.4F).ignite(8).explode(3.0F).build(), SoundEvents.BLAZE_SHOOT),
                            new ScorchCasterEffect(1.0F)))
                    .build());

    /** A powerful, wide-reaching shockwave, improved Sonic Boom (LN vol. 16 ch. 7). */
    public static final DeferredHolder<Spell, Spell> SONIC_BLAST = SPELLS.register("sonic_blast",
            () -> Spell.builder(SpellSchool.WIND, Rank.ADVANCED).manaCost(35).cooldownTicks(160)
                    .effect(new ConeBlastEffect(10.0, 35.0, 10.0F, 2.5, MushoParticles.WIND_STREAK.get(), SoundEvents.BREEZE_WIND_CHARGE_BURST.value()))
                    .build());

    /** Wind shaped into a spinning drill that punches a hole through the target (LN vol. 16 ch. 7). */
    public static final DeferredHolder<Spell, Spell> TORNADO_IMPACT = SPELLS.register("tornado_impact",
            () -> Spell.builder(SpellSchool.WIND, Rank.ADVANCED).manaCost(35).cooldownTicks(160)
                    .effect(new PiercingRayEffect(24.0, 0.6, 16.0F, ParticleTypes.SWEEP_ATTACK, SoundEvents.BREEZE_SHOOT))
                    .build());

    /** Raises a fortress of earth around the caster (LN vol. 1 ch. 6). */
    public static final DeferredHolder<Spell, Spell> EARTH_FORTRESS = SPELLS.register("earth_fortress",
            () -> Spell.builder(SpellSchool.EARTH, Rank.ADVANCED).intent(SpellIntent.DEFEND).manaCost(40).cooldownTicks(600)
                    .effect(new FortressEffect(MushoBlocks.CONJURED_EARTH, 2, 3, 400, SoundEvents.ROOTED_DIRT_PLACE)
                            .lookingLike(PillarKind.EARTH_FORTRESS))
                    .build());

    /** Earthen spikes burst from the ground all around the aimed spot (LN vol. 7 ch. 4). */
    public static final DeferredHolder<Spell, Spell> EARTH_HEDGEHOG = SPELLS.register("earth_hedgehog",
            () -> Spell.builder(SpellSchool.EARTH, Rank.ADVANCED).manaCost(40).cooldownTicks(200)
                    .effect(new ScatteredEruptionEffect(new EruptionEffect(MushoBlocks.CONJURED_EARTH, 2, 7.0F, 0.7, 0, 18.0,
                            ParticleTypes.ASH, SoundEvents.ROOTED_DIRT_PLACE).lookingLike(EruptionVisual.pillar(PillarKind.EARTH_SPIKE, 25.0F)), 9, 3.0))
                    .build());

    /** Conjures a dusty storm (no source cited by the wiki): it blinds and slows those caught in it. */
    public static final DeferredHolder<Spell, Spell> DUST_STORM = SPELLS.register("dust_storm",
            () -> Spell.builder(SpellSchool.EARTH, Rank.ADVANCED).manaCost(30).cooldownTicks(300)
                    .effect(new CloudEffect(4.0F, 200, 18.0, List.of(
                            new CloudEffect.Affliction(MobEffects.BLINDNESS, 60, 0),
                            new CloudEffect.Affliction(MobEffects.MOVEMENT_SLOWDOWN, 60, 1)),
                            ParticleTypes.WHITE_ASH, SoundEvents.SAND_BREAK))
                    .build());

    /** Heals major open wounds and reattaches freshly severed limbs (LN vol. 12 ch. 9). */
    public static final DeferredHolder<Spell, Spell> SHINE_HEALING = SPELLS.register("shine_healing",
            () -> Spell.builder(SpellSchool.HEALING, Rank.ADVANCED).intent(SpellIntent.SUPPORT).manaCost(35).cooldownTicks(160)
                    .effect(new HealEffect(20.0F, 5.0))
                    .build());

    // ---- Saint (wiki, page Magic Spells) ----

    /**
     * Calls down a thundercloud: rain falls and lightning strikes inside the storm for a while (LN vol. 1 ch. 6, the
     * spell Rudeus passes his Saint water exam with). Bolts deal their damage but set nothing alight.
     */
    public static final DeferredHolder<Spell, Spell> CUMULONIMBUS = SPELLS.register("cumulonimbus",
            () -> Spell.builder(SpellSchool.WATER, Rank.SAINT).manaCost(120).cooldownTicks(1200)
                    .effect(new ZoneEffect(() -> ZoneBehaviors.thunderstorm(12, 14.0F), ZoneVisual.STORM_CLOUD, 10.0, 300, 32.0,
                            SoundEvents.LIGHTNING_BOLT_THUNDER))
                    .build());

    /** Spreads flames over a very wide area instantly (no source cited by the wiki). */
    public static final DeferredHolder<Spell, Spell> FLASH_OVER = SPELLS.register("flash_over",
            () -> Spell.builder(SpellSchool.FIRE, Rank.SAINT).manaCost(100).cooldownTicks(600)
                    .effect(new AreaBurstEffect(9.0, 16.0F, 8, 24.0, MushoParticles.FLAME.get(), SoundEvents.FIRECHARGE_USE))
                    .build());

    /** Releases a strong blast of wind over a wide area, all around the caster (no source cited by the wiki). */
    public static final DeferredHolder<Spell, Spell> VIOLENT_STORM = SPELLS.register("violent_storm",
            () -> Spell.builder(SpellSchool.WIND, Rank.SAINT).manaCost(90).cooldownTicks(600)
                    .effect(new ConeBlastEffect(12.0, 180.0, 12.0F, 4.0, MushoParticles.WIND_STREAK.get(),
                            SoundEvents.BREEZE_WIND_CHARGE_BURST.value()))
                    .build());

    /** Creates a tornado that draws in and lifts everything around (LN vol. 7 ch. 4). */
    public static final DeferredHolder<Spell, Spell> TORNADO = SPELLS.register("tornado",
            () -> Spell.builder(SpellSchool.WIND, Rank.SAINT).manaCost(110).cooldownTicks(900)
                    .effect(new ZoneEffect(() -> ZoneBehaviors.vortex(0.12, 0.09, 4.0F), ZoneVisual.TORNADO, 6.0, 200, 24.0,
                            SoundEvents.ELYTRA_FLYING))
                    .build());

    /** A fierce torrent of sand and wind blanketing a wide area (LN vol. 9 ch. 7). */
    public static final DeferredHolder<Spell, Spell> SANDSTORM = SPELLS.register("sandstorm",
            () -> Spell.builder(SpellSchool.EARTH, Rank.SAINT).manaCost(100).cooldownTicks(900)
                    .effect(new CloudEffect(9.0F, 300, 24.0, List.of(
                            new CloudEffect.Affliction(MobEffects.BLINDNESS, 80, 0),
                            new CloudEffect.Affliction(MobEffects.MOVEMENT_SLOWDOWN, 80, 2),
                            new CloudEffect.Affliction(MobEffects.WEAKNESS, 80, 1)),
                            ParticleTypes.WHITE_ASH, SoundEvents.SAND_BREAK))
                    .build());

    /** A barrier that seals spirits: the undead inside are held fast and worn away (LN vol. 20 ch. 3). */
    public static final DeferredHolder<Spell, Spell> SPIRIT_SEALING_BARRIER = SPELLS.register("spirit_sealing_barrier",
            () -> Spell.builder(SpellSchool.BARRIER, Rank.SAINT).manaCost(100).cooldownTicks(900)
                    .effect(new ZoneEffect(() -> ZoneBehaviors.spiritSeal(5.0F), ZoneVisual.SEALING_DOME, 6.0, 300, 20.0,
                            SoundEvents.BEACON_ACTIVATE))
                    .build());

    // ---- King rank: the only spells the wiki names at this rank (Emperor and God: none, see docs/ECARTS.md, E23) ----

    /**
     * A thundercloud gathers over the target and condenses all its energy into one bolt (LN vol. 13 ch. 6). Falls on
     * the aimed spot when no creature is aimed at.
     */
    public static final DeferredHolder<Spell, Spell> LIGHTNING = SPELLS.register("lightning",
            () -> Spell.builder(SpellSchool.WATER, Rank.KING).manaCost(250).cooldownTicks(1200)
                    .effect(new TargetedZoneEffect(target -> ZoneBehaviors.lightning(target, 30, 60.0F), ZoneVisual.THUNDERHEAD, 2.0, 40,
                            48.0, false, SoundEvents.LIGHTNING_BOLT_THUNDER).followingTarget())
                    .build());

    // ---- Rudeus' own development of Lightning (wiki, Magic § Lightning Magic) ----

    /**
     * Lightning compressed into a small cloud low over the target: a weaker discharge that shocks and paralyses
     * everyone near it, and goes through Battle Aura since it strikes the nerves (LN vol. 13 ch. 6; anime S3 ep. 5).
     * Rank chosen for the game: Saint, below the Lightning it comes from (docs/ECARTS.md, E25).
     */
    public static final DeferredHolder<Spell, Spell> ELECTRIC = SPELLS.register("electric",
            () -> Spell.builder(SpellSchool.WATER, Rank.SAINT).manaCost(90).cooldownTicks(400)
                    .effect(new TargetedZoneEffect(target -> ZoneBehaviors.electric(target, 12, 14.0F, 60), ZoneVisual.CHARGED_CLOUD,
                            2.2, 20, 32.0, false, SoundEvents.BEEHIVE_WORK).followingTarget())
                    .build());

    /**
     * Shuts the target inside a barrier it cannot leave, where magic cannot be used (LN vol. 6 ch. 4). Fizzles without
     * a creature to confine.
     */
    public static final DeferredHolder<Spell, Spell> CONFINEMENT_BARRIER = SPELLS.register("confinement_barrier",
            () -> Spell.builder(SpellSchool.BARRIER, Rank.KING).manaCost(220).cooldownTicks(1200)
                    .effect(new TargetedZoneEffect(ZoneBehaviors::confinement, ZoneVisual.CONFINEMENT_CELL, 1.6, 300, 24.0, true,
                            SoundEvents.BEACON_POWER_SELECT))
                    .build());

    // ---- Emperor rank ----

    /**
     * Freezes a very wide area instantly (WN ch. 194), all around the caster: everything in it freezes and the water
     * ices over. The wiki places it at the Imperial (Emperor) rank.
     */
    public static final DeferredHolder<Spell, Spell> ABSOLUTE_ZERO = SPELLS.register("absolute_zero",
            () -> Spell.builder(SpellSchool.WATER, Rank.EMPEROR).manaCost(600).cooldownTicks(2400)
                    .effect(new FreezeFieldEffect(16.0, 30.0F, 300, 0.0).aroundCaster().freezingWater())
                    .build());

    // ---- Spells the wiki gives no rank (ranks chosen for the game: docs/ECARTS.md, E19) ----

    /** Launches a barrage of ice bolts at a target (wiki, Water; WN vol. 4 ch. 3.5). */
    public static final DeferredHolder<Spell, Spell> ICICLE_BREAK = SPELLS.register("icicle_break",
            () -> Spell.builder(SpellSchool.WATER, Rank.INTERMEDIATE).manaCost(16).cooldownTicks(60)
                    .effect(new VolleyEffect(ProjectileProfile.of(SpellElement.ICE, 3.0F)
                            .speed(2.2F).size(0.25F).slow(30).shape(ProjectileShape.BOLT).build(), 6, SoundEvents.GLASS_PLACE))
                    .build());

    /** Launches a crescent-shaped blade of ice at the enemy (LN vol. 12 ch. 6). */
    public static final DeferredHolder<Spell, Spell> ICICLE_EDGE = SPELLS.register("icicle_edge",
            () -> Spell.builder(SpellSchool.WATER, Rank.INTERMEDIATE).manaCost(14).cooldownTicks(50)
                    .effect(new ProjectileEffect(ProjectileProfile.of(SpellElement.ICE, 8.0F)
                            .speed(2.4F).size(0.7F).slow(60).shape(ProjectileShape.CRESCENT).build(), SoundEvents.GLASS_PLACE))
                    .build());

    /** An ice spike springs from the ground and impales the enemy (LN vol. 16 ch. 7). */
    public static final DeferredHolder<Spell, Spell> ICE_LANCE = SPELLS.register("ice_lance",
            () -> Spell.builder(SpellSchool.WATER, Rank.INTERMEDIATE).manaCost(15).cooldownTicks(80)
                    .effect(new EruptionEffect(MushoBlocks.CONJURED_ICE, 3, 8.0F, 1.0, 0, 16.0,
                            ParticleTypes.SNOWFLAKE, SoundEvents.GLASS_PLACE).lookingLike(EruptionVisual.pillar(PillarKind.ICE_LANCE, 6.0F)))
                    .build());

    /** Raises walls of ice around the caster (manga, Depressed Magician arc ch. 7). */
    public static final DeferredHolder<Spell, Spell> ICE_FORTRESS = SPELLS.register("ice_fortress",
            () -> Spell.builder(SpellSchool.WATER, Rank.ADVANCED).intent(SpellIntent.DEFEND).manaCost(40).cooldownTicks(600)
                    .effect(new FortressEffect(MushoBlocks.CONJURED_ICE, 2, 3, 400, SoundEvents.GLASS_PLACE)
                            .lookingLike(PillarKind.ICE_FORTRESS))
                    .build());

    /** Attacks with a jet of flames over a wide area (LN vol. 7 ch. 3): it pours for a second where the caster looks. */
    public static final DeferredHolder<Spell, Spell> FLAMETHROWER = SPELLS.register("flamethrower",
            () -> Spell.builder(SpellSchool.FIRE, Rank.INTERMEDIATE).manaCost(16).cooldownTicks(80)
                    .effect(new ZoneEffect(() -> ZoneBehaviors.flameJet(8.0, 16.0, 5.0F, 5), 1.0, 20, 0.0, SoundEvents.BLAZE_SHOOT))
                    .build());

    /** A gust of strong wind that pushes everything ahead away, without harm (LN vol. 21 ch. 3). */
    public static final DeferredHolder<Spell, Spell> WIND_BLAST = SPELLS.register("wind_blast",
            () -> Spell.builder(SpellSchool.WIND, Rank.BEGINNER).intent(SpellIntent.DEFEND).manaCost(6).cooldownTicks(40)
                    .effect(new ConeBlastEffect(6.0, 40.0, 0.0F, 2.2, MushoParticles.WIND_STREAK.get(), SoundEvents.BREEZE_WIND_CHARGE_BURST.value()))
                    .build());

    /**
     * Gives the sick the strength to rise again (manga, Roxy Gets Serious vol. 11): cures the illnesses and strong
     * poisons Detoxification cannot, as higher detoxification does (wiki, Detoxification Magic).
     */
    public static final DeferredHolder<Spell, Spell> ANTIDOTE = SPELLS.register("antidote",
            () -> Spell.builder(SpellSchool.DETOXIFICATION, Rank.INTERMEDIATE).intent(SpellIntent.SUPPORT).manaCost(14).cooldownTicks(80)
                    .effect(new CureEffect(List.of(MobEffects.POISON, MobEffects.WITHER, MobEffects.HUNGER, MobEffects.CONFUSION,
                            MobEffects.WEAKNESS, MobEffects.BLINDNESS, MobEffects.DIG_SLOWDOWN)))
                    .build());

    /** Bombards the area with countless water droplets, soaking everyone in it (wiki, Water). */
    public static final DeferredHolder<Spell, Spell> WATER_SPLASH = SPELLS.register("water_splash",
            () -> Spell.builder(SpellSchool.WATER, Rank.BEGINNER).manaCost(8).cooldownTicks(80)
                    .effect(new AreaBurstEffect(4.0, 1.0F, 0, 16.0, ParticleTypes.SPLASH, SoundEvents.GENERIC_SPLASH,
                            List.of(new CloudEffect.Affliction(MushoMobEffects.SOAKED, 300, 0))).withBody(ZoneVisual.SPLASH_CROWN, 16))
                    .build());

    /** Heats the caster's hands, hot enough to melt stone: blows set the foe alight (wiki, Fire). */
    public static final DeferredHolder<Spell, Spell> HEAT_HAND = SPELLS.register("heat_hand",
            () -> Spell.builder(SpellSchool.FIRE, Rank.BEGINNER).intent(SpellIntent.DEFEND).manaCost(8).cooldownTicks(400)
                    .effect(new SelfStatusEffect(MushoMobEffects.HEATED_HANDS, 400, 0, SoundEvents.FIRECHARGE_USE))
                    .build());

    /** Raises a wall of earth ahead (wiki, Earth Wall). */
    public static final DeferredHolder<Spell, Spell> EARTH_WALL = SPELLS.register("earth_wall",
            () -> Spell.builder(SpellSchool.EARTH, Rank.BEGINNER).intent(SpellIntent.DEFEND).manaCost(10).cooldownTicks(160)
                    .effect(new WallEffect(MushoBlocks.CONJURED_EARTH, 2, 3, 3, 300, SoundEvents.ROOTED_DIRT_PLACE)
                            .lookingLike(PillarKind.EARTH_WALL))
                    .build());

    /** Water pours down without end like a real waterfall, pressing down and soaking (wiki, Water). */
    public static final DeferredHolder<Spell, Spell> WATERFALL = SPELLS.register("waterfall",
            () -> Spell.builder(SpellSchool.WATER, Rank.INTERMEDIATE).manaCost(18).cooldownTicks(300)
                    .effect(new ZoneEffect(() -> ZoneBehaviors.waterfall(0.08), ZoneVisual.WATERFALL, 2.5, 160, 16.0,
                            SoundEvents.WATER_AMBIENT))
                    .build());

    /** Floods the ground with water: creatures wade, slowed and soaked (wiki, Water). */
    public static final DeferredHolder<Spell, Spell> FLOOD_FLUSH = SPELLS.register("flood_flush",
            () -> Spell.builder(SpellSchool.WATER, Rank.INTERMEDIATE).manaCost(20).cooldownTicks(300)
                    .effect(new ZoneEffect(ZoneBehaviors::flood, ZoneVisual.FLOOD, 6.0, 200, 16.0, SoundEvents.GENERIC_SPLASH))
                    .build());

    /** Freezes everything in the area, all the more the wet (wiki, Water). */
    public static final DeferredHolder<Spell, Spell> ICICLE_FIELD = SPELLS.register("icicle_field",
            () -> Spell.builder(SpellSchool.WATER, Rank.INTERMEDIATE).manaCost(18).cooldownTicks(200)
                    .effect(new FreezeFieldEffect(4.0, 5.0F, 80, 16.0))
                    .build());

    /** Heats the surroundings: the frozen thaw, the soaked dry, foes scorch (wiki, Fire). */
    public static final DeferredHolder<Spell, Spell> HEAT_ISLAND = SPELLS.register("heat_island",
            () -> Spell.builder(SpellSchool.FIRE, Rank.INTERMEDIATE).intent(SpellIntent.UTILITY).manaCost(15).cooldownTicks(400)
                    .effect(new ZoneEffect(() -> ZoneBehaviors.heatIsland(1.0F), 5.0, 200, 0.0, SoundEvents.FIRE_AMBIENT))
                    .build());

    /** A wreath of fire around the caster engulfs whoever comes close (wiki, Fire). */
    public static final DeferredHolder<Spell, Spell> BURNING_PLACE = SPELLS.register("burning_place",
            () -> Spell.builder(SpellSchool.FIRE, Rank.INTERMEDIATE).intent(SpellIntent.DEFEND).manaCost(18).cooldownTicks(400)
                    .effect(new ZoneEffect(() -> ZoneBehaviors.burningRing(3.0F, 4), 3.0, 120, 0.0, SoundEvents.BLAZE_SHOOT))
                    .build());

    /** Shoots a blade of flame that sears and slices (wiki, Fire). */
    public static final DeferredHolder<Spell, Spell> FLAME_SLICE = SPELLS.register("flame_slice",
            () -> Spell.builder(SpellSchool.FIRE, Rank.INTERMEDIATE).manaCost(12).cooldownTicks(40)
                    .effect(new ProjectileEffect(ProjectileProfile.of(SpellElement.FIRE, 7.0F)
                            .speed(2.4F).size(0.5F).ignite(3).shape(ProjectileShape.CRESCENT).build(), SoundEvents.BLAZE_SHOOT))
                    .build());

    /** A whip of wind that ties the foe down (wiki, Wind). */
    public static final DeferredHolder<Spell, Spell> WIND_BIND = SPELLS.register("wind_bind",
            () -> Spell.builder(SpellSchool.WIND, Rank.INTERMEDIATE).manaCost(14).cooldownTicks(200)
                    .effect(new ProjectileEffect(ProjectileProfile.of(SpellElement.WIND, 1.0F)
                            .speed(2.0F).size(0.4F).bind(60).build(), SoundEvents.BREEZE_SHOOT))
                    .build());

    /** An earth bullet that bursts into a blinding cloud of sand where it lands (wiki, Sand Cloud). */
    public static final DeferredHolder<Spell, Spell> SAND_CLOUD = SPELLS.register("sand_cloud",
            () -> Spell.builder(SpellSchool.EARTH, Rank.INTERMEDIATE).manaCost(14).cooldownTicks(160)
                    .effect(new ProjectileEffect(ProjectileProfile.of(SpellElement.EARTH, 3.0F)
                            .speed(1.6F).gravity(0.02F).size(0.4F).blindingDust(3.0F).build(), SoundEvents.SAND_BREAK))
                    .build());

    // ---- Mixed magic (wiki, Magic Spells § Melded Magic): not taught, discovered by mastering every part ----

    /** A fog that hides the caster from its enemies (LN vol. 1 ch. 5): Waterfall + Heat Island + Icicle Field. */
    public static final DeferredHolder<Spell, Spell> DEEP_MIST = SPELLS.register("deep_mist",
            () -> Spell.builder(SpellSchool.MIXED, Rank.INTERMEDIATE).intent(SpellIntent.DEFEND).manaCost(25).cooldownTicks(400)
                    .meldedFrom(MeldIngredient.spell(WATERFALL), MeldIngredient.spell(HEAT_ISLAND), MeldIngredient.spell(ICICLE_FIELD))
                    .effect(new ZoneEffect(ZoneBehaviors::deepMist, ZoneVisual.MIST, 7.0, 300, 0.0, SoundEvents.FIRE_EXTINGUISH))
                    .build());

    /** A sudden burst of lava (LN vol. 1 ch. 10): a fire spell + an earth spell. Rank from the glossary. */
    public static final DeferredHolder<Spell, Spell> MAGMA_GUSH = SPELLS.register("magma_gush",
            () -> Spell.builder(SpellSchool.MIXED, Rank.ADVANCED).manaCost(35).cooldownTicks(240)
                    .meldedFrom(MeldIngredient.anyOf(SpellSchool.FIRE), MeldIngredient.anyOf(SpellSchool.EARTH))
                    .effect(new CompositeEffect(
                            new EruptionEffect(null, 3, 12.0F, 0.7, 6, 18.0, ParticleTypes.LAVA, SoundEvents.LAVA_POP)
                                    .lookingLike(EruptionVisual.fireColumn(1.3F, 24)),
                            new ZoneEffect(() -> ZoneBehaviors.moltenGround(3.0F, 4), 2.5, 120, 18.0, SoundEvents.LAVA_AMBIENT)))
                    .build());

    /** The ground turns into a swamp of mud that holds the enemy fast (anime S2): a water spell + an earth spell. */
    public static final DeferredHolder<Spell, Spell> QUAGMIRE = SPELLS.register("quagmire",
            () -> Spell.builder(SpellSchool.MIXED, Rank.INTERMEDIATE).manaCost(20).cooldownTicks(300)
                    .meldedFrom(MeldIngredient.anyOf(SpellSchool.WATER), MeldIngredient.anyOf(SpellSchool.EARTH))
                    .effect(new ZoneEffect(ZoneBehaviors::quagmire, 3.5, 200, 18.0, SoundEvents.MUD_PLACE))
                    .build());

    /** A rain of mud that leaves the ground slippery and loose (LN vol. 17 ch. 2): a water spell + an earth spell. */
    public static final DeferredHolder<Spell, Spell> MUD_RAIN = SPELLS.register("mud_rain",
            () -> Spell.builder(SpellSchool.MIXED, Rank.INTERMEDIATE).manaCost(22).cooldownTicks(400)
                    .meldedFrom(MeldIngredient.anyOf(SpellSchool.WATER), MeldIngredient.anyOf(SpellSchool.EARTH))
                    .effect(new ZoneEffect(ZoneBehaviors::mudRain, 6.0, 300, 20.0, SoundEvents.WEATHER_RAIN))
                    .build());

    /** A blast of hot wind (LN vol. 1 ch. 11): a fire spell + a wind spell. */
    public static final DeferredHolder<Spell, Spell> BLAST = SPELLS.register("blast",
            () -> Spell.builder(SpellSchool.MIXED, Rank.INTERMEDIATE).manaCost(16).cooldownTicks(100)
                    .meldedFrom(MeldIngredient.anyOf(SpellSchool.FIRE), MeldIngredient.anyOf(SpellSchool.WIND))
                    .effect(new ConeBlastEffect(7.0, 30.0, 6.0F, 1.6, MushoParticles.FLAME.get(), SoundEvents.FIRECHARGE_USE)
                            .igniting(3))
                    .build());

    /** Soaks the enemies with droplets, then freezes them through (LN vol. 12 ch. 4): Water Splash + Icicle Field. */
    public static final DeferredHolder<Spell, Spell> FROST_NOVA = SPELLS.register("frost_nova",
            () -> Spell.builder(SpellSchool.MIXED, Rank.ADVANCED).manaCost(35).cooldownTicks(300)
                    .meldedFrom(MeldIngredient.spell(WATER_SPLASH), MeldIngredient.spell(ICICLE_FIELD))
                    // soaked first, so the frost bites twice as hard
                    .effect(new CompositeEffect(
                            new AreaBurstEffect(5.0, 2.0F, 0, 16.0, ParticleTypes.SPLASH, SoundEvents.GENERIC_SPLASH,
                                    List.of(new CloudEffect.Affliction(MushoMobEffects.SOAKED, 200, 0))),
                            new FreezeFieldEffect(5.0, 7.0F, 140, 16.0)))
                    .build());

    /** A strong gust of freezing wind that disables the enemy (LN vol. 5 ch. 1): Icicle Field + Blast. */
    public static final DeferredHolder<Spell, Spell> BORA_BLAST = SPELLS.register("bora_blast",
            () -> Spell.builder(SpellSchool.MIXED, Rank.ADVANCED).manaCost(30).cooldownTicks(240)
                    .meldedFrom(MeldIngredient.spell(ICICLE_FIELD), MeldIngredient.spell(BLAST))
                    .effect(new ConeBlastEffect(9.0, 35.0, 7.0F, 2.0, ParticleTypes.SNOWFLAKE, SoundEvents.POWDER_SNOW_BREAK)
                            .freezing(120, new CloudEffect.Affliction(MobEffects.MOVEMENT_SLOWDOWN, 120, 3),
                                    new CloudEffect.Affliction(MobEffects.DIG_SLOWDOWN, 120, 2)))
                    .build());

    private MushoSpells() {
    }

    public static void register(IEventBus modEventBus) {
        SPELLS.register(modEventBus);
    }
}
