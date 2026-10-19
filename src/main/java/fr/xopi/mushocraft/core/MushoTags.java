package fr.xopi.mushocraft.core;

import fr.xopi.mushocraft.MushoCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

/**
 * Tags of the mod.
 */
public final class MushoTags {
    /**
     * Creatures Monster Summon cannot call: the intelligent humanoid races its covenant forbids (wiki, Magic Spells),
     * and the great bosses.
     */
    public static final TagKey<EntityType<?>> UNSUMMONABLE = TagKey.create(Registries.ENTITY_TYPE, MushoCraft.id("unsummonable"));

    private MushoTags() {
    }
}
