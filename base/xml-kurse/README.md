# XML-Kurse — Vorkurs 0.2 : XML allgemein

Exercices de code (pas de quiz) pour le cours Notion
**Start Here → Kurse → Kapitel 0 → 0.2 XML allgemein (0.2.1 à 0.2.22)**.
Même méthode que le dépôt `Kurse` (OCP) : chaque exercice est une classe avec des
`TODO` à remplir, un `main()` qui vérifie tout avec `ExerciseChecker`, et un corrigé
commenté séparé. Niveau visé : **challenge / entretien technique**.

Tout vient du module `java.xml` du JDK : aucune dépendance externe.

## Structure

```
base/xml-kurse/
├── pom.xml                                   (Java 17, pas de dépendance)
└── src/main/
    ├── java/vorkurs02_xml/
    │   ├── ExerciseChecker.java              check(...) / summary()
    │   ├── Fixtures.java                     accès aux fichiers de test via le classpath
    │   ├── exercises/Exercise01..18_*.java   les TODO
    │   ├── solutions/Solution01..18_*.java   les corrigés commentés
    │   └── drills/
    │       ├── Campus.java                   données partagées par les drills
    │       ├── REVISION.md                   parcours + répétition espacée + suivi
    │       ├── exercises/Drill01..07_*.java
    │       └── solutions/SolutionDrill01..07_*.java
    └── resources/vorkurs02_xml/fixtures/     les vrais fichiers .xml / .xsd / .dtd (ex01 … ex18, drills)
```

## Les 18 exercices (rangés par thème du cours)

| # | Exercice | Cours | Ce que tu construis |
|---|---|---|---|
| 01 | `WellFormednessDiagnoser` | 0.2.2 – 0.2.9 | ton propre vérificateur de bonne formation (pile LIFO, noms, attributs, références), arbitré par le parseur du JDK sur 21 fichiers |
| 02 | `EscapingAndNormalization` | 0.2.6 | échapper / décoder / normaliser exactement comme le parseur (fins de ligne, attributs, références) |
| 03 | `EntitiesCdataAndMarkup` | 0.2.7 – 0.2.8 | lookahead après `<`, CDATA, entités récursives avec détection de cycle et limite anti « billion laughs » |
| 04 | `DtdValidationReport` | 0.2.9, 0.2.11 | rapport VALID / INVALID / NOT_WELL_FORMED, attributs ajoutés par la DTD, `getElementById` |
| 05 | `ContentModelCompiler` | 0.2.11 | compilateur « modèle de contenu DTD → regex » + mini-validateur |
| 06 | `NamespaceResolver` | 0.2.12 – 0.2.13 | résolution des QName à la main (pile de portées, `xmlns=""`, `xsi:type`) |
| 07 | `NamespaceAwareDom` | 0.2.12 – 0.2.13, 0.2.17 | comparer deux documents qui n'utilisent pas les mêmes préfixes |
| 08 | `XsdBatchValidation` | 0.2.15, 0.2.22 | valider un lot contre un XSD, causes racines, rapport par code `cvc-*` |
| 09 | `XsdAuthoring` | 0.2.14 – 0.2.15 | **écrire le XSD** : 7 morceaux, jugés par 18 documents |
| 10 | `XPathQueries` | 0.2.16 | requêtes XPath 1.0 : positions, axes inverses, namespaces |
| 11 | `MiniXPathEngine` | 0.2.16 | ton propre moteur XPath, comparé nœud pour nœud à celui du JDK |
| 12 | `DomNavigation` | 0.2.10, 0.2.17 | boîte à outils DOM : nœuds blancs, `ownText`, chemin XPath d'un nœud, `NodeList` vivante |
| 13 | `DomMigration` | 0.2.17 | migration v1 → v2 en modifiant le DOM, puis sérialisation |
| 14 | `SaxStreaming` | 0.2.18 | agrégation en flux, `characters()` en morceaux, arrêt anticipé |
| 15 | `StaxPullParsing` | 0.2.19 | curseur StAX : `nextTag`, `getElementText`, sauter un bloc, API événements |
| 16 | `ThreeParsersOneTask` | 0.2.20 | la même tâche en DOM, SAX et StAX sur 30 000 produits, et la table de choix |
| 17 | `XmlSecurity` | 0.2.21 | voir une XXE fonctionner (en local), puis sécuriser DOM / SAX / StAX / XSD |
| 18 | `XmlLabPipeline` | 0.2.22 | **capstone** : pipeline d'import couche par couche (bonne formation → namespace → XSD → métier) |

Les 7 drills et le plan de révision : voir `src/main/java/vorkurs02_xml/drills/REVISION.md`.

## Lancer un exercice

**IntelliJ** : ouvre `base/xml-kurse/pom.xml` comme projet Maven (ou ouvre le dépôt et
ajoute ce `pom.xml` comme projet Maven). Clic droit sur un `ExerciseNN_*.java` → *Run*.
Tant qu'un TODO n'est pas fait, le programme s'arrête sur
`UnsupportedOperationException: TODO 1 : ...` : c'est normal, c'est ton point de départ.

**Ligne de commande** (depuis `base/xml-kurse`) :

```
mvn -q compile
java -cp target/classes vorkurs02_xml.exercises.Exercise01_WellFormednessDiagnoser
```

Les fichiers de test sont lus **par le classpath** (`Fixtures.path("ex08/order.xsd")`) :
ça marche quel que soit le dossier de lancement, à condition que `src/main/resources`
soit copié dans `target/classes` (Maven et IntelliJ le font).

## Règles de ce dépôt

- Tout comportement cité dans un énoncé (message, ligne d'erreur, résultat) a été
  **vérifié en exécutant le vrai JDK 17**, jamais écrit de mémoire.
- Chaque corrigé passe 100 % des tests de son exercice ; chaque exercice non résolu
  s'arrête sur son TODO 1.
- Les messages du parseur sont **traduits** selon la langue de la machine (allemand
  ici) : les tests ne comparent jamais un message, seulement des codes (`cvc-...`),
  des lignes et des résultats.
- `.gitattributes` empêche Git de convertir les fins de ligne des `.xml` / `.xsd` /
  `.dtd` (l'exercice 02 contient volontairement un CRLF).
