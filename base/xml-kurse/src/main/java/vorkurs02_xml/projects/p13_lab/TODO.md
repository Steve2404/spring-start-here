# Projet 13 — CAPSTONE : le laboratoire d'import (cours 0.2.22)

> Première fois ? Lis d'abord le mode d'emploi [`vorkurs02_xml/PARCOURS.md`](../../PARCOURS.md) : comment lire cette fiche, lancer `Check`, quoi faire en cas de blocage.

**Notions visées :** examiner un fichier avant de corriger ; erreurs de bonne forme avec ligne et colonne ; valider proprement contre un XSD ; contrôler le DOM et des valeurs avec XPath ; choisir DOM, SAX ou StAX selon la couche ; corriger de façon reproductible ; une chaîne complète, du fichier brut à la logique métier (0.2.22, et tout le chapitre).  
**Ce qui est donné :** `Data.java` (`Data.FILES`, `Data.file(nom)`), les commandes et le schéma `lab-order.xsd` dans `files/`, et `Check.java`.  
**Ce que TU crées :** tous les fichiers `.java`, dans le paquet `vorkurs02_xml.projects.p13_lab`. La classe du `main` s'appelle **`LabPipeline`**.

---

## Le problème

Le laboratoire du campus reçoit des commandes de matériel (namespace `urn:campus:lab`). Tu construis la **chaîne d'import** : chaque fichier traverse des couches, chacune avec l'outil qui lui convient. La chaîne s'arrête à la **première** couche en échec, et le rapport dit **où** et **pourquoi**.

Ouvre `lab-order.xsd` et les onze commandes avant de commencer. Pour chaque fichier, prédis sur papier la couche qui l'arrêtera.

---

## Tableau de bord

### ☐ Étape 1 — Couche 1 : bien formé ? (0.2.22 S2)

Un passage **SAX** sûr (namespace-aware, `FEATURE_SECURE_PROCESSING`, `disallow-doctype-decl`, `ACCESS_EXTERNAL_DTD` à `""` sur le parseur), avec un `DefaultHandler` vide. Échec : la ligne de la `SAXParseException`.
- **Question :** pourquoi SAX pour cette couche, et pas DOM ?

### ☐ Étape 2 — Couche 2 : le bon vocabulaire ? (0.2.22 S1)

Un lecteur **StAX** (DTD et entités externes désactivées) : `nextTag()` jusqu'à la racine, et **rien de plus**. La racine doit être `order` dans le namespace `urn:campus:lab`. Échec : la ligne de `getLocation()`. Ferme le lecteur.
- **Question :** sans cette couche, quelle erreur le XSD donnerait-il pour `05` ? Pourquoi est-elle moins utile à un humain ?

### ☐ Étape 3 — Couche 3 : valide ? (0.2.22 S3)

- Compile `lab-order.xsd` **une seule fois** pour tout le lot : `SchemaFactory` (avec `ACCESS_EXTERNAL_DTD` et `ACCESS_EXTERNAL_SCHEMA` à `""`), puis `newSchema(…)`.
- Pour chaque fichier, un `Validator` neuf (`schema.newValidator()`) et un `ErrorHandler` à toi : à la **première** erreur, retiens sa ligne et son **code**, puis relance l'exception pour arrêter la validation.
- **Le code** est le début du message, jusqu'au premier `:` (par exemple `cvc-enumeration-valid`). C'est le **seul** usage permis du message : le reste est traduit selon la langue de la machine.
- **Question :** pourquoi compiler le schéma une seule fois, mais créer un `Validator` par fichier ?

### ☐ Étape 4 — Couches 4 et 5 : l'arbre, puis le métier (0.2.22 S4)

- **Couche 4 :** un **DOM** sûr (namespace-aware, `FEATURE_SECURE_PROCESSING`, `disallow-doctype-decl`, accès externes à `""`, `ErrorHandler` silencieux), seulement maintenant.
- **Couche 5 :** des règles que le XSD ne sait pas dire, avec `javax.xml.xpath`. Un `XPath` à qui tu donnes un `NamespaceContext` à toi, qui lie le préfixe `l` à `urn:campus:lab`. Dans cet ordre :
  - la somme des `quantity` dépasse 50 → `QUANTITY_LIMIT` ;
  - un `device` a le même `serial` qu'un `device` **précédent** → `DUPLICATE_SERIAL` ;
  - la date de `delivery` est avant l'attribut `date` de la commande → `DELIVERY_BEFORE_ORDER`. Compare-les comme des nombres, une fois les tirets retirés (`translate`).
