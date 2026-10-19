package fr.xopi.mushocraft.gametest.showcase;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.sword.entity.ThrownTrick;
import net.minecraft.world.phys.Vec3;
import fr.xopi.mushocraft.core.Rank;
import fr.xopi.mushocraft.sword.technique.TechniqueContext;
import fr.xopi.mushocraft.sword.technique.Technique;
import fr.xopi.mushocraft.sword.technique.MushoTechniques;
import fr.xopi.mushocraft.monster.MushoMonster;
import fr.xopi.mushocraft.core.MushoRegistries;
import fr.xopi.mushocraft.life.LifeService;
import fr.xopi.mushocraft.magic.block.ConjuredBlock;
import fr.xopi.mushocraft.magic.entity.SpellBeam;
import fr.xopi.mushocraft.magic.entity.SpellFire;
import fr.xopi.mushocraft.magic.entity.SpellPillar;
import fr.xopi.mushocraft.magic.entity.SpellProjectile;
import fr.xopi.mushocraft.magic.entity.SpellZone;
import fr.xopi.mushocraft.magic.entity.WaterSheet;
import fr.xopi.mushocraft.magic.fx.SpellFx;
import fr.xopi.mushocraft.magic.mana.ManaService;
import fr.xopi.mushocraft.client.screen.SpellbookScreen;
import fr.xopi.mushocraft.combat.move.MoveService;
import fr.xopi.mushocraft.magic.spell.ChantBroadcast;
import fr.xopi.mushocraft.magic.spell.MagicService;
import fr.xopi.mushocraft.sword.SwordService;
import fr.xopi.mushocraft.magic.spell.MushoSpells;
import fr.xopi.mushocraft.magic.spell.Spell;
import fr.xopi.mushocraft.magic.spell.SpellContext;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Development only (run {@code runClientGallery}): casts every spell of the catalogue in turn, seen in first person
 * the way a player sees it, on a stone arena in the sky, at a dummy standing ahead, and saves two screenshots of each (just after the cast, and a
 * moment later) as run/screenshots/gallery_NN_spell_a/b.png. An audit of what every spell looks like. A few effects
 * that no spell shows (a sword slash) close the round.
 */
@EventBusSubscriber(modid = MushoCraft.MODID, value = Dist.CLIENT)
public final class GalleryDirector {
    private static final boolean ENABLED = Boolean.getBoolean("mushocraft.gallery");
    /** Only the exhibits whose name starts with this, when set (the spellbook is then skipped). */
    private static final String ONLY = System.getProperty("mushocraft.gallery.only", "");
    private static final int START_TICK = 60;
    private static final int TICKS_PER_SPELL = 34;
    private static final int FIRST_SHOT = 3;
    private static final int SECOND_SHOT = 14;
    private static final double DUMMY_DISTANCE = 10.0;

    /** The middle of the stone arena built high in the sky, so nothing stands in the way. */
    private static BlockPos arena;

    private static int ticksInWorld;
    private static List<Exhibit> exhibits;

