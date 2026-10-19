package fr.xopi.mushocraft.core;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.body.BodyTraining;
import fr.xopi.mushocraft.body.aura.BattleAuraData;
import fr.xopi.mushocraft.combat.mastery.MoveMastery;
import fr.xopi.mushocraft.combat.move.Cooldowns;
import fr.xopi.mushocraft.combat.move.MoveLoadout;
import fr.xopi.mushocraft.life.LifeData;
import fr.xopi.mushocraft.magic.mana.ManaData;
import fr.xopi.mushocraft.magic.spell.MagicKnowledge;
import fr.xopi.mushocraft.magic.spell.MagicProficiency;
import fr.xopi.mushocraft.magic.spell.ManaControl;
import fr.xopi.mushocraft.magic.summon.SummonBond;
import fr.xopi.mushocraft.sword.SwordMastery;
import fr.xopi.mushocraft.talent.Talents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * Data attached to players (and, for summoned creatures, to them). Everything is saved; player data survives death.
 * Use {@code hasData} to know whether a value was ever set: the defaults below are never meant to be read.
 */
public final class MushoAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MushoCraft.MODID);

    public static final Supplier<AttachmentType<LifeData>> LIFE = ATTACHMENT_TYPES.register("life",
            () -> AttachmentType.builder(LifeData::unborn).serialize(LifeData.CODEC).copyOnDeath().build());

    public static final Supplier<AttachmentType<Talents>> TALENTS = ATTACHMENT_TYPES.register("talents",
            () -> AttachmentType.builder(Talents::neutral).serialize(Talents.CODEC).copyOnDeath().build());

    public static final Supplier<AttachmentType<ManaData>> MANA = ATTACHMENT_TYPES.register("mana",
            () -> AttachmentType.builder(ManaData::empty).serialize(ManaData.CODEC).copyOnDeath().build());

    public static final Supplier<AttachmentType<MagicKnowledge>> MAGIC_KNOWLEDGE = ATTACHMENT_TYPES.register("magic_knowledge",
            () -> AttachmentType.builder(MagicKnowledge::none).serialize(MagicKnowledge.CODEC).copyOnDeath().build());

    public static final Supplier<AttachmentType<MagicProficiency>> MAGIC_PROFICIENCY = ATTACHMENT_TYPES.register("magic_proficiency",
            () -> AttachmentType.builder(MagicProficiency::none).serialize(MagicProficiency.CODEC).copyOnDeath().build());

    public static final Supplier<AttachmentType<MoveLoadout>> LOADOUT = ATTACHMENT_TYPES.register("move_loadout",
            () -> AttachmentType.builder(MoveLoadout::empty).serialize(MoveLoadout.CODEC).copyOnDeath().build());

    public static final Supplier<AttachmentType<Cooldowns>> COOLDOWNS = ATTACHMENT_TYPES.register("cooldowns",
            () -> AttachmentType.builder(Cooldowns::none).serialize(Cooldowns.CODEC).copyOnDeath().build());

    public static final Supplier<AttachmentType<BodyTraining>> BODY_TRAINING = ATTACHMENT_TYPES.register("body_training",
            () -> AttachmentType.builder(BodyTraining::none).serialize(BodyTraining.CODEC).copyOnDeath().build());

    public static final Supplier<AttachmentType<BattleAuraData>> BATTLE_AURA = ATTACHMENT_TYPES.register("battle_aura",
            () -> AttachmentType.builder(BattleAuraData::none).serialize(BattleAuraData.CODEC).copyOnDeath().build());

    public static final Supplier<AttachmentType<SwordMastery>> SWORD_MASTERY = ATTACHMENT_TYPES.register("sword_mastery",
            () -> AttachmentType.builder(SwordMastery::none).serialize(SwordMastery.CODEC).copyOnDeath().build());

    public static final Supplier<AttachmentType<MoveMastery>> MOVE_MASTERY = ATTACHMENT_TYPES.register("move_mastery",
            () -> AttachmentType.builder(MoveMastery::none).serialize(MoveMastery.CODEC).copyOnDeath().build());

    public static final Supplier<AttachmentType<ManaControl>> MANA_CONTROL = ATTACHMENT_TYPES.register("mana_control",
            () -> AttachmentType.builder(ManaControl::none).serialize(ManaControl.CODEC).copyOnDeath().build());

    /** On a summoned creature (not a player): the covenant binding it to its summoner. Saved with the creature. */
    public static final Supplier<AttachmentType<SummonBond>> SUMMON_BOND = ATTACHMENT_TYPES.register("summon_bond",
            () -> AttachmentType.builder(SummonBond::none).serialize(SummonBond.CODEC).build());

    private MushoAttachments() {
    }

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
