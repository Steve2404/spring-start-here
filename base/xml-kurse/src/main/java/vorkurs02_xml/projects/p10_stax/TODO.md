# Projet 10 — Le quai de chargement (cours 0.2.19)

> Première fois ? Lis d'abord le mode d'emploi [`vorkurs02_xml/PARCOURS.md`](../../PARCOURS.md) : comment lire cette fiche, lancer `Check`, quoi faire en cas de blocage.

**Notions visées :** StAX et le pull streaming ; `XMLInputFactory`, `XMLStreamReader` créé de façon sûre ; états du curseur, `next()`, `hasNext()`, méthodes permises par état ; `getText()`, `getElementText()`, `nextTag()` ; namespaces, `QName` et attributs avec le curseur ; `XMLEventReader` ; erreurs, ressources, sécurité (0.2.19).  
**Ce qui est donné :** `Data.java` (`Data.file(nom)`), `files/manifest.xml`, `files/with-entity.xml`, et `Check.java`.  
**Ce que TU crées :** tous les fichiers `.java`, dans le paquet `vorkurs02_xml.projects.p10_stax`. La classe du `main` s'appelle **`StaxLab`**.

---

## Le problème

Le quai de chargement lit le manifeste du jour, `manifest.xml` (namespace `urn:logistics`) : des expéditions, et un bloc `audit` qui ne l'intéresse pas. Avec SAX, le parseur menait la danse. Avec StAX, **c'est toi** qui avances le curseur, décides de sauter un bloc ou de t'arrêter.

**Une méthode utilitaire** crée la fabrique : `XMLInputFactory.newFactory()`, puis `SUPPORT_DTD` à la valeur d'un paramètre, et `IS_SUPPORTING_EXTERNAL_ENTITIES` à `false`. Sauf à l'étape 7, le support des DTD est **désactivé**. Chaque lecture ouvre le fichier avec `Files.newInputStream(Data.file(nom))` dans un `try`-avec-ressources, et **ferme** son lecteur (`close()`) à la fin.

---

## Tableau de bord

### ☐ Étape 1 — Les états du curseur (0.2.19 S1, S3)

