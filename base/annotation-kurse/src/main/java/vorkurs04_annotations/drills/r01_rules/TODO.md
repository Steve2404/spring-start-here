# Drill de rappel 1 — Les règles de déclaration et les annotations connues (0.4.1 → 0.4.3)

> Première fois ? Lis d'abord le mode d'emploi [`vorkurs04_annotations/PARCOURS.md`](../../PARCOURS.md) et les règles des drills [`drills/README.md`](../README.md).

**Chrono cible :** 20 min la 1re fois, puis 10 min aux répétitions.

**Règles :**
- Tout se fait **de mémoire** : ni solution, ni cours, ni carte mémoire avant d'avoir fini.
- Crée toi-même, dans ce paquet, la classe **`Recall01`** et son `main`.
- **Chaque défi est une petite source que TU écris** (un `String`). Fais-la compiler par `Javac.compile(Map.of("D", source))`, puis affiche :
  - le **code** du premier diagnostic ;
  - ou `OK` s'il n'y en a aucun.
- Quand un défi demande plusieurs sources, affiche leurs verdicts dans l'ordre, séparés par un espace (D12 : par ` | `).
- Écris **une ligne par défi**, préfixée `Dxx : `. `Check` compare au caractère près.

## Défis

- ☐ **D01.** Une annotation avec un élément texte **sans valeur par défaut**, posée sur une classe sans lui donner de valeur.
  → `D01 : compiler.err.annotation.missing.default.value`
- ☐ **D02.** Une annotation dont l'élément est de type `Object`.
  → `D02 : compiler.err.invalid.annotation.member.type`
- ☐ **D03.** Une annotation dont l'élément prend un paramètre `int`.
  → `D03 : compiler.err.intf.annotation.members.cant.have.params`
- ☐ **D04.** Un élément texte dont la valeur par défaut est `null`.
  → `D04 : compiler.err.attribute.value.must.be.constant`
- ☐ **D05.** Une annotation qui `extends java.lang.annotation.Annotation`.
  → `D05 : compiler.err.cant.extend.intf.annotation`
- ☐ **D06.** Une annotation dont un élément a le type de cette **même** annotation.
  → `D06 : compiler.err.cyclic.annotation.element`
- ☐ **D07.** Deux sources. Une annotation avec `value` (texte) et un deuxième élément `n` (entier), utilisée avec le raccourci `@A("x")` :
  - d'abord avec `n` **sans** défaut ;
  - puis avec `n` défaut `0`.
  → `D07 : compiler.err.annotation.missing.default.value OK`
- ☐ **D08.** Deux sources :
  - un élément `String[]` qui reçoit une **seule** chaîne, sans accolades ;
  - un élément texte qui reçoit un champ `static` **non `final`**.
  → `D08 : OK compiler.err.attribute.value.must.be.constant`
- ☐ **D09.** Deux sources :
  - `@Override` sur une méthode `tostring()` (faute de frappe) ;
  - `@Override` sur une méthode `static` qui en masque une autre.
  → `D09 : compiler.err.method.does.not.override.superclass compiler.err.static.methods.cannot.be.annotated.with.override`
- ☐ **D10.** Deux sources :
  - une `@FunctionalInterface` avec une méthode abstraite, une méthode `default` et `boolean equals(Object o)` ;
  - une `@FunctionalInterface` avec deux méthodes abstraites.
  → `D10 : OK compiler.err.bad.functional.intf.anno.1`
- ☐ **D11.** `@SafeVarargs` sur une méthode générique varargs, en trois versions :
  - d'instance, ni `final` ni `private` ;
  - `final` ;
  - `private`.
  → `D11 : compiler.err.varargs.invalid.trustme.anno OK OK`
- ☐ **D12.** Une méthode `@Deprecated(since = "2", forRemoval = true)`, appelée depuis une autre classe, en trois versions de l'appelant. Affiche **tous** les codes de chaque version, et sépare les versions par ` | ` :
  - sans rien ;
  - avec `@SuppressWarnings("deprecation")` ;
  - avec `@SuppressWarnings("removal")`.
  → `D12 : compiler.warn.has.been.deprecated.for.removal | compiler.warn.has.been.deprecated.for.removal | OK`
- ☐ **D13.** Une annotation non répétable écrite deux fois sur la même classe.
  → `D13 : compiler.err.duplicate.annotation.missing.container`
- ☐ **D14.** Une annotation **marqueur** utilisée avec une valeur : `@M("x")`.
  → `D14 : compiler.err.cant.resolve.location.args`

**Expérience** (hors sortie attendue) : pour D04, essaie aussi `@A(id = null)` à l'usage. Même code ?

## Sortie attendue complète

```
D01 : compiler.err.annotation.missing.default.value
D02 : compiler.err.invalid.annotation.member.type
D03 : compiler.err.intf.annotation.members.cant.have.params
D04 : compiler.err.attribute.value.must.be.constant
D05 : compiler.err.cant.extend.intf.annotation
D06 : compiler.err.cyclic.annotation.element
D07 : compiler.err.annotation.missing.default.value OK
D08 : OK compiler.err.attribute.value.must.be.constant
D09 : compiler.err.method.does.not.override.superclass compiler.err.static.methods.cannot.be.annotated.with.override
D10 : OK compiler.err.bad.functional.intf.anno.1
D11 : compiler.err.varargs.invalid.trustme.anno OK OK
D12 : compiler.warn.has.been.deprecated.for.removal | compiler.warn.has.been.deprecated.for.removal | OK
D13 : compiler.err.duplicate.annotation.missing.container
D14 : compiler.err.cant.resolve.location.args
```

## Carte mémoire (à lire **après** le drill)

<details><summary>Ouvrir la carte</summary>

| Règle | Code javac |
|---|---|
| élément sans défaut oublié | `annotation.missing.default.value` |
| types d'éléments permis : primitifs, `String`, `Class<…>`, enum, annotation, et tableaux de ceux-ci | sinon `invalid.annotation.member.type` |
| pas de paramètre, pas de `throws`, pas de `extends` | `intf.annotation.members.cant.have.params`, `cant.extend.intf.annotation` |
| valeur = **constante de compilation** (jamais `null`) | `attribute.value.must.be.constant` |
| une annotation ne se contient pas elle-même | `cyclic.annotation.element` |
| `@A("x")` = `@A(value = "x")`, seulement si les autres éléments ont un défaut | |
| tableau : `tags = "x"` équivaut à `tags = {"x"}` | |
| `@Override` : redéfinition réelle, jamais sur `static` | `method.does.not.override.superclass`, `static.methods.cannot.be.annotated.with.override` |
| `@FunctionalInterface` : exactement une méthode abstraite (ni `default`, ni `static`, ni méthode d'`Object`) | `bad.functional.intf.anno.1` |
| `@SafeVarargs` : `static`, `final`, `private`, ou constructeur | `varargs.invalid.trustme.anno` |
| `@Deprecated(since, forRemoval)` ; `forRemoval` se tait avec `"removal"` seulement | `warn.has.been.deprecated(.for.removal)` |
| une annotation répétée sans `@Repeatable` | `duplicate.annotation.missing.container` |

Préfixes : `compiler.err.` pour une erreur, `compiler.warn.` pour un avertissement.
</details>
