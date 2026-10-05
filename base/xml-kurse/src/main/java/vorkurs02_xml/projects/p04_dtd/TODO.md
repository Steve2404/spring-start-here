# Projet 4 — Le validateur DTD maison (cours 0.2.11)

> Première fois ? Lis d'abord le mode d'emploi [`vorkurs02_xml/PARCOURS.md`](../../PARCOURS.md) : comment lire cette fiche, lancer `Check`, quoi faire en cas de blocage.

**Notions visées :** ce qu'une DTD décrit, DOCTYPE et sous-ensemble interne, `<!ELEMENT>` et ses modèles de contenu (séquence, choix, `? * +`, `#PCDATA`, mixte, `EMPTY`, `ANY`), `<!ATTLIST>` (types `CDATA`, `ID`, `IDREF`, énumérations ; `#REQUIRED`, `#IMPLIED`, `#FIXED`, défaut), DTD externe par `SYSTEM`, interne contre externe : qui gagne, ce que doivent faire les processeurs validants et non validants (0.2.11).  
**Ce qui est donné :** `Data.java` (la DTD externe `catalog.dtd` sous forme de texte, et 12 documents) et `Check.java`. De `xmlkit.XmlKit` : `validateDtd(xml, Map.of("catalog.dtd", Data.CATALOG_DTD))` est l'arbitre ; `value` sert à l'étape 5.  
**Ce que TU crées :** tous les fichiers `.java`, dans le paquet `vorkurs02_xml.projects.p04_dtd`. La classe du `main` s'appelle **`DtdCheck`**. Reprends ton arbre du projet 3 (copie ton code) : un élément, ses attributs, ses enfants éléments, son texte direct.

---

## Le problème

Un parseur validant compare chaque document à sa DTD. Tu écris **ton** validateur : il lit la DTD, la fusionne avec le sous-ensemble interne, transforme chaque modèle de contenu en **expression régulière**, puis juge chaque élément et chaque attribut. Le vrai parseur arbitre : vous devez être d'accord sur **valide / invalide**.

---

## Tableau de bord

### ☐ Étape 1 — Lire les déclarations (0.2.11 S3, S4)

Retire d'abord les commentaires `<!-- … -->` de la DTD.
- **`<!ELEMENT nom modèle>`** : garde le modèle avec ses blancs **réduits à un seul espace**.
- **`<!ATTLIST élément …>`** : une suite de définitions `nom type défaut`, sur une ou plusieurs lignes :
  - type : `CDATA`, `ID`, `IDREF`, ou une liste `(a|b|c)` ;
  - défaut : `#REQUIRED`, `#IMPLIED`, `#FIXED "valeur"`, ou `"valeur"` ;
  - un `record` pour une définition est une bonne idée.

### ☐ Étape 2 — Du modèle de contenu à l'expression régulière (0.2.11 S3)

Les enfants **éléments** d'un élément s'écrivent comme un texte : chaque nom suivi de `;` (`name;price;tag;`). Le texte et les autres nœuds n'y figurent pas. Un modèle devient une regex sur ce texte :

| Morceau du modèle | Regex |
|---|---|
| `EMPTY` | (rien : la chaîne vide) |
| `ANY` | `(?:[^;]+;)*` |
| un nom `x` | `(?:x;)` |
| `#PCDATA` | (rien) |
| un groupe `(a, b, c)` | `(?:` + les morceaux collés + `)` |
| un groupe `(a \| b \| c)` | `(?:` + les morceaux séparés par `\|` + `)` |
| un quantificateur `?`, `*`, `+` après un nom ou un groupe | recopié juste après son morceau |

- Lis le modèle en **jetons** : `(`, `)`, `,`, `|`, `?`, `*`, `+`, ou un mot. Puis une **descente récursive** : un morceau est un nom ou un groupe, suivi d'un quantificateur facultatif.
- Pour chaque `<!ELEMENT>` de la DTD externe, dans l'ordre :
```
MODELE bundle : (item, item+) -> [(?:(?:item;)(?:item;)+)]
```
- **Question :** pourquoi un modèle mixte doit-il s'écrire `(#PCDATA | b | i)*` et jamais `(#PCDATA, b)` ?

### ☐ Étape 3 — Fusionner l'interne et l'externe (0.2.11 S2, S5, S6)

Dans chaque document, le DOCTYPE donne le **nom attendu de la racine** (le premier mot après `<!DOCTYPE`) et, peut-être, un sous-ensemble interne `[ … ]`. Tous désignent `SYSTEM "catalog.dtd"`, c'est-à-dire `Data.CATALOG_DTD`.
- Lis le sous-ensemble **interne en premier**, puis l'externe.
- Un attribut (élément + nom) déjà déclaré garde sa **première** déclaration : c'est ainsi que l'interne « l'emporte ».
- Mais un `<!ELEMENT>` déjà déclaré est une **erreur** : `<nom> -> declare deux fois`. La première déclaration reste en vigueur.
- **Question :** pourquoi « l'interne remplace tout » est-il faux ? Que se passe-t-il avec `V10` ?

