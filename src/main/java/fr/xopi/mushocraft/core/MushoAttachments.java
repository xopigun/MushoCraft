package fr.xopi.mushocraft.core;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.life.LifeData;
import fr.xopi.mushocraft.magic.mana.ManaData;
import fr.xopi.mushocraft.talent.Talents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * Data attached to players. Everything is saved with the player and survives death.
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

    private MushoAttachments() {
    }

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
