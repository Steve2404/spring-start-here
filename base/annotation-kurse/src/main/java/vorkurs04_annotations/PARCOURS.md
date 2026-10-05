# Vorkurs 0.4 (Annotations & Reflection) — Mode d'emploi

Lis ce fichier **en entier une fois** avant de commencer. Il répond à cinq questions : par où commencer, comment le dossier est rangé, comment lire une consigne, comment travailler, et comment se comporter quand on bloque.

---

## 1. Ce que tu vas faire, en une phrase

Tu vas **construire 9 applications** (les projets) pour **comprendre** les annotations, la reflection et l'annotation processing, c'est-à-dire ce que Spring fait pour toi. Ensuite, tu **refais de mémoire, à intervalles espacés**, 8 petits drills chronométrés pour les **retenir**.

| | Projets (`projects/`) | Drills de rappel (`drills/`) |
|---|---|---|
| But | comprendre, concevoir, résoudre un problème | retrouver vite et sans aide |
| Durée | 2 à 6 h chacun, en plusieurs séances | 15 à 30 min chacun |
| Combien de fois | une fois, puis p05 et p09 refaits 2 à 3 semaines plus tard | 6 fois chacun (J0, J+1, J+3, J+7, J+14, J+30) |
| Aide autorisée | Javadoc, réflexion, papier | **aucune** pendant le drill |

---

## 1 bis. La règle du crescendo : seulement ce que le cours a déjà montré

Chaque projet se résout **uniquement** avec les sections du cours 0.4 déjà vues. `Check` refuse les notions qui arrivent plus tard. Si tu vois `[FAIL] API : interdit ici`, cherche la solution avec les outils de la section en cours.

| Projet | Sections | Interdit, car vu plus tard |
|---|---|---|
| p01 | 0.4.1 → 0.4.3 | `@Target`, `@Retention` et les autres méta-annotations ; toute reflection (`getAnnotation`, `getDeclared…`, `invoke`, `Class.forName`) ; le texte des diagnostics (`.message()`) |
| p02 | 0.4.4 → 0.4.5 | `@Repeatable`, `@Inherited`, `@Documented` ; toute reflection ; `.message()` |
| p03 | 0.4.6 → 0.4.7 | toute reflection ; `.message()` |
| p04 | 0.4.8 | `invoke`, `newInstance`, forcer l'accès, les types génériques (`getGeneric…`), les processors |
| p05 | 0.4.9 → 0.4.10 | `setAccessible`, `newInstance`, `getGeneric…`, les processors |
| p06 | 0.4.11 | `invoke`, `newInstance`, `setAccessible`, les processors ; `getTypeName` (tu écris le rendu toi-même) |
| p07 | 0.4.12 | les processors |
| p08 | 0.4.13 | — |
| p09 | 0.4.14 → 0.4.15 | `setAccessible` : un bon framework passe par les constructeurs publics |

**Pourquoi refuser `.message()` ?** Le texte des diagnostics de javac dépend de la langue de la machine (allemande ici). Le **code** (`Javac.Diag::code`, par exemple `compiler.err.invalid.annotation.member.type`) ne change jamais : c'est lui qu'un outil sérieux compare.

---

## 2. Par où commencer (aujourd'hui)

1. Lis ce fichier jusqu'au bout.
2. Ouvre `projects/p01_pluginstore/TODO.md` en **aperçu Markdown** (dans IntelliJ, l'icône « Preview » en haut à droite de l'éditeur).
3. Suis la section 4 ci-dessous, « Comment faire un projet ».
4. Quand p01 affiche `PROJET REUSSI`, fais le drill `drills/r01_rules`, en suivant la section 5.

**L'ordre complet :**

```
p01 → r01
p02
p03 → r02
p04 → r03
p05 → r04
p06 → r05
p07 → r06
p08 → r07
p09 → r08 (1er passage, puis test final 2 semaines plus tard)
```

Pendant tout ce temps, les **répétitions** des drills déjà faits passent **avant** le projet du jour (section 6).

---

## 3. La disposition des dossiers

