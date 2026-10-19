package fr.xopi.mushocraft.magic.spell;

import fr.xopi.mushocraft.combat.move.Move;
import fr.xopi.mushocraft.combat.move.MoveRef;
import fr.xopi.mushocraft.combat.move.MoveUseResult;
import fr.xopi.mushocraft.core.MushoRegistries;
import fr.xopi.mushocraft.core.Rank;
import fr.xopi.mushocraft.magic.spell.meld.MeldIngredient;
import fr.xopi.mushocraft.magic.spell.meld.MeldRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

/**
 * One spell of the novel (Type Object pattern): a single instance per spell, declared in {@link MushoSpells}.
 * Its behaviour is delegated to a {@link SpellEffect}; the casting itself (incantation, mana, cooldown)
 * is handled by {@link SpellCasting}.
 */
public final class Spell implements Move {
    private final SpellSchool school;
    private final Rank rank;
    private final double manaCost;
    private final int incantationTicks;
    private final int cooldownTicks;
    private final SpellEffect effect;
    private final SpellIntent intent;
    @Nullable
    private final MeldRecipe meld;

    private Spell(Builder builder) {
        this.school = Objects.requireNonNull(builder.school, "school");
        this.rank = Objects.requireNonNull(builder.rank, "rank");
        this.manaCost = builder.manaCost;
        this.incantationTicks = builder.incantationTicks >= 0 ? builder.incantationTicks : Incantation.defaultTicks(rank);
        this.cooldownTicks = builder.cooldownTicks;
        this.effect = Objects.requireNonNull(builder.effect, "effect");
        this.intent = builder.intent;
        this.meld = builder.meld;
        if ((school == SpellSchool.MIXED) != (meld != null)) {
            throw new IllegalStateException("Mixed magic, and only it, is melded from other spells");
        }
    }

    public static Builder builder(SpellSchool school, Rank rank) {
        return new Builder(school, rank);
    }

    public ResourceLocation id() {
        return Objects.requireNonNull(MushoRegistries.SPELL.getKey(this), "Spell used before being registered");
    }

    public SpellSchool school() {
        return school;
    }

    public Rank rank() {
        return rank;
    }

    public double manaCost() {
        return manaCost;
    }

    public int incantationTicks() {
        return incantationTicks;
    }

    public int cooldownTicks() {
        return cooldownTicks;
    }

    public SpellEffect effect() {
        return effect;
    }

    public SpellIntent intent() {
        return intent;
    }

    /** For mixed magic: the spells it is melded from. */
    public Optional<MeldRecipe> meld() {
        return Optional.ofNullable(meld);
    }

    @Override
    public MoveRef ref() {
        return MoveRef.spell(id());
    }

    @Override
    public Component displayName() {
        return Component.translatable("spell." + id().getNamespace() + "." + id().getPath());
    }

    public Component description() {
        return Component.translatable("spell." + id().getNamespace() + "." + id().getPath() + ".desc");
    }

    @Override
    public ResourceLocation icon() {
        return ResourceLocation.fromNamespaceAndPath(id().getNamespace(), "textures/spell/" + id().getPath() + ".png");
    }

    @Override
    public ResourceLocation cooldownKey() {
        return id();
    }

    @Override
    public MoveUseResult use(ServerPlayer user) {
        return SpellCasting.begin(user, this);
    }

    public static final class Builder {
        private final SpellSchool school;
        private final Rank rank;
        private double manaCost;
        private int incantationTicks = -1;
        private int cooldownTicks;
        private SpellEffect effect;
        private SpellIntent intent = SpellIntent.ATTACK;
        private MeldRecipe meld;

        private Builder(SpellSchool school, Rank rank) {
            this.school = school;
            this.rank = rank;
        }

        public Builder manaCost(double value) {
            this.manaCost = value;
            return this;
        }

        /** Overrides the incantation length given by the rank. */
        public Builder incantationTicks(int value) {
            this.incantationTicks = value;
            return this;
        }

        public Builder cooldownTicks(int value) {
            this.cooldownTicks = value;
            return this;
        }

        public Builder effect(SpellEffect value) {
            this.effect = value;
            return this;
        }

        /** What the spell is for (default: attack); tells spellcasting creatures when to use it. */
        public Builder intent(SpellIntent value) {
            this.intent = value;
            return this;
        }

        /** Mixed magic: the spells it is melded from; it is discovered by mastering them all. */
        public Builder meldedFrom(MeldIngredient... ingredients) {
            this.meld = MeldRecipe.of(ingredients);
            return this;
        }

        public Spell build() {
            return new Spell(this);
        }
    }
}
