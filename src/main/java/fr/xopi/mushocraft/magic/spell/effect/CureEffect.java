package fr.xopi.mushocraft.magic.spell.effect;

import fr.xopi.mushocraft.magic.fx.SpellFx;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import fr.xopi.mushocraft.magic.spell.SpellEffect;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;

import java.util.List;

/**
 * Removes poisons and illnesses from the caster (Detoxification). Most conditions are curable at the lowest rank
 * (wiki, page <i>Detoxification Magic</i>); stronger poisons will need higher-ranked spells.
 */
public final class CureEffect implements SpellEffect {
    private final List<Holder<MobEffect>> cured;

    public CureEffect(List<Holder<MobEffect>> cured) {
        this.cured = List.copyOf(cured);
    }

    @Override
    public boolean cast(SpellContext context) {
        cured.forEach(context.caster()::removeEffect);
        // a pale, cleansing light
        SpellFx.healingLight(context.level(), context.caster(), 0xD8F0FF);
        context.level().playSound(null, context.caster().blockPosition(), SoundEvents.BREWING_STAND_BREW,
                SoundSource.PLAYERS, 0.8F, 1.4F);
        return true;
    }
}
