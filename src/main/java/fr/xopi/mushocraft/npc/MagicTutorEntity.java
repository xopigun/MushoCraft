package fr.xopi.mushocraft.npc;

import fr.xopi.mushocraft.combat.move.MoveRef;
import fr.xopi.mushocraft.core.MushoRegistries;
import fr.xopi.mushocraft.core.Rank;
import fr.xopi.mushocraft.magic.entity.SpellProjectile;
import fr.xopi.mushocraft.magic.mana.ManaRules;
import fr.xopi.mushocraft.magic.mana.ManaService;
import fr.xopi.mushocraft.magic.spell.MagicProficiency;
import fr.xopi.mushocraft.magic.spell.MagicService;
import fr.xopi.mushocraft.magic.spell.Spell;
import fr.xopi.mushocraft.magic.spell.SpellElement;
import fr.xopi.mushocraft.magic.spell.SpellSchool;
import fr.xopi.mushocraft.magic.spell.effect.ProjectileProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.annotation.Nullable;

/**
 * A magic tutor, teaching one school up to their own rank, the way Roxy teaches Rudeus: one spell at a time, each to be
 * mastered by repetition before the next, a reservoir large enough before each new rank (see {@link TeacherEntity#teach}). Barrier and divine strike
 * tutors are Holy Knights of Milis: only the Holy Knight orders may teach those schools above Beginner (WN ch. 69).
 * When attacked, a tutor answers with bolts of their element.
 */
public class MagicTutorEntity extends TeacherEntity {
    private static final EntityDataAccessor<Integer> SCHOOL = SynchedEntityData.defineId(MagicTutorEntity.class, EntityDataSerializers.INT);
    private static final List<SpellSchool> TEACHABLE = List.of(SpellSchool.WATER, SpellSchool.FIRE, SpellSchool.WIND,
            SpellSchool.EARTH, SpellSchool.HEALING, SpellSchool.DETOXIFICATION, SpellSchool.BARRIER, SpellSchool.DIVINE_STRIKE);

    public MagicTutorEntity(EntityType<? extends MagicTutorEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    public SpellSchool school() {
        return SpellSchool.values()[Math.floorMod(entityData.get(SCHOOL), SpellSchool.values().length)];
    }

    public void setSchool(SpellSchool school) {
        entityData.set(SCHOOL, school.ordinal());
        refreshName();
    }

    /** Holy Knights of Milis guard barrier and divine strike magic. */
    public boolean isMilisKnight() {
        return school() == SpellSchool.BARRIER || school() == SpellSchool.DIVINE_STRIKE;
    }

    @Override
    protected Component title() {
        return Component.translatable(isMilisKnight() ? "entity.mushocraft.magic_tutor.title.milis" : "entity.mushocraft.magic_tutor.title",
                rank().displayName(), school().displayName());
    }

    @Override
    protected Rank pupilRank(ServerPlayer player) {
        return MagicService.proficiency(player).rank(school());
    }

    @Override
    protected List<MoveRef> movesOf(Rank tier) {
        return MushoRegistries.SPELL.stream()
                .filter(spell -> spell.school() == school() && spell.rank() == tier)
                .sorted(Comparator.comparing(spell -> spell.id().toString()))
                .map(Spell::ref)
                .toList();
    }

    /** A tutor first demands a reservoir able to carry the rank's spells. */
    @Override
    protected Optional<Component> unreadyFor(ServerPlayer player, Rank tier) {
        double capacity = ManaService.snapshot(player).capacity();
        double required = ManaRules.capacityToStudy(tier);
        if (capacity >= required) {
            return Optional.empty();
        }
        return Optional.of(Component.translatable("message.mushocraft.tutor.mana", tier.displayName(),
                String.format(Locale.ROOT, "%.0f", capacity), String.format(Locale.ROOT, "%.0f", required)));
    }

    @Override
    protected boolean knows(ServerPlayer player, MoveRef move) {
        return MagicService.knows(player, move);
    }

    @Override
    protected void show(ServerPlayer player, MoveRef move) {
        Optional.ofNullable(MushoRegistries.SPELL.get(move.id())).ifPresent(spell -> MagicService.learn(player, spell));
    }

    /** Every spell of the rank mastered: the school reaches the rank (its experience is raised to the threshold). */
    @Override
    protected Optional<Component> grant(ServerPlayer player, Rank tier) {
        double threshold = MagicProficiency.threshold(tier);
        double xp = MagicService.proficiency(player).experience(school());
        if (xp < threshold) {
            MagicService.gainExperience(player, school(), threshold - xp);
        }
        return Optional.empty();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(2, new CastAtTargetGoal(this));
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                        @Nullable SpawnGroupData data) {
        RandomSource random = level.getRandom();
        setSchool(TEACHABLE.get(random.nextInt(TEACHABLE.size())));
        setRank(random.nextInt(100) < 70 ? Rank.INTERMEDIATE : Rank.ADVANCED);
        return super.finalizeSpawn(level, difficulty, spawnType, data);
    }

    private SpellElement element() {
        return switch (school()) {
            case FIRE -> SpellElement.FIRE;
            case EARTH -> SpellElement.EARTH;
            case WIND -> SpellElement.WIND;
            case DIVINE_STRIKE, BARRIER, HEALING, DETOXIFICATION -> SpellElement.LIGHT;
            default -> SpellElement.WATER;
        };
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SCHOOL, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("school", school().getSerializedName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        String name = tag.getString("school");
        Arrays.stream(SpellSchool.values()).filter(school -> school.getSerializedName().equals(name)).findFirst()
                .ifPresent(this::setSchool);
    }

    /** Keeps some distance and hurls a bolt of the tutor's element every few seconds. */
    private static final class CastAtTargetGoal extends Goal {
        private static final int CAST_INTERVAL_TICKS = 40;
        private static final double CAST_RANGE = 16.0;

        private final MagicTutorEntity tutor;
        private int cooldown;

        private CastAtTargetGoal(MagicTutorEntity tutor) {
            this.tutor = tutor;
            setFlags(EnumSet.of(Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = tutor.getTarget();
            return target != null && target.isAlive() && tutor.distanceTo(target) < CAST_RANGE;
        }

        @Override
        public void tick() {
            LivingEntity target = tutor.getTarget();
            if (target == null) {
                return;
            }
            tutor.getLookControl().setLookAt(target);
            if (--cooldown > 0 || !tutor.hasLineOfSight(target)) {
                return;
            }
            cooldown = CAST_INTERVAL_TICKS;
            float damage = 3.0F + 1.5F * tutor.rank().ordinal();
            SpellProjectile bolt = SpellProjectile.create(tutor.level(), tutor,
                    ProjectileProfile.of(tutor.element(), damage).speed(1.4F).size(0.5F).knockback(0.4F).build());
            Vec3 start = tutor.getEyePosition().add(tutor.getLookAngle().scale(0.8));
            Vec3 aim = target.getEyePosition().subtract(start);
            bolt.setPos(start);
            bolt.shoot(aim.x, aim.y, aim.z, 1.4F, 2.0F);
            tutor.level().addFreshEntity(bolt);
            tutor.level().playSound(null, tutor.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 0.8F, 1.2F);
        }
    }
}
