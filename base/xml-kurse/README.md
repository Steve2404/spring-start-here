# XML-Kurse — Vorkurs 0.2 : XML allgemein

Exercices de code (pas de quiz) pour le cours Notion
**Start Here → Kurse → Kapitel 0 → 0.2 XML allgemein (0.2.1 à 0.2.22)**.

Format **projet**, le même que `base/annotation-kurse` et les chapitres refaits du dépôt `Kurse` :
- tu reçois seulement un énoncé (`TODO.md`), des données (`Data.java`, parfois des fichiers dans `files/`) et un correcteur (`Check.java`) ;
- **tu crées toi-même** tous les fichiers ;
- `Check` lance ton `main`, compare la sortie ligne par ligne, puis lit tes sources (API exigée, notions des sections suivantes refusées) ;
- un corrigé commenté est rangé dans `solution/`.

> **En cours de construction.** Les projets p01 à p08 sont prêts. p09 à p13, les drills et le mode d'emploi `PARCOURS.md` arrivent. L'ancien format (exercices « remplir le corps ») a été retiré ; il reste consultable dans l'historique git (commit `194194b`).

## La règle du crescendo

Jusqu'à la section 0.2.16, le cours ne montre **aucune** API Java pour XML : tu écris tes propres outils. L'outil fourni `xmlkit.XmlKit` sert d'arbitre (un vrai parseur, utilisé comme boîte noire). À partir de 0.2.17, tu utilises les vraies API (DOM, SAX, StAX…) et `XmlKit` est interdit.

| Projet | Cours | Classe `main` | Ce que tu construis |
|---|---|---|---|
| `p01_gate` | 0.2.1 → 0.2.5 | `Gate` | un portier qui diagnostique la bonne forme caractère par caractère (noms, balises, attributs, pile LIFO, racine) |
| `p02_textgate` | 0.2.6 → 0.2.8 | `TextGate` | écrire et relire du texte comme un parseur : échappement, normalisation, références, entités, CDATA, lookahead |
| `p03_tree` | 0.2.9 → 0.2.10 | `Tree` | ton propre arbre de nœuds, confronté au parseur ; la chaîne bien formé → valide → métier |
| `p04_dtd` | 0.2.11 | `DtdCheck` | ton validateur DTD : modèles de contenu en regex, ATTLIST, interne contre externe |
| `p05_names` | 0.2.12 → 0.2.13 | `Names` | ton résolveur de namespaces : portées, défaut, `xsi:type`, équivalence de documents |
| `p06_schema` | 0.2.14 → 0.2.15 | `SchemaCheck` | **tu écris le XSD** du catalogue, jugé par 18 documents |
| `p07_xpath` | 0.2.16 | `Queries` | des requêtes XPath, puis ton propre mini moteur XPath |
| `p08_dom` | 0.2.17 | `DomLab` | DOM : naviguer, NodeList vivante, namespaces, migration v1 → v2, `Transformer` |

## Lancer

**IntelliJ :** ajoute `base/xml-kurse/pom.xml` comme projet Maven, puis clic droit sur un `Check.java` → *Run*. Argument `solution` pour vérifier le corrigé.

**Terminal (Git Bash), depuis `base/xml-kurse` :**

```bash
javac --release 17 -encoding UTF-8 -proc:none -d target/classes $(find src/main/java -name '*.java')
java -cp target/classes vorkurs02_xml.projects.p01_gate.Check solution
```
