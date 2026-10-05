# Projet 9 — La station de mesure en flux (cours 0.2.18)

> Première fois ? Lis d'abord le mode d'emploi [`vorkurs02_xml/PARCOURS.md`](../../PARCOURS.md) : comment lire cette fiche, lancer `Check`, quoi faire en cas de blocage.

**Notions visées :** SAX, des événements au lieu d'un arbre ; `SAXParserFactory`, `SAXParser`, `DefaultHandler` ; `startElement`, `characters`, `endElement` ; `characters()` livre des **morceaux** ; garder soi-même le contexte et construire ses résultats ; namespaces, noms et attributs en SAX ; erreurs et arrêt anticipé (0.2.18).  
**Ce qui est donné :** `Data.java` (`Data.file(nom)`), les fichiers de `files/`, et `Check.java`.  
**Ce que TU crées :** tous les fichiers `.java`, dans le paquet `vorkurs02_xml.projects.p09_sax`. La classe du `main` s'appelle **`SaxLab`**.

---

## Le problème

Une station météo envoie `readings.xml` : plus de cent mesures, des notes, des alertes, dans le namespace `urn:sensors`. Le vrai flux en contient des millions : on ne construit **pas** d'arbre. Avec SAX, le parseur appelle **tes** méthodes au fil de la lecture, et ne garde rien en mémoire.

**Une méthode utilitaire** crée un `SAXParser` : `SAXParserFactory.newInstance()`, puis `setNamespaceAware(…)` selon un paramètre, puis la fonctionnalité `http://apache.org/xml/features/disallow-doctype-decl` à `true` (ce flux n'a jamais de DOCTYPE), puis `newSAXParser()`. Tous les parsings du projet passent par elle. Les fichiers se lisent avec `parse(Data.file(nom).toFile(), handler)`.

---

## Tableau de bord

### ☐ Étape 1 — Voir les événements (0.2.18 S3, S4)

Un handler (namespace-aware) qui note les événements de la **première** `reading` seulement, de son ouverture à sa fermeture comprises :
- `start <nom local>`, `end <nom local>` ;
- chaque appel à `characters` : `texte [<le morceau reçu>]`.
```
TRACE start reading | start temp | texte [21.5] | …
```
- les événements joints par ` | `.
- **Question :** combien d'appels `characters` reçoit la note `Tom &amp; Jerry` ? Qu'en conclure pour ton code ?

### ☐ Étape 2 — Noms et attributs (0.2.18 S6)

Pour le **premier** `startElement` (la racine), avec puis sans namespaces :
```
RACINE avec namespaces : uri=[urn:sensors] local=[readings] qName=[m:readings] attributs=1 station=Lyon
```
- `uri`, `localName`, `qName` entre crochets ; le nombre d'attributs (`getLength()`) ; puis le nom du **premier** attribut (`getQName(0)`), `=`, et la valeur de l'attribut `station` (`getValue("station")`).
- **Question :** sans namespaces, combien d'attributs la racine a-t-elle, et lequel est le premier ? Pourquoi ?

### ☐ Étape 3 — Agréger en flux (0.2.18 S4, S5)

Un handler à toi, qui **est** le résultat. Il ne regarde que les éléments du namespace `urn:sensors` pour ses résultats, mais compte tous les éléments.
- **Ouverture** d'un élément : profondeur +1 (la racine est à 1), compte +1, et **vide** le tampon de texte. Sur `reading` : mémorise le capteur (attribut `sensor`) et une note vide. Sur `temp` : mémorise l'unité (attribut `unit`).
- **`characters`** : **ajoute** le morceau au tampon.
- **Fermeture**, profondeur −1, puis selon l'élément :
  - `temp` : la valeur du tampon (blancs retirés) ; si l'unité est `F`, convertis en °C par `(v − 32) × 5 / 9` ;
  - `note` : le tampon, tel quel ;
  - `reading` : une mesure (capteur, °C, note) de plus ;
  - `msg` : une alerte, le tampon sans blancs autour.
```
LECTURES 103 elements=213 profondeur=3
MOYENNE s1 22.50 C sur 2
NOTES [Tom & Jerry, capteur <nettoye>]
ALERTES [Surchauffe salle B, Batterie faible]
```
- `MOYENNE` : une ligne par capteur, triés par nom ; la moyenne des °C avec 2 décimales (`String.format(Locale.ROOT, "%.2f", …)`), puis le nombre de mesures ;
- `NOTES` : les notes **non vides**, dans l'ordre ; `ALERTES` : les alertes, dans l'ordre (le `toString()` d'une `List`).
- **Question :** pourquoi vider le tampon à **l'ouverture** et le lire à la **fermeture** ?
- **Question :** la note en CDATA arrive-t-elle différemment de l'autre ?

### ☐ Étape 4 — S'arrêter tôt (0.2.18 S7)

On veut seulement la **première** alerte, sans lire la suite du flux. SAX ne s'arrête pas tout seul : lance une exception de contrôle (une sous-classe de `SAXException` que tu définis) depuis `endElement` du premier `msg`, et attrape-la **autour** de `parse`. Compte les ouvertures d'éléments vues jusque-là.
```
PREMIERE ALERTE Surchauffe salle B apres 11 elements
```

### ☐ Étape 5 — Les erreurs (0.2.18 S7)

Parse, avec un `DefaultHandler` vide, `broken.xml`, `with-doctype.xml` puis `readings.xml`. Attrape `SAXParseException` :
```
ERREUR broken.xml : ligne 5 colonne 12
ERREUR readings.xml : aucune
```
- **Question :** `broken.xml` est cassé à la ligne 4. Pourquoi le parseur parle-t-il de la ligne 5 ?
- **Question :** `with-doctype.xml` est bien formé. Pourquoi est-il refusé, et pourquoi est-ce une bonne politique (projet 12) ?

### ☐ Étape 6 — Le `main` de `SaxLab`

Dans l'ordre : TRACE, les deux RACINE (avec, puis sans), LECTURES, MOYENNE, NOTES, ALERTES, PREMIERE ALERTE, puis les trois ERREUR.

---

## Checklist API (vérifiée par `Check`)

| Élément | Étape | ☐ |
|---|---|---|
| `SAXParserFactory.newInstance()`, `setNamespaceAware(`, `setFeature(`, `newSAXParser()` | intro | ☐ |
| `extends DefaultHandler` | 1, 3 | ☐ |
| `startElement(`, `characters(`, `endElement(` | 1, 3 | ☐ |
| `Attributes`, `getValue(`, `getLength()`, `getQName(` | 2, 3 | ☐ |
| `StringBuilder`, `setLength(0)` | 3 | ☐ |
| `extends SAXException` | 4 | ☐ |
| `SAXParseException`, `getLineNumber()`, `getColumnNumber()` | 5 | ☐ |
| ~~`XmlKit`~~, ~~DOM (`DocumentBuilder`)~~, ~~StAX~~, ~~XPath~~, ~~validation~~ | **interdits** | — |

---

## Sortie attendue complète

C'est le contrat exact que vérifie `Check` :

```
a remplir
```
