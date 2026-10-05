# Drill de rappel 4 — `Method.invoke` dans tous ses états (0.4.9 → 0.4.10)

> Première fois ? Lis d'abord [`PARCOURS.md`](../../PARCOURS.md) et les règles des drills [`drills/README.md`](../README.md).

**Chrono cible :** 20 min la 1re fois, puis 10 min aux répétitions.

**Règles :**
- De mémoire.
- Crée la classe **`Recall04`**, avec en types imbriqués `static` :
  - **`Animal`**, avec ces méthodes :
    - `public String speak()` → `"..."` ;
    - `public static int twice(int n)` ;
    - `public long widen(long n)` → `n + 1` ;
    - deux surcharges `public String kind(int n)` → `"int"` et `kind(Integer n)` → `"Integer"` ;
    - `public void touch()` ;
    - `public static int count(String... words)` → nombre de mots ;
    - `public int fail()`, qui lève `IllegalStateException` ;
    - `private String secret()` → `"s"`.
  - **`Dog extends Animal`**, qui redéfinit seulement `speak()` → `"wouf"`.
- Écris une petite méthode qui appelle `invoke` et rend la valeur, ou le nom de l'erreur :
  - `IllegalArgumentException` ;
  - `cause <nom simple>` pour une `InvocationTargetException`.
- Une ligne par défi, préfixée `Dxx : `.

## Défis

- ☐ **D01.** `twice(21)` par reflection.
  → `D01 : 42`
- ☐ **D02.** La `Method` `speak` prise sur `Animal`, invoquée sur un `Dog`.
  → `D02 : wouf`
- ☐ **D03.** `kind` avec le paramètre `int`, puis avec le paramètre `Integer`, chacune invoquée avec l'argument `1`.
  → `D03 : int Integer`
- ☐ **D04.** `widen` (paramètre `long`) invoquée avec l'`Integer` `5`, puis `twice` (paramètre `int`) invoquée avec le `Long` `5L`.
  → `D04 : 6 IllegalArgumentException`
- ☐ **D05.** Le type de retour déclaré de `twice`, puis le nom simple de la classe de la valeur rendue par `invoke`.
  → `D05 : int Integer`
- ☐ **D06.** Ce que rend `invoke` de `touch`.
  → `D06 : null`
- ☐ **D07.** `count` invoquée avec **un** tableau de 3 mots, en évitant qu'`invoke` ne l'éclate. Puis invoquée avec deux arguments `"a", "b"`.
  → `D07 : 3 IllegalArgumentException`
- ☐ **D08.** `fail` invoquée.
  → `D08 : cause IllegalStateException`
- ☐ **D09.** Deux appels :
  - `touch` invoquée avec un argument en trop ;
  - `touch` invoquée avec le receveur `null`, en affichant le nom simple de l'exception levée.
  → `D09 : IllegalArgumentException NullPointerException`
- ☐ **D10.** `getMethod("secret")` sur `Animal`.
  → `D10 : NoSuchMethodException`
- ☐ **D11.** Le nombre de méthodes **déclarées** par `Dog`. Puis, avec `getMethod` sur `Dog`, la méthode `twice` : quelle est sa classe de déclaration (nom simple) ?
  → `D11 : 1 Animal`
- ☐ **D12.** Pour `count` : est-elle `static` ? varargs ? Combien de paramètres ?
  → `D12 : true true 1`
- ☐ **D13.** La méthode privée `secret` (trouvée avec `getDeclaredMethod`) : `canAccess` sur un `Animal`, puis le résultat de son appel, **sans** forcer l'accès.
  → `D13 : true s`

**Expérience** (hors sortie attendue) : pour D13, pourquoi l'appel privé passe-t-il sans `trySetAccessible` ? Indice : les classes imbriquées et leur classe englobante sont des « nestmates » (Java 11). Au projet 5, la même chose échouait : pourquoi ?

## Sortie attendue complète

```
D01 : 42
D02 : wouf
D03 : int Integer
D04 : 6 IllegalArgumentException
D05 : int Integer
D06 : null
D07 : 3 IllegalArgumentException
D08 : cause IllegalStateException
D09 : IllegalArgumentException NullPointerException
D10 : NoSuchMethodException
D11 : 1 Animal
D12 : true true 1
D13 : true s
```

## Carte mémoire (à lire **après** le drill)

<details><summary>Ouvrir la carte</summary>

| Point | À retenir |
|---|---|
| `invoke(receveur, args…)` | `null` pour une méthode `static` ; un receveur `null` pour une méthode d'instance → `NullPointerException` |
| liaison dynamique | la `Method` d'une super-classe, invoquée sur une sous-classe, exécute la **redéfinition** |
| surcharge | se choisit au `getMethod(nom, types…)`, pas au moment d'`invoke` |
| conversions | déballage et **élargissement** acceptés (`Integer` → `long`) ; jamais de rétrécissement → `IllegalArgumentException` |
| retour | primitif emballé ; `void` → `null` |
| varargs | un paramètre tableau : `invoke(null, (Object) new String[]{…})` |
| erreurs | `IllegalArgumentException` (mauvais arguments) ; `InvocationTargetException` (la méthode a levé : `getCause()`) ; `IllegalAccessException` (accès refusé) |
| `getMethod` / `getMethods` | publiques, **héritées comprises** |
| `getDeclaredMethod` / `getDeclaredMethods` | toutes celles de la classe, privées comprises, sans héritage |
| accès | `canAccess(receveur)`, `trySetAccessible()` ; les nestmates se voient en privé |

</details>
