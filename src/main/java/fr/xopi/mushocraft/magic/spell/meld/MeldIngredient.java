package fr.xopi.mushocraft.magic.spell.meld;

import fr.xopi.mushocraft.combat.mastery.MasteryService;
import fr.xopi.mushocraft.core.MushoRegistries;
import fr.xopi.mushocraft.magic.spell.MagicService;
import fr.xopi.mushocraft.magic.spell.Spell;
import fr.xopi.mushocraft.magic.spell.SpellSchool;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

/**
 * One part of a melded spell (wiki, <i>Magic Spells</i> § Melded Magic): either a given spell (Deep Mist needs
 * Waterfall, Heat Island and Icicle Field) or any spell of a school (Quagmire: a water spell and an earth spell).
 * A part counts once the caster has mastered such a spell.
 */
public sealed interface MeldIngredient {
    boolean masteredBy(ServerPlayer player);

    /** « Icicle Field », « a Water spell », for tooltips. */
    Component describe();

    static MeldIngredient spell(Supplier<Spell> spell) {
        return new Exact(spell);
    }

    static MeldIngredient anyOf(SpellSchool school) {
        return new AnyOf(school);
    }

    record Exact(Supplier<Spell> spell) implements MeldIngredient {
        @Override
        public boolean masteredBy(ServerPlayer player) {
            Spell needed = spell.get();
            return MagicService.knows(player, needed) && MasteryService.isMastered(player, needed.ref());
        }

        @Override
        public Component describe() {
            return spell.get().displayName();
        }
    }

    record AnyOf(SpellSchool school) implements MeldIngredient {
        @Override
        public boolean masteredBy(ServerPlayer player) {
            return MagicService.knownSpells(player).stream()
                    .map(MushoRegistries.SPELL::get)
                    .anyMatch(known -> known != null && known.school() == school
                            && MasteryService.isMastered(player, known.ref()));
        }

        @Override
        public Component describe() {
            return Component.translatable("message.mushocraft.magic.meld.any_of", school.displayName());
        }
    }
}
