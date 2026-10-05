# Drill de rappel 2 — Cibles, rétention, méta-annotations, annotations de type (0.4.4 → 0.4.7)

> Première fois ? Lis d'abord [`PARCOURS.md`](../../PARCOURS.md) et les règles des drills [`drills/README.md`](../README.md).

**Chrono cible :** 25 min la 1re fois, puis 12 min aux répétitions.

**Règles :**
- De mémoire.
- Crée la classe **`Recall02`** et son `main`.
- Chaque source que tu écris commence par `import java.lang.annotation.*;`.
- Pour un verdict, compile la source avec `Javac.compile(Map.of("D", source))`, puis affiche le code du premier diagnostic ou `OK`.
- Plusieurs sources dans un défi : leurs verdicts sont séparés par un espace.
- Une ligne par défi, préfixée `Dxx : `.

## Défis

- ☐ **D01.** Une annotation **sans `@Target`**. Deux sources :
  - la poser sur une classe, un champ, un paramètre et une variable locale ;
  - la poser sur un paramètre de type `<@N T>`.
  → `D01 : OK compiler.err.annotation.type.not.applicable.to.type`
- ☐ **D02.** Une annotation `@Target(FIELD)` posée sur une classe.
  → `D02 : compiler.err.annotation.type.not.applicable`
- ☐ **D03.** `@Target` qui contient deux fois `FIELD`.
  → `D03 : compiler.err.repeated.annotation.target`
- ☐ **D04.** Une annotation `TYPE_USE`. Deux sources :
  - sur une méthode `void` ;
  - sur une **classe** et sur une méthode qui rend un `String`.
  → `D04 : compiler.err.annotation.type.not.applicable OK`
- ☐ **D05.** Une annotation `@Target(METHOD)` posée sur un **composant de record**.
  → `D05 : OK`
- ☐ **D06.** Deux sources :
  - `@Target(ANNOTATION_TYPE)` posée sur une classe ;
  - `@Target(TYPE)` posée sur une déclaration `@interface`.
  → `D06 : compiler.err.annotation.type.not.applicable OK`
- ☐ **D07.** Trois annotations sur une classe `X` : une `RUNTIME`, une **sans** `@Retention`, une `SOURCE` nommée `S`.
  - Compile sous le nom `"X"`, puis lis `Javac.javap(résultat, "X")`.
  - Affiche : le texte contient-il `RuntimeVisibleAnnotations` ? Contient-il `RuntimeInvisibleAnnotations` ? Une de ses lignes vaut-elle exactement `S`, blancs retirés ?
  → `D07 : true true false`
- ☐ **D08.** Une annotation `RUNTIME` de **variable locale**, posée dans une méthode de `Y`. Le `javap` de `Y` contient-il `RuntimeVisible` ?
  → `D08 : false`
- ☐ **D09.** Un `@Repeatable(Tags.class)` dont le conteneur nomme son élément `items` au lieu de `value`.
  → `D09 : compiler.err.invalid.repeatable.annotation.no.value`
- ☐ **D10.** L'annotation en `RUNTIME`, son conteneur en `CLASS`.
  → `D10 : compiler.err.invalid.repeatable.annotation.retention`
- ☐ **D11.** L'annotation en `TYPE`, son conteneur en `TYPE` et `FIELD`.
  → `D11 : compiler.err.invalid.repeatable.annotation.incompatible.target`
- ☐ **D12.** Deux sources : l'annotation est `@Documented`, puis `@Inherited`, et le conteneur ne l'est pas.
  → `D12 : compiler.err.invalid.repeatable.annotation.not.documented compiler.err.invalid.repeatable.annotation.not.inherited`
- ☐ **D13.** Une annotation `TYPE_USE` `NN`. Deux sources : `@NN java.lang.String s;` puis `java.lang.@NN String s;`.
  → `D13 : compiler.err.cant.type.annotate.scoping.1 OK`
- ☐ **D14.** `@NN` sur une variable `var`, puis `@NN` devant `String.class`.
  → `D14 : compiler.err.annotation.type.not.applicable compiler.err.no.annotations.on.dot.class`
