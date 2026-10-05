# Projet 6 — Le contrôle de schéma d'import (cours 0.4.11)

> Première fois ? Lis d'abord le mode d'emploi [`vorkurs04_annotations/PARCOURS.md`](../../PARCOURS.md).

**Notions visées (0.4.11) :**
- `Class<?>`, `Type` et `AnnotatedType` : trois vues différentes d'un même type ;
- `ParameterizedType` : type brut, arguments, type propriétaire ;
- `TypeVariable` : bornes, `getGenericDeclaration` ;
- `WildcardType` : `?`, `? extends`, `? super` ;
- `GenericArrayType` ;
- `AnnotatedType`, l'arbre **parallèle** ;
- les signatures génériques de `Field`, `Method` et `Class` ;
- l'effacement des types (type erasure).

**Ce qui est donné :** `Data.java` (des classes cibles **sous forme de source**, plus des lignes à importer), `Check.java`, `projectkit.Javac` (`compile`, `load`).  
**Ce que TU crées :** tout le reste, dans `vorkurs04_annotations.projects.p06_schema`. Noms imposés : les annotations **`Positive`** et **`NotBlank`**, toutes deux annotations de **type**, lisibles à l'exécution ; la classe `main` **`SchemaCheck`**.

**Pour vérifier :** lance `Check.java` depuis `base/annotation-kurse`.

---

## Le problème

Un import de données remplit des objets à partir de lignes de texte. Avant de démarrer, il doit vérifier que **chaque champ** a un type qu'il sait remplir. Pour ça, `getType()` ne suffit pas : il ne voit que `List` ou `Map`. Il faut le type **générique complet** : `List<List<Integer>>`, `Map<String, Double>`… C'est le « contrôle avant démarrage » des frameworks.

- **Les classes :** compile les sources de `Data.SCHEMAS` **ensemble** (avec `{{PKG}}` remplacé), puis charge chaque classe dans l'ordre de la `Map`.
- **Les champs :** tous publics. Utilise `getFields()`, qui inclut les champs **hérités** (`IntBox` hérite de `value` et `history`). Trie-les **par nom**.

---

## Tableau de bord

### ☐ Étape 1 — Décrire un type générique, récursivement (0.4.11 S1–S5)

Écris une méthode récursive `Type` → texte, **sans** `getTypeName()` ni `toString()` (`Check` les refuse) :

