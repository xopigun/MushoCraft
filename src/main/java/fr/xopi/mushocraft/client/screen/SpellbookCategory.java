package fr.xopi.mushocraft.client.screen;

import fr.xopi.mushocraft.combat.move.Move;
import fr.xopi.mushocraft.core.MushoRegistries;
import fr.xopi.mushocraft.core.Rank;
import fr.xopi.mushocraft.magic.spell.Spell;
import fr.xopi.mushocraft.magic.spell.SpellSchool;
import fr.xopi.mushocraft.sword.SwordStyle;
import fr.xopi.mushocraft.sword.technique.Technique;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * One ribbon of the spellbook: a school of magic or a sword style, with all of its moves sorted by rank.
 *
 * @param icon the icon shown on the ribbon (the category's first move)
 */
public record SpellbookCategory(Component title, ResourceLocation icon, List<Entry> entries) {
    /** A move of the category and the rank it belongs to. */
    public record Entry(Move move, Rank rank) {
    }

    /** Every school that has spells, then every sword style that has techniques. */
    public static List<SpellbookCategory> all() {
        List<SpellbookCategory> categories = new ArrayList<>();
        for (SpellSchool school : SpellSchool.values()) {
            List<Entry> entries = MushoRegistries.SPELL.stream()
                    .filter(spell -> spell.school() == school)
                    .sorted(Comparator.comparing(Spell::rank).thenComparing(spell -> spell.id().toString()))
                    .map(spell -> new Entry(spell, spell.rank()))
                    .toList();
            if (!entries.isEmpty()) {
                categories.add(new SpellbookCategory(school.displayName(), entries.getFirst().move().icon(), entries));
            }
        }
        for (SwordStyle style : SwordStyle.values()) {
            List<Entry> entries = MushoRegistries.TECHNIQUE.stream()
                    .filter(technique -> technique.style() == style)
                    .sorted(Comparator.comparing(Technique::rank).thenComparing(technique -> technique.id().toString()))
                    .map(technique -> new Entry(technique, technique.rank()))
                    .toList();
            if (!entries.isEmpty()) {
                categories.add(new SpellbookCategory(style.displayName(), entries.getFirst().move().icon(), entries));
            }
        }
        return categories;
    }
}
