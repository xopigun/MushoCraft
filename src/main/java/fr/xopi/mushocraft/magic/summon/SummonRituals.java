package fr.xopi.mushocraft.magic.summon;

import fr.xopi.mushocraft.core.MushoTags;
import fr.xopi.mushocraft.magic.mana.ManaService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * The summonings of the wiki (<i>Magic Spells</i> § Summoning Magic) that a magician can perform. Dragon Gate (a secret
 * art of the dragon tribe) and Otherworld Summon (Nanahoshi's research) are left out (docs/ECARTS.md, E24).
 */
public final class SummonRituals {
    private SummonRituals() {
    }

    /** Summon Lamplight Spirits: radiant spirits that circle the caster and light the dark, for almost no mana. */
    public static final SummonRitual LAMPLIGHT = new SummonRitual() {
        @Override
        public double manaCost() {
            return SummonRules.LAMPLIGHT_COST;
        }

        @Override
        public boolean perform(ServerPlayer summoner) {
            for (int i = 0; i < SummonRules.LAMPLIGHT_SPIRITS; i++) {
                summoner.serverLevel().addFreshEntity(LamplightSpirit.create(summoner, i, SummonRules.LAMPLIGHT_SPIRITS,
                        SummonRules.LAMPLIGHT_TICKS));
            }
            return true;
        }

        @Override
        public Component description() {
            return Component.translatable("summon.mushocraft.lamplight");
        }
    };

    /** Spirit Summon: a spirit made of the caster's mana, which guards it and lives on its mana. */
    public static final SummonRitual SPIRIT = new SummonRitual() {
        @Override
        public double manaCost() {
            return SummonRules.SPIRIT_COST;
        }

        @Override
        public boolean perform(ServerPlayer summoner) {
            SummonedSpirit spirit = SummonedSpirit.create(summoner);
            summoner.serverLevel().addFreshEntity(spirit);
            SummonBonds.bind(spirit, summoner, SummonRules.SPIRIT_MAX_TICKS);
            return true;
        }

        @Override
        public Component description() {
            return Component.translatable("summon.mushocraft.spirit");
        }
    };

    /**
     * Monster Summon: calls an existing creature of the land to the caster and binds it by covenant. The covenant
     * forbids intelligent humanoid races. A creature with more mana than its summoner may break loose and turn on it.
     */
    public static final SummonRitual BEAST = new SummonRitual() {
        @Override
        public double manaCost() {
            return SummonRules.BEAST_COST;
        }

        @Override
        public boolean perform(ServerPlayer summoner) {
            ServerLevel level = summoner.serverLevel();
            List<EntityType<?>> candidates = summonable(level, summoner.blockPosition());
            if (candidates.isEmpty()) {
                return false;
            }
            RandomSource random = summoner.getRandom();
            Entity entity = candidates.get(random.nextInt(candidates.size())).create(level);
            if (!(entity instanceof Mob creature)) {
                return false;
            }
            Vec3 front = summoner.position().add(summoner.getLookAngle().multiply(1.0, 0.0, 1.0).normalize().scale(2.5));
            creature.moveTo(front.x, summoner.getY(), front.z, summoner.getYRot() + 180.0F, 0.0F);
            creature.finalizeSpawn(level, level.getCurrentDifficultyAt(creature.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            level.addFreshEntity(creature);
            double loseControl = SummonRules.lossOfControlChance(creature.getMaxHealth(), ManaService.snapshot(summoner).capacity());
            if (random.nextDouble() < loseControl) {
                summoner.sendSystemMessage(Component.translatable("message.mushocraft.summon.broke_loose", creature.getDisplayName()));
                if (creature instanceof Enemy) {
                    creature.setTarget(summoner);
                }
            } else {
                SummonBonds.bind(creature, summoner, SummonRules.BEAST_BOND_TICKS);
                summoner.sendSystemMessage(Component.translatable("message.mushocraft.summon.bound", creature.getDisplayName()));
            }
            return true;
        }

        @Override
        public Component description() {
            return Component.translatable("summon.mushocraft.beast");
        }
    };

    /** The creatures that live where the summoner stands, minus the intelligent races the covenant forbids. */
    static List<EntityType<?>> summonable(ServerLevel level, BlockPos pos) {
        MobSpawnSettings settings = level.getBiome(pos).value().getMobSettings();
        List<EntityType<?>> types = new ArrayList<>();
        for (MobCategory category : List.of(MobCategory.CREATURE, MobCategory.MONSTER)) {
            settings.getMobs(category).unwrap().forEach(data -> {
                if (!data.type.is(MushoTags.UNSUMMONABLE) && !types.contains(data.type)) {
                    types.add(data.type);
                }
            });
        }
        return types;
    }
}
