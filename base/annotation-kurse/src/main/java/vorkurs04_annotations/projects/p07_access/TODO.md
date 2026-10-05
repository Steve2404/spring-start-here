# Projet 7 — L'auditeur d'accès d'un framework (cours 0.4.12)

> Première fois ? Lis d'abord le mode d'emploi [`vorkurs04_annotations/PARCOURS.md`](../../PARCOURS.md).

**Notions visées (0.4.12) :**
- **trouver n'est pas accéder** : le contrôle d'accès Java s'applique aussi à la reflection ;
- `AccessibleObject`, `canAccess`, le drapeau d'accessibilité ;
- `trySetAccessible()` contre `setAccessible(true)` ;
- les modules : `exports` contre `opens`, module nommé ou anonyme ;
- `Field.get` et `Field.set`, et les limites de `final` ;
- les méthodes et constructeurs privés, `newInstance` ;
- classer les erreurs.

**Ce qui est donné :** `Data.java` (4 classes cibles **sous forme de source**, plus 18 opérations), `Check.java`, `projectkit.Javac`.  
**Ce que TU crées :** tout le reste, dans `vorkurs04_annotations.projects.p07_access`. Classe `main` : **`AccessAuditor`**.

**Pour vérifier :** lance `Check.java` depuis `base/annotation-kurse`.

---

## Le problème

Un framework (injection, sérialisation, ORM) lit et écrit des champs privés, appelle des méthodes et crée des objets. Avant de s'en servir sur une base de code, l'équipe veut un **audit** : pour chaque opération, qu'est-ce qui marche **directement**, qu'est-ce qui marche **en forçant**, et qu'est-ce qui reste **impossible**, et pourquoi.

- **Les classes :** compile `Data.TARGETS` en une fois et charge chaque classe. `String` vise `java.lang.String`.
- **Les receveurs :** un objet par classe, créé par son constructeur **public** :
  - `Vault` avec `"abc"` ;
  - `Point` avec `(1, 2)` ;
  - pour `String`, la chaîne `"hello"`.
  - Un membre `static` se lit, s'écrit et s'appelle avec le receveur `null`.

---

## Tableau de bord

### ☐ Étape 1 — Découvrir, puis juger l'accès (0.4.12 S1–S2)

Pour chaque opération de `Data.PROBES` :
- **Découverte :**
  - un champ avec `getDeclaredField` ;
  - une méthode, parmi `getDeclaredMethods()`, par son nom ;
  - le constructeur **sans argument** avec `getDeclaredConstructor()`.
- **Introuvable :** la ligne finit par `introuvable (<nom simple de l'exception>)` : on n'arrive même pas à la question de l'accès.
- **Sinon :** commence la ligne par `accessible` ou `inaccessible`, selon `canAccess(receveur)` (`null` pour un membre `static` ou un constructeur), puis ` ; `.
- **Question :** la découverte réussit sur le champ **privé** `secret`. Qu'est-ce que cela prouve ?

### ☐ Étape 2 — Tenter sans forcer, puis forcer (0.4.12 S2–S3, S5–S6)

**Écris une méthode « tenter » réutilisable.** Elle reçoit le membre, et l'opération sous forme d'une interface fonctionnelle à toi qui peut lever une `ReflectiveOperationException`. Elle raconte :

