# Projet 5 — La console d'exploitation (cours 0.4.9 → 0.4.10)

> Première fois ? Lis d'abord le mode d'emploi [`vorkurs04_annotations/PARCOURS.md`](../../PARCOURS.md).

**Notions visées :**
- **0.4.9** :
  - les annotations sont des **données** : les lire ne fait encore rien ;
  - scanner et filtrer les éléments annotés, lire les éléments de l'annotation ;
  - marqueurs, drapeaux et valeurs comme signaux de décision ;
  - séparer la recherche par reflection de la logique métier ;
  - **valider** les métadonnées, détecter les conflits **sans inventer d'ordre** ;
  - la chaîne complète : découverte → détection → lecture → validation → modèle → décision.
- **0.4.10** :
  - `Method.invoke` ;
  - le **receveur** d'une méthode d'instance, et `null` pour une méthode `static` ;
  - la conversion des arguments, les retours `void` et primitifs, les varargs ;
  - une méthode `private` : `canAccess` et `trySetAccessible` ;
  - les erreurs de la reflection contre les erreurs **de la méthode appelée**.

**Ce qui est donné :** `Data.java` (trois classes de commandes **sous forme de source**, plus le script), `Check.java`, `projectkit.Javac` (`compile`, `load`).  
**Ce que TU crées :** tout le reste, dans `vorkurs04_annotations.projects.p05_console`. Noms imposés : l'annotation **`Command`**, avec les éléments du tableau ci-dessous, et la classe `main` **`Console`**.

**Pour vérifier :** lance `Check.java` depuis `base/annotation-kurse`.

---

## Le problème

Une console d'exploitation exécute des commandes tapées par des utilisateurs. Les commandes ne sont **pas codées en dur** : ce sont des méthodes marquées `@Command` dans des classes que la console découvre au démarrage. C'est le cœur de tout framework web : une URL, une méthode annotée, `invoke`.

- `Data.HANDLERS` : nom de classe → source, **dans l'ordre d'enregistrement**.
  - Compile les trois sources **ensemble**, avec `{{PKG}}` remplacé.
  - Charge chaque classe avec `Javac.load`.
- `Data.SCRIPT` : les lignes tapées, au format `utilisateur commande arguments…`, séparés par un espace.

---

## Tableau de bord

### ☐ Étape 1 — L'annotation `@Command` (0.4.9 S3–S4)

| Élément | Type | Défaut |
|---|---|---|
| `name` | texte | **obligatoire** |
| `description` | texte | `""` |
| `adminOnly` | booléen | `false` |
| `priority` | entier | `0` |

- Sur les méthodes seulement, et lisible à l'exécution.
- **Question :** `adminOnly` est un **drapeau**, `priority` une **valeur**, `description` un **texte d'affichage**. Laquelle des trois change le **comportement** de la console, laquelle seulement l'**ordre**, et laquelle rien du tout ?

### ☐ Étape 2 — Découvrir et valider (0.4.9 S2, S5, S6)

- Pour chaque classe, dans l'ordre de `Data.HANDLERS`, prends ses méthodes déclarées portant `@Command`, **triées par nom**.
- Chaque méthode est validée, et la **première** règle violée l'écarte :
```
REJET BrokenCommands.blank : nom vide
REJET BrokenCommands.dump : type non gere Object
```
  - **Nom vide :** un nom fait uniquement de blancs.
  - **Type non géré :** les types de paramètre gérés sont `String`, `int`, `long`, `double` et `boolean`.
    - Un **varargs** (le dernier paramètre d'une méthode `isVarArgs()`) est géré si le type de **ses éléments** l'est.
    - Le libellé donne le nom simple du premier type non géré.
- **Les conflits :** si **plusieurs** méthodes valides portent le même nom de commande, on **n'invente pas** de priorité, et **aucune** n'est enregistrée. Une ligne par nom en conflit (noms triés), avec les méthodes concernées `Classe.méthode` triées et séparées par `, ` :
```
CONFLIT add : BrokenCommands.plus, MathCommands.add
```
- **Le receveur :** chaque classe fournit une méthode `public static … create()` qui fabrique **le** receveur de ses méthodes d'instance.
  - Appelle-la par reflection : pour une méthode `static`, le receveur passé à `invoke` est `null`.
  - **Question :** pourquoi un **seul** receveur par classe ? Regarde l'état de `FileCommands` entre `touch` et `ls`.
- **Conception (0.4.9 S5) :** sépare la **recherche** par reflection (une étape qui produit un registre de commandes validées, avec leur méthode, leur receveur et leurs métadonnées lues) de l'**exécution**, qui ne relit plus jamais les annotations.

### ☐ Étape 3 — Le registre

Une seule ligne, avec les commandes enregistrées **triées par nom**, séparées par un espace.
- Chaque commande s'écrit : son nom, ses types de paramètres entre parenthèses (séparés par `,`, un varargs s'écrit `int...`), puis ` [admin]` si elle est réservée aux admins, puis ` [prive]` si la méthode est `private`.
```
COMMANDES : count() div(int,int) ls() mul(long,long) pct(double,double) ping(boolean) rm(String) [admin] secret() [admin] [prive] sum(int...) touch(String)
```
- **Question :** `getParameterTypes()` rend `int[]` pour `sum`. Comment retrouves-tu `int` ?

