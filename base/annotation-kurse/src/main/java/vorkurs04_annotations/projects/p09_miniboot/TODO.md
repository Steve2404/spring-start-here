# Projet 9 — CAPSTONE : MiniBoot, un mini-framework sans Spring (cours 0.4.14 → 0.4.15)

> Première fois ? Lis d'abord le mode d'emploi [`vorkurs04_annotations/PARCOURS.md`](../../PARCOURS.md).

**Notions visées :**
- **0.4.14** :
  - des métadonnées qui décrivent une **intention** ;
  - le contrat d'annotations (`@MiniComponent`, `@Inject`, `@OnStart`) ;
  - découverte, validation, instanciation, registre, injection dans les champs, cycle de vie ;
  - la stratégie d'erreur.
- **0.4.15** :
  - le labo MiniConfig : `@MiniConfig`, `@ConfigValue`, `@AfterLoad` ;
  - clé complète, valeur obligatoire ou par défaut, conversion stricte ;
  - refuser tôt les champs `static` / `final` ;
  - toutes les erreurs de configuration réunies ;
  - les exceptions de `@AfterLoad` déballées.
- **Tout le reste du 0.4.**

**Ce qui est donné :** `Data.java`, `Check.java`, `projectkit.Javac`.
- `Data.SOURCES` : 13 classes candidates.
- `Data.APPS` : 7 applications, chacune avec les classes qu'elle scanne et sa configuration.

**Ce que TU crées :** tout le reste, dans `vorkurs04_annotations.projects.p09_miniboot`. Noms imposés :
- les 6 annotations ci-dessous ;
- la classe `main` **`MiniBoot`**.

**Pour vérifier :** lance `Check.java` depuis `base/annotation-kurse`.

---

## Le problème

Tu écris le cœur d'un conteneur à la Spring, **en moins de 300 lignes** : il trouve les composants, calcule dans quel ordre les créer, les crée, charge leur configuration, les branche entre eux, puis les démarre. Chaque application de `Data.APPS` démarre **de zéro**. Six des sept échouent volontairement, chacune à une phase différente.

Compile `Data.SOURCES` **une seule fois** (avec `{{PKG}}` remplacé), puis lance chaque application dans l'ordre.

---

## Tableau de bord

### ☐ Étape 1 — Le contrat d'annotations (0.4.14 S2, 0.4.15 S2)

Toutes lisibles à l'exécution.

| Annotation | Cible | Éléments |
|---|---|---|
| `MiniComponent` | type | — |
| `Inject` | champ | — |
| `OnStart` | méthode | — |
| `MiniConfig` | type | `prefix` (texte, obligatoire) |
| `ConfigValue` | champ | `key` (texte, obligatoire), `required` (défaut `true`), `defaultValue` (défaut `""`) |
| `AfterLoad` | méthode | — |

### ☐ Étape 2 — Découverte et structure (0.4.14 S3)

```
APP boutique
  ignore Mailer
  composants : [Audit, Clock, OrderService, Repository, ShopSettings, SmtpMailer]
```
- **Un composant** est une classe scannée qui porte `@MiniComponent` **ou** `@MiniConfig`. Une autre classe affiche `ignore <nom>`.
- **La liste des composants** est triée par nom.
- **La structure**, validée pour **tous** les composants avant d'aller plus loin. On réunit toutes les erreurs :
  - classe abstraite → `structure <Classe> : classe abstraite` ;
  - méthode `@OnStart` ou `@AfterLoad` qui rend une valeur, prend un paramètre ou est `static` → `structure <Classe>.<méthode> : @<Annotation> doit etre void, sans parametre, non static`.
- **Dès qu'il y a des erreurs :** chacune donne `  ECHEC <erreur>`, puis `  ARRET`, et on passe à l'application suivante.
  - **Conception :** une exception **à toi**, attrapée dans le `main` autour de chaque application, rend cela simple.

### ☐ Étape 3 — Le câblage (0.4.14 S5)

