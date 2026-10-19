package fr.xopi.mushocraft.gametest.showcase;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.client.screen.SpellbookScreen;
import fr.xopi.mushocraft.combat.mastery.MasteryRules;
import fr.xopi.mushocraft.combat.mastery.MasteryService;
import fr.xopi.mushocraft.combat.move.CooldownService;
import fr.xopi.mushocraft.combat.move.MoveRef;
import fr.xopi.mushocraft.combat.move.MoveService;
import fr.xopi.mushocraft.core.MushoEntities;
import fr.xopi.mushocraft.core.MushoItems;
import fr.xopi.mushocraft.core.Rank;
import fr.xopi.mushocraft.life.LifeService;
import fr.xopi.mushocraft.magic.block.ConjuredBlock;
import fr.xopi.mushocraft.magic.entity.PillarKind;
import fr.xopi.mushocraft.magic.entity.SpellFire;
import fr.xopi.mushocraft.magic.entity.SpellPillar;
import fr.xopi.mushocraft.magic.entity.SpellProjectile;
import fr.xopi.mushocraft.magic.entity.SpellZone;
import fr.xopi.mushocraft.magic.entity.WaterSheet;
import fr.xopi.mushocraft.magic.mana.ManaService;
import fr.xopi.mushocraft.magic.spell.MagicService;
import fr.xopi.mushocraft.magic.spell.MushoSpells;
import fr.xopi.mushocraft.magic.spell.SpellElement;
import fr.xopi.mushocraft.magic.spell.SpellSchool;
import fr.xopi.mushocraft.magic.spell.effect.ProjectileProfile;
import fr.xopi.mushocraft.magic.spell.effect.ProjectileShape;
import fr.xopi.mushocraft.npc.MagicTutorEntity;
import fr.xopi.mushocraft.npc.SwordMasterEntity;
import fr.xopi.mushocraft.sword.SwordService;
import fr.xopi.mushocraft.sword.SwordStyle;
import fr.xopi.mushocraft.sword.technique.MushoTechniques;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import yesman.epicfight.registry.entries.EpicFightItems;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Development only, never shipped (gametest source set). Started by the {@code clientShowcase} run with
 * {@code -Dmushocraft.showcase=true}: once in the world, it plays a short scripted sequence and saves
 * screenshots to run/screenshots, then quits. Lets the HUD and the rendering be checked without a human.
 */
@EventBusSubscriber(modid = MushoCraft.MODID, value = Dist.CLIENT)
public final class ShowcaseDirector {
    private static final boolean ENABLED = Boolean.getBoolean("mushocraft.showcase");

    private record Step(int tick, Runnable action) {
    }