- ☐ **D15.** Une annotation `@Documented` `Pub` et une non documentée `Priv`, toutes deux sur une classe publique `Doc` avec un commentaire javadoc.
  - Passe `Javac.javadocPage(Map.of("Doc", source), "Doc")`.
  - La page contient-elle `@Pub` ? Contient-elle `@Priv` ?
  → `D15 : true false`
- ☐ **D16.** Une annotation publique `P`, `@Target(PACKAGE)`, déclarée dans le paquet `p`. Une déclaration de paquet n'a qu'**un** endroit où être annotée : son propre fichier. Compile donc **plusieurs fichiers** à la fois, par exemple `Map.of("p/P", sourceDeP, "p/package-info", "@P package p;")`, et prends le verdict de l'ensemble. Trois compilations :
  - `P` posée sur le paquet ;
  - `P` posée sur une classe `X` du paquet `p` ;
  - une annotation `@Target(TYPE)` posée sur le paquet.
  → `D16 : OK compiler.err.annotation.type.not.applicable compiler.err.annotation.type.not.applicable`
- ☐ **D17.** Trois sources :
  - une annotation `@Target(RECORD_COMPONENT)` posée sur un composant de record ;
  - la même, posée sur un champ de classe ;
  - une annotation `@Target(MODULE)` posée sur une classe.
  → `D17 : OK compiler.err.annotation.type.not.applicable compiler.err.annotation.type.not.applicable`

## Sortie attendue complète

```
D01 : OK compiler.err.annotation.type.not.applicable.to.type
D02 : compiler.err.annotation.type.not.applicable
D03 : compiler.err.repeated.annotation.target
D04 : compiler.err.annotation.type.not.applicable OK
D05 : OK
D06 : compiler.err.annotation.type.not.applicable OK
D07 : true true false
D08 : false
D09 : compiler.err.invalid.repeatable.annotation.no.value
D10 : compiler.err.invalid.repeatable.annotation.retention
D11 : compiler.err.invalid.repeatable.annotation.incompatible.target
D12 : compiler.err.invalid.repeatable.annotation.not.documented compiler.err.invalid.repeatable.annotation.not.inherited
D13 : compiler.err.cant.type.annotate.scoping.1 OK
D14 : compiler.err.annotation.type.not.applicable compiler.err.no.annotations.on.dot.class
D15 : true false
D16 : OK compiler.err.annotation.type.not.applicable compiler.err.annotation.type.not.applicable
D17 : OK compiler.err.annotation.type.not.applicable compiler.err.annotation.type.not.applicable
```

## Carte mémoire (à lire **après** le drill)

<details><summary>Ouvrir la carte</summary>

| Notion | À retenir |
|---|---|
| sans `@Target` | toutes les **déclarations**, mais ni paramètre de type ni usage de type |
| `TYPE` | classe, interface, enum, record **et** `@interface` |
| `PACKAGE` | seulement dans `package-info.java` (une annotation sans `@Target` y est permise aussi) |
| `MODULE` | seulement dans `module-info.java` |
| `RECORD_COMPONENT` | seulement un composant de record ; lisible par `getRecordComponents()` (drill r03) |
| composant de record | accepte une annotation `FIELD`, `METHOD`, `PARAMETER`, `RECORD_COMPONENT` ou `TYPE_USE` (propagée) |
| `TYPE_USE` | usage d'un type ; aussi sur une déclaration de type ou de paramètre de type ; jamais sur `void`, `var`, `.class` |
| nom qualifié | `java.lang.@A String`, `Map.@A Entry` |
| `SOURCE` | absente du `.class` |
| `CLASS` (défaut) | `RuntimeInvisibleAnnotations` : présente, mais invisible pour la reflection |
| `RUNTIME` | `RuntimeVisible…Annotations` |
| variable locale | jamais dans le `.class` |
| conteneur `@Repeatable` | `value()` qui est un tableau de l'annotation ; rétention ≥ ; cibles ⊆ ; `@Documented` / `@Inherited` si l'annotation l'est ; les autres éléments ont un défaut |
| `@Documented` | javadoc affiche l'annotation |
| `@Inherited` | classe → sous-classe seulement (vu par `getAnnotation`) |

</details>
