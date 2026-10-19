package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;

import java.util.List;

/**
 * Several effects in one cast (Composite pattern). The main effect is the spell itself: if it cannot take
 * shape, the side effects do not happen either.
 */
public final class CompositeEffect implements SpellEffect {
    private final SpellEffect main;
    private final List<SpellEffect> sideEffects;

    public CompositeEffect(SpellEffect main, SpellEffect... sideEffects) {
        this.main = main;
        this.sideEffects = List.of(sideEffects);
    }

    @Override
    public boolean cast(SpellContext context) {
        if (!main.cast(context)) {
            return false;
        }
        sideEffects.forEach(effect -> effect.cast(context));
        return true;
    }
}
