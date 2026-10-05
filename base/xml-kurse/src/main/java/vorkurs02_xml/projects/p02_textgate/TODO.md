# Projet 2 — La passerelle de textes (cours 0.2.6 → 0.2.8)

> Première fois ? Lis d'abord le mode d'emploi [`vorkurs02_xml/PARCOURS.md`](../../PARCOURS.md) : comment lire cette fiche, lancer `Check`, quoi faire en cas de blocage.

**Notions visées :** caractères permis, blancs XML, fins de ligne, échappement dans le contenu et dans les attributs, normalisation des attributs, références de caractères et les cinq entités prédéfinies (0.2.6), entités internes, développement imbriqué sans récursion, CDATA (0.2.7), commentaires, processing instructions, DOCTYPE, lookahead après `<` (0.2.8).  
**Ce qui est donné :** `Data.java` et `Check.java`. L'outil `xmlkit.XmlKit` sert d'arbitre : `wellFormed(xml)` (vu au projet 1) et, nouveau, `value(xml, expression)`, qui rend la valeur **lue par un vrai parseur**. Les expressions à lui passer sont données dans chaque étape : recopie-les telles quelles, leur langage (XPath) n'arrive qu'au projet 7.  
**Ce que TU crées :** tous les fichiers `.java`, dans le paquet `vorkurs02_xml.projects.p02_textgate`. La classe du `main` s'appelle **`TextGate`**.

**Pour vérifier :** lance `Check.java`. Ne regarde `solution/` qu'à la fin.

---

## Le problème

Une passerelle échange des textes avec des partenaires, en XML. Elle doit savoir faire deux choses, au caractère près :
1. **écrire** n'importe quelle valeur métier dans un fichier XML sans le casser ;
2. **relire** une valeur exactement comme le fait un parseur (fins de ligne, blancs, références, entités, CDATA).

Pour chaque cas, ta réponse est comparée à celle du vrai parseur. Toujours **sans** API XML de Java.

**Affichage commun.** Une valeur s'affiche entre crochets `[…]` quand ses espaces comptent. Les caractères de contrôle s'y affichent **rendus visibles** : une tabulation devient les deux caractères `\t`, un saut de ligne `\n`, un retour chariot `\r`.

---

## Tableau de bord

### ☐ Étape 1 — Écrire sans casser (0.2.6 S2, S6, S7)

Pour chaque valeur de `Data.VALUES`, numérotée `E01`, `E02`… :
- **Refus :** si la valeur contient un caractère **interdit en XML 1.0**, aucun échappement ne peut le sauver. Affiche `E11 REFUS U+0001` (code du caractère en hexadécimal majuscule, au moins 4 chiffres).
  - Permis : `9`, `A`, `D`, `20`–`D7FF`, `E000`–`FFFD`, `10000`–`10FFFF`.
  - Parcours par points de code : l'emoji tient sur deux `char`.
- **Pour le contenu d'un élément :**
  - `&` → `&amp;` et `<` → `&lt;` ;
  - `>` reste brut, **sauf** s'il suit `]]` (il fermerait un `]]>`) : alors `&gt;` ;
  - un `\r` → `&#13;` (sinon la relecture le transformerait en `\n`) ;
  - tout le reste brut.
