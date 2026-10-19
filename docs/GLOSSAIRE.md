# MushoCraft — Glossaire

**Règles**
- L'**anglais** est la langue de référence du mod (`en_us`). Les noms suivent le wiki anglais (mushokutensei.fandom.com), qui reprend la traduction anglaise officielle.
- Le **français** est la traduction du mod (`fr_fr`). Les noms français ci-dessous sont des **propositions** : je n'ai pas vérifié s'il existe une traduction officielle française pour chaque terme. Statut : `proposé` tant que Xopi n'a pas validé, `figé` ensuite.
- L'**id** sert de nom de registre dans le code (`mushocraft:<id>`). Une fois figé, on ne le change plus.
- Rang : celui donné par le wiki. `—` = le wiki ne donne pas de rang (à chercher dans le roman, ne pas inventer).
- Les textes d'incantation ne sont **pas** reproduits ici (voir `ECARTS.md`, E6).

Dernière mise à jour : 19/10/2026. Source de chaque entrée : page *Magic Spells* du wiki sauf mention contraire.

## Incohérences relevées sur le wiki (à trancher)

| Sujet | Variantes vues | Proposition |
|---|---|---|
| Rang 1 | « Beginner » (page *Magic*) / « Elementary » (page *Magic Spells*) | **Beginner** |
| Rangs 4 à 7 | Saint/Saintly, King/Kingly, Emperor/Imperial, God/Divine | **Saint, King, Emperor, God** |
| Boule d'eau | « Waterball » (tableau) / « Water Ball » (incantations) | **Water Ball** |
| Frappe divine | « Exorcist Light » (tableau) / « Exorcist Rate » (incantations) | **Exorcist Light** |
| Magie mixte | « Mixed Magic » (page *Magic*) / « Melded Magic » (page *Magic Spells*) | **Mixed Magic** |
| Soin avancé | « Shine Healing » / « Shine-Healing » | **Shine Healing** |
| *Magma Gush* | Listé en Feu (Advanced) **et** en magie mixte (Feu + Terre) | Magie mixte, rang Advanced |

## Rangs (magie et escrime)

| id | EN | FR (proposé) |
|---|---|---|
| `beginner` | Beginner | Débutant |
| `intermediate` | Intermediate | Intermédiaire |
| `advanced` | Advanced | Avancé |
| `saint` | Saint | Saint |
| `king` | King | Roi |
| `emperor` | Emperor | Empereur |
| `god` | God | Dieu |

Titres d'escrime : *Sword Saint / King / Emperor / God*. En français : Saint de l'Épée, Roi de l'Épée, etc. Même schéma pour *Water* (de l'Eau) et *North* (du Nord).

## Notions de magie

| id | EN | FR (proposé) | Source |
|---|---|---|---|
| `mana` | Mana | Mana | page *Magic* |
| `incantation` | Incantation | Incantation | page *Magic* |
| `truncated_incantation` | Truncated Incantation | Incantation abrégée | page *Magic* |
| `silent_casting` | Silent Casting | Incantation silencieuse | page *Magic* (aussi « chantless », « voiceless ») |
| `magic_circle` | Magic Circle | Cercle magique | page *Magic*, LN vol. 10 ch. 11 |
| `battle_aura` | Battle Aura | Aura de combat | page *Battle Aura* (闘気, Tōki) |

## Écoles de magie

| id | EN | FR (proposé) |
|---|---|---|
| `water` | Water Magic | Magie de l'eau |
| `fire` | Fire Magic | Magie du feu |
| `wind` | Wind Magic | Magie du vent |
| `earth` | Earth Magic | Magie de la terre |
| `healing` | Healing Magic | Magie de soin |
| `detoxification` | Detoxification Magic | Magie de détoxification |
| `barrier` | Barrier Magic | Magie de barrière |
| `divine_strike` | Divine Strike Magic | Magie de frappe divine |
| `summoning` | Summoning Magic | Magie d'invocation |
| `mixed` | Mixed Magic | Magie mixte |
| `necromancy` | Necromancy | Nécromancie |
| `teleportation` | Teleportation Magic | Magie de téléportation |
| `divination` | Divination Magic | Magie de divination |
| `lightning` | Lightning Magic | Magie de la foudre |
| `inherent` | Inherent Magic | Magie innée |

## Sorts

### Eau

