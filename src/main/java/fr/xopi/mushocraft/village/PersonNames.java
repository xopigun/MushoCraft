package fr.xopi.mushocraft.village;

import net.minecraft.util.RandomSource;

import java.util.List;

/**
 * Names of the common folk of the Asura Kingdom's countryside. The novel's villagers bear plain European-sounding
 * names and a family name (Somar Eto, of Buena Village); these lists are made up in that spirit and never reuse a
 * character's name.
 */
public final class PersonNames {
    private static final List<String> MALE = List.of("Albert", "Bernd", "Conrad", "Dietmar", "Emil", "Falk", "Gustav",
            "Hagen", "Ivo", "Jonas", "Kaspar", "Lukas", "Matthis", "Nils", "Otto", "Pieter", "Rolf", "Sven", "Tobias",
            "Ulrich", "Valentin", "Willem", "Anselm", "Bruno", "Edwin", "Gerold", "Heiko", "Lorenz");
    private static final List<String> FEMALE = List.of("Anna", "Berta", "Clara", "Dora", "Elsa", "Frida", "Greta",
            "Hanna", "Ilse", "Johanna", "Katrin", "Lena", "Marta", "Nora", "Ottilie", "Paula", "Rosa", "Senta", "Tilda",
            "Ursula", "Vera", "Wilma", "Adele", "Bettina", "Edith", "Gisela", "Helga", "Liesel");
    private static final List<String> FAMILY = List.of("Abel", "Brandt", "Dorn", "Feld", "Haas", "Kern", "Lang", "Mohr",
            "Roth", "Stein", "Vogt", "Weber", "Ziegler", "Amsel", "Bauer", "Falkner", "Hirt", "Kessler", "Linde", "Mahler",
            "Reuter", "Sommer", "Tanner", "Wendt");

    private PersonNames() {
    }

    public static String given(Sex sex, RandomSource random) {
        List<String> names = sex == Sex.MALE ? MALE : FEMALE;
        return names.get(random.nextInt(names.size()));
    }

    public static String family(RandomSource random) {
        return FAMILY.get(random.nextInt(FAMILY.size()));
    }
}
