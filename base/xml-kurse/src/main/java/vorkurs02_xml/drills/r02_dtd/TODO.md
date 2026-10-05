# Drill de rappel 2 — DTD, bien formé contre valide (0.2.9 → 0.2.11)

> Première fois ? Lis d'abord [`PARCOURS.md`](../../PARCOURS.md) et les règles des drills [`drills/README.md`](../README.md).

**Chrono cible :** 20 min la 1re fois, puis 10 min aux répétitions.

**Règles :**
- De mémoire.
- Crée **`Recall02`**. Tous les documents tiennent sur **une seule ligne** : le DOCTYPE et son sous-ensemble interne, collés à la racine.
- Un verdict de validité = `XmlKit.validateDtd(doc, Map.of())`, sauf mention contraire.
- Une ligne par défi, préfixée `Dxx : `. Deux verdicts dans un défi sont séparés par ` | `.

## Défis

- ☐ **D01.** Une DTD interne : `r` contient un `a` puis, facultatif, un `b` ; `a` et `b` sont vides. Verdicts de `<r><a/></r>`, puis de `<r><b/><a/></r>`.
  → `D01 : VALID | INVALID 1 (ligne 1)`
- ☐ **D02.** Le document `<r/>`, sans DTD : verdict de bonne forme, puis de validité.
  → `D02 : OK | INVALID 2 (ligne 1)`
- ☐ **D03.** La DTD de D01 (écrite exactement `<!DOCTYPE r [<!ELEMENT r (a, b?)><!ELEMENT a EMPTY><!ELEMENT b EMPTY>]>`), suivie de `<r><a></r>`.
  → `D03 : NOT_WELL_FORMED 1:80`
- ☐ **D04.** Une DTD interne : `r` vide, avec trois attributs : `id` texte obligatoire ; `lang` parmi `fr` et `de`, défaut `fr` ; `v` texte fixé à `2`. Verdicts de `<r/>`, puis de `<r id="x"/>`.
  → `D04 : INVALID 1 (ligne 1) | VALID`
- ☐ **D05.** Avec la DTD de D04, sur `<r id="x"/>` : la valeur de `concat(/r/@lang, '-', /r/@v)`.
  → `D05 : fr-2`
- ☐ **D06.** Avec la DTD de D04 : `<r id="x" lang="en"/>`, puis `<r id="x" v="3"/>`.
  → `D06 : INVALID 1 (ligne 1) | INVALID 1 (ligne 1)`
- ☐ **D07.** Une DTD interne : `r` contient des `p` (zéro ou plus) ; `p` est vide, avec un `id` de type `ID` obligatoire et un `ref` de type `IDREF` facultatif. Verdicts de `<r><p id="a"/><p id="b" ref="a"/></r>`, puis de `<r><p id="a"/><p id="a" ref="z"/></r>`.
  → `D07 : VALID | INVALID 2 (ligne 1)`
- ☐ **D08.** Une DTD interne où `r` a un contenu **mixte** : du texte et des `b`, dans n'importe quel ordre ; `b` contient du texte. Verdict de `<r>x<b>y</b>z<b/></r>`.
  → `D08 : VALID`
- ☐ **D09.** Avec la DTD de D01 : `<r><a> </a></r>` (un espace dans `a`).
  → `D09 : INVALID 1 (ligne 1)`
- ☐ **D10.** `<!DOCTYPE x [<!ELEMENT r EMPTY>]><r/>` : bonne forme, puis validité.
  → `D10 : OK | INVALID 1 (ligne 1)`
- ☐ **D11.** Une DTD **externe** `r.dtd` (texte : `r` vide, `lang` parmi `fr`/`de`, défaut `fr`), et un document qui la désigne par `SYSTEM "r.dtd"` **et** ajoute un sous-ensemble interne redéclarant `lang` avec le défaut `de`. Le document : `<r/>`. Valide-le avec `Map.of("r.dtd", texte)`, puis affiche ` lang=` et la valeur relue (`string(/r/@lang)`).
  → `D11 : VALID lang=de`
- ☐ **D12.** Ta traduction d'un modèle de contenu en regex sur `nom;nom;` (projet 4) appliquée à `(a, (b | c)*, d?)`.
  → `D12 : (?:(?:a;)(?:(?:b;)|(?:c;))*(?:d;)?)`

## Sortie attendue complète

```
D01 : VALID | INVALID 1 (ligne 1)
D02 : OK | INVALID 2 (ligne 1)
D03 : NOT_WELL_FORMED 1:80
D04 : INVALID 1 (ligne 1) | VALID
D05 : fr-2
D06 : INVALID 1 (ligne 1) | INVALID 1 (ligne 1)
D07 : VALID | INVALID 2 (ligne 1)
D08 : VALID
D09 : INVALID 1 (ligne 1)
D10 : OK | INVALID 1 (ligne 1)
D11 : VALID lang=de
D12 : (?:(?:a;)(?:(?:b;)|(?:c;))*(?:d;)?)
```

## Carte mémoire (à lire **après** le drill)

<details><summary>Ouvrir la carte</summary>

| Point | À retenir |
|---|---|
| bien formé | règles WFC, vérifiées par **tout** parseur ; une erreur arrête tout |
| valide | bien formé **et** conforme à la DTD (règles VC) ; seul un parseur validant le vérifie |
| sans DTD | la validité n'est pas définie : un parseur validant signale « pas de grammaire » |
| modèles | `,` séquence, `\|` choix, `?` `*` `+` ; mixte : `(#PCDATA \| a \| b)*` ; `EMPTY`, `ANY` |
| ATTLIST | `CDATA`, `ID`, `IDREF`, `(a\|b)` ; `#REQUIRED`, `#IMPLIED`, `#FIXED "v"`, `"défaut"` |
| défauts | ajoutés par le parseur, même non validant, s'ils sont dans le sous-ensemble **interne** |
| interne + externe | l'interne est lu d'abord : premier ATTLIST gagne ; deux ELEMENT du même nom = erreur |
| DOCTYPE | son nom doit être celui de la racine (VC) |

</details>
