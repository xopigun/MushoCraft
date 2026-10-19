package fr.xopi.mushocraft.sword.technique;

import fr.xopi.mushocraft.combat.mastery.MasteryService;
import fr.xopi.mushocraft.combat.move.CooldownService;
import fr.xopi.mushocraft.combat.move.Move;
import fr.xopi.mushocraft.combat.move.MoveRef;
import fr.xopi.mushocraft.combat.move.MoveUseResult;
import fr.xopi.mushocraft.core.MushoRegistries;
import fr.xopi.mushocraft.core.Rank;
import fr.xopi.mushocraft.sword.StyleRules;
import fr.xopi.mushocraft.sword.SwordService;
import fr.xopi.mushocraft.sword.SwordStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;
import java.util.Optional;

/**
 * One special technique of a sword style (Type Object pattern, like {@code Spell}), declared in
 * {@link MushoTechniques}. Every technique of a style shares the style's cooldown, which shrinks with mastery.
 */
public final class Technique implements Move {
    /** Extra strength per rank of the style above the technique's own. */
    private static final double POWER_PER_RANK = 0.1;
    /** Style experience earned each time a technique is performed. */
    private static final double PRACTICE_EXPERIENCE = 5.0;
    /** How long a technique's pace (see {@link TechniqueTempo}) lasts: the length of its animation, roughly. */
    private static final int TEMPO_TICKS = 40;

    private final SwordStyle style;
    private final Rank rank;
    private final int cooldownTicks;
    private final WeaponRequirement weapon;
    private final TechniqueAction action;

    private Technique(Builder builder) {
        this.style = Objects.requireNonNull(builder.style, "style");
        this.rank = Objects.requireNonNull(builder.rank, "rank");
        this.cooldownTicks = builder.cooldownTicks;
        this.weapon = Objects.requireNonNull(builder.weapon, "weapon");
        this.action = Objects.requireNonNull(builder.action, "action");
    }

    public static Builder builder(SwordStyle style, Rank rank) {
        return new Builder(style, rank);
    }

    public ResourceLocation id() {
        return Objects.requireNonNull(MushoRegistries.TECHNIQUE.getKey(this), "Technique used before being registered");
    }

    public SwordStyle style() {
        return style;
    }

    public Rank rank() {
        return rank;
    }

    public int cooldownTicks() {
        return cooldownTicks;
    }

    public WeaponRequirement weapon() {
        return weapon;
    }

    public TechniqueAction action() {
        return action;
    }

    @Override
    public MoveRef ref() {
        return MoveRef.technique(id());
    }

    @Override
    public Component displayName() {
        return Component.translatable("technique." + id().getNamespace() + "." + id().getPath());
    }

    public Component description() {
        return Component.translatable("technique." + id().getNamespace() + "." + id().getPath() + ".desc");
    }

    @Override
    public ResourceLocation icon() {
        return ResourceLocation.fromNamespaceAndPath(id().getNamespace(), "textures/technique/" + id().getPath() + ".png");
    }

    @Override
    public ResourceLocation cooldownKey() {
        return style.cooldownKey();
    }

    @Override
    public MoveUseResult use(ServerPlayer user) {
        if (!SwordService.knows(user, this)) {
            return MoveUseResult.fail("message.mushocraft.technique.unknown", displayName());
        }
        if (!weapon.isMetBy(user)) {
            return MoveUseResult.fail(weapon.failureKey(), displayName());
        }
        Optional<Rank> styleRank = SwordService.rank(user, style);
        if (styleRank.isEmpty()) {
            return MoveUseResult.fail("message.mushocraft.technique.unknown", displayName());
        }
        int cooldown = (int) Math.round(cooldownTicks * StyleRules.cooldownFactor(styleRank.get()));
        if (MasteryService.botches(user, ref())) {
            // Not mastered yet: the technique falls apart, and only half the cooldown is lost.
            CooldownService.start(user, cooldownKey(), cooldown / 2);
            return MoveUseResult.fail("message.mushocraft.move.botched", displayName(), MasteryService.progressText(user, ref()));
        }
        double power = (1.0 + POWER_PER_RANK * Math.max(0, styleRank.get().ordinal() - rank.ordinal()))
                * MasteryService.powerFactor(user, ref());
        TechniqueTempo.apply(user, MasteryService.speedFactor(user, ref()), TEMPO_TICKS);
        if (!action.perform(new TechniqueContext(user, this, styleRank.get(), power))) {
            TechniqueTempo.clear(user);
            return MoveUseResult.fail("message.mushocraft.technique.failed", displayName());
        }
        CooldownService.start(user, cooldownKey(), cooldown);
        SwordService.gainExperience(user, style, PRACTICE_EXPERIENCE);
        MasteryService.succeeded(user, ref());
        return MoveUseResult.SUCCESS;
    }

    public static final class Builder {
        private final SwordStyle style;
        private final Rank rank;
        private int cooldownTicks;
        private WeaponRequirement weapon = WeaponRequirement.BLADE;
        private TechniqueAction action;

        private Builder(SwordStyle style, Rank rank) {
            this.style = style;
            this.rank = rank;
        }

        public Builder cooldownTicks(int value) {
            this.cooldownTicks = value;
            return this;
        }

        public Builder weapon(WeaponRequirement value) {
            this.weapon = value;
            return this;
        }

        public Builder action(TechniqueAction value) {
            this.action = value;
            return this;
        }

        public Technique build() {
            return new Technique(this);
        }
    }
}
