package fr.xopi.mushocraft.village;

import fr.xopi.mushocraft.life.LifeClock;
import fr.xopi.mushocraft.life.LifeService;
import fr.xopi.mushocraft.life.LifeStage;
import fr.xopi.mushocraft.talent.GaussianTalentRoller;
import fr.xopi.mushocraft.talent.Talents;
import fr.xopi.mushocraft.village.ai.FollowParentGoal;
import fr.xopi.mushocraft.village.ai.GoHomeAtNightGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * One of the common folk of the Asura Kingdom's villages (wiki, page <i>Buena Village</i>: farming households
 * living far apart among their fields). A person of any age, from babe in arms to elder: they were born on a day of
 * the world's calendar and age and grow as players do ({@link LifeClock}, {@link LifeService#BODY_GROWTH}). They have
 * a name, a family (spouse, parents, children) and a home, which they keep to by day and go back to at night. Epic
 * Fight animates them on its biped skeleton.
 * <p>
 * A house of a village holds one <i>founder</i>: on its first tick it raises its household around it
 * ({@link Household}).
 */
public class TownsfolkEntity extends PathfinderMob {
    private static final EntityDataAccessor<Integer> SEX = SynchedEntityData.defineId(TownsfolkEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(TownsfolkEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STAGE = SynchedEntityData.defineId(TownsfolkEntity.class, EntityDataSerializers.INT);
    /** How far a person strays from home. */
    public static final int HOME_RANGE = 24;
    private static final int REFRESH_TICKS = 20;

    private String givenName = "";
    private String familyName = "";
    private long birthTick;
    private Talents talents = Talents.neutral();
    @Nullable
    private BlockPos home;
    @Nullable
    private UUID spouse;
    private final List<UUID> parents = new ArrayList<>();
    private final List<UUID> children = new ArrayList<>();
    /** A founder raises its household on its first tick (a house placed by a village carries one). */
    private boolean founder;

    public TownsfolkEntity(EntityType<? extends TownsfolkEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        if (getNavigation() instanceof GroundPathNavigation ground) {
            ground.setCanOpenDoors(true);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    // ---- identity

    /** Gives a newcomer to the world a whole identity: who they are, how old, how gifted. */
    public void becomePerson(Sex sex, String given, String family, double ageInYears) {
        entityData.set(SEX, sex.ordinal());
        entityData.set(VARIANT, getRandom().nextInt(1 << 16));
        this.givenName = given;
        this.familyName = family;
        setAge(ageInYears);
        talents = GaussianTalentRoller.fromConfig().roll(getRandom());
        refreshName();
    }

    public Sex sex() {
        return Sex.values()[Math.floorMod(entityData.get(SEX), Sex.values().length)];
    }

    public int variant() {
        return entityData.get(VARIANT);
    }

    public String givenName() {
        return givenName;
    }

    public String familyName() {
        return familyName;
    }

    public String fullName() {
        return familyName.isEmpty() ? givenName : givenName + " " + familyName;
    }

    public Talents talents() {
        return talents;
    }

    public void setTalents(Talents talents) {
        this.talents = talents;
    }

    private void refreshName() {
        setCustomName(Component.literal(fullName()));
    }

    // ---- age

    public long birthTick() {
        return birthTick;
    }

    public double ageInYears() {
        return LifeClock.fromConfig().toYears(now() - birthTick);
    }

    public void setAge(double years) {
        birthTick = now() - LifeClock.fromConfig().toTicks(years);
        refreshAge();
    }

    /** Synchronised to clients, which pick the outfit by it. */
    public LifeStage stage() {
        return LifeStage.values()[Math.floorMod(entityData.get(STAGE), LifeStage.values().length)];
    }

    public boolean isAdult() {
        LifeStage stage = stage();
        return stage == LifeStage.ADULT || stage == LifeStage.ELDER;
    }

    private void refreshAge() {
        if (level().isClientSide) {
            return;
        }
        double age = ageInYears();
        entityData.set(STAGE, LifeStage.at(age).ordinal());
        LifeService.BODY_GROWTH.apply(this, age);
    }

    private long now() {
        return level() instanceof ServerLevel server ? server.getServer().overworld().getGameTime() : level().getGameTime();
    }

    // ---- home and family

    public Optional<BlockPos> home() {
        return Optional.ofNullable(home);
    }

    public void settleAt(BlockPos home) {
        this.home = home.immutable();
        restrictTo(this.home, HOME_RANGE);
    }

    public Optional<UUID> spouse() {
        return Optional.ofNullable(spouse);
    }

    public void marry(@Nullable UUID spouse) {
        this.spouse = spouse;
    }

    public List<UUID> parents() {
        return List.copyOf(parents);
    }

    public List<UUID> children() {
        return List.copyOf(children);
    }

    public void addParent(UUID parent) {
        if (!parents.contains(parent)) {
            parents.add(parent);
        }
    }

    public void addChild(UUID child) {
        if (!children.contains(child)) {
            children.add(child);
        }
    }

    public void setFounder(boolean founder) {
        this.founder = founder;
    }

    // ---- life

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Monster.class, 10.0F, 1.0, 1.3));
        goalSelector.addGoal(3, new GoHomeAtNightGoal(this, 1.0));
        goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));
        goalSelector.addGoal(5, new OpenDoorGoal(this, true));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            if (founder) {
                founder = false;
                Household.found(this);
            }
            if (tickCount % REFRESH_TICKS == 0) {
                refreshAge();
            }
        }
    }

    /** Talking to someone: who they are. Marriage and family come later through {@link VillageLife}. */
    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            VillageLife.talk(serverPlayer, this);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (level() instanceof ServerLevel server) {
            VillageLife.mourn(server, this);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }

    // ---- saving

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SEX, Sex.MALE.ordinal());
        builder.define(VARIANT, 0);
        builder.define(STAGE, LifeStage.ADULT.ordinal());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Sex", sex().getSerializedName());
        tag.putInt("Variant", variant());
        tag.putString("GivenName", givenName);
        tag.putString("FamilyName", familyName);
        tag.putLong("BirthTick", birthTick);
        Talents.CODEC.encodeStart(NbtOps.INSTANCE, talents).ifSuccess(encoded -> tag.put("Talents", encoded));
        if (home != null) {
            tag.put("Home", NbtUtils.writeBlockPos(home));
        }
        if (spouse != null) {
            tag.putUUID("Spouse", spouse);
        }
        tag.put("Parents", uuids(parents));
        tag.put("Children", uuids(children));
        if (founder) {
            tag.putBoolean("Founder", true);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Sex")) {
            entityData.set(SEX, "female".equals(tag.getString("Sex")) ? Sex.FEMALE.ordinal() : Sex.MALE.ordinal());
        }
        entityData.set(VARIANT, tag.getInt("Variant"));
        givenName = tag.getString("GivenName");
        familyName = tag.getString("FamilyName");
        birthTick = tag.getLong("BirthTick");
        if (tag.contains("Talents")) {
            Talents.CODEC.parse(NbtOps.INSTANCE, tag.get("Talents")).ifSuccess(read -> talents = read);
        }
        NbtUtils.readBlockPos(tag, "Home").ifPresent(this::settleAt);
        spouse = tag.hasUUID("Spouse") ? tag.getUUID("Spouse") : null;
        parents.clear();
        parents.addAll(readUuids(tag.getList("Parents", Tag.TAG_INT_ARRAY)));
        children.clear();
        children.addAll(readUuids(tag.getList("Children", Tag.TAG_INT_ARRAY)));
        founder = tag.getBoolean("Founder");
        if (!givenName.isEmpty()) {
            refreshName();
        }
    }

    private static ListTag uuids(List<UUID> ids) {
        ListTag list = new ListTag();
        ids.forEach(id -> list.add(NbtUtils.createUUID(id)));
        return list;
    }

    private static List<UUID> readUuids(ListTag list) {
        List<UUID> ids = new ArrayList<>();
        list.forEach(entry -> ids.add(NbtUtils.loadUUID(entry)));
        return ids;
    }
}
