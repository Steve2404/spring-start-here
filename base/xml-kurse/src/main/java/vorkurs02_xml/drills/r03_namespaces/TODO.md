# Drill de rappel 3 — Les namespaces (0.2.12 → 0.2.13)

> Première fois ? Lis d'abord [`PARCOURS.md`](../../PARCOURS.md) et les règles des drills [`drills/README.md`](../README.md).

**Chrono cible :** 15 min la 1re fois, puis 8 min aux répétitions.

**Règles :**
- De mémoire.
- Crée **`Recall03`**. Le document partagé `campus.xml` se lit avec `vorkurs02_xml.drills.Data.campusText()`. Ouvre-le (`drills/files/campus.xml`) avant de commencer.
- Les valeurs viennent de `XmlKit.value(doc, expression)` (fonctions XPath `namespace-uri(…)`, `local-name(…)`, `name(…)`) ; les verdicts, de `XmlKit.wellFormed` / `wellFormedNs`.
- Une valeur qui peut être vide s'affiche entre crochets.
- Une ligne par défi, préfixée `Dxx : `.

## Défis

- ☐ **D01.** L'URI de namespace puis le nom local de la racine de `campus.xml`.
  → `D01 : urn:campus campus`
- ☐ **D02.** L'URI du premier élément dont le nom local est `grade`.
  → `D02 : urn:campus:grades`
- ☐ **D03.** L'URI de l'attribut `ref` du premier élément de nom local `student`.
  → `D03 : []`
- ☐ **D04.** Écris une racine `r` avec un namespace **par défaut** `urn:x` et un attribut `a="1"`. Les URI de `r`, puis de `a`.
  → `D04 : [urn:x] []`
- ☐ **D05.** Écris une racine `r` dans le défaut `urn:x`, qui contient un enfant `c` qui **annule** le défaut. L'URI de `c`.
  → `D05 : []`
- ☐ **D06.** Écris une racine `p:r` (`p` lié à `urn:a`) qui contient `p:c`, où `p` est **redéfini** en `urn:b`. L'URI de `c`.
  → `D06 : urn:b`
- ☐ **D07.** Deux documents à racine vide `r` dans `urn:same` : l'un avec le préfixe `x`, l'autre avec le défaut. Leurs noms étendus (URI + nom local) sont-ils égaux ? Puis leurs noms **écrits** (`name(/*)`).
  → `D07 : true x:r r`
- ☐ **D08.** `<a:r/>` : verdict XML 1.0, puis avec namespaces.
  → `D08 : OK KO 1:7`
- ☐ **D09.** `<r xmlns:xml="urn:autre"/>` avec namespaces.
  → `D09 : KO 1:25`
- ☐ **D10.** Sur `campus.xml`, le nombre de `course` : en liant **toi-même** le préfixe `c` à `urn:campus` (`XmlKit.value(doc, expr, Map.of("c", …))`), puis avec `count(//course)` sans préfixe.
  → `D10 : 3 0`
- ☐ **D11.** Une méthode qui rend le nom étendu `{uri}local` d'un QName, avec des liaisons données (`""` = le défaut) et un drapeau « élément ou attribut ». Avec les liaisons `"" → urn:d` et `m → urn:money` : la valeur `xsi:type` `m:Euro`, la valeur `Euro`, puis un **attribut** `rate`.
  → `D11 : {urn:money}Euro {urn:d}Euro rate`

## Sortie attendue complète

```
D01 : urn:campus campus
D02 : urn:campus:grades
D03 : []
D04 : [urn:x] []
D05 : []
D06 : urn:b
D07 : true x:r r
D08 : OK KO 1:7
D09 : KO 1:25
D10 : 3 0
D11 : {urn:money}Euro {urn:d}Euro rate
```

## Carte mémoire (à lire **après** le drill)

<details><summary>Ouvrir la carte</summary>

| Point | À retenir |
|---|---|
| identité | le **nom étendu** (URI, nom local) ; le préfixe n'est qu'une abréviation locale |
| défaut | `xmlns="uri"` vaut pour les éléments sans préfixe, **jamais** pour les attributs |
| annuler | `xmlns=""` ; un préfixe, lui, ne peut pas être délié en Namespaces 1.0 |
| portée | une déclaration vaut pour l'élément et ses descendants ; la plus proche gagne |
| réservés | `xml` → `http://www.w3.org/XML/1998/namespace` toujours lié ; `xmlns` jamais déclarable |
| `xsi:type` | sa **valeur** est un QName : on la résout avec les liaisons de l'élément |
| XPath 1.0 | un nom sans préfixe = sans namespace : lier ses propres préfixes |

</details>
