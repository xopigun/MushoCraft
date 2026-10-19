package fr.xopi.mushocraft.magic.spell;

import fr.xopi.mushocraft.core.Rank;
import fr.xopi.mushocraft.magic.entity.SpellProjectile;
import fr.xopi.mushocraft.npc.MagicTutorEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

import java.util.Optional;

/**
 * Recognises Divine Strike magic in a blow, and how far its caster has mastered the school: the only magic that rids the
 * world of a wraith for good, an advanced one for a Deadly Wraith (wiki, page <i>Monsters</i>).
 */
public final class DivineStrike {
    private DivineStrike() {
    }

    public static boolean isDivine(DamageSource source) {
        return source.getDirectEntity() instanceof SpellProjectile projectile && projectile.element() == SpellElement.LIGHT;
    }

    /** The Divine Strike rank of whoever cast the blow, if it is divine. */
    public static Optional<Rank> rank(DamageSource source) {
        if (!isDivine(source)) {
            return Optional.empty();
        }
        if (source.getEntity() instanceof ServerPlayer player) {
            return Optional.of(MagicService.proficiency(player).rank(SpellSchool.DIVINE_STRIKE));
        }
        if (source.getEntity() instanceof MagicTutorEntity tutor && tutor.school() == SpellSchool.DIVINE_STRIKE) {
            return Optional.of(tutor.rank());
        }
        return Optional.of(Rank.BEGINNER);
    }
}
