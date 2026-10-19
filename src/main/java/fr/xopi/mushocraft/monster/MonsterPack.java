package fr.xopi.mushocraft.monster;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Queries and orders on a pack: its members around a monster, its leader, the call to hunt, and who leads when the
 * leader falls (the strongest member left takes its place at once).
 */
public final class MonsterPack {
    /** How far packmates hear each other. */
    public static final double RANGE = 32.0;

    private MonsterPack() {
    }

    /** The other members of the monster's pack within range. */
    public static List<MushoMonster> mates(MushoMonster monster) {
        if (monster.pack().isEmpty()) {
            return List.of();
        }
        return monster.level().getEntitiesOfClass(MushoMonster.class, new AABB(monster.blockPosition()).inflate(RANGE),
                other -> other != monster && other.isAlive() && monster.inSamePack(other));
    }

    public static Optional<MushoMonster> leader(MushoMonster monster) {
        if (monster.isPackLeader()) {
            return Optional.of(monster);
        }
        return mates(monster).stream().filter(MushoMonster::isPackLeader).findFirst();
    }

    /** The whole pack in range turns on the prey, unless already busy with another. */
    public static void callToHunt(MushoMonster caller, LivingEntity prey) {
        for (MushoMonster mate : mates(caller)) {
            if (mate.getTarget() == null || !mate.getTarget().isAlive()) {
                mate.setTarget(prey);
            }
        }
    }

    /** The leader fell: the strongest member left leads the pack from now on. */
    public static void succeed(MushoMonster fallen) {
        mates(fallen).stream()
                .max(Comparator.comparingDouble(LivingEntity::getMaxHealth).thenComparingDouble(LivingEntity::getHealth))
                .ifPresent(next -> next.joinPack(next.pack().orElseThrow(), true));
    }

    /** A pack found without a living leader nearby (it wandered off, or the save lost it) picks a new one. */
    public static void ensureLeader(MushoMonster monster) {
        if (monster.pack().isPresent() && leader(monster).isEmpty()) {
            List<MushoMonster> pack = new ArrayList<>(mates(monster));
            pack.add(monster);
            pack.stream().max(Comparator.<MushoMonster>comparingDouble(LivingEntity::getMaxHealth).thenComparing(Entity::getUUID))
                    .ifPresent(next -> next.joinPack(next.pack().orElseThrow(), true));
        }
    }
}
