# Projet 3 — L'arbre maison (cours 0.2.9 → 0.2.10)

> Première fois ? Lis d'abord le mode d'emploi [`vorkurs02_xml/PARCOURS.md`](../../PARCOURS.md) : comment lire cette fiche, lancer `Check`, quoi faire en cas de blocage.

**Notions visées :** bien formé contre valide, règles WFC contre VC, processeurs validants ou non, valide ne veut pas dire correct pour le métier (0.2.9) ; XML comme arbre : document contre élément racine, sortes de nœuds, parent / enfant / ancêtre / descendant, frères ordonnés, attributs hors des enfants, nœud enfant contre élément enfant (0.2.10).  
**Ce qui est donné :** `Data.java` et `Check.java`. De `xmlkit.XmlKit`, tu utilises `wellFormed`, `value` (projet 2), et deux nouveaux arbitres :
- `select(xml, expression)` : la liste des nœuds qu'un vrai parseur trouve, chacun rendu en texte (voir l'étape 1) ;
- `validateDtd(xml, Map.of())` : `VALID`, `INVALID n (ligne l)` ou `NOT_WELL_FORMED …`.

Les expressions sont données : recopie-les, le langage XPath arrive au projet 7.  
**Ce que TU crées :** tous les fichiers `.java`, dans le paquet `vorkurs02_xml.projects.p03_tree`. La classe du `main` s'appelle **`Tree`**.

---

## Le problème

Un parseur ne rend pas du texte : il rend un **arbre de nœuds**. Tu construis le tien, à la main, puis tu le confrontes nœud par nœud à celui d'un vrai parseur. Ensuite, tu l'utilises au bout d'une chaîne de contrôle : bien formé → valide → correct pour le métier.

---

## Tableau de bord

### ☐ Étape 1 — Le modèle de nœuds (0.2.10 S2, S3, S6)

Cinq sortes de nœuds : `DOCUMENT`, `ELEMENT`, `TEXT`, `COMMENT`, `PI`.
- Chaque nœud connaît son **parent** et la liste **ordonnée** de ses enfants.
- Un élément a en plus ses **attributs**, dans l'ordre du texte. Ils ne sont **pas** dans la liste des enfants.
- **Le rendu d'un nœud** (le même que `XmlKit.select`, pour pouvoir comparer) :

| Sorte | Rendu |
|---|---|
| document | `/` |
| élément | son nom |
| texte | son texte entre guillemets doubles : `"Stylo"` |
| commentaire | `<!--` + texte + `-->` |
| PI | `<?` + cible + un espace + données + `?>` |
| attribut | `@nom=valeur` |

- **Question :** pourquoi le document et l'élément racine sont-ils deux nœuds différents ? Que peut contenir le document, à part la racine ?

### ☐ Étape 2 — Construire l'arbre (0.2.10 S1)

Lis `Data.ORDER` de gauche à droite, avec un « nœud courant » (au début, le document) :
- la **déclaration** `<?xml … ?>` n'est **pas** un nœud ; un **DOCTYPE** non plus (avec son éventuel `[ … ]`) ;
- `<!-- … -->` : un nœud commentaire dans le nœud courant ;
- `<?cible données?>` : un nœud PI. La cible est le premier mot ; les données, le reste après les blancs ;
- une balise ouvrante : un élément enfant du nœud courant, qui **devient** le nœud courant. Une balise vide ne le devient pas. Une fermante remonte au parent ;
- le texte : au niveau du document, les blancs **ne sont pas** des nœuds. Dans un élément, **tout** texte est un nœud, même fait seulement de blancs. Deux morceaux de texte qui se suivent ne font qu'**un** nœud ;
- dans le texte et dans les valeurs d'attributs, décode les cinq entités prédéfinies (`&amp;` en dernier).

Affiche l'arbre après `ARBRE :`, un nœud par ligne, en ordre de lecture :
- 2 espaces par niveau, le document étant au niveau 1 ;
- la sorte, un espace, le rendu (caractères de contrôle rendus visibles comme au projet 2 : `\n`) ;
- pour un élément, chaque attribut au format ` @nom=valeur`.
```
  DOCUMENT /
    COMMENT <!-- commande du jour -->
    …
      TEXT "\n  "
```
- **Question :** combien de nœuds texte « invisibles » ton arbre contient-il ? D'où viennent-ils ?

### ☐ Étape 3 — Naviguer et confronter (0.2.10 S4, S5, S7)

Pour chaque requête, calcule la liste **avec ton arbre**, rends chaque nœud, puis compare **toute** la liste à `XmlKit.select(Data.ORDER, expression)` :
```
Q2 9 : "\n  " · ligne · … | parseur identique
```
- `Q1 <nombre> : ` les rendus joints par ` · ` (avec contrôles visibles), puis ` | parseur identique` ou ` | parseur DIFFERENT`.

