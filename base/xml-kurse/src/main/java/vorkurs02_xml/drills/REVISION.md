# Drills du Vorkurs 0.2 (XML) — mode d'emploi et plan de révision

Les **exercices** (`vorkurs02_xml/exercises`, 01 → 18) t'apprennent les notions en
écrivant de vrais outils (un mini-parseur, un moteur XPath, un pipeline d'import…).
Les **drills** (`vorkurs02_xml/drills/exercises`, 01 → 07) te font répéter les API
Java XML jusqu'à ce que les méthodes sortent toutes seules. Tous les drills utilisent
le même petit fichier : `src/main/resources/vorkurs02_xml/fixtures/drills/campus.xml`
(3 cours, 5 notes, 4 personnes), chargé par `drills/Campus.java`. Lis-le une fois et
garde-le ouvert à côté.

| Drill | API couverte | TODO | À faire après les exercices |
|---|---|---|---|
| 01 `DomApi` | `Document`, `Element`, `Node`, `NodeList`, création / insertion / suppression / clonage / import | 19 | 12 – 13 |
| 02 `NamespaceDomApi` | `getElementsByTagNameNS`, `getNamespaceURI`, `getLocalName`, `getPrefix`, `lookup*`, `createElementNS`, `setAttributeNS`, `renameNode` | 15 | 06 – 07 |
| 03 `XPathApi` | `XPathFactory`, `compile`, les 5 `XPathConstants`, `NamespaceContext`, axes, prédicats, fonctions XPath 1.0 | 20 | 10 – 11 |
| 04 `SaxApi` | `SAXParserFactory`, `DefaultHandler` (tous les callbacks), `Attributes`, `Locator`, arrêt volontaire | 13 | 14 |
| 05 `StaxApi` | `XMLStreamReader`, `XMLEventReader` (`peek`), `XMLStreamWriter` | 15 | 15 – 16 |
| 06 `ValidationTransformApi` | `SchemaFactory`, `Validator`, `ErrorHandler`, `setSchema`, `Transformer` / `OutputKeys`, réglages de sécurité | 12 | 08 – 09, 17 |
| 07 `MixedKata` | **tout, sans indice** : 15 questions métier | 15 | 18 |

Les corrigés sont dans `drills/solutions/SolutionDrillNN_*.java` (et ceux des exercices
dans `solutions/`). Ils sont **commentés** : chaque méthode explique pourquoi on a choisi
cet outil et quel piège il évite. Lis-les **après** avoir réussi, jamais avant.

---

## Par quoi commencer : exercices ou drills ?

**Les deux, en alternant, thème par thème.**

- L'**exercice** sert à **comprendre** (histoire, calcul à la main, plan). On commence toujours par lui.
- Le **drill** sert à **mémoriser**. On le fait **le lendemain** des exercices du même thème,
  quand il faut déjà un petit effort pour se souvenir. C'est cet effort qui fixe la mémoire.

### Le parcours, étape par étape

| Étape | Thème (cours) | Jour 1 : comprendre (exercices) | Jour 2 : mémoriser |
|---|---|---|---|
| 1 | Syntaxe, bonne formation, échappement, entités (0.2.1 – 0.2.8) | Exercise01 → 02 → 03 | Refaire de mémoire les TODO « croix » de 01 à 03 |
| 2 | Well-formed vs valid, DTD (0.2.9, 0.2.11) | Exercise04 → 05 | Refaire le TODO 2 de 05 (le compilateur de modèles) |
| 3 | Namespaces (0.2.12 – 0.2.13) | Exercise06 → 07 | Drill02 |
| 4 | XSD (0.2.14 – 0.2.15) | Exercise08 → 09 | Drill06 (TODO 1 – 7) |
| 5 | XPath (0.2.16) | Exercise10 → 11 | Drill03 |
| 6 | L'arbre et DOM (0.2.10, 0.2.17) | Exercise12 → 13 | Drill01, Drill06 (TODO 8 – 12) |
| 7 | SAX, StAX, le choix du modèle (0.2.18 – 0.2.20) | Exercise14 → 15 → 16 | Drill04, Drill05 |
| 8 | Sécurité et labo final (0.2.21 – 0.2.22) | Exercise17 → Exercise18 (capstone) | Drill07 (kata mélangé) |

Les étapes 1, 5 et 7 sont longues (01, 05, 11 et 16 sont des défis de niveau entretien) :
étale-les sur 2 ou 3 jours si besoin.

**Une séance type (environ 1 h) :**
1. **D'abord les révisions dues** (10 – 20 min) : les drills déjà réussis dont la date
   J+1 / J+3 / J+7… tombe aujourd'hui (voir le tableau de suivi en bas).
2. **Ensuite, la nouveauté** : les exercices ou le drill de l'étape en cours.
3. **Pour finir, 2 minutes de « carte vierge »** : sur une feuille, écris de mémoire les
   méthodes vues aujourd'hui (ex. les 5 `XPathConstants` et le type Java de chacun).

Compte environ 3 semaines pour les 8 étapes.

---

## Comment faire un drill

1. Lance un chronomètre.
2. Remplis les TODO **sans regarder la « CARTE MÉMOIRE »** en bas du Javadoc.
3. Bloqué plus d'une minute ? Regarde la carte, **cache-la, puis réécris la ligne
   de mémoire**. Mets une croix à côté de ce TODO : c'est un point faible.
4. Lance `main()` jusqu'à obtenir 100 %.
5. Note ton temps, ton score au premier lancement et tes TODO « croix » dans le
   tableau de suivi ci-dessous.
6. Seulement ensuite, compare avec le corrigé : il y a souvent une écriture plus courte.

## Pourquoi tu oublies, et comment ne plus oublier

On oublie ce qu'on a seulement **relu**. On retient ce qu'on a dû **retrouver de
mémoire**, plusieurs fois, en espaçant les séances. D'où trois règles.

**1. Rappel actif.** Refaire un drill depuis une page blanche vaut dix relectures
du corrigé.

**2. Répétition espacée.** Refais chaque drill selon ce calendrier, en comptant
à partir du jour où tu l'as réussi pour la première fois :

| Séance | Quand |
|---|---|
| 1 | Jour J (première réussite) |
| 2 | J + 1 |
| 3 | J + 3 |
| 4 | J + 7 |
| 5 | J + 14 |
| 6 | J + 30 |
| ensuite | tous les 2 mois, et juste avant d'attaquer la config XML de Spring |

Un drill refait à 100 % du premier coup et en moins de 10 minutes peut passer à
l'étape suivante. Sinon, refais-le le lendemain, puis reprends le calendrier.

**3. Mélange.** Après les drills « une API à la fois » (01 – 06), le drill 07
mélange tout. C'est lui qui t'apprend à **choisir** l'outil (DOM ? XPath ? `*NS` ?).
Refais-le chaque semaine pendant la révision.

**Petits plus qui marchent :**
- **Carte vierge.** Sans rien regarder, écris les 4 réglages de sécurité d'une
  `DocumentBuilderFactory` (exercice 17), puis compare.
- **À voix haute.** Explique pourquoi `//book[1]` rend plusieurs livres, ou pourquoi
  un attribut sans préfixe n'a pas de namespace. Si tu bloques en expliquant, ce
  n'est pas encore acquis.
- **Avant Spring.** Ouvre un vrai `applicationContext.xml` ou un `pom.xml` : repère le
  namespace par défaut, les préfixes, `xsi:schemaLocation`. Tout le Vorkurs sert à ça.

## Remettre un drill à zéro pour le refaire

Les drills sont commités « vierges » (tous les TODO avec leur `throw`). Pour repartir
de zéro sur un drill (depuis `base/xml-kurse`) :

```
git restore src/main/java/vorkurs02_xml/drills/exercises/Drill01_DomApi.java
```

Pour tout remettre à zéro :

```
git restore src/main/java/vorkurs02_xml/drills/exercises/
```

> ⚠️ `git restore` **efface ta version** du fichier. C'est voulu pour un drill : le
> but est de réécrire, pas de garder. Si tu veux conserver une tentative, copie-la
> avant ailleurs, hors de `src/`.

## Tableau de suivi

Format d'une case : `date – temps – score au 1er lancement` (ex. `30/09 – 14 min – 16/19`).
Liste aussi les TODO « croix » : ce sont eux qu'il faut surveiller.

| Drill | J | J+1 | J+3 | J+7 | J+14 | J+30 | TODO faibles |
|---|---|---|---|---|---|---|---|
| 01 DOM | | | | | | | |
| 02 DOM namespaces | | | | | | | |
| 03 XPath | | | | | | | |
| 04 SAX | | | | | | | |
| 05 StAX | | | | | | | |
| 06 Validation / Transformer | | | | | | | |
| 07 Kata mélangé | | | | | | | |
