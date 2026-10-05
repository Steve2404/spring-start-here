# Projet 8 — Le générateur de builders (cours 0.4.13)

> Première fois ? Lis d'abord le mode d'emploi [`vorkurs04_annotations/PARCOURS.md`](../../PARCOURS.md).

**Notions visées (0.4.13) :**
- l'annotation processing se passe à la **compilation**, ce n'est pas de la reflection à l'exécution ;
- `Processor`, `AbstractProcessor`, `init` et `process` ;
- `Element` et `TypeMirror` ;
- les rounds et `RoundEnvironment` ;
- la validation avec le `Messager` ;
- la génération avec le `Filer` ;
- les options `-A`.

**Ce qui est donné :** `Data.java` (3 lots de sources), `Check.java`, `projectkit.Javac`.
- `compile(sources, options, processor)` lance javac **avec ton processor**, comme `-processor`.
- Le résultat contient `generated()` : les sources générées.

**Ce que TU crées :** tout le reste, dans `vorkurs04_annotations.projects.p08_codegen`. Noms imposés :
- l'annotation **`Builder`** (les sources l'importent) ;
- la classe `main` **`CodeGen`**.

Le processor, tu le nommes comme tu veux.

**Pour vérifier :** lance `Check.java` depuis `base/annotation-kurse`.

---

## Le problème

Écrire un builder à la main pour chaque record est répétitif. Ton processor le génère **pendant la compilation** : pour `@Builder record Person(String name, int age, List<String> tags)`, il écrit une classe `PersonBuilder` avec un setter chaînable par composant et une méthode `build()`. C'est le principe de Lombok, MapStruct, Dagger… et du traitement à la compilation dont Spring se sert de plus en plus.

`Data.BATCHES` contient 3 lots, chacun avec son nom, ses options javac et ses sources. **Compile chaque lot en une fois, avec un processor NEUF.**

---

## Tableau de bord

### ☐ Étape 1 — L'annotation `@Builder` (0.4.13 S1)

- Sur les types seulement.
- **Rétention : la plus courte possible.** Un processor lit les sources pendant la compilation ; personne n'a besoin de `@Builder` dans le `.class`.
- **Question :** pourquoi SOURCE suffit-il ici, alors qu'aux projets 4 à 7 il fallait `RUNTIME` ?

### ☐ Étape 2 — Le processor : déclaration et cycle de vie (0.4.13 S2, S7)

- Hérite d'`AbstractProcessor`.
- Annonce **l'annotation traitée** : son nom **qualifié**, calculé depuis `Builder.class`, car le paquet n'est pas le même pour toi et pour la solution.
- Annonce la **version de source** : la plus récente supportée. Sans ça, javac émet un avertissement.
- Annonce **l'option** `builder.suffix`, et lis-la dans `init` : par défaut `Builder`.
- Compte les **rounds** : un `process` = un round. Ton `main` affichera ce nombre.
- **`process` rend `false`** : tu ne « consommes » pas l'annotation.
  - **Question :** que voudrait dire `true` ?

### ☐ Étape 3 — Valider avec le `Messager` (0.4.13 S3, S5)

Pour chaque élément annoté `@Builder` dans ce round :
- **Pas un record** → **ERREUR**, attachée à l'élément : `@Builder exige un record, pas <genre en minuscules>`, plus ` abstract` s'il est abstrait.
  - Exemple : `@Builder exige un record, pas class abstract`.
- **Un record sans composant** → **AVERTISSEMENT** `record sans composant : aucun builder`, et rien n'est généré.
- **Un composant qui s'appelle `build`** → **ERREUR** `le composant build cache la methode build()`.
  - **Attache-la au record**, pas au composant : sur ce JDK, un message attaché à un composant de record n'a **pas** de position (ligne 0). C'est vérifié.
- Une erreur du `Messager` fait **échouer la compilation**, exactement comme une erreur de javac.

### ☐ Étape 4 — Générer avec le `Filer` (0.4.13 S3, S4, S6)

