package fr.xopi.mushocraft.magic.spell.meld;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.combat.mastery.MoveMasteredEvent;
import fr.xopi.mushocraft.core.MushoRegistries;
import fr.xopi.mushocraft.magic.spell.MagicService;
import fr.xopi.mushocraft.magic.spell.Spell;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Mixed magic is not taught: a magician finds it out by melding spells they have mastered, the way Rudeus came to
 * Quagmire and Deep Mist. Each time a move is mastered (and at login, for older saves), every melded spell whose parts
 * are all mastered is learnt, with a message naming what was melded.
 */
@EventBusSubscriber(modid = MushoCraft.MODID)
public final class MeldDiscovery {
    private MeldDiscovery() {
    }

    @SubscribeEvent
    public static void onMoveMastered(MoveMasteredEvent event) {
        discover(event.getEntity());
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            discover(player);
        }
    }

    /**
     * Learns every melded spell this player can now work out. A melded spell can itself be part of another (Bora
     * Blast is melded from Blast): that one waits until the first is mastered in turn.
     *
     * @return the spells discovered
     */
    public static List<Spell> discover(ServerPlayer player) {
        List<Spell> found = new ArrayList<>();
        for (Spell spell : melded()) {
            MeldRecipe recipe = spell.meld().orElseThrow();
            if (!MagicService.knows(player, spell) && recipe.masteredBy(player)) {
                player.sendSystemMessage(Component.translatable("message.mushocraft.magic.meld.discovered",
                        spell.displayName(), recipe.describe()));
                MagicService.learn(player, spell);
                found.add(spell);
            }
        }
        return found;
    }

    private static List<Spell> melded() {
        return MushoRegistries.SPELL.stream()
                .filter(spell -> spell.meld().isPresent())
                .sorted(Comparator.comparing(spell -> spell.id().toString()))
                .toList();
    }
}
