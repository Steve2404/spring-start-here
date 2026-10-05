# Projet 11 — Trois parseurs, une tâche (cours 0.2.20)

> Première fois ? Lis d'abord le mode d'emploi [`vorkurs02_xml/PARCOURS.md`](../../PARCOURS.md) : comment lire cette fiche, lancer `Check`, quoi faire en cas de blocage.

**Notions visées :** trois modèles mentaux (arbre, push, pull) ; mémoire et données gardées ; qui contrôle le flux ; navigation et modification ; la même tâche métier dans les trois API ; erreurs et ressources ; traduire des besoins en choix de parseur (0.2.20).  
**Ce qui est donné :** `Data.java` et `Check.java`.
- `Data.catalog()` rend le chemin d'un catalogue de **30 000 produits**, généré une fois. Ouvre-le pour voir sa forme : `<product sku="…" category="book|music|game"><name>…</name><price>12.34</price></product>`. Un prix sur mille est entouré de blancs.
- `Data.BROKEN` est un petit document cassé ; `Data.SCENARIOS`, des besoins d'applications.

**Ce que TU crées :** tous les fichiers `.java`, dans le paquet `vorkurs02_xml.projects.p11_three`. La classe du `main` s'appelle **`ThreeParsers`**.

---

## Le problème

Le service comptable veut le **nombre de livres** et la **somme de leurs prix**. Tu écris ce calcul trois fois, en DOM, SAX et StAX, et tu dois obtenir **exactement** le même résultat. Puis tu mesures ce qui les distingue.

**Les prix** se convertissent en centimes, exactement : `new BigDecimal(prix.strip()).movePointRight(2).longValueExact()`. Pas de `double` : sur 10 000 additions, l'arrondi se verrait. Un total s'affiche `<n> livres, <somme> EUR`, la somme avec 2 décimales (`BigDecimal.valueOf(centimes, 2)`).

---

## Tableau de bord

### ☐ Étape 1 — La même tâche, trois fois (0.2.20 S1, S5)

- **DOM :** parse tout le catalogue, puis parcours les `product` ; pour ceux de catégorie `book`, le texte de leur `price`.
- **SAX :** au début d'un `product`, retiens s'il est un livre ; accumule le texte ; à la fin d'un `price`, s'il s'agit d'un livre, additionne.
- **StAX :** ta boucle `next()` ; sur l'ouverture d'un `product`, retiens s'il est un livre ; sur l'ouverture d'un `price` d'un livre, `getElementText()`.
```
TOTAL DOM 10000 livres, 505690.00 EUR
TOTAL SAX …
TOTAL STAX …
TOTAL identiques : true
```
- `identiques` : les trois résultats sont-ils égaux (un `record` avec `equals` automatique t'aide).

### ☐ Étape 2 — La mémoire (0.2.20 S2)

Compte tous les nœuds de l'arbre DOM : le document, puis récursivement chaque enfant (éléments, textes, blancs compris).
```
MEMOIRE DOM : 270003 noeuds en memoire | SAX, StAX : un produit a la fois
```
- **Question :** d'où viennent 9 nœuds par produit, alors qu'un produit n'a que 3 éléments ? Que donnerait un fichier de 40 Go ?

### ☐ Étape 3 — S'arrêter, trier (0.2.20 S3, S4)

- **StAX :** le premier produit dont le prix vaut au moins `99.00`. Compte les ouvertures d'éléments lues jusqu'à son `price` compris, puis **arrête-toi** (lecteur fermé dans un `finally`).
```
ARRET STAX premier prix >= 99.00 : P00005 apres 16 elements
```
- **DOM :** les 3 produits les plus chers, prix décroissant puis `sku` croissant à égalité.
```
TOP DOM 3 plus chers : [P07921, P17821, P27721]
```
- **Question :** pourquoi trier est-il le terrain naturel de DOM, et s'arrêter tôt celui de StAX ? Comment ferais-tu le top 3 en SAX, sans tout garder ?

### ☐ Étape 4 — Les erreurs, trois fois (0.2.20 S6)

Lis `Data.BROKEN` (avec un `StringReader` / `InputSource`) avec chacune des trois API :
- DOM et SAX : attrape `SAXParseException`. Pour DOM, donne au builder un `ErrorHandler` silencieux (`new DefaultHandler()`), sinon il écrit aussi `[Fatal Error]` sur la console ;
- StAX : compte les `next()` réussis, attrape `XMLStreamException`, sa ligne vient de `getLocation()`.
```
ERREUR DOM SAXParseException ligne 3
ERREUR STAX XMLStreamException ligne 3 apres 6 evenements deja lus
```
- **Question :** en StAX et en SAX, ton application a **déjà agi** sur une partie du document avant l'erreur. Pourquoi est-ce un problème pour un import ? Comment le régler (projet 13) ?

### ☐ Étape 5 — Choisir (0.2.20 S7)

Pour chaque scénario de `Data.SCENARIOS`, la règle, **dans cet ordre** :
1. il faut **modifier** le document → `DOM` ;
2. il faut **naviguer** librement et le fichier n'est **pas énorme** → `DOM` ;
3. il faut **s'arrêter tôt** → `STAX` ;
4. sinon : fichier énorme → `SAX`, petit fichier → `DOM`.
```
CHOIX migration de configuration -> DOM
```
- **Question :** le dernier scénario veut naviguer dans un fichier énorme. Que rend la règle ? Quelle autre solution existe (deux passes, ou un index construit en streaming) ?

### ☐ Étape 6 — Le `main` de `ThreeParsers`

Dans l'ordre : les 4 lignes TOTAL, MEMOIRE, ARRET, TOP, les 3 ERREUR, puis les CHOIX.

---

## Checklist API (vérifiée par `Check`)

| Élément | Étape | ☐ |
|---|---|---|
| `BigDecimal`, `movePointRight(2)`, `longValueExact()` | intro | ☐ |
| `DocumentBuilderFactory`, `getElementsByTagName(` | 1, 3 | ☐ |
| `SAXParserFactory`, `extends DefaultHandler` (ou une classe anonyme) | 1 | ☐ |
| `XMLInputFactory`, `getElementText()` | 1, 3 | ☐ |
| `getFirstChild()`, `getNextSibling()` | 2 | ☐ |
| `Comparator`, `thenComparing(` | 3 | ☐ |
| `SAXParseException`, `XMLStreamException`, `getLocation()`, `setErrorHandler(` | 4 | ☐ |
| `enum ` (les trois parseurs) | 5 | ☐ |
| ~~`XmlKit`~~, ~~`javax.xml.xpath`~~, ~~`javax.xml.validation`~~, ~~`double`~~ pour les prix | **interdits** | — |

---

## Sortie attendue complète

C'est le contrat exact que vérifie `Check` :

```
a remplir
```
