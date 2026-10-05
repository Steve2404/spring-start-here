# Annotation-Kurse — Vorkurs 0.4 : annotations Java & reflection

Exercices de code (pas de quiz) pour le cours Notion
**Start Here → Kurse → Kapitel 0 → 0.4 Java-Annotationen & Reflection (0.4.1 à 0.4.15)**.

Format **projet**, le même que les chapitres refaits du dépôt `Kurse` (OCP) :
- tu reçois seulement un énoncé (`TODO.md`), des données (`Data.java`) et un correcteur (`Check.java`) ;
- **tu crées toi-même** tous les fichiers : annotations, classes, records, processor ;
- `Check` lance ton `main`, compare la sortie ligne par ligne, puis lit tes sources (API exigée, notions des sections suivantes refusées) ;
- un corrigé commenté est rangé dans `solution/`.

Tout vient du JDK (Java 17) : aucune dépendance.

**Commence par [`src/main/java/vorkurs04_annotations/PARCOURS.md`](src/main/java/vorkurs04_annotations/PARCOURS.md)** : c'est le mode d'emploi complet.

## Contenu

| | |
|---|---|
| 9 projets | [`projects/README.md`](src/main/java/vorkurs04_annotations/projects/README.md) : de `@interface` jusqu'à un mini-Spring (`MiniBoot`) |
| 8 drills | [`drills/README.md`](src/main/java/vorkurs04_annotations/drills/README.md) : rappel chronométré + répétition espacée |
| `projectkit.Javac` | outil fourni : javac en mémoire, chargement des classes, `javap`, javadoc. Il sert à tester les règles du **compilateur** |
| `projectkit.ProjectChecker` | le moteur des `Check` (copie de celui de `Kurse`) |

## Lancer

**IntelliJ :** ouvre le dépôt, puis ajoute `base/annotation-kurse/pom.xml` comme projet Maven (clic droit → *Add as Maven Project*). Ensuite, clic droit sur un `Check.java` → *Run*. Argument `solution` pour vérifier le corrigé.

**Terminal (Git Bash), depuis `base/annotation-kurse` :**

```bash
javac --release 17 -encoding UTF-8 -proc:none -d target/classes $(find src/main/java -name '*.java')
java -cp target/classes vorkurs04_annotations.projects.p01_pluginstore.Check solution
```
