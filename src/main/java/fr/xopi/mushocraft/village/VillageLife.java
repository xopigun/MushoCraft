package fr.xopi.mushocraft.village;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * What happens between players and the folk of the villages: talking with them, and the grief of a family when one of
 * them dies.
 */
public final class VillageLife {
    private VillageLife() {
    }

    /** A word with someone: who they are. */
    public static void talk(ServerPlayer player, TownsfolkEntity person) {
        person.getLookControl().setLookAt(player);
        player.sendSystemMessage(Component.translatable("message.mushocraft.townsfolk.introduces", person.fullName(),
                (int) Math.floor(person.ageInYears()), person.stage().displayName()));
    }

    /** One of the folk has died: their spouse is a widow or widower. */
    public static void mourn(ServerLevel level, TownsfolkEntity person) {
        person.spouse().map(level::getEntity).ifPresent(spouse -> {
            if (spouse instanceof TownsfolkEntity widowed) {
                widowed.marry(null);
            }
        });
    }
}
