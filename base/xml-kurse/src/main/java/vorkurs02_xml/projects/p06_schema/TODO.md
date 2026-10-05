# Projet 6 — Le schéma du catalogue (cours 0.2.14 → 0.2.15)

> Première fois ? Lis d'abord le mode d'emploi [`vorkurs02_xml/PARCOURS.md`](../../PARCOURS.md) : comment lire cette fiche, lancer `Check`, quoi faire en cas de blocage.

**Notions visées :** un XSD est du XML, `xs:schema` et `targetNamespace`, `xs:element` et son type, types simples et complexes, `xs:sequence`, `minOccurs` / `maxOccurs`, éléments globaux et locaux, types nommés et anonymes (0.2.14) ; contraintes, `xs:restriction` et ses facettes, `sequence` / `choice` / `all`, `xs:attribute` avec `use`, `default`, `fixed`, `targetNamespace`, la chaîne de validation (0.2.15).  
**Ce qui est donné :** `Data.java` (les noms des 18 documents, et `Data.text(nom)` qui lit un fichier de `files/`), les documents eux-mêmes dans `files/`, et `Check.java`. De `xmlkit.XmlKit` :
- `validateXsd(cheminDuXsd, texte)` : la liste des erreurs, une par entrée, au format `ligne code`. Vide si le document est valide ;
- `file(TaClasse.class, "catalog.xsd")` : le chemin d'un fichier rangé **dans le dossier de ton paquet**.

**Ce que TU crées :**
- **`catalog.xsd`**, dans ce dossier (à côté de ce `TODO.md`). C'est l'essentiel du projet : `Check` le lit aussi ;
- la classe **`SchemaCheck`**, avec le `main`, dans le paquet `vorkurs02_xml.projects.p06_schema`.

---

## Le problème

Une bibliothèque échange ses catalogues en XML, dans le namespace `urn:lib:catalog`. Elle te confie l'écriture du **contrat** : un schéma XSD. Ouvre les fichiers de `files/` :
- `good-1.xml` et `good-2.xml` doivent être **valides** ;
- chaque `bad-*.xml` viole **une seule** règle, et doit être refusé pour **cette** raison.

Avant d'écrire une ligne, ouvre `good-1.xml` et dessine l'arbre du catalogue sur papier.

---

## Tableau de bord

### ☐ Étape 1 — L'en-tête du schéma (0.2.14 S2 ; 0.2.15 S6)

`catalog.xsd` commence par la déclaration XML, puis `xs:schema` :
- `xs` lié à `http://www.w3.org/2001/XMLSchema` ;
- `targetNamespace="urn:lib:catalog"`, et le préfixe `c` lié à la **même** URI, pour pouvoir écrire `type="c:BookType"` ;
- `elementFormDefault="qualified"` : les éléments **locaux** sont eux aussi dans le namespace cible.
- **Question :** sans `elementFormDefault="qualified"`, dans quel namespace serait `<title>` ? `good-1.xml` resterait-il valide ?

### ☐ Étape 2 — Les types simples nommés (0.2.15 S3)

| Type | Base | Règle | Facettes |
|---|---|---|---|
| `IsbnType` | `xs:string` | `978` ou `979`, puis exactement 10 chiffres | `xs:pattern` |
| `LanguageType` | `xs:string` | `de`, `fr` ou `en`, en minuscules | `xs:enumeration` |
| `AmountType` | `xs:decimal` | strictement positif, au plus `9999.99`, au plus 2 décimales | `xs:minExclusive`, `xs:maxInclusive`, `xs:fractionDigits` |

- Un `xs:pattern` porte sur la valeur **entière** : pas de `^` ni de `$`.
- **Les facettes exactes comptent :** `minInclusive 0.01` refuserait aussi `0`, mais avec un **autre** code d'erreur.
- **Question :** `fractionDigits 2` : `19.9` passe-t-il ? Et `8.505` ?

### ☐ Étape 3 — Le prix : du texte et un attribut (0.2.15 S5)

`<price currency="CHF">19.9</price>` : un **contenu simple** (un `AmountType`) **plus** un attribut. C'est un type complexe nommé `PriceType` :
- `xs:simpleContent`, puis `xs:extension` de base `c:AmountType` ;
- l'attribut `currency` : facultatif, défaut `EUR`, et trois lettres **majuscules**. Son type est **anonyme** (un `xs:simpleType` écrit à l'intérieur de l'attribut), car il ne sert qu'ici.

