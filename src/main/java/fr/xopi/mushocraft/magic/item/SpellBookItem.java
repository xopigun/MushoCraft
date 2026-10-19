package fr.xopi.mushocraft.magic.item;

import fr.xopi.mushocraft.magic.spell.MagicService;
import fr.xopi.mushocraft.magic.spell.Spell;
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
import java.util.function.Supplier;

/**
 * A book that teaches the spells it describes when read. Rudeus learns magic alone from such a textbook
 * (LN vol. 1 ch. 3 « A Textbook of Magic », per the wiki). The book is kept after reading.
 */
public class SpellBookItem extends Item {
    private final Supplier<List<Spell>> spells;

    public SpellBookItem(Properties properties, Supplier<List<Spell>> spells) {
        super(properties);
        this.spells = spells;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            boolean learnedAny = false;
            boolean allKnown = true;
            for (Spell spell : spells.get()) {
                MagicService.StudyResult result = MagicService.study(serverPlayer, spell);
                learnedAny |= result == MagicService.StudyResult.LEARNED;
                allKnown &= result == MagicService.StudyResult.ALREADY_KNOWN;
                if (result == MagicService.StudyResult.NOT_READY) {
                    serverPlayer.sendSystemMessage(Component.translatable("message.mushocraft.magic.not_ready",
                            spell.displayName(), spell.school().displayName(), spell.rank().displayName()));
                }
            }
            if (allKnown) {
                serverPlayer.displayClientMessage(Component.translatable("message.mushocraft.magic.book_known"), true);
            } else if (!learnedAny) {
                serverPlayer.displayClientMessage(Component.translatable("message.mushocraft.magic.book_too_hard"), true);
            }
            level.playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.mushocraft.spell_book.tooltip").withStyle(style -> style.withColor(0xB0B0B0)));
        spells.get().forEach(spell -> tooltip.add(Component.literal(" • ").append(spell.displayName())
                .withStyle(style -> style.withColor(0x7FB8FF))));
    }
}