```
base/annotation-kurse/
├── pom.xml                      ← Java 17, aucune dépendance
└── src/main/java/
    ├── projectkit/
    │   ├── ProjectChecker.java  ← le moteur des Check (ne pas modifier)
    │   └── Javac.java           ← OUTIL FOURNI : javac en mémoire, chargement des classes, javap, javadoc
    └── vorkurs04_annotations/
        ├── PARCOURS.md          ← ce fichier (le mode d'emploi)
        ├── projects/
        │   ├── README.md        ← la liste des 9 projets, à cocher
        │   └── p01_pluginstore/
        │       ├── TODO.md      ← L'ÉNONCÉ : tu le lis
        │       ├── Data.java    ← les données : tu les lis, tu ne les modifies pas
        │       ├── Check.java   ← le correcteur : tu le LANCES, tu ne le modifies pas
        │       ├── solution/    ← la correction : tu ne l'ouvres qu'à la fin
        │       └── (tes fichiers) ← TOUT le reste, c'est TOI qui le crées ici
        └── drills/
            ├── README.md        ← règles des drills + tableau de suivi des répétitions
            └── r01_rules/
                ├── TODO.md      ← les défis + la carte mémoire repliée en bas
                ├── Check.java
                ├── solution/
                └── (ton RecallNN.java)
```

**L'outil `projectkit.Javac`.** Beaucoup de règles des annotations sont des règles du **compilateur** (un type d'élément interdit, une cible non permise, un conteneur mal formé…). Pour les tester dans un programme, `Javac` compile des sources écrites dans des `String` :
- `Javac.compile(Map.of("Nom", source))` rend un `Result` : `success()`, `diags()` (chaque `Diag` a `kind`, `file`, `line`, `code`, `message`), `classes()`, `generated()` ;
- `Javac.compile(sources, options, processor)` lance en plus **ton** annotation processor (p08, r07) ;
- `Javac.load(result, "Nom")` charge une classe compilée (pour la reflection) ;
- `Javac.javap(result, "Nom")` rend le bytecode désassemblé (p02, p03) ;
- `Javac.javadocPage(sources, "Nom")` rend la page javadoc (p03).

Lis sa Javadoc une fois : c'est le seul outil fourni, tout le reste vient de toi.

**Où créer tes fichiers :**
- Dans le **même dossier** que le `TODO.md`, donc dans le même paquet, par exemple `package vorkurs04_annotations.projects.p01_pluginstore;`.
- Clic droit sur le dossier → New → Java Class.
- Tu peux créer autant de fichiers que tu veux, ou tout mettre dans un seul fichier avec des types imbriqués. Le correcteur lit tous les `.java` du dossier, sauf `Data.java` et `Check.java`.
- **Les noms imposés** sont listés en haut de chaque `TODO.md` : la classe du `main` (`PluginStore`, `Inspector`… ; pour les drills, `Recall01`, `Recall02`…), et, quand des sources de `Data` s'en servent, les annotations et types qu'elles utilisent.

---

## 4. Comment faire un projet

### 4.1 Lire le `TODO.md` (dans cet ordre)

| Partie du `TODO.md` | Ce que tu en fais |
|---|---|
| **En-tête** (notions visées, donné, à créer, noms imposés) | Tu sais quelles notions tu vas pratiquer et quels noms respecter. |
| **Le problème** | Tu comprends l'application dans son ensemble. Ne code rien encore. |
| **Tableau de bord** (les étapes ☐) | C'est ta feuille de route. **Chaque étape contient tout ce qu'il te faut :** la règle, puis les lignes exactes à afficher (dans un bloc gris), puis les **contraintes**, puis les **questions**. |
| **Checklist API** | Les éléments que `Check` cherchera dans ton code, avec l'étape où ils ont leur place, et ceux qui sont **interdits**. |
| **Sortie attendue complète** | Le contrat exact, ligne par ligne. `Check` compare au caractère près. |

### 4.2 Travailler, étape par étape

1. **Prévois à la main** 2 ou 3 lignes de la sortie attendue, à partir de `Data.java` : « cette soumission compile-t-elle ? cette annotation est-elle visible à l'exécution ? ». Si tu n'arrives pas à le prévoir, relis la section du cours.
2. **Conçois sur papier** : quelles annotations (cible, rétention, éléments) ? quels records pour les résultats ? qui fait quoi ? C'est **toi** qui décides.
3. **Crée la classe du `main`** tout de suite, même vide. Cela te permet de lancer `Check` dès le début.
4. **Fais une étape à la fois.** Code-la, lance `Check`, corrige, puis coche ☐ → ☑ dans le `TODO.md`.
5. **Réponds aux questions de l'étape par écrit**, en commentaire dans ton code. Ce sont les pièges qu'on te posera en entretien. Une étape dont tu n'as pas répondu aux questions n'est pas finie.
6. **Trie** tout ce qui vient de la reflection (`getDeclaredMethods()`, `getDeclaredFields()`…) : leur ordre n'est **pas garanti**. Les sorties attendues sont toujours triées, l'étape dit comment.

