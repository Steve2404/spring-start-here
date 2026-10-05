# Projet 2 — Le guichet de conformité (cours 0.4.4 → 0.4.5)

> Première fois ? Lis d'abord le mode d'emploi [`vorkurs04_annotations/PARCOURS.md`](../../PARCOURS.md).

**Notions visées :** `@Target` et toutes les valeurs d'`ElementType`, le cas « sans `@Target` », les cibles en double (0.4.4) ; `@Retention` avec `SOURCE`, `CLASS`, `RUNTIME`, la valeur par défaut, ce qui est réellement écrit dans le `.class`, le cas des variables locales (0.4.5).  
**Ce qui est donné :** `Data.java`, `Check.java`, et l'outil `projectkit.Javac` (`compile` et `javap`).  
**Ce que TU crées :** tous les `.java` du projet, dans le paquet `vorkurs04_annotations.projects.p02_compliance`. Noms imposés : les 10 annotations du tableau ci-dessous (les sondes les utilisent) et la classe **`ComplianceDesk`** qui contient `main`.

**Pour vérifier :** lance `Check.java` depuis `base/annotation-kurse`. Ne regarde `solution/` qu'à la fin.

---

## Le problème

L'entreprise publie un **catalogue d'annotations maison**. Chaque annotation doit être posée **seulement** là où elle a un sens (`@Target`) et vivre **juste assez longtemps** (`@Retention`). Le guichet de conformité vérifie trois choses :
1. **La matrice réelle :** pour chaque annotation, dans quels contextes javac l'accepte **vraiment**. On ne croit pas la documentation, on **mesure**.
2. **Les déclarations des autres équipes :** certaines sont fautives.
3. **Le fichier `.class` :** ce qui reste vraiment de chaque annotation après la compilation.

Interdit ici : la reflection (`getAnnotation`, etc.), qui arrive au projet 4. Tu n'as donc pas le droit de **lire** `@Target` à l'exécution. Tu le **mesures** avec javac, et c'est plus instructif.

---

## Tableau de bord

### ☐ Étape 1 — Le catalogue (0.4.4, 0.4.5)

Écris ces 10 annotations, sans élément. Respecte **exactement** les cibles et les rétentions :

| Annotation | `@Target` | `@Retention` |
|---|---|---|
| `Audited` | `TYPE`, `METHOD` | `RUNTIME` |
| `Sensitive` | `FIELD`, `PARAMETER` | `RUNTIME` |
| `Factory` | `CONSTRUCTOR`, `METHOD` | `CLASS`, écrit explicitement |
| `Trace` | `METHOD` | **aucune annotation `@Retention`** |
| `Generated` | `TYPE` | `SOURCE` |
| `Local` | `LOCAL_VARIABLE` | `RUNTIME` |
| `Meta` | `ANNOTATION_TYPE` | `RUNTIME` |
| `TypeTag` | `TYPE_PARAMETER` | `RUNTIME` |
| `NotNull` | `TYPE_USE` | `RUNTIME` |
| `Anywhere` | **aucune annotation `@Target`** | `RUNTIME` |

- `@Target` reçoit un **tableau** d'`ElementType` : `{A, B}`. Avec une seule valeur, les accolades sont facultatives.
- **Question :** quelle rétention a `Trace` ? Trois réponses possibles ; la sortie de l'étape 4 te la confirmera.
- **Question :** où `Anywhere` est-il permis ? Prédis-le **avant** l'étape 2.

### ☐ Étape 2 — La matrice mesurée (0.4.4)

`Data.CONTEXTS` liste 10 **contextes** dans un ordre fixe : `type`, `annotation`, `record`, `typeparam`, `field`, `constructor`, `method`, `parameter`, `local`, `typeuse`. Chaque contexte a une ligne de code complète, avec `{A}` à la place de l'annotation.
- **Construis une sonde par annotation :** la ligne `import <ton paquet>.*;`, puis les 10 lignes, `{A}` remplacé par `@NomDeLAnnotation`.
  - Le contexte n° `i` (0, 1, …) se trouve donc à la ligne **`i + 2`** de la sonde.
- Compile la sonde **seule**, sous le nom `Probe` : `Javac.compile(Map.of("Probe", sonde))`.
  - Une **erreur** sur la ligne `i + 2` veut dire que le contexte `i` est refusé.
  - **Une seule compilation par annotation**, pas une par contexte. C'est l'algorithme de ce projet.
- Affiche les contextes **acceptés**, dans l'ordre de `Data.CONTEXTS`, séparés par un espace :
```
MATRICE Audited : type annotation record method
MATRICE Local : local
```
- Les 10 annotations, dans l'ordre du tableau de l'étape 1.

**Avant de lancer, prédis, puis explique les surprises :**
- **Pourquoi `Audited` (`TYPE`, `METHOD`) est-il accepté :**
  - sur une déclaration d'**annotation** ? Qu'est-ce que `TYPE` couvre exactement ?
  - sur un **composant de record** ? Pense à tout ce que le compilateur génère à partir d'un composant.
- **`NotNull` (`TYPE_USE`) :**
  - Il est accepté sur `type`, `annotation`, `constructor`… mais **pas** sur `method`. Regarde la ligne de `method` : quel est le type de retour ?
  - Que désigne un `TYPE_USE` posé sur un constructeur ?
- **`Anywhere` (sans `@Target`) :** dans quels contextes **n'est-il pas** permis ?

