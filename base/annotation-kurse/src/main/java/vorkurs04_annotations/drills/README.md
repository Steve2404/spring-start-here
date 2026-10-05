# Vorkurs 0.4 — Drills de rappel (mémorisation)

> Première fois ? Lis d'abord le mode d'emploi [`vorkurs04_annotations/PARCOURS.md`](../PARCOURS.md) : comment lire une fiche, lancer `Check`, quoi faire en cas de blocage.

Les **projets** (`../projects`) te font **comprendre** les annotations et la reflection. Les **drills de rappel** te les font **retenir** : retrouver vite, de mémoire, la bonne méthode, ce qu'elle rend et son piège. C'est ce qu'on attend de toi en entretien, et ce que Spring fait sous le capot.

## Le format

- **Dans chaque dossier `rNN_…`, il y a :**
  - `TODO.md` : les défis D01, D02… Chaque défi donne sa consigne et **la ligne exacte attendue juste en dessous**. La **carte mémoire** est repliée en bas (sauf pour le kata r08) ;
  - `Check.java` : le correcteur. Il vérifie la sortie, puis les éléments de l'API que tu dois avoir utilisés ;
  - `solution/` : la solution commentée.
- **Tu crées toi-même** la classe `RecallNN`, son `main`, et tout ce que le `TODO.md` décrit (annotations, classes cibles, processor…). Les drills n'ont pas de `Data.java` : le modèle est assez petit pour être réécrit à chaque fois, et le réécrire **fait partie** du rappel.
- Pour compiler du code « à la volée » (r01, r02, r06, r07, r08), utilise l'outil fourni `projectkit.Javac`.
- Lance `Check.java`. Avec l'argument `solution`, il vérifie la solution.

## Les règles d'un drill (c'est là que la mémoire se construit)

1. **Chronomètre-toi.** Vise le temps indiqué en haut du `TODO.md`.
2. **Ni carte, ni Javadoc, ni solution pendant le drill.** L'effort de rappel est **précisément** ce qui fixe la mémoire. Relire ne la fixe presque pas.
3. **Si tu bloques sur un défi,** passe au suivant et marque-le ✗.
4. **À la fin,** ouvre la carte mémoire. Relis **seulement** ce qui concerne tes ✗, puis termine ces défis.
5. **Remise à zéro :** supprime ton `RecallNN.java` avant chaque répétition. On repart toujours d'un fichier vide.

## Le plan de répétition espacée

Après le premier passage (J0), refais **le même drill, depuis un fichier vide**, aux intervalles suivants :

| Répétition | Quand | Objectif |
|---|---|---|
| R1 | J+1 | tout juste, même lentement |
| R2 | J+3 | sous le chrono cible |
| R3 | J+7 | sous le chrono, **sans ouvrir la carte** |
| R4 | J+14 | moitié du chrono cible |
| R5 | J+30 | d'une traite ; ensuite, le drill est **acquis** |

**Ajuster le rythme :**
- Un drill raté (plus de 2 ✗) revient au palier précédent.
- Un drill réussi sans faute **et** sous le chrono peut sauter un palier.

## Quand faire quel drill (lien avec les projets)

| Après le projet… | Fais les drills |
|---|---|
| p01 `PluginStore` | r01 |
| p02 `ComplianceDesk` | (rien : r02 attend la fin de p03) |
| p03 `TypeGuard` | r02 |
| p04 `Inspector` | r03 |
| p05 `Console` | r04 |
| p06 `SchemaCheck` | r05 |
| p07 `AccessAuditor` | r06 |
| p08 `CodeGen` | r07 |
| p09 `MiniBoot` | r08 (premier passage), puis r08 en test final 2 semaines plus tard |

## Tableau de suivi

Note la date et le temps (par exemple `03/10 · 14 min · 1✗`).

| Drill | Thème | J0 | R1 | R2 | R3 | R4 | R5 |
|---|---|---|---|---|---|---|---|
| r01 | Règles de déclaration, annotations connues | | | | | | |
| r02 | Cibles, rétention, méta-annotations, annotations de type | | | | | | |
| r03 | Lire les annotations par reflection | | | | | | |
| r04 | `Method.invoke` | | | | | | |
| r05 | Reflection sur les génériques | | | | | | |
| r06 | Contrôle d'accès, modules | | | | | | |
| r07 | Annotation processing | | | | | | |
| r08 | Kata mixte (test final) | | | | | | |

**Critère de fin du chapitre :**
- les 8 drills ont passé R3 ;
- r08 passe en moins de 30 minutes, sans carte ;
- p05 et p09 sont refaits **depuis zéro** 2 à 3 semaines plus tard.
