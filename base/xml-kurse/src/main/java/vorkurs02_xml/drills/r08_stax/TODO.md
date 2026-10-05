# Drill de rappel 8 — L'API StAX (0.2.19)

> Première fois ? Lis d'abord [`PARCOURS.md`](../../PARCOURS.md) et les règles des drills [`drills/README.md`](../README.md).

**Chrono cible :** 20 min la 1re fois, puis 10 min aux répétitions.

**Règles :**
- De mémoire. `XmlKit` est interdit.
- Crée **`Recall08`**. Le fichier : `vorkurs02_xml.drills.Data.campus()`, ouvert avec `Files.newInputStream` dans un `try`-avec-ressources. Une `XMLInputFactory` avec `SUPPORT_DTD` à `false`. Ferme chaque lecteur.
- Plusieurs défis peuvent partager une même lecture.
- Une ligne par défi, préfixée `Dxx : `.

## Défis

- ☐ **D01.** Le lecteur est-il sur `START_DOCUMENT` avant tout appel ? Puis, après `nextTag()`, le nom local courant.
  → `D01 : true campus`
- ☐ **D02.** Sur la racine : le nombre de déclarations de namespace, l'URI liée au préfixe `g`, puis l'attribut `year`.
  → `D02 : 2 urn:campus:grades 2026`
- ☐ **D03.** Le nombre total de `START_ELEMENT` du document (racine comprise).
  → `D03 : 25`
- ☐ **D04.** Les `id` des cours.
  → `D04 : [C1, C2, C3]`
- ☐ **D05.** Les textes des `title`, lus avec `getElementText()`.
  → `D05 : [Algorithmique, Bases de donnees, Reseaux]`
- ☐ **D06.** Le nombre de `person` qui ont un `email`.
  → `D06 : 3`
- ☐ **D07.** La ligne (`getLocation()`) de la première `person`.
  → `D07 : 22`
- ☐ **D08.** Après `nextTag()` (la racine) puis un `next()`, appelle `getAttributeValue(null, "id")` : le nom simple de l'exception, puis `isWhiteSpace()`.
  → `D08 : IllegalStateException true`
- ☐ **D09.** Avec un `XMLEventReader` : consomme le début du document, puis **regarde** l'événement suivant avec `peek()` (`commentaire`, `racine` ou `autre`). Puis le texte de chaque élément `grade` de `urn:campus:grades` (compare des `QName`).
  → `D09 : commentaire [15.5, 9, 12, 18, 7.5]`
- ☐ **D10.** La première note **sous 10** : le `ref` de son étudiant, ` en `, et l'`id` de son cours. **Sors de la boucle** dès qu'elle est trouvée.
  → `D10 : S2 en C1`

## Sortie attendue complète

```
D01 : true campus
D02 : 2 urn:campus:grades 2026
D03 : 25
D04 : [C1, C2, C3]
D05 : [Algorithmique, Bases de donnees, Reseaux]
D06 : 3
D07 : 22
D08 : IllegalStateException true
D09 : commentaire [15.5, 9, 12, 18, 7.5]
D10 : S2 en C1
```

## Carte mémoire (à lire **après** le drill)

<details><summary>Ouvrir la carte</summary>

| Point | À retenir |
|---|---|
| création | `XMLInputFactory.newFactory()` + propriétés (`SUPPORT_DTD`, `IS_SUPPORTING_EXTERNAL_ENTITIES`) → `createXMLStreamReader(in)` |
| curseur | commence sur `START_DOCUMENT` ; `next()` avance **puis** rend l'état ; `hasNext()` |
| états | `getLocalName` / `getAttributeValue` seulement sur `START_ELEMENT` (sinon `IllegalStateException`) |
| raccourcis | `nextTag()` saute blancs et commentaires ; `getElementText()` lit un élément texte, finit sur sa fermeture |
| namespaces | `getNamespaceURI()`, `getNamespaceURI(prefixe)`, `getNamespaceCount()`, `getName()` (un `QName`) |
| position | `getLocation().getLineNumber()` |
| événements | `XMLEventReader` : `nextEvent()`, `peek()`, `isStartElement()`, `asStartElement().getName()`, `asCharacters().getData()` |
| pull | on sort de la boucle quand on veut : pas d'exception, juste `break` / `return` (et `close()`) |

</details>
