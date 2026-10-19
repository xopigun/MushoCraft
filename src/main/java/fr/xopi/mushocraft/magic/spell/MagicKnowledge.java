package fr.xopi.mushocraft.magic.spell;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Spells a character has learned. Everyone has mana, but nobody can cast before learning (decision of 19/10/2026).
 */
public record MagicKnowledge(Set<ResourceLocation> spells) {
    public static final Codec<MagicKnowledge> CODEC = ResourceLocation.CODEC.listOf()
            .xmap(list -> new MagicKnowledge(Set.copyOf(list)), knowledge -> List.copyOf(knowledge.spells()));

    public MagicKnowledge {
        spells = Set.copyOf(spells);
    }

    public static MagicKnowledge none() {
        return new MagicKnowledge(Set.of());
    }

    public boolean knows(ResourceLocation spell) {
        return spells.contains(spell);
    }

    public MagicKnowledge with(ResourceLocation spell) {
        Set<ResourceLocation> copy = new HashSet<>(spells);
        copy.add(spell);
        return new MagicKnowledge(copy);
    }
}
