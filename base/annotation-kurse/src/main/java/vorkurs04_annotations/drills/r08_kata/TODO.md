# Drill de rappel 8 — Kata mixte chronométré (tout le chapitre 0.4)

> Première fois ? Lis d'abord [`PARCOURS.md`](../../PARCOURS.md) et les règles des drills [`drills/README.md`](../README.md).

**Chrono cible :** 30 min, **sans carte mémoire**. C'est le test final de chaque cycle de révision.

**Règles :**
- Tout se fait de mémoire.
- Crée **`Recall08`**. Tout ce qui suit y est imbriqué.
- **Les annotations :**
  - `Role(String value)` : visible à l'exécution, sur les types et les méthodes, **répétable** (son conteneur s'appelle `Roles`) ;
  - `Limit(int max, String unit, "x" par défaut)` : visible à l'exécution, sur les champs ;
  - `Audited` : visible à l'exécution, **héritée** par les sous-classes ;
  - `Internal` : sans aucune méta-annotation.
- **Le modèle :**
  - un record `Pair<A, B>(A left, B right)` ;
  - une classe `Service` marquée `@Audited @Internal @Role("admin") @Role("ops")`, avec :
    - `@Limit(max = 3) private int retries = 1` ;
    - `@Limit(max = 100, unit = "ms") int timeout = 50` ;
    - `Pair<String, List<Integer>> last` ;
    - `@Role("ops") public String restart(String why)`, qui rend `"restart:" + why` ;
    - `public String status()`, qui rend `"up"` ;
  - une classe `Child extends Service`, qui redéfinit `restart` (**sans** annotation) pour rendre `"child:" + why`.
- **Interdit :** `setAccessible` (tes classes sont imbriquées : as-tu besoin de forcer ?).
- Les listes de champs et de méthodes sont **triées par nom**.
- Une ligne par défi, préfixée `Dxx : `.

## Défis

- ☐ **D01.** Compile avec `Javac` deux sources fautives. Affiche les codes de leurs **erreurs**, séparés par ` | ` :
  - `@interface Bad { Object value(); }` ;
  - une annotation `M` ciblée sur les méthodes, posée sur une classe `X`.
  → `D01 : compiler.err.invalid.annotation.member.type | compiler.err.annotation.type.not.applicable`
- ☐ **D02.** Sur `Service` : le nombre de `Role` ; `getAnnotation(Role.class)` est-il `null` ? Puis les valeurs, jointes par `,`.
  → `D02 : 2 true admin,ops`
- ☐ **D03.** Sur `Child` : `Audited` est-il présent ? Combien d'annotations **déclarées** ? `Roles` est-il présent ?
  → `D03 : true 0 false`
- ☐ **D04.** Les champs `@Limit` de `Service`, au format `nom<=max unité`, sans espace avant l'unité.
  → `D04 : retries<=3x timeout<=100ms`
- ☐ **D05.** `Internal` est-il présent sur `Service` ? Puis la valeur par défaut de `unit`, lue **sur l'annotation** elle-même.
  → `D05 : false x`
- ☐ **D06.** Sur un nouveau `Service`, invoque chaque méthode déclarée portant `Role`, avec `"panne"`. Affiche `nom résultat`.
  → `D06 : restart restart:panne`
- ☐ **D07.** Sur ce même `Service`, écris `5` dans `retries` par reflection. Puis valide chaque champ `@Limit` : `nom valeur ok` si la valeur est au plus `max`, sinon `nom valeur trop` ; les champs sont séparés par ` | `.
  → `D07 : retries 5 trop | timeout 50 ok`
- ☐ **D08.** Les arguments de type du champ `last`, puis les composants de `Pair` au format `nom:type générique`.
  → `D08 : java.lang.String java.util.List<java.lang.Integer> | left:A right:B`
- ☐ **D09.** Le `Role` de `restart` est-il égal au 2e `Role` de `Service` ? Le nom simple de son type d'annotation.
  → `D09 : true Role`
- ☐ **D10.** Invoque la méthode `restart` **de `Service`** sur un `Child`, avec `"x"`. Puis : le `restart` de `Child` porte-t-il `Role` ?
  → `D10 : child:x false`

## Sortie attendue complète

```
D01 : compiler.err.invalid.annotation.member.type | compiler.err.annotation.type.not.applicable
D02 : 2 true admin,ops
D03 : true 0 false
D04 : retries<=3x timeout<=100ms
D05 : false x
D06 : restart restart:panne
D07 : retries 5 trop | timeout 50 ok
D08 : java.lang.String java.util.List<java.lang.Integer> | left:A right:B
D09 : true Role
D10 : child:x false
```

## Après le kata

- **Pour chaque défi raté ou lent (plus de 3 min) :** relis la carte du drill correspondant, puis refais **ce drill** le lendemain.

  | Défi | Drill |
  |---|---|
  | D01 | r01 |
  | D02, D03, D05 | r02 |
  | D04, D09 | r03 |
  | D06, D10 | r04 |
  | D08 | r05 |
  | D07 | r06 |

- **Note ton temps** dans le tableau de suivi de `drills/README.md`.
