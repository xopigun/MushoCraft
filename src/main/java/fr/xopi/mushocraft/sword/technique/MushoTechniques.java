package fr.xopi.mushocraft.sword.technique;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.core.MushoRegistries;
import fr.xopi.mushocraft.core.Rank;
import fr.xopi.mushocraft.sword.SwordStyle;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import yesman.epicfight.gameasset.Animations;

/**
 * Every sword technique of the mod. Names follow docs/GLOSSAIRE.md; the description of each one comes from the wiki
 * page of its style. Ranks are given by the wiki where it says so, otherwise they are choices (docs/ECARTS.md).
 * Animations are Epic Fight's own, referenced and never copied.
 */
public final class MushoTechniques {
    private static final DeferredRegister<Technique> TECHNIQUES = DeferredRegister.create(MushoRegistries.TECHNIQUE, MushoCraft.MODID);

    private static final int DISARM_WEAKNESS_TICKS = 100;

    // ---- Sword God Style: « victory goes to he who moves first » ----

    /**
     * Initial Strike: aims at the forearm to cut it off or break it, so the arm cannot hold a weapon any more.
     * A basic technique, practised by Beginners in hundreds of thousands of swings.
     */
    public static final DeferredHolder<Technique, Technique> ARM_CHOP = TECHNIQUES.register("arm_chop",
            () -> Technique.builder(SwordStyle.SWORD_GOD, Rank.BEGINNER).cooldownTicks(60)
                    .action(StrikeAction.of(() -> Animations.SWORD_AUTO1, 1.3F).onHit(MushoTechniques::disarm).build())
                    .build());

    /** An advanced, slower variant of the Longsword of Light, beyond the speed of sound: it leaves no sound behind. */
    public static final DeferredHolder<Technique, Technique> LONGSWORD_OF_SILENCE = TECHNIQUES.register("longsword_of_silence",
            () -> Technique.builder(SwordStyle.SWORD_GOD, Rank.ADVANCED).cooldownTicks(160)
                    .action(StrikeAction.of(() -> Animations.BATTOJUTSU, 2.2F).build())
                    .build());

    /**
     * The ultimate technique: all the force in a single two-handed swing, near the speed of light. Being able to use it
     * is part of what makes a standard Sword Saint. Forbidden until the sword is mastered with both hands.
     */
    public static final DeferredHolder<Technique, Technique> LONGSWORD_OF_LIGHT = TECHNIQUES.register("longsword_of_light",
            () -> Technique.builder(SwordStyle.SWORD_GOD, Rank.SAINT).cooldownTicks(400)
                    .weapon(WeaponRequirement.TWO_HANDED_BLADE)
                    .action(StrikeAction.of(() -> Animations.BATTOJUTSU_DASH, 4.0F).lunge(0.8).build())
                    .build());

    // ---- Water God Style: defence and counter-attack ----

    /**
     * The fundamental counter: receive the attack with the weapon, redirect its force and strike back.
     * From Saint upward the stance also turns away missiles and spells.
     */
    public static final DeferredHolder<Technique, Technique> FLOW = TECHNIQUES.register("flow",
            () -> Technique.builder(SwordStyle.WATER_GOD, Rank.BEGINNER).cooldownTicks(80)
                    .action(new CounterStanceAction(15, 1.5F, Rank.SAINT))
                    .build());

    // ---- North God Style: survive, adapt, use anything ----

    /** When cornered, throw the weapon at high speed, blade whirling: enough force to cut through a Fireball. */
    public static final DeferredHolder<Technique, Technique> SWORD_THROWING = TECHNIQUES.register("sword_throwing",
            () -> Technique.builder(SwordStyle.NORTH_GOD, Rank.BEGINNER).cooldownTicks(80)
                    .weapon(WeaponRequirement.ANY_WEAPON)
                    .action(new ThrowWeaponAction(1.5F, 2.0F))
                    .build());

    /** Charge low on arms and legs like a beast, a smaller target, drawing the weapon when closing in (Paul). */
    public static final DeferredHolder<Technique, Technique> FOUR_LEGGED_FORM = TECHNIQUES.register("four_legged_form",
            () -> Technique.builder(SwordStyle.NORTH_GOD, Rank.INTERMEDIATE).cooldownTicks(120)
                    .weapon(WeaponRequirement.ANY_WEAPON)
                    .action(StrikeAction.of(() -> Animations.SWORD_DASH, 1.6F).lunge(1.2).build())
                    .build());

    /** A secret technique: an upward swing so violent that its shockwave sweeps away everyone around. */
    public static final DeferredHolder<Technique, Technique> MOONLIGHT_CLEAVE = TECHNIQUES.register("moonlight_cleave",
            () -> Technique.builder(SwordStyle.NORTH_GOD, Rank.KING).cooldownTicks(600)
                    .weapon(WeaponRequirement.ANY_WEAPON)
                    .action(new ShockwaveAction(() -> Animations.SWEEPING_EDGE, 7.0, 2.5F, 1.5))
                    .build());

    private MushoTechniques() {
    }

    public static void register(IEventBus modEventBus) {
        TECHNIQUES.register(modEventBus);
    }

    /** The struck arm can no longer hold a weapon: creatures drop what they hold, everyone is weakened for a while. */
    private static void disarm(LivingEntity target) {
        if (target instanceof Mob mob && !mob.getMainHandItem().isEmpty()) {
            mob.spawnAtLocation(mob.getMainHandItem().copy());
            mob.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, DISARM_WEAKNESS_TICKS, 1));
    }
}
