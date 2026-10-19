package fr.xopi.mushocraft.gametest;

import fr.xopi.mushocraft.MushoCraft;
import fr.xopi.mushocraft.compat.epicfight.EpicFightSwordStyles;
import fr.xopi.mushocraft.compat.epicfight.EpicFightWeapons;
import fr.xopi.mushocraft.sword.SwordService;
import fr.xopi.mushocraft.sword.SwordStyle;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.registry.entries.EpicFightItems;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

@GameTestHolder(MushoCraft.MODID)
@PrefixGameTestTemplate(false)
public final class StyleMovesetGameTests {
    private static final String EMPTY = "empty";

    private StyleMovesetGameTests() {
    }

    private static PlayerPatch<?> armed(GameTestHelper helper, ServerPlayer player, ItemStack weapon) {
        player.setItemInHand(InteractionHand.MAIN_HAND, weapon);
        PlayerPatch<?> patch = EpicFightCapabilities.getPlayerPatch(player);
        helper.assertTrue(patch != null, "test player has an Epic Fight patch");
        return patch;
    }

    private static CapabilityItem capability(ItemStack weapon) {
        return EpicFightCapabilities.getItemStackCapability(weapon);
    }

    @GameTest(template = EMPTY)
    public static void swordGodGetsItsOwnComboOnASword(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.join(helper);
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        PlayerPatch<?> patch = armed(helper, player, sword);

        helper.assertValueEqual(capability(sword).getAutoAttackMotion(patch).getFirst(), Animations.SWORD_AUTO1,
                "no style: Epic Fight's sword combo");

        SwordService.adopt(player, SwordStyle.SWORD_GOD);
        helper.assertValueEqual(capability(sword).getStyle(patch), EpicFightSwordStyles.MUSHO_SWORD_GOD, "style chosen");
        helper.assertValueEqual(capability(sword).getAutoAttackMotion(patch).getFirst(), Animations.UCHIGATANA_AUTO1,
                "Sword God combo");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void eachStyleHasItsComboOnATachi(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.join(helper);
        ItemStack tachi = new ItemStack(EpicFightItems.IRON_TACHI.get());
        PlayerPatch<?> patch = armed(helper, player, tachi);

        SwordService.adopt(player, SwordStyle.WATER_GOD);
        helper.assertValueEqual(capability(tachi).getAutoAttackMotion(patch).size(), 4, "Water God: two cuts, dash and air");
        SwordService.adopt(player, SwordStyle.NORTH_GOD);
        helper.assertValueEqual(capability(tachi).getAutoAttackMotion(patch).get(1), Animations.LONGSWORD_AUTO2,
                "North God mixes weapons' cuts");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void swordStylesReachEverySword(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.join(helper);
        SwordService.adopt(player, SwordStyle.SWORD_GOD);
        ItemStack longsword = new ItemStack(EpicFightItems.IRON_LONGSWORD.get());
        PlayerPatch<?> patch = armed(helper, player, longsword);
        helper.assertValueEqual(capability(longsword).getAutoAttackMotion(patch).getFirst(), Animations.UCHIGATANA_AUTO1,
                "Sword God on a longsword");
        helper.assertTrue(EpicFightWeapons.holdsWithBothHands(player), "a longsword is held with both hands");
        ItemStack dagger = new ItemStack(EpicFightItems.IRON_DAGGER.get());
        armed(helper, player, dagger);
        helper.assertValueEqual(capability(dagger).getStyle(patch), EpicFightSwordStyles.MUSHO_SWORD_GOD, "and on a dagger");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void theNorthGodFightsWithAPairOfSwords(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.join(helper);
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        PlayerPatch<?> patch = armed(helper, player, sword);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.IRON_SWORD));
        SwordService.adopt(player, SwordStyle.SWORD_GOD);
        helper.assertFalse(capability(sword).getStyle(patch) instanceof EpicFightSwordStyles, "the Sword God fights with one sword");
        SwordService.adopt(player, SwordStyle.NORTH_GOD);
        helper.assertValueEqual(capability(sword).getStyle(patch), EpicFightSwordStyles.MUSHO_NORTH_GOD_DUAL, "two swords, North God");
        helper.assertValueEqual(capability(sword).getAutoAttackMotion(patch).get(1), Animations.DAGGER_DUAL_AUTO2,
                "its own two-weapon combo");
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        helper.assertValueEqual(capability(sword).getStyle(patch), EpicFightSwordStyles.MUSHO_NORTH_GOD, "one sword again");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void onlyTheNorthGodFightsWithAnAxe(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.join(helper);
        ItemStack axe = new ItemStack(Items.IRON_AXE);
        PlayerPatch<?> patch = armed(helper, player, axe);
        SwordService.adopt(player, SwordStyle.SWORD_GOD);
        helper.assertValueEqual(capability(axe).getAutoAttackMotion(patch).getFirst(), Animations.AXE_AUTO1,
                "a style of the sword leaves the axe its own combo");
        helper.assertFalse(capability(axe).getStyle(patch) == EpicFightSwordStyles.MUSHO_SWORD_GOD, "no Sword God axe");
        SwordService.adopt(player, SwordStyle.NORTH_GOD);
        helper.assertValueEqual(capability(axe).getStyle(patch), EpicFightSwordStyles.MUSHO_NORTH_GOD, "the North God fights with it");
        helper.assertValueEqual(capability(axe).getAutoAttackMotion(patch).get(1), Animations.SWORD_AUTO2,
                "mixing in other weapons' cuts");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void dualWieldingKeepsEpicFightsMovesetOutsideTheNorthGod(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.join(helper);
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        PlayerPatch<?> patch = armed(helper, player, sword);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.IRON_SWORD));
        SwordService.adopt(player, SwordStyle.WATER_GOD);

        helper.assertValueEqual(capability(sword).getStyle(patch), CapabilityItem.Styles.TWO_HAND, "two swords: Epic Fight's dual style");
        TestPlayers.leave(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void aTachiStaysTwoHandedUnderAStyle(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.join(helper);
        SwordService.adopt(player, SwordStyle.SWORD_GOD);

        armed(helper, player, new ItemStack(EpicFightItems.IRON_TACHI.get()));
        helper.assertTrue(EpicFightWeapons.holdsWithBothHands(player), "a tachi is held with both hands (Longsword of Light)");
        armed(helper, player, new ItemStack(Items.IRON_SWORD));
        helper.assertFalse(EpicFightWeapons.holdsWithBothHands(player), "a sword is one-handed");
        TestPlayers.leave(player);
        helper.succeed();
    }
}