### ☐ Étape 4 — Valider un document (0.2.11 S3, S4)

Les erreurs, dans cet ordre :
1. les erreurs de la fusion (étape 3) ;
2. le nom de la racine diffère du nom du DOCTYPE : `racine <nom> != DOCTYPE <nom>` ;
3. chaque élément, en **ordre de lecture**, puis ses enfants :
   - **a.** pas de `<!ELEMENT>` : `<nom> -> non declare`. Sinon, la suite de ses enfants ne correspond pas **entièrement** à la regex : `<nom> -> enfants`. Sinon, problème de texte : `<nom> -> texte`. Un seul de ces trois messages au maximum ;
     - `EMPTY` : aucun contenu, pas même un blanc ;
     - modèle sans `#PCDATA` ni `ANY` : seul un texte **blanc** (l'indentation) est permis ;
   - **b.** chaque attribut **présent** sans déclaration, dans l'ordre du texte : `<élément>@<attribut> -> non declare` ;
   - **c.** chaque attribut **déclaré** pour cet élément, dans l'ordre de la DTD fusionnée (interne d'abord) :
     - absent et `#REQUIRED` : `<élément>@<attribut> -> manquant` ;
     - s'il est présent, on note `<élément>@<attribut>=<valeur>`, puis :
       - une liste qui ne contient pas la valeur : `… -> hors liste` ;
       - un `#FIXED` de valeur différente : `… -> fixe <valeur fixée>` ;
       - un `ID` qui n'est pas un nom (une lettre, `_` ou `:`, puis lettres, chiffres, `.`, `_`, `:`, `-`) : `… -> ID invalide`. Sinon, un `ID` déjà vu **dans tout le document** : `… -> ID double` ;
       - un `IDREF` : mis de côté ;
4. à la fin, chaque `IDREF` mis de côté dont la valeur n'est l'`ID` d'aucun élément, dans l'ordre : `… -> IDREF sans cible`.

```
V07 INVALIDE 4 : catalog@version=3 -> fixe 2 ; section@id -> manquant ; … | parseur INVALID 4 (ligne 3) | accord oui
```
- `VALIDE`, ou `INVALIDE n : ` et les erreurs séparées par ` ; ` ;
- puis le résultat de l'arbitre, puis `accord oui` si vous êtes d'accord sur valide / invalide, sinon `accord non`.
- **Question :** pourquoi les `IDREF` ne peuvent-ils être vérifiés qu'à la fin ?
- **Question :** `V06` : un seul élément inconnu produit deux erreurs. Lesquelles, et pourquoi ?

### ☐ Étape 5 — Les valeurs par défaut (0.2.11 S4, S7)

Pour l'élément `catalog` de `V01`, puis le premier `item` de `V09` : ses attributs **après** ajout des défauts.
- D'abord ceux du texte, dans l'ordre ;
- puis chaque attribut déclaré pour cet élément, absent, qui a une valeur (`#FIXED` ou défaut), dans l'ordre de la DTD fusionnée.

Puis, pour chacun, la valeur lue par `XmlKit.value(texte, "string(<chemin>/@<nom>)")`, ou `absent` si elle est vide. Le chemin est `/catalog` pour `V01` et `//item[1]` pour `V09`.
```
DEFAUTS V09 item : sku=a1 color=red status=old | parseur : sku=a1 color=red status=old
```
- **Question :** pour `V01`, le parseur ne voit **aucun** défaut. Le parseur de lecture ne charge pas la DTD externe, alors qu'il applique celle de l'interne (`V09`). Est-ce permis à un processeur non validant ? Quel danger pour une application qui compte sur une valeur par défaut ?

### ☐ Étape 6 — Le `main` de `DtdCheck`

Dans l'ordre : les lignes MODELE, les lignes `V01` à `V12`, puis les deux lignes DEFAUTS.

---

## Checklist API (vérifiée par `Check`)

| Élément | Étape | ☐ |
|---|---|---|
| `Pattern`, `Matcher` | 1, 2 | ☐ |
| `Pattern.matches(` | 4 | ☐ |
| `putIfAbsent(` (la première déclaration gagne) | 3 | ☐ |
| `record ` | 1 | ☐ |
| `XmlKit.validateDtd(` | 4 | ☐ |
| `XmlKit.value(` | 5 | ☐ |
| ~~`javax.xml`~~, ~~`org.w3c`~~, ~~`org.xml`~~, ~~`XmlKit.validateXsd`~~, ~~`XmlKit.select`~~ | **interdits** | — |

---

## Sortie attendue complète

C'est le contrat exact que vérifie `Check` :

```
a remplir
```
