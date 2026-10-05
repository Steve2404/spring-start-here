# Drill de rappel 6 — Le contrôle d'accès de la reflection (0.4.12)

> Première fois ? Lis d'abord [`PARCOURS.md`](../../PARCOURS.md) et les règles des drills [`drills/README.md`](../README.md).

**Chrono cible :** 20 min la 1re fois, puis 10 min aux répétitions.

**Règles :**
- De mémoire.
- Crée **`Recall06`**.
- **La classe cible** est écrite par toi, comme une **source** (un `String`), car une classe imbriquée dans `Recall06` serait « nestmate » et verrait tout. Compile-la avec `Javac.compile(Map.of("Safe", source))`, puis charge-la avec `Javac.load`. Voici ce que doit être `public class Safe` :
  - `private int pin = 1;`
  - `private final String owner = new String("lea");`
  - `private final int code = 7;`
  - `private static final int MAX = 3;`
  - `public static String motto = "ok";`
  - un constructeur `private` sans argument, et un constructeur `public Safe(int pin)` ;
  - `private String open()`, qui rend `"ouvert"` ;
  - `public int code()`, qui rend `code`.
- **Le receveur** : un `Safe` créé par le constructeur public avec `42`.
- **Une petite méthode « tenter »** qui rend le résultat d'une opération, ou le nom simple de l'exception levée.
- Une ligne par défi, préfixée `Dxx : `.

## Défis

- ☐ **D01.** Le champ `pin` (trouvé par `getDeclaredField`) : son nom, `canAccess` sur le receveur, puis la tentative de `get` sans forcer.
  → `D01 : pin false IllegalAccessException`
- ☐ **D02.** `trySetAccessible()` sur `pin`, puis `get`, puis `canAccess` à nouveau.
  → `D02 : true 42 true`
- ☐ **D03.** Le champ public statique `motto` : `canAccess` avec quel receveur ? Puis sa valeur.
  → `D03 : true ok`
- ☐ **D04.** Forcez `owner` (un `final` d'instance), écris `"tom"`, puis relis-le. Ensuite, forcez `MAX` (`static final`) et tentez d'y écrire `9`.
  → `D04 : tom IllegalAccessException`
- ☐ **D05.** Forcez `code`, écris `99`. Affiche la valeur lue par reflection, puis celle rendue par la méthode `code()`.
  → `D05 : 99 7`
- ☐ **D06.** Le constructeur privé : `newInstance` sans forcer, puis `trySetAccessible()`, puis le nom simple de la classe de l'objet créé. Ensuite, la méthode privée `open` : `trySetAccessible()`, puis son résultat.
  → `D06 : IllegalAccessException true Safe true ouvert`
- ☐ **D07.** Un record **local** `Point(int x)`. Forcez son champ `x` et tentez d'y écrire `2`.
  → `D07 : IllegalAccessException`
- ☐ **D08.** Le champ `value` de `String` : `trySetAccessible()`, puis le nom simple de ce que lève `setAccessible(true)`.
  → `D08 : false InaccessibleObjectException`
- ☐ **D09.** Le module de `String` : son nom ; `java.lang` est-il exporté ? Ouvert (à tous) ? Puis : le module de `Safe` est-il nommé ?
  → `D09 : java.base true false false`
- ☐ **D10.** Les modificateurs de `MAX` (au format de `Modifier.toString`), puis : le constructeur sans argument est-il privé ?
  → `D10 : private static final | true`

## Sortie attendue complète

```
D01 : pin false IllegalAccessException
D02 : true 42 true
D03 : true ok
D04 : tom IllegalAccessException
D05 : 99 7
D06 : IllegalAccessException true Safe true ouvert
D07 : IllegalAccessException
D08 : false InaccessibleObjectException
D09 : java.base true false false
D10 : private static final | true
```

## Carte mémoire (à lire **après** le drill)

<details><summary>Ouvrir la carte</summary>

| Point | À retenir |
|---|---|
| découverte contre accès | `getDeclared…` trouve même le privé ; l'utiliser sans droit → `IllegalAccessException` |
| `canAccess(receveur)` | `null` pour un membre `static` ou un constructeur |
| `trySetAccessible()` | rend `true`/`false`, **sans exception** |
| `setAccessible(true)` | lève `InaccessibleObjectException` si le module n'**ouvre** pas le paquet |
| exports / opens | exporté = API publique utilisable ; ouvert = reflection profonde (privé) ; `--add-opens` au lancement |
| `final` d'instance | modifiable une fois forcé (sauf record et classe cachée) |
| `static final`, champ de record | jamais modifiables → `IllegalAccessException` |
| constante de compilation | `final int code = 7` est **inlinée** : le code compilé garde 7 |
| constructeur | `getDeclaredConstructor(…).newInstance(…)` ; une classe abstraite → `InstantiationException` |

</details>