- Pour chaque champ `@Inject` (composants pris dans l'ordre trié, champs triés par nom), trouve les composants **assignables** au type du champ.
  - Un champ de type `Mailer` (une interface) accepte `SmtpMailer`.
- **Aucun** candidat → `injection <Classe>.<champ> : aucun composant de type <Type>`.
- **Plusieurs** candidats → `… : plusieurs composants de type <Type>`.
- Les erreurs sont réunies, puis on arrête.

### ☐ Étape 4 — L'ordre de création : tri topologique (0.4.14 S4)

Un composant ne peut être créé qu'**après** les composants qu'il injecte.
- **Algorithme de Kahn :**
  1. Les composants sans dépendance restante sont « prêts ».
  2. Retire **toujours le premier prêt par ordre alphabétique**, ajoute-le à l'ordre, puis rends prêts ceux dont c'était la dernière dépendance.
- **Reste-t-il des composants à la fin ?** Ils sont dans un **cycle** : `cycle : Ping, Pong` (triés), et on arrête.
```
  ordre : Clock -> Repository -> ShopSettings -> SmtpMailer -> OrderService -> Audit
```
- **Question :** pourquoi ne pas « inventer » un ordre, l'ordre de scan par exemple ? (0.4.9 S6)

### ☐ Étape 5 — Création et configuration (0.4.14 S4, 0.4.15 S3–S6)

Dans l'ordre calculé, crée chaque composant avec son constructeur **sans argument**.
- Le constructeur lève une exception → `creation <Classe> : <cause> <message>`, puis arrêt.
  - Il faut **déballer** l'`InvocationTargetException`.

**Pour une classe `@MiniConfig`**, juste après sa création :
- **Les champs `@ConfigValue`**, triés par nom. La clé complète est `<prefix>.<key>`.
- **Les erreurs**, toutes réunies avant d'arrêter :

| Cas | Erreur |
|---|---|
| champ `static` | `config <Classe>.<champ> : champ static interdit` |
| champ `final` | `… : champ final interdit` |
| clé absente et `required` | `… : cle <cle complète> manquante` |
| clé absente et pas `required` | on prend `defaultValue` |
| conversion impossible | `… : '<texte>' n'est pas un <type>` |

- **La conversion est stricte :**
  - `int` et `long` passent par `parse…` ;
  - un `boolean` doit valoir exactement `true` ou `false` ;
  - un `String` est pris tel quel.
- **Sans erreur**, écris les valeurs dans les champs (ils sont privés), puis affiche-les. Une valeur venue du défaut est suivie de ` (defaut)` :
```
  config ShopSettings : debug=false (defaut) owner=admin (defaut) port=8080 timeout=30
```
- **Ensuite, les méthodes `@AfterLoad`** (triées par nom). Elles sont privées : demande l'accès poliment.
  - Succès → `  afterLoad <Classe>.<méthode> : ok`.
  - Exception → `afterLoad <Classe>.<méthode> : <cause> <message>`, puis arrêt.

### ☐ Étape 6 — Injection, démarrage (0.4.14 S5–S6)

**Injection :**
- Remplis chaque champ `@Inject` avec **l'instance** de son composant.
- Puis une ligne avec toutes les injections au format `Classe.champ<-Composant`, triées, séparées par un espace.

**Démarrage :**
- Dans l'ordre calculé, appelle les méthodes `@OnStart` (triées par nom) : `  onStart <Classe>.<méthode> : ok`.
  - Les composants affichent eux-mêmes une ligne pendant leur démarrage, **avant** la tienne.
- Enfin :
```
  DEMARRE : 6 composant(s)
```

**Question :** dans `boutique`, `OrderService.warmUp` affiche le port lu dans `ShopSettings`. Pourquoi est-il garanti que la configuration est chargée à ce moment-là ?

### ☐ Étape 7 — Le `main` de `MiniBoot`

Les 7 applications, dans l'ordre de `Data.APPS`.

---

## Checklist API (vérifiée par `Check`)

| Élément | Étape | ☐ |
|---|---|---|
| `@Retention`, `@Target`, éléments avec `default` | 1 | ☐ |
| `isAnnotationPresent`, `getAnnotation` | 2, 5 | ☐ |
| `Modifier.isAbstract`, `isStatic`, `isFinal` ; `getParameterCount()`, `getReturnType()`, `void.class` | 2, 5 | ☐ |
| `getDeclaredFields()`, `isAssignableFrom`, `getDeclaringClass()` | 3, 6 | ☐ |
| `getDeclaredConstructor()`, `newInstance()`, `canAccess`, `trySetAccessible()` | 5 | ☐ |
| `Field.set`, `getDeclaredMethods()`, `invoke` | 5, 6 | ☐ |
| `InvocationTargetException` + `getCause()` | 5, 6 | ☐ |
| ~~`setAccessible`~~ | **interdit** | — |

---

## Sortie attendue complète

C'est le contrat exact que vérifie `Check` :

```
a remplir
```
