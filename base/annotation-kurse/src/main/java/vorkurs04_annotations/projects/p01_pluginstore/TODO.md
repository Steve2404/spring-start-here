# Projet 1 — La boutique de plugins (cours 0.4.1 → 0.4.3)

> Première fois ? Lis d'abord le mode d'emploi [`vorkurs04_annotations/PARCOURS.md`](../../PARCOURS.md) : comment lire cette fiche, lancer `Check`, quoi faire en cas de blocage.

**Notions visées :** métadonnées contre données (0.4.1), les annotations connues `@Override`, `@Deprecated`, `@SuppressWarnings`, `@SafeVarargs`, `@FunctionalInterface` (0.4.2), tes propres annotations avec `@interface` : éléments, types permis, valeurs par défaut, marqueur, élément unique `value` (0.4.3).  
**Ce qui est donné :** `Data.java` (les soumissions, du texte) et `Check.java` (le correcteur). L'outil `projectkit.Javac` (un javac en mémoire) est fourni aussi.  
**Ce que TU crées :** tous les fichiers `.java` du projet, dans ce paquet `vorkurs04_annotations.projects.p01_pluginstore`.

**Noms imposés.** Les soumissions sont écrites par des développeurs extérieurs contre l'API publique de la boutique. Elles utilisent donc des noms précis, que tu dois respecter :
- les annotations `Plugin`, `Since`, `Experimental`, `Requires` et leurs éléments ;
- l'enum `Category`, l'interface `Action` et la classe abstraite `BasePlugin` ;
- la classe qui contient `main` : **`PluginStore`**.

Tout le reste est libre : records, enums, méthodes, découpage.

**Pour vérifier :** lance `Check.java` depuis le dossier `base/annotation-kurse`. Il exécute ton `main`, compare ta sortie à la sortie attendue (en bas de ce fichier) et montre la première ligne fausse. Il lit aussi tes sources : il liste ce qui manque et refuse ce qui vient d'une section plus loin du cours. Ne regarde `solution/` qu'à la fin.

---

## Le problème

Une boutique en ligne accepte des **plugins** écrits par d'autres développeurs. Avant de publier un plugin, la boutique le **compile** contre sa propre API et décide :
- `ACCEPTE` : aucune erreur, aucun avertissement ;
- `ACCEPTE avec N avertissement(s)` : ça compile, mais le plugin utilise des choses risquées ;
- `REFUSE` : javac a trouvé au moins une erreur.

La règle centrale du cours 0.4.1 : **une annotation ne fait rien toute seule.** Ici, c'est **javac** qui lit les annotations et applique leurs règles. Ta boutique se contente de lancer javac et de **traduire** ce qu'il dit en raisons compréhensibles.

`Data.submissions(paquet)` rend les 18 soumissions dans l'ordre (`S01` → `S18`) : identifiant → source. Chaque source commence par `import {{PKG}}.*;`. Le paquet que tu passes remplace `{{PKG}}`, pour que les soumissions voient **tes** annotations. Passe `PluginStore.class.getPackageName()`.

---

## Tableau de bord

### ☐ Étape 1 — L'API d'annotations de la boutique (0.4.3)

Écris ces quatre annotations, chacune dans son fichier, avec `@interface` :

