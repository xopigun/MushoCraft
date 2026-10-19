package fr.xopi.mushocraft.body.aura;

import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * How far a character controls their Battle Aura (wiki, page <i>Battle Aura</i>): it can show up before being
 * understood (Eris, still Intermediate), conscious control marks an Advanced swordsman and complete control
 * a Saint (according to Ghislaine, for the Sword God Style).
 */
public enum AuraLevel {
    NONE(0.0, 0.0, 0.0, false),
    UNCONSCIOUS(300.0, 0.15, 0.01, false),
    CONSCIOUS(2_000.0, 0.35, 0.02, true),
    COMPLETE(8_000.0, 0.6, 0.015, true);

    private final double mastery;
    private final double bonus;
    private final double drainPerSecond;
    private final boolean manual;

    /**
     * @param mastery        aura mastery needed to reach the level
     * @param bonus          extra share of each stat's maximal bonus while the aura is up
     * @param drainPerSecond fraction of the mana capacity burnt each second while the aura is up
     * @param manual         whether the aura is raised at will (otherwise it rises by itself in battle)
     */
    AuraLevel(double mastery, double bonus, double drainPerSecond, boolean manual) {
        this.mastery = mastery;
        this.bonus = bonus;
        this.drainPerSecond = drainPerSecond;
        this.manual = manual;
    }

    public static AuraLevel at(double mastery) {
        AuraLevel reached = NONE;
        for (AuraLevel level : values()) {
            if (mastery >= level.mastery) {
                reached = level;
            }
        }
        return reached;
    }

    public double mastery() {
        return mastery;
    }

    public double bonus() {
        return bonus;
    }

    public double drainPerSecond() {
        return drainPerSecond;
    }

    public boolean manual() {
        return manual;
    }

    public Component displayName() {
        return Component.translatable("aura_level.mushocraft." + name().toLowerCase(Locale.ROOT));
    }
}
