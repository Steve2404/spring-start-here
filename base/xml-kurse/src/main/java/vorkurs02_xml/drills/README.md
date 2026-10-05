# Vorkurs 0.2 — Drills de rappel (mémorisation)

> Première fois ? Lis d'abord le mode d'emploi [`vorkurs02_xml/PARCOURS.md`](../PARCOURS.md).

Les **projets** (`../projects`) te font **comprendre** XML et ses API. Les **drills de rappel** te les font **retenir** : retrouver vite, de mémoire, la bonne règle, la bonne méthode, et son piège.

## Le format

- **Dans chaque dossier `rNN_…` :**
  - `TODO.md` : les défis D01, D02…, chacun avec **la ligne exacte attendue juste en dessous**, et la **carte mémoire** repliée en bas (sauf pour le kata r10) ;
  - `Check.java` : le correcteur (sortie, puis éléments d'API exigés) ;
  - `solution/` : la solution commentée.
- **Tu crées toi-même** la classe `RecallNN` et son `main`.
- Les drills r03, r05 à r10 partagent le document `files/campus.xml`, lu par `vorkurs02_xml.drills.Data` (`campus()` donne le chemin, `campusText()` le texte).
- Les drills r01 à r05 utilisent l'outil `xmlkit.XmlKit` (comme les projets 1 à 7) ; r06 à r09 les vraies API.
- Lance `Check.java`. Avec l'argument `solution`, il vérifie la solution.

## Les règles d'un drill

1. **Chronomètre-toi.** Vise le temps indiqué en haut du `TODO.md`.
2. **Ni carte, ni Javadoc, ni solution pendant le drill.** L'effort de rappel est **précisément** ce qui fixe la mémoire.
3. **Si tu bloques sur un défi,** passe au suivant et marque-le ✗.
4. **À la fin,** ouvre la carte mémoire. Relis **seulement** ce qui concerne tes ✗, puis termine ces défis.
5. **Remise à zéro :** supprime ton `RecallNN.java` avant chaque répétition.

## Le plan de répétition espacée

| Répétition | Quand | Objectif |
|---|---|---|
| R1 | J+1 | tout juste, même lentement |
| R2 | J+3 | sous le chrono cible |
| R3 | J+7 | sous le chrono, **sans ouvrir la carte** |
| R4 | J+14 | moitié du chrono cible |
| R5 | J+30 | d'une traite ; ensuite, le drill est **acquis** |

- Un drill raté (plus de 2 ✗) revient au palier précédent.
- Un drill réussi sans faute **et** sous le chrono peut sauter un palier.

## Quand faire quel drill

| Après le projet… | Fais les drills |
|---|---|
| p02 `TextGate` | r01 |
| p04 `DtdCheck` | r02 |
| p05 `Names` | r03 |
| p06 `SchemaCheck` | r04 |
| p07 `Queries` | r05 |
| p08 `DomLab` | r06 |
| p09 `SaxLab` | r07 |
| p10 `StaxLab` | r08 |
| p12 `SafeImport` | r09 |
| p13 `LabPipeline` | r10 (premier passage), puis r10 en test final 2 semaines plus tard |

## Tableau de suivi

Note la date et le temps (par exemple `03/10 · 14 min · 1✗`).

| Drill | Thème | J0 | R1 | R2 | R3 | R4 | R5 |
|---|---|---|---|---|---|---|---|
| r01 | Syntaxe : déclaration, échappement, entités, CDATA, lookahead | | | | | | |
| r02 | DTD, bien formé contre valide | | | | | | |
| r03 | Namespaces | | | | | | |
| r04 | Écrire du XSD | | | | | | |
| r05 | Écrire du XPath | | | | | | |
| r06 | API DOM | | | | | | |
| r07 | API SAX | | | | | | |
| r08 | API StAX | | | | | | |
| r09 | Sécurité, validation et XPath par l'API | | | | | | |
| r10 | Kata mixte (test final) | | | | | | |

**Critère de fin du chapitre :**
- les 10 drills ont passé R3 ;
- r10 passe en moins de 30 minutes, sans carte ;
- p08 et p13 sont refaits **depuis zéro** 2 à 3 semaines plus tard.
