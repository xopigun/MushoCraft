package fr.xopi.mushocraft.magic.summon;

import fr.xopi.mushocraft.magic.mana.ManaService;
import fr.xopi.mushocraft.magic.spell.MagicService;
import fr.xopi.mushocraft.magic.spell.SpellSchool;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Locale;

/**
 * A summoning paper: a summoning circle drawn in advance, « mostly used in battle or when a summoning circle is
 * unavailable » (wiki, <i>Summoning Magic</i>). Pour mana into it and it performs its summoning, then is spent. Anyone
 * with the mana can use one: summoning « requires minimal control to learn » but demands much mana.
 */
public class SummoningPaperItem extends Item {
    private static final int COOLDOWN_TICKS = 40;

    private final SummonRitual ritual;

    public SummoningPaperItem(Properties properties, SummonRitual ritual) {
        super(properties);
        this.ritual = ritual;
    }

    public SummonRitual ritual() {
        return ritual;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer summoner)) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        if (!ManaService.trySpend(summoner, ritual.manaCost())) {
            summoner.displayClientMessage(Component.translatable("message.mushocraft.summon.not_enough_mana",
                    String.format(Locale.ROOT, "%.0f", ritual.manaCost())), true);
            return InteractionResultHolder.fail(stack);
        }
        if (!ritual.perform(summoner)) {
            ManaService.restore(summoner, ritual.manaCost());
            summoner.displayClientMessage(Component.translatable("message.mushocraft.summon.nothing_answers"), true);
            return InteractionResultHolder.fail(stack);
        }
        MagicService.gainExperience(summoner, SpellSchool.SUMMONING, ritual.manaCost());
        level.playSound(null, player.blockPosition(), SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.PLAYERS, 0.8F, 1.2F);
        summoner.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        stack.consume(1, summoner);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(ritual.description().copy().withStyle(style -> style.withColor(0xC0C0C0).withItalic(true)));
        tooltip.add(Component.translatable("item.mushocraft.summoning_paper.cost",
                String.format(Locale.ROOT, "%.0f", ritual.manaCost())).withStyle(style -> style.withColor(0x7FB8FF)));
    }
}
