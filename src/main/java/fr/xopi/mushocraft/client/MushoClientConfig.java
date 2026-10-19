package fr.xopi.mushocraft.client;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Client configuration: each player's own comfort settings.
 */
public final class MushoClientConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.DoubleValue CAMERA_SHAKE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("effects");
        CAMERA_SHAKE = builder
                .comment("How hard the view shakes near violent spells (explosions, lightning). 0 turns it off.")
                .defineInRange("cameraShake", 1.0, 0.0, 2.0);
        builder.pop();
        SPEC = builder.build();
    }

    private MushoClientConfig() {
    }
}
