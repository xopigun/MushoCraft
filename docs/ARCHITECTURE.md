# MushoCraft — Plan d'architecture

Version du 19/10/2026. Rien n'est codé. Les décisions validées sont listées au §0 ; les points encore marqués **[À VALIDER]** attendent l'accord de Xopi.

## 0. Décisions déjà prises

| Date | Décision |
|---|---|
| 19/10/2026 | Le mode combat de MushoCraft **est** le mode combat d'Epic Fight (touche R par défaut). La barre de move s'affiche quand Epic Fight passe en `PlayerMode.EPICFIGHT`. Alt reste l'esquive d'Epic Fight. |
| 19/10/2026 | Le catalogue doit couvrir **tous** les sorts du roman et du wiki. |
| 19/10/2026 | Langue : le mod est en **anglais** par défaut (`en_us`), traduit en **français** (`fr_fr`). Le glossaire fixe les deux orthographes. |
| 19/10/2026 | L'aura de combat **puise dans le réservoir de mana**. Tout le monde peut l'apprendre, avec un **facteur de facilité aléatoire** tiré à la naissance. |
| 19/10/2026 | Tout le monde peut utiliser la magie, mais il faut **l'apprendre** pour pouvoir lancer des sorts. |
| 19/10/2026 | La croissance du réservoir de mana **continue faiblement** à l'âge adulte. |
| 19/10/2026 | Un maître peut faire monter son élève jusqu'à un **rang égal au sien**. |
| 19/10/2026 | En mode combat, la **molette change d'arme**. |
| 19/10/2026 | Conception des sorts : un objet `Spell` par sort dans un registre Java typé, des classes `SpellEffect` polymorphes, une classe dédiée pour chaque comportement unique, le datapack réservé aux chiffres d'équilibrage (§4.2). Même schéma pour les techniques d'épée. |

## 1. Principes

- **Serveur autoritaire.** Mana, cooldowns, apprentissage et lancement de sorts sont calculés côté serveur. Le client affiche et envoie des demandes.
- **Une classe par comportement, un objet par sort.** Chaque sort est un objet `Spell` déclaré en Java dans un registre typé. Son comportement est composé d'objets `SpellEffect` polymorphes, et un comportement unique a sa propre classe. Le JSON de datapack ne sert qu'à l'équilibrage des chiffres. Détail et justification au §4.2 (validé).
- **Epic Fight isolé derrière une façade.** Tous les appels à Epic Fight passent par le paquet `compat.epicfight`. Le reste du mod ne parle qu'à nos interfaces. Une mise à jour d'Epic Fight ne touche qu'un seul paquet.
- **Patrons utilisés :**
  - Strategy : effets de sort, formules de croissance.
  - Registry : sorts, styles, techniques.
  - Command : exécution d'un move.
  - State : étapes de vie.
  - Adapter/Façade : Epic Fight.
  - Observer : événements NeoForge et événements Epic Fight.

## 2. Découpage en paquets (`fr.xopi.mushocraft`)

| Paquet | Rôle |
|---|---|
| `core` | Registres (`DeferredRegister`), types d'attachments, configuration, constantes |
| `life` | Naissance, âge, étapes de vie (bébé, enfant, adolescent, adulte), talents tirés à la naissance |
| `magic.mana` | Réservoir de mana : capacité, plafond, régénération, croissance |
| `magic.spell` | `SpellDefinition` (données), `SpellEffect` (stratégies), modes d'incantation, écoles, rangs |
| `sword.stats` | Force, vitalité, vitesse, résistance : valeurs, entraînement, application en `AttributeModifier` |
| `sword.aura` | Aura de combat (闘気), voir §5.3 |
| `sword.style` | Styles d'escrime, maîtrise, techniques, cooldown partagé par style |
| `sword.master` | PNJ maîtres : niveau, enseignement, IA de combat Epic Fight |
| `combat` | Abstraction `Move` (sort **ou** technique), loadout de 9 slots, cooldowns |
| `client.hud` | Barre de move, overlay de cooldown, jauge de mana |
| `client.input` | Touches 1-9 en mode combat, charge des sorts sans incantation |
| `network` | Payloads `CustomPacketPayload` + `StreamCodec` |
| `compat.epicfight` | Façade : mode joueur, styles et movesets, animations, patchs de PNJ |
| `data` | Datagen : langues, JSON de sorts et de styles, tags |

