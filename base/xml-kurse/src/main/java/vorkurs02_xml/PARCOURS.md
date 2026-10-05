# Vorkurs 0.2 (XML allgemein) — Mode d'emploi

Lis ce fichier **en entier une fois** avant de commencer. Il répond à cinq questions : par où commencer, comment le dossier est rangé, comment lire une consigne, comment travailler, et comment se comporter quand on bloque.

---

## 1. Ce que tu vas faire, en une phrase

Tu vas **construire 13 applications** (les projets) pour **comprendre** XML de l'intérieur : d'abord en écrivant toi-même ce que fait un parseur, puis en utilisant les vraies API de Java. Ensuite, tu **refais de mémoire, à intervalles espacés**, 10 petits drills chronométrés pour les **retenir**.

| | Projets (`projects/`) | Drills de rappel (`drills/`) |
|---|---|---|
| But | comprendre, concevoir, résoudre un problème | retrouver vite et sans aide |
| Durée | 2 à 6 h chacun, en plusieurs séances | 15 à 30 min chacun |
| Combien de fois | une fois, puis p08 et p13 refaits 2 à 3 semaines plus tard | 6 fois chacun (J0, J+1, J+3, J+7, J+14, J+30) |
| Aide autorisée | Javadoc, cours Notion, papier | **aucune** pendant le drill |

---

## 1 bis. La règle du crescendo : seulement ce que le cours a déjà montré

Jusqu'à la section 0.2.16, le cours ne montre **aucune** API Java pour XML. Dans les projets 1 à 7, tu écris donc **tes propres outils** (diagnostic, normalisation, arbre, validateur DTD, résolveur de namespaces, moteur XPath). Pour savoir si tu as raison, un arbitre est fourni : l'outil **`xmlkit.XmlKit`**, un vrai parseur utilisé comme une boîte noire. À partir de 0.2.17, tu utilises les vraies API, et `XmlKit` est **interdit**.

| Projet | Sections | Ce que tu as le droit d'utiliser | Interdit, car vu plus tard |
|---|---|---|---|
| p01 | 0.2.1 → 0.2.5 | `XmlKit.wellFormed` | toute API XML de Java ; les autres méthodes de `XmlKit` |
| p02 | 0.2.6 → 0.2.8 | + `XmlKit.value` | API XML de Java, `select`, `validate…` |
| p03 | 0.2.9 → 0.2.10 | + `XmlKit.select`, `XmlKit.validateDtd` | API XML de Java, `validateXsd` |
| p04 | 0.2.11 | `validateDtd`, `value` | API XML de Java, `select`, `validateXsd` |
| p05 | 0.2.12 → 0.2.13 | + `XmlKit.wellFormedNs` | API XML de Java, `select`, `validateXsd` |
| p06 | 0.2.14 → 0.2.15 | `XmlKit.validateXsd`, `XmlKit.file` | API XML de Java |
| p07 | 0.2.16 | `XmlKit.select`, `XmlKit.value` (tu écris le XPath) | API XML de Java |
| p08 | 0.2.17 | DOM, `Transformer` | `XmlKit`, SAX, StAX, `javax.xml.xpath`, `javax.xml.validation` |
| p09 | 0.2.18 | SAX | `XmlKit`, DOM, StAX, XPath, validation |
| p10 | 0.2.19 | StAX | `XmlKit`, DOM, SAX, XPath, validation |
| p11 | 0.2.20 | DOM, SAX, StAX | `XmlKit`, XPath, validation, `double` pour les prix |
| p12 | 0.2.21 | tout, plus la configuration sûre et `SchemaFactory` | `XmlKit`, `getMessage()`, XPath |
| p13 | 0.2.22 | tout : validation et XPath par l'API | `XmlKit` |

