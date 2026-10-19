package fr.xopi.mushocraft.core;

import fr.xopi.mushocraft.MushoCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The MushoCraft creative tab.
 */
public final class MushoCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MushoCraft.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.mushocraft"))
                    .icon(() -> MushoItems.MAGIC_TEXTBOOK.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(MushoItems.MAGIC_TEXTBOOK.get());
                        output.accept(MushoItems.HEALING_PRIMER.get());
                        output.accept(MushoItems.MILIS_PRAYER_BOOK.get());
                        output.accept(MushoItems.INTERMEDIATE_ATTACK_TREATISE.get());
                        output.accept(MushoItems.INTERMEDIATE_HEALING_TREATISE.get());
                        output.accept(MushoItems.APPRENTICE_WAND.get());
                        output.accept(MushoItems.MAGE_STAFF.get());
                        output.accept(MushoItems.AQUA_HEARTIA.get());
                        output.accept(MushoItems.LAMPLIGHT_SUMMONING_PAPER.get());
                        output.accept(MushoItems.SPIRIT_SUMMONING_PAPER.get());
                        output.accept(MushoItems.BEAST_SUMMONING_PAPER.get());
                        output.accept(MushoItems.SWORD_MASTER_SPAWN_EGG.get());
                        output.accept(MushoItems.MAGIC_TUTOR_SPAWN_EGG.get());
                        output.accept(MushoItems.ASSAULT_DOG_SPAWN_EGG.get());
                    })
                    .build());

    private MushoCreativeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }
}