## 3. Données persistantes (attachments NeoForge sur le joueur)

| Attachment | Contenu | Mort |
|---|---|---|
| `LifeData` | Date de naissance (ticks de jeu), étape de vie, graine de tirage | conservé |
| `Talents` | Facteurs tirés une fois : potentiel de mana, talent par stat, aptitude à l'aura | conservé |
| `ManaData` | Capacité actuelle, plafond théorique, mana courant, mana dépensé (compteur de croissance) | conservé (mana courant remis à 0 ?) |
| `BodyStats` | XP d'entraînement par stat, valeur dérivée | conservé |
| `MagicKnowledge` | Sorts connus, niveau par école, modes d'incantation débloqués | conservé |
| `SwordMastery` | Styles connus, rang par style, techniques apprises, style actif | conservé |
| `MoveLoadout` | 9 slots → id de move | conservé |
| `Cooldowns` | Cooldowns de moves et de styles (en ticks de jeu) | conservé |

- Tout est sérialisé par `Codec` et copié à la mort (`copyOnDeath`).
- La synchronisation vers le client se fait par nos propres paquets. À ma connaissance, NeoForge 21.1 ne synchronise pas les attachments automatiquement : à vérifier.

## 4. Système de magie

### 4.1 Mana (fidèle au wiki, page *Magic*)

- **Naissance.** Le roman dit que la réserve de mana est héritée et fixée à la naissance. Au tout premier login, on tire :
  - `potential` : facteur aléatoire, distribution centrée et bornée. Il dépend de la race, mais seuls les humains existent en v1.
  - `ceiling` : plafond théorique = `potential × plafondHumain`. Le facteur de naissance reste donc déterminant.
- **Croissance par l'usage.** Le wiki dit que tout le monde peut augmenter sa mana en la stimulant *dès le plus jeune âge* (cas de Julie), et que l'apprentissage de l'incantation silencieuse est aussi lié à la pratique jeune. Proposition de formule (Strategy, réglable en config) :

  `Δcapacité = k × manaDépensé × facteurÂge(âge) × (1 − capacité / plafond)`

  - `facteurÂge` est fort chez l'enfant et faible, mais jamais nul, chez l'adulte (décidé).
- **Apprentissage obligatoire.** Tout le monde a un réservoir, mais on ne lance aucun sort avant d'avoir appris la magie : premier sort appris via un manuel ou un professeur.
  - Le terme `(1 − capacité / plafond)` donne des rendements décroissants : on n'atteint jamais le plafond.
- **Pas de rang** dans la mana : la capacité est un nombre, sans lien avec un rang.
- **Régénération** au repos, plus rapide en dormant **[À VALIDER]**.

### 4.2 Sorts : un objet par sort, une classe par comportement (validé le 19/10)

**Le problème.** *Waterball*, *Fireball* et *Rock Bullet* font la même chose : un projectile qu'on matérialise puis qu'on lance. Seuls changent l'élément, les dégâts, la vitesse et le rendu.
- Avec une classe par sort, soit on copie trois fois la même logique (DRY violé), soit on empile des héritages (`Spell` → `ProjectileSpell` → `WaterProjectileSpell`…). Ces hiérarchies deviennent rigides : il faut casser l'arbre dès qu'un sort mélange deux comportements, ce qui arrive avec la magie mixte.
- À l'inverse, ma première formulation (« des fichiers JSON ») était trop raccourcie. Un sort entièrement décrit en JSON perd le typage, le refactoring et les erreurs à la compilation, et ce n'est plus vraiment de l'objet.

