package fr.xopi.mushocraft.combat.move;

import net.minecraft.network.chat.Component;

import java.util.Optional;

/**
 * Outcome of pressing a move key, with the reason shown to the player when it fails.
 */
public record MoveUseResult(boolean success, Optional<Component> failure) {
    public static final MoveUseResult SUCCESS = new MoveUseResult(true, Optional.empty());

    public static MoveUseResult fail(String translationKey, Object... args) {
        return new MoveUseResult(false, Optional.of(Component.translatable(translationKey, args)));
    }
}
