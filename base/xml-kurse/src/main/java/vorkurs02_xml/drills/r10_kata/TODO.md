# Drill de rappel 10 — Kata mixte chronométré (tout le chapitre 0.2)

> Première fois ? Lis d'abord [`PARCOURS.md`](../../PARCOURS.md) et les règles des drills [`drills/README.md`](../README.md).

**Chrono cible :** 30 min, **sans carte mémoire**. C'est le test final de chaque cycle de révision.

**Règles :**
- Tout se fait de mémoire.
- Crée **`Recall10`**. Le campus : `vorkurs02_xml.drills.Data.campus()` / `campusText()`. Préfixes XPath : `c` → `urn:campus`, `g` → `urn:campus:grades`.
- D01 à D05 utilisent `XmlKit` ; D06 à D10, les vraies API (sauf la valeur `cours` de D10).
- Une ligne par défi, préfixée `Dxx : `.

## Défis

- ☐ **D01.** Écris une racine `r` dont l'attribut `a`, entre guillemets doubles, vaut `<"&'`. Sa valeur relue.
  → `D01 : <"&'`
- ☐ **D02.** Sur le campus, le nombre d'éléments du namespace `urn:campus:grades`, en une expression qui teste l'URI.
  → `D02 : 5`
- ☐ **D03.** Une DTD interne où `r` contient **au moins un** `a` vide, et le document `<r/>` : son verdict de validité.
  → `D03 : INVALID 1 (ligne 1)`
- ☐ **D04.** Un schéma où `q` est un entier strictement positif (type prédéfini) ; le document `<q>0</q>` : les erreurs de `validateXsd`.
  → `D04 : [1 cvc-minInclusive-valid, 1 cvc-type.3.1.3]`
- ☐ **D05.** Le nom de l'enseignant d'un cours dont l'enseignant enseigne **aussi** un cours qui suit, en **une** expression.
  → `D05 : Dupont`
- ☐ **D06.** En DOM (namespace-aware) : retire toutes les `person` sans email. Puis le nombre de `person` restantes, relu sur la **même** `NodeList` qu'au départ.
  → `D06 : 3`
- ☐ **D07.** En SAX : la somme des `credits` des cours qui ont au moins un étudiant.
  → `D07 : 9`
- ☐ **D08.** En StAX : le nom de la personne `S3`, en arrêtant la lecture dès qu'il est trouvé.
  → `D08 : Chloe`
- ☐ **D09.** En StAX **sans** support des DTD : lis tout `<!DOCTYPE r [<!ENTITY e "x">]><r>&e;</r>`. `accepte` ou `refuse`.
  → `D09 : refuse`
- ☐ **D10.** Construis en DOM un document dont la racine vide `bilan` a deux attributs : `etudiants` (le nombre de `person` restantes après D06) et `cours` (le nombre de cours, par `XmlKit`). Sérialise-le sans déclaration.
  → `D10 : <bilan cours="3" etudiants="3"/>`

## Sortie attendue complète

```
D01 : <"&'
D02 : 5
D03 : INVALID 1 (ligne 1)
D04 : [1 cvc-minInclusive-valid, 1 cvc-type.3.1.3]
D05 : Dupont
D06 : 3
D07 : 9
D08 : Chloe
D09 : refuse
D10 : <bilan cours="3" etudiants="3"/>
```

## Après le kata

- **Pour chaque défi raté ou lent (plus de 3 min) :** relis la carte du drill correspondant, puis refais **ce drill** le lendemain.

  | Défi | Drill |
  |---|---|
  | D01 | r01 |
  | D02 | r03 |
  | D03 | r02 |
  | D04 | r04 |
  | D05 | r05 |
  | D06, D10 | r06 |
  | D07 | r07 |
  | D08 | r08 |
  | D09 | r09 |

- **Note ton temps** dans le tableau de suivi de `drills/README.md`.
