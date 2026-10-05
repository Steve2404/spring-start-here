# Drill de rappel 6 — L'API DOM (0.2.17)

> Première fois ? Lis d'abord [`PARCOURS.md`](../../PARCOURS.md) et les règles des drills [`drills/README.md`](../README.md).

**Chrono cible :** 20 min la 1re fois, puis 10 min aux répétitions.

**Règles :**
- De mémoire. `XmlKit` est interdit.
- Crée **`Recall06`**. Parse `vorkurs02_xml.drills.Data.campus()` avec un DOM **namespace-aware**. Garde le `DocumentBuilder` (D10).
- Les défis s'enchaînent sur le **même** document : D08 et D09 le modifient.
- Une ligne par défi, préfixée `Dxx : `.

## Défis

- ☐ **D01.** Le nom du nœud document, le nom de la racine, puis son attribut `name`.
  → `D01 : #document campus Campus Lyon`
- ☐ **D02.** Le nombre d'éléments `course` (par nom écrit), puis de `grade` du namespace `urn:campus:grades`.
  → `D02 : 3 5`
- ☐ **D03.** Les `id` des cours, dans une liste.
  → `D03 : [C1, C2, C3]`
- ☐ **D04.** Pour le 1er puis le 3e cours : `hasAttribute("status")`, puis `getAttribute("status")` entre crochets.
  → `D04 : false [] true [closed]`
- ☐ **D05.** Le nom du **premier nœud enfant** du 1er cours, puis le nom de son premier enfant **élément**.
  → `D05 : #text title`
- ☐ **D06.** Le nombre de nœuds enfants du 1er cours, puis le nom de son parent.
  → `D06 : 11 campus`
- ☐ **D07.** Pour le `title` du 1er cours : `getNodeValue()` de l'élément, `getNodeValue()` de son premier enfant, puis `getTextContent()`.
  → `D07 : null Algorithmique Algorithmique`
- ☐ **D08.** Ajoute à la racine un `course` créé avec `createElement`. Compte les `course` par nom écrit, puis ceux de `urn:campus`. Ajoute ensuite un `course` créé avec `createElementNS("urn:campus", …)` et recompte ceux de `urn:campus`.
  → `D08 : 4 3 4`
- ☐ **D09.** Remplace le `title` du 1er cours par un nouveau `title` (de `urn:campus`) de texte `Algo 2`. Retire l'élément `people`. Affiche le texte du `title` du 1er cours, le nom du nœud retiré, puis le nombre de `person` restant dans le document.
  → `D09 : Algo 2 people 0`
- ☐ **D10.** Une copie profonde du 3e cours : a-t-elle un parent ? Puis importe le 3e cours dans un **nouveau** document (`builder.newDocument()`), dont il devient la racine : son propriétaire est-il ce nouveau document ? Son `id` ?
  → `D10 : true true C3`
- ☐ **D11.** Le nombre d'attributs de la racine, selon le DOM.
  → `D11 : 4`
- ☐ **D12.** Le nouveau document de D10, sérialisé sans déclaration, avec chaque suite de blancs réduite à un espace.
  → `D12 : <course credits="6" id="C3" level="L2" status="closed" xmlns="urn:campus"> <title>Reseaux</title> <teacher>Dupont</teacher> </course>`

## Sortie attendue complète

```
D01 : #document campus Campus Lyon
D02 : 3 5
D03 : [C1, C2, C3]
D04 : false [] true [closed]
D05 : #text title
D06 : 11 campus
D07 : null Algorithmique Algorithmique
D08 : 4 3 4
D09 : Algo 2 people 0
D10 : true true C3
D11 : 4
D12 : <course credits="6" id="C3" level="L2" status="closed" xmlns="urn:campus"> <title>Reseaux</title> <teacher>Dupont</teacher> </course>
```

## Carte mémoire (à lire **après** le drill)

<details><summary>Ouvrir la carte</summary>

| Point | À retenir |
|---|---|
| fabrique | `setNamespaceAware(false)` par défaut ! |
| document | `getDocumentElement()` = la racine ; le `Document` est son parent |
| recherche | `getElementsByTagName(nomÉcrit)`, `getElementsByTagNameNS(uri, local)` ; `"*"` = tous |
| attributs | `getAttribute` → `""` si absent ; `hasAttribute` ; les `xmlns` sont des attributs du DOM |
| enfants | `getFirstChild` / `getNextSibling` voient les textes blancs ; `getChildNodes()` aussi |
| valeurs | `getNodeValue()` : `null` sur un élément ; `getTextContent()` concatène tout |
| créer | `createElement` = sans namespace ; `createElementNS(uri, nom)` sinon ; puis `appendChild` |
| modifier | `replaceChild(nouveau, ancien)`, `parent.removeChild(n)`, `insertBefore(n, ref)` |
| copier | `cloneNode(true)` (même document, sans parent) ; `importNode(n, true)` vers un autre document |
| écrire | `Transformer` : `DOMSource` → `StreamResult` ; `OutputKeys.OMIT_XML_DECLARATION`, `INDENT` |

</details>
