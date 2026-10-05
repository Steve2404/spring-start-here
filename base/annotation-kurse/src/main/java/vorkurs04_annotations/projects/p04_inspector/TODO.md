# Projet 4 — L'inspecteur d'annotations (cours 0.4.8)

> Première fois ? Lis d'abord le mode d'emploi [`vorkurs04_annotations/PARCOURS.md`](../../PARCOURS.md).

**Notions visées (0.4.8) :**
- pourquoi `RUNTIME` est nécessaire ;
- `Class` comme point d'entrée, et les annotations sur `Class`, `Field`, `Method` et `Parameter` ;
- `getAnnotation` contre `getDeclaredAnnotation`, et `@Inherited` ;
- les répétables : méthodes `…ByType` et conteneur ;
- `AnnotatedType` et ses quatre formes complexes ;
- trouver le bon niveau de reflection.

**Ce qui est donné :** `Data.java` (le modèle à inspecter, **sous forme de source**), `Check.java`, `projectkit.Javac` (`compile`, `load`, `javap`).

**Ce que TU crées :** tout le reste, dans `vorkurs04_annotations.projects.p04_inspector`.
- Noms imposés (le modèle les utilise) : `Audit`, `Tag`, `Tags`, `Column`, `Sensitive`, `Trace`, `NonEmpty`, `Range`.
- Classe `main` : **`Inspector`**.

**Pour vérifier :** lance `Check.java` depuis `base/annotation-kurse`.

---

## Le problème

Une équipe veut un **inspecteur** : un outil qui affiche ce que les annotations laissent **à l'exécution** sur un modèle bancaire. C'est exactement ce que fait un framework au démarrage, avant de décider quoi que ce soit.

- **Le modèle :** `Data.MODEL` est une source (`Account`, `SavingsAccount`, `Exportable`, `Loan`).
  - Compile-la : `Javac.compile(Map.of("Model", …))`, avec `{{PKG}}` remplacé par ton paquet.
  - Charge chaque classe de `Data.CLASSES` : `Javac.load(résultat, "Account")` rend un `Class<?>`.
  - Les annotations du modèle **sont les tiennes** : `getAnnotation(Column.class)` marche directement sur ces classes.

**Règle d'or de ce projet :** l'ordre de `getDeclaredFields()`, `getDeclaredMethods()` et `getAnnotations()` **n'est pas garanti** par la spécification. **Trie toujours** avant d'afficher.

---

## Tableau de bord

### ☐ Étape 1 — Les annotations (rappel des projets 1 à 3)

Toutes en `RUNTIME`, sauf `Trace`.

| Annotation | Éléments | Cibles | Particularité |
|---|---|---|---|
| `Audit` | — | `TYPE` | **`@Inherited`** |
| `Tag` | `value` (texte) | `TYPE`, `METHOD` | répétable, conteneur `Tags` (**pas** `@Inherited`) |
| `Tags` | conteneur | `TYPE`, `METHOD` | |
| `Column` | `name` (texte, obligatoire), `nullable` (booléen, défaut `true`) | `FIELD` | |
| `Sensitive` | — | `FIELD`, `PARAMETER` | |
| `Trace` | — | `METHOD` | **sans `@Retention`** |
| `NonEmpty` | — | `TYPE_USE` | |
| `Range` | `min` (entier, défaut `0`), `max` (entier, obligatoire) | `TYPE_USE` | |

### ☐ Étape 2 — Les annotations d'une classe (0.4.8 S1–S3)

Pour chaque classe, dans l'ordre de `Data.CLASSES` :
```
CLASSE Account : declarees [Audit, Tag] ; visibles [Audit, Tag]
CLASSE SavingsAccount : declarees [] ; visibles [Audit]
```
- **declarees** : posées **sur cette classe**.
- **visibles** : posées sur cette classe, plus celles héritées grâce à `@Inherited`.
- Affiche les noms simples, triés, au format `[a, b]`.
  - Le type d'une annotation s'obtient avec `annotationType()`, pas avec `getClass()`.
  - **Expérience :** affiche `getClass()` d'une annotation, puis retire la ligne. Qu'est-ce que c'est ?
- **Questions :**
  - Pourquoi `SavingsAccount` voit-il `Audit` mais pas `Tag` ?
  - Pourquoi `Loan` ne voit-il **pas** les tags de l'interface `Exportable`, qu'il implémente ?

### ☐ Étape 3 — Les répétables (0.4.8 S4)

```
TAGS Account : par type [core] ; getAnnotation(Tag) core ; conteneur absent
TAGS Exportable : par type [x, y] ; getAnnotation(Tag) absent ; conteneur 2 tag(s)
```
- **par type** : toutes les valeurs de `Tag` visibles, conteneur déballé. Une seule méthode fait ça.
- **getAnnotation(Tag)** : la valeur de l'annotation rendue, ou `absent`.
- **conteneur** : le `Tags` **déclaré sur la classe**, avec son nombre de tags.
- **Question :** pourquoi `getAnnotation(Tag.class)` rend-il `null` sur `Exportable`, qui porte pourtant deux `@Tag` ? Relis ce que javac a fait des deux `@Tag` au projet 3.

### ☐ Étape 4 — Les champs (0.4.8 S2, S5)

