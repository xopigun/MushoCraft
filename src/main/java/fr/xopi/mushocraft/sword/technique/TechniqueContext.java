package fr.xopi.mushocraft.sword.technique;

import fr.xopi.mushocraft.core.Rank;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * One use of a technique.
 *
 * @param styleRank the user's rank in the technique's style
 * @param power     multiplier on the technique's strength, grows with the rank above the technique's own
 */
public record TechniqueContext(ServerPlayer user, Technique technique, Rank styleRank, double power) {
    public ServerLevel level() {
        return user.serverLevel();
    }
}
