# Projet 5 — Le résolveur de noms (cours 0.2.12 → 0.2.13)

> Première fois ? Lis d'abord le mode d'emploi [`vorkurs02_xml/PARCOURS.md`](../../PARCOURS.md) : comment lire cette fiche, lancer `Check`, quoi faire en cas de blocage.

**Notions visées :** pourquoi les namespaces, `xmlns:p`, QName contre nom étendu, le préfixe n'est pas l'identité, portée et redéfinition, préfixes réservés `xml` et `xmlns` (0.2.12) ; namespace par défaut, qui ne s'applique **pas** aux attributs, `xmlns=""`, `xsi`, la valeur de `xsi:type` est elle-même un QName, `xsi:schemaLocation` (0.2.13).  
**Ce qui est donné :** `Data.java` et `Check.java`. De `xmlkit.XmlKit` : `value` (arbitre des noms), `wellFormed` et, nouveau, `wellFormedNs(xml)` : le même verdict, mais **avec** les règles des namespaces.  
**Ce que TU crées :** tous les fichiers `.java`, dans le paquet `vorkurs02_xml.projects.p05_names`. La classe du `main` s'appelle **`Names`**.

---

## Le problème

Une plateforme reçoit des factures et des commandes qui mélangent plusieurs vocabulaires. Pour comprendre un nom, il ne suffit pas de le lire : `inv:line` ne veut rien dire tant qu'on ne sait pas à quelle URI `inv` est lié **à cet endroit**. Tu écris le résolveur : chaque nom devient un **nom étendu** `{uri}local`.

Les documents de ce projet sont bien formés au sens XML 1.0 et n'ont ni DOCTYPE, ni entité, ni CDATA. Un petit lecteur suffit : des **événements** « ouverture (nom, attributs dans l'ordre) », « fermeture », « texte ». Une balise vide donne une ouverture puis une fermeture. Ignore la déclaration et les commentaires.

**Le nom étendu** s'écrit `{uri}local`, ou juste `local` s'il n'a pas de namespace.

---

## Tableau de bord

### ☐ Étape 1 — Les déclarations (0.2.12 S2, S6 ; 0.2.13 S4)

Les attributs `xmlns` et `xmlns:p` d'une balise **déclarent** des liaisons. Ce ne sont **pas** des attributs de l'élément.
- `xmlns="uri"` lie le préfixe vide (le défaut). `xmlns=""` est permis : il **annule** le défaut.
- `xmlns:p="uri"` lie `p`. Les erreurs, au premier problème :
  - `p` vaut `xmlns`, ou `p` vaut `xml` avec une autre URI que `http://www.w3.org/XML/1998/namespace` : `PREFIXE_RESERVE <p>` ;
  - l'URI est vide : `LIAISON_VIDE <p>`.
- **Question :** pourquoi `xmlns=""` est-il permis, mais pas `xmlns:p=""` ?

### ☐ Étape 2 — La pile des portées (0.2.12 S5)

À **chaque** ouverture, empile les liaisons déclarées par cette balise, même s'il n'y en a aucune. À chaque fermeture, dépile.
- Chercher un préfixe : du **sommet** vers le bas. La liaison la plus proche gagne.
- `xml` est **toujours** lié à `http://www.w3.org/XML/1998/namespace`, sans déclaration.
- **Question :** pourquoi faut-il empiler même une portée vide ?

### ☐ Étape 3 — Résoudre un nom (0.2.12 S3 ; 0.2.13 S1, S3)

- Un QName contient au plus un `:`, ni au début ni à la fin. Sinon : `QNAME_INVALIDE <nom>`.
- **Avec préfixe :** le préfixe doit être lié. Sinon : `PREFIXE_NON_DECLARE <p>`.
- **Sans préfixe :**
  - un **élément** prend le namespace par défaut, s'il y en a un et qu'il n'est pas vide ;
  - un **attribut** n'en prend **jamais**.
