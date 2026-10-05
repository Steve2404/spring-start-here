# Drill de rappel 9 — Sécurité, validation et XPath par l'API (0.2.21 → 0.2.22)

> Première fois ? Lis d'abord [`PARCOURS.md`](../../PARCOURS.md) et les règles des drills [`drills/README.md`](../README.md).

**Chrono cible :** 25 min la 1re fois, puis 12 min aux répétitions.

**Règles :**
- De mémoire. `XmlKit` est interdit.
- Crée **`Recall09`**. Le fichier : `vorkurs02_xml.drills.Data.campus()`. Les petits documents sont lus depuis des chaînes (`StringReader` dans une `InputSource` ou une `StreamSource`).
- Un **code** d'erreur de schéma = le début du message, jusqu'au premier `:`. On n'affiche jamais le reste du message.
- Une ligne par défi, préfixée `Dxx : `.

## Défis

- ☐ **D01.** Un DOM **sûr** (namespace-aware, `FEATURE_SECURE_PROCESSING`, `disallow-doctype-decl`, accès externes DTD et schéma à `""`, `ErrorHandler` silencieux) lit `<!DOCTYPE r [<!ENTITY e "x">]><r>&e;</r>` : `accepte`, ou `refuse ligne <l>`.
  → `D01 : refuse ligne 1`
- ☐ **D02.** Le même DOM lit le campus : le nom local de la racine.
  → `D02 : campus`
- ☐ **D03.** Un DOM qui **permet** le DOCTYPE, sans entités externes, avec l'attribut `jdk.xml.entityExpansionLimit` à `10` puis à `100`, lit `<!DOCTYPE r [<!ENTITY a "ha"><!ENTITY b "&a;&a;&a;&a;&a;"><!ENTITY c "&b;&b;&b;&b;&b;">]><r>&c;</r>`. Pour chaque limite : ` <limite>=<longueur du texte de r>` ou ` <limite>=refuse`.
  → `D03 : 10=refuse 100=50`
- ☐ **D04.** Une `SchemaFactory` (accès externes à `""`) compile, depuis une chaîne, un schéma : `r` contient de 1 à 3 `n` entiers (`xs:int`). Un `Validator` **sans** `ErrorHandler` valide `<r><n>x</n><n>2</n><n>y</n></r>` : le nom simple de l'exception, puis son code.
  → `D04 : SAXParseException cvc-datatype-valid.1.2.1`
- ☐ **D05.** Un nouveau `Validator` du **même** `Schema`, avec un `ErrorHandler` qui **collecte** les codes sans s'arrêter.
  → `D05 : [cvc-datatype-valid.1.2.1, cvc-type.3.1.3, cvc-datatype-valid.1.2.1, cvc-type.3.1.3]`
- ☐ **D06.** Un `XPath` avec ton `NamespaceContext` (`c` → `urn:campus`, `g` → `urn:campus:grades`), sur le DOM du campus : le nombre de cours (`NUMBER`), le titre du cours `C2` (`evaluate` sans type), puis l'existence d'un cours fermé (`BOOLEAN`).
  → `D06 : 3.0 Bases de donnees true`
- ☐ **D07.** Les personnes qui ont un email (`NODESET`) : leurs `id`.
  → `D07 : [S1, S3, S4]`
- ☐ **D08.** **Compile** une fois l'expression « crédits du cours × nombre de ses étudiants », puis évalue-la **relativement** à chaque cours (ordre du document).
  → `D08 : [18, 6, 0]`
- ☐ **D09.** En `BOOLEAN` : `'b' > 'a'`, puis `'b' != 'a'`.
  → `D09 : false true`

## Sortie attendue complète

```
D01 : refuse ligne 1
D02 : campus
D03 : 10=refuse 100=50
D04 : SAXParseException cvc-datatype-valid.1.2.1
D05 : [cvc-datatype-valid.1.2.1, cvc-type.3.1.3, cvc-datatype-valid.1.2.1, cvc-type.3.1.3]
D06 : 3.0 Bases de donnees true
D07 : [S1, S3, S4]
D08 : [18, 6, 0]
D09 : false true
```

## Carte mémoire (à lire **après** le drill)

<details><summary>Ouvrir la carte</summary>

| Point | À retenir |
|---|---|
| DOM / SAX sûrs | `FEATURE_SECURE_PROCESSING`, `disallow-doctype-decl`, entités externes à `false`, `ACCESS_EXTERNAL_DTD` / `_SCHEMA` à `""` |
| StAX sûr | `SUPPORT_DTD` et `IS_SUPPORTING_EXTERNAL_ENTITIES` à `false` |
| limites | `jdk.xml.entityExpansionLimit` (et les autres `jdk.xml.*`) contre l'explosion d'entités |
| schéma | `SchemaFactory.newSchema(…)` une fois ; un `Validator` par document (pas thread-safe) |
| erreurs | sans `ErrorHandler` : exception à la 1re erreur ; avec : collecter ou relancer |
| XPath API | `XPathFactory.newInstance().newXPath()`, `setNamespaceContext`, `evaluate(expr, nœud, XPathConstants.X)` |
| types | `NODESET` → `NodeList`, `NUMBER` → `Double`, `BOOLEAN` → `Boolean`, sans type → `String` |
| compiler | `compile(expr)` puis `evaluate(nœud)` : relatif au nœud contexte |
| piège | en XPath 1.0, `<` `>` comparent des **nombres** ; `=` `!=` comparent des chaînes |

</details>
