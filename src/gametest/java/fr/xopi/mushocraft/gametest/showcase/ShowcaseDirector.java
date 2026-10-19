package fr.xopi.mushocraft.gametest.showcase;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.combat.move.MoveService;
import fr.xopi.mushocraft.life.LifeService;
import fr.xopi.mushocraft.magic.entity.SpellProjectile;
import fr.xopi.mushocraft.magic.spell.SpellElement;
import fr.xopi.mushocraft.magic.spell.effect.ProjectileProfile;
import fr.xopi.mushocraft.magic.mana.ManaService;
import fr.xopi.mushocraft.magic.spell.MagicService;
import fr.xopi.mushocraft.magic.spell.MushoSpells;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
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
            new Step(305, () -> {
                Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
                onServer(player -> {
                    List.of(MushoSpells.ICE_PILLAR, MushoSpells.EX_FLAME, MushoSpells.EARTH_LANCE, MushoSpells.EXPLOSION)
                            .forEach(spell -> MagicService.learn(player, spell.get()));
                    MoveService.setSlot(player, 0, Optional.of(MushoSpells.ICE_PILLAR.get().ref()));
                    MoveService.setSlot(player, 1, Optional.of(MushoSpells.EX_FLAME.get().ref()));
                    MoveService.setSlot(player, 2, Optional.of(MushoSpells.EARTH_LANCE.get().ref()));
                    MoveService.setSlot(player, 3, Optional.of(MushoSpells.EXPLOSION.get().ref()));
                    player.teleportTo(player.serverLevel(), player.getX() + 4, player.getY(), player.getZ(), 0.0F, 25.0F);
                });
            }),
            new Step(315, () -> onServer(player -> MoveService.use(player, 0))),
            new Step(350, () -> shoot("07_ice_pillar")),
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
            new Step(520, () -> onServer(player -> {
                setBattleMode(player, false);
                LifeService.rebirth(player);
            })),
            new Step(550, () -> {
                Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK);
            }),
            new Step(560, () -> shoot("11_baby_third_person")),
            new Step(570, () -> Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON)),
            new Step(580, () -> shoot("12_baby_hud")),
            new Step(600, () -> Minecraft.getInstance().stop())
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

    private static void onServer(Consumer<ServerPlayer> action) {
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server != null) {
            server.execute(() -> server.getPlayerList().getPlayers().forEach(action));
        }
    }

    /** A clear stage high above the terrain, so nothing hides the spells. */
    private static void standOnSkyPlatform(ServerPlayer player) {
        BlockPos center = new BlockPos(player.getBlockX(), 200, player.getBlockZ());
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -6; dz <= 12; dz++) {
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

    private static void shoot(String name) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.gui.getChat().clearMessages(false);
        Screenshot.grab(minecraft.gameDirectory, "showcase_" + name + ".png", minecraft.getMainRenderTarget(),
                message -> MushoCraft.LOGGER.info("Showcase screenshot {}: {}", name, message.getString()));
    }
}