| Ce qui arrive | Texte |
|---|---|
| l'opération réussit directement | `direct -> <résultat>` |
| `InvocationTargetException` | `direct -> la cible a leve <cause> (<message de la cause>)` |
| une autre `ReflectiveOperationException`, sauf l'accès refusé | `direct -> <nom simple>` |
| `IllegalAccessException` | `sans forcer IllegalAccessException ; `, puis on demande l'accès avec `trySetAccessible()` |
| … et l'accès est refusé | `trySetAccessible false (module ferme)` |
| … et l'accès est accordé, on retente | `force -> <résultat>`, ou `force -> <nom simple de l'exception>` |

**Les opérations :**

| Opération | Résultat affiché |
|---|---|
| `READ` | `String.valueOf` de la valeur lue |
| `WRITE` | `ecrit` (la valeur est convertie en `int` si le champ est un `int`) |
| `CALL` | `String.valueOf` du retour ; les arguments éventuels sont des `String` |
| `CREATE` | `objet <nom simple de la classe>` |

```
READ Vault.counter : accessible ; direct -> 7
READ Vault.secret : inaccessible ; sans forcer IllegalAccessException ; force -> abc
WRITE Vault.VERSION 2.0 : inaccessible ; sans forcer IllegalAccessException ; force -> IllegalAccessException
READ String.value : inaccessible ; sans forcer IllegalAccessException ; trySetAccessible false (module ferme)
```
**À expliquer après le lancement :**
- `WRITE Vault.owner tom` réussit en forçant (un `final` **d'instance**), puis `CALL Vault.owner` rend `tom`. Mais `WRITE Vault.VERSION` (`static final`) et `WRITE Point.x` (composant d'un **record**) échouent **même forcés**. Pourquoi ces deux cas sont-ils protégés ?
- `WRITE Vault.code 99` réussit et `READ Vault.code` donne `99`… mais `CALL Vault.code` rend **`1234`**. Regarde la déclaration de `code` : qu'est-ce qu'une **constante de compilation**, et qu'a fait javac de `return code;` ?
- `CREATE Shape` : quelle exception, et pourquoi n'a-t-elle rien à voir avec l'accès ?
- `CREATE Thrower` : la reflection a réussi, c'est le constructeur qui a échoué. Comment le sais-tu ?

### ☐ Étape 3 — `setAccessible(true)` sur `java.base` (0.4.12 S3)

```
FORCE String.value : InaccessibleObjectException
```
- Appelle `setAccessible(true)` sur le champ `value` de `String` et affiche ce qui se passe.
- **Question :** `trySetAccessible()` répondait poliment `false`, alors que `setAccessible(true)` lance une exception. Laquelle choisir dans un framework, et pourquoi ?

### ☐ Étape 4 — Ce que disent les modules (0.4.12 S4)

```
MODULE Vault : nomme false ; MODULE String : java.base ; java.lang exporte true ; ouvert a Vault false
```
- **`Vault`** : son module est-il nommé ?
- **`String`** : le nom de son module.
- **Le paquet `java.lang`** : est-il **exporté**, et est-il **ouvert** au module de `Vault` ?
- **Questions :**
  - **Exporté** permet quoi ? **Ouvert** permet quoi de plus ?
  - Quelle option de la ligne de commande `java` ouvrirait `java.lang` (`--add-opens …`) ? Pourquoi est-ce une décision de **déploiement**, et pas du code ?

### ☐ Étape 5 — Les modificateurs, pour diagnostiquer (0.4.12 S7)

```
MODIFICATEURS VERSION : private static final ; Point.x : private final
```
- Affiche les modificateurs du champ `VERSION` de `Vault`, puis ceux du champ `x` de `Point`, au format de `Modifier.toString`.

### ☐ Étape 6 — Le `main` d'`AccessAuditor`

Il affiche, dans l'ordre :
1. une ligne par opération, au format `<opération> : <diagnostic>` ;
2. la ligne `FORCE` ;
3. la ligne `MODULE` ;
4. la ligne `MODIFICATEURS`.

---

## Checklist API (vérifiée par `Check`)

| Élément | Étape | ☐ |
|---|---|---|
| `getDeclaredField`, `getDeclaredMethods`, `getDeclaredConstructor`, `getConstructor` | 1, intro | ☐ |
| `canAccess`, `AccessibleObject` | 1, 2 | ☐ |
| `get`, `set`, `invoke`, `newInstance` | 2 | ☐ |
| `trySetAccessible()` | 2 | ☐ |
| `IllegalAccessException`, `ReflectiveOperationException`, `InvocationTargetException` + `getCause()` | 2 | ☐ |
| `NoSuchFieldException`, `NoSuchMethodException` | 1 | ☐ |
| `setAccessible(true)`, `InaccessibleObjectException` | 3 | ☐ |
| `getModule()`, `isNamed()`, `isExported`, `isOpen` | 4 | ☐ |
| `Modifier.isStatic`, `Modifier.toString` | 1, 5 | ☐ |

---

## Sortie attendue complète

C'est le contrat exact que vérifie `Check` :

```
a remplir
```