| id | EN | FR (proposé) | Rang | Source (wiki) |
|---|---|---|---|---|
| `water_ball` | Water Ball | Boule d'eau | Beginner | LN vol. 1 ch. 3 |
| `water_shield` | Water Shield | Bouclier d'eau | Beginner | LN vol. 1 ch. 3 |
| `water_arrow` | Water Arrow | Flèche d'eau | Beginner | LN vol. 1 ch. 3 |
| `ice_smash` | Ice Smash | Frappe de glace | Beginner | LN vol. 1 ch. 3 |
| `ice_blade` | Ice Blade | Lame de glace | Beginner | LN vol. 1 ch. 3 |
| `water_cannon` | Water Cannon | Canon à eau | Intermediate | LN vol. 4 ch. 4 |
| `ice_pillar` | Ice Pillar | Pilier de glace | Intermediate | LN vol. 1 ch. 8 |
| `waterfall` | Waterfall | Cascade | — | aucune référence sur le wiki (vu dans l'anime S1) |
| `water_splash` | Water Splash | Gerbe d'eau | — | LN vol. 12 ch. 4 |
| `flood_flush` | Flood Flush | Crue | — | LN vol. 17 ch. 10 |
| `icicle_field` | Icicle Field | Champ de givre | — | LN vol. 12 ch. 4 |
| `icicle_break` | Icicle Break | Salve de glaçons | — | WN vol. 4 ch. 3.5 |
| `icicle_edge` | Icicle Edge | Lame de givre | — | LN vol. 12 ch. 6 |
| `ice_lance` | Ice Lance | Lance de glace | — | LN vol. 16 ch. 7 |
| `blizzard_storm` | Blizzard Storm | Tempête de blizzard | Advanced | LN vol. 7 ch. 2 |
| `squall` | Squall | Averse | Advanced | anime S1 |
| `cumulonimbus` | Cumulonimbus | Cumulonimbus | Saint | LN vol. 1 ch. 6 |
| `lightning` | Lightning | Foudre | King | LN vol. 13 ch. 6 |
| `absolute_zero` | Absolute Zero | Zéro absolu | Emperor | WN ch. 194 |
| `ice_fortress` | Ice Fortress | Forteresse de glace | — | manga uniquement |

### Feu

| id | EN | FR (proposé) | Rang | Source (wiki) |
|---|---|---|---|---|
| `fireball` | Fireball | Boule de feu | Beginner | LN vol. 2 ch. 2 |
| `heat_hand` | Heat Hand | Main chaude | — | LN vol. 1 ch. 8 |
| `heat_island` | Heat Island | Îlot de chaleur | — | LN vol. 1 ch. 5 |
| `burning_place` | Burning Place | Brasier | — | LN vol. 2 ch. 8 |
| `flame_slice` | Flame Slice | Tranche de flammes | — | LN vol. 12 ch. 4 |
| `flamethrower` | Flamethrower | Lance-flammes | — | LN vol. 7 ch. 3 |
| `flame_pillar` | Flame Pillar | Pilier de flammes | Intermediate | WN ch. 184 |
| `explosion` | Explosion | Explosion | Intermediate | aucune référence sur le wiki |
| `ex_flame` | Ex-Flame | Ex-Flamme | Intermediate | LN vol. 7 ch. 2 |
| `exodus_flame` | Exodus Flame | Flamme de l'Exode | Advanced | LN vol. 5, interlude |
| `flash_over` | Flash Over | Embrasement | Saint | aucune référence sur le wiki |

### Vent

| id | EN | FR (proposé) | Rang | Source (wiki) |
|---|---|---|---|---|
| `sonic_boom` | Sonic Boom | Bang sonique | Intermediate | LN vol. 2 ch. 2 |
| `wind_slice` | Wind Slice | Tranche de vent | Intermediate | aucune référence sur le wiki |
| `wind_bind` | Wind Bind | Lien de vent | — | LN vol. 14 ch. 8 |
| `wind_blast` | Wind Blast | Rafale | — | LN vol. 21 ch. 3 |
| `sonic_blast` | Sonic Blast | Onde sonique | Advanced | LN vol. 16 ch. 7 |
| `tornado_impact` | Tornado Impact | Impact tornade | Advanced | LN vol. 16 ch. 7 |
| `violent_storm` | Violent Storm | Tempête violente | Saint | aucune référence sur le wiki |
| `tornado` | Tornado | Tornade | Saint | LN vol. 7 ch. 4 |

### Terre

| id | EN | FR (proposé) | Rang | Source (wiki) |
|---|---|---|---|---|
| `rock_bullet` | Rock Bullet | Balle de roche | Beginner | LN vol. 8 ch. 7 |
| `sand_cloud` | Sand Cloud | Nuage de sable | — | LN vol. 8 ch. 7 |
| `earth_wall` | Earth Wall | Mur de terre | — | LN vol. 2 ch. 2 |
| `stone_cannon` | Stone Cannon | Canon de pierre | Intermediate | LN vol. 6 |
| `earth_pillar` | Earth Pillar | Pilier de terre | Intermediate | LN vol. 12 ch. 8 |
| `earth_lance` | Earth Lance | Lance de terre | Intermediate | LN vol. 6 ch. 3 |
| `earth_fortress` | Earth Fortress | Forteresse de terre | Advanced | LN vol. 1 ch. 6 |
| `earth_hedgehog` | Earth Hedgehog | Hérisson de terre | Advanced | LN vol. 7 ch. 4 |
| `dust_storm` | Dust Storm | Tempête de poussière | Advanced | aucune référence sur le wiki |
| `sandstorm` | Sandstorm | Tempête de sable | Saint | LN vol. 9 ch. 7 |

### Magie mixte

| id | EN | FR (proposé) | Composants (wiki) | Source (wiki) |
|---|---|---|---|---|
| `deep_mist` | Deep Mist | Brume épaisse | Waterfall + Heat Island + Icicle Field | LN vol. 1 ch. 5 |
| `magma_gush` | Magma Gush | Jaillissement de magma | feu + terre (rang Advanced) | LN vol. 1 ch. 10 |
| `quagmire` | Quagmire | Bourbier | eau + terre | anime S2 |
| `bora_blast` | Bora Blast | Souffle de bora | Icicle Field + Blast | LN vol. 5 ch. 1 |
| `frost_nova` | Frost Nova | Nova de givre | Water Splash + Icicle Field | LN vol. 12 ch. 4 |
| `mud_rain` | Mud Rain | Pluie de boue | eau + terre | LN vol. 17 ch. 2 |
| `blast` | Blast | Souffle brûlant | feu + vent | LN vol. 1 ch. 11 |

### Soin, détoxification, frappe divine

| id | EN | FR (proposé) | École | Rang | Source (wiki) |
|---|---|---|---|---|---|
| `healing` | Healing | Soin | soin | Beginner | anime S1 ép. 1 |
| `x_healing` | X-Healing | Soin X | soin | Intermediate | LN vol. 2 ch. 2 |
| `shine_healing` | Shine Healing | Soin radieux | soin | Advanced | LN vol. 12 ch. 9 |
| `detoxification` | Detoxification | Détoxification | détoxification | — | wiki : « Citation Needed » |
| `antidote` | Antidote | Antidote | détoxification | — | manga *Roxy Gets Serious* vol. 11 |
| `exorcist_light` | Exorcist Light | Lumière exorciste | frappe divine | Beginner | WN vol. 10 ch. 93 |

Rangs supérieurs de soin (page *Healing Magic*) : le wiki décrit des effets sans nom de sort :
- Saint : restaure les membres si les os sont là ;
- Roi : régénère les membres perdus ;
- Empereur : rien d'indiqué ;
- Dieu : ressuscite un mort récent (source : réponse de l'auteur, ask.fm, sept. 2015).

### Barrière

| id | EN | FR (proposé) | Rang | Source (wiki) |
|---|---|---|---|---|
| `magic_shield` | Magic Shield | Bouclier magique | Beginner | LN vol. 8 ch. 4 |
| `physical_shield` | Physical Shield | Bouclier physique | Intermediate | LN vol. 4 ch. 6 |
| `spirit_sealing_barrier` | Spirit-Sealing Barrier | Barrière scelle-esprits | Saint | LN vol. 20 ch. 3 |
| `confinement_barrier` | Confinement Barrier | Barrière de confinement | King | LN vol. 6 ch. 4 |
| `badigadi_sealing_barrier` | Badigadi's Sealing Barrier | Sceau de Badigadi | God | wiki, sans tome |
| `illusion_barrier` | Illusion Barrier | Barrière d'illusion | inconnu | wiki, sans tome |

### Invocation

| id | EN | FR (proposé) | Source (wiki) |
|---|---|---|---|
| `dragon_gate` | Dragon Gate | Porte du Dragon | LN vol. 6 ch. 9, vol. 14 ch. 8 |
| `monster_summon` | Monster Summon | Invocation de bête | wiki, sans tome |
| `spirit_summon` | Spirit Summon | Invocation d'esprit | wiki, sans tome |
| `otherworld_summon` | Otherworld Summon | Invocation d'un autre monde | LN vol. 10 ch. 11 |
| `lamplight_spirits` | Summon Lamplight Spirits | Esprits lumineux | LN vol. 12 ch. 2 |

### Magies uniques ou non classées

| id | EN | FR (proposé) | Source (wiki) |
|---|---|---|---|
| `disturb_magic` | Disturb Magic | Magie de perturbation | wiki, sans tome |
| `nuclear_explosion` | Nuclear Explosion | Explosion nucléaire | LN vol. 15 ch. 8 |
| `howling` | Howling | Hurlement | LN vol. 4 ch. 6 |
| `teleportation_magic` | Teleportation Magic | Magie de téléportation | wiki, sans tome |
| `electric` | Electric | Décharge | anime S3 ép. 5 |
| `time_travel_magic` | Time Travel Magic | Magie de voyage temporel | wiki, sans tome |
| `gravity_magic` | Gravity Magic | Magie de gravité | LN vol. 14 ch. 11 |
| `hypnosis_magic` | Hypnosis Magic | Magie d'hypnose | LN vol. 16 ch. 1 |

## Styles d'escrime et techniques

| id | EN | FR (proposé) | Source |
|---|---|---|---|
| `sword_god_style` | Sword God Style | Style du Dieu de l'Épée | page *Sword God Style* |
| `water_god_style` | Water God Style | Style du Dieu de l'Eau | page *Water God Style* |
| `north_god_style` | North God Style | Style du Dieu du Nord | page *North God Style* |

### Style du Dieu de l'Épée

| id | EN | FR (proposé) |
|---|---|---|
| `longsword_of_light` | Longsword of Light | Épée longue de lumière |
| `light_reversal` | Light Reversal | Renversement de lumière |
| `longsword_of_silence` | Longsword of Silence | Épée longue du silence |
| `arm_chop` | Arm Chop | Tranche-bras |
| `lake_surface_longsword` | Lake Surface Longsword | Épée longue de la surface du lac |
| `crescent_moon_longsword` | Crescent Moon Longsword | Épée longue du croissant de lune |

### Style du Dieu de l'Eau

| id | EN | FR (proposé) |
|---|---|---|
| `flow` | Flow | Flux |
| `five_secret_arts` | Five Secret Arts | Cinq arts secrets |
| `deprivation_sword_kingdom` | Deprivation Sword Kingdom | Royaume de l'épée de privation |

### Style du Dieu du Nord

| id | EN | FR (proposé) |
|---|---|---|
| `sword_throwing` | Sword Throwing Techniques | Techniques de lancer d'épée |
| `four_legged_form` | Four Legged Form | Posture à quatre pattes |
| `hazy_cross` | Hazy Cross | Croix brumeuse |
| `teardrop_bullet` | Teardrop Bullet | Balle en larme |
| `crimson_ink` | Crimson Ink | Encre écarlate |
| `pecking` | Pecking | Becquetage |
| `aura_shifting` | Battle Aura shifting | Déplacement d'aura |
| `moonlight_cleave` | Moonlight Cleave | Fendoir de lune |
| `gravity_fracture` | Gravity Fracture | Fracture gravitationnelle |

La page mentionne aussi l'**Incurable North God Style**, une branche du style, et le **déplacement d'aura de combat**. Le détail de chaque technique (effet, rang requis, tome) reste à relever page par page avant de les concevoir.

## Lieux et institutions (cités dans les pages lues)

| id | EN | FR (proposé) |
|---|---|---|
| `six_faced_world` | Six-Faced World | Monde aux six faces |
| `central_continent` | Central Continent | Continent central |
| `holy_land_of_swords` | Holy Land of Swords | Terre sainte de l'Épée |
| `asura_kingdom` | Asura Kingdom | Royaume d'Asura |
| `dragon_king_kingdom` | Kingdom of the Dragon King | Royaume du Roi Dragon |
| `shirone_kingdom` | Shirone Kingdom | Royaume de Shirone |
| `holy_milis_kingdom` | Holy Milis Kingdom | Saint-Royaume de Milis |
| `millishion` | Millishion | Millishion |
| `milis_religion` | Milis Religion | Religion de Milis |
| `ranoa_magic_academy` | Ranoa Magic Academy | Académie de magie de Ranoa |
