package fr.xopi.mushocraft.combat.move;

import fr.xopi.mushocraft.combat.mastery.MasteryCurve;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Anything that can sit in a slot of the move bar: a spell or a sword technique.
 * The move bar and its cooldowns only know this interface (Command pattern: the bar triggers, the move acts).
 */
public interface Move {
    MoveRef ref();

    Component displayName();

    /** Texture drawn in the move bar slot (16×16). */
    ResourceLocation icon();

    /**
     * What the cooldown is attached to. A spell has its own; the techniques of a sword style share their style's.
     */
    ResourceLocation cooldownKey();

    /** Server side: try to use the move. Applying the cooldown is the move's responsibility. */
    MoveUseResult use(ServerPlayer user);

    /** How practice masters this move; most moves follow the standard curve. */
    default MasteryCurve masteryCurve() {
        return MasteryCurve.STANDARD;
    }
}