### 4.3 Lancer `Check` et lire sa réponse

Clic droit sur `Check.java` → **Run 'Check.main()'**. Ça marche que IntelliJ ait ouvert le dépôt entier ou seulement `base/annotation-kurse`. Au tout début de p01, il affiche :

```
=== Verification de vorkurs04_annotations.projects.p01_pluginstore.PluginStore ===
[ERREUR] classe introuvable : vorkurs04_annotations.projects.p01_pluginstore.PluginStore (as-tu cree la classe avec ce nom et ce paquet ?)
[FAIL] sortie : 0/23 lignes justes avant la 1re difference (ligne 1)
       attendu : ...
       obtenu  : (rien de plus)
[FAIL] API : encore a placer dans ton code : [...]
```

| Ligne | Signification | Que faire |
|---|---|---|
| `[ERREUR] classe introuvable` | ta classe `main` n'existe pas, ou elle a un mauvais nom ou un mauvais paquet | crée-la avec le nom exact |
| `[ERREUR] ton programme a lance …` | ton `main` a planté (exception) | lis l'exception ; avec `invoke`, regarde la **cause** de l'`InvocationTargetException` |
| `[FAIL] sortie : 12/23 lignes justes … (ligne 13)` | les 12 premières lignes sont bonnes, la 13e diffère | compare `attendu` et `obtenu` **caractère par caractère** |
| `[FAIL] API : encore a placer …` | ces éléments n'apparaissent pas encore dans ton code | normal tant que tu n'as pas fini ; la checklist te dit à quelle étape ils servent |
| `[FAIL] API : … interdit …` | tu as utilisé une notion d'une section suivante | remplace-la (section 1 bis) |
| `*** PROJET REUSSI ***` | tout est juste | passe à la section 4.5 |

**Astuces :**
- Lance `Check` **souvent** : il te donne toujours la **première** ligne fausse, c'est donc ta prochaine tâche.
- Pour voir ta sortie brute, lance directement ta classe `main`.
- `Check` avec l'argument `solution` (Run → Edit Configurations → Program arguments) vérifie la solution, sans que tu aies à la lire.
- Sans IntelliJ, depuis `base/annotation-kurse` (Git Bash) : compile tout avec `javac --release 17 -encoding UTF-8 -proc:none -d target/classes $(find src/main/java -name '*.java')`, puis lance `java -cp target/classes vorkurs04_annotations.projects.p01_pluginstore.Check`. Sans `-encoding UTF-8`, les accents cassent sous Windows.

### 4.4 Quand tu bloques (la règle des 3 paliers)

| Palier | Combien de temps | Ce que tu fais |
|---|---|---|
| 1 | jusqu'à 20 min | relis l'étape, ses contraintes et ses questions ; relis la section du cours dans Notion ; ouvre la **Javadoc** (`java.lang.reflect`, `java.lang.annotation`, `javax.annotation.processing`) |
| 2 | 20 min de plus | relis la **carte mémoire** du drill du même thème (bas du `TODO.md` dans `drills/`) ; ou demande-moi un **indice** sur ce point précis, sans la solution |
| 3 | en dernier recours | ouvre `solution/`, mais lis **uniquement** la méthode qui te bloque, puis **ferme**, et réécris-la de mémoire. Note `// AIDE : solution consultée` dans ton code : cette étape devra être refaite plus tard |

**Ce qu'il ne faut jamais faire :**
- copier-coller depuis `solution/` ;
- modifier `Data.java`, `Check.java` ou `projectkit` pour faire passer le test ;
- sauter les questions des étapes ;
- coder sans avoir prévu la sortie à la main.

### 4.5 Quand c'est réussi

