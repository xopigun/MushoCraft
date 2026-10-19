package fr.xopi.mushocraft.magic.spell.meld;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * What a melded spell is made of. Mixed magic is « magic from multiple schools used in succession or combination »
 * (wiki, page <i>Magic</i>): whoever has mastered every part finds out how to meld them.
 */
public record MeldRecipe(List<MeldIngredient> ingredients) {
    public MeldRecipe {
        if (ingredients.size() < 2) {
            throw new IllegalArgumentException("A melded spell needs at least two parts");
        }
        ingredients = List.copyOf(ingredients);
    }

    public static MeldRecipe of(MeldIngredient... ingredients) {
        return new MeldRecipe(List.of(ingredients));
    }

    public boolean masteredBy(ServerPlayer player) {
        return ingredients.stream().allMatch(ingredient -> ingredient.masteredBy(player));
    }

    /** « Waterfall + Heat Island + Icicle Field ». */
    public Component describe() {
        MutableComponent text = Component.empty();
        for (int i = 0; i < ingredients.size(); i++) {
            if (i > 0) {
                text.append(" + ");
            }
            text.append(ingredients.get(i).describe());
        }
        return text;
    }
}
