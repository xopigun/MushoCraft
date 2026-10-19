package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.talent.GaussianTalentRoller;
import fr.xopi.mushocraft.talent.Talent;
import fr.xopi.mushocraft.talent.Talents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(MushoCraft.MODID)
@PrefixGameTestTemplate(false)
public final class TalentGameTests {
    private static final String EMPTY = "empty";
    private static final int ROLLS = 1_000;

    private TalentGameTests() {
    }

    @GameTest(template = EMPTY)
    public static void rolledTalentsStayWithinBounds(GameTestHelper helper) {
        // A huge spread forces many rolls to hit the bounds, so clamping is really exercised.
        GaussianTalentRoller roller = new GaussianTalentRoller(1.0, 5.0, 0.5, 1.5);
        RandomSource random = RandomSource.create(42L);
        for (int i = 0; i < ROLLS; i++) {
            Talents talents = roller.roll(random);
            for (Talent talent : Talent.values()) {
                double value = talents.get(talent);
                helper.assertTrue(value >= 0.5 && value <= 1.5, talent + " out of bounds: " + value);
            }
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void missingTalentReadsAsNeutral(GameTestHelper helper) {
        Talents talents = Talents.neutral().with(Talent.STRENGTH, 1.3);
        helper.assertValueEqual(talents.get(Talent.STRENGTH), 1.3, "talent that was set");
        helper.assertValueEqual(talents.get(Talent.SPEED), Talents.NEUTRAL, "talent that was never set");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void talentsSurviveSerialization(GameTestHelper helper) {
        Talents original = new GaussianTalentRoller(1.0, 0.15, 0.5, 1.5).roll(RandomSource.create(7L));
        Tag encoded = Talents.CODEC.encodeStart(NbtOps.INSTANCE, original).getOrThrow();
        Talents decoded = Talents.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow();
        helper.assertValueEqual(decoded, original, "talents after a save and load");
        helper.succeed();
    }
}