1. Ouvre `solution/` et **compare ta conception**. Les commentaires de la solution expliquent **pourquoi** et signalent les **pièges**.
2. Si tu veux, montre-moi ton code : je relis ta **conception**, pas seulement le résultat.
3. Coche le projet dans `projects/README.md`.
4. Fais le drill associé (voir l'ordre en section 2).

---

## 5. Comment faire un drill de rappel

1. Note l'heure de départ et le **chrono cible** (écrit en haut du `TODO.md` du drill).
2. Crée `RecallNN.java` dans le dossier du drill, avec tout ce que les règles du `TODO.md` décrivent.
3. **Fais les défis D01, D02…** Chaque défi donne sa consigne, et la ligne exacte à afficher est juste en dessous (`→ D01 : …`).
4. **Rien d'autre que ta mémoire :** ni carte mémoire, ni Javadoc, ni solution, ni tes projets. Si un défi bloque plus de 3 minutes, marque-le ✗ et passe au suivant.
5. Lance `Check`.
6. **Après seulement,** ouvre la **carte mémoire** (en bas du `TODO.md`, « Ouvrir la carte »). Relis ce qui concerne tes ✗ et termine ces défis.
7. Note la date, le temps et le nombre de ✗ dans le tableau de `drills/README.md`.
8. **Avant chaque répétition, supprime ton `RecallNN.java`.** On repart toujours d'un fichier vide.

Pourquoi ces règles ? La mémoire se renforce quand on **fait l'effort de retrouver**, pas quand on relit. Regarder la carte avant de chercher supprime précisément cet effort.

---

## 6. Le rythme

**Une séance type (1 h 30) :**

| Durée | Activité |
|---|---|
| 10 à 20 min | les **répétitions dues** aujourd'hui (voir le tableau de `drills/README.md`) : toujours en premier |
| 60 min | le projet en cours, une ou deux étapes |
| 10 à 20 min | si un projet vient d'être fini : le premier passage (J0) de son drill |

**La répétition espacée** (détails dans `drills/README.md`) :
- après le premier passage J0, refais le même drill à J+1, J+3, J+7, J+14 et J+30 ;
- un drill raté (plus de 2 ✗) recule d'un palier ;
- un drill parfait et rapide peut en sauter un.

---

## 7. Comment savoir que le chapitre est acquis

- [ ] Les 9 projets affichent `PROJET REUSSI`, et toutes les questions des `TODO.md` ont une réponse écrite.
- [ ] Les 8 drills ont passé la répétition R3 (J+7, sans carte).
- [ ] r08 (kata mixte) passe en moins de 30 minutes, sans carte.
- [ ] p05 et p09 ont été refaits **depuis un dossier vide** (déplace tes fichiers ailleurs), 2 à 3 semaines plus tard, sans regarder ton ancien code.
- [ ] Tu sais expliquer, sans notes, comment Spring trouve un `@Component`, l'instancie, injecte ses dépendances et appelle ses méthodes de cycle de vie : c'est ce que p09 t'a fait construire.

---

## 8. Ce que couvre le parcours

| Cours | Projets | Drills |
|---|---|---|
| 0.4.1 Métadonnées : pourquoi les annotations | p01 | r01 |
| 0.4.2 Lire les annotations connues (`@Override`, `@Deprecated`, `@SuppressWarnings`, `@SafeVarargs`, `@FunctionalInterface`) | p01 | r01 |
| 0.4.3 Définir sa propre annotation avec `@interface` (éléments, types permis, défauts, `value`) | p01 | r01, r08 |
| 0.4.4 `@Target` et `ElementType` | p02 | r02, r08 |
| 0.4.5 `@Retention` : `SOURCE`, `CLASS`, `RUNTIME` | p02 | r02, r08 |
| 0.4.6 `@Documented`, `@Inherited`, `@Repeatable` | p03 | r02, r08 |
| 0.4.7 Annotation de déclaration contre annotation de type | p03, p06 | r02 |
| 0.4.8 Lire les annotations par reflection (`Class`, `Field`, `Method`, `Parameter`, `AnnotatedType`) | p04 | r03, r08 |
| 0.4.9 → 0.4.10 Décider à l'exécution d'après les annotations ; `Method.invoke` (receveur, arguments, retour, erreurs) | p05 | r04, r08 |
| 0.4.11 Reflection, génériques et `AnnotatedType` | p06 | r05, r08 |
| 0.4.12 Contrôle d'accès, encapsulation, limites de la reflection | p07 | r06, r08 |
| 0.4.13 Annotation processing à la compilation (processor, rounds, code généré) | p08 | r07 |
| 0.4.14 → 0.4.15 Design piloté par les métadonnées : un mini-framework sans Spring, puis le labo | p09 | r08 |
