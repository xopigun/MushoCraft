package fr.xopi.mushocraft.magic.summon;

import fr.xopi.mushocraft.core.MushoBlocks;
import fr.xopi.mushocraft.core.MushoEntities;
import fr.xopi.mushocraft.magic.block.SpiritLightBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * A lamplight spirit (wiki, <i>Magic Spells</i> § Summoning Magic): a radiant spirit that flies up and circles its
 * caster, lighting the dark better than a torch and leaving both hands free. It costs almost no mana and lasts long.
 * It sheds real light through {@link SpiritLightBlock}s that it renews as it moves. Spirits are made of mana: they are
 * not saved and vanish with their caster.
 */
public class LamplightSpirit extends Entity {
    private static final double ORBIT_RADIUS = 1.3;
    private static final double HEIGHT = 2.3;

    private UUID summoner;
    private WeakReference<ServerPlayer> summonerRef = new WeakReference<>(null);
    private int index;
    private int count = 1;
    private int lifetime = 100;

    public LamplightSpirit(EntityType<? extends LamplightSpirit> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public static LamplightSpirit create(ServerPlayer summoner, int index, int count, int lifetime) {
        LamplightSpirit spirit = new LamplightSpirit(MushoEntities.LAMPLIGHT_SPIRIT.get(), summoner.level());
        spirit.summoner = summoner.getUUID();
        spirit.summonerRef = new WeakReference<>(summoner);
        spirit.index = index;
        spirit.count = count;
        spirit.lifetime = lifetime;
        spirit.moveTo(summoner.getX(), summoner.getY() + 1.0, summoner.getZ());
        return spirit;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        ServerPlayer owner = summoner == null ? null : level.getServer().getPlayerList().getPlayer(summoner);
        if (owner == null) {
            owner = summonerRef.get();
        }
        if (owner == null || owner.isRemoved() || owner.level() != level || !owner.isAlive() || tickCount > lifetime) {
            discard();
            return;
        }
        double angle = tickCount * 0.06 + index * Mth.TWO_PI / count;
        setPos(owner.getX() + Math.cos(angle) * ORBIT_RADIUS, owner.getY() + HEIGHT + Math.sin(tickCount * 0.1 + index) * 0.2,
                owner.getZ() + Math.sin(angle) * ORBIT_RADIUS);
        if (tickCount % 2 == 0) {
            shedLight(level, blockPosition());
        }
    }

    private static void shedLight(ServerLevel level, BlockPos pos) {
        if (level.getBlockState(pos).isAir() && !level.getBlockState(pos).is(MushoBlocks.SPIRIT_LIGHT.get())) {
            level.setBlock(pos, MushoBlocks.SPIRIT_LIGHT.get().defaultBlockState(), 3);
            level.scheduleTick(pos, MushoBlocks.SPIRIT_LIGHT.get(), SpiritLightBlock.LINGER_TICKS);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