- Chaque règle s'évalue en `XPathConstants.BOOLEAN`.

### ☐ Étape 5 — Le rapport (0.2.22 S7)

Pour chaque fichier de `Data.FILES`, dans l'ordre :
```
01-ok.xml : DONE devices=2 quantity=22
03-not-wellformed.xml : WELL_FORMED l.4
05-wrong-namespace.xml : NAMESPACE l.2
07-schema-enum.xml : SCHEMA l.3 cvc-enumeration-valid
10-business-multi.xml : BUSINESS [DUPLICATE_SERIAL, DELIVERY_BEFORE_ORDER]
```
- `DONE` : le nombre de `device` (`count`, en `NUMBER`) et la somme des `quantity` (`sum`), en entiers ;
- `BUSINESS` : la liste des règles violées (son `toString()`).

Puis :
```
BILAN : 2 importes sur 11
```

### ☐ Étape 6 — Corriger de façon reproductible (0.2.22 S6)

Pour chaque fichier arrêté à la couche `SCHEMA`, dans l'ordre du rapport : **toutes** ses erreurs. Un deuxième `ErrorHandler`, qui **collecte** chaque erreur sans relancer. Chaque erreur s'écrit `<ligne> <code>`.
```
TOUTES 08-schema-missing.xml : [5 cvc-complex-type.2.4.b]
```
- **Question :** `11-schema-many.xml` a 8 erreurs, mais combien de **fautes** ? Dans quel ordre les corriger ?

### ☐ Étape 7 — Les types de retour de XPath (0.2.22 S4)

Sur le DOM de `01-ok.xml`, quatre évaluations :
- `NODESET` : les `serial` de tous les `device` (la valeur de chaque nœud, dans une liste) ;
- `NUMBER` : la somme de tous les `quantity` (un `Double`, affiché tel quel) ;
- `STRING` (l'`evaluate` sans type) : le `type` du premier `device` ;
- `BOOLEAN` : `/l:order/l:delivery > '2026-09-05'`.
```
XPATH 01-ok.xml : NODESET [SN-100, SN-101] | NUMBER 22.0 | STRING microscope | BOOLEAN false
```
- **Question :** la livraison est le 10 septembre, donc après le 5. Pourquoi XPath 1.0 répond-il `false` ? Comment l'étape 4 contourne-t-elle ce piège ?

### ☐ Étape 8 — Le `main` de `LabPipeline`

Dans l'ordre : les 11 lignes du rapport, le BILAN, les lignes TOUTES, puis XPATH.

---

## Checklist API (vérifiée par `Check`)

| Élément | Étape | ☐ |
|---|---|---|
| `SAXParserFactory`, `disallow-doctype-decl`, `SAXParseException` | 1 | ☐ |
| `XMLInputFactory`, `nextTag()`, `getLocation()` | 2 | ☐ |
| `SchemaFactory`, `newSchema(`, `newValidator()`, `setErrorHandler(`, `implements ErrorHandler` (ou anonyme), `StreamSource` | 3, 6 | ☐ |
| `DocumentBuilderFactory`, `XMLConstants.FEATURE_SECURE_PROCESSING`, `XMLConstants.ACCESS_EXTERNAL_SCHEMA` | 4 | ☐ |
| `XPathFactory`, `NamespaceContext`, `setNamespaceContext(`, `XPathConstants.BOOLEAN`, `translate(`, `preceding::` | 4 | ☐ |
| `XPathConstants.NUMBER`, `XPathConstants.NODESET`, `NodeList` | 5, 7 | ☐ |
| `enum ` (les couches), `record ` | 5 | ☐ |
| ~~`XmlKit`~~ | **interdit** | — |

---

## Sortie attendue complète

C'est le contrat exact que vérifie `Check` :

```
a remplir
```
