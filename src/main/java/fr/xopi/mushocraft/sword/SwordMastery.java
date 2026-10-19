package fr.xopi.mushocraft.sword;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.xopi.mushocraft.core.Rank;
import net.minecraft.resources.ResourceLocation;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Everything a character has learned of swordsmanship: experience and rank in each style they practise,
 * the techniques they were taught, and the style they currently fight in.
 */
public record SwordMastery(Map<SwordStyle, StyleProgress> styles, Set<ResourceLocation> techniques,
                           Optional<SwordStyle> active) {

    public static final Codec<SwordMastery> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(SwordStyle.CODEC, StyleProgress.CODEC).fieldOf("styles").forGetter(SwordMastery::styles),
            ResourceLocation.CODEC.listOf().xmap(Set::copyOf, List::copyOf)
                    .fieldOf("techniques").forGetter(SwordMastery::techniques),
            SwordStyle.CODEC.optionalFieldOf("active").forGetter(SwordMastery::active)
    ).apply(instance, SwordMastery::new));

    public SwordMastery {
        styles = Map.copyOf(styles);
        techniques = Set.copyOf(techniques);
    }

    public static SwordMastery none() {
        return new SwordMastery(Map.of(), Set.of(), Optional.empty());
    }

    public Optional<StyleProgress> progress(SwordStyle style) {
        return Optional.ofNullable(styles.get(style));
    }

    public boolean practises(SwordStyle style) {
        return styles.containsKey(style);
    }

    public SwordMastery with(SwordStyle style, StyleProgress progress) {
        Map<SwordStyle, StyleProgress> copy = new EnumMap<>(SwordStyle.class);
        copy.putAll(styles);
        copy.put(style, progress);
        return new SwordMastery(copy, techniques, active);
    }

    public SwordMastery withTechnique(ResourceLocation technique) {
        Set<ResourceLocation> copy = new HashSet<>(techniques);
        copy.add(technique);
        return new SwordMastery(styles, copy, active);
    }

    public SwordMastery withActive(SwordStyle style) {
        return new SwordMastery(styles, techniques, Optional.of(style));
    }

    /** Experience and current rank in one style. The rank is stored: past Intermediate, only a master grants it. */
    public record StyleProgress(double experience, Rank rank) {
        public static final Codec<StyleProgress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("experience").forGetter(StyleProgress::experience),
                Rank.CODEC.fieldOf("rank").forGetter(StyleProgress::rank)
        ).apply(instance, StyleProgress::new));

        public static StyleProgress beginner() {
            return new StyleProgress(0.0, Rank.BEGINNER);
        }

        public StyleProgress withExperience(double value) {
            return new StyleProgress(Math.max(0.0, value), rank);
        }

        public StyleProgress withRank(Rank value) {
            return new StyleProgress(experience, value);
        }
    }
}
