# Drill de rappel 4 — Écrire du XSD (0.2.14 → 0.2.15)

> Première fois ? Lis d'abord [`PARCOURS.md`](../../PARCOURS.md) et les règles des drills [`drills/README.md`](../README.md).

**Chrono cible :** 25 min la 1re fois, puis 12 min aux répétitions.

**Règles :**
- De mémoire.
- Crée **`Recall04`**, avec deux outils :
  - une méthode qui écrit un schéma dans un **fichier temporaire** (`Files.createTempFile`, `Files.writeString`) et rend son chemin ;
  - une méthode qui valide un document avec `XmlKit.validateXsd(chemin, doc)` et rend **seulement les codes** des erreurs (chaque entrée est `ligne code` : garde ce qui suit l'espace).
- Sauf en D10, le schéma n'a **pas** de namespace cible : `<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"> … </xs:schema>`.
- Tous les documents tiennent sur une ligne.
- Un défi à deux documents affiche les deux listes de codes, séparées par un espace : d'abord le document correct, puis le fautif.
- Une ligne par défi, préfixée `Dxx : `.

## Défis

- ☐ **D01.** Un élément global `r` de type entier (`xs:int`). Documents `<r>5</r>` et `<r>x</r>`.
  → `D01 : [] [cvc-datatype-valid.1.2.1, cvc-type.3.1.3]`
- ☐ **D02.** `r` : un texte de deux majuscules puis un chiffre (type simple **anonyme**, `pattern`). `<r>AB1</r>` et `<r>AB12</r>`.
  → `D02 : [] [cvc-pattern-valid, cvc-type.3.1.3]`
- ☐ **D03.** `r` : un entier de 1 **inclus** à 10 **exclu**. `<r>1</r>` et `<r>10</r>`.
  → `D03 : [] [cvc-maxExclusive-valid, cvc-type.3.1.3]`
- ☐ **D04.** `r` : `rouge` ou `vert`. `<r>vert</r>` et `<r>Vert</r>`.
  → `D04 : [] [cvc-enumeration-valid, cvc-type.3.1.3]`
- ☐ **D05.** `r` contient un `a`, puis de 0 à 2 `b`, dans cet ordre (`a` et `b` sans type). `<r><a/></r>` et `<r><a/><b/><b/><b/></r>`. **Garde ce schéma** pour D11.
  → `D05 : [] [cvc-complex-type.2.4.e]`
- ☐ **D06.** `r` contient **soit** un `a`, **soit** un `b`. `<r><b/></r>` et `<r><a/><b/></r>`.
  → `D06 : [] [cvc-complex-type.2.4.d]`
- ☐ **D07.** `r` contient un `a` et un `b`, **dans n'importe quel ordre**, chacun une fois. `<r><b/><a/></r>` et `<r><a/><a/><b/></r>`.
  → `D07 : [] [cvc-complex-type.2.4.a]`
- ☐ **D08.** `r` vide, avec un attribut `id` (texte) **obligatoire** et un attribut `v` (texte) **fixé** à `2`. `<r/>`, puis `<r id="x" v="3"/>`.
  → `D08 : [cvc-complex-type.4] [cvc-complex-type.3.1]`
- ☐ **D09.** `prix` : un décimal **et** un attribut `devise` (texte). `<prix devise="EUR">9.5</prix>` et `<prix devise="EUR">neuf</prix>`.
  → `D09 : [] [cvc-datatype-valid.1.2.1, cvc-complex-type.2.2]`
- ☐ **D10.** Un schéma de namespace cible `urn:t`, `elementFormDefault="qualified"`, avec un élément global `r` texte. `<r xmlns="urn:t">x</r>` et `<r>x</r>`.
  → `D10 : [] [cvc-elt.1.a]`
- ☐ **D11.** Avec le schéma de D05 : le document `<a/>`.
  → `D11 : [cvc-elt.1.a]`

## Sortie attendue complète

```
D01 : [] [cvc-datatype-valid.1.2.1, cvc-type.3.1.3]
D02 : [] [cvc-pattern-valid, cvc-type.3.1.3]
D03 : [] [cvc-maxExclusive-valid, cvc-type.3.1.3]
D04 : [] [cvc-enumeration-valid, cvc-type.3.1.3]
D05 : [] [cvc-complex-type.2.4.e]
D06 : [] [cvc-complex-type.2.4.d]
D07 : [] [cvc-complex-type.2.4.a]
D08 : [cvc-complex-type.4] [cvc-complex-type.3.1]
D09 : [] [cvc-datatype-valid.1.2.1, cvc-complex-type.2.2]
D10 : [] [cvc-elt.1.a]
D11 : [cvc-elt.1.a]
```

## Carte mémoire (à lire **après** le drill)

<details><summary>Ouvrir la carte</summary>

| Point | À retenir |
|---|---|
| global / local | seul un élément enfant direct de `xs:schema` peut être une racine |
| type simple | `xs:restriction base=…` + facettes : `pattern` (ancré), `enumeration`, `min/maxInclusive`, `min/maxExclusive`, `fractionDigits`, `min/maxLength` |
| type complexe | `sequence` (ordre), `choice` (un seul), `all` (ordre libre, au plus 1) ; `minOccurs`/`maxOccurs` (1 par défaut, `unbounded`) |
| attributs | après le modèle ; `use="required"`, `default`, `fixed` |
| texte + attribut | `xs:simpleContent` → `xs:extension base=…` → `xs:attribute` |
| namespace | `targetNamespace` + `elementFormDefault="qualified"` ; sinon `cvc-elt.1.a` à la racine |
| codes | souvent par deux : la facette (`cvc-pattern-valid`), puis le type (`cvc-type.3.1.3`) |

</details>