**Pourquoi n'affiche-t-on jamais le texte d'une exception ?** Les messages des parseurs sont traduits selon la langue de la machine (ici l'allemand). Les projets affichent donc des **verdicts**, des **lignes**, des **noms de classes** ou des **codes** stables (`cvc-pattern-valid`), jamais le reste du message.

---

## 2. Par où commencer (aujourd'hui)

1. Lis ce fichier jusqu'au bout.
2. Ouvre `projects/p01_gate/TODO.md` en **aperçu Markdown** (dans IntelliJ, l'icône « Preview » en haut à droite de l'éditeur).
3. Suis la section 4 ci-dessous, « Comment faire un projet ».
4. Quand p02 affiche `PROJET REUSSI`, fais le drill `drills/r01_syntax`, en suivant la section 5.

**L'ordre complet :**

```
p01
p02 → r01
p03
p04 → r02
p05 → r03
p06 → r04
p07 → r05
p08 → r06
p09 → r07
p10 → r08
p11
p12 → r09
p13 → r10 (1er passage, puis test final 2 semaines plus tard)
```

Pendant tout ce temps, les **répétitions** des drills déjà faits passent **avant** le projet du jour (section 6).

---

## 3. La disposition des dossiers

```
base/xml-kurse/
├── pom.xml                      ← Java 17, aucune dépendance
└── src/main/java/
    ├── projectkit/
    │   └── ProjectChecker.java  ← le moteur des Check (ne pas modifier)
    ├── xmlkit/
    │   └── XmlKit.java          ← OUTIL FOURNI : l'arbitre des projets 1 à 7 (lis sa Javadoc)
    └── vorkurs02_xml/
        ├── PARCOURS.md          ← ce fichier (le mode d'emploi)
        ├── projects/
        │   ├── README.md        ← la liste des 13 projets, à cocher
        │   └── p06_schema/
        │       ├── TODO.md      ← L'ÉNONCÉ : tu le lis
        │       ├── Data.java    ← les données (ou l'accès aux fichiers) : tu ne les modifies pas
        │       ├── files/       ← de vrais fichiers XML, XSD… (certains projets)
        │       ├── Check.java   ← le correcteur : tu le LANCES, tu ne le modifies pas
        │       ├── solution/    ← la correction : tu ne l'ouvres qu'à la fin
        │       └── (tes fichiers) ← TOUT le reste, c'est TOI qui le crées ici
        └── drills/
            ├── README.md        ← règles des drills + tableau de suivi des répétitions
            ├── Data.java        ← l'accès à files/campus.xml, partagé par les drills
            └── r01_syntax/
                ├── TODO.md      ← les défis + la carte mémoire repliée en bas
                ├── Check.java
                ├── solution/
                └── (ton RecallNN.java)
```

**L'outil `XmlKit`** (projets 1 à 7, drills 1 à 5) :
- `wellFormed(xml)` / `wellFormedNs(xml)` : `OK` ou `KO ligne:colonne`, sans puis avec les règles des namespaces ;
- `value(xml, expression[, préfixes])` : une valeur lue par le parseur (une expression XPath) ;
- `select(xml, expression[, préfixes])` : des nœuds, rendus en texte ;
- `validateDtd(xml, dtdExternes)` : `VALID`, `INVALID n (ligne l)` ou `NOT_WELL_FORMED …` ;
- `validateXsd(cheminXsd, xml)` : la liste des erreurs `ligne code` ;
- `file(TaClasse.class, "nom")` : le chemin d'un fichier du dossier de ton paquet.

**Où créer tes fichiers :** dans le **même dossier** que le `TODO.md` (le même paquet). Un seul nom est imposé : la classe du `main`, écrite en haut du `TODO.md` (`Gate`, `TextGate`… ; pour les drills, `Recall01`, `Recall02`…). En p06, tu crées aussi `catalog.xsd`, au même endroit. Le correcteur lit tous tes `.java` (et tes `.xsd`), sauf `Data.java`, `Check.java`, `solution/` et `files/`.

---

## 4. Comment faire un projet

### 4.1 Lire le `TODO.md` (dans cet ordre)

| Partie du `TODO.md` | Ce que tu en fais |
|---|---|
| **En-tête** (notions visées, donné, à créer) | Tu sais quelles notions tu vas pratiquer et quel nom donner au `main`. |
| **Le problème** | Tu comprends l'application dans son ensemble. Ouvre les fichiers de données. Ne code rien encore. |
| **Tableau de bord** (les étapes ☐) | Ta feuille de route. Chaque étape contient la règle, les lignes exactes à afficher (dans un bloc gris), et des **questions**. |
| **Checklist API** | Ce que `Check` cherchera dans ton code, et ce qui est **interdit**. |
| **Sortie attendue complète** | Le contrat exact, ligne par ligne. `Check` compare au caractère près. |

### 4.2 Travailler, étape par étape

1. **Prévois à la main** 2 ou 3 lignes de la sortie attendue, à partir des données : « ce document est-il bien formé ? quelle valeur le parseur relira-t-il ? ». Si tu ne peux pas le prévoir, relis la section du cours.
2. **Conçois sur papier** : quels records, enums, classes ? Qui fait quoi ? C'est **toi** qui décides.
3. **Crée la classe du `main`** tout de suite, même vide, pour lancer `Check` dès le début.
4. **Fais une étape à la fois.** Code-la, lance `Check`, corrige, coche ☐ → ☑.
5. **Réponds aux questions de l'étape par écrit**, en commentaire dans ton code.

### 4.3 Lancer `Check` et lire sa réponse

Clic droit sur `Check.java` → **Run 'Check.main()'**. Ça marche que IntelliJ ait ouvert le dépôt entier ou seulement `base/xml-kurse`.

| Ligne | Signification | Que faire |
|---|---|---|
| `[ERREUR] classe introuvable` | ta classe `main` n'existe pas, ou mauvais nom ou paquet | crée-la avec le nom exact |
| `[ERREUR] ton programme a lance …` | ton `main` a planté | lis l'exception, corrige |
| `[FAIL] sortie : 12/23 lignes justes … (ligne 13)` | les 12 premières lignes sont bonnes | compare `attendu` et `obtenu` caractère par caractère |
| `[FAIL] API : encore a placer …` | ces éléments n'apparaissent pas encore | normal tant que tu n'as pas fini |
| `[FAIL] API : … interdit …` | une notion d'une section suivante | remplace-la (section 1 bis) |
| `*** PROJET REUSSI ***` | tout est juste | section 4.5 |

**Sans IntelliJ**, depuis `base/xml-kurse` (Git Bash) : `javac --release 17 -encoding UTF-8 -proc:none -d target/classes $(find src/main/java -name '*.java')`, puis `java -cp target/classes vorkurs02_xml.projects.p01_gate.Check`. Sans `-encoding UTF-8`, les accents cassent sous Windows.

### 4.4 Quand tu bloques (la règle des 3 paliers)

| Palier | Combien de temps | Ce que tu fais |
|---|---|---|
| 1 | jusqu'à 20 min | relis l'étape et ses questions ; relis la section du cours dans Notion ; ouvre la **Javadoc** (`javax.xml.parsers`, `org.w3c.dom`, `org.xml.sax`, `javax.xml.stream`, `javax.xml.validation`, `javax.xml.xpath`) |
| 2 | 20 min de plus | relis la **carte mémoire** du drill du même thème ; ou demande-moi un **indice** sur ce point précis |
| 3 | en dernier recours | ouvre `solution/`, lis **uniquement** la méthode qui te bloque, ferme, réécris de mémoire. Note `// AIDE : solution consultée` |

**Ce qu'il ne faut jamais faire :** copier-coller depuis `solution/` ; modifier `Data.java`, `Check.java`, les fichiers de `files/`, `projectkit` ou `xmlkit` ; sauter les questions ; coder sans avoir prévu la sortie.

### 4.5 Quand c'est réussi

1. Ouvre `solution/` et **compare ta conception**. Les commentaires expliquent le **pourquoi** et les **pièges**.
2. Si tu veux, montre-moi ton code : je relis ta **conception**, pas seulement le résultat.
3. Coche le projet dans `projects/README.md`, puis fais le drill associé (section 2).

---

## 5. Comment faire un drill de rappel

1. Note l'heure de départ et le **chrono cible** (en haut du `TODO.md`).
2. Crée `RecallNN.java` dans le dossier du drill.
3. **Fais les défis D01, D02…** La ligne exacte à afficher est juste sous chaque défi.
4. **Rien d'autre que ta mémoire.** Un défi bloque plus de 3 minutes : marque-le ✗, passe au suivant.
5. Lance `Check`.
6. **Après seulement,** ouvre la **carte mémoire** et relis ce qui concerne tes ✗.
7. Note la date, le temps et le nombre de ✗ dans `drills/README.md`.
8. **Avant chaque répétition, supprime ton `RecallNN.java`.**

---

## 6. Le rythme

| Durée | Activité |
|---|---|
| 10 à 20 min | les **répétitions dues** aujourd'hui : toujours en premier |
| 60 min | le projet en cours, une ou deux étapes |
| 10 à 20 min | si un projet vient d'être fini : le premier passage (J0) de son drill |

Après J0 : J+1, J+3, J+7, J+14, J+30. Un drill raté recule d'un palier ; un drill parfait et rapide peut en sauter un.

---

## 7. Comment savoir que le chapitre est acquis

- [ ] Les 13 projets affichent `PROJET REUSSI`, et toutes les questions ont une réponse écrite.
- [ ] Les 10 drills ont passé la répétition R3 (J+7, sans carte).
- [ ] r10 (kata mixte) passe en moins de 30 minutes, sans carte.
- [ ] p08 et p13 ont été refaits **depuis un dossier vide**, 2 à 3 semaines plus tard.
- [ ] Tu sais expliquer, sans notes, pourquoi un import XML public refuse le DOCTYPE, et quelle couche de la chaîne de p13 attrape quelle erreur.

---

## 8. Ce que couvre le parcours

| Cours | Projets | Drills |
|---|---|---|
| 0.2.1 Pourquoi XML | p01 | — |
| 0.2.2 Anatomie : déclaration, prologue, racine | p01 | r01 |
| 0.2.3 Éléments, balises, éléments vides | p01 | r01 |
| 0.2.4 Attributs ; élément ou attribut | p01 | r01 |
| 0.2.5 Noms, casse, imbrication, parent / enfant / frère | p01 | r01 |
| 0.2.6 Texte, blancs, échappement, références | p02 | r01, r10 |
| 0.2.7 Entités et CDATA | p02 | r01 |
| 0.2.8 Commentaires, PI, DOCTYPE, lookahead | p02 | r01 |
| 0.2.9 Bien formé contre valide | p03 | r02 |
| 0.2.10 XML comme arbre | p03 | r06 |
| 0.2.11 DTD | p04 | r02, r10 |
| 0.2.12 → 0.2.13 Namespaces | p05 | r03, r10 |
| 0.2.14 → 0.2.15 XSD | p06 | r04, r10 |
| 0.2.16 XPath | p07 | r05, r10 |
| 0.2.17 DOM | p08 | r06, r10 |
| 0.2.18 SAX | p09 | r07, r10 |
| 0.2.19 StAX | p10 | r08, r10 |
| 0.2.20 DOM contre SAX contre StAX | p11 | — |
| 0.2.21 Sécurité | p12 | r09, r10 |
| 0.2.22 Le laboratoire | p13 | r09 |

L'ancien format (exercices « remplir le corps ») a été retiré ; il reste consultable dans l'historique git (commit `194194b`).