| Sorte de `Type` | Rendu |
|---|---|
| `Class` | nom simple ; un tableau donne le composant rendu suivi de `[]` |
| `ParameterizedType` | type propriétaire rendu suivi de `.` (s'il existe), nom simple du type brut, `<` arguments séparés par `, ` `>` |
| `TypeVariable` | son nom (`T`), ou ce qu'elle vaut si elle est résolue (étape 3) |
| `WildcardType` | `?`, `? extends X` ou `? super X` (borne basse s'il y en a une ; borne haute `Object` → `?` seul) |
| `GenericArrayType` | composant rendu suivi de `[]` |

**Question :** il n'existe que ces cinq sortes de `Type`. Pourquoi `List<String>[]` est-il un `GenericArrayType` et pas un `Class` ?

### ☐ Étape 2 — L'arbre annoté, en parallèle (0.4.11 S6)

Même rendu, mais à partir de `Field.getAnnotatedType()`. Chaque nœud porte ses annotations de type, écrites `@Nom ` devant. Les deux arbres ont **la même forme**, avec les annotations en plus.
```
SCHEMA Order.quantities : List<@Positive Integer>
```

### ☐ Étape 3 — Ce que l'import accepte (0.4.11 S2–S5, S7)

Parcours l'arbre du type générique et donne la **première** raison de refus rencontrée :

| Rencontré | Raison |
|---|---|
| un joker | `joker` |
| un `GenericArrayType` | `tableau generique` |
| un `ParameterizedType` avec un type propriétaire | `type imbrique` |
| un `Class` qui a des paramètres de type mais n'en reçoit aucun (`List` tout court) | `type brut` |
| une variable de type non résolue | `variable T de Box non resolue (borne Comparable<T>)` (qui la déclare, et sa première borne) |

- **Les variables résolues :** une classe qui hérite d'une super-classe **paramétrée** (`IntBox extends Box<@Positive Integer>`) fixe les variables de cette super-classe.
  - **Associe** chaque variable de type de la super-classe brute à l'argument **annoté** correspondant de la super-classe générique annotée.
  - Ensuite, `T` se rend et s'importe comme `@Positive Integer`.
- **L'affichage :** un champ accepté donne une ligne `SCHEMA` (rendu annoté) ; un refusé donne une ligne `REJET` (raison, puis rendu **non annoté**) :
```
REJET Broken.first : type imbrique dans Map.Entry<String, Integer>
REJET Box.value : variable T de Box non resolue (borne Comparable<T>) dans T
SCHEMA IntBox.value : @Positive Integer
```
- Une classe avec au moins un `REJET` est **invalide** pour l'import.
- **Question :** pourquoi `Box` est-il refusé alors qu'`IntBox`, avec les **mêmes** champs, est accepté ?

### ☐ Étape 4 — Importer les lignes (0.4.11 S6)

**Le format des lignes de `Data.ROWS` :**
- `Classe | champ=valeur ; champ=valeur …` ;
- coupe sur ` | `, puis sur ` ; `, puis sur le **premier** `=` ;
- **ne retire pas** les espaces d'une valeur.

**Le format d'une valeur, selon le type du champ :**

| Type | Texte | Valeur |
|---|---|---|
| `String` | tel quel | le texte |
| `Integer` / `Double` | un nombre | `parseInt` / `parseDouble` |
| `List<X>` | éléments séparés par `,`, ou par `/` si `X` est lui-même une liste ; texte vide → liste vide | une `List` |
| `Map<String, X>` | `cle:valeur` séparés par `,` ; texte vide → map vide | une `Map` qui garde l'ordre |
| `String[]` | séparés par `,` | un tableau, affiché avec `Arrays.toString` |
| `Optional<X>` | texte vide → vide, sinon la valeur | un `Optional` |

**La validation :**
- Un nœud **annoté** `@Positive` doit valoir plus que 0 : `<chemin> = <valeur> doit etre > 0`.
- Un nœud annoté `@NotBlank` ne doit pas être blanc : `<chemin> ne doit pas etre vide`.
- **Le chemin :** le nom du champ, plus `[i]` pour un élément de liste, ou `.cle` pour une valeur de map.

**Affiche une ligne par ligne importée** (numérotée à partir de 1) :
- tous les champs **triés par nom**, au format `champ=valeur`, séparés par un espace ; un champ absent de la ligne s'écrit `champ=absent` ;
- s'il y a des erreurs, `REFUS` suivi des erreurs, séparées par ` ; ` ;
- pour une classe invalide, `REFUS schema invalide`.
```
IMPORT ligne 1 Order : id=A1 matrix=[[1, 2], [3]] note=Optional[urgent] prices={pen=1.5, ink=2.0} quantities=[1, 2, 3] tags=[x, y]
IMPORT ligne 2 Order : REFUS note ne doit pas etre vide ; quantities[1] = 0 doit etre > 0
```
**Question :** l'annotation `@Positive` de `quantities` n'est **pas** sur le champ, ni sur `List`. Où est-elle exactement, et comment ton code la trouve-t-il ?

### ☐ Étape 5 — Une signature générique de méthode (0.4.11 S7)

La méthode `top` de `Ranking` : reconstruis sa signature avec :
- ses paramètres de type et leur première borne ;
- son type de retour générique ;
- ses paramètres génériques, le dernier s'écrivant `E...` si la méthode est varargs ;
- ses exceptions génériques.
```
SIGNATURE <E extends Comparable<E>> List<E> top(Map<String, ? super E>, int, E...) throws IllegalStateException
```

### ☐ Étape 6 — L'effacement des types (0.4.11 S7)

```
EFFACEMENT words/counts : meme getType ? true ; meme getGenericType ? false
EFFACEMENT top : [Map, int, Comparable[]]
```
- **La 1re ligne :** compare les champs `words` (`List<String>`) et `counts` (`List<Integer>`) de `Ranking`, d'abord avec `getType()`, puis avec `getGenericType()`.
- **La 2e ligne :** `getParameterTypes()` de `top` (les types **effacés**), en noms simples.
- **Question :** pourquoi `E...` s'efface-t-il en `Comparable[]` et pas en `Object[]` ?

### ☐ Étape 7 — Le `main` de `SchemaCheck`

Il affiche, dans l'ordre :
1. les lignes `SCHEMA` / `REJET` (classe après classe) ;
2. les lignes `IMPORT` ;
3. la ligne `SIGNATURE` ;
4. les 2 lignes `EFFACEMENT`.

---

## Checklist API (vérifiée par `Check`)

| Élément | Étape | ☐ |
|---|---|---|
| `getFields()`, `getType()`, `getGenericType()`, `getAnnotatedType()` | 1, 2, 6 | ☐ |
| `ParameterizedType` : `getRawType()`, `getActualTypeArguments()`, `getOwnerType()` | 1, 3 | ☐ |
| `TypeVariable` : `getBounds()`, `getGenericDeclaration()` | 3 | ☐ |
| `WildcardType` : `getUpperBounds()`, `getLowerBounds()` | 1 | ☐ |
| `GenericArrayType` : `getGenericComponentType()` | 1 | ☐ |
| `getGenericSuperclass()`, `getAnnotatedSuperclass()`, `getTypeParameters()` | 3 | ☐ |
| `AnnotatedParameterizedType`, `getAnnotatedActualTypeArguments()`, `isAnnotationPresent` | 2, 4 | ☐ |
| `getGenericReturnType()`, `getGenericParameterTypes()`, `getGenericExceptionTypes()` | 5 | ☐ |
| `getParameterTypes()` | 6 | ☐ |
| ~~`getTypeName`~~, ~~`invoke`~~, ~~`newInstance`~~, ~~`setAccessible`~~ | **interdits** | — |

---

## Sortie attendue complète

C'est le contrat exact que vérifie `Check` :

```
a remplir
```
