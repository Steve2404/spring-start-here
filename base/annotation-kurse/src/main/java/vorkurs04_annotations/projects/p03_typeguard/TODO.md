# Projet 3 — TypeGuard : conteneurs, annotations de type, javadoc (cours 0.4.6 → 0.4.7)

> Première fois ? Lis d'abord le mode d'emploi [`vorkurs04_annotations/PARCOURS.md`](../../PARCOURS.md).

**Notions visées :**
- **0.4.6** : `@Documented`, `@Inherited` (déclaration), `@Repeatable` et toutes les règles du conteneur.
- **0.4.7** : annotation de **déclaration** contre annotation de **type**, le cas `@A String x` (les deux à la fois), les noms qualifiés, `var`, `.class`, les tableaux et les génériques.

**Ce qui est donné :** `Data.java`, `Check.java`, `projectkit.Javac` (`compile`, `javap`, `javadocPage`).  
**Ce que TU crées :** tout le reste, dans `vorkurs04_annotations.projects.p03_typeguard`.
- Noms imposés (les sources de `Data` les utilisent) : `Tag`, `Tags`, `NonEmpty`, `Column`, `Trimmed`, `Public`, `Internal`, `Audit`.
- Classe `main` : **`TypeGuard`**.

**Pour vérifier :** lance `Check.java` depuis `base/annotation-kurse`.

---

## Le problème

Une équipe construit une bibliothèque de validation. Elle s'appuie sur des annotations **répétables** et des annotations **de type** (`List<@NonEmpty String>`). TypeGuard vérifie, toujours **sans reflection** :
1. les conteneurs `@Repeatable` proposés par d'autres équipes ;
2. des placements d'annotations, où javac accepte ou refuse ;
3. **sur quelle partie exacte d'un type** chaque annotation atterrit, en lisant `javap` ;
4. ce que **javadoc** publie.

---

## Tableau de bord

### ☐ Étape 1 — Tes annotations (0.4.6, 0.4.7)

Toutes en `RUNTIME`.

| Annotation | Élément | Cibles | Autres méta-annotations |
|---|---|---|---|
| `Tag` | `value` (texte) | `TYPE`, `METHOD` | `@Documented`, répétable, conteneur `Tags` |
| `Tags` | à toi de trouver | à toi de trouver | à toi de trouver |
| `NonEmpty` | — | `TYPE_USE` | — |
| `Column` | — | `FIELD` | — |
| `Trimmed` | — | `FIELD` **et** `TYPE_USE` | — |
| `Public` | — | `TYPE` | `@Documented` |
| `Internal` | — | `TYPE` | **pas** `@Documented` |
| `Audit` | — | `TYPE` | `@Inherited` |

- Pour `Tags`, déduis les règles de l'étape 2 : ton conteneur doit toutes les respecter.
- **Question sur `Audit`** : sur une classe, une interface, une méthode, qu'est-ce que `@Inherited` change, et qu'est-ce qu'il ne change pas ? Tu le **verras** au projet 4.

### ☐ Étape 2 — Les conteneurs des autres équipes (0.4.6)

`Data.CONTAINERS` : 9 propositions `C1` → `C9`. Chacune est une annotation `Label` avec son conteneur `Labels`.
- Compile chaque proposition seule. Garde la **première** erreur et traduis son code :

| Code `compiler.err.invalid.repeatable.annotation…` | Libellé |
|---|---|
| `….no.value` | `le conteneur n'a pas d'element value` |
| `….value.return` | `value n'est pas un tableau de l'annotation` |
| `….retention` | `conteneur moins durable` |
| `….incompatible.target` | `conteneur applicable a plus d'endroits` |
| `….not.documented` | `conteneur non @Documented` |
| `….not.inherited` | `conteneur non @Inherited` |
| `….elem.nondefault` | `autre element sans valeur par defaut` |

(Le code complet est `compiler.err.invalid.repeatable.annotation.no.value`, etc.)
- Un code absent de la table s'affiche tel quel.
```
CONTENEUR C1 OK
CONTENEUR C2 REFUSE l.2 : le conteneur n'a pas d'element value
```
- **Avant de lancer, prédis `C9` :** le conteneur a **moins** de cibles que `Label`, plus un élément avec une valeur par défaut. Accepté ?

### ☐ Étape 3 — Les placements (0.4.6, 0.4.7)

`Data.PLACEMENTS` : 16 sources `P01` → `P16`, qui utilisent **tes** annotations (remplace `{{PKG}}`). Même verdict qu'à l'étape 2, avec cette table :

| Code | Libellé |
|---|---|
| `compiler.err.cant.type.annotate.scoping.1` | `annotation devant un nom qualifie` |
| `compiler.err.annotation.type.not.applicable` | `contexte interdit` |
| `compiler.err.annotation.type.not.applicable.to.type` | `pas une annotation de type` |
| `compiler.err.no.annotations.on.dot.class` | `annotation sur .class` |

