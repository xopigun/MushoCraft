# Visuels : techniques relevées dans *My Hero Craft*

Xopi a fourni le 19/10/2026 le mod *My Hero Craft* 1.0.0 (auteur « kujira », Forge 1.20.1) comme référence visuelle, et a demandé de le décompiler pour l'étudier.

**Licence : « All Rights Reserved ».** On n'en reprend **ni code, ni texture, ni modèle, ni son**. Seules les techniques ci-dessous servent d'inspiration ; tout est réécrit et dessiné pour MushoCraft.

## Ce qui fait son rendu

Pas de shader, pas de post-traitement, pas de bloom : uniquement des types de rendu vanilla, des astuces de mélange et des dessins.

1. **Particules en flipbook dessinées à la main**, à bords nets (aspect cel-shading) :
   - jusqu'à 18 images, en 64 à 720 px selon l'importance de l'effet ;
   - durées très courtes (3 à 9 ticks pour une entaille ou un éclair) ;
   - rotation aléatoire, toujours en pleine lumière ;
   - l'image affichée dépend de l'âge rapporté à la durée de vie.
2. **Mélange additif pour le feu** (type de rendu de particule personnalisé, sans écriture de profondeur). La flamme **s'assombrit au lieu de s'effacer**, puis **passe en translucide quand elle est assez sombre** : le même sprite devient de la fumée.
3. **Deux teintes tirées au hasard par particule** (par exemple bleu profond et cyan pâle) : des bandes de couleur sans rien dessiner de plus.
4. **Plans orientés et doubles faces** (quad émis deux fois, miroir) pour les entailles, les anneaux de choc et les ondes, orientés selon l'attaque et non vers la caméra.
5. **Tunnel d'anneaux de choc** laissés le long de la trajectoire, inclinés au hasard. **Anneaux au sol** qui s'agrandissent de tick en tick et épousent le relief.
6. **Rayons** : deux prismes additifs imbriqués (gaine colorée et cœur chaud), texture qui défile.
7. **Coque lumineuse** : cœur sombre ou blanc, plus une coque additive 1,5 fois plus grande. Donne une silhouette nette sans shader.
8. **Tornade en anneaux empilés** : torsion qui croît avec la hauteur, ondulation, et dissipation par le bas.
9. **Éclairs procéduraux** : marches aléatoires de 3 à 6 segments, qui vivent 3 à 7 ticks et sont retirées tous les 2 ticks.
10. **Traînées en ruban** face à la caméra, plus un ruban large et pâle derrière, qui fait office de « bloom ».
11. **Os GeckoLib comme émetteurs de particules** (position monde d'un os nommé).
12. **Tremblement de caméra** déclenché par un effet de statut, qui s'éteint tout seul (roulis et translation sinusoïdaux).
13. **Un son à chaque tick** tant que l'effet dure, à hauteur aléatoire, et deux sons superposés à l'impact.

## Ce qu'on adopte dans MushoCraft (ordre proposé, état au 19/10)

1. ✅ Tremblement de caméra pour les gros sorts (réglage client `effects.cameraShake`).
2. ✅ Particules de feu additives, avec passage du feu à la fumée et deux teintes.
3. ✅ Plans orientés doubles faces : entailles des techniques d'épée, anneaux de choc le long des projectiles de vent et des souffles, anneaux au sol. Leurs images sont hors de l'atlas des particules, en bandes de 256 px lues en filtrage linéaire sans mipmap (`SmoothSheet`) : leurs bords restent lisses même grands et vus de biais.
4. ✅ Rayons en prismes imbriqués (*Tornado Impact*).
5. ✅ Coque lumineuse sur les projectiles de lumière, de vent et de glace.
6. ✅ Éclairs procéduraux (*Lightning*, *Cumulonimbus*).
7. ✅ Traînées en ruban avec halo (feu, vent, lumière, glace).
8. ✅ Particules émises depuis les os des modèles Blockbench (pointe du pilier de glace, éclats des pics de terre).
9. ✅ Flipbooks dessinés en haute résolution (128 px), générés par script pour MushoCraft :
   - étincelle d'impact en 8 images, teintée par l'élément, à la fin de chaque projectile ;
   - explosion en 12 images : boule blanche à rayons, boule orange cernée de sombre qui se déchire, fumée qui se disperse.

**Outil de contrôle** : `gradlew runClientGallery` lance chaque sort du catalogue en vue à la première personne, sur une arène dans le ciel, et enregistre deux captures par sort (`run/screenshots/gallery_*`).