### ☐ Étape 3 — Les déclarations des autres équipes (0.4.4)

`Data.DECLARATIONS` : 4 déclarations (`D1` → `D4`). Le nom de l'annotation est celui de la **dernière** ligne (`@interface Twice {}` → `Twice`).
- **Compile d'abord la déclaration seule.**
  - S'il y a une erreur, affiche la ligne de la **première** erreur.
  - Le code `compiler.err.repeated.annotation.target` s'écrit `cible en double`. Tout autre code s'affiche tel quel.
```
DECLARATION D1 Twice refusee l.2 : cible en double
```
- **Sinon, mesure sa matrice.**
  - Compile **ensemble**, dans la même `Map`, la déclaration (sous son id) et la sonde (`Probe`).
  - **Attention :** ne compte que les erreurs **de `Probe`** (`Diag.file()`).
  - Affiche une ligne `MATRICE`. Une annotation acceptée nulle part s'écrit `MATRICE Nowhere : aucun contexte`.
- **Question :** à quoi peut bien servir `@Target({})` ? Cherche un cas où une annotation ne s'écrit **que comme valeur** d'une autre annotation.

### ☐ Étape 4 — Le fichier `.class` (0.4.5)

- Compile `Data.SAMPLE` sous le nom `Sample`, avec `{{PKG}}` remplacé par ton paquet.
- `Javac.javap(résultat, "Sample")` rend le texte de `javap -v -p`. Voici un vrai extrait, que tu dois apprendre à lire :
```
  void run(java.lang.String);
    descriptor: (Ljava/lang/String;)V
    flags: (0x0000)
    Code:
      ...
      LineNumberTable:
    RuntimeVisibleAnnotations:
      0: #22()
        <ton paquet>.Audited
    RuntimeInvisibleAnnotations:
      0: #23()
        <ton paquet>.Trace
    RuntimeVisibleParameterAnnotations:
      parameter 0:
        0: #15()
          <ton paquet>.Sensitive
```
- **Algorithme de lecture :**
  - Une ligne qui finit par `Annotations:` **ouvre une section**, dont le nom est le texte sans le `:` final.
  - Dans une section, une ligne faite **d'un seul nom qualifié** (des points, aucun espace) est une annotation. Garde son nom simple.
  - Les lignes `0: #22()` et `parameter 0:` font partie de la section ; **toute autre ligne la ferme**.
  - Compte, pour chaque annotation, ses occurrences dans chaque section.
- **Affiche** une ligne par annotation **écrite dans `Sample`**, par ordre alphabétique :
  - les sections triées par nom, chacune suivie de `x<nombre>`, séparées par `, ` ;
  - `absente` si l'annotation n'apparaît nulle part dans le `.class`.
```
CLASSFILE Factory : RuntimeInvisibleAnnotations x2
CLASSFILE Generated : absente
CLASSFILE Sensitive : RuntimeVisibleAnnotations x1, RuntimeVisibleParameterAnnotations x1
```
- **La dernière ligne** donne les annotations qui ont au moins une section `RuntimeVisible…` : seules celles-là seront lisibles par la reflection au projet 4. Ensemble trié, au format `toString()` d'un `TreeSet` :
```
REFLECTION : lisibles a l'execution [Audited, NotNull, Sensitive]
```
- **Questions :**
  - `Local` est `RUNTIME` : pourquoi est-il `absente` ?
  - `NotNull`, sur un type de retour, n'est pas dans `RuntimeVisibleAnnotations` mais dans une autre section. Laquelle, et pourquoi ?
  - `Factory` et `Trace` sont dans le `.class`, mais invisibles à l'exécution : à qui sert `CLASS` ?

### ☐ Étape 5 — Le `main` de `ComplianceDesk`

Il affiche, dans l'ordre :
1. les 10 lignes `MATRICE` du catalogue ;
2. les lignes des déclarations `D1` → `D4` ;
3. les lignes `CLASSFILE` ;
4. la ligne `REFLECTION`.

Puis lance `Check.java`.

**Expérience** (hors sortie attendue) :
- Retire `@Retention` de `Audited` et relance : quelles lignes changent ? Remets-le.
- Change `Local` en `@Retention(RetentionPolicy.SOURCE)` : la ligne `CLASSFILE Local` change-t-elle ? Pourquoi pas ?

---

## Checklist API (vérifiée par `Check`)

| Élément | Étape | ☐ |
|---|---|---|
| `@Target(...)` avec `TYPE`, `METHOD`, `FIELD`, `PARAMETER`, `CONSTRUCTOR`, `LOCAL_VARIABLE`, `ANNOTATION_TYPE`, `TYPE_PARAMETER`, `TYPE_USE` | 1 | ☐ |
| `@Retention(...)` avec `SOURCE`, `CLASS`, `RUNTIME` | 1 | ☐ |
| `Data.CONTEXTS`, `Javac.compile`, `Diag.file()`, `line()`, `code()` | 2, 3 | ☐ |
| `Data.DECLARATIONS` | 3 | ☐ |
| `Data.SAMPLE`, `Javac.javap` | 4 | ☐ |
| ~~`.message()`~~, ~~méta-annotations de 0.4.6~~, ~~reflection~~ | **interdits** | — |

---

## Sortie attendue complète

C'est le contrat exact que vérifie `Check` :

```
a remplir
```