### ☐ Étape 4 — Exécuter une ligne (0.4.10)

Chaque ligne du script, sauf `help`, affiche `<ligne> : <résultat>`. Le résultat est le **premier** cas qui s'applique :

| Cas | Résultat |
|---|---|
| commande absente du registre (inconnue, rejetée, en conflit) | `INCONNU` |
| `adminOnly` et l'utilisateur n'est pas `admin` | `REFUS reserve aux admins` |
| méthode inaccessible : `canAccess` est faux **et** `trySetAccessible()` aussi | `ERREUR ACCES` |
| un argument ne se convertit pas | `ERREUR ARGUMENT` |
| `invoke` lève `IllegalArgumentException` | `ERREUR APPEL` |
| `invoke` lève `InvocationTargetException` | `ERREUR COMMANDE <nom simple de la cause> (<message de la cause>)` |
| méthode `void` | `OK` |
| sinon | la valeur rendue (`String.valueOf`) |

- **La conversion est TON travail :**
  - `int`, `long`, `double` et `boolean` se lisent avec les `parse…` habituels ;
  - **un varargs reçoit tous les jetons dans un seul tableau**, et `invoke` attend alors un `Object[]` d'**une** case : ce tableau ;
  - sinon, **un argument par jeton**. Si le nombre est faux, ne le vérifie pas toi-même : laisse `invoke` le refuser.
- **Le receveur :** `null` pour une méthode `static`, le receveur de la classe sinon.

```
guest touch a.txt : OK
guest ls : a.txt,b.txt
guest div 7 0 : ERREUR COMMANDE ArithmeticException (/ by zero)
guest touch : ERREUR APPEL
admin secret : 42
```
**Questions :**
- `div 7 0` et `touch` sans argument échouent tous les deux, mais **pas au même moment**. Lequel a commencé à exécuter la commande ? Comment le distingues-tu ?
- Une méthode `count()` rend un `int` : que rend `invoke` ?
- `secret` est `private`. Que se passerait-il avec `setAccessible(true)` si la classe était dans un module qui ne l'**ouvre** pas ? Pourquoi `trySetAccessible()` est-il plus sûr ? (`Check` interdit `setAccessible`.)

### ☐ Étape 5 — L'aide (une décision prise sur les métadonnées)

La ligne `guest help` n'affiche pas de résultat. Elle affiche une ligne par commande enregistrée :
- triées par **priorité décroissante**, puis par nom ;
- au format `AIDE <nom> : <description>`, ou `(sans description)` si elle est vide.
```
AIDE ls : liste les fichiers
AIDE mul : (sans description)
AIDE count : (sans description)
```

### ☐ Étape 6 — Le `main` de `Console`

Il affiche, dans l'ordre :
1. les lignes `REJET` et `CONFLIT` ;
2. la ligne `COMMANDES` ;
3. une ligne par commande du script, et les lignes `AIDE` à la place de `help`.

---

## Checklist API (vérifiée par `Check`)

| Élément | Étape | ☐ |
|---|---|---|
| `@Retention`, éléments avec `default` | 1 | ☐ |
| `getDeclaredMethods()`, `isAnnotationPresent`, `getAnnotation`, `isBlank()` | 2 | ☐ |
| `getDeclaredMethod("create")`, `invoke(null)` | 2 | ☐ |
| `getParameterTypes()`, `isVarArgs()`, `getComponentType()` | 2, 3 | ☐ |
| `Modifier.isStatic`, `Modifier.isPrivate` | 3, 4 | ☐ |
| `canAccess`, `trySetAccessible()` | 4 | ☐ |
| `invoke`, `getReturnType()`, `void.class` | 4 | ☐ |
| `IllegalArgumentException`, `InvocationTargetException` + `getCause()` | 4 | ☐ |
| un tri (`Comparator`) | 2, 5 | ☐ |
| ~~`setAccessible`~~, ~~`newInstance`~~, ~~`getGeneric…`~~ | **interdits** | — |

---

## Sortie attendue complète

C'est le contrat exact que vérifie `Check` :

```
a remplir
```
