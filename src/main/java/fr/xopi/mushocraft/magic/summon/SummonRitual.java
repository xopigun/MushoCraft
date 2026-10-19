package fr.xopi.mushocraft.magic.summon;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * What a summoning paper calls forth (Strategy pattern). Summoning is cast through a magic circle drawn in advance
 * (wiki: a summoning circle, or a summoning paper « mostly used in battle »), so there is no incantation; it is easy to
 * learn but « demands immense mana from the master ».
 */
public interface SummonRitual {
    /** Mana poured into the circle to perform it. */
    double manaCost();

    /**
     * Performs the summoning; the mana is already paid.
     *
     * @return false when nothing could be summoned (the mana is then given back)
     */
    boolean perform(ServerPlayer summoner);

    /** For the paper's tooltip. */
    Component description();
}
