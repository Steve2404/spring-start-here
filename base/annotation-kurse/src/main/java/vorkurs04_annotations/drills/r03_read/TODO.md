# Drill de rappel 3 — Lire les annotations par reflection (0.4.8)

> Première fois ? Lis d'abord [`PARCOURS.md`](../../PARCOURS.md) et les règles des drills [`drills/README.md`](../README.md).

**Chrono cible :** 25 min la 1re fois, puis 12 min aux répétitions.

**Règles :**
- De mémoire.
- Crée la classe **`Recall03`**. Tout s'écrit **dedans**, en types imbriqués : les annotations, les classes et le record.
- Une ligne par défi, préfixée `Dxx : `. Les valeurs sont séparées par un espace.

**À déclarer dans `Recall03` :**
- **`Audit`** : `@Inherited`, `RUNTIME`.
- **`Col(name, nullable = true)`** : `RUNTIME`, sur champs, méthodes et constructeurs.
- **`Tag(value)`** : `RUNTIME`, répétable, avec son conteneur `Tags`.
- **`Param`** : `RUNTIME`, sur paramètres.
- **`Hidden`** : sans `@Retention`.
- **`NN`** : `RUNTIME`, `TYPE_USE`.
- **La classe `Base`**, avec `@Audit @Tag("a") @Tag("b")` :
  - les champs `@Col(name = "id") long id`, `@Col(name = "ref") long ref`, `@NN String @NN [] matrix` et `List<@NN String> names` ;
  - un constructeur sans argument `@Col(name = "ctor")` ;
  - une méthode `@Tag("solo") @Hidden void run(@Param String a, int b)`.
- **`Child extends Base`**, vide.
- **L'interface `@Audit Marked`**, et `Impl implements Marked`.
- **`record Point(@Col(name = "x") int x)`**.

## Défis

- ☐ **D01.** `Audit` est-il présent sur `Base`, puis sur `Child` ? Que rend `getDeclaredAnnotation(Audit.class)` sur `Child` ?
  → `D01 : true true null`
- ☐ **D02.** `Audit` est-il présent sur `Impl` ?
  → `D02 : false`
- ☐ **D03.** Le `name` et le `nullable` du `@Col` du champ `id`.
  → `D03 : id true`
- ☐ **D04.** Le nom simple du **type** de cette annotation, puis : la classe de l'objet annotation est-elle une classe **proxy** ?
  → `D04 : Col true`
- ☐ **D05.** Sur `Base`, trois valeurs :
  - `getAnnotation(Tag.class)` ;
  - le nombre de `Tag` par type ;
  - le nombre de tags dans le conteneur.
  → `D05 : null 2 2`
- ☐ **D06.** Sur `run`, la valeur de son `Tag`, puis son conteneur.
  → `D06 : solo null`
- ☐ **D07.** Le nombre d'annotations du paramètre 0, puis du paramètre 1, de `run`. Puis : le paramètre 0 porte-t-il `Param` ?
  → `D07 : 1 0 true`
- ☐ **D08.** Le `name` du `@Col` du constructeur de `Base`.
  → `D08 : ctor`
- ☐ **D09.** `Hidden` est-il présent sur `run` ? Combien d'annotations `run` montre-t-elle ?
  → `D09 : false 1`
- ☐ **D10.** Le champ `names` : son nombre d'annotations **de déclaration**, puis : son premier argument de type porte-t-il `NN` ?
  → `D10 : 0 true`
- ☐ **D11.** Le champ `matrix` : le nombre d'annotations du **tableau**, puis de son **élément**.
  → `D11 : 1 1`
- ☐ **D12.** Le `@Col` de `id` est-il égal (`equals`) au `@Col` relu sur `id` ? Puis au `@Col` de `ref` ?
  → `D12 : true false`
- ☐ **D13.** Sur `Child` : le nombre d'annotations **visibles**, puis **déclarées**.
  → `D13 : 1 0`
- ☐ **D14.** Le record `Point` : `Col` est-il présent sur le **champ** `x` ? Sur l'**accesseur** `x()` ? Sur le **composant** de record ?
  → `D14 : true true false`

## Sortie attendue complète

```
D01 : true true null
D02 : false
D03 : id true
D04 : Col true
D05 : null 2 2
D06 : solo null
D07 : 1 0 true
D08 : ctor
D09 : false 1
D10 : 0 true
D11 : 1 1
D12 : true false
D13 : 1 0
D14 : true true false
```

## Carte mémoire (à lire **après** le drill)

<details><summary>Ouvrir la carte</summary>

| Méthode (`AnnotatedElement`) | Héritées (`@Inherited`) ? | Répétables déballées ? |
|---|---|---|
| `getAnnotation(T)` / `isAnnotationPresent(T)` | oui | non (rend `null` s'il y a plusieurs `T`) |
| `getAnnotations()` | oui | non (on voit le conteneur) |
| `getAnnotationsByType(T)` | oui | **oui** |
| `getDeclaredAnnotation(T)` / `getDeclaredAnnotations()` | non | non |
| `getDeclaredAnnotationsByType(T)` | non | oui |

- **Paramètres :** `Method.getParameterAnnotations()` donne un `Annotation[][]`, un tableau par paramètre ; `getParameters()[i].getAnnotation(…)` marche aussi.
- **Types annotés :** `Field.getAnnotatedType()`, `Method.getAnnotatedReturnType()`, `Parameter.getAnnotatedType()`.
  - `AnnotatedParameterizedType.getAnnotatedActualTypeArguments()` ;
  - `AnnotatedArrayType.getAnnotatedGenericComponentType()`.
- **Un objet annotation** est un proxy : son vrai type s'obtient avec `annotationType()`, ses éléments se lisent comme des méthodes, `equals` compare les valeurs.
- **`@Inherited`** ne marche que d'une **classe** vers ses sous-classes, jamais via une interface.
- **Un composant de record** reçoit l'annotation seulement si elle cible `RECORD_COMPONENT`. Sinon, elle se propage au champ, à l'accesseur ou au paramètre du constructeur, selon ses cibles.

</details>
