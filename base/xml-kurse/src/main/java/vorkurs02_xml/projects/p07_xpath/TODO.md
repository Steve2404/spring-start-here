# Projet 7 — Le moteur de requêtes (cours 0.2.16)

> Première fois ? Lis d'abord le mode d'emploi [`vorkurs02_xml/PARCOURS.md`](../../PARCOURS.md) : comment lire cette fiche, lancer `Check`, quoi faire en cas de blocage.

**Notions visées :** contexte et focus, chemins absolus et relatifs, `/` contre `//`, step = axe + test de nœud, prédicats et position, axes (`ancestor`, `following-sibling`…), noms XPath et namespaces, ensembles de nœuds contre valeurs, fonctions (`count`, `sum`, `normalize-space`, `not`) (0.2.16).  
**Ce qui est donné :** `Data.java` et `Check.java`. De `xmlkit.XmlKit` : `select(xml, expression)` et `select(xml, expression, préfixes)` (des nœuds), `value(xml, expression)` (une valeur en texte). Cette fois, **c'est toi qui écris les expressions**.  
**Ce que TU crées :** tous les fichiers `.java`, dans le paquet `vorkurs02_xml.projects.p07_xpath`. La classe du `main` s'appelle **`Queries`**.

---

## Le problème

Les RH interrogent l'organigramme `Data.COMPANY`. Deux missions :
1. écrire les **expressions XPath** qui répondent à leurs questions ;
2. comprendre XPath de l'intérieur : écrire **ton propre moteur** pour un sous-ensemble du langage, et le confronter au vrai.

---

## Tableau de bord

### ☐ Étape 1 — Les questions des RH (0.2.16 S1 → S7)

Pour chaque question, écris **une** expression XPath (deux pour `Q10`), évalue-la sur `Data.COMPANY` et affiche la ligne.
- Quand la réponse est une liste de nœuds, utilise `select` et joins les rendus par `, `. Un nœud texte se rend entre guillemets (`"Alice"`), un attribut en `@nom=valeur`.
- Quand c'est une valeur, utilise `value`.

| Ligne | Question | Attendu |
|---|---|---|
| `Q1` | les textes des `name` des employés **directs** du département `IT` (pas ceux de ses sous-départements) | `"Alice", "Bob", "Chloe"` |
| `Q2` | le texte du `name` du **2e employé de chaque département** | `"Bob", "Eva", "  Gina   Lopez "` |
| `Q3` | le texte du `name` du **2e employé de tout le document** | `"Bob"` |
| `Q4` | la somme des salaires du département `IT`, sous-départements **compris** | `24500` |
| `Q5` | les textes des `name` des managers (`role='manager'`) dont **personne** n'a l'`id` dans son `reportsTo` | `"Farid", "Hugo"` |
| `Q6` | le nom du département **le plus proche** qui contient l'employé `e5` | `Dev` |
| `Q7` | le nom de l'employé `e7`, blancs du début et de la fin retirés, blancs internes réduits à un espace | `Gina Lopez` |
| `Q8` | les attributs `employee` des `review` (namespace `urn:hr:reviews`) dont le `score` vaut **au moins 4**. Lie le préfixe **`h`** à cette URI (pas `r`) | `@employee=e2, @employee=e7` |
| `Q9` | combien d'éléments `review` trouve `//review`, **sans** préfixe | `0` |
| `Q10` | le nombre d'employés, un espace, le nombre de départements | `8 4` |
| `Q11` | les textes des `name` des employés dont le salaire dépasse **la moyenne** de tous les salaires (calculée dans l'expression) | `"Alice", "Dan", "Farid", "Hugo"` |

- **Question :** `Q2` et `Q3` ne diffèrent que par des parenthèses. Explique avec le mot « step ».
- **Question :** `Q5` : que veut dire `=` entre deux **ensembles** de nœuds en XPath 1.0 ?
- **Question :** `Q6` : pourquoi `ancestor::department[1]` est-il le **plus proche**, et pas le premier du document ?
- **Question :** `Q8` : le document écrit `r:`. Pourquoi `h:` marche-t-il ? Pourquoi `Q9` ne trouve-t-il rien ?

### ☐ Étape 2 — Ton arbre (0.2.16 S1)

Reprends ton arbre (projets 3 à 5) : un nœud document (parent de la racine), des éléments avec leur nom **tel qu'écrit**, leurs attributs, leurs enfants dans l'ordre, et leur texte. La **valeur texte** d'un élément = la concaténation de tous ses textes descendants. **Normalisée**, ses blancs de début et de fin sont retirés et toute suite de blancs devient un espace.

### ☐ Étape 3 — Le mini moteur (0.2.16 S2 → S5)

Ton moteur évalue les chemins de `Data.PATHS`. Le sous-ensemble à gérer :
- un chemin **absolu** : une suite de steps séparés par `/` ; `//` devant un step veut dire « ce step, appliqué à **tous** les descendants-ou-soi du contexte » ;
- un test de nœud : un nom d'élément, ou `*` (tout élément) ;
- des prédicats, appliqués dans l'ordre : `[n]` (position), `[last()]`, `[@a]`, `[@a='v']`, `[x]` (a un enfant `x`), `[x='v']` (a un enfant `x` dont la valeur texte normalisée vaut `v`).

**La règle qui fait tout :** les prédicats s'appliquent à la liste des enfants qui passent le test, **parent par parent**. `//employee[1]` rend donc le premier employé de **chaque** département.
- Le résultat d'un step : l'union sans doublon, rangée dans l'**ordre du document**.

Pour chaque chemin, numéroté `M01`, `M02`… : la liste de **tes** nœuds, chacun rendu par sa valeur texte normalisée, joints par ` | `.
- L'arbitre : `count(<chemin>)` donne le nombre, puis, pour `k` de 1 à ce nombre, `normalize-space((<chemin>)[k])` donne le k-ième. Compare les deux listes.
```
M02 4 : Alice | Dan | Farid | Hugo | parseur identique
```
- Sinon ` | parseur DIFFERENT ` suivi de la liste du parseur (le `toString()` de la liste).
- **Question :** `M09` (`//employee[@reportsTo][2]`) : pourquoi deux résultats, et pas « le 2e employé qui a un `reportsTo` » ?
- **Question :** `M03` affiche `Bob4000`. D'où vient cette valeur collée ?

### ☐ Étape 4 — Le `main` de `Queries`

Dans l'ordre : `Q1` à `Q11`, puis `M01` à `M12`.

---

## Checklist API (vérifiée par `Check`)

| Élément | Étape | ☐ |
|---|---|---|
| `XmlKit.select(`, `XmlKit.value(` | 1, 3 | ☐ |
| `sum(`, `count(`, `normalize-space(`, `not(`, `ancestor::` | 1 | ☐ |
| `Map.of("h"` (le préfixe lié par toi) | 1 | ☐ |
| `LinkedHashSet` (union sans doublon) | 3 | ☐ |
| `record ` | 3 | ☐ |
| ~~`javax.xml`~~, ~~`org.w3c`~~, ~~`org.xml`~~, ~~`XmlKit.validate…`~~ | **interdits** | — |

---

## Sortie attendue complète

C'est le contrat exact que vérifie `Check` :

```
a remplir
```