    private static final List<Step> SCRIPT = List.of(
            new Step(40, () -> onServer(player -> {
                player.setGameMode(GameType.SURVIVAL);
                player.serverLevel().setDayTime(6000);
                standOnSkyPlatform(player);
                LifeService.setAge(player, 20.0);
                ManaService.setTrained(player, 300.0);
                ManaService.refill(player);
                List.of(MushoSpells.WATER_BALL, MushoSpells.FIREBALL, MushoSpells.ROCK_BULLET, MushoSpells.WATER_SHIELD,
                        MushoSpells.ICE_SMASH, MushoSpells.HEALING, MushoSpells.MAGIC_SHIELD, MushoSpells.EXORCIST_LIGHT,
                        MushoSpells.WATER_ARROW).forEach(spell -> MagicService.learn(player, spell.get()));
                    masterAll(player);
                setBattleMode(player, true);
            })),
            new Step(60, () -> onServer(player -> {
                for (SpellElement element : SpellElement.values()) {
                    SpellProjectile orb = SpellProjectile.create(player.serverLevel(), player,
                            ProjectileProfile.of(element, 0.0F).size(0.6F).build());
                    orb.setPos(player.getX() - 2.5 + element.ordinal(), player.getY() + 1.5, player.getZ() + 4.0);
                    orb.setDeltaMovement(0.0, 0.0, 0.0);
                    player.serverLevel().addFreshEntity(orb);
                }
            })),
            new Step(80, () -> shoot("01_battle_hud_and_orbs")),
            new Step(90, () -> {
                onServer(player -> MoveService.use(player, 0));
            }),
            new Step(100, () -> shoot("02_water_ball_chant")),
            new Step(101, () -> shootWhenProjectileFlies("03_water_ball_flight")),
            new Step(160, () -> {
                onServer(player -> MoveService.use(player, 1));
                shootWhenProjectileFlies("04_fireball_flight");
            }),
            new Step(220, () -> {
                onServer(player -> MoveService.use(player, 2));
                shootWhenProjectileFlies("05_rock_bullet_flight");
            }),
            new Step(265, () -> Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK)),
            new Step(270, () -> onServer(player -> MoveService.use(player, 3))),
            new Step(300, () -> shoot("06_water_shield")),
            new Step(302, () -> {
                // over the caster's shoulder, from above: the screen of water curves round them
                Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK);
                onServer(player -> player.setXRot(28.0F));
            }),
            new Step(318, () -> shoot("06b_water_shield_wall")),
            new Step(320, () -> {
                Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
                onServer(player -> {
                    clearStage(player);
                    List.of(MushoSpells.ICE_PILLAR, MushoSpells.EX_FLAME, MushoSpells.EARTH_LANCE, MushoSpells.EXPLOSION)
                            .forEach(spell -> MagicService.learn(player, spell.get()));
                    masterAll(player);
                    MoveService.setSlot(player, 0, Optional.of(MushoSpells.ICE_PILLAR.get().ref()));
                    MoveService.setSlot(player, 1, Optional.of(MushoSpells.EX_FLAME.get().ref()));
                    MoveService.setSlot(player, 2, Optional.of(MushoSpells.EARTH_LANCE.get().ref()));
                    MoveService.setSlot(player, 3, Optional.of(MushoSpells.EXPLOSION.get().ref()));
                    player.setXRot(25.0F);
                });
            }),
            new Step(325, () -> onServer(player -> MoveService.use(player, 0))),
            new Step(358, () -> {
                logPillars();
                shoot("07_ice_pillar");
            }),
            new Step(360, () -> onServer(player -> {
                player.setXRot(-5.0F);
                MoveService.use(player, 1);
            })),
            new Step(361, () -> shootWhenProjectileFlies("08_ex_flame_flight")),
            new Step(420, () -> onServer(player -> {
                player.setXRot(25.0F);
                MoveService.use(player, 2);
            })),
            new Step(458, () -> shoot("09_earth_lance")),
            new Step(470, () -> onServer(player -> MoveService.use(player, 3))),
            new Step(507, () -> shoot("10_explosion")),
            new Step(508, () -> {
                onServer(player -> {
                    player.setXRot(10.0F);
                    spawnLabProjectile(player, ProjectileProfile.of(SpellElement.WATER, 0.0F).size(0.9F).gravity(0.0F).build(), 0.35, 0.2);
                    spawnLabProjectile(player, ProjectileProfile.of(SpellElement.WATER, 0.0F).size(0.3F).gravity(0.0F)
                            .shape(ProjectileShape.BOLT).build(), 0.45, -0.5);
                    spawnLabProjectile(player, ProjectileProfile.of(SpellElement.ICE, 0.0F).size(0.7F).gravity(0.0F)
                            .shape(ProjectileShape.CRYSTAL_CLUSTER).build(), 0.25, 1.3);
                });
            }),
            new Step(509, () -> Minecraft.getInstance().gui.getChat().clearMessages(false)),
            new Step(519, () -> {
                Minecraft minecraft = Minecraft.getInstance();
                for (Entity entity : minecraft.level.entitiesForRendering()) {
                    if (entity instanceof SpellProjectile projectile) {
                        MushoCraft.LOGGER.info("Showcase lab projectile {} at {} (player at {})", projectile.shape(),
                                projectile.position(), minecraft.player.position());
                    }
                }
                shoot("00_water_closeup");
            }),
            new Step(520, () -> {
                Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_FRONT);
                onServer(player -> {
                    player.getInventory().clearContent();
                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
                    player.setXRot(0.0F);
                    List.of(MushoTechniques.ARM_CHOP, MushoTechniques.SWORD_THROWING, MushoTechniques.MOONLIGHT_CLEAVE,
                            MushoTechniques.LONGSWORD_OF_LIGHT).forEach(technique -> SwordService.learn(player, technique.get()));
                    masterAll(player);
                    SwordService.adopt(player, SwordStyle.SWORD_GOD);
                    MoveService.setSlot(player, 0, Optional.of(MushoTechniques.ARM_CHOP.get().ref()));
                    MoveService.setSlot(player, 1, Optional.of(MushoTechniques.MOONLIGHT_CLEAVE.get().ref()));
                    MoveService.setSlot(player, 2, Optional.of(MushoTechniques.SWORD_THROWING.get().ref()));
                    MoveService.setSlot(player, 3, Optional.of(MushoTechniques.LONGSWORD_OF_LIGHT.get().ref()));
                    for (int i = 0; i < 3; i++) {
                        Zombie zombie = EntityType.HUSK.create(player.serverLevel());
                        zombie.moveTo(player.getX() - 1.5 + i * 1.5, player.getY(), player.getZ() + 2.5, 180.0F, 0.0F);
                        zombie.setNoAi(true);
                        zombie.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
                        player.serverLevel().addFreshEntity(zombie);
                    }
                });
            }),
            new Step(535, ShowcaseDirector::logClientCombo),
            new Step(540, () -> onServer(player -> logUse(player, 0))),
            new Step(546, () -> shoot("13_arm_chop")),
            new Step(600, () -> onServer(player -> {
                CooldownService.clear(player);
                logUse(player, 1);
            })),
            new Step(606, () -> shoot("14_moonlight_cleave")),
            new Step(660, () -> onServer(player -> {
                CooldownService.clear(player);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(EpicFightItems.IRON_TACHI.get()));
            })),
            new Step(670, () -> onServer(player -> logUse(player, 3))),
            new Step(674, () -> shoot("15_longsword_of_light")),
            new Step(720, () -> onServer(player -> {
                CooldownService.clear(player);
                logUse(player, 2);
            })),
            new Step(723, () -> shoot("16_sword_throwing")),
            new Step(740, () -> {
                Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
                onServer(player -> {
                    setBattleMode(player, false);
                    player.serverLevel().getEntitiesOfClass(Zombie.class, player.getBoundingBox().inflate(8.0)).forEach(Entity::discard);
                    player.setYRot(0.0F);
                    player.setXRot(5.0F);
                    spawnTeachers(player);
                });
            }),
            new Step(780, () -> shoot("18_teachers")),
            new Step(785, () -> onServer(player -> player.serverLevel()
                    .getEntitiesOfClass(SwordMasterEntity.class, player.getBoundingBox().inflate(8.0)).stream()
                    .filter(master -> master.style() == SwordStyle.SWORD_GOD)
                    .findFirst().ifPresent(master -> master.interact(player, InteractionHand.MAIN_HAND)))),
            new Step(795, () -> shootKeepingChat("19_master_lesson")),
            new Step(800, () -> {
                Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK);
                onServer(player -> {
                    player.getInventory().clearContent();
                    player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(MushoItems.AQUA_HEARTIA.get()));
                    player.setXRot(0.0F);
                    List.of(MushoSpells.BLIZZARD_STORM, MushoSpells.EARTH_HEDGEHOG, MushoSpells.EARTH_FORTRESS)
                            .forEach(spell -> MagicService.learn(player, spell.get()));
                    masterAll(player);
                    MoveService.setSlot(player, 0, Optional.of(MushoSpells.BLIZZARD_STORM.get().ref()));
                    MoveService.setSlot(player, 1, Optional.of(MushoSpells.EARTH_HEDGEHOG.get().ref()));
                    MoveService.setSlot(player, 2, Optional.of(MushoSpells.EARTH_FORTRESS.get().ref()));
                    ManaService.refill(player);
                    CooldownService.clear(player);
                    setBattleMode(player, true);
                });
            }),
            new Step(810, () -> onServer(player -> logUse(player, 0))),
            new Step(811, () -> shootWhenProjectileFlies("20_blizzard_storm")),
            new Step(870, () -> onServer(player -> {
                player.setXRot(25.0F);
                logUse(player, 1);
            })),
            new Step(926, () -> shoot("21_earth_hedgehog")),
            new Step(930, () -> onServer(player -> {
                player.setXRot(10.0F);
                logUse(player, 2);
            })),
            new Step(985, () -> {
                Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
                onServer(player -> {
                    player.teleportTo(player.getX() + 6.0, player.getY(), player.getZ() - 3.0);
                    player.setYRot(60.0F);
                    player.setYHeadRot(60.0F);
                    player.setXRot(15.0F);
                });
            }),
            new Step(995, () -> shoot("22_earth_fortress")),
            new Step(1000, () -> {
                Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK);
                onServer(player -> {
                    player.setYRot(0.0F);
                    player.setYHeadRot(0.0F);
                    player.setXRot(25.0F);
                    ManaService.setTrained(player, 2000.0);
                    ManaService.refill(player);
                    CooldownService.clear(player);
                    List.of(MushoSpells.CUMULONIMBUS, MushoSpells.TORNADO, MushoSpells.FLASH_OVER)
                            .forEach(spell -> MagicService.learn(player, spell.get()));
                    masterAll(player);
                    MoveService.setSlot(player, 3, Optional.of(MushoSpells.CUMULONIMBUS.get().ref()));
                    MoveService.setSlot(player, 4, Optional.of(MushoSpells.TORNADO.get().ref()));
                    MoveService.setSlot(player, 5, Optional.of(MushoSpells.FLASH_OVER.get().ref()));
                    // One spell left half-practised: its mastery gauge shows under its slot.
                    MoveService.setSlot(player, 8, Optional.of(MushoSpells.WATER_ARROW.get().ref()));
                    MasteryService.setRepetitions(player, MushoSpells.WATER_ARROW.get().ref(), 20.0);
                });
            }),
            new Step(1005, () -> onServer(player -> logUse(player, 3))),
            new Step(1095, () -> shoot("24_cumulonimbus")),
            new Step(1110, () -> onServer(player -> {
                ManaService.refill(player);
                logUse(player, 4);
            })),
            new Step(1200, () -> shoot("25_tornado")),
            new Step(1210, () -> onServer(player -> {
                ManaService.refill(player);
                logUse(player, 5);
            })),
            new Step(1283, () -> shoot("26_flash_over")),
            new Step(1288, () -> Minecraft.getInstance().setScreen(new SpellbookScreen())),
            new Step(1294, () -> shoot("27_spellbook")),
            new Step(1297, () -> Minecraft.getInstance().setScreen(null)),
            new Step(1300, () -> onServer(player -> {
                setBattleMode(player, false);
                LifeService.rebirth(player);
            })),
            new Step(1330, () -> Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK)),
            new Step(1340, () -> shoot("17_baby_third_person")),
            new Step(1342, () -> {
                Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
                Minecraft.getInstance().gui.getChat().clearMessages(false);
                onServer(player -> {
                    player.setYRot(0.0F);
                    player.setXRot(8.0F);
                    clearStage(player);
                    raiseLabPillars(player);
                });
            }),
            new Step(1352, () -> onServer(ShowcaseDirector::lightLabFires)),
            new Step(1357, () -> {
                logPillars();
                shoot("00b_pillars_and_fire");
            }),
            new Step(1366, () -> shoot("00c_blast_smoke")),
            new Step(1372, () -> Minecraft.getInstance().stop())
    );

    private static final int FLIGHT_TICKS_BEFORE_SHOT = 3;

    private static int ticksInWorld;
    private static int nextStep;
    private static String pendingFlightShot;

    private ShowcaseDirector() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!ENABLED || minecraft.player == null || minecraft.getSingleplayerServer() == null || nextStep >= SCRIPT.size()) {
            return;
        }
        ticksInWorld++;
        if (pendingFlightShot != null && projectileInFlight(minecraft)) {
            shoot(pendingFlightShot);
            pendingFlightShot = null;
        }
        while (nextStep < SCRIPT.size() && SCRIPT.get(nextStep).tick() <= ticksInWorld) {
            SCRIPT.get(nextStep++).action().run();
        }
    }

    private static void shootWhenProjectileFlies(String name) {
        pendingFlightShot = name;
    }

    private static boolean projectileInFlight(Minecraft minecraft) {
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof SpellProjectile projectile && projectile.tickCount >= FLIGHT_TICKS_BEFORE_SHOT
                    && projectile.tickCount <= FLIGHT_TICKS_BEFORE_SHOT + 3 && projectile.distanceTo(minecraft.player) < 12.0F) {
                return true;
            }
        }
        return false;
    }

    private static void logUse(ServerPlayer player, int slot) {
        var result = MoveService.use(player, slot);
        MushoCraft.LOGGER.info("Showcase move slot {}: success {} {}", slot, result.success(),
                result.failure().map(net.minecraft.network.chat.Component::getString).orElse(""));
        player.serverLevel().getEntitiesOfClass(Zombie.class, player.getBoundingBox().inflate(8.0))
                .forEach(zombie -> MushoCraft.LOGGER.info("Showcase target health {}", zombie.getHealth()));
    }

    /** What Epic Fight's client side picks as basic combo for the local player (style sync check). */
    private static void logClientCombo() {
        Minecraft minecraft = Minecraft.getInstance();
        PlayerPatch<?> patch = EpicFightCapabilities.getPlayerPatch(minecraft.player);
        if (patch != null) {
            var weapon = EpicFightCapabilities.getItemStackCapability(minecraft.player.getMainHandItem());
            MushoCraft.LOGGER.info("Showcase client style {} combo {}", weapon.getStyle(patch),
                    weapon.getAutoAttackMotion(patch).stream().map(Object::toString).toList());
        }
    }

    /** A projectile crossing the view from left to right, 3 blocks ahead, to look at its model closely. */
    private static void spawnLabProjectile(ServerPlayer player, ProjectileProfile profile, double speed, double height) {
        SpellProjectile projectile = SpellProjectile.create(player.serverLevel(), player, profile);
        projectile.setPos(player.getX() + 1.6, player.getEyeY() + height - 0.4, player.getZ() + 3.0);
        projectile.setDeltaMovement(-speed, 0.0, 0.0);
        player.serverLevel().addFreshEntity(projectile);
    }

    /** Debug: where the pillar models are and which animation they play. */
    private static void logPillars() {
        Minecraft minecraft = Minecraft.getInstance();
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof SpellPillar pillar) {
                var controller = pillar.getAnimatableInstanceCache().getManagerForId(pillar.getId())
                        .getAnimationControllers().get("pillar");
                MushoCraft.LOGGER.info("Showcase pillar {} age {} at {} (player at {}): animation {} state {}", pillar.kind(),
                        pillar.tickCount, pillar.position(), minecraft.player.position(),
                        controller == null || controller.getCurrentAnimation() == null ? "none"
                                : controller.getCurrentAnimation().animation().name(),
                        controller == null ? "none" : controller.getAnimationState());
            }
        }
    }

    /** Clears what earlier scenes left around the player: lasting spells, conjured walls, the weather. */
    private static void clearStage(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        level.getEntitiesOfClass(Entity.class, player.getBoundingBox().inflate(48.0),
                entity -> entity instanceof WaterSheet || entity instanceof SpellZone || entity instanceof SpellPillar
                        || entity instanceof SpellFire).forEach(Entity::discard);
        BlockPos.betweenClosed(player.blockPosition().offset(-12, -2, -12), player.blockPosition().offset(12, 8, 12)).forEach(pos -> {
            if (level.getBlockState(pos).getBlock() instanceof ConjuredBlock) {
                level.removeBlock(pos, false);
            }
        });
        level.setWeatherParameters(6000, 0, false, false);
    }

    /** The Blockbench pillars side by side, behind the lab projectiles: ice, earthen lance, dark spike. */
    private static void raiseLabPillars(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        double z = player.getZ() + 6.5;
        level.addFreshEntity(SpellPillar.create(level, PillarKind.ICE_PILLAR, new Vec3(player.getX() + 2.4, player.getY(), z), 3.0F, 32, 20.0F, 0.0F));
        level.addFreshEntity(SpellPillar.create(level, PillarKind.EARTH_LANCE, new Vec3(player.getX(), player.getY(), z), 3.0F, 32, 70.0F, 4.0F));
        level.addFreshEntity(SpellPillar.create(level, PillarKind.EARTH_SPIKE, new Vec3(player.getX() - 2.4, player.getY(), z), 2.0F, 32, 140.0F, 18.0F));
        level.addFreshEntity(SpellPillar.create(level, PillarKind.FROST_MOUND, new Vec3(player.getX() - 1.4, player.getY(), z - 2.5), 1.4F, 32, 30.0F, 0.0F));
    }

    /** Fire beside the pillars: a burning column, a blast and a fireball drifting across. */
    private static void lightLabFires(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        level.addFreshEntity(SpellFire.column(level, new Vec3(player.getX() + 5.0, player.getY(), player.getZ() + 8.0), 1.1F, 4.0F, 30));
        level.addFreshEntity(SpellFire.blast(level, new Vec3(player.getX() - 5.0, player.getY() + 1.6, player.getZ() + 8.0), 1.8F));
        spawnLabProjectile(player, ProjectileProfile.of(SpellElement.FIRE, 0.0F).size(1.0F).gravity(0.0F).build(), 0.3, 0.4);
    }

    /** The showcase films moves at their best: everything known is mastered (no random botch). */
    private static void masterAll(ServerPlayer player) {
        MagicService.knownSpells(player).forEach(id ->
                MasteryService.setRepetitions(player, MoveRef.spell(id), MasteryRules.REPETITIONS_TO_MASTER));
        SwordService.knownTechniques(player).forEach(id ->
                MasteryService.setRepetitions(player, MoveRef.technique(id), MasteryRules.REPETITIONS_TO_MASTER));
    }

    private static void onServer(Consumer<ServerPlayer> action) {
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server != null) {
            server.execute(() -> server.getPlayerList().getPlayers().forEach(action));
        }
    }

    /** A clear stage high above the terrain, so nothing hides the spells. */
    private static void standOnSkyPlatform(ServerPlayer player) {
        BlockPos center = new BlockPos(player.getBlockX(), 200, player.getBlockZ());
        for (int dx = -16; dx <= 16; dx++) {
            for (int dz = -16; dz <= 24; dz++) {
                player.serverLevel().setBlockAndUpdate(center.offset(dx, -1, dz), Blocks.SMOOTH_STONE.defaultBlockState());
            }
        }
        player.teleportTo(player.serverLevel(), center.getX() + 0.5, center.getY(), center.getZ() + 0.5, 0.0F, -12.0F);
    }

    private static void setBattleMode(ServerPlayer player, boolean battle) {
        PlayerPatch<?> patch = EpicFightCapabilities.getPlayerPatch(player);
        if (patch != null) {
            if (battle) {
                patch.toEpicFightMode(true);
            } else {
                patch.toVanillaMode(true);
            }
        }
    }

    /** One master of each style and two tutors, standing in a row in front of the player. */
    private static void spawnTeachers(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        double x = player.getX() - 4.0;
        for (SwordStyle style : SwordStyle.values()) {
            SwordMasterEntity master = MushoEntities.SWORD_MASTER.get().create(level);
            master.setStyle(style);
            master.setRank(Rank.SAINT);
            place(level, master, x, player.getY(), player.getZ() + 5.0);
            x += 2.0;
        }
        for (SpellSchool school : List.of(SpellSchool.WATER, SpellSchool.BARRIER)) {
            MagicTutorEntity tutor = MushoEntities.MAGIC_TUTOR.get().create(level);
            tutor.setSchool(school);
            tutor.setRank(Rank.ADVANCED);
            place(level, tutor, x, player.getY(), player.getZ() + 5.0);
            x += 2.0;
        }
    }

    private static void place(ServerLevel level, Mob mob, double x, double y, double z) {
        mob.moveTo(x, y, z, 180.0F, 0.0F);
        mob.setYHeadRot(180.0F);
        mob.setYBodyRot(180.0F);
        mob.setNoAi(true);
        level.addFreshEntity(mob);
    }

    private static void shoot(String name) {
        Minecraft.getInstance().gui.getChat().clearMessages(false);
        shootKeepingChat(name);
    }

    private static void shootKeepingChat(String name) {
        Minecraft minecraft = Minecraft.getInstance();
        Screenshot.grab(minecraft.gameDirectory, "showcase_" + name + ".png", minecraft.getMainRenderTarget(),
                message -> MushoCraft.LOGGER.info("Showcase screenshot {}: {}", name, message.getString()));
    }
}
