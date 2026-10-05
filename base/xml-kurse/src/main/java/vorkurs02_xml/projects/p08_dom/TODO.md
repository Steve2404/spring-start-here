# Projet 8 — Le laboratoire DOM (cours 0.2.17)

> Première fois ? Lis d'abord le mode d'emploi [`vorkurs02_xml/PARCOURS.md`](../../PARCOURS.md) : comment lire cette fiche, lancer `Check`, quoi faire en cas de blocage.

**Notions visées :** DOM, un arbre d'objets en mémoire ; `DocumentBuilderFactory` configure, `DocumentBuilder` parse ; `Document`, `Element`, `Attr`, `Node` ; nœuds texte blancs, `NodeList` vivante ; DOM et namespaces ; modifier l'arbre ; le sérialiser avec un `Transformer` (0.2.17).  
**Ce qui est donné :** `Data.java` (`Data.file("playlist.xml")` rend le chemin d'un fichier de `files/`), les trois fichiers de `files/`, et `Check.java`.  
**Ce que TU crées :** tous les fichiers `.java`, dans le paquet `vorkurs02_xml.projects.p08_dom`. La classe du `main` s'appelle **`DomLab`**.

**À partir de ce projet, tu utilises les vraies API de Java.** `XmlKit` est **interdit** : l'arbitre, c'est maintenant le parseur que tu configures toi-même.

---

## Le problème

Trois fichiers, trois missions :
- `playlist.xml` : lire et naviguer, en voyant **tous** les nœuds, même invisibles ;
- `orders-ns.xml` : retrouver des éléments dans un document à plusieurs namespaces ;
- `customers-v1.xml` : un export CRM au format v1, à **migrer** vers le format v2, puis à écrire.

Ouvre les trois fichiers avant de commencer.

---

## Tableau de bord

### ☐ Étape 1 — Parser (0.2.17 S1, S2)

Une méthode qui parse un fichier de `files/`, **namespace-aware ou non** selon un paramètre :
- `DocumentBuilderFactory.newInstance()`, puis `setNamespaceAware(…)` ;
- `newDocumentBuilder()`, puis `parse(Data.file(nom).toFile())`.
- **Question :** quelle est la valeur **par défaut** de `setNamespaceAware` ? Pourquoi est-ce un piège (étape 4) ?

### ☐ Étape 2 — Naviguer dans la playlist (0.2.17 S3, S4)

Sur `playlist.xml` (namespace-aware) :
```
DOCUMENT PI COMMENT ELEMENT
RACINE playlist name=Dimanche | PI player [version="2"]
```
- `DOCUMENT` : la sorte de chaque enfant **du document** (`getFirstChild` / `getNextSibling`). Sortes : `ELEMENT`, `TEXT`, `CDATA`, `COMMENT`, `PI`, sinon `AUTRE` ;
- `RACINE` : le nom de l'élément racine et son attribut `name` ; puis la cible et les données de la PI (le premier enfant du document), les données entre crochets.

Puis une ligne par `track` (`getElementsByTagName`), dans l'ordre :
```
PISTE t1 "Blue in Green" 337s noeuds=7 elements=3 tags=[jazz, modal]
```
- le titre : `getTextContent()` de son `title` (balises internes et CDATA comprises) ;
- la durée `m:ss` convertie en secondes ;
- `noeuds` : tous ses nœuds enfants (`getChildNodes()`) ; `elements` : ses enfants **éléments** seulement ;
- `tags` : le texte de chaque `tag` descendant, au format `toString()` d'une `List`.

Enfin, l'attribut `rating` (absent) de la première piste :
```
ATTRIBUT absent : [] present=false
```
- **Question :** pourquoi `t1` a-t-il 7 nœuds pour 3 éléments, et `t3` 4 pour 4 ?
- **Question :** que rend `getAttribute` pour un attribut absent ? Comment le distinguer d'un attribut vide ?

### ☐ Étape 3 — La `NodeList` vivante (0.2.17 S4)

Sur le même document, la liste des `ad` (`getElementsByTagName("ad")`) :
1. Retire-les avec une boucle **naïve** : `for (int i = 0; i < liste.getLength(); i++)`, en retirant `item(i)` de son parent à chaque tour. Compte les tours.
```
PUBS boucle naive : 4 au depart, 2 retirees, reste 2
```
2. Puis la bonne méthode : **fige** d'abord les nœuds de la liste dans une `List<Node>`, et retire-les ensuite.
```
PUBS liste figee : 2 retirees, reste 0
```
- `reste` : `getLength()` de la **même** liste, relu après coup.
- **Question :** pourquoi la boucle naïve saute-t-elle un élément sur deux ? Quelle autre boucle aurait marché sans copie ?

### ☐ Étape 4 — DOM et namespaces (0.2.17 S5)

Sur `orders-ns.xml`, namespace-aware :
```
NS par nom ecrit : item=1 o:item=2
NS par URI : orders=1 common=1 other=1 tous=3
```
- `getElementsByTagName("item")`, puis `("o:item")` ;
- `getElementsByTagNameNS(uri, "item")` pour `urn:shop:orders`, `urn:shop:common`, `urn:other`, puis `"*"`.

Puis, pour chaque enfant élément de la racine :
```
NS A : nom=o:item prefixe=o local=item uri=urn:shop:orders
```
- `getTagName()`, `getPrefix()`, `getLocalName()`, `getNamespaceURI()` (un `null` s'affiche `null`).

Enfin, le **même** fichier parsé **sans** namespaces, son premier enfant élément de la racine :
```
NS sans namespaces : nom=o:item local=null uri=null
```
- **Question :** `A` et `C` s'écrivent tous deux `o:item`. Sont-ils le même nom ? Lequel des deux appels le montre ?

### ☐ Étape 5 — La migration v1 → v2 (0.2.17 S6)

Sur `customers-v1.xml`, namespace-aware. Dans **cet ordre** :
1. retire, partout dans le document, les nœuds texte **blancs** et les commentaires. Compte-les ;
   - retiens le frère suivant **avant** de retirer un nœud : un nœud détaché n'a plus de frère ;
2. renomme `customers` en `clients`, puis `customer` en `client` (`renameNode`, sans namespace). Compte-les ;
3. ajoute à la racine l'attribut `version="2"` ;
4. pour chaque `client` : s'il a un attribut `vip`, crée un élément `vip` qui contient sa valeur, place-le comme **premier** enfant, puis retire l'attribut. Compte les clients concernés ;
5. pour chaque élément enfant d'un `client` : son texte, blancs du début et de la fin retirés, toute suite de blancs réduite à un espace. Pour `email`, en plus, en minuscules (`Locale.ROOT`) ;
6. retire les éléments **vides** (aucun nœud enfant et aucun attribut), **du bas vers le haut**. Compte-les ;
7. trie les `client` par leur attribut `id` (ordre naturel des `String`). Re-ajouter un nœud déjà présent avec `appendChild` le **déplace**.
```
MIGRATION nettoyes=18 renommes=4 deplaces=2 vides=2
```
- **Question :** pourquoi faut-il normaliser le texte (5) **avant** de retirer les éléments vides (6) ? Pense à `<email></email>` et à un `<email> </email>`.

### ☐ Étape 6 — Sérialiser (0.2.17 S7)

`TransformerFactory.newInstance().newTransformer()`, **sans** déclaration XML (`OutputKeys.OMIT_XML_DECLARATION`) et **sans** indentation, d'un `DOMSource` vers un `StreamResult` sur un `StringWriter`. Affiche `V2 ` suivi du résultat, sur une seule ligne.
- **Question :** avant cette étape, qu'est-il arrivé au fichier `customers-v1.xml` sur le disque ?

### ☐ Étape 7 — Le `main` de `DomLab`

Dans l'ordre : étapes 2, 3, 4, puis 5 et 6.

---

## Checklist API (vérifiée par `Check`)

| Élément | Étape | ☐ |
|---|---|---|
| `DocumentBuilderFactory.newInstance()`, `setNamespaceAware(`, `newDocumentBuilder()` | 1 | ☐ |
| `getFirstChild()`, `getNextSibling()`, `getNodeType()`, `getDocumentElement()` | 2 | ☐ |
| `ProcessingInstruction`, `getTarget()`, `getData()` | 2 | ☐ |
| `getElementsByTagName(`, `getTextContent()`, `getChildNodes()`, `getAttribute(`, `hasAttribute(` | 2 | ☐ |
| `removeChild(`, `getParentNode()` | 3, 5 | ☐ |
| `getElementsByTagNameNS(`, `getPrefix()`, `getLocalName()`, `getNamespaceURI()` | 4 | ☐ |
| `renameNode(`, `setAttribute(`, `createElement(`, `insertBefore(`, `removeAttribute(`, `setTextContent(`, `hasChildNodes()`, `hasAttributes()`, `appendChild` | 5 | ☐ |
| `TransformerFactory`, `OutputKeys.OMIT_XML_DECLARATION`, `DOMSource`, `StreamResult` | 6 | ☐ |
| ~~`XmlKit`~~, ~~SAX~~, ~~StAX~~, ~~`javax.xml.xpath`~~, ~~`javax.xml.validation`~~ | **interdits** | — |

---

## Sortie attendue complète

C'est le contrat exact que vérifie `Check` :

```
a remplir
```
