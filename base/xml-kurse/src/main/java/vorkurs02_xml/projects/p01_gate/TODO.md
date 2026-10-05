# Projet 1 — Le portier d'import (cours 0.2.1 → 0.2.5)

> Première fois ? Lis d'abord le mode d'emploi [`vorkurs02_xml/PARCOURS.md`](../../PARCOURS.md) : comment lire cette fiche, lancer `Check`, quoi faire en cas de blocage.

**Notions visées :** pourquoi XML (balisage contre données à plat, 0.2.1), l'anatomie d'un document : déclaration, prologue, une seule racine (0.2.2), éléments et balises, contenu, éléments vides (0.2.3), attributs : syntaxe, place, unicité, guillemets, élément ou attribut ? (0.2.4), noms XML, casse, imbrication LIFO, parent / enfant / frère (0.2.5).  
**Ce qui est donné :** `Data.java` (les documents soumis, du texte brut) et `Check.java` (le correcteur). L'outil `xmlkit.XmlKit` (un vrai parseur, utilisé comme arbitre) est fourni aussi.  
**Ce que TU crées :** tous les fichiers `.java` du projet, dans ce paquet `vorkurs02_xml.projects.p01_gate`. La classe qui contient `main` s'appelle **`Gate`**. Tout le reste est libre.

**Pour vérifier :** lance `Check.java`. Il exécute ton `main`, compare ta sortie à la sortie attendue (en bas de ce fichier) et montre la première ligne fausse. Il lit aussi tes sources : il liste ce qui manque et refuse ce qui vient d'une section plus loin du cours. Ne regarde `solution/` qu'à la fin.

---

## Le problème

Une plateforme reçoit des fichiers XML de partenaires. Avant tout traitement, un **portier** lit chaque document **caractère par caractère** et rend un verdict : `OK`, ou la **première** règle violée et la ligne où se trouve la **cause**.

Tu n'as pas le droit d'utiliser une API XML de Java : le cours ne l'a pas encore montrée (elle arrive en 0.2.17). Tu écris donc ton propre lecteur, comme le fait un vrai parseur : une passe de gauche à droite, avec une **pile** des éléments ouverts. Puis, pour chaque document, tu compares ton verdict à celui d'un vrai parseur, `XmlKit.wellFormed(texte)`, qui rend `OK` ou `KO ligne:colonne`.

Les documents de ce projet ne contiennent **ni** commentaire, **ni** référence `&…;`, **ni** CDATA, **ni** DOCTYPE : ce sera le projet 2.

---

## Tableau de bord

### ☐ Étape 1 — Les noms XML (0.2.5 S1 → S4)

Écris un test « ce texte est-il un nom XML ? » :
- un nom n'est pas vide ;
- son **premier** caractère est un `NameStartChar` : `:`, `A`–`Z`, `_`, `a`–`z`, ou un caractère des plages `C0–D6`, `D8–F6`, `F8–2FF`, `370–37D`, `37F–1FFF`, `200C–200D`, `2070–218F`, `2C00–2FEF`, `3001–D7FF`, `F900–FDCF`, `FDF0–FFFD`, `10000–EFFFF` (en hexadécimal) ;
- **chacun des suivants** est un `NameChar` : un `NameStartChar`, ou `-`, `.`, `0`–`9`, `B7`, `300–36F`, `203F–2040`.
- Parcours le nom par **points de code** (`codePoints()`), pas par `char` : un caractère hors du plan de base tient sur deux `char`.
- **Question :** `prénom` est-il un nom XML ? Et `1erPrix` ? Et `b-c` ?
- **Question :** `xmlConfig` respecte la grammaire. Pourquoi le cours dit-il pourtant qu'il ne faut pas l'utiliser ?

### ☐ Étape 2 — Lire une balise (0.2.3, 0.2.4)

