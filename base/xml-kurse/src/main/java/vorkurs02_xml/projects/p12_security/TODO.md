# Projet 12 — La politique d'import sûre (cours 0.2.21)

> Première fois ? Lis d'abord le mode d'emploi [`vorkurs02_xml/PARCOURS.md`](../../PARCOURS.md) : comment lire cette fiche, lancer `Check`, quoi faire en cas de blocage.

**Notions visées :** pourquoi XML peut être un risque ; DTD et entités externes ; configurer DOM et SAX de façon sûre ; configurer StAX ; XSD et propriétés d'accès externe ; limites de traitement contre l'explosion d'entités ; défense en profondeur (0.2.21).  
**Ce qui est donné :** `Data.java` (`Data.file(nom)`), les fichiers de `files/`, et `Check.java`. Ouvre chaque fichier : aucun n'est dangereux pour ta machine, mais certains **utilisent** des mécanismes qu'un import public doit refuser.  
**Ce que TU crées :** tous les fichiers `.java`, dans le paquet `vorkurs02_xml.projects.p12_security`. La classe du `main` s'appelle **`SafeImport`**.

---

## Le problème

Une plateforme reçoit des commandes XML de partenaires inconnus. Un document XML peut demander au parseur bien plus que « lire ce texte » : charger une DTD externe, développer des entités en cascade, inclure d'autres schémas. Ta mission : écrire la **politique d'import** et configurer chaque API pour qu'elle ne fasse **que** ce dont l'import a besoin.

Les fichiers, dans l'ordre utilisé partout : `clean.xml`, `legacy-internal.xml`, `external-dtd.xml`, `param-entity.xml`, `lol-small.xml`, `lol-big.xml`.

**Règle d'affichage :** les messages d'exception dépendent de la langue de la machine. On n'affiche **jamais** `getMessage()`, seulement un verdict, une ligne ou un nom de classe.

---

## Tableau de bord

### ☐ Étape 1 — Le tri sur le texte brut (0.2.21 S1, S2)

Une `enum Risk { DOCTYPE, EXTERNAL_DTD, INTERNAL_ENTITY, EXTERNAL_ENTITY, PARAMETER_ENTITY }` et une méthode qui, sur le **texte** d'un fichier, rend l'ensemble (`EnumSet`) des risques présents :
- `DOCTYPE` : le texte contient `<!DOCTYPE` ;
- `EXTERNAL_DTD` : `<!DOCTYPE nom SYSTEM` ou `PUBLIC` ;
- `INTERNAL_ENTITY` : `<!ENTITY nom "…"` ou `'…'` (pas une entité paramètre) ;
- `EXTERNAL_ENTITY` : `<!ENTITY` (paramètre ou non) `nom SYSTEM` ou `PUBLIC` ;
- `PARAMETER_ENTITY` : `<!ENTITY %`.
```
RISQUES param-entity.xml : [DOCTYPE, EXTERNAL_ENTITY, PARAMETER_ENTITY]
```
- **Question :** ce tri par expressions régulières suffit-il à sécuriser l'import ? À quoi sert-il alors ?

### ☐ Étape 2 — Trois lecteurs stricts (0.2.21 S3, S4)

- **DOM strict :** sur la fabrique, `FEATURE_SECURE_PROCESSING` à `true`, la fonctionnalité `http://apache.org/xml/features/disallow-doctype-decl` à `true`, les entités externes générales et paramètres à `false` (`http://xml.org/sax/features/external-general-entities` et `…/external-parameter-entities`), les attributs `ACCESS_EXTERNAL_DTD` et `ACCESS_EXTERNAL_SCHEMA` à `""`, `setXIncludeAware(false)`, `setExpandEntityReferences(false)`. Un `ErrorHandler` silencieux sur le builder.
- **SAX strict :** sur la fabrique, `FEATURE_SECURE_PROCESSING` et `disallow-doctype-decl` ; sur le **parseur**, la propriété `ACCESS_EXTERNAL_DTD` à `""`.
- **StAX strict :** `SUPPORT_DTD` à `false`, `IS_SUPPORTING_EXTERNAL_ENTITIES` à `false`, `ACCESS_EXTERNAL_DTD` à `""` ; lis tout le flux.

