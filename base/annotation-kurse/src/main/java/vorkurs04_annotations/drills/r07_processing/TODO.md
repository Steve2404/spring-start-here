# Drill de rappel 7 — L'annotation processing (0.4.13)

> Première fois ? Lis d'abord [`PARCOURS.md`](../../PARCOURS.md) et les règles des drills [`drills/README.md`](../README.md).

**Chrono cible :** 25 min la 1re fois, puis 12 min aux répétitions.

**Règles :**
- De mémoire.
- Crée **`Recall07`**, avec dedans :
  - une annotation imbriquée **`Mark`**, publique, de rétention **`SOURCE`** (un processor la voit quand même : il travaille sur les sources) ;
  - une liste statique **`SEEN`** des observations du processor ;
  - **un seul** processor imbriqué, `Probe extends AbstractProcessor`, construit avec un **mode** (`String`) qui choisit ce qu'il fait.
- **`Probe`** :
  - supporte `Mark` (nom canonique) ;
  - supporte `SourceVersion.latestSupported()`, sauf en mode `"old"` : `SourceVersion.RELEASE_8` ;
  - dans `init`, mémorise l'option `greeting` (lue par `getOptions()`, `null` si absente) ;
  - dans `process`, compte les rounds (à partir de 1) :
    - au dernier round (`processingOver()`), ajoute `fin round N` et ne fait rien d'autre ;
    - sinon, ajoute `round N racines R marques M`, où R = nombre d'éléments racines et M = nombre d'éléments portant `Mark` ; puis applique le mode à **chaque** élément marqué (voir les défis) ;
  - `process` rend **`true`** (« ces annotations sont à moi »), sauf en mode `"unclaimed"`.
- **`run(mode, source, options)`** :
  - vide `SEEN`, puis compile la source (nom `"S"`) avec `Javac.compile(sources, options, probe)` ;
  - rend `succès | observations | codes | option X`, où :
    - les observations sont jointes par ` ; ` (ou `jamais appele` si `SEEN` est vide) ;
    - les **codes** distincts des diagnostics sont joints par un espace (partie omise s'il n'y en a aucun) ;
    - la partie `| option X` est omise si l'option vaut `null`.
- La source de base, nommée ici **ONE** : l'import de `Mark`, puis `@Mark class A { int n; java.util.List<String> names; void m() {} }`.
- Une ligne par défi, préfixée `Dxx : `.

## Défis

- ☐ **D01.** Mode `"count"` (rien de plus) sur ONE. Combien de rounds ?
  → `D01 : true | round 1 racines 1 marques 1 ; fin round 2`
- ☐ **D02.** Mode `"generate"` : pour chaque élément marqué, crée avec le `Filer` la source `<Nom>Gen` (une classe publique vide). Que devient le nombre de rounds ? Que voit le round 2 ?
  → `D02 : true | round 1 racines 1 marques 1 ; round 2 racines 1 marques 0 ; fin round 3`
- ☐ **D03.** Mode `"kinds"` : ajoute `<kind> <nom simple>` par élément marqué. Source : `@Mark class A { @Mark int n; @Mark void m() {} }`.
  → `D03 : true | round 1 racines 1 marques 3 ; CLASS A ; FIELD n ; METHOD m ; fin round 2`
- ☐ **D04.** Mode `"types"` : pour chaque membre de l'élément qui est un `VariableElement`, ajoute `<nom> <son TypeMirror> <kind du TypeMirror>`.
  → `D04 : true | round 1 racines 1 marques 1 ; n int INT ; names java.util.List<java.lang.String> DECLARED ; fin round 2`
- ☐ **D05.** Mode `"members"` : les kinds de **tous** les membres de l'élément, triés, joints par un espace. D'où vient le membre que tu n'as pas écrit ?
  → `D05 : true | round 1 racines 1 marques 1 ; CONSTRUCTOR FIELD FIELD METHOD ; fin round 2`
- ☐ **D06.** Mode `"error"` : le `Messager` signale une **erreur** `"refuse"` sur l'élément. Le processor lève-t-il une exception ? La compilation réussit-elle ?
  → `D06 : false | round 1 racines 1 marques 1 ; fin round 2 | compiler.err.proc.messager`