- Après avoir résolu les attributs d'une balise : deux attributs de même nom étendu → `ATTRIBUT_DOUBLE <nom étendu>`.
- **La valeur de `xsi:type`** (l'attribut dont le nom étendu est `{http://www.w3.org/2001/XMLSchema-instance}type`) est elle-même un QName. Résous-la **comme un nom d'élément**, avec les portées de l'élément qui la porte, et remplace la valeur par ce nom étendu.

### ☐ Étape 4 — La facture, élément par élément

Pour chaque élément de `Data.INVOICE`, numérotés `n` = 1, 2… en ordre de lecture :
```
N3 {urn:shop:invoice}line @nr | parseur identique
```
- le nom étendu, puis chaque attribut (hors déclarations) au format ` @<nom étendu>`, triés dans l'ordre naturel des `String` ;
- l'arbitre : `XmlKit.value(Data.INVOICE, "namespace-uri((//*)[n])")` et `XmlKit.value(Data.INVOICE, "local-name((//*)[n])")`, avec `n` remplacé par le numéro. Avec ces deux valeurs, construis le nom étendu (sans accolades si l'URI est vide). Affiche ` | parseur identique` s'il est égal au tien, sinon ` | parseur DIFFERENT <le sien>`.

Ensuite, un deuxième passage sur les mêmes éléments, dans le même ordre. Pour chacun, dans cet ordre :
- si c'est `note` ou l'élément de nom local `tax` : `PORTEE <nom étendu> :` puis chaque liaison **visible** à cet endroit, au format ` <préfixe>=<uri>`. Le défaut s'écrit `(defaut)`. Triées par préfixe, le défaut (préfixe vide) en premier. `xml` y figure toujours. Un défaut annulé (vide) n'y figure pas ;
- s'il porte un `xsi:type` : `XSI <nom étendu de l'élément> : <valeur résolue>` ;
- s'il porte un `xsi:schemaLocation` : sa valeur est une suite de **paires** `namespace emplacement` séparées par des blancs. `SCHEMAS ` puis chaque paire `<namespace> -> <emplacement>`, séparées par ` ; `.
- **Question :** `note` est-il dans un namespace ? Et son attribut s'il en avait un ?
- **Question :** `customer` et `inv:line` n'ont pas de préfixe commun. Lequel est dans `urn:shop:invoice` ? Pourquoi `@nr` n'y est-il pas ?
- **Question :** `xsi:schemaLocation` oblige-t-il un parseur à télécharger `invoice.xsd` ?

### ☐ Étape 5 — Les règles des namespaces (0.2.12 S6)

Pour chaque document de `Data.CASES` : résous-le entièrement, puis affiche `OK` ou la **première** erreur. Ajoute ensuite les deux verdicts du parseur :
```
B05 ATTRIBUT_DOUBLE {urn:x}id | XML 1.0 OK | namespaces KO 1:55
```
- **Question :** tous ces documents sont bien formés pour XML 1.0. Les namespaces sont donc une couche **au-dessus** de XML. Qu'est-ce que cela implique pour un parseur configuré sans namespaces (projet 8) ?

### ☐ Étape 6 — Même commande, autres préfixes (0.2.12 S4)

La **signature** d'un document : pour chaque élément, en ordre de lecture, une ligne
`<nom étendu> <attributs> "<texte>"`, où :
- `<attributs>` est le `toString()` d'une `TreeMap` nom étendu → valeur (avec `xsi:type` résolu), par exemple `{sku=X1}` ;
- `<texte>` est son texte **direct** (sans celui des enfants), blancs du début et de la fin retirés.

Compare la signature de `Data.ORDER_A` à celles de `ORDER_B` puis de `ORDER_C` :
```
EGAL A B : oui (6 elements)
EGAL A C : non, element 2 : <ligne de A> != <ligne de C>
```
- À la première ligne différente, son numéro (à partir de 1) et les deux lignes. Une signature plus courte que l'autre donne `(rien)`.
- **Question :** `A` et `B` n'ont aucun préfixe en commun pour la commande. Pourquoi sont-elles pourtant identiques ?
- **Question :** `A` et `C` ne diffèrent que d'une majuscule. Pourquoi cela suffit-il ?

### ☐ Étape 7 — Le `main` de `Names`

Dans l'ordre : `N1` à `N7`, le deuxième passage, `B01` à `B09`, puis les deux lignes EGAL.

---

## Checklist API (vérifiée par `Check`)

| Élément | Étape | ☐ |
|---|---|---|
| une pile : `Deque`, `push`, `pop` | 2 | ☐ |
| `TreeMap` | 4, 6 | ☐ |
| `XmlKit.value(` | 4 | ☐ |
| `XmlKit.wellFormed(`, `XmlKit.wellFormedNs(` | 5 | ☐ |
| `record ` | — | ☐ |
| ~~`javax.xml`~~, ~~`org.w3c`~~, ~~`org.xml`~~, ~~`XmlKit.validateXsd`~~, ~~`XmlKit.select`~~ | **interdits** | — |

---

## Sortie attendue complète

C'est le contrat exact que vérifie `Check` :

```
a remplir
```