Les champs déclarés **dans cette classe**, triés par nom :
```
CHAMP Account.owner : String ; @Column(name=owner, nullable=true) @Sensitive
CHAMP Loan.limits : Map<@NonEmpty String, @Range(0..10) Integer> ; aucune
```
- **La partie de gauche** est le type du champ **avec ses annotations de type** (étape 6).
- **La partie de droite** donne les annotations de **déclaration** :
  - d'abord `@Column(name=…, nullable=…)`, avec ses deux valeurs **lues** dans l'annotation, défaut compris ;
  - puis `@Sensitive` ;
  - `aucune` s'il n'y en a pas.
- **Question :** `nullable=true` sur `owner`, alors que personne ne l'a écrit. D'où vient cette valeur ?

### ☐ Étape 5 — Les méthodes, leurs paramètres de type et leurs paramètres (0.4.8 S2)

- Méthodes déclarées, **sans les méthodes synthétiques**, triées par nom.
```
METHODE Account.describe : retour String ; visibles [Tags] ; tags [api, v2]
```
- Pour chaque **paramètre de type** de la méthode, le nom puis ses bornes annotées, jointes par ` & ` :
```
TYPEPARAM Loan.pick : T extends @NonEmpty CharSequence
```
- Pour chaque **paramètre**, son index, puis son nom tel que la reflection le donne :
  - si le vrai nom n'est **pas** dans le `.class`, ajoute ` (nom absent)` ;
  - ensuite, son type annoté, puis ses annotations de **déclaration** (triées, entre crochets).
```
PARAM Account.describe#0 arg0 (nom absent) : String ; declarees [Sensitive]
PARAM Loan.pick#0 arg0 (nom absent) : @NonEmpty T ; declarees []
```
- **Question :** pourquoi `arg0` et pas `prefix` ? Quelle option de javac garderait les vrais noms ?

### ☐ Étape 6 — Le type annoté, reconstruit récursivement (0.4.8 S5–S6)

Écris **une** méthode récursive qui transforme un `AnnotatedType` en texte :

| Sorte | Rendu |
|---|---|
| paramétré (`AnnotatedParameterizedType`) | annotations + nom simple du type brut + `<` + arguments rendus, séparés par `, ` + `>` |
| tableau (`AnnotatedArrayType`) | le composant rendu, puis les annotations **du tableau lui-même** (précédées d'un espace, s'il y en a), puis `[]` |
| joker (`AnnotatedWildcardType`) | `?` tout seul si la borne haute est `Object` sans annotation ; `? extends X` ; `? super X` s'il y a une borne basse |
| variable de type (`AnnotatedTypeVariable`) | annotations + nom de la variable |
| autre | annotations + nom simple de la classe |

- **Les annotations s'écrivent devant**, chacune suivie d'un espace :
  - `@Range(min..max)` pour `Range`, avec les deux valeurs lues, défaut compris ;
  - `@Nom` pour les autres.
- **Exemple :** `@NonEmpty String @Range(1..3) []`. Le composant est `@NonEmpty String`, et le tableau lui-même porte `@Range(1..3)`.
- **Questions :**
  - Pour un type paramétré, `getType()` rend un `ParameterizedType` : quelle méthode donne le type brut (`List`, `Map`) ?
  - Pourquoi la récursion est-elle naturelle ici ? Dessine l'arbre de `Map<@NonEmpty String, @Range(0..10) Integer>`.

### ☐ Étape 7 — La rétention, vue de l'exécution (0.4.5 revu en 0.4.8)

```
RETENTION : Trace sur close visible a l'execution ? false ; dans le .class ? true
```
- **La 1re valeur** vient de la reflection : `Trace` est-il présent sur la méthode `close` d'`Account` ?
- **La 2e valeur :** le texte de `Javac.javap(résultat, "Account")` contient-il `Trace` ?

### ☐ Étape 8 — Le `main` d'`Inspector`

Pour chaque classe de `Data.CLASSES`, affiche dans l'ordre :
1. `CLASSE` ;
2. `TAGS` ;
3. les `CHAMP` ;
4. pour chaque méthode, sa ligne `METHODE`, puis ses `TYPEPARAM` et ses `PARAM`.

Termine par la ligne `RETENTION`.

---

## Checklist API (vérifiée par `Check`)

| Élément | Étape | ☐ |
|---|---|---|
| `Javac.compile`, `Javac.load`, `Javac.javap` | intro, 7 | ☐ |
| `getDeclaredAnnotations()` / `getAnnotations()`, `annotationType()` | 2 | ☐ |
| `getAnnotation(…)`, `getDeclaredAnnotation(…)`, `getAnnotationsByType(…)` | 3 | ☐ |
| `getDeclaredFields()`, `isAnnotationPresent(…)`, `getAnnotatedType()` | 4 | ☐ |
| `getDeclaredMethods()`, `isSynthetic()`, `getAnnotatedReturnType()`, `getTypeParameters()`, `getAnnotatedBounds()` | 5 | ☐ |
| `getParameters()`, `isNamePresent()` | 5 | ☐ |
| `AnnotatedParameterizedType` + `getAnnotatedActualTypeArguments()` | 6 | ☐ |
| `AnnotatedArrayType` + `getAnnotatedGenericComponentType()` | 6 | ☐ |
| `AnnotatedWildcardType` + `getAnnotatedUpperBounds()` / `getAnnotatedLowerBounds()` | 6 | ☐ |
| `AnnotatedTypeVariable` | 6 | ☐ |
| un tri (`Comparator`) | tout | ☐ |
| ~~`invoke`~~, ~~`setAccessible`~~, ~~`newInstance`~~, ~~`getGeneric…`~~ | **interdits** (projets 5 à 7) | — |

---

## Sortie attendue complète

C'est le contrat exact que vérifie `Check` :

```
a remplir
```
