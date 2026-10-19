package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.combat.mastery.MasteryRules;
import fr.xopi.mushocraft.combat.mastery.MasteryService;
import fr.xopi.mushocraft.core.MushoAttachments;
import fr.xopi.mushocraft.life.LifeService;
import fr.xopi.mushocraft.magic.entity.SpellProjectile;
import fr.xopi.mushocraft.magic.mana.ManaService;
import fr.xopi.mushocraft.magic.mana.ManaTraining;
import fr.xopi.mushocraft.magic.spell.IncantationMode;
import fr.xopi.mushocraft.magic.spell.IncantationRules;
import fr.xopi.mushocraft.magic.spell.MagicService;
import fr.xopi.mushocraft.magic.spell.ManaControl;
import fr.xopi.mushocraft.magic.spell.MushoSpells;
import fr.xopi.mushocraft.magic.spell.Spell;
import fr.xopi.mushocraft.magic.spell.SpellCasting;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(MushoCraft.MODID)
@PrefixGameTestTemplate(false)
public final class IncantationGameTests {
    private static final String EMPTY = "empty";

    private IncantationGameTests() {
    }

    private static ServerPlayer caster(GameTestHelper helper, double age) {
        ServerPlayer player = TestPlayers.join(helper);
        Vec3 spot = helper.absoluteVec(new Vec3(1.5, 1.0, 1.5));
        player.moveTo(spot.x, spot.y, spot.z, 0.0F, 0.0F);
        LifeService.setAge(player, age);
        ManaService.setTrained(player, 500.0);
        ManaService.refill(player);
        return player;
    }

    @GameTest(template = EMPTY)
    public static void practiceShortensTheIncantation(GameTestHelper helper) {
        ManaControl untrained = ManaControl.none();
        ManaControl silentHands = new ManaControl(IncantationRules.SILENT_CASTING_CONTROL);
        helper.assertValueEqual(IncantationRules.mode(0.0, untrained), IncantationMode.FULL, "a new spell is chanted in full");
        helper.assertValueEqual(IncantationRules.mode(IncantationRules.ABBREVIATED_AT, untrained), IncantationMode.ABBREVIATED,
                "a spell repeated far beyond mastery can be truncated");
        helper.assertValueEqual(IncantationRules.mode(MasteryRules.REPETITIONS_TO_MASTER, silentHands), IncantationMode.SILENT,
                "with the control of mana, a mastered spell needs no words");
        helper.assertValueEqual(IncantationRules.mode(10.0, silentHands), IncantationMode.FULL, "but not a spell still unmastered");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void silentCastingIsLearnedYoung(GameTestHelper helper) {
        ServerPlayer toddler = caster(helper, 3.0);
        ServerPlayer adult = caster(helper, 25.0);
        for (ServerPlayer player : new ServerPlayer[]{toddler, adult}) {
            ManaTraining.setCirculating(player, true);
            ManaTraining.tick(player, 10.0);
        }
        double young = MagicService.manaControl(toddler).practice();
        double old = MagicService.manaControl(adult).practice();
        helper.assertTrue(young > 0.0 && young >= old * 15.0, "a toddler learns it far faster (" + young + " vs " + old + ")");
        TestPlayers.leave(toddler);
        TestPlayers.leave(adult);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void aSilentCasterReleasesAtOnce(GameTestHelper helper) {
        ServerPlayer player = caster(helper, 20.0);
        Spell waterBall = MushoSpells.WATER_BALL.get();
        MagicService.learn(player, waterBall);
        MasteryService.setRepetitions(player, waterBall.ref(), MasteryRules.REPETITIONS_TO_MASTER);
        player.setData(MushoAttachments.MANA_CONTROL, new ManaControl(IncantationRules.SILENT_CASTING_CONTROL));

        helper.assertTrue(SpellCasting.begin(player, waterBall).success(), "cast");
        helper.assertTrue(SpellCasting.chanting(player).isEmpty(), "no chant at all");
        var balls = helper.getLevel().getEntitiesOfClass(SpellProjectile.class, player.getBoundingBox().inflate(4.0));
        helper.assertValueEqual(balls.size(), 1, "the water ball is already flying");
        balls.forEach(SpellProjectile::discard);
        TestPlayers.leave(player);
        helper.succeed();
    }
}