À partir d'un `<`, lis une balise et détermine sa sorte : **ouvrante** `<a …>`, **fermante** `</a>`, ou **vide** `<a …/>`.
- Le nom va jusqu'au premier `>`, `/` ou **blanc XML**. Les blancs XML sont **exactement** l'espace, la tabulation, `\n` et `\r` (`Character.isWhitespace` en accepte d'autres : interdit).
- **Une fermante** n'a que des blancs entre son nom et `>`. Un attribut dans `</b x="1">` → `MALFORMED_TAG`.
- **Une ouvrante ou vide :** une suite d'attributs `nom = "valeur"`.
  - Des blancs sont permis autour du `=`.
  - La valeur est entre guillemets **doubles ou simples**, et se termine au prochain guillemet **de la même sorte**.
  - Deux attributs sont séparés par **au moins un blanc**.
  - Garde les attributs dans l'ordre du texte. L'ordre n'a aucun sens pour XML, mais il en a un pour ton affichage.
- **Les erreurs**, toutes situées à la ligne du `<` de la balise, dans l'ordre où on les rencontre en lisant :
  - nom d'élément ou d'attribut invalide → `BAD_NAME` ;
  - pas de `=`, pas de guillemet ouvrant ou fermant, pas de blanc entre deux attributs, fin du texte, attribut dans une fermante → `MALFORMED_TAG` ;
  - un nom d'attribut déjà vu dans **cette** balise → `DUPLICATE_ATTRIBUTE`.
- **Question :** `<a x=1/>` : pourquoi est-ce interdit en XML, alors que HTML l'accepte ?

### ☐ Étape 3 — La déclaration XML (0.2.2 S2, S4)

- Si le document commence **exactement** (indice 0) par `<?xml` suivi d'un blanc, c'est la déclaration. Elle doit avoir cette forme, et rien d'autre :
  - `version="1.x"`, **obligatoire** et **en premier** (`x` : un ou plusieurs chiffres) ;
  - puis, facultatif, `encoding="nom"`, où le nom commence par une lettre puis contient lettres, chiffres, `.`, `_` ou `-` ;
  - puis, facultatif, `standalone="yes"` ou `"no"` ;
  - chaque pseudo-attribut est précédé d'au moins un blanc ; blancs permis autour de `=` ; guillemets simples ou doubles ;
  - des blancs facultatifs, puis `?>`.
  - Sinon → `BAD_DECLARATION`, ligne 1.
- Un `<?xml` rencontré **n'importe où ailleurs** (même après une seule ligne vide) → `MISPLACED_DECLARATION`, à sa ligne.
- **Question :** la déclaration ressemble à une balise avec des attributs. Pourquoi ses « attributs » ont-ils un **ordre imposé**, contrairement aux vrais ?

### ☐ Étape 4 — Le diagnostic complet, en une passe (0.2.2 S5, 0.2.3, 0.2.5 S5)

Lis le document de gauche à droite. La **première** erreur rencontrée gagne. Tiens une **pile** (`Deque`) des éléments ouverts.
- **Le texte** (tout ce qui n'est pas une balise) :
  - dans un élément ouvert : c'est son contenu ;
  - hors de tout élément : seuls des blancs sont permis. Sinon → `CONTENT_OUTSIDE_ROOT`, à la ligne du **premier caractère non blanc**.
- **Une ouvrante ou une vide** alors que la pile est vide **et** qu'une racine a déjà été vue → `MULTIPLE_ROOTS`, à la ligne de cette balise.
- **Une ouvrante** s'empile. **Une vide** ne s'empile pas : elle s'ouvre et se ferme d'un coup.
- **Une fermante :**
  - pile vide → `END_WITHOUT_START` ;
  - le nom ne correspond pas **exactement** (majuscules comprises) au sommet de la pile → `MISMATCHED_END_TAG` ;
  - à la ligne de la fermante, dans les deux cas.
- **À la fin du texte :**
  - la pile n'est pas vide → `UNCLOSED_ELEMENT`, à la ligne de la balise ouvrante de l'élément **le plus interne** encore ouvert ;
  - aucune racine → `NO_ROOT`, à la ligne de la fin du document (la ligne d'un indice = 1 + le nombre de `\n` avant lui) ;
  - sinon → `OK`.
- **Question :** pour `D14`, ta ligne et celle du parseur diffèrent. Lequel montre la **cause**, lequel le **point de découverte** ? Lequel est le plus utile à un humain ?
- **Question :** `D12` (`<b><i>…</b></i>`) : pourquoi la pile suffit-elle à refuser un chevauchement ?

### ☐ Étape 5 — Construire l'arbre (0.2.5 S6)

Pendant la passe, construis tes propres éléments : nom, attributs, parent, liste des enfants **dans l'ordre**, texte direct. Le parent d'un élément est l'élément au **sommet de la pile** quand il s'ouvre.

### ☐ Étape 6 — Une ligne par document

```
D01 OK racine=catalogue elements=6 attributs=5 profondeur=3 | parseur OK
D03 BAD_DECLARATION ligne 1 | parseur KO 1:23
```
- Pour un document `OK` :
  - `elements` : tous les éléments, racine comprise ;
  - `attributs` : le total des attributs de tous les éléments ;
  - `profondeur` : la profondeur maximale (la racine est à 1) ;
  - si au moins un nom d'élément ou d'attribut **commence par `xml`**, quelle que soit la casse : ajoute ` reserve=` suivi de ces noms, sans doublon, triés et séparés par `,` ;
  - le document est **accepté**, même avec un nom réservé.
- `parseur` : ce que rend `XmlKit.wellFormed` pour ce document.

### ☐ Étape 7 — Le bilan

```
BILAN : 3 acceptes, 17 refuses, accord avec le parseur 20/20
```
- L'**accord** compte les documents où toi et le parseur êtes d'accord sur **OK / pas OK**. Les lignes ne comptent pas.

### ☐ Étape 8 — Le plan et les relations du premier document accepté (0.2.3 S5, 0.2.5 S6)

Après la ligne `PLAN de D01 :`, un élément par ligne, en ordre de lecture :
- indentation : 2 espaces par niveau, la racine étant au niveau 1 ;
- le nom, puis chaque attribut au format ` nom=valeur` ;
- si son texte direct, sans les blancs du début et de la fin, n'est pas vide : ` : ` suivi de ce texte ;
- s'il n'a **ni enfant ni aucun texte** (pas même un blanc) : ` (vide)`.
```
      titre : Dune
      stock (vide)
```
- **Question :** `<stock/>` et `<stock></stock>` donnent-ils le même élément ? Et `<stock> </stock>` ?

Après `RELATIONS de D01 :`, un élément par ligne, en ordre de lecture, indenté de 2 espaces :
```
  catalogue/livre[1]/titre[1] : parent=livre precedent=- suivant=stock enfants=0
```
- Le chemin : le nom de la racine, puis pour chaque descendant `/nom[n]`, où `n` est son rang parmi les frères **de même nom** (à partir de 1).
- `precedent` / `suivant` : le nom du frère juste avant / juste après, quel que soit son nom ; `-` s'il n'y en a pas. La racine n'a ni parent ni frère.
- `enfants` : le nombre d'éléments enfants directs.

### ☐ Étape 9 — Des données à plat vers XML (0.2.1, 0.2.4 S6)

Chaque fiche de `Data.BOOKS` est une suite de valeurs sans structure, dans l'ordre de `Data.FIELDS`. Convertis-la en **une ligne** XML :
- `id` et `langue` décrivent la fiche : ce sont des **attributs**, dans cet ordre, entre guillemets doubles ;
- `titre`, `auteur`, `annee` sont le contenu : des **éléments enfants**, dans cet ordre ;
- la racine s'appelle `livre`.
- Puis passe cette ligne à **ton** portier et au parseur :
```
CONVERSION <livre id="b1" langue="fr"><titre>Dune</titre>…</livre> | portier OK | parseur OK
```
- **Question :** qu'est-ce que XML apporte par rapport à `b1;Dune;Herbert;1965;fr` ? Que se passerait-il si un titre contenait un `;` ? Et un `<` (réponse au projet 2) ?

### ☐ Étape 10 — Le `main` de `Gate`

Dans l'ordre : une ligne par document de `Data.SUBMISSIONS`, le BILAN, le PLAN, les RELATIONS, puis les 3 CONVERSION.

---

## Checklist API (vérifiée par `Check`)

| Élément | Étape | ☐ |
|---|---|---|
| `codePoints()` | 1 | ☐ |
| une pile : `Deque`, `push`, `pop`, `peek` | 4 | ☐ |
| `record`, `enum` (les règles) | 2, 4 | ☐ |
| `XmlKit.wellFormed(` | 6 | ☐ |
| ~~`Character.isWhitespace`~~, ~~`equalsIgnoreCase`~~ | **interdits** (pièges) | — |
| ~~`javax.xml`~~, ~~`org.w3c`~~, ~~`org.xml`~~, ~~les autres méthodes de `XmlKit`~~ | **interdits** (sections suivantes) | — |

---

## Sortie attendue complète

C'est le contrat exact que vérifie `Check` :

```
a remplir
```