```
PLACEMENT P01 OK
PLACEMENT P06 REFUSE l.3 : annotation devant un nom qualifie
```
**À expliquer après le lancement :**
- **P03** mélange un `@Tags(…)` explicite et un `@Tag` seul. Ce javac l'accepte. Que verra-t-on à l'exécution ?
- **P06 / P07** : `@NonEmpty java.lang.String` est refusé, alors que `java.lang.@NonEmpty String` est accepté. Une annotation de type se place **juste devant le nom simple** du type qu'elle qualifie. Pourquoi ?
- **P10** (`@NonEmpty var`) : il n'y a pas de type écrit à annoter.
- **P14 / P15** : `@Column` est permis devant le champ, mais interdit dans `List<…>`. Pourquoi ?

### ☐ Étape 4 — Où atterrit chaque annotation (0.4.7)

- Compile `Data.MODEL` sous le nom `Model`, puis lis `Javac.javap(résultat, "Model")`.
- Pour une annotation de **type**, javap écrit la **partie** du type annotée. Vrai extrait :
```
  java.lang.String[] lines;
    descriptor: [Ljava/lang/String;
    flags: (0x0000)
    RuntimeVisibleTypeAnnotations:
      0: #12(): FIELD, location=[ARRAY]
        <ton paquet>.NonEmpty
```
- **Comment lire le texte :**
  - Un **champ** s'annonce par une ligne indentée de **2 espaces exactement**, qui finit par `;` et n'a pas de parenthèse. Le nom du champ est le dernier mot.
  - Une ligne de 2 espaces **avec** parenthèse est une méthode (ici, le constructeur) : elle termine le champ.
  - Dans `RuntimeVisibleAnnotations`, une annotation est une annotation de **déclaration**.
  - Dans `RuntimeVisibleTypeAnnotations`, la ligne `N: #x(): FIELD` (avec ou sans `, location=[…]`) précède le nom de l'annotation et donne sa **partie du type**.
- **Traduction de `location` :**
  - pas de location → `type entier` ;
  - `ARRAY` → `element` ;
  - `TYPE_ARGUMENT(i)` → `argument i` ;
  - plusieurs pas, joints par ` > ` : `[TYPE_ARGUMENT(1), TYPE_ARGUMENT(0)]` → `argument 1 > argument 0`.
- **Affiche une ligne par champ**, dans l'ordre de javap. Chaque annotation s'écrit `@Nom sur <partie>`, ou `@Nom sur declaration`, et les annotations sont séparées par `, ` :
```
CHAMP lines : @NonEmpty sur element
CHAMP code : @Trimmed sur declaration, @Trimmed sur type entier
```
- **Questions :**
  - `@NonEmpty String[] lines` et `String @NonEmpty [] pages` : lequel annote les chaînes, lequel annote le tableau ?
  - `@NonEmpty int[][] grid` annote quoi exactement ?
  - Pourquoi `@Trimmed` apparaît-il **deux fois** sur `code` (0.4.7 S2, « déclaration, type ou les deux ») ?

### ☐ Étape 5 — Ce que publie javadoc (0.4.6)

- `Javac.javadocPage(Map.of("Report", source), "Report")` lance **javadoc** sur `Data.REPORT` (avec `{{PKG}}` remplacé) et rend la page HTML de la classe.
- Trouve, **dans l'ordre de la page**, chaque occurrence de `@Public`, `@Internal` ou `@Tag` (le nom suivi d'une fin de mot), puis affiche les noms sans `@`, séparés par un espace :
```
JAVADOC Report : Public Tag Tag
```
- **Questions :**
  - Pourquoi `Internal` n'est-il pas publié ?
  - Que se passerait-il pour `Tag` si `Tags` n'était pas `@Documented` ? (Indice : l'étape 2 répond déjà.)

### ☐ Étape 6 — Le `main` de `TypeGuard`

Il affiche, dans l'ordre :
1. les 9 lignes `CONTENEUR` ;
2. les 16 lignes `PLACEMENT` ;
3. les lignes `CHAMP` ;
4. la ligne `JAVADOC`.

Puis lance `Check.java`.

**Expérience** (hors sortie attendue) : retire `@Documented` de `Tags`, recompile. Qui proteste, et avec quel code ? Remets-le.

---

## Checklist API (vérifiée par `Check`)

| Élément | Étape | ☐ |
|---|---|---|
| `@Repeatable(...)`, `@Documented`, `@Inherited` | 1 | ☐ |
| `ElementType.TYPE_USE`, `ElementType.FIELD` | 1 | ☐ |
| `Data.CONTAINERS`, `Data.PLACEMENTS`, `Javac.compile`, `code()`, `line()` | 2, 3 | ☐ |
| `Data.MODEL`, `Javac.javap` | 4 | ☐ |
| `Data.REPORT`, `Javac.javadocPage` | 5 | ☐ |
| ~~`.message()`~~, ~~reflection~~ | **interdits** | — |

---

## Sortie attendue complète

C'est le contrat exact que vérifie `Check` :

```
a remplir
```