- ☐ **D07.** Mode `"warn"` : la même chose avec un **avertissement** `"attention"`.
  → `D07 : true | round 1 racines 1 marques 1 ; fin round 2 | compiler.warn.proc.messager`
- ☐ **D08.** Mode `"twice"` : crée **deux fois** la même source `<Nom>Gen`. Attrape `FilerException` (ajoute `FilerException`) avant `IOException`.
  → `D08 : true | round 1 racines 1 marques 1 ; FilerException ; round 2 racines 1 marques 0 ; fin round 3 | compiler.warn.proc.type.recreate`
- ☐ **D09.** Mode `"count"` avec l'option javac `-Agreeting=salut`. Pourquoi javac avertit-il alors que l'option a bien été lue ?
  → `D09 : true | round 1 racines 1 marques 1 ; fin round 2 | compiler.warn.proc.unmatched.processor.options | option salut`
- ☐ **D10.** Mode `"old"` sur ONE.
  → `D10 : true | round 1 racines 1 marques 1 ; fin round 2 | compiler.warn.proc.processor.incompatible.source.version`
- ☐ **D11.** Mode `"count"` sur la source `class B {}` (aucune annotation).
  → `D11 : true | jamais appele`
- ☐ **D12.** Mode `"unclaimed"` sur ONE : `process` rend `false`.
  → `D12 : true | round 1 racines 1 marques 1 ; fin round 2 | compiler.warn.proc.annotations.without.processors`

**Expérience** (hors sortie attendue) : en D09, ajoute `getSupportedOptions()` qui rend `Set.of("greeting")` (ou `@SupportedOptions`). Quel avertissement disparaît ?

## Sortie attendue complète

```
D01 : true | round 1 racines 1 marques 1 ; fin round 2
D02 : true | round 1 racines 1 marques 1 ; round 2 racines 1 marques 0 ; fin round 3
D03 : true | round 1 racines 1 marques 3 ; CLASS A ; FIELD n ; METHOD m ; fin round 2
D04 : true | round 1 racines 1 marques 1 ; n int INT ; names java.util.List<java.lang.String> DECLARED ; fin round 2
D05 : true | round 1 racines 1 marques 1 ; CONSTRUCTOR FIELD FIELD METHOD ; fin round 2
D06 : false | round 1 racines 1 marques 1 ; fin round 2 | compiler.err.proc.messager
D07 : true | round 1 racines 1 marques 1 ; fin round 2 | compiler.warn.proc.messager
D08 : true | round 1 racines 1 marques 1 ; FilerException ; round 2 racines 1 marques 0 ; fin round 3 | compiler.warn.proc.type.recreate
D09 : true | round 1 racines 1 marques 1 ; fin round 2 | compiler.warn.proc.unmatched.processor.options | option salut
D10 : true | round 1 racines 1 marques 1 ; fin round 2 | compiler.warn.proc.processor.incompatible.source.version
D11 : true | jamais appele
D12 : true | round 1 racines 1 marques 1 ; fin round 2 | compiler.warn.proc.annotations.without.processors
```

## Carte mémoire (à lire **après** le drill)

<details><summary>Ouvrir la carte</summary>

| Point | À retenir |
|---|---|
| rounds | 1 round par vague de **nouvelles** sources, puis un round final où `processingOver()` est vrai |
| générer | une source créée par le `Filer` → un round de plus, où elle est la seule racine |
| `RetentionPolicy.SOURCE` | suffit pour un processor : il lit les sources, pas les `.class` |
| `Element` | `getKind()`, `getSimpleName()`, `getEnclosedElements()` (le constructeur par défaut y est) |
| `TypeMirror` | `asType()` ; `toString()` donne le type complet ; `getKind()` : `INT`, `DECLARED`… |
| `Messager` ERROR | pas d'exception : javac échoue **à la fin** (`success` false), le dernier round a lieu |
| `Filer` | recréer le même type → `FilerException` (sous-classe d'`IOException` : attrape-la d'abord) |
| options `-Akey=val` | lues dans `init` par `getOptions()` ; non déclarées (`getSupportedOptions`) → avertissement |
| version | une version supportée trop ancienne → avertissement, pas d'erreur |
| aucune annotation supportée | `process` n'est **jamais** appelé |
| `return true` | réclame les annotations ; `false` les laisse aux suivants (avertissement s'il n'y en a aucun) |

</details>