**Le fichier :**
- `createSourceFile(<NomDuRecord><suffixe>, record)` ;
- même paquet que le record : ici le paquet par défaut, donc juste le nom.

**Ce qu'il contient :**
- une classe publique avec un **champ privé par composant**, du type du composant. Le `TypeMirror` du composant, écrit en texte, donne le type complet : `java.util.List<java.lang.String>` ;
- un **setter chaînable** par composant : `public PersonBuilder name(java.lang.String value) { … return this; }` ;
- `public Person build()`, qui appelle le constructeur canonique avec les champs, dans l'ordre des composants.

Ensuite, une **NOTE** sur le record : `builder genere : <nom de la classe générée>`.

Un `FilerException` (même fichier créé deux fois) devient une erreur.

**Questions :**
- `UsePerson`, dans le lot `ok`, utilise `PersonBuilder`, qui **n'existe pas** au début de la compilation. Pourquoi javac ne proteste-t-il pas ? Combien de rounds cela fait-il, et pourquoi un de plus que tu ne le penses ?
- Pourquoi le lot `bad` ne fait-il que 2 rounds ?

### ☐ Étape 5 — Le banc d'essai : `main` de `CodeGen`

**Pour chaque lot :**
1. Compile avec un processor neuf :
```
LOT ok : succes true ; rounds 3 ; genere [PersonBuilder.java, PointBuilder.java]
```
2. Les messages **de ton processor** : les diagnostics dont le code finit par `proc.messager`, au format `  <kind> <source> l.<ligne> : <ton message>` (deux espaces devant) :
```
  NOTE Person l.5 : builder genere : PersonBuilder
```

**Pour le lot `ok`, utilise le code généré par réflexion** (deux espaces devant chaque ligne) :
- charge `PersonBuilder` et affiche ses méthodes déclarées, triées :
```
  PersonBuilder : [age, build, name, tags]
```
- crée un builder avec son constructeur sans argument ;
- appelle `name("Lea")`, `age(30)`, puis `tags(List.of("java", "xml"))`. Retrouve chaque méthode par son nom **et** son type de paramètre ;
- affiche le résultat de `build()` :
```
  build() -> Person[name=Lea, age=30, tags=[java, xml]]
```
- appelle la méthode statique `demo()` de `UsePerson` et affiche son résultat :
```
  UsePerson.demo() -> Person[name=Ines, age=41, tags=[ops]]
```

**Pour le lot `maker`** (option `-Abuilder.suffix=Maker`), vérifie le texte généré :
```
  PointMaker declare build : true
```
(le texte contient-il `public Point build()` ?)

---

## Checklist API (vérifiée par `Check`)

| Élément | Étape | ☐ |
|---|---|---|
| `RetentionPolicy.SOURCE` | 1 | ☐ |
| `extends AbstractProcessor`, `init(ProcessingEnvironment)`, `process(…, RoundEnvironment)` | 2 | ☐ |
| `getSupportedAnnotationTypes`, `getSupportedSourceVersion`, `SourceVersion.latestSupported()` | 2 | ☐ |
| `@SupportedOptions`, `getOptions()` | 2 | ☐ |
| `getElementsAnnotatedWith`, `getKind()`, `ElementKind.RECORD`, `getModifiers()`, `Modifier.ABSTRACT` | 3 | ☐ |
| `getRecordComponents()`, `getSimpleName()`, `asType()` | 3, 4 | ☐ |
| `getMessager()`, `printMessage`, `Diagnostic.Kind.ERROR` / `WARNING` / `NOTE` | 3, 4 | ☐ |
| `getFiler()`, `createSourceFile`, `openWriter()`, `FilerException` | 4 | ☐ |
| `Javac.compile(…, processor)`, `success()`, `generated()`, `Javac.load`, `newInstance`, `invoke` | 5 | ☐ |

---

## Sortie attendue complète

C'est le contrat exact que vérifie `Check` :

```
a remplir
```