    private GalleryDirector() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!ENABLED || minecraft.player == null || minecraft.getSingleplayerServer() == null) {
            return;
        }
        ticksInWorld++;
        if (ticksInWorld == 20) {
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
            minecraft.options.hideGui = true;
            onServer(player -> {
                player.setGameMode(GameType.CREATIVE);
                LifeService.setAge(player, 20.0);
                ManaService.setTrained(player, 5000.0);
                arena = new BlockPos(player.getBlockX(), 200, player.getBlockZ());
                for (int dx = -20; dx <= 20; dx++) {
                    for (int dz = -12; dz <= 28; dz++) {
                        player.serverLevel().setBlockAndUpdate(arena.offset(dx, -1, dz), Blocks.SMOOTH_STONE.defaultBlockState());
                    }
                }
            });
        }
        if (ticksInWorld < START_TICK) {
            return;
        }
        if (exhibits == null) {
            exhibits = exhibits();
        }
        int elapsed = ticksInWorld - START_TICK;
        int index = 0;
        while (index < exhibits.size() && elapsed >= exhibits.get(index).ticks()) {
            elapsed -= exhibits.get(index).ticks();
            index++;
        }
        if (index >= exhibits.size()) {
            if (ONLY.isEmpty()) {
                showBook(minecraft, elapsed);
            } else {
                minecraft.stop();
            }
            return;
        }
        Exhibit exhibit = exhibits.get(index);
        String name = String.format(Locale.ROOT, "gallery_%02d_%s", index, exhibit.name());
        int phase = elapsed;
        if (phase == 0) {
            onServer(player -> setUp(player));
        } else if (phase == 2) {
            onServer(player -> {
                player.removeAllEffects();
                ManaService.refill(player);
                exhibit.show().accept(player);
            });
        }
        int[] shots = exhibit.shots();
        for (int i = 0; i < shots.length; i++) {
            if (phase == 2 + shots[i]) {
                if (i == 0) {
                    logVisible(minecraft, exhibit.name());
                }
                shoot(minecraft, name + "_" + (char) ('a' + i));
            }
        }
    }

    /** Casts a spell as if facing the given yaw, without turning the camera. */
    private static void castFacing(ServerPlayer player, Spell spell, float yaw) {
        player.setYRot(yaw);
        player.setYHeadRot(yaw);
        spell.effect().cast(new SpellContext(player, spell, 1.0));
    }

    /**
     * The close of the round: every move learnt, a few on the move bar, the spellbook opened at each of its ribbons in
     * turn, one screenshot each (gallery_book_NN.png), to see the move icons as a player does.
     */
    private static void showBook(Minecraft minecraft, int tick) {
        if (tick == 0) {
            onServer(player -> {
                MushoRegistries.SPELL.stream().forEach(spell -> MagicService.learn(player, spell));
                MushoRegistries.TECHNIQUE.stream().forEach(technique -> SwordService.learn(player, technique));
                List<Spell> some = MushoRegistries.SPELL.stream().limit(9).toList();
                for (int slot = 0; slot < some.size(); slot++) {
                    MoveService.setSlot(player, slot, Optional.of(some.get(slot).ref()));
                }
            });
        } else if (tick == 10) {
            minecraft.options.hideGui = false;
            minecraft.setScreen(new SpellbookScreen());
        } else if (tick >= 20 && minecraft.screen instanceof SpellbookScreen book) {
            int page = (tick - 20) / 8;
            if (page >= book.categoryCount()) {
                minecraft.stop();
            } else if ((tick - 20) % 8 == 0) {
                book.openCategory(page);
            } else if ((tick - 20) % 8 == 4) {
                shoot(minecraft, String.format(Locale.ROOT, "gallery_book_%02d", page));
            }
        } else if (tick > 60) {
            minecraft.stop();
        }
    }

    /** Something to show: every spell, in order, then the effects no spell shows, then the monsters. */
    record Exhibit(String name, Consumer<ServerPlayer> show, int ticks, int[] shots) {
        Exhibit(String name, Consumer<ServerPlayer> show) {
            this(name, show, TICKS_PER_SPELL, new int[]{FIRST_SHOT, SECOND_SHOT});
        }
    }

    private static List<Exhibit> exhibits() {
        List<Exhibit> all = new ArrayList<>();
        MushoRegistries.SPELL.stream().sorted(Comparator.comparing(spell -> spell.id().toString())).forEach(spell ->
                all.add(new Exhibit(spell.id().getPath(), player -> {
                    boolean cast = spell.effect().cast(new SpellContext(player, spell, 1.0));
                    MushoCraft.LOGGER.info("Gallery {}: cast {}", spell.id(), cast);
                })));
        // a sword technique's slash, as Technique draws it
        all.add(new Exhibit("fx_slash", player -> SpellFx.slash(player.serverLevel(), player, player.getLookAngle(), 1.6F, 0xDDEEFF)));
        // walls raised sideways (east, then west) while the camera looks south: both must show edge-on, side by side
        all.add(new Exhibit("fx_walls_sideways", player -> {
            castFacing(player, MushoSpells.EARTH_WALL.get(), -90.0F);
            castFacing(player, MushoSpells.ICE_FORTRESS.get(), 90.0F);
            player.setYRot(0.0F);
        }));
        // Disturb Magic on a spell taking shape in the dummy's hands
        all.add(new Exhibit("fx_disturb_chant", player -> player.serverLevel().getEntitiesOfClass(Husk.class,
                player.getBoundingBox().inflate(16.0)).stream().findFirst().ifPresent(dummy -> {
                    Spell disturb = MushoSpells.DISTURB_MAGIC.get();
                    ChantBroadcast.started(dummy, MushoSpells.WATER_CANNON.get(), 40);
                    disturb.effect().cast(new SpellContext(player, disturb, 1.0));
                    ChantBroadcast.stopped(dummy);
                })));
        // the North God's thrown tricks and the Water God's field, aimed at the dummy
        all.add(new Exhibit("fx_tech_teardrop_bullet", player -> technique(player, MushoTechniques.TEARDROP_BULLET.get())));
        all.add(new Exhibit("fx_tech_crimson_ink", player -> technique(player, MushoTechniques.CRIMSON_INK.get())));
        all.add(new Exhibit("fx_tech_sword_kingdom", player -> {
            technique(player, MushoTechniques.DEPRIVATION_SWORD_KINGDOM.get());
            player.serverLevel().getEntitiesOfClass(Husk.class, player.getBoundingBox().inflate(16.0)).stream().findFirst()
                    .ifPresent(dummy -> {
                        dummy.moveTo(player.getX(), player.getY(), player.getZ() + 4.0, 180.0F, 0.0F);
                        ChantBroadcast.started(dummy, MushoSpells.WATER_CANNON.get(), 40);
                    });
        }, 34, new int[]{2, 8, 14}));
        MonsterExhibits.add(all);
        return all.stream().filter(exhibit -> exhibit.name().startsWith(ONLY)).toList();
    }

    /** A sword technique performed by the player towards the dummy, at the God rank. */
    private static void technique(ServerPlayer player, Technique technique) {
        Vec3 aim = player.serverLevel().getEntitiesOfClass(Husk.class, player.getBoundingBox().inflate(16.0)).stream().findFirst()
                .map(dummy -> dummy.getEyePosition().subtract(player.getEyePosition()).normalize()).orElse(player.getLookAngle());
        technique.action().perform(new TechniqueContext(player, technique, Rank.GOD, 1.0, aim));
    }

    /** A clean stage: the caster at the arena's middle facing south, slightly down, a dummy ahead. */
    private static void setUp(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        level.getEntitiesOfClass(Entity.class, player.getBoundingBox().inflate(64.0),
                entity -> entity instanceof WaterSheet || entity instanceof SpellZone || entity instanceof SpellPillar
                        || entity instanceof SpellFire || entity instanceof SpellProjectile || entity instanceof SpellBeam
                        || entity instanceof Husk || entity instanceof AreaEffectCloud
                        || entity instanceof MushoMonster || entity instanceof ThrownTrick).forEach(Entity::discard);
        BlockPos.betweenClosed(player.blockPosition().offset(-14, -2, -14), player.blockPosition().offset(14, 10, 14)).forEach(pos -> {
            if (level.getBlockState(pos).getBlock() instanceof ConjuredBlock) {
                level.removeBlock(pos, false);
            }
        });
        level.setWeatherParameters(6000, 0, false, false);
        level.setDayTime(6000);
        // a teleport also turns the client's own view (setting the rotation on the server alone does not)
        player.teleportTo(level, arena.getX() + 0.5, arena.getY(), arena.getZ() + 0.5, 0.0F, 12.0F);
        player.setHealth(player.getMaxHealth());
        player.clearFire();
        player.removeAllEffects();
        Husk dummy = EntityType.HUSK.create(level);
        if (dummy != null) {
            dummy.moveTo(arena.getX() + 0.5, arena.getY(), arena.getZ() + 0.5 + DUMMY_DISTANCE, 180.0F, 0.0F);
            dummy.setNoAi(true);
            dummy.setInvulnerable(true);
            level.addFreshEntity(dummy);
        }
    }

    /** Logs which spell bodies the client can see, to tell a spell drawn wrongly from one never drawn. */
    private static void logVisible(Minecraft minecraft, String exhibit) {
        StringBuilder seen = new StringBuilder();
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof WaterSheet || entity instanceof SpellZone || entity instanceof SpellPillar
                    || entity instanceof SpellFire || entity instanceof SpellProjectile || entity instanceof SpellBeam) {
                seen.append(entity.getType().builtInRegistryHolder().key().location().getPath())
                        .append(String.format(Locale.ROOT, "@%.1f ", entity.distanceTo(minecraft.player)));
            }
        }
        MushoCraft.LOGGER.info("Gallery {}: visible bodies [{}] player at {}", exhibit, seen.toString().trim(),
                minecraft.player.position());
    }

    private static void shoot(Minecraft minecraft, String name) {
        Screenshot.grab(minecraft.gameDirectory, name + ".png", minecraft.getMainRenderTarget(),
                message -> MushoCraft.LOGGER.info("Gallery screenshot {}: {}", name, message.getString()));
    }

    private static void onServer(Consumer<ServerPlayer> action) {
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null) {
            return;
        }
        server.execute(() -> {
            ServerPlayer player = server.getPlayerList().getPlayers().stream().findFirst().orElse(null);
            if (player != null) {
                action.accept(player);
            }
        });
    }
}
