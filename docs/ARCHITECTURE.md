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

**Ajouts du 19/10 (sorts du wiki manquants)** :
- `VolleyEffect` : une salve de projectiles qui se forment en arc au-dessus de la tête puis filent tous vers le point visé (*Icicle Break*).
- `Cone` (sommet, axe, portée, demi-angle) : sait si un point est dans le cône et tire une direction au hasard à l'intérieur. Partagé par `ConeBlastEffect` et le jet de flammes.
- `SpellFx.jet` : des particules lancées depuis le lanceur dans tout le cône, assez vite pour le traverser (souffles, lance-flammes).
- `FreezeFieldEffect.aroundCaster()` et `.freezingWater()` : le champ centré sur le lanceur, et l'eau qui gèle en glace fragile, qui fond d'elle-même (*Absolute Zero*).
- `ZoneBehaviors.flameJet` (*Flamethrower*, un jet d'une seconde qui suit le regard) et `ZoneBehaviors.electric` (*Electric*).
- Type de dégâts `mushocraft:electric` : ignore l'armure, donc l'Aura de combat ; atténué par le *Magic Shield*.
- Forme `PillarKind.ICE_LANCE` : le modèle de la pointe de terre, en glace translucide ; `ICE_FORTRESS` : le rempart de glace.

**Magie innée (19/10, décision de Xopi : maître + long entraînement, sans incantation)** :
- École `SpellSchool.INHERENT`, enseignée seulement par de rares tuteurs (`MagicTutorEntity`, un sur 12), qui exigent une école déjà Intermédiaire.
- `MasteryCurve` : chaque move a sa courbe de maîtrise (`Move.masteryCurve()`). `STANDARD` : 50 répétitions, un échec sur deux au début ; `LONG_TRAINING` (magie innée) : 300 répétitions, 90 % d'échec au début.
- *Disturb Magic* (`DisturbMagicEffect`) : disperse le sort en formation de la créature visée (portée 10, à vue, pas dans une *Deep Mist*) en lui posant l'effet `DISTURBED` ; défait aussi boucliers et barrières ; sans cible, on s'exerce sur son propre mana. `ChantBroadcast` retient côté serveur qui incante et quel sort. Visuel d'après l'anime (S1 ép. 21) : pas de rayon, le sort se déchire en lambeaux de sa propre couleur.
- *Howling* (`HowlingEffect`) : étourdit (étourdissement court d'Epic Fight) tout ce qui est à 5 blocs ; grandes ondes pâles devant soi, d'après l'anime (S1 ép. 13).

### 4.2 bis Baguettes et bâtons (wiki, page *Magic Tools and Magic Items* ; rappel de Xopi du 19/10)

- **On lance des sorts sans baguette ni bâton.** Un focus tenu dans l'une ou l'autre main multiplie la puissance des sorts et, dans la même proportion, leur taille. Exemple : une boule d'eau 20 % plus forte est 20 % plus grosse ; le rayon d'explosion grandit aussi.
- La taille d'un projectile est un paramètre synchronisé, que le rendu applique au modèle : pas besoin de Pehkui.
- `MagicFocusItem` : un bonus général et des affinités par école. Seul le meilleur focus tenu compte.
  - *Baguette d'apprenti* : +10 % (bâtons et éclat d'améthyste).
  - *Bâton de magicien* : +20 % (bâtons, or et amas d'améthyste).
  - *Aqua Heartia* : +40 % en magie de l'eau, +15 % ailleurs ; unique, non fabricable.
- La pierre magique n'existe pas encore : l'améthyste la remplace dans les recettes **[À VALIDER]**.
- Les précepteurs portent une baguette (Intermédiaire) ou un bâton (Avancé et plus), qui renforce aussi leurs sorts.

### 4.2 ter Visuels des sorts (exigence de Xopi du 19/10 : qualité professionnelle)

- **Chaque sort a un vrai modèle 3D animé, jamais statique**, construit d'après sa référence dans l'anime ou le manga. Les images de référence sont téléchargées depuis le wiki dans un dossier temporaire, hors du mod, et ne sont jamais publiées.
- **Technique hybride** (choix de Xopi, révisable vers « tout Blockbench ») :
  - **Fluides** (eau, vent) : maillages générés par le code (`client/render/mesh`).
    - `LatheMesh` : surfaces de révolution (sphère, goutte, flèche, entonnoir).
    - `GridMesh` : surfaces libres (mur d'eau, transformation sphère → mur).
    - Ombrage Fresnel (bords plus opaques et plus clairs), textures qui défilent.
  - **Solides** (cristaux, rochers, lances) : modèles Blockbench animés, joués par GeckoLib 4.9.3 (licence MIT, dépendance requise). Les `.bbmodel` sont dans `blockbench/`.
- **Sorts déjà refaits :**
  - **Water Ball** : sphère d'eau claire qui ondule, s'étire en goutte avec la vitesse (volume conservé), cœur plus dense, reflet ; traînée de gouttes ; éclatement en gerbe à l'impact.
  - **Water Arrow** : flèche d'eau effilée dont la surface ondule.
  - **Ice Smash** : amas de pointes de glace (Blockbench + GeckoLib), animations « form » (jaillit) puis « fly » (tournoie, les pointes respirent).
  - **Water Shield** : une bulle grossit, puis se transforme en mur d'eau ondulant aux bords hérissés, l'eau déborde du sommet, puis le mur s'effondre. Des blocs invisibles assurent le blocage physique.
  - **Tornado** : entonnoir de vent de près de 16 blocs, deux couches qui tournent en sens contraires, colonne qui oscille. Physique : aspire, fait tourner et soulève créatures et objets jusqu'au sommet, d'où ils sont éjectés.
  - **Water Shield (v2)** : le mur s'arrondit autour du joueur, comme un écran courbe, plus grand (6 × 4).
  - **Piliers en modèles Blockbench** (entité `SpellPillar`, GeckoLib) : *Ice Pillar* (fût de glace à facettes, cristaux à la base), *Earth Lance* (pointe de roche brune en couches), *Earth Hedgehog* (pointes de pierre sombre penchées dans tous les sens), *Icicle Field* (monticules de givre qui engloutissent les cibles, comme dans l'anime). Animations « rise » (jaillit du sol), « idle » (frémit), « crumble » (s'effrite). Les blocs de collision sont cachés (propriété `hidden` de `ConjuredBlock`). L'apparence d'une éruption est une stratégie (`EruptionVisual`).
  - **Murs de terre** (même entité `SpellPillar`) : *Earth Wall* (dalles de pierre empilées de travers, crête irrégulière, gravats au pied) et *Earth Fortress* (quatre murs et un toit de grandes pierres, qui montent l'un après l'autre). `WallEffect` et `FortressEffect` cachent leurs blocs de collision et posent le modèle (`.lookingLike(PillarKind)`). Modèles générés directement en `.geo.json`, sans `.bbmodel` pour l'instant.
  - **Ondes de choc et entailles** (`OrientedParticle`) : plans orientés dont les images viennent d'une bande à part (`SmoothSheet`, `textures/effect/`), lue en filtrage linéaire sans mipmap. L'atlas des particules, lu texel par texel avec mipmaps, donnait des bords en escalier aux grands plans vus de biais.
  - **Règle de travail** (rappel de Xopi) : chaque visuel part d'images de référence de l'anime ou du manga, tirées des galeries du wiki et gardées hors du dépôt.
  - **Icônes des moves** : les 76 icônes (69 sorts, 7 techniques) sont peintes en 64 px par script (dégradés, ombrage, halos), chacune d'après les images de référence de son sort ; cadre aux couleurs de l'école, rang en pierres dorées (couronnes pour Roi et Empereur). Elles s'affichent lissées (`MoveIcons`), nettes à toute échelle d'interface.
  - **Particules lissées** (`SmoothSheet`) : en plus des ondes, entailles et impacts, la fumée (bouffées grises ombrées en trois tons, d'après les explosions de l'anime), les points de mana et lumières de soin (`mote.png`), et une étincelle à quatre branches (`SparkleParticle`, soins). Les nuages vanilla pixelisés des orages et de la brume sont remplacés par cette fumée.
  - **Corps de sorts ajoutés d'après les références** : *Burning Place* (`FLAME_RING`, mur de flammes en anneau qui suit le lanceur), *Quagmire* (`MUD`, nappe de boue, texture `mud.png`), *Sand Cloud* (nuage de sable à l'impact), *Frost Nova* (`FreezeFieldEffect.withCrystals()`, grands cristaux de glace) ; *Icicle Field* aux monticules plus grands.
  - **Brouillard d'écran** (`ClientMist`) : dans une *Deep Mist*, le brouillard du jeu se resserre à quelques blocs et devient blanc, comme dans l'anime ; les bancs de brume s'effacent alors.
  - **Orientation des modèles** : GeckoLib ne tourne que les entités vivantes ; `SpellPillarRenderer` applique lui-même l'orientation d'un pilier, pour qu'un mur suive la ligne de ses blocs invisibles.
  - **Lames en croissant** (`ProjectileShape.CRESCENT`, `CrescentVisual`) : une lame courbe qui vole face en avant, pointes en arrière, inclinée à sa façon ; *Wind Slice*, *Flame Slice* (manga), *Sonic Boom* (anime S2).
  - **Disque de glace** (`ProjectileShape.DISC`, `DiscVisual`, texture `ice_disc.png`) : *Icicle Edge*, d'après l'anime (S2) : disque lenticulaire au bord tranchant, strié de givre, dressé dans le sens du vol et tournant comme une lame de scie.
  - **Éclats de glace** (`IceShardVisual`) : morceaux de glace déchiquetés et facettés, chacun brisé à sa façon (*Icicle Break*, anime S1 ép. 13).
  - **Torrent de feu** (`ZoneVisual.FLAME_JET`, `FireVisuals.renderJet`) : *Flamethrower*, d'après l'anime (S2) : un cône de flammes qui roule en volutes, part de devant les yeux, suit le regard (zone attachée au lanceur, `ZoneEffect.followingCaster()`).
  - **Rempart de glace** (modèle `ice_rampart.geo.json`) : *Ice Fortress*, d'après le manga : deux rangées de dalles dentelées penchées vers l'ennemi, une crête de pointes, des éclats au pied.
  - **Traits de vent** (`StreakParticle`) : des lignes d'air étirées dans le sens du mouvement, comme le vent dessiné dans l'anime ; elles remplacent les bouffées vanilla pixelisées des souffles de vent.
  - **Nuage chargé** (`ZoneVisual.CHARGED_CLOUD`) : *Electric*, d'après l'anime (S3 ép. 5) : une petite boule de nuage presque noire, lueurs violettes ; la décharge fait tomber plusieurs grands éclairs blancs autour de la cible.
  - **Impacts dessinés** (`FlipbookParticle`) : étincelle teintée par l'élément (`IMPACT`), explosion dessinée (`BLAST`) par-dessus la boule de feu. Même lecture lissée que les ondes de choc (`SmoothSheet`).
  - **Ice Blade** : des points de lumière glacée convergent vers la main, qui s'illumine quand l'épée se forme (`SpellFx.conjureInHand`).
  - **Heat Hand** : tant que l'effet dure, lueurs orange et étincelles montent des deux mains (`HeatedHandsEffect`, `SpellFx.heatedHands`).
  - **Feu** :
    - particules dessinées pour le mod, en aplats façon anime : flamme animée en 8 images, braise, fumée, éclair blanc, onde de choc couchée au sol ;
    - **boule de feu** : cœur blanc, flammes qui fuient vers l'arrière (plus longues avec la vitesse), halo rouge, braises et fumée ;
    - **explosion** (entité `SpellFire`, souffle) : éclair, boule de feu en couches (blanc, jaune, orange) qui gonfle puis refroidit, nuage de fumée qui monte, onde de choc, braises. Les particules vanilla de l'explosion sont remplacées ;
    - **Flame Pillar** : colonne de flammes dont les langues montent.
  - **Incantation** : le sort prend forme dans la main (orbe à la couleur de l'école, particules de mana qui convergent), pour les joueurs comme pour les PNJ. Pas de cercle magique (écart E21).
- **Grimoire** (`SpellbookScreen`) : livre en cuir, rubans par école et par style, pages par rang, glisser-déposer vers la barre de move.

### 4.3 Modes d'incantation (wiki, page *Magic*)

| Mode | Comportement |
|---|---|
| Incantation complète | Temps d'incantation, coût normal. Disponible dès qu'on connaît le sort. |
| Incantation abrégée | Moitié du temps. Débloquée pour un sort après 150 répétitions (trois fois la maîtrise). **Fait (19/10).** |
| Incantation silencieuse | Aucune incantation. Elle demande un « contrôle du mana » (`ManaControl`) qui se construit en faisant circuler son mana : ×4 avant 6 ans, ×1 jusqu'à 15 ans, ×0,25 adulte (Rudeus l'apprend tout petit ; Roxy écrit qu'elle s'apprend plus facilement jeune). Une fois acquis, tous les sorts maîtrisés se lancent sans incantation. **Fait (19/10).** Le déroulé en 4 temps du wiki (forme, puissance, vitesse, relâchement), en maintenant la touche, reste à faire **[À VALIDER]**. |
| Cercles magiques | Objets ou blocs gravés : puissance fixe, lancement instantané quand on y verse du mana. Méthode principale de l'invocation selon le wiki. |

### 4.3 bis Magie mixte (wiki, *Magic Spells* § Melded Magic)

- Sept sorts : *Deep Mist*, *Magma Gush*, *Quagmire*, *Mud Rain*, *Blast*, *Frost Nova*, *Bora Blast*. École `MIXED`.
- Chaque sort porte sa **recette** (`MeldRecipe`) : un sort précis (*Deep Mist* = *Waterfall* + *Heat Island* + *Icicle Field*) ou n'importe quel sort d'une école (*Quagmire* = eau + terre).
- **Découverte** (`MeldDiscovery`) : dès qu'un move est maîtrisé (`MoveMasteredEvent`, patron Observer) et à la connexion, tout sort mixte dont les composants sont maîtrisés est appris. Les précepteurs ne l'enseignent pas. Écart E22 **[À VALIDER]**.
- L'infobulle du grimoire indique le mélange (« Mélange : Water Splash + Icicle Field »).
- Effets :
  - *Deep Mist* : brouillard autour du lanceur ; ceux qui s'y trouvent sont aveuglés ; qui traque quelqu'un caché dedans perd sa trace, sauf au contact.
  - *Quagmire* : la boue retient (lenteur VI, pas de saut).
  - *Mud Rain* : sol glissant, impossible de courir, glissades.
  - *Magma Gush* : jaillissement de lave puis sol en fusion.
  - *Blast* : souffle de vent brûlant en cône.
  - *Frost Nova* : trempe puis gèle (deux fois plus longtemps sur les cibles trempées).
  - *Bora Blast* : rafale glacée en cône qui gèle et engourdit.

### 4.3 ter Invocation (wiki, *Summoning Magic*)

- **Papiers d'invocation** (`SummoningPaperItem`) : un cercle tracé d'avance. On y verse le mana, l'invocation a lieu, le papier est consommé. Pas d'incantation, utilisable par tous. Recettes : papier, lapis, améthyste (à la place de la pierre magique) et un ingrédient.
- **Rituels** (`SummonRitual`, patron Stratégie) :
  - *Esprits lumineux* : trois esprits tournent autour du lanceur et éclairent vraiment (bloc de lumière invisible qui s'efface tout seul).
  - *Esprit* : un esprit combattant volant, dont la santé et les dégâts suivent la réserve du maître. Il prend 1 mana/s et disparaît quand il n'y en a plus.
  - *Bête* : une créature qui vit dans le biome, liée 5 min. Elle peut se révolter si elle est trop forte pour la réserve de l'invocateur.
- **Pacte** (`SummonBond`, sauvegardé sur la créature, `SummonBonds`, `SummonerGoals`) : la créature suit son maître, le défend, frappe ce qu'il frappe et ne s'en prend jamais à lui ni aux autres invoqués du même maître.
- Écart E24 **[À VALIDER]**.

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

Répartition demandée par Xopi le 19/10 :

| Stat | Effet (au maximum, adulte entraîné, talent moyen) | S'entraîne par |
|---|---|---|
| Force | `attack_damage` +6, `epicfight:impact` | coups portés, dégâts infligés |
| Vitalité | `max_health` +20 (dix cœurs), `epicfight:stamina` | dégâts encaissés, course |
| Résistance | `armor` +10, `armor_toughness` +6, `epicfight:stun_armor`, `knockback_resistance` | coups encaissés, gardes réussies |
| Vitesse | `attack_speed` et `epicfight:offhand_attack_speed` ×1,35 (Epic Fight joue ses animations d'attaque à cette vitesse : coups plus rapides), élan de course et de saut | sprint, esquives |

**Élan de course et de saut** (`SprintMomentum`, même principe que la `PlayerPhysicalAbilityProcedure` de JujutsuCraft) :
- sans entraînement, aucune vitesse en plus ;
- un coureur entraîné gagne Vitesse I, puis un niveau de plus à chaque renouvellement, tant qu'il court, jusqu'à un plafond fixé par la stat (Vitesse VII au maximum) ;
- accroupi, on charge un saut (Saut amélioré IV au maximum) ; la vitesse acquise porte aussi le saut ;
- les effets sont courts et cachés ; une potion n'est jamais écrasée.

Les stats suivent aussi l'âge : un bébé est faible, et l'échelle de taille vanilla `minecraft:generic.scale` sert à la croissance physique.

### 5.2 Styles d'escrime (wiki, catégorie *Swordstyle*)

- Trois styles : **Style du Dieu de l'Épée**, **Style du Dieu de l'Eau**, **Style du Dieu du Nord**. Les noms français sont à fixer dans le glossaire.
- Sept rangs de maîtrise, les mêmes pour les trois styles : Débutant, Intermédiaire, Avancé, Saint, Roi, Empereur, Dieu.
- **Branchement Epic Fight :**
  - Un `Style` Epic Fight par style MushoCraft (enum extensible).
  - Un `Moveset` par style, sur les catégories d'armes existantes, via `WeaponModifier` : épée, tachi, épée longue, uchigatana, épée à deux mains et dague prennent les trois styles ; la hache et la lance seulement le Style du Dieu du Nord, dont les branches se battent avec d'autres armes que l'épée (E28).
  - Une arme dans chaque main : le Dieu du Nord a son propre style Epic Fight (`MUSHO_NORTH_GOD_DUAL`) et son enchaînement à deux épées ou deux dagues, bâti sur les jeux doubles d'Epic Fight ; avec les autres styles, la paire garde le jeu double d'Epic Fight (20/10).
  - Sélection par `ProviderConditional.createCustom(style, joueur → style actif == X)`. C'est le conditionnel de plus haute priorité.
  - Au changement de style, appeler `changeWeaponInnateSkill` et `modifyLivingMotionByCurrentItem` (vérifié dans le code d'Epic Fight).
- **Techniques spéciales.** Ce sont nos propres `Move`, pas des skills Epic Fight. Les slots de skills d'Epic Fight sont fixes et chaque skill a sa propre recharge, alors qu'on veut une recharge **commune au style**. L'exécution passe par `LivingEntityPatch.playAnimationSynchronized` avec une `AttackAnimation`. On commence par les animations existantes d'Epic Fight (référencées, jamais copiées) ; les nouvelles animations seront créées à partir de zéro.
- **Recharge partagée** : `cooldownStyle = base × f(rang de maîtrise)`, décroissante avec la maîtrise.
- **Techniques recensées sur le wiki** (orthographe à fixer) :
  - Dieu de l'Épée : *Longsword of Light*, *Light Reversal*, *Longsword of Silence*, *Arm Chop*, *Lake Surface Longsword*, *Crescent Moon Longsword*.
  - Dieu de l'Eau : *Flow*, les *Five Secret Arts*, *Deprivation Sword Kingdom*.
  - Dieu du Nord : lancer d'épée, *Four Legged Form*, *Hazy Cross*, *Teardrop Bullet*, *Crimson Ink*, *Pecking*, *Moonlight Cleave*, *Gravity Fracture*.
- **Techniques en jeu (14)** — en plus des 7 premières, celles que le wiki décrit assez (20/10, écart E34) :
  - *Light Reversal* (Dieu de l'Épée, Roi, épée à deux mains) : garde très brève ; le prochain coup reçu est annulé et le poignet qui le porte tranché (dégâts, désarmement, faiblesse).
  - *Deprivation Sword Kingdom* (Dieu de l'Eau, Dieu) : un champ de 7 blocs pendant 10 s (`SwordDomains`) ; quiconque y attaque, ou y commence une incantation, est abattu aussitôt et son sort coupé ; il ne frappe pas qui ne fait rien.
  - *Teardrop Bullet* (Dieu du Nord, Intermédiaire) : un sachet d'épices lancé, qui éclate en nuage (cécité, nausée, lenteur). *Crimson Ink* (Avancé) : une boule rouge qui éclate en glu (immobilisé 4 s). Les deux ruses sont un projectile commun (`ThrownTrick` + `TrickKind`) et existent aussi en objets lançables à la main.
  - Déplacement d'aura (Dieu du Nord, Avancé) : bascule l'Aura de combat levée vers la vitesse (+18 % de vitesse, +25 % de vitesse d'attaque, -20 % de dégâts) ; s'efface si l'aura retombe.
  - *Hazy Cross* (Dieu du Nord, Saint, une arme dans chaque main) : coup de haut des deux armes, puis l'arme de la main gauche lancée et une taille horizontale plus forte.
  - *Pecking* (Dieu du Nord, Roi, mains nues possibles) : contre qui encaisse un tiers du coup et broie le poing de l'attaquant (désarmement, faiblesse, lenteur de minage).
  - Les trois contres (*Flow*, *Light Reversal*, *Pecking*) partagent la même posture ; leur réponse est une stratégie (`CounterKind`).
  - Non faites, faute de description dans le wiki : *Lake Surface Longsword*, *Crescent Moon Longsword*, chacun des *Five Secret Arts*. *Gravity Fracture* exige l'épée du Roi Dragon Kajakut, qui n'existe pas encore dans le mod.

#### Enchaînements de base par style (API ExCap d'Epic Fight)

- Chaque style MushoCraft est aussi un `Style` d'Epic Fight (`EpicFightSwordStyles`, énumération extensible enregistrée dans le constructeur du mod).
- Un `WeaponModifier` par type d'arme (`StyleMovesets`) ajoute à l'arme :
  - trois conditions `CUSTOM` (« ce joueur combat dans ce style, main secondaire libre ») ;
  - trois movesets enfants de celui de l'arme. Ils changent les coups de base, l'attaque en course et l'attaque en l'air, et gardent la garde, la compétence innée et les postures.
- Animations : uniquement celles d'Epic Fight, par référence.
- Epic Fight choisit le moveset des deux côtés. Le style actif est donc synchronisé vers le joueur et vers ceux qui le voient (`StyleSyncPayload`, `ActiveStyles`).
- Le combat à deux armes et l'épée + bouclier gardent les movesets d'Epic Fight.
- Pas de route datapack : Epic Fight 21.17 ignore encore les modificateurs en JSON, et ses conditions JSON n'ont pas de type `CUSTOM`.

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

**Maîtrise par move (demande de Xopi du 19/10)** : chaque sort et chaque technique a un compteur de répétitions (`MoveMastery`).
- Un move tout juste appris rate une fois sur deux. Il n'a que 60 % de sa puissance et il est un tiers plus lent : incantation plus longue, ou animation d'Epic Fight ralentie par un modificateur temporaire de vitesse d'attaque.
- Le risque baisse vite avec la pratique. Après 50 répétitions, le move est maîtrisé : pleine puissance, pleine vitesse, plus d'échec.
- Un échec coûte la moitié de la recharge (et le mana d'un sort) et compte pour une demi-répétition.

**Leçon (patron Template Method, `TeacherEntity.teach`)**, la même pour tous les maîtres :
1. Palier par palier jusqu'au rang du maître, le maître commence par une exigence : un corps entraîné pour l'épée (`BodyRules.requiredBodyLevel`), une capacité de mana pour la magie (`ManaRules.capacityToStudy`).
2. Il montre un seul move du palier à la fois.
3. Il attend qu'il soit maîtrisé avant de montrer le suivant.
4. Quand tous les moves du palier sont maîtrisés, il accorde le rang. L'aura de combat exigée par le wiki reste nécessaire pour l'épée.

L'expérience de style ne donne plus de rang : seul un maître en donne.

**Règle pour tous les mobs du mod** : ils sont des entités Epic Fight (patch, squelette, rendu animé). Ceux qui connaissent la magie lancent les vrais sorts du mod comme un joueur (`SpellcasterGoal`) :
- ils incantent et peuvent être interrompus par un coup ;
- ils choisissent selon la situation : un soin s'ils sont blessés, une défense si l'ennemi est proche, sinon une attaque visée sur la cible ;
- chaque sort indique à quoi il sert (`SpellIntent`).

- PNJ humanoïdes avec un patch Epic Fight (`HumanoidMobPatch`), enregistré via `EpicFightEventHooks.Registry.ENTITY_PATCH`. Ce n'est pas le bus NeoForge : la doc d'Epic Fight est périmée sur ce point.
- `HumanoidMobPatch` choisit l'IA de combat d'après la catégorie et le style de l'arme tenue. Chaque maître reçoit donc l'arme de son style :
  - Dieu de l'Épée : tachi ;
  - Dieu de l'Eau : épée et bouclier ;
  - Dieu du Nord : deux épées.
- Le squelette est le biped d'Epic Fight (`Armatures.registerEntityTypeArmature`, au setup commun). Le rendu est `PHumanoidRenderer` (`EpicFightClientEventHooks.Registry.ADD_PATCHED_ENTITY`, côté client seulement).
- Classe abstraite `TeacherEntity` (patron Template Method) : rang synchronisé, titre au-dessus de la tête, clic droit = leçon. Deux sous-classes :
  - `SwordMasterEntity` : un style. Il prend le joueur pour disciple, le promeut jusqu'à son propre rang au maximum, puis lui apprend les techniques de son style jusqu'à son rang. Sinon il dit ce qui manque : expérience, aura, ou plus rien à apprendre.
  - `MagicTutorEntity` : une école. Il apprend les sorts de son école jusqu'à son rang, si l'élève est prêt (même règle que les livres). Les écoles de barrière et de frappe divine sont enseignées par un chevalier de Milis.
- Chaque maître a un style et un rang. **Décidé :** il peut faire monter son élève jusqu'à un rang égal au sien, pas au-delà.

## 5 bis. Monstres (wiki, page *Monsters*)

- **Paquet `monster`** : `MushoMonster` (monstre de base, rang de la Guilde `MonsterRank` F à S, appartenance à une meute), `MonsterPack` (membres, chef, appel à la chasse, succession), `EngagementTokens` (places d'attaque), goals dans `monster/ai`.
- **Meute** : les monstres apparus ensemble forment une meute (`PackSpawn`), le premier la mène. Les membres ne se blessent pas entre eux ; une meute peut mêler plusieurs espèces (des Sangliers Terminateurs menant des Chiens d'assaut).
- **IA de meute** (goals réutilisables) :
  - `CallThePackGoal` : le chef qui repère une proie s'arrête, lance l'appel (hurlement, rugissement) et toute la meute à portée se tourne vers elle ;
  - `PackHuntGoal` : seuls quelques membres attaquent à la fois (places d'attaque, deux pour les chiens) ; les autres tournent autour de la proie, chacun de son côté, et prennent le relais ;
  - `RetreatWhenWoundedGoal` : un monstre gravement blessé se replie quelques secondes et cède sa place, sauf s'il est seul ;
  - `FollowPackLeaderGoal` : hors combat, la meute suit son chef ; si le chef tombe, le membre le plus fort lui succède aussitôt.
- **Epic Fight** : chaque espèce a son propre squelette, son maillage et ses animations, faits pour le mod (rien n'est copié d'Epic Fight). `MonsterPatch` ajoute l'attaque animée d'Epic Fight, qui ne frappe que si le monstre tient une place d'attaque ; les déplacements de combat restent ceux de l'IA ci-dessus.
- **Outil `tools/bestiary`** (Python) : un monstre se décrit en os et en blocs (`rig.py`), ses animations en fonctions du temps (`anim.py`) ; l'outil écrit le squelette, le maillage et les animations au format d'Epic Fight, peint la texture point par point d'après la position 3D de chaque texel (`paint.py`, pour que taches et rayures passent d'un bloc à l'autre) et rend des aperçus. `compare.py` met le modèle à côté de ses images de référence. Les rigs restent simples (une vingtaine d'os) pour bien s'animer.
- **Chien d'assaut** (rang E, plaines et forêts) : d'après la case du manga du wiki ; robe noire, marques feu de dobermann, oreilles dressées, crête osseuse, yeux rouges. Morsure, double morsure, bond sur une proie à 3-6 blocs, hurlement d'appel ; groupes de 3 à 5.
- **Coyote de Pax** (rang C, badlands et plateaux de savane) : d'après l'anime (S1 ép. 11) ; maigre, hautes pattes, robe gris-violet hérissée (crinière aux bords déchiquetés par des texels transparents), yeux rouges. Groupes de 5 à 8 sous un chef ; trois attaquent à la fois ; le chef se divise tous les trois jours de jeu tant que le groupe compte moins de 10 coyotes (le roman : tous les quelques mois).
- **Sanglier terminateur** (rang D, C ou B avec sa meute ; plaines et forêts) : d'après l'anime (S2 ép. 0) ; bipède à quatre bras, fourrure brun-noir, crinière grise sur le dos, museau clair, énormes défenses ivoire. Il n'apparaît jamais seul : trois ou quatre Chiens d'assaut naissent dans sa meute autour de lui. Il rugit pour lancer ses chiens, griffe d'un côté puis de l'autre, frappe des deux bras (renverse), charge défenses baissées sur une proie à 4-10 blocs.
- **Grizzly lustré** (rang B, plaines enneigées, taïgas, bosquets) : d'après l'anime (S2 ép. 1) et le manga ; grand ours blanc, une bande noire du museau au bas du dos, yeux rouges. Meutes de 2 à 4 ; mord, se dresse et abat ses pattes avant (renverse), le chef rugit debout. La nuit il est plus lent d'un tiers et ne voit qu'au tiers de sa portée (wiki : il voit mal dans le noir et se traîne la nuit).
- **Gobelin** (rang E, lisières : forêts, plaines, prairies) : d'après l'anime (S1 OVA 1) ; petit, voûté, chauve, peau olive, grandes oreilles pointues, yeux verts, crocs, fourrure en écharpe, pagne noué, gourdin. Bandes de 3 à 6 ; seul ou à deux face à une personne, il garde ses distances (`FleeWhenOutnumberedGoal`) ; attaque d'abord les villageois ; la bande grossit d'un gobelin tous les deux jours de jeu jusqu'à 8 (`PackGrowth`, partagé avec la division des coyotes).
- **Spectre** (rang C ; *Spectre mortel* rang A, un sur quatre ; partout, dans l'obscurité) : d'après la case du manga du wiki (couleurs choisies, le manga est en noir et blanc) ; figure flottante sans jambes, robe déchirée qui finit en volutes, mains osseuses griffues, flammes pâles, rendu translucide (`TranslucentMonsterRenderer`). Les coups ordinaires le traversent ; le feu et la magie le blessent mais, mortels, ne font que le dissiper 30 s ; seule la frappe divine (*Exorcist Light*, ×3 contre les morts-vivants) l'achève, avancée pour un Spectre mortel (`DivineStrike`). Il relève 2 squelettes armés (3 pour le mortel) qui reviennent tant qu'il existe et s'effondrent avec lui (`RaiseTheDeadGoal`). Le Spectre mortel lance la vraie magie d'attaque du mod (`SpellcasterGoal`) : premier monstre magicien, comme le demande la règle « certains mobs utilisent la magie comme les joueurs ».
- **Guerrier en armure** (rang A, grottes : concrétions, grottes luxuriantes, abîmes) : d'après l'anime (S2 ép. 21) ; armure rouillée animée à quatre bras armés de lames, casque à fente. Les armes légères (poings, dague, épée, uchigatana) ne passent qu'au quart ; terre et frappe divine font double dégât ; mort-vivant. Il prend la garde *Flow* du Dieu de l'Eau quand l'ennemi est à portée ou qu'un projectile ou un sort fonce sur lui (`WaterGodGuardGoal`) : le coup est retourné, le sort dévié, avec sa propre animation de parade (les contres d'un monstre jouent désormais l'animation de son espèce).
- **Crâne fou** (rang B, grottes et marais) : d'après l'anime (S2 ép. 20) ; géant de boue sans tête, fissuré et troué, une entaille aux trois yeux orange luisants, un crâne humain dans la poitrine. Les coups sur la boue n'enlèvent qu'un dixième ; le crâne ne sort de la boue que pendant ses attaques et ses incantations (et un instant après) : frappé alors **de face**, il prend une fois et demie les dégâts. Il lance des boules de boue ralentissantes (`MudSlingGoal`) et incante *Stone Cannon*. Tout monstre qui incante joue désormais le geste d'incantation de son espèce (`MushoMonster.aiStep`).
- **Défense de la meute** : quiconque blesse un membre a toute la meute sur le dos.
- **Galerie** : `-Dmushocraft.gallery.only=monster_` montre chaque monstre de profil, chacune de ses animations, puis une meute qui chasse.

## 5 ter. Lieux (structures du monde)

- **Outil `tools/structures`** (Python) : un écrivain NBT minimal et un gabarit de construction écrivent les modèles de structures (`data/mushocraft/structure/*.nbt`), avec leurs entités, et des aperçus isométriques. Les vitres et barrières se raccordent à leurs voisines à l'écriture.
- **Dojo** (`dojo`, un des trois modèles au hasard : Dieu de l'Épée, de l'Eau, du Nord) : d'après le dojo du Style du Dieu de l'Épée en Terre sainte des Épées (anime S3, manga) — halle sur socle de pierre, pans de bois sombre et murs blancs, toit de tuiles grises à pignon, autel au fond, bannières de la couleur du style, bancs et mannequins. Un maître d'épée **résident** du style du dojo y vit. Plaines, forêts, taïgas et terres enneigées (la Terre sainte est enneigée).
- **Église de Milis** (`milis_church`) : d'après l'anime (S3 ép. 5) — pierre blanche, toit de cuivre vert-de-gris, rosace, fenêtre à la croix de Milis dans l'abside, clocher à flèche et croix ; nef à colonnes, bancs, tapis bleu, autel aux bougies, lustres. Deux précepteurs **résidents** : soin et frappe divine.
- **Résidents** (`TeacherEntity.isResident`) : un maître placé par une structure garde le style ou l'école du lieu (seul son rang est tiré) et reste à quelques blocs de son poste ; c'est sauvegardé.

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
   - **Commandes** (opérateurs) : `/mushocraft life info|setage|rebirth`, `/mushocraft talents show|reroll|set`. `/mushocraft max [joueurs]` rend aussi fort que possible dans toutes les disciplines (`PlayerMaximizer`) : adulte, talents au maximum, corps entraîné, Aura complète, réservoir au plafond, incantation silencieuse, tous les sorts et techniques appris et maîtrisés, toutes les écoles et tous les styles au rang Dieu.
   - **Tests automatiques** : `src/gametest`, lancés par `gradlew runGameTestServer` (9 tests). Ils ne sont pas inclus dans le jar.
3. ✅ `ManaData` + jauge de mana + circulation pour s'entraîner (nuit du 19/10, voir `docs/nuits/2026-10-19.md`).
4. ✅ Abstraction `Move`, `Cooldowns`, barre de move branchée sur le mode Epic Fight, écran des moves (nuit du 19/10).
5. 🟡 Moteur de sorts + 58 sorts (Débutant à Roi, les 11 sans rang du wiki, les 7 sorts de magie mixte) + maîtrise par école + incantation abrégée et silencieuse (19/10). Au-delà du rang Saint, le wiki ne nomme que *Lightning* et *Confinement Barrier* (E23). Invocation par papiers (§4.3 ter). Les sorts Saint, Roi et Empereur s'apprennent auprès de tuteurs de ce rang, rarissimes (E27).
6. ✅ Stats physiques + entraînement + aura de combat (nuit du 19/10).
7. 🟡 Styles + 7 techniques + recharge partagée + règles de rang + enchaînements de base propres à chaque style sur toutes les épées, plus la hache et la lance pour le Dieu du Nord (nuit du 19/10) ; 14 techniques depuis le 20/10 ; restent celles que le wiki ne décrit pas et *Gravity Fracture* (épée Kajakut).
8. 🟡 Maîtres PNJ : maîtres d'épée et précepteurs de magie, avec patch Epic Fight, skins originaux, œufs d'apparition et apparition naturelle rare (nuit du 19/10). Lieux dédiés depuis le 20/10 : dojos des trois styles et églises de Milis, générés dans le monde (voir ci-dessous).
