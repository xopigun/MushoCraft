# MushoCraft

A Minecraft mod inspired by the world of *Mushoku Tensei*: be born as a baby, grow up, and follow the path of magic, of the sword, or both.

> Early development. Nothing is playable yet beyond birth, aging and natural talents.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.263 or newer
- [Epic Fight](https://modrinth.com/mod/epic-fight) 21.17.3 or newer (required, installed separately — it is never bundled with MushoCraft)

## Building

```
./gradlew build
```

Gradle 8.8 needs a JDK 17 to run (`JAVA_HOME`); Java 21 for compiling is downloaded automatically.

Useful tasks:

| Task | What it does |
|---|---|
| `runClient` | Starts a development client with Epic Fight |
| `runClientTestWorld` | Opens the save `run/saves/mushotest` directly |
| `runGameTestServer` | Runs the automated in-game tests (`src/gametest`) and exits |

## Design documents (French)

- [Architecture](docs/ARCHITECTURE.md)
- [Glossary](docs/GLOSSAIRE.md): spell, technique and place names, English and French
- [Consistency sheet](docs/FICHE_COHERENCE.md): every feature against its source in the novel
- [Deliberate deviations](docs/ECARTS.md) from the novel

## License and assets

MushoCraft is licensed under the [GNU GPL v3](LICENSE).

- No asset from Epic Fight is copied, modified or redistributed; existing Epic Fight animations are only referenced from code.
- No illustration, music, logo or text from the *Mushoku Tensei* novels or anime is included. Spell and place names come from the [Mushoku Tensei Wiki](https://mushokutensei.fandom.com).
- *Mushoku Tensei* belongs to Rifujin na Magonote. This is an unofficial fan project.
