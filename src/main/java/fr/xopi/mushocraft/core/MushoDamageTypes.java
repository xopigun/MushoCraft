package fr.xopi.mushocraft.core;

import fr.xopi.mushocraft.MushoCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * Damage types of the mod. The types themselves are data: {@code data/mushocraft/damage_type/*.json}.
 */
public final class MushoDamageTypes {
    public static final ResourceKey<DamageType> SPELL = ResourceKey.create(Registries.DAMAGE_TYPE, MushoCraft.id("spell"));

    /** Damage that Magic Shield weakens: magic, and the heat and cold it can isolate (wiki, Magic Spells). */
    public static final TagKey<DamageType> BLOCKED_BY_MAGIC_SHIELD =
            TagKey.create(Registries.DAMAGE_TYPE, MushoCraft.id("blocked_by_magic_shield"));

    /** Damage that Physical Shield weakens: blows and missiles (wiki, Magic Spells). */
    public static final TagKey<DamageType> BLOCKED_BY_PHYSICAL_SHIELD =
            TagKey.create(Registries.DAMAGE_TYPE, MushoCraft.id("blocked_by_physical_shield"));

    private MushoDamageTypes() {
    }

    public static DamageSource spell(Level level, Entity direct, @Nullable Entity caster) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(SPELL),
                direct, caster);
    }
}
