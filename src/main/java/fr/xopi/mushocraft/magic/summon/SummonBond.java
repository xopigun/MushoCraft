package fr.xopi.mushocraft.magic.summon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.Optional;
import java.util.UUID;

/**
 * The covenant binding a summoned creature to its summoner (wiki, <i>Magic Spells</i> § Summoning Magic): whom it
 * serves and until when (game time). Attached to the creature and saved with it; once it runs out, the creature is
 * free again.
 */
public record SummonBond(Optional<UUID> summoner, long until) {
    public static final Codec<SummonBond> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf("summoner").forGetter(SummonBond::summoner),
            Codec.LONG.optionalFieldOf("until", 0L).forGetter(SummonBond::until)
    ).apply(instance, SummonBond::new));

    public static SummonBond none() {
        return new SummonBond(Optional.empty(), 0L);
    }

    public static SummonBond to(UUID summoner, long until) {
        return new SummonBond(Optional.of(summoner), until);
    }

    public boolean binds(long gameTime) {
        return summoner.isPresent() && gameTime < until;
    }

    public boolean servant(UUID player, long gameTime) {
        return binds(gameTime) && summoner.get().equals(player);
    }
}
