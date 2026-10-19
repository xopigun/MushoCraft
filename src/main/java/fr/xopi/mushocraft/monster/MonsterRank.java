package fr.xopi.mushocraft.monster;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * The Adventurers' Guild's threat ranks for monsters, from F (an untrained adult deals with it) to S (wiki, page
 * <i>Monsters</i>). A pack can be ranked above its members, as Terminate Boars leading Assault Dogs.
 */
public enum MonsterRank implements StringRepresentable {
    F, E, D, C, B, A, S;

    public Component displayName() {
        return Component.translatable("monster_rank.mushocraft." + getSerializedName());
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
