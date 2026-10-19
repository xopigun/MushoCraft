package fr.xopi.mushocraft.magic.spell;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.core.MushoBlocks;
import fr.xopi.mushocraft.core.MushoItems;
import fr.xopi.mushocraft.core.MushoMobEffects;
import fr.xopi.mushocraft.core.MushoRegistries;
import fr.xopi.mushocraft.core.Rank;
import fr.xopi.mushocraft.magic.spell.effect.BurstEffect;
import fr.xopi.mushocraft.magic.spell.effect.CloudEffect;
import fr.xopi.mushocraft.magic.spell.effect.CompositeEffect;
import fr.xopi.mushocraft.magic.spell.effect.ConeBlastEffect;
import fr.xopi.mushocraft.magic.spell.effect.ConjureItemEffect;
import fr.xopi.mushocraft.magic.spell.effect.CureEffect;
import fr.xopi.mushocraft.magic.spell.effect.EruptionEffect;
import fr.xopi.mushocraft.magic.spell.effect.ExplosionEffect;
import fr.xopi.mushocraft.magic.spell.effect.FallingProjectileEffect;
import fr.xopi.mushocraft.magic.spell.effect.FortressEffect;
import fr.xopi.mushocraft.magic.spell.effect.HealEffect;
import fr.xopi.mushocraft.magic.spell.effect.PiercingRayEffect;
import fr.xopi.mushocraft.magic.spell.effect.ProjectileEffect;
import fr.xopi.mushocraft.magic.spell.effect.ProjectileProfile;
import fr.xopi.mushocraft.magic.spell.effect.ScatteredEruptionEffect;
import fr.xopi.mushocraft.magic.spell.effect.ScorchCasterEffect;
import fr.xopi.mushocraft.magic.spell.effect.SelfStatusEffect;
import fr.xopi.mushocraft.magic.spell.effect.WallEffect;
import fr.xopi.mushocraft.magic.spell.effect.WeatherEffect;
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
            () -> Spell.builder(SpellSchool.WATER, Rank.BEGINNER).manaCost(8).cooldownTicks(100)
                    .effect(new WallEffect(MushoBlocks.WATER_WALL, 1, 3, 2, 200, SoundEvents.BUCKET_EMPTY))
                    .build());

    /** Launches a bolt of water, roughly twenty centimeters long. */
    public static final DeferredHolder<Spell, Spell> WATER_ARROW = SPELLS.register("water_arrow",
            () -> Spell.builder(SpellSchool.WATER, Rank.BEGINNER).manaCost(4).cooldownTicks(15)
                    .effect(new ProjectileEffect(ProjectileProfile.of(SpellElement.WATER, 3.0F)
                            .speed(2.4F).size(0.3F).knockback(0.2F).build(), SoundEvents.TRIDENT_THROW.value()))
                    .build());

    /** Strikes an opponent with a mound of ice. */
    public static final DeferredHolder<Spell, Spell> ICE_SMASH = SPELLS.register("ice_smash",
            () -> Spell.builder(SpellSchool.WATER, Rank.BEGINNER).manaCost(6).cooldownTicks(30)
                    .effect(new ProjectileEffect(ProjectileProfile.of(SpellElement.ICE, 5.0F)
                            .speed(1.6F).gravity(0.02F).size(0.7F).knockback(0.4F).slow(60).build(),
                            SoundEvents.GLASS_PLACE))
                    .build());

    /** Creates a sword made out of ice. */
    public static final DeferredHolder<Spell, Spell> ICE_BLADE = SPELLS.register("ice_blade",
            () -> Spell.builder(SpellSchool.WATER, Rank.BEGINNER).manaCost(10).cooldownTicks(200)
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
            () -> Spell.builder(SpellSchool.HEALING, Rank.BEGINNER).manaCost(8).cooldownTicks(40)
                    .effect(new HealEffect(4.0F, 5.0))
                    .build());

    /**
     * Cures poison and sickness. The wiki gives no rank for this spell but says most conditions are curable
     * at the lowest rank: classed Beginner (docs/ECARTS.md, choice to validate).
     */
    public static final DeferredHolder<Spell, Spell> DETOXIFICATION = SPELLS.register("detoxification",
            () -> Spell.builder(SpellSchool.DETOXIFICATION, Rank.BEGINNER).manaCost(8).cooldownTicks(60)
                    .effect(new CureEffect(List.of(MobEffects.POISON, MobEffects.HUNGER, MobEffects.CONFUSION,
                            MobEffects.WEAKNESS)))
                    .build());

    // ---- Barrier and divine strike, Beginner (the only rank Milis lets others teach) ----

    /** Semi-transparent barrier that blocks magical attacks and isolates heat and cold (LN vol. 8 ch. 4). */
    public static final DeferredHolder<Spell, Spell> MAGIC_SHIELD = SPELLS.register("magic_shield",
            () -> Spell.builder(SpellSchool.BARRIER, Rank.BEGINNER).manaCost(10).cooldownTicks(300)
                    .effect(new SelfStatusEffect(MushoMobEffects.MAGIC_SHIELD, 300, 0, SoundEvents.BEACON_ACTIVATE))
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
                            ParticleTypes.SNOWFLAKE, SoundEvents.GLASS_PLACE))
                    .build());

    /** Materializes a pillar of fire (WN ch. 184). */
    public static final DeferredHolder<Spell, Spell> FLAME_PILLAR = SPELLS.register("flame_pillar",
            () -> Spell.builder(SpellSchool.FIRE, Rank.INTERMEDIATE).manaCost(16).cooldownTicks(80)
                    .effect(new EruptionEffect(null, 4, 7.0F, 0.4, 5, 16.0, ParticleTypes.FLAME, SoundEvents.BLAZE_SHOOT))
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
                            .speed(2.2F).size(0.6F).knockback(2.5F).build(), SoundEvents.BREEZE_SHOOT))
                    .build());

    /** Shoots a gust of cutting wind (no source cited by the wiki). */
    public static final DeferredHolder<Spell, Spell> WIND_SLICE = SPELLS.register("wind_slice",
            () -> Spell.builder(SpellSchool.WIND, Rank.INTERMEDIATE).manaCost(12).cooldownTicks(40)
                    .effect(new ProjectileEffect(ProjectileProfile.of(SpellElement.WIND, 7.0F)
                            .speed(2.6F).size(0.4F).build(), SoundEvents.BREEZE_SHOOT))
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
                            ParticleTypes.ASH, SoundEvents.ROOTED_DIRT_PLACE))
                    .build());

    /** Heals wounds as severe as broken bones (LN vol. 2 ch. 2). */
    public static final DeferredHolder<Spell, Spell> X_HEALING = SPELLS.register("x_healing",
            () -> Spell.builder(SpellSchool.HEALING, Rank.INTERMEDIATE).manaCost(18).cooldownTicks(100)
                    .effect(new HealEffect(10.0F, 5.0))
                    .build());

    /**
     * Barrier that blocks physical attacks (LN vol. 4 ch. 6). Above Beginner, only the Holy Knights of Milis may
     * teach barrier magic (WN ch. 69): no book teaches it, a teacher will.
     */
    public static final DeferredHolder<Spell, Spell> PHYSICAL_SHIELD = SPELLS.register("physical_shield",
            () -> Spell.builder(SpellSchool.BARRIER, Rank.INTERMEDIATE).manaCost(20).cooldownTicks(400)
                    .effect(new SelfStatusEffect(MushoMobEffects.PHYSICAL_SHIELD, 300, 0, SoundEvents.ANVIL_PLACE))
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
            () -> Spell.builder(SpellSchool.WATER, Rank.ADVANCED).manaCost(60).cooldownTicks(2400)
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
                    .effect(new ConeBlastEffect(10.0, 35.0, 10.0F, 2.5, ParticleTypes.GUST, SoundEvents.BREEZE_WIND_CHARGE_BURST.value()))
                    .build());

    /** Wind shaped into a spinning drill that punches a hole through the target (LN vol. 16 ch. 7). */
    public static final DeferredHolder<Spell, Spell> TORNADO_IMPACT = SPELLS.register("tornado_impact",
            () -> Spell.builder(SpellSchool.WIND, Rank.ADVANCED).manaCost(35).cooldownTicks(160)
                    .effect(new PiercingRayEffect(24.0, 0.6, 16.0F, ParticleTypes.SWEEP_ATTACK, SoundEvents.BREEZE_SHOOT))
                    .build());

    /** Raises a fortress of earth around the caster (LN vol. 1 ch. 6). */
    public static final DeferredHolder<Spell, Spell> EARTH_FORTRESS = SPELLS.register("earth_fortress",
            () -> Spell.builder(SpellSchool.EARTH, Rank.ADVANCED).manaCost(40).cooldownTicks(600)
                    .effect(new FortressEffect(MushoBlocks.CONJURED_EARTH, 2, 3, 400, SoundEvents.ROOTED_DIRT_PLACE))
                    .build());

    /** Earthen spikes burst from the ground all around the aimed spot (LN vol. 7 ch. 4). */
    public static final DeferredHolder<Spell, Spell> EARTH_HEDGEHOG = SPELLS.register("earth_hedgehog",
            () -> Spell.builder(SpellSchool.EARTH, Rank.ADVANCED).manaCost(40).cooldownTicks(200)
                    .effect(new ScatteredEruptionEffect(new EruptionEffect(MushoBlocks.CONJURED_EARTH, 2, 7.0F, 0.7, 0, 18.0,
                            ParticleTypes.ASH, SoundEvents.ROOTED_DIRT_PLACE), 9, 3.0))
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
            () -> Spell.builder(SpellSchool.HEALING, Rank.ADVANCED).manaCost(35).cooldownTicks(160)
                    .effect(new HealEffect(20.0F, 5.0))
                    .build());

    private MushoSpells() {
    }

    public static void register(IEventBus modEventBus) {
        SPELLS.register(modEventBus);
    }
}