**La conception proposée** (composition plutôt qu'héritage) :

| Élément | Rôle | Patron |
|---|---|---|
| `Spell` | Un objet par sort, déclaré en Java avec un builder typé dans un registre NeoForge `mushocraft:spell`. Il porte : école, rang, coût, temps d'incantation, recharge, effet. | *Type Object* |
| `SpellEffect` | Interface du comportement : `cast(SpellContext)`. Une classe par comportement : `ProjectileEffect`, `WallEffect`, `AreaEffect`, `HealEffect`… | Strategy |
| `SequenceEffect` | Enchaîne plusieurs effets. Sert à la magie mixte : *Frost Nova* = *Water Splash* puis *Icicle Field* (wiki). | Composite |
| Comportements uniques | Les sorts qui ne ressemblent à aucun autre ont **leur propre classe d'effet** : *Lightning* (nuage puis éclair), *Disturb Magic*, *Spirit Summon*, *Gravity Magic*… | Strategy |
| `SpellCaster` | Moteur : vérifie le mana, l'apprentissage et la recharge, puis délègue à l'effet. Ne connaît aucun sort précis. | — |
| Datapack (optionnel) | Ne contient **que** des chiffres d'équilibrage (coût, dégâts, recharge), qui écrasent les valeurs par défaut. C'est le même principe que les `skill_parameters` d'Epic Fight. | — |

**Ce que ça respecte :**
- **Responsabilité unique :** un effet sait produire un phénomène ; le moteur sait vérifier et payer ; le sort sait ce qu'il est.
- **Ouvert/fermé :** ajouter un sort = une déclaration, sans toucher au moteur. Ajouter un comportement = une nouvelle classe d'effet.
- **Substitution de Liskov :** tout `SpellEffect` est interchangeable pour le moteur.
- **Inversion des dépendances :** le moteur dépend de l'interface `SpellEffect`, jamais d'une implémentation.

Le même schéma vaut pour les **techniques d'épée** : un objet `Technique` par technique, composé d'actions (jouer une animation Epic Fight, appliquer des propriétés de coup). Les mécaniques à part, comme la contre-attaque *Flow* du Style du Dieu de l'Eau, ont leur propre classe.

Estimation : une quinzaine de classes d'effets génériques, plus une classe pour chaque sort au comportement unique, couvrent toutes les entrées du glossaire.

### 4.3 Modes d'incantation (wiki, page *Magic*)

| Mode | Comportement |
|---|---|
| Incantation complète | Temps d'incantation, coût normal. Disponible dès qu'on connaît le sort. |
| Incantation abrégée | Temps réduit, à débloquer par la pratique. |
| Incantation silencieuse | Pas d'incantation. Le joueur **maintient** la touche : forme, puis ajout de puissance (taille), puis vitesse, puis relâchement. Ce déroulé suit le mécanisme en 4 temps décrit par le wiki. Apprentissage nettement plus facile enfant. |
| Cercles magiques | Objets ou blocs gravés : puissance fixe, lancement instantané quand on y verse du mana. Méthode principale de l'invocation selon le wiki. |

### 4.4 Rangs et apprentissage

- Le titre de mage = le rang du sort le plus avancé qu'on sait lancer (wiki). C'est un titre affiché, pas une contrainte sur la mana.
- **Apprentissage :**
  - manuel de magie (Rudeus apprend avec un manuel, LN1 ch. 3 selon le wiki) ;
  - maîtres et professeurs PNJ ;
  - pratique.
- Le wiki indique que la religion de Milis contrôle la barrière et la frappe divine au-delà du rang débutant : piste pour l'apprentissage de ces écoles.

## 5. Système d'épée

### 5.1 Stats

Chaque stat vaut : base humaine commune × (1 + tirage autour de 0, influencé par le talent) + gains d'entraînement. Le gain d'entraînement a des rendements décroissants et un plafond par personnage.

Les stats s'appliquent en `AttributeModifier` permanents (ids `mushocraft:stat/<nom>`). Epic Fight cumule bien les modificateurs du joueur avec ceux de l'arme (`PlayerPatch.getWeaponAttribute`).

| Stat | Attributs visés **[À VALIDER]** | S'entraîne par |
|---|---|---|
| Force | `attack_damage`, `epicfight:impact` | coups portés, dégâts infligés |
| Vitalité | `max_health`, `epicfight:stamina` | dégâts encaissés, endurance dépensée |
| Vitesse | `movement_speed`, `attack_speed`, `epicfight:stamina_regen` | sprint, esquives |
| Résistance | `armor`, `epicfight:stun_armor`, `knockback_resistance` | coups encaissés, gardes réussies |

Les stats suivent aussi l'âge : un bébé est faible, et l'échelle de taille vanilla `minecraft:generic.scale` sert à la croissance physique.

### 5.2 Styles d'escrime (wiki, catégorie *Swordstyle*)

- Trois styles : **Style du Dieu de l'Épée**, **Style du Dieu de l'Eau**, **Style du Dieu du Nord**. Les noms français sont à fixer dans le glossaire.
- Sept rangs de maîtrise, les mêmes pour les trois styles : Débutant, Intermédiaire, Avancé, Saint, Roi, Empereur, Dieu.
- **Branchement Epic Fight :**
  - Un `Style` Epic Fight par style MushoCraft (enum extensible).
  - Un `Moveset` par style, sur les catégories d'armes existantes (épée, épée longue, tachi…), via `WeaponModifier`.
  - Sélection par `ProviderConditional.createCustom(style, joueur → style actif == X)`. C'est le conditionnel de plus haute priorité.
  - Au changement de style, appeler `changeWeaponInnateSkill` et `modifyLivingMotionByCurrentItem` (vérifié dans le code d'Epic Fight).
- **Techniques spéciales.** Ce sont nos propres `Move`, pas des skills Epic Fight. Les slots de skills d'Epic Fight sont fixes et chaque skill a sa propre recharge, alors qu'on veut une recharge **commune au style**. L'exécution passe par `LivingEntityPatch.playAnimationSynchronized` avec une `AttackAnimation`. On commence par les animations existantes d'Epic Fight (référencées, jamais copiées) ; les nouvelles animations seront créées à partir de zéro.
- **Recharge partagée** : `cooldownStyle = base × f(rang de maîtrise)`, décroissante avec la maîtrise.
- **Techniques recensées sur le wiki** (orthographe à fixer) :
  - Dieu de l'Épée : *Longsword of Light*, *Light Reversal*, *Longsword of Silence*, *Arm Chop*, *Lake Surface Longsword*, *Crescent Moon Longsword*.
  - Dieu de l'Eau : *Flow*, les *Five Secret Arts*, *Deprivation Sword Kingdom*.
  - Dieu du Nord : lancer d'épée, *Four Legged Form*, *Hazy Cross*, *Teardrop Bullet*, *Crimson Ink*, *Pecking*, *Moonlight Cleave*, *Gravity Fracture*.

### 5.3 Aura de combat (Battle Aura, 闘気)

Le wiki (page *Battle Aura*, WN ch. 80) la décrit ainsi :
- c'est du **mana** qui enveloppe et renforce le corps ;
- elle s'acquiert par l'entraînement physique, pas par une incantation ;
- certains ne l'obtiennent jamais : Rudeus est le cas cité.
- Pour le Style du Dieu de l'Épée, la contrôler consciemment = rang Avancé, la contrôler complètement = rang Saint (d'après Ghislaine).

C'est le lien naturel entre épée et stats. **Décidé :**
- l'aura consomme du mana du réservoir commun ;
- tout le monde peut l'apprendre ;
- un facteur de facilité aléatoire, tiré à la naissance, règle la vitesse d'apprentissage.

Mise en œuvre proposée :
- un multiplicateur sur les stats tant que l'aura est active, avec un coût en mana par seconde ;
- trois niveaux de contrôle (inconscient, conscient, complet) liés aux rangs de style, d'après ce que dit Ghislaine.

Le refus de naître sans aptitude est un écart (voir `ECARTS.md`).

### 5.4 Maîtres

- PNJ humanoïdes avec un patch Epic Fight (`HumanoidMobPatch`), enregistré via `EpicFightEventHooks.Registry.ENTITY_PATCH`. Ce n'est pas le bus NeoForge : la doc d'Epic Fight est périmée sur ce point.
- IA de combat par `CombatBehaviors`. Les PNJ n'utilisent pas les movesets des objets : il faut leur donner des motions par catégorie et par style.
- Chaque maître a un style et un rang. **Décidé :** il peut faire monter son élève jusqu'à un rang égal au sien, pas au-delà.

## 6. Mode combat et barre de move

- **Activation.** On suit l'état d'Epic Fight (`PlayerPatch.isEpicFightMode()`, événement `TOGGLE_MODE`).
  - Côté client, le HUD lit l'état à chaque frame plutôt que de dépendre de l'événement. Raison : Epic Fight envoie son paquet avant que l'événement client puisse être annulé.
- **HUD.** En mode combat :
  - on masque le calque vanilla `HOTBAR` ;
  - on dessine 9 slots aux mêmes coordonnées que la hotbar vanilla ;
  - les cooldowns s'affichent comme `ItemCooldowns` : voile blanc qui se vide de bas en haut.
  - Les calques d'Epic Fight (endurance, skills, arme innée) restent à leur place. Il y a un risque de chevauchement de la barre d'endurance sous ~422 px de large en échelle GUI.
- **Touches 1-9.** En mode combat, elles lancent le move du slot ; la sélection d'objet ne change pas. On consomme les clics de `keyHotbarSlots` dans `ClientTickEvent.Pre`, avant le `handleKeybinds` vanilla et avant Epic Fight, qui les vide pendant ~20 ticks après une attaque. À confirmer en test.
- **Molette** en mode combat : **elle change d'arme** (décidé). La hotbar vanilla étant masquée, il faudra un petit indicateur de l'arme tenue près de la barre de move.
- **Loadout.** Le joueur range ses sorts et ses techniques dans les 9 slots, depuis un écran dédié.

## 7. Documentation (`docs/`)

| Fichier | Contenu |
|---|---|
| `FICHE_COHERENCE.md` | Une ligne par fonctionnalité : source (tome, chapitre, ou page du wiki) et citation courte |
| `GLOSSAIRE.md` | Sorts, techniques, styles, rangs, lieux : orthographe figée |
| `ECARTS.md` | Écarts volontaires avec le roman, justifiés |

Limite à connaître : je n'ai accès qu'au wiki et à ses références. Je ne peux pas lire le roman lui-même. Toute citation que je n'ai pas vue dans le texte sera marquée « à vérifier dans le tome ».

## 8. Ordre de développement proposé

1. ✅ Docs (squelette + entrées connues), nettoyage du contenu d'exemple du template, ajout du fichier `LICENSE` GPLv3.
2. ✅ `core`, `life`, `Talents` + commandes de debug (19/10/2026). Détail :
   - **Naissance** au premier login ; l'événement `PlayerBornEvent` déclenche le tirage des talents (patron Observer).
   - **Âge** calculé à partir du temps de jeu de l'overworld. Dormir ne fait pas vieillir. Réglage `life.daysPerYear` dans la config serveur, défaut **2 jours = 1 an**, adulte après 30 jours de jeu **[À VALIDER]**.
   - **Étapes de vie** : `LifeStageChangedEvent` à chaque changement, message au joueur.
   - **Taille** par l'attribut vanilla `scale` : modificateur transitoire réappliqué chaque seconde.
   - **Talents** : six talents, loi normale (moyenne 1,0, écart-type 0,15, bornes 0,5-1,5, réglables), stratégie `TalentRoller` à décliner par race plus tard.
   - **Commandes** (opérateurs) : `/mushocraft life info|setage|rebirth`, `/mushocraft talents show|reroll|set`.
   - **Tests automatiques** : `src/gametest`, lancés par `gradlew runGameTestServer` (9 tests). Ils ne sont pas inclus dans le jar.
3. ✅ `ManaData` + jauge de mana + circulation pour s'entraîner (nuit du 19/10, voir `docs/nuits/2026-10-19.md`).
4. ✅ Abstraction `Move`, `Cooldowns`, barre de move branchée sur le mode Epic Fight, écran des moves (nuit du 19/10).
5. 🟡 Moteur de sorts + 11 sorts Débutants + 12 Intermédiaires + maîtrise par école (nuit du 19/10) ; restent les rangs Avancé et plus, la magie mixte, l'incantation abrégée et silencieuse.
6. ✅ Stats physiques + entraînement + aura de combat (nuit du 19/10).
7. Styles Epic Fight + techniques + recharge partagée.
8. Maîtres PNJ.