Pour chaque fichier :
```
STRICT clean.xml : DOM accepte (2 item) | SAX accepte | STAX accepte
STRICT legacy-internal.xml : DOM refuse ligne 2 | SAX refuse ligne 2 | STAX refuse
```
- DOM : `accepte (n item)` (le nombre d'éléments `item`), ou `refuse ligne l` (`SAXParseException`) ;
- SAX : `accepte` ou `refuse ligne l` ;
- StAX : `accepte` ou `refuse` (`XMLStreamException`).
- **Question :** StAX accepte `external-dtd.xml` et `param-entity.xml`. Pourquoi « ignorer la DTD » n'est-il pas la même politique que « refuser le DOCTYPE » ? Laquelle est la plus claire pour un partenaire ?

### ☐ Étape 3 — Le chemin « ancien format », borné (0.2.21 S6)

Certains anciens partenaires de confiance utilisent des entités **internes**. Un DOM « ancien format » : DOCTYPE **permis**, mais `FEATURE_SECURE_PROCESSING`, entités externes à `false`, `http://apache.org/xml/features/nonvalidating/load-external-dtd` à `false`, accès externes à `""`, et l'attribut `jdk.xml.entityExpansionLimit` à `"1000"`.

Pour `legacy-internal.xml`, `lol-small.xml`, `lol-big.xml` :
```
ANCIEN legacy-internal.xml : accepte, note de 20 caracteres : Client : Acme & Fils
ANCIEN lol-small.xml : refuse (limite d'expansions)
```
- le texte de la `note` : sa longueur, puis le texte (s'il dépasse 40 caractères, ses 40 premiers suivis de `...`).
- **Question :** combien d'expansions demande `lol-small.xml` ? Et `lol-big.xml` ? Quelle taille aurait sa note ?

### ☐ Étape 4 — La politique (0.2.21 S7)

Pour chaque fichier :
- aucun risque → le DOM **strict** ;
- seulement `DOCTYPE` et/ou `INTERNAL_ENTITY` → le DOM **ancien format** ;
- sinon → `REFUSE <risques>` **sans même parser**.

Après parsing : `ACCEPTE <n> item, note=<texte de la note>`, ou, si le parsing échoue, `REFUSE au parsing <risques>`.
```
IMPORT external-dtd.xml : REFUSE [DOCTYPE, EXTERNAL_DTD]
```
- **Question :** pourquoi refuser **avant** de parser, quand c'est possible ?

### ☐ Étape 5 — Les schémas aussi (0.2.21 S5)

Une `SchemaFactory` sûre : `FEATURE_SECURE_PROCESSING`, propriétés `ACCESS_EXTERNAL_DTD` et `ACCESS_EXTERNAL_SCHEMA` à `""`. Compile `main.xsd`, qui contient un `xs:include`.
```
SCHEMA main.xsd : refuse (SAXParseException)
```
- le nom simple de l'exception attrapée (`SAXException` ou une sous-classe).
- **Question :** pourquoi un schéma qui en inclut un autre est-il un accès externe ?

### ☐ Étape 6 — Le `main` de `SafeImport`

Dans l'ordre : les 6 RISQUES, les 6 STRICT, les 3 ANCIEN, les 6 IMPORT, puis SCHEMA.

---

## Checklist API (vérifiée par `Check`)

| Élément | Étape | ☐ |
|---|---|---|
| `enum `, `EnumSet`, `Pattern` | 1 | ☐ |
| `XMLConstants.FEATURE_SECURE_PROCESSING`, `disallow-doctype-decl`, `external-general-entities`, `external-parameter-entities` | 2 | ☐ |
| `XMLConstants.ACCESS_EXTERNAL_DTD`, `XMLConstants.ACCESS_EXTERNAL_SCHEMA`, `setXIncludeAware(false)`, `setExpandEntityReferences(false)` | 2 | ☐ |
| `SUPPORT_DTD`, `IS_SUPPORTING_EXTERNAL_ENTITIES` | 2 | ☐ |
| `load-external-dtd`, `jdk.xml.entityExpansionLimit` | 3 | ☐ |
| `containsAll(` | 4 | ☐ |
| `SchemaFactory` | 5 | ☐ |
| ~~`XmlKit`~~, ~~`getMessage()`~~, ~~`javax.xml.xpath`~~ | **interdits** | — |

---

## Sortie attendue complète

C'est le contrat exact que vérifie `Check` :

```
a remplir
```