| Annotation | Éléments | Règle |
|---|---|---|
| `Plugin` | `id` (texte, **obligatoire**), `version` (entier, défaut `1`), `tags` (tableau de textes, défaut **vide**), `category` (une `Category`, défaut `TOOL`) | la carte d'identité d'un plugin |
| `Since` | **un seul** élément, texte, qui doit permettre d'écrire `@Since("1.0")` | version d'apparition |
| `Experimental` | aucun élément | un **marqueur** : sa présence suffit |
| `Requires` | un tableau de `Class` (n'importe quelle classe) qui doit permettre `@Requires(String.class)` | ce dont le plugin a besoin |

Et l'enum `Category` avec `TOOL`, `GAME`, `THEME`.

- Un élément d'annotation s'écrit comme une méthode **sans paramètre**. Pourtant ce n'est **pas** une méthode à appeler : c'est un champ de métadonnées.
- **Question :** quels types un élément a-t-il le droit d'avoir ? Les soumissions S16 et S17 vont tester deux interdits. Lesquels, d'après toi, avant de lancer ?
- **Question :** pourquoi `@Since("1.0")` marche-t-il sans écrire `value =`, alors que `@Plugin("x")` ne marcherait pas ?
- **Question :** `@Requires(String.class)` passe une seule classe à un élément de type tableau. Pourquoi est-ce permis ?
- **Interdit dans ce projet :** `@Target`, `@Retention` et les autres méta-annotations. Ils arrivent aux projets 2 et 3. Sans eux, tes annotations gardent leur comportement par défaut, et c'est très bien ici.

### ☐ Étape 2 — `Action`, une interface fonctionnelle vérifiée (0.4.2)

- `Action` a **une** méthode abstraite : elle reçoit un texte et rend un texte. Appelle-la `run`, car les soumissions l'implémentent.
- Marque l'interface pour que **javac refuse** toute deuxième méthode abstraite.
- Ajoute une méthode **`default`** nommée `twice()`, qui rend une `Action` appliquant `run` deux fois de suite.
  - **Question :** pourquoi une méthode `default` ne casse-t-elle pas la règle « une seule méthode abstraite » ?

### ☐ Étape 3 — `BasePlugin`, la classe de base (0.4.2)

Classe abstraite, avec :
- une méthode abstraite `name()` qui rend un texte ;
- `start()` : la nouvelle méthode de démarrage, qui ne fait rien ;
- `init()` : l'ancienne méthode, **dépréciée depuis la version `2.0`** ;
- `legacyInit()` : très ancienne, **dépréciée depuis `2.0` et qui sera supprimée**.
  - Choisis les bons éléments de `@Deprecated`.
  - **Question :** quel avertissement javac donnera-t-il dans chaque cas ?
- `listOf(...)` : une méthode **générique** à **varargs**. Elle reçoit des éléments de type `T` et rend une `List<T>` (avec `List.of`).
  - Promets au compilateur qu'elle ne pollue pas son tableau générique.
  - Où cette promesse est-elle permise ? `static`, `final`, `private`… La soumission S09 se trompe exactement là-dessus.
- `toString()` qui rend `Plugin <nom>`, avec l'annotation qui fait vérifier que tu **redéfinis** bien une méthode existante.
  - **Expérience** (hors sortie attendue) : écris `tostring()` au lieu de `toString()`, compile, lis l'erreur, puis corrige.

### ☐ Étape 4 — Compiler une soumission

- `Javac.compile(Map.of(id, source))` compile **une** soumission. Il rend un `Javac.Result`.
- Le résultat contient `diags()` : une liste de `Javac.Diag(kind, file, line, code, message)`, déjà triée par ligne.
  - `kind` vaut `"ERROR"`, `"WARNING"` ou `"NOTE"`.
  - `line` est la ligne **dans la soumission**.
  - `code` est le code stable de javac.
- **Règle :** tu classes **uniquement par `code`**. Le `message` dépend de la langue de la machine et de la version de Java. `Check` refuse tout appel à `.message()`.
- Sépare les erreurs et les avertissements. Garde-les **dans l'ordre des lignes**.

### ☐ Étape 5 — Traduire un code en raison

La table officielle de la boutique (le **libellé** est ce qui s'affiche) :

| Code javac | Libellé |
|---|---|
| `compiler.err.method.does.not.override.superclass` | `faux @Override` |
| `compiler.err.static.methods.cannot.be.annotated.with.override` | `@Override sur une methode static` |
| `compiler.err.annotation.missing.default.value` | `element obligatoire manquant` |
| `compiler.err.attribute.value.must.be.constant` | `valeur non constante` |
| `compiler.err.prob.found.req` | `mauvais type de valeur` |
| `compiler.err.cant.resolve.location.args` | `element inexistant` |
| `compiler.err.bad.functional.intf.anno.1` | `@FunctionalInterface invalide` |
| `compiler.err.varargs.invalid.trustme.anno` | `@SafeVarargs mal place` |
| `compiler.err.duplicate.annotation.missing.container` | `annotation repetee` |
| `compiler.err.invalid.annotation.member.type` | `type d'element interdit` |
| `compiler.warn.has.been.deprecated` | `API depreciee` |
| `compiler.warn.has.been.deprecated.for.removal` | `API retiree bientot` |
| `compiler.warn.unchecked.varargs.non.reifiable.type` | `varargs generique non sur` |
| `compiler.warn.unchecked.generic.array.creation` | `tableau generique cree a l'appel` |

- Un code absent de la table s'affiche `code inconnu <code>`, sans planter.
- **Conception (chapitre 7) :** une enum dont chaque constante porte son code et son libellé, plus une recherche « par code » qui rend un `Optional`. C'est plus solide qu'une cascade de `if`.

### ☐ Étape 6 — Une ligne par soumission

Les trois formes :
```
S01 ACCEPTE
S02 REFUSE : l.6 faux @Override
S07 ACCEPTE avec 2 avertissement(s) : l.7 API depreciee ; l.8 API retiree bientot
```
- Chaque diagnostic s'écrit `l.<ligne> <libellé>`, et ils sont séparés par ` ; `.
- Un refus liste **toutes** ses erreurs. Ici, chaque soumission refusée n'en a qu'une.

**Avant de lancer, prédis le verdict de :**
- **S04 et S05.** Les deux mettent une constante de la classe dans `@Plugin(id = …)`. Pourquoi l'une est-elle refusée et pas l'autre ? Qu'est-ce qu'une **constante de compilation** ?
- **S08.** Elle appelle les mêmes méthodes que S07. Pourquoi n'a-t-elle aucun avertissement ?
- **S13.** Elle appelle `listOf` avec des `String`, sans avertissement. **S14** fait pareil avec sa **propre** méthode varargs générique et reçoit deux avertissements. Pourquoi ?

### ☐ Étape 7 — Le bilan (3 lignes)

```
BILAN : 7 accepte(s) dont 2 avec avertissements, 11 refuse(s)
```
- La ligne des causes : chaque libellé d'**erreur** avec son nombre d'occurrences, écrit `<libellé> x<nombre>`.
  - Tri : du plus fréquent au plus rare, puis par ordre alphabétique du libellé (ordre naturel de `String`).
  - Séparateur : `, `.
- La ligne « à migrer » : les plugins **acceptés** qui ont au moins un avertissement `API retiree bientot`, dans l'ordre d'arrivée. Sans aucun, la ligne finit par `aucun`.
```
BILAN : a migrer avant la v3 : S07
```

### ☐ Étape 8 — Une annotation n'exécute rien (0.4.1, 0.4.2)

Deux dernières lignes prouvent qu'au moment de l'**exécution**, les annotations ne changent rien :
- Crée un `BasePlugin` anonyme nommé `Demo`. Fais-le démarrer par une méthode de la boutique qui appelle `legacyInit()`.
  - Ce code de la boutique **utilise** une API vouée à la suppression, en connaissance de cause. Fais taire **ce seul** avertissement, sur **cette seule** méthode.
  - **Question :** quel nom exact d'avertissement faut-il supprimer ? `"deprecation"` suffit-il ?
- Affiche ensuite le plugin (avec son `toString`) et la taille de `listOf("a", "b")` :
```
EXECUTION : Plugin Demo a demarre via legacyInit() ; 2 elements via listOf
```
- Crée une `Action` par une lambda qui met le texte en majuscules, puis affiche `twice().run("hey")` :
```
EXECUTION : HEY
```

### ☐ Étape 9 — Le `main` de `PluginStore`

Il revoit les soumissions dans l'ordre, affiche une ligne par soumission, puis les 3 lignes de BILAN et les 2 lignes d'EXECUTION. Ensuite, lance `Check.java`.

**Expérience** (hors sortie attendue) : ajoute `@Experimental` sur ta classe `PluginStore`, relance : rien ne change. Qui pourrait lire cette information, et quand ? (C'est tout l'objet des projets 4 et suivants.)

---

## Checklist API (vérifiée par `Check`)

| Élément | Étape | ☐ |
|---|---|---|
| `@interface`, éléments, `default` | 1 | ☐ |
| élément `String[]` et `Class<?>[]`, élément de type `enum` | 1 | ☐ |
| élément unique `value()` | 1 | ☐ |
| `@FunctionalInterface` | 2 | ☐ |
| `@Deprecated(since…)` et `forRemoval` | 3 | ☐ |
| `@SafeVarargs` | 3 | ☐ |
| `@Override` | 3 | ☐ |
| `Javac.compile`, `diags()`, `kind()`, `line()`, `code()` | 4 | ☐ |
| `@SuppressWarnings(...)` | 8 | ☐ |
| `getPackageName()` | intro | ☐ |
| ~~`.message()`~~, ~~`@Target`~~, ~~`@Retention`~~, ~~reflection~~ | **interdits** | — |

---

## Sortie attendue complète

C'est le contrat exact que vérifie `Check` :

```
a remplir
```