Sur `manifest.xml` :
- l'état **initial** du lecteur (`getEventType()`), avant tout `next()` ;
- puis chaque état rendu par `next()` tant que `hasNext()`.
```
DEBUT START_DOCUMENT START_ELEMENT:manifest CHARACTERS COMMENT CHARACTERS START_ELEMENT:shipment
COMPTES {CHARACTERS=27, COMMENT=2, END_DOCUMENT=1, END_ELEMENT=17, START_ELEMENT=17}
```
- `DEBUT` : l'état initial puis les 5 premiers états rendus. Noms : `START_DOCUMENT`, `START_ELEMENT`, `END_ELEMENT`, `CHARACTERS`, `COMMENT`, `SPACE`, `DTD`, `END_DOCUMENT`, sinon `AUTRE`. Sur un `START_ELEMENT` ou un `END_ELEMENT`, ajoute `:` et le nom local ;
- `COMPTES` : le nombre de chaque état rendu par `next()` (sans l'état initial), au format `toString()` d'une `TreeMap`.
- **Question :** pourquoi `START_DOCUMENT` n'apparaît-il pas dans les comptes ?

### ☐ Étape 2 — Namespaces et attributs (0.2.19 S5)

Avance jusqu'à la racine avec `nextTag()` :
```
RACINE {urn:logistics}manifest prefixe=[] declarations=1 defaut=urn:logistics attributs=1 date=2026-09-29
```
- `getName()` (un `QName`), `getPrefix()` entre crochets, `getNamespaceCount()`, `getNamespaceURI(0)`, `getAttributeCount()`, puis `getAttributeValue(null, "date")`.

### ☐ Étape 3 — Lire les expéditions, sauter le reste (0.2.19 S4)

Une lecture complète du manifeste :
1. `nextTag()` pour atteindre la racine ;
2. tant que `nextTag()` rend un `START_ELEMENT` (un enfant de la racine) :
   - une `shipment` du namespace `urn:logistics` : lis-la ;
   - tout autre élément : **saute-le en entier**, en comptant la profondeur (+1 à chaque ouverture, −1 à chaque fermeture, arrêt à 0).
3. **Lire une expédition** (le curseur est sur sa balise ouvrante) :
   - sa ligne : `getLocation().getLineNumber()` ; ses attributs `id` et `destination` ;
   - tant que `nextTag()` rend un `START_ELEMENT` : un `item` (attribut `sku`). Dans l'`item`, tant que `nextTag()` rend un `START_ELEMENT` : `qty` (un entier), ou `weight` (un nombre avec un attribut `unit` : `g` → divise par 1000, sinon kg). Lis chaque valeur avec `getElementText()`, blancs retirés.
```
EXPEDITION S1 Lyon ligne 4 : 2 article(s), 3.25 kg
```
- le poids total = la somme de `qty × poids unitaire`, avec 2 décimales (`Locale.ROOT`).
- **Question :** où est le curseur après `getElementText()` ? Pourquoi est-ce exactement ce qu'il faut pour enchaîner un `nextTag()` ?
- **Question :** le commentaire au milieu de l'`item` de `S2` gêne-t-il `nextTag()` ?

### ☐ Étape 4 — S'arrêter dès qu'on a la réponse (0.2.19 S1, S7)

La première expédition dont le poids total dépasse une limite. Parcours avec `next()`, et à chaque ouverture d'une `shipment`, lis-la (étape 3) et compte-la. Dès qu'elle dépasse : rends-la, et **arrête** (le lecteur est fermé dans un `finally`).
```
PLUS LOURDE QUE 5 kg : S2 (apres 2 expedition(s))
PLUS LOURDE QUE 100 kg : aucune (3 expeditions lues)
```
- **Question :** en SAX (projet 9), il fallait une exception pour s'arrêter. Pourquoi pas ici ?

### ☐ Étape 5 — Les pièges du curseur (0.2.19 S3, S4)

Sur un nouveau lecteur : `nextTag()` (la racine), puis **un** `next()` (le texte blanc qui suit), puis `getLocalName()`. Attrape l'exception et note son nom simple. Puis avance jusqu'au premier `entry` (contenu mixte : du texte **et** un `<b>`) et appelle `getElementText()`. Attrape, note.
```
PIEGES getLocalName sur du texte : IllegalStateException | getElementText sur contenu mixte : XMLStreamException
```

### ☐ Étape 6 — L'API événements (0.2.19 S6)

Un `XMLEventReader` sur le manifeste. Consomme deux événements (le début du document, la racine), puis **regarde** le suivant avec `peek()`, sans le consommer : `blanc` si ce sont des caractères blancs, sinon `autre`. Puis, pour chaque `StartElement` dont le nom est égal au `QName` (`urn:logistics`, `shipment`), son attribut `destination` (`getAttributeByName`, avec un `QName` **sans** namespace).
```
EVENEMENTS apres la racine : blanc | destinations [Lyon, Paris, Nice]
```
- **Question :** qu'apporte un événement **objet** par rapport à un état du curseur ?

### ☐ Étape 7 — DTD et entités (0.2.19 S2, S7)

Lis `with-entity.xml` deux fois : avec `SUPPORT_DTD` à `true`, puis à `false`. Concatène le texte de chaque `CHARACTERS` et compte-les. Attrape `XMLStreamException`.
```
DTD SUPPORT_DTD=true : [valeur secret] en 2 morceau(x)
DTD SUPPORT_DTD=false : XMLStreamException
```
- **Question :** pourquoi refuser les DTD est-il le bon réglage par défaut pour un flux venu de l'extérieur ? (Projet 12.)

### ☐ Étape 8 — Le `main` de `StaxLab`

Dans l'ordre : DEBUT, COMPTES, RACINE, les EXPEDITION, les deux PLUS LOURDE, PIEGES, EVENEMENTS, puis les deux DTD.

---

## Checklist API (vérifiée par `Check`)

| Élément | Étape | ☐ |
|---|---|---|
| `XMLInputFactory.newFactory()`, `SUPPORT_DTD`, `IS_SUPPORTING_EXTERNAL_ENTITIES` | intro | ☐ |
| `createXMLStreamReader(`, `getEventType()`, `hasNext()`, `next()`, `XMLStreamConstants`, `close()` | 1 | ☐ |
| `getName()`, `getPrefix()`, `getNamespaceCount()`, `getAttributeCount()`, `getAttributeValue(null` | 2 | ☐ |
| `nextTag()`, `getElementText()`, `getLocation()` | 3 | ☐ |
| `IllegalStateException`, `XMLStreamException` | 5 | ☐ |
| `createXMLEventReader(`, `nextEvent()`, `peek()`, `StartElement`, `getAttributeByName(`, `QName` | 6 | ☐ |
| ~~`XmlKit`~~, ~~DOM~~, ~~SAX~~, ~~XPath~~, ~~validation~~ | **interdits** | — |

---

## Sortie attendue complète

C'est le contrat exact que vérifie `Check` :

```
a remplir
```
