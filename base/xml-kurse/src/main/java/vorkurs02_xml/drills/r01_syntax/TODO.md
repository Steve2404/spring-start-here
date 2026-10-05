# Drill de rappel 1 — La syntaxe XML (0.2.2 → 0.2.8)

> Première fois ? Lis d'abord [`PARCOURS.md`](../../PARCOURS.md) et les règles des drills [`drills/README.md`](../README.md).

**Chrono cible :** 20 min la 1re fois, puis 10 min aux répétitions.

**Règles :**
- De mémoire.
- Crée **`Recall01`**.
- Dans les défis D01 à D09, **c'est toi qui écris le XML** (dans une `String`), puis le parseur dit ce qu'il relit, avec `XmlKit.wellFormed`, `XmlKit.value` ou `XmlKit.select`.
- Une ligne par défi, préfixée `Dxx : `.

## Défis

- ☐ **D01.** Un document : la déclaration complète (`version` 1.0, `encoding` UTF-8, `standalone` yes), puis une racine vide `r`. Son verdict, puis celui de la même déclaration où `standalone` passe **avant** `version`.
  → `D01 : OK KO 1:23`
- ☐ **D02.** Une racine `r` dont l'attribut `t`, entre guillemets **doubles**, vaut exactement `il dit "oui" & part`. Affiche sa valeur relue (`string(/r/@t)`).
  → `D02 : il dit "oui" & part`
- ☐ **D03.** Un élément `c` dont le texte vaut `a < b && c > d`, **sans** CDATA. Sa valeur relue.
  → `D03 : a < b && c > d`
- ☐ **D04.** Le même texte, écrit **dans une CDATA**. Les deux valeurs relues sont-elles égales ?
  → `D04 : true`
- ☐ **D05.** Une racine `r` avec deux attributs : `a` contient `x`, une **vraie** tabulation, `y` ; `b` contient `x`, la **référence** de la tabulation, `y`. Leurs valeurs relues, avec la tabulation affichée `\t`.
  → `D05 : x y x\ty`
- ☐ **D06.** La longueur (`string-length(/t)`) du texte de `<t>a` + `\r\n` + `b</t>`.
  → `D06 : 3`
- ☐ **D07.** Un DOCTYPE interne qui déclare l'entité `nom` valant `ACME`, puis une racine `r` dont le texte est `&nom; SA`. Sa valeur relue.
  → `D07 : ACME SA`
- ☐ **D08.** Un texte écrit **seulement** avec des références de caractères : `A` en décimal, puis `B` en hexadécimal. Sa valeur relue.
  → `D08 : AB`
- ☐ **D09.** Un document avec, **avant** la racine vide `r`, le commentaire ` note - ok ` puis la PI de cible `trace` et de données `on`. Ses nœuds de premier niveau (`select(doc, "/node()")`).
  → `D09 : [<!-- note - ok -->, <?trace on?>, r]`
- ☐ **D10.** Une méthode `isXmlName` (version simple : 1er caractère lettre, `_` ou `:` ; les suivants lettre, chiffre, `_`, `:`, `-`, `.`). Ses verdicts pour `prénom`, `1er`, `_a`, `a-b`, `.x`, `xml-data`, séparés par un espace.
  → `D10 : true false true true false true`
- ☐ **D11.** Une méthode qui échappe un **texte de contenu** (`&`, `<`, et `>` seulement après `]]`). Son résultat sur `x]]>y & <z>`.
  → `D11 : x]]&gt;y &amp; &lt;z>`
- ☐ **D12.** Une méthode de **lookahead** qui classe un `<` (`DECLARATION` seulement à l'indice 0, `DOCTYPE`, `COMMENT`, `CDATA`, `PI`, `END_TAG`, `START_TAG`). Applique-la à **chaque** `<` du texte `<?xml version="1.0"?><!DOCTYPE r><!--c--><r><![CDATA[x]]><?p d?></r>`.
  → `D12 : DECLARATION DOCTYPE COMMENT START_TAG CDATA PI END_TAG`

## Sortie attendue complète

```
D01 : OK KO 1:23
D02 : il dit "oui" & part
D03 : a < b && c > d
D04 : true
D05 : x y x\ty
D06 : 3
D07 : ACME SA
D08 : AB
D09 : [<!-- note - ok -->, <?trace on?>, r]
D10 : true false true true false true
D11 : x]]&gt;y &amp; &lt;z>
D12 : DECLARATION DOCTYPE COMMENT START_TAG CDATA PI END_TAG
```

## Carte mémoire (à lire **après** le drill)

<details><summary>Ouvrir la carte</summary>

| Point | À retenir |
|---|---|
| déclaration | facultative, mais au tout début ; `version`, puis `encoding`, puis `standalone` (yes/no) |
| contenu | `&` → `&amp;`, `<` → `&lt;` ; `>` libre sauf dans `]]>` |
| attribut | en plus, le guillemet qui délimite ; blancs littéraux → espaces, sauf écrits en références |
| fins de ligne | `\r\n` et `\r` deviennent `\n` à la lecture |
| références | `&#65;`, `&#x41;` (x minuscule) ; 5 entités prédéfinies seulement sans DTD |
| CDATA | rien n'y est reconnu ; ne peut pas contenir `]]>` |
| commentaire | jamais `--` dedans, ni `-` juste avant `-->` |
| PI | `<?cible données?>` ; la cible `xml` (toute casse) est réservée |
| lookahead | tester `<!--`, `<![CDATA[`, `<!DOCTYPE`, `<?`, `</` avant le simple `<` |

</details>