| Requête | Ce que tu calcules | Expression pour le parseur |
|---|---|---|
| `Q1` | les enfants du **document** | `/node()` |
| `Q2` | les enfants de la racine `commande` | `/commande/node()` |
| `Q3` | les enfants de l'élément `note` | `/commande/note/node()` |
| `Q4` | les ancêtres du `prix` de la 2e `ligne`, du plus haut (le document) au parent direct | `/commande/ligne[2]/prix/ancestor::node()` |
| `Q5` | les nœuds frères **qui suivent** la 1re `ligne` | `/commande/ligne[1]/following-sibling::node()` |
| `Q6` | tous les nœuds texte **descendants** de `commande`, en ordre de lecture | `/commande//text()` |
| `Q7` | les attributs de la 1re `ligne` | `/commande/ligne[1]/@*` |

Puis une ligne de comptes pour `commande` :
```
COMPTES commande : noeuds enfants=9 elements enfants=3 attributs=2
```
- **Question :** `Q3` montre un **contenu mixte**. Pourquoi « le texte de `note` » est-il ambigu ?
- **Question :** deux nœuds sont frères s'ils ont le même parent direct. Le commentaire `promo` est-il un frère des `ligne` ?

### ☐ Étape 4 — La chaîne bien formé → valide → métier (0.2.9)

Pour chaque document de `Data.ORDERS`, une ligne. Chaque couche suppose la précédente : on **s'arrête** à la première qui échoue.
1. **Bien formé ?** Avec `wellFormed`. Sinon : `NON_BIEN_FORME KO l:c [WFC]`.
2. **Valide ?**
   - Si le texte ne contient pas `<!DOCTYPE`, la validité n'est pas définie : l'en-tête est `BIEN_FORME sans DTD`, et on passe au métier.
   - Sinon, avec `validateDtd(texte, Map.of())`. Si ce n'est pas `VALID`, affiche son résultat avec `INVALID` traduit en `INVALIDE`, suivi de ` [VC]`.
   - Sinon, l'en-tête est `VALIDE`.
3. **Correct pour le métier ?** Avec **ton** arbre, pour chaque `ligne` dans l'ordre (numérotées à partir de 1). Le texte d'un élément = la concaténation de ses nœuds texte descendants.
   - `quantite` doit être un entier strictement positif sans zéro en tête. Sinon : `KO ligne n quantite=<texte>`.
   - `prix` doit être des chiffres, éventuellement suivis d'un point et de chiffres. Sinon : `KO ligne n prix=<texte>`.
   - Sinon : `OK total=<somme des quantite × prix>`, avec exactement 2 décimales (`BigDecimal`, arrondi `HALF_UP`).
   - L'en-tête, puis ` | metier ` et ce résultat.
```
O01 VALIDE | metier OK total=4.50
O02 INVALIDE 1 (ligne 10) [VC]
```
- **Question :** `O03` : pourquoi la DTD ne peut-elle **rien** dire d'un document mal formé ?
- **Question :** `O04` est valide. Pourquoi la DTD laisse-t-elle passer `-2` et `gratuit` ? Quel outil pourrait l'en empêcher (projet 6) ?

### ☐ Étape 5 — Ce que le parseur ajoute (0.2.9 S6, 0.2.11)

La DTD de `Data.DTD` déclare une valeur **par défaut** pour l'attribut `devise`. Pour `O01` puis `O07`, compare l'attribut `devise` de la racine dans **ton** arbre (`absent` s'il n'y est pas) et `XmlKit.value(texte, "string(/commande/@devise)")` :
```
DEFAUT O01 devise : arbre maison absent | parseur EUR
```
- **Question :** ton arbre et celui du parseur ne contiennent donc pas les mêmes données. Pourquoi un processeur, même **non validant**, doit-il lire le sous-ensemble interne de la DTD ?

### ☐ Étape 6 — Le `main` de `Tree`

Dans l'ordre : l'ARBRE, `Q1` à `Q7`, les COMPTES, les lignes `O01` à `O07`, puis les deux lignes DEFAUT.

---

## Checklist API (vérifiée par `Check`)

| Élément | Étape | ☐ |
|---|---|---|
| une `enum` des sortes de nœuds | 1 | ☐ |
| `XmlKit.select(` | 3 | ☐ |
| `XmlKit.wellFormed(`, `XmlKit.validateDtd(` | 4 | ☐ |
| `BigDecimal`, `RoundingMode.HALF_UP` | 4 | ☐ |
| `XmlKit.value(` | 5 | ☐ |
| ~~`javax.xml`~~, ~~`org.w3c`~~, ~~`org.xml`~~, ~~`XmlKit.validateXsd`~~ | **interdits** (sections suivantes) | — |

---

## Sortie attendue complète

C'est le contrat exact que vérifie `Check` :

```
a remplir
```
