# Drill de rappel 5 — La reflection sur les génériques (0.4.11)

> Première fois ? Lis d'abord [`PARCOURS.md`](../../PARCOURS.md) et les règles des drills [`drills/README.md`](../README.md).

**Chrono cible :** 20 min la 1re fois, puis 10 min aux répétitions.

**Règles :**
- De mémoire.
- Crée **`Recall05`**. Ici, `getTypeName()` est **permis** (c'est un drill d'API).
- Déclare en types imbriqués `static` :
  - **`Holder<T extends Number & Comparable<T>>`**, avec ces champs :
    - `List<String> words` ;
    - `List<Integer> counts` ;
    - `Map<String, List<Integer>> index` ;
    - `List<? extends Number> numbers` ;
    - `List<? super Integer> sinks` ;
    - `List<String>[] pages` ;
    - `Map.Entry<String, Integer> entry` ;
    - `T value`.
  - Et la méthode `@SafeVarargs private <E extends CharSequence> List<E> pick(Map<String, ? super E> source, E... extra) throws IllegalStateException`, qui rend `null`.
  - **`abstract TypeRef<X>`**, avec une méthode qui rend l'argument de type de **sa propre** super-classe générique.
  - **`IntSupplier implements Supplier<Integer>`**.
- Une ligne par défi, préfixée `Dxx : `.

## Défis

- ☐ **D01.** Le champ `words` : le nom simple de son type **effacé**, puis le nom de son type **générique**.
  → `D01 : List java.util.List<java.lang.String>`
- ☐ **D02.** Le type générique de `index` : son type brut (nom simple), son nombre d'arguments, et son 2e argument est-il lui-même paramétré ?
  → `D02 : Map 2 true`
- ☐ **D03.** Le type générique de `entry` : son type **propriétaire**, puis son type brut, en noms simples.
  → `D03 : Map Entry`
- ☐ **D04.** Les jokers de `numbers` et `sinks`. Pour `numbers` : sa borne haute, puis son nombre de bornes basses. Pour `sinks` : sa borne haute, puis sa borne basse.
  → `D04 : java.lang.Number 0 java.lang.Object java.lang.Integer`
- ☐ **D05.** Le type générique de `pages` est-il un `GenericArrayType` ? Quel est son composant ?
  → `D05 : true java.util.List<java.lang.String>`
- ☐ **D06.** Le type générique de `value` est une variable de type. Affiche :
  - son nom ;
  - son nombre de bornes ;
  - qui la déclare (nom simple) ;
  - puis le type **effacé** du champ.
  → `D06 : T 2 Holder Number`
- ☐ **D07.** `words` et `counts` ont-ils le même `getType()` ? Le même `getGenericType()` ?
  → `D07 : true false`
- ☐ **D08.** `pick` : le nom de son paramètre de type, son retour générique, puis ses paramètres génériques, séparés par un espace.
  → `D08 : E java.util.List<E> java.util.Map<java.lang.String, ? super E> E[]`
- ☐ **D09.** `pick` : sa 1re exception générique, puis les noms simples de ses paramètres **effacés**.
  → `D09 : java.lang.IllegalStateException Map CharSequence[]`
- ☐ **D10.** Un **jeton de type** : une sous-classe **anonyme** de `TypeRef<Map<String, Integer>>`. Affiche l'argument qu'elle a capturé.
  → `D10 : java.util.Map<java.lang.String, java.lang.Integer>`
- ☐ **D11.** La 1re interface générique implémentée par `IntSupplier`.
  → `D11 : java.util.function.Supplier<java.lang.Integer>`
- ☐ **D12.** Le nombre de paramètres de type de `Holder`, le nom du paramètre de type de `List`, puis celui du 2e paramètre de type de `Map`.
  → `D12 : 1 E V`

**Expérience** (hors sortie attendue) : pourquoi `new TypeRef<…>() {}` capture-t-il le type alors que `new ArrayList<String>()` ne le peut pas ? Où l'information est-elle écrite ?

## Sortie attendue complète

```
D01 : List java.util.List<java.lang.String>
D02 : Map 2 true
D03 : Map Entry
D04 : java.lang.Number 0 java.lang.Object java.lang.Integer
D05 : true java.util.List<java.lang.String>
D06 : T 2 Holder Number
D07 : true false
D08 : E java.util.List<E> java.util.Map<java.lang.String, ? super E> E[]
D09 : java.lang.IllegalStateException Map CharSequence[]
D10 : java.util.Map<java.lang.String, java.lang.Integer>
D11 : java.util.function.Supplier<java.lang.Integer>
D12 : 1 E V
```

## Carte mémoire (à lire **après** le drill)

<details><summary>Ouvrir la carte</summary>

| Sorte de `Type` | Méthodes |
|---|---|
| `Class<?>` | un type sans argument (ou effacé) ; `getTypeParameters()` |
| `ParameterizedType` | `getRawType()`, `getActualTypeArguments()`, `getOwnerType()` (`Map` pour `Map.Entry<…>`) |
| `TypeVariable<D>` | `getName()`, `getBounds()`, `getGenericDeclaration()` |
| `WildcardType` | `getUpperBounds()` (`Object` par défaut), `getLowerBounds()` (vide sauf `? super`) |
| `GenericArrayType` | `getGenericComponentType()` |

| Où lire le type générique | Méthode |
|---|---|
| champ | `getGenericType()` (effacé : `getType()`) |
| méthode | `getGenericReturnType()`, `getGenericParameterTypes()`, `getGenericExceptionTypes()`, `getTypeParameters()` |
| classe | `getGenericSuperclass()`, `getGenericInterfaces()` |

- **L'effacement :** `List<String>` et `List<Integer>` ont la même `Class`. Une variable de type s'efface en sa **première borne**.
- **Le jeton de type :** une sous-classe garde dans son `.class` les arguments de sa super-classe générique.

</details>
