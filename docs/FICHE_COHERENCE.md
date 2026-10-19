# MushoCraft — Fiche de cohérence

Pour chaque fonctionnalité : ce que dit la source, où le trouver, et comment le mod s'y conforme.

**Conventions**
- **Source** : tome et chapitre tels que cités par le wiki (LN = light novel, WN = web novel). Le texte du roman lui-même n'a pas été consulté.
- **Ce que dit la source** : résumé en français, pas une citation. Je n'ai pas accès au texte des tomes, et je ne recopie pas de longs passages du wiki.
- **Citation du roman** : à relever par Xopi dans le tome indiqué (phrase courte). Tant qu'elle manque, la ligne est marquée `à vérifier`.
- **Statut** :
  - `conforme` : le mod suit la source ;
  - `écart` : renvoie à `ECARTS.md` ;
  - `jeu` : mécanique de jeu sans équivalent dans le roman ;
  - `à vérifier` : la source est incertaine.

Dernière mise à jour : 19/10/2026.

## Magie

| # | Fonctionnalité | Ce que dit la source | Source | Citation du roman | Statut |
|---|---|---|---|---|---|
| M1 | Réservoir de mana fixé à la naissance | On croit généralement la réserve de mana héritée et déterminée à la naissance. | wiki *Magic* § Mana (pas de tome cité) | à relever | à vérifier |
| M2 | Croissance du réservoir par l'usage | Tout le monde peut augmenter sa mana en la stimulant dès le plus jeune âge (exemple : Julie). Rudeus l'a développée en l'épuisant encore et encore pendant son enfance (propos d'Orsted). | wiki *Magic* § Mana ; wiki *Mana* § Mana Capacity (pas de tome cité) | à relever | conforme (`ManaRules.growth`, bonus d'épuisement), voir E3 pour l'âge adulte |
| M2b | Corps capable de contenir beaucoup de mana ≠ réserve réelle | Rudeus avait un corps capable de contenir une quantité extraordinaire de mana, mais sans entraînement il aurait eu à peine plus qu'une personne ordinaire. | wiki *Mana* § Mana Capacity | à relever | conforme (plafond par talent, capacité réelle à entraîner) |
| M2c | Circulation du mana pour s'entraîner | Le wiki parle de « stimuler » son mana enfant, sans décrire de méthode jouable. | wiki *Magic*, *Mana* | à relever | jeu (touche de circulation) |
| M3 | Plafond par personnage | Pas de source explicite. Le plafond sert à garder le facteur de naissance déterminant. | — | — | jeu |
| M4 | Tout le monde peut faire de la magie | Une personne sur vingt naît capable d'utiliser la mana. | wiki *Magic* § Magic Rank Title (pas de tome cité) | à relever | écart E1 |
| M5 | Apprentissage obligatoire avant de lancer un sort | Rudeus apprend la magie avec un manuel. | LN vol. 1 ch. 3 « A Textbook of Magic » | à relever | conforme |
| M6 | Incantation complète | On récite une formule qui déclenche le sort sans contrôle fin du mana ; méthode la plus courante. | wiki *Magic* § Magic Spells | à relever | conforme |
| M7 | Incantation abrégée | Formule raccourcie (Roxy, Rudeus) : plus rapide, plus souple. | wiki *Magic* § Truncated | à relever | conforme |
| M8 | Incantation silencieuse en 4 temps | Forme, puis ajout de puissance (taille), puis vitesse, puis relâchement. Apprentissage lié à la pratique jeune. | wiki *Magic* § Silent Spell-casting | à relever | conforme |
| M9 | Cercles magiques | Puissance fixe, le plus rapide à utiliser ; méthode principale de l'invocation. Inventés par Kirisis Calisis. | LN vol. 1 ch. 3 ; LN vol. 10 ch. 11 | à relever | conforme |
| M10 | 7 rangs de sort | Beginner, Intermediate, Advanced, Saint, King, Emperor, God. Le rang suit en général le mana nécessaire. | wiki *Magic* § Magic Rank Title | à relever | conforme |
| M11 | Titre de mage | Le titre d'un mage = le sort le plus avancé qu'il sait lancer. | wiki *Magic* § Magic Rank Title | à relever | conforme (`MagicService.title`) |
| M11b | Progression par école | Pas de règle chiffrée dans le roman ; les mages ont un rang par école (ex. Rudeus Saint de l'eau). | wiki *Magic* | à relever | jeu (expérience par école, `MagicProficiency`) |
| M12 | Pas de contrainte de rang sur le mana | Décision de design : la capacité est un nombre, pas un rang. | — | — | jeu |
| M13 | Magie mixte | Enchaîner des sorts d'écoles différentes ; réservée à la magie d'attaque. | wiki *Magic* § Mixed Magic | à relever | conforme |
| M14 | Barrière et frappe divine bridées | Milis contrôle l'enseignement au-delà du rang Débutant ; Ranoa n'enseigne que le rang Débutant. | WN ch. 69 | à relever | conforme : seuls les sorts Débutants de ces écoles sont dans un livre ; *Physical Shield* attend un chevalier de Milis |
| M15 | Effets du soin par rang | Débutant : petites plaies. Intermédiaire : blessures graves. Avancé : rattache un membre tout juste tranché. Saint : restaure si les os sont là. Roi : régénère. Dieu : ressuscite un mort récent. | manga *Roxy Gets Serious* vol. 8 ch. 39 ; LN vol. 17 ch. 10 ; Q&A de l'auteur | à relever | à vérifier |
| M16 | Détoxification | La plupart des maux se soignent au rang Débutant ; les cas graves demandent un rang plus élevé. | LN vol. 1 ch. 8 (selon le wiki) | à relever | conforme |
| M17 | Foudre rangée dans l'eau | Du vivant de Rudeus, la foudre dérive de la magie de l'eau (*Lightning* vient de *Cumulonimbus*). | LN vol. 13 ch. 6 | à relever | conforme |
| M18 | Liste des sorts | Voir `GLOSSAIRE.md` : chaque sort y porte sa référence. | wiki *Magic Spells* | — | en cours |

## Épée et aura

| # | Fonctionnalité | Ce que dit la source | Source | Citation du roman | Statut |
|---|---|---|---|---|---|
| S1 | Trois grands styles | Dieu de l'Épée, Dieu de l'Eau, Dieu du Nord. | wiki, catégorie *Swordstyle* | à relever | conforme |
| S2 | Rangs d'escrime | Débutant à Dieu, avec les titres Saint, Roi, Empereur, Dieu par style. | pages des trois styles | à relever | conforme |
| S3 | Aura de combat = mana | L'aura enveloppe et comprime le corps dans le mana pour décupler les capacités physiques. | WN ch. 80 | à relever | conforme (elle puise dans le réservoir) |
| S4 | Aura par l'entraînement | Elle s'acquiert par l'entraînement physique, sans incantation. | WN ch. 80 | à relever | conforme |
| S5 | Certains n'obtiennent jamais l'aura | Rudeus et Laplace n'en ont pas. | WN ch. 80 ; Q&A de l'auteur, réponse 187 | à relever | écart E2 |
| S6 | Contrôle de l'aura et rang | Contrôle conscient = Avancé, contrôle complet = Saint (propos de Ghislaine, style du Dieu de l'Épée). | wiki *Battle Aura* (pas de tome cité) | à relever | à vérifier |
| S7 | Maîtres de différents niveaux | Chaque style a des pratiquants de tous rangs. | pages des trois styles | à relever | conforme |
| S8 | Un maître forme jusqu'à son propre rang | Aucune source trouvée. | — | — | écart E4 |
| S9 | Stats force, vitalité, vitesse, résistance | Mécanique de jeu. | — | — | jeu |
| S10 | Recharge commune par style | Mécanique de jeu. | — | — | jeu |

## Vie du personnage

| # | Fonctionnalité | Ce que dit la source | Source | Citation du roman | Statut |
|---|---|---|---|---|---|
| V1 | Naissance en bébé | Rudeus se réincarne en nourrisson. | LN vol. 1, prologue et ch. 1 (à confirmer) | à relever | à vérifier |
| V2 | Humains seulement en v1 | Choix de périmètre ; les autres races existent dans le roman. | wiki *Races* | — | écart E5 |
| V3 | Majorité à 15 ans | Dans le royaume d'Asura, on est considéré adulte à 15 ans ; les anniversaires ne sont fêtés que tous les 5 ans. | LN vol. 1 ch. 5 (wiki *Human*) | à relever | conforme (`LifeStage.ADULT` = 15 ans) |
| V4 | Étapes bébé (0-3), enfant (3-12), adolescent (12-15) | Pas de source : découpage de jeu. | — | — | jeu |
| V5 | Taille selon l'âge | Pas de source : environ 35 % de la taille adulte à la naissance, 100 % à 15 ans, interpolation linéaire. | — | — | jeu |
| V6 | Talents tirés à la naissance | Le brief demande un tirage autour d'une base commune. Le roman ne chiffre rien. | — | — | jeu |

## Interface

| # | Fonctionnalité | Statut |
|---|---|---|
| I1 | Mode combat = mode combat d'Epic Fight (touche R) | jeu |
| I2 | Barre de 9 moves à la place de la hotbar, recharges affichées | jeu |
| I3 | Molette = changer d'arme en mode combat | jeu |
