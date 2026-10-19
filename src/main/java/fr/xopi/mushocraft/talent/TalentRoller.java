package fr.xopi.mushocraft.talent;

import net.minecraft.util.RandomSource;

/**
 * Strategy that decides the talents a character is born with.
 * Each race will get its own roller once other races exist; v1 only has humans.
 */
@FunctionalInterface
public interface TalentRoller {
    Talents roll(RandomSource random);
}