### ☐ Étape 4 — Les métadonnées, dans n'importe quel ordre (0.2.15 S4)

Type complexe nommé `MetaType` : `publisher` (texte, obligatoire) et `year` (de type `xs:gYear`, facultatif), **dans n'importe quel ordre**, chacun au plus une fois. C'est `xs:all`.

### ☐ Étape 5 — Le livre (0.2.14 S5, S7 ; 0.2.15 S2, S4, S5)

Type complexe nommé `BookType`. Une **séquence** :
1. `title`, texte ;
2. `author`, texte, de 1 à 3 fois ;
3. `price`, de type `c:PriceType` ;
4. `meta`, de type `c:MetaType`, facultatif ;
5. puis **un choix** : soit `ebookUrl` (type `xs:anyURI`), soit `shelf` (une majuscule puis 2 chiffres, type anonyme).

Puis les attributs, **après** le modèle de contenu :
- `isbn` : `c:IsbnType`, **obligatoire** ;
- `lang` : `c:LanguageType`, défaut `fr` ;
- `status` : texte, valeur **fixée** à `catalogued`.

### ☐ Étape 6 — La racine (0.2.14 S6)

Le seul élément **global** : `catalog`, d'un type complexe **anonyme**, qui contient de 0 à une infinité de `book` de type `c:BookType`.
- **Question :** pourquoi un type nommé est-il global, alors que l'élément `book` est local ? Un document pourrait-il commencer par `<book>` ?

### ☐ Étape 7 — Le `main` de `SchemaCheck` (0.2.15 S7)

Pour chaque nom de `Data.DOCUMENTS`, dans l'ordre, valide `Data.text(nom)` contre **ton** schéma :
```
good-1 VALIDE
bad-isbn INVALIDE 3 cvc-pattern-valid ; 3 cvc-attribute.3
```
- toutes les erreurs, séparées par ` ; `, telles que rendues par `validateXsd`.

Puis :
```
BILAN : 2 valides, 16 invalides
CAUSE cvc-pattern-valid : bad-currency, bad-isbn, bad-shelf
```
- la **cause** d'un document invalide est le code de sa **première** erreur ;
- une ligne `CAUSE` par code, triées par code (ordre naturel des `String`) ; pour chacune, les documents triés par nom, séparés par `, `.

**Avant de lancer, prédis :**
- pourquoi `bad-isbn` produit **deux** erreurs pour une seule faute ;
- pourquoi `bad-no-namespace` est refusé dès la racine, alors que son contenu est parfait ;
- pourquoi `bad-meta-twice` et `bad-order` ont le **même** code.

**Expérience** (hors sortie attendue) : remplace `minExclusive value="0"` par `minInclusive value="0.01"`, relance et regarde quel code change. Puis remets-le.

**Question :** le schéma donne `lang="fr"` par défaut. Le premier livre de `good-1.xml` n'a pas de `lang`. Qui ajoute la valeur, et quand ? (Projet 13.)

---

## Checklist API (vérifiée par `Check`, dans tes `.java` **et** ton `.xsd`)

| Élément | Étape | ☐ |
|---|---|---|
| `targetNamespace`, `elementFormDefault="qualified"` | 1 | ☐ |
| `xs:simpleType`, `xs:restriction`, `xs:pattern`, `xs:enumeration` | 2 | ☐ |
| `xs:minExclusive`, `xs:maxInclusive`, `xs:fractionDigits` | 2 | ☐ |
| `xs:simpleContent`, `xs:extension`, `default="EUR"` | 3 | ☐ |
| `xs:all`, `xs:gYear` | 4 | ☐ |
| `xs:sequence`, `xs:choice`, `maxOccurs="3"`, `xs:anyURI` | 5 | ☐ |
| `use="required"`, `fixed="catalogued"` | 5 | ☐ |
| `maxOccurs="unbounded"` | 6 | ☐ |
| `XmlKit.file(`, `XmlKit.validateXsd(` | 7 | ☐ |
| `TreeMap` | 7 | ☐ |
| ~~`javax.xml`~~, ~~`org.w3c`~~, ~~`org.xml`~~ | **interdits** (le projet 13 utilisera la vraie API de validation) | — |

---

## Sortie attendue complète

C'est le contrat exact que vérifie `Check` :

```
a remplir
```
