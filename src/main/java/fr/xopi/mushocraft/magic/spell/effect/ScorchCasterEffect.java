package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;

/**
 * The heat of a great fire spell burns its own caster, who channels it at close range (Exodus Flame).
 */
public final class ScorchCasterEffect implements SpellEffect {
    private final float seconds;

    public ScorchCasterEffect(float seconds) {
        this.seconds = seconds;
    }

    @Override
    public boolean cast(SpellContext context) {
        if (!context.caster().fireImmune()) {
            context.caster().igniteForSeconds(seconds);
        }
        return true;
    }
}
