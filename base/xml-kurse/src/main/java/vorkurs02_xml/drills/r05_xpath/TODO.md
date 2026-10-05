# Drill de rappel 5 — Écrire du XPath (0.2.16)

> Première fois ? Lis d'abord [`PARCOURS.md`](../../PARCOURS.md) et les règles des drills [`drills/README.md`](../README.md).

**Chrono cible :** 20 min la 1re fois, puis 10 min aux répétitions.

**Règles :**
- De mémoire.
- Crée **`Recall05`**. Le document : `vorkurs02_xml.drills.Data.campusText()`. Ouvre `drills/files/campus.xml` avant de commencer.
- Lie **toi-même** deux préfixes : `c` → `urn:campus` et `g` → `urn:campus:grades`, et passe-les à chaque évaluation.
- Une **valeur** : `XmlKit.value(doc, expr, préfixes)`. Des **nœuds** : `XmlKit.select(doc, expr, préfixes)`, rendus joints par `, `.
- **Une seule expression XPath par défi** (deux en D14). Une ligne par défi, préfixée `Dxx : `.

## Défis

- ☐ **D01.** Les nœuds texte des titres de tous les cours.
  → `D01 : "Algorithmique", "Bases de donnees", "Reseaux"`
- ☐ **D02.** Le nombre d'étudiants du **premier** cours.
  → `D02 : 3`
- ☐ **D03.** L'attribut `ref` du **2e étudiant de chaque cours** (des nœuds).
  → `D03 : @ref=S2, @ref=S4`
- ☐ **D04.** La valeur du `ref` du **2e étudiant de tout le document**.
  → `D04 : S2`
- ☐ **D05.** L'`id` du cours qui n'a **aucun** étudiant.
  → `D05 : C3`
- ☐ **D06.** La somme des notes du cours `C1`.
  → `D06 : 36.5`
- ☐ **D07.** La moyenne de **toutes** les notes, calculée dans l'expression.
  → `D07 : 12.4`
- ☐ **D08.** L'enseignant du cours où est inscrit l'étudiant `S4`, en **remontant** depuis cet étudiant.
  → `D08 : Martin`
- ☐ **D09.** L'`id` du **dernier** cours.
  → `D09 : C3`
- ☐ **D10.** Les nœuds texte des personnes **sans** email.
  → `D10 : "Ben"`
- ☐ **D11.** Le nom de la personne qui a la **meilleure** note (une note dont aucune autre n'est plus grande), par une jointure entre `student/@ref` et `person/@id`.
  → `D11 : Ana`
- ☐ **D12.** Les attributs `id` des cours qui **suivent** `C1` parmi ses frères.
  → `D12 : @id=C2, @id=C3`
- ☐ **D13.** Le nombre d'éléments **ancêtres** de la première note.
  → `D13 : 3`
- ☐ **D14.** La partie avant `@` de l'email de `S1`, un espace, puis `<nom du campus> (<année>)` construit avec `concat`.
  → `D14 : ana Campus Lyon (2026)`

## Sortie attendue complète

```
D01 : "Algorithmique", "Bases de donnees", "Reseaux"
D02 : 3
D03 : @ref=S2, @ref=S4
D04 : S2
D05 : C3
D06 : 36.5
D07 : 12.4
D08 : Martin
D09 : C3
D10 : "Ben"
D11 : Ana
D12 : @id=C2, @id=C3
D13 : 3
D14 : ana Campus Lyon (2026)
```

## Carte mémoire (à lire **après** le drill)

<details><summary>Ouvrir la carte</summary>

| Point | À retenir |
|---|---|
| `/` et `//` | `/a/b` : enfants directs ; `//b` : à n'importe quelle profondeur ; `a//b` : descendants de `a` |
| position | `x[2]` : le 2e **par parent** ; `(//x)[2]` : le 2e du document ; `[last()]` |
| prédicats | `[@a]`, `[@a='v']`, `[enfant]`, `[not(…)]`, `[enfant = 'v']` |
| axes | `parent::` (`..`), `ancestor::` (inverse : `[1]` = le plus proche), `following-sibling::`, `preceding::` |
| node-sets | `A = B` vrai si **une** valeur de A égale **une** de B : c'est une jointure |
| fonctions | `count`, `sum`, `div`, `not`, `concat`, `substring-before`, `normalize-space`, `translate`, `string-length` |
| namespaces | un nom sans préfixe = sans namespace ; lier ses préfixes soi-même |

</details>
