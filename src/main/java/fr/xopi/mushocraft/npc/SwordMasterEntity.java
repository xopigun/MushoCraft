package fr.xopi.mushocraft.npc;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.body.BodyRules;
import fr.xopi.mushocraft.body.BodyService;
import fr.xopi.mushocraft.combat.move.MoveRef;
import fr.xopi.mushocraft.core.MushoRegistries;
import fr.xopi.mushocraft.core.Rank;
import fr.xopi.mushocraft.sword.StyleRules;
import fr.xopi.mushocraft.sword.SwordService;
import fr.xopi.mushocraft.sword.SwordStyle;
import fr.xopi.mushocraft.sword.technique.Technique;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import yesman.epicfight.registry.entries.EpicFightItems;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.annotation.Nullable;

/**
 * A master of one of the three sword styles (wiki: each style has practitioners of every rank). A master takes the
 * player as a disciple and leads them rank by rank up to their own: a trained body first, then each technique of the
 * rank shown and mastered by repetition, then the rank itself (see {@link TeacherEntity#teach}).
 * Each style carries its own weapon, which also gives the master Epic Fight's matching combat behaviour:
 * a two-handed tachi for the Sword God Style, sword and shield for the Water God Style, two swords for the North God.
 */
public class SwordMasterEntity extends TeacherEntity {
    private static final EntityDataAccessor<Integer> STYLE = SynchedEntityData.defineId(SwordMasterEntity.class, EntityDataSerializers.INT);
    private static final ResourceLocation RANK_HEALTH = MushoCraft.id("master/rank_health");

    public SwordMasterEntity(EntityType<? extends SwordMasterEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    public SwordStyle style() {
        return SwordStyle.values()[Math.floorMod(entityData.get(STYLE), SwordStyle.values().length)];
    }

    public void setStyle(SwordStyle style) {
        entityData.set(STYLE, style.ordinal());
        equipFor(style);
        refreshName();
    }

    @Override
    public void setRank(Rank rank) {
        super.setRank(rank);
        // Higher ranks are tougher opponents: +10 health per rank above Beginner.
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.addOrUpdateTransientModifier(new AttributeModifier(RANK_HEALTH, 10.0 * rank.ordinal(),
                    AttributeModifier.Operation.ADD_VALUE));
            setHealth(getMaxHealth());
        }
    }

    private void equipFor(SwordStyle style) {
        switch (style) {
            case SWORD_GOD -> {
                setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(EpicFightItems.IRON_TACHI.get()));
                setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            }
            case WATER_GOD -> {
                setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
            }
            case NORTH_GOD -> {
                setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.IRON_SWORD));
            }
        }
        for (EquipmentSlot slot : List.of(EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND)) {
            setDropChance(slot, 0.0F);
        }
    }

    @Override
    protected Component title() {
        return Component.translatable("entity.mushocraft.sword_master.title", rank().displayName(), style().displayName());
    }

    /** Takes the player as a disciple of the style, and makes them fight in it. */
    @Override
    protected void welcome(ServerPlayer player) {
        SwordStyle style = style();
        if (!SwordService.mastery(player).practises(style)) {
            SwordService.adopt(player, style);
            say(player, Component.translatable("message.mushocraft.master.disciple", style.displayName()));
        } else if (SwordService.activeStyle(player).filter(style::equals).isEmpty()) {
            SwordService.adopt(player, style);
        }
    }

    @Override
    protected Rank pupilRank(ServerPlayer player) {
        return SwordService.rank(player, style()).orElse(Rank.BEGINNER);
    }

    @Override
    protected List<MoveRef> movesOf(Rank tier) {
        return MushoRegistries.TECHNIQUE.stream()
                .filter(technique -> technique.style() == style() && technique.rank() == tier)
                .sorted(Comparator.comparing(technique -> technique.id().toString()))
                .map(Technique::ref)
                .toList();
    }

    /** A master first demands a trained body (gameplay choice from Xopi's brief: physical training first). */
    @Override
    protected Optional<Component> unreadyFor(ServerPlayer player, Rank tier) {
        double level = BodyService.bodyLevel(player);
        double required = BodyRules.requiredBodyLevel(tier);
        if (level >= required) {
            return Optional.empty();
        }
        return Optional.of(Component.translatable("message.mushocraft.master.body", tier.displayName(),
                percent(level), percent(required)));
    }

    @Override
    protected boolean knows(ServerPlayer player, MoveRef move) {
        return technique(move).map(technique -> SwordService.knows(player, technique)).orElse(true);
    }

    @Override
    protected void show(ServerPlayer player, MoveRef move) {
        technique(move).ifPresent(technique -> SwordService.learn(player, technique));
    }

    @Override
    protected Optional<Component> grant(ServerPlayer player, Rank tier) {
        return switch (SwordService.promoteTo(player, style(), tier)) {
            case NEEDS_AURA -> Optional.of(Component.translatable("message.mushocraft.master.aura",
                    StyleRules.requiredAura(tier).displayName(), tier.displayName()));
            case PROMOTED, NOT_PRACTISED, NOT_NEXT -> Optional.empty();
        };
    }

    private static Optional<Technique> technique(MoveRef move) {
        return Optional.ofNullable(MushoRegistries.TECHNIQUE.get(move.id()));
    }

    private static String percent(double share) {
        return String.format(Locale.ROOT, "%.0f", share * 100.0);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                        @Nullable SpawnGroupData data) {
        RandomSource random = level.getRandom();
        setStyle(SwordStyle.values()[random.nextInt(SwordStyle.values().length)]);
        setRank(randomRank(random));
        return super.finalizeSpawn(level, difficulty, spawnType, data);
    }

    /** Masters met by chance are mostly Intermediate or Advanced; Saints are rare, Kings very rare, none higher. */
    private static Rank randomRank(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 40) {
            return Rank.INTERMEDIATE;
        } else if (roll < 80) {
            return Rank.ADVANCED;
        } else if (roll < 95) {
            return Rank.SAINT;
        }
        return Rank.KING;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STYLE, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("style", style().getSerializedName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        String name = tag.getString("style");
        Arrays.stream(SwordStyle.values()).filter(style -> style.getSerializedName().equals(name)).findFirst()
                .ifPresent(this::setStyle);
    }
}
