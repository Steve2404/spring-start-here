# Drill de rappel 7 — L'API SAX (0.2.18)

> Première fois ? Lis d'abord [`PARCOURS.md`](../../PARCOURS.md) et les règles des drills [`drills/README.md`](../README.md).

**Chrono cible :** 25 min la 1re fois, puis 12 min aux répétitions.

**Règles :**
- De mémoire. `XmlKit` est interdit.
- Crée **`Recall07`**. Le fichier : `vorkurs02_xml.drills.Data.campus()`. Les parseurs sont namespace-aware, sauf en D04.
- Plusieurs défis peuvent partager un même passage SAX.
- Une ligne par défi, préfixée `Dxx : `.

## Défis

- ☐ **D01.** Le nombre d'appels à `startElement`.
  → `D01 : 25`
- ☐ **D02.** Pour la racine : `uri`, `localName`, `qName`, séparés par un espace.
  → `D02 : urn:campus campus campus`
- ☐ **D03.** Pour le premier élément de nom local `grade` : `qName`, `localName`, `uri`.
  → `D03 : g:grade grade urn:campus:grades`
- ☐ **D04.** **Sans** namespaces, pour le premier élément de `qName` `g:grade` : `uri` et `localName` entre crochets, puis `qName`.
  → `D04 : [] [] g:grade`
- ☐ **D05.** Pour le cours d'`id` `C1` : le nombre d'attributs, le nom du premier, puis la valeur de `credits`.
  → `D05 : 3 id 6`
- ☐ **D06.** Le texte de chaque `title`, dans une liste (accumule le texte entre l'ouverture et la fermeture).
  → `D06 : [Algorithmique, Bases de donnees, Reseaux]`
- ☐ **D07.** La moyenne des notes de chaque cours (retiens le cours **courant** à son ouverture), triés par `id`, au format `id=moyenne` avec 2 décimales (`Locale.ROOT`), séparés par un espace. Un cours sans note n'apparaît pas.
  → `D07 : C1=12.17 C2=12.75`
- ☐ **D08.** La profondeur maximale (la racine est à 1).
  → `D08 : 4`
- ☐ **D09.** Le texte de la première `person` **sans** attribut `email`, puis le nombre d'ouvertures d'éléments vues jusque-là. Arrête le parseur dès qu'elle est trouvée.
  → `D09 : Ben apres 23 elements`
- ☐ **D10.** Parse `<a><b></a>` (un `StringReader` dans une `InputSource`) : la ligne et la colonne de l'erreur.
  → `D10 : 1:9`

## Sortie attendue complète

```
D01 : 25
D02 : urn:campus campus campus
D03 : g:grade grade urn:campus:grades
D04 : [] [] g:grade
D05 : 3 id 6
D06 : [Algorithmique, Bases de donnees, Reseaux]
D07 : C1=12.17 C2=12.75
D08 : 4
D09 : Ben apres 23 elements
D10 : 1:9
```

## Carte mémoire (à lire **après** le drill)

<details><summary>Ouvrir la carte</summary>

| Point | À retenir |
|---|---|
| mise en place | `SAXParserFactory.newInstance()` → `setNamespaceAware` → `newSAXParser()` → `parse(fichier, handler)` |
| handler | `extends DefaultHandler` : `startElement`, `characters`, `endElement`, `startDocument`, `endDocument` |
| noms | avec namespaces : `uri` + `localName` ; sans : seul `qName` est rempli |
| attributs | `Attributes` : `getLength()`, `getQName(i)`, `getValue(nom)` (`null` si absent) |
| texte | `characters` livre des **morceaux** : vider à l'ouverture, accumuler, lire à la fermeture |
| état | SAX ne garde rien : le handler mémorise le contexte (élément courant, pile, compteurs) |
| s'arrêter | lancer une sous-classe de `SAXException` depuis le handler, l'attraper autour de `parse` |
| erreurs | `SAXParseException` : `getLineNumber()`, `getColumnNumber()` (le point de découverte) |

</details>