- **Pour un attribut entre guillemets doubles :**
  - `&` → `&amp;`, `<` → `&lt;`, `"` → `&quot;` ;
  - les blancs littéraux `\t`, `\n`, `\r` → `&#9;`, `&#10;`, `&#13;` (voir l'étape 2) ;
  - l'apostrophe et `>` restent bruts.
- Construis `<v a="ATTRIBUT">TEXTE</v>`, puis relis-le : `XmlKit.value(xml, "string(/v)")` (le texte) et `XmlKit.value(xml, "string(/v/@a)")` (l'attribut). Compare chacun à la valeur d'origine.
```
E01 texte=Tom &amp; Jerry | attribut=Tom &amp; Jerry | relu=oui,oui
```
  - Les formes échappées s'affichent avec la règle d'affichage commune, mais **sans** crochets.
- **Question :** pourquoi `>` est-il permis dans du texte, alors que `<` ne l'est jamais ?

### ☐ Étape 2 — Relire comme le parseur (0.2.6 S3, S4, S5, S8)

**Les attributs.** Chaque entrée de `Data.RAW_ATTRIBUTES` est le texte écrit **entre les guillemets** d'un attribut. Calcule sa valeur normalisée, dans cet ordre :
1. fins de ligne : `\r\n` → `\n`, puis tout `\r` restant → `\n` ;
2. chaque blanc **littéral** (`\t`, `\n`) → un espace ;
3. **seulement ensuite**, les références (étape 3). Ce qu'elles produisent n'est plus touché.
- Les espaces ne sont **ni fusionnés ni rognés**.
- Le parseur : `XmlKit.value("<a v=\"" + brut + "\"/>", "string(/a/@v)")`.
```
N01 [a\tb] -> [a b] | parseur [a b]
```

**Les contenus.** Chaque entrée de `Data.RAW_TEXTS` est le contenu brut d'un élément : fins de ligne, puis références. Les blancs restent tels quels. Le parseur : `XmlKit.value("<t>" + brut + "</t>", "string(/t)")`. Même format, préfixe `T01`…

- **Question :** `N04` garde une tabulation, `N01` non. Pourquoi l'ordre « blancs, puis références » est-il exactement ce qui permet de protéger un blanc ?
- **Question :** le parseur garde les blancs du contenu (`T03`). Qui décide s'ils comptent ? Que signale `xml:space` ?

### ☐ Étape 3 — Les références (0.2.6 S9)

Pour chaque entrée de `Data.REFERENCES` :
- **une seule passe** de gauche à droite : ce qu'une référence produit n'est jamais relu (`&amp;lt;` donne `&lt;`, pas `<`) ;
- les cinq noms prédéfinis `lt`, `gt`, `amp`, `apos`, `quot`, sensibles à la casse ;
- `&#` + 1 à 7 chiffres décimaux ; `&#x` (x **minuscule**) + 1 à 6 chiffres hexadécimaux ;
- les refus :
  - pas de `;`, ou un corps qui commence par `#` sans être l'une des deux formes : `REFUS syntaxe` ;
  - un point de code interdit (étape 1) : `REFUS caractere interdit` ;
  - un autre nom : `REFUS entite inconnue <nom>`.
- Puis le verdict du parseur sur `<r>` + référence + `</r>` (avec `wellFormed`).
```
R01 &lt; -> [<] | parseur OK
R06 &#0; -> REFUS caractere interdit | parseur KO 1:8
```
- **Question :** `&eacute;` marche en HTML. Que faudrait-il en XML pour qu'il soit accepté ?

### ☐ Étape 4 — Les entités internes (0.2.7)

Chaque document de `Data.ENTITY_DOCS` a un DOCTYPE avec un sous-ensemble interne `[ … ]`, puis un élément racine.

1. **Les déclarations** `<!ENTITY nom "valeur">` (guillemets simples ou doubles) :
   - la **première** déclaration d'un nom gagne ;
   - ignore les entités **paramètres** (`<!ENTITY % …>`) et **externes** (`SYSTEM` ou `PUBLIC`).
2. **Le texte de remplacement :** dès la déclaration, seules les références **de caractères** (`&#…;`) sont remplacées. Les `&nom;` attendent l'utilisation.
3. **Le texte de la racine** (son contenu entre la balise ouvrante et la dernière fermante) :
   - développe les références : prédéfinies et de caractères comme à l'étape 3, et les entités **récursivement** ;
   - une section `<![CDATA[ … ]]>` donne son texte **brut**, sans rien développer ;
   - commentaires et PI ne produisent rien.
4. **Les refus**, au premier problème :
   - une entité déjà en cours de développement (un cycle) : `RECURSION a -> b -> a`, le chemin des entités ouvertes ;
   - plus de **100** développements d'entités (hors prédéfinies et références de caractères) pour tout le document : `LIMITE 100 expansions depassee` ;
   - un nom non déclaré : `ENTITE INCONNUE <nom>`.
5. **Le parseur :** si `wellFormed` dit `OK`, prends `XmlKit.value(doc, "string(/*)")`. Affiche-le entre crochets, ou, s'il dépasse 60 caractères, `accepte, N caracteres`. Sinon, affiche le verdict `KO …`.
```
X01 [Bonjour, L'equipe ACME & ses amis] | parseur [Bonjour, L'equipe ACME & ses amis]
X04 REFUS RECURSION a -> b -> a | parseur KO 1:11
```
- **Question :** `X05` : le parseur accepte, toi tu refuses. Qui a raison ? Calcule le nombre d'expansions, puis la taille si on montait à 9 niveaux. C'est l'attaque « billion laughs » (projet 12).
- **Question :** `X03` : pourquoi `&code;` reste-t-il tel quel dans la CDATA ?

### ☐ Étape 5 — Le lookahead après `<` (0.2.8)

**Le balisage de `Data.MARKUP_DOC`.** Classe chaque `<` du document, en testant les préfixes **les plus longs d'abord** :

| Commence par | Sorte |
|---|---|
| `<!--` | `COMMENT` |
| `<![CDATA[` | `CDATA` |
| `<!DOCTYPE` | `DOCTYPE` |
| `<?xml` + un blanc, **à l'indice 0** | `DECLARATION` |
| `<?` (ailleurs) | `PI` |
| `</` | `END_TAG` |
| `<` + une lettre, `_` ou `:` | `START_TAG` |
| autre | `INVALID` |

- Après chaque balisage, reprends la recherche **après sa fin** : `-->`, `]]>` ou `?>` selon la sorte, sinon le premier `>`. Un `<` dans une CDATA ou un commentaire n'est donc **pas** compté.
- Une `START_TAG` d'élément vide (`<vide/>`) reste une `START_TAG` : le lookahead ne voit que le début.
```
BALISAGE DECLARATION DOCTYPE COMMENT PI START_TAG …
```

**Les cas de `Data.MARKUP_CASES`.** Juge chaque document, au premier problème, puis ajoute le verdict du parseur :
- un commentaire qui contient `--`, ou dont le texte finit par `-` : `COMMENTAIRE_INVALIDE` ;
- une PI dont la cible (le premier mot) vaut `xml` **quelle que soit la casse** : `CIBLE_RESERVEE <cible>` ;
- un deuxième DOCTYPE : `DOCTYPE_DOUBLE` ;
- sinon `OK`. Si un DOCTYPE existe et que son nom (le premier mot après `<!DOCTYPE`) diffère du nom de la racine, ajoute ` mais DOCTYPE <nom> != racine <racine>`.
```
M07 OK mais DOCTYPE rapport != racine autre | parseur OK
```
- **Question :** pourquoi le parseur accepte-t-il `M07` ? Quelle sorte de règle (WFC ou VC) est violée ? Réponse complète au projet 3.
- **Question :** `xml-stylesheet` commence par `xml`. Pourquoi est-ce permis ?

### ☐ Étape 6 — Protéger un texte dans une CDATA (0.2.7 S6)

Pour chaque texte de `Data.CDATA_TEXTS`, écris une section CDATA qui le contient **exactement**. Une CDATA ne peut pas contenir `]]>` : coupe **entre** `]]` et `>`, ferme la section, puis rouvre-en une. Relis avec `XmlKit.value("<c>" + section + "</c>", "string(/c)")`.
```
C01 <![CDATA[if (a < b && c) {}]]> | relu=oui
```

### ☐ Étape 7 — Le `main` de `TextGate`

Dans l'ordre : les lignes `E`, `N`, `T`, `R`, `X`, `BALISAGE`, `M`, puis `C`.

---

## Checklist API (vérifiée par `Check`)

| Élément | Étape | ☐ |
|---|---|---|
| `codePoints()`, `appendCodePoint(` | 1, 3 | ☐ |
| `XmlKit.value(` | 1, 2, 4, 6 | ☐ |
| `XmlKit.wellFormed(` | 3, 4, 5 | ☐ |
| une pile ou un chemin des entités ouvertes (`Deque`) | 4 | ☐ |
| `putIfAbsent` (la première déclaration gagne) | 4 | ☐ |
| ~~`javax.xml`~~, ~~`org.w3c`~~, ~~`org.xml`~~, ~~`XmlKit.select`~~, ~~`XmlKit.validate…`~~ | **interdits** (sections suivantes) | — |

---

## Sortie attendue complète

C'est le contrat exact que vérifie `Check` :

```
a remplir
```
