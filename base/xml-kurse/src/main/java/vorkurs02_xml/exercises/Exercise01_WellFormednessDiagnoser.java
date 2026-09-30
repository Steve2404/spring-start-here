package vorkurs02_xml.exercises;

import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import vorkurs02_xml.ExerciseChecker;
import vorkurs02_xml.Fixtures;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * EXERCICE 1 - Ecrire son propre verificateur de bonne formation (niveau : challenge / entretien)
 * ==============================================================================================
 *
 * -- Rappel du decoupage en "boites magiques" --
 *
 * Une methode, c'est une boite magique : tu la nourris d'ingredients
 * (parametres), et elle rend un resultat, sans que tu aies besoin de
 * savoir comment elle travaille dedans. Pour CHAQUE etape d'un plan,
 * demande-toi : est-ce qu'elle se raconte seule ? revient-elle
 * plusieurs fois ? cache-t-elle sa propre petite recette ? Si oui a au
 * moins une question, elle merite sa propre boite.
 *
 * -- Le contexte --
 *
 * Le cours (0.2.2 a 0.2.9) a pose les regles de "well-formed" : noms
 * valides, balises bien fermees dans l'ordre LIFO, attributs uniques et
 * entre guillemets, '&' et '<' echappes, un seul element racine, rien
 * d'autre que des blancs/commentaires/PI autour de la racine.
 *
 * Ta mission : ecrire un MINI-PARSEUR qui ne construit rien, mais qui
 * trouve la PREMIERE regle violee et sa ligne. Le vrai parseur du JDK
 * sert d'arbitre : pour chaque fichier de test, ton verdict "OK / pas
 * OK" doit etre identique au sien. Seule difference voulue : tu donnes
 * un NOM de regle precis (Rule) au lieu d'un message en allemand.
 *
 * Perimetre (pour rester raisonnable) : declaration XML, commentaires,
 * processing instructions, elements, attributs, texte et references.
 * Pas de DOCTYPE ni de CDATA dans cet exercice (voir exercices 3 et 4).
 *
 * Les fichiers de test sont dans src/main/resources/vorkurs02_xml/
 * fixtures/ex01/ : le nom de chaque fichier donne la regle attendue
 * (ex. "15-MISMATCHED_END_TAG.xml"). Ouvre-les, ils sont courts.
 *
 * Deja ecrits pour toi (a ne pas modifier) : les types Rule, Kind,
 * Attribute, Tag, Diagnosis, XmlProblem, et, apres main(), lineOf(...)
 * et jdkSaysWellFormed(...).
 *
 *
 * ==================================================================
 * TODO 1 : isXmlName(name)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Un prenom peut contenir des chiffres ("Louis14"), mais ne commence
 * jamais par un chiffre. XML fait pareil avec deux listes : les
 * caracteres permis au DEBUT (NameStartChar) et ceux permis ENSUITE
 * (NameChar, un peu plus large : chiffres, '-', '.', point median...).
 * Attention : "xml-foo" est une orthographe correcte. Le prefixe "xml"
 * est RESERVE (on ne devrait pas l'inventer), mais le parseur l'accepte :
 * ce n'est pas une erreur de bonne formation.
 *
 * -- Essayons a la main --
 *
 *   "order"        -> o start, r,d,e,r ensuite          -> true
 *   "_plat.du-jour"-> '_' start, '.', '-' ensuite       -> true
 *   "cafe·menu" (avec e accent) -> accent et '·' sont permis ensuite -> true
 *   "1st"          -> '1' interdit au debut             -> false
 *   "·a"           -> '·' permis ensuite, PAS au debut  -> false
 *   ""             -> pas de premier caractere          -> false
 *
 * -- Le plan --
 *
 *   1. Refuser null ou vide.
 *   2. Decouper en codepoints (pas en char !).
 *   3. Le premier doit etre un NameStartChar.
 *   4. Tous les suivants doivent etre des NameChar.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui : "est-ce un NameStartChar ?" et "est-ce un NameChar ?" sont deux
 * petites recettes (des listes de plages) : fais-en deux methodes privees
 * isNameStartChar(int) et isNameChar(int). La 2e appelle la 1re.
 *
 *
 * ==================================================================
 * TODO 2 : readTag(doc, lt)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * doc.charAt(lt) est un '<' qui ouvre une balise de debut, de fin ou
 * vide. Tu dois la lire jusqu'a son '>' comme on lit une etiquette de
 * colis : le nom, puis chaque "cle=valeur". A la moindre chose qui ne
 * ressemble pas a une etiquette, tu jettes une XmlProblem(regle, lt).
 *
 * Les pieges verifies avec le vrai parseur :
 *   <item sku="X1" qty = '2'/>  OK   (blancs autour de '=' permis, ' ou ")
 *   <a b='1'c='2'/>             MALFORMED_TAG (pas de blanc entre attributs)
 *   <price currency=EUR>        MALFORMED_TAG (valeur sans guillemets)
 *   <a/ >                       MALFORMED_TAG ('/' doit coller au '>')
 *   </order >                   OK   (blanc permis avant '>' d'une fin)
 *   </ order>                   BAD_NAME (le nom doit suivre "</")
 *   <1st-item>                  BAD_NAME
 *   <item -qty="2">             BAD_NAME (nom d'ATTRIBUT invalide)
 *
 * -- Essayons a la main --
 *
 *   readTag("<item sku=\"X1\" qty = '2'/>", 0)
 *     nom "item" ; blanc ; sku = "X1" ; blanc ; qty = '2' ; puis "/>"
 *     -> Tag(EMPTY, "item", [sku=X1, qty=2], start 0, end 26)
 *   readTag("x</order >", 1) -> Tag(END, "order", [], start 1, end 10)
 *
 * -- Le plan --
 *
 *   1. Balise de fin si doc commence par "</" en lt.
 *   2. Lire le nom : avancer tant que ce n'est ni un blanc, ni '/', ni '>'.
 *      Nom invalide -> BAD_NAME.
 *   3. Balise de fin : sauter les blancs, exiger '>' -> Tag END.
 *   4. Sinon boucler :
 *      a. sauter les blancs en retenant s'il y en avait ;
 *      b. '>' -> Tag START ; "/>" -> Tag EMPTY ;
 *      c. sinon, il FALLAIT un blanc avant cet attribut ;
 *      d. lire le nom de l'attribut (s'arreter aussi sur '='), le valider ;
 *      e. blancs, '=', blancs, un guillemet ' ou ", puis chercher le MEME
 *         guillemet fermant ; ajouter l'Attribute.
 *   5. Arriver a la fin du texte sans '>' -> MALFORMED_TAG.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui : "sauter les blancs XML" revient 5 fois. Ecris skipWhitespace
 * (doc, j) et isXmlWhitespace(c). ATTENTION : XML ne connait que 4
 * blancs (espace, \t, \n, \r) - pas Character.isWhitespace().
 *
 *
 * ==================================================================
 * TODO 3 : checkAttributes(tag)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Un colis ne peut pas porter deux etiquettes "poids" differentes : on ne
 * saurait pas laquelle croire. Et une valeur d'attribut n'a pas le droit
 * de contenir '<' (le parseur croirait qu'une balise commence) ni un '&'
 * qui ne forme pas une vraie reference. Par contre "a > b ]]> c" est
 * PERMIS dans un attribut (verifie avec le parseur).
 *
 * -- Essayons a la main --
 *
 *   [sku=X1, qty=2, sku=X9]   -> DUPLICATE_ATTRIBUTE
 *   [test="stock < 10"]       -> LT_IN_ATTRIBUTE
 *   [label="Prix&nbsp;"]      -> BAD_REFERENCE (nbsp n'existe pas en XML)
 *   [note="a > b ]]> c"]      -> vide (tout va bien)
 *
 * -- Le plan --
 *
 *   1. Pour chaque attribut, dans l'ordre :
 *      a. nom deja vu -> DUPLICATE_ATTRIBUTE ;
 *      b. '<' dans la valeur -> LT_IN_ATTRIBUTE ;
 *      c. references de la valeur invalides -> la regle du TODO 4.
 *   2. Rien trouve -> boite vide.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui : l'etape c EST le TODO 4 (checkReferences), reutilise aussi pour
 * le texte.
 *
 *
 * ==================================================================
 * TODO 4 : checkReferences(text)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * En XML, '&' est un panneau "attention, code secret !". Le code doit
 * finir par ';' et etre l'un des codes connus : les 5 predefinis (lt,
 * gt, amp, apos, quot), ou un numero de caractere &#233; / &#xE9;.
 * Verifie avec le parseur :
 *   "Tom & Jerry"  -> invalide (pas de ';' apres "& Jerry...")
 *   "&amp"         -> invalide (pas de ';')
 *   "&AMP;"        -> invalide (les noms sont sensibles a la casse)
 *   "&#X41;"       -> invalide (le x de l'hexa est MINUSCULE)
 *   "&#0;"         -> invalide (le caractere 0 est interdit en XML 1.0)
 *   "&#x10FFFF;"   -> valide
 *
 * -- Le plan --
 *
 *   1. Chercher chaque '&'.
 *   2. Chercher le ';' suivant ; absent -> BAD_REFERENCE.
 *   3. Le "corps" entre les deux doit etre : un des 5 noms, ou '#'+chiffres,
 *      ou "#x"+hexa ; dans les 2 derniers cas le codepoint doit etre un
 *      caractere XML permis (voir indices).
 *   4. Continuer apres le ';'.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui : "ce corps de reference est-il valide ?" se raconte seul
 * (isValidReferenceBody). Et Integer.parseInt peut exploser sur un
 * nombre geant : protege-le dans une petite methode.
 *
 *
 * ==================================================================
 * TODO 5 : checkText(text)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Dans le texte, '>' seul est permis (<a>a>b</a> passe), MAIS la suite
 * "]]>" est reservee a la fin d'une section CDATA : la trouver dans du
 * texte normal, c'est comme ecrire "FIN" au milieu d'un livre.
 *
 * -- Essayons a la main --
 *
 *   "if (a[b[0]]> 3)" -> contient "]]>" -> CDATA_END_IN_TEXT
 *   "a>b"             -> vide
 *   "Tom & Jerry"     -> BAD_REFERENCE
 *
 * -- Le plan --
 *
 *   1. D'abord les references (TODO 4).
 *   2. Puis la recherche de "]]>".
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non : deux lignes qui reutilisent le TODO 4.
 *
 *
 * ==================================================================
 * TODO 6 : checkComment(content)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * content est ce qu'il y a ENTRE "<!--" et "-->". La regle : jamais deux
 * tirets "--" a l'interieur, et pas de tiret colle a la fin (sinon on
 * lit "--->"). Le commentaire vide <!----> est permis !
 *
 * -- Essayons a la main --
 *
 *   " ok "          -> vide
 *   ""              -> vide
 *   " TODO -- x "   -> BAD_COMMENT
 *   " a-"           -> BAD_COMMENT
 *   " - "           -> vide
 *
 * -- Le plan --
 *
 *   1. Contient "--" ou finit par "-" -> BAD_COMMENT.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non : une condition.
 *
 *
 * ==================================================================
 * TODO 7 : diagnose(doc)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Imagine une pile d'assiettes : chaque balise ouvrante pose une
 * assiette, chaque balise fermante doit retirer CELLE DU DESSUS et
 * porter le meme nom exact. Si le nom differe (Title / title, ou
 * <b><i></b>), c'est MISMATCHED_END_TAG. Si on veut retirer une
 * assiette alors que la pile est vide : END_WITHOUT_START. S'il reste
 * des assiettes a la fin : UNCLOSED_ELEMENT, et on donne la ligne de
 * l'assiette du dessus (la CAUSE ; le parseur, lui, ne s'en rend compte
 * qu'a la fin du fichier : c'est le point de DECOUVERTE, cf. 0.2.22).
 *
 * Autour de la racine (pile vide) : seulement des blancs, commentaires
 * et PI. Du texte -> CONTENT_OUTSIDE_ROOT ; une 2e racine ->
 * MULTIPLE_ROOTS ; aucune racine -> NO_ROOT. La declaration <?xml ...?>
 * n'est permise qu'a la position 0 (meme une ligne vide avant la rend
 * fautive : MISPLACED_DECLARATION, comme toute PI de cible xml/XML).
 *
 * La ligne rapportee est celle ou COMMENCE la construction fautive
 * (balise, commentaire, PI, texte). 0 pour OK et NO_ROOT.
 *
 * -- Essayons a la main --
 *
 *   17-END_WITHOUT_START.xml
 *     <order>      pile [order]
 *       <item/>    vide : rien a empiler
 *     </order>     pile []
 *     </order>     pile deja vide -> END_WITHOUT_START ligne 4
 *
 *   18-UNCLOSED_ELEMENT.xml
 *     <order> <items> <item>..</item> <item>..</item> puis fin du fichier
 *     pile [items, order] -> UNCLOSED_ELEMENT, ligne de <items> = 2
 *
 *   07-DUPLICATE_ATTRIBUTE.xml : la balise <item ... commence ligne 2
 *     -> ligne 2 (le parseur dit ligne 4, la ou il DECOUVRE le doublon).
 *
 * -- Le plan --
 *
 *   1. Si le texte commence par "<?xml" + un blanc : sauter jusqu'au "?>".
 *   2. Tant qu'il reste du texte :
 *      a. pas un '<' : lire le texte jusqu'au prochain '<'. Pile vide ->
 *         seul du blanc est permis ; sinon checkText.
 *      b. "<!--" : trouver "-->", checkComment sur l'interieur.
 *      c. "<?"   : trouver "?>", la cible (1er mot) ne doit pas valoir
 *         xml (casse ignoree) et doit etre un nom XML.
 *      d. sinon : readTag (attraper XmlProblem), checkAttributes, puis
 *         START/EMPTY : 2e racine ? START : empiler ;
 *         END : pile vide ? depiler et comparer les noms.
 *   3. Fin : pile non vide -> UNCLOSED_ELEMENT ; pas de racine -> NO_ROOT ;
 *      sinon OK.
 *   Commentaire ou PI jamais ferme -> MALFORMED_TAG.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Deja fait : chaque sous-verification est un TODO precedent. Ajoute
 * juste firstNonBlank(doc, i) pour pointer la ligne du vrai texte (et
 * non celle du saut de ligne qui le precede).
 *
 *
 * Exemple a verifier :
 *
 *   isXmlName : order, _plat.du-jour, cafe·menu, a:b, xml-foo -> true ;
 *               1st, -x, .a, ·a, "a b", "" -> false (et meme verdict que le JDK)
 *   readTag("<item sku=\"X1\" qty = '2'/>", 0) == Tag(EMPTY, item, [sku=X1, qty=2], 0, 26)
 *   readTag("x</order >", 1) == Tag(END, order, [], 1, 10)
 *   readTag : "<a b='1'c='2'/>", "<a b=1/>", "<a/ >" -> MALFORMED_TAG ;
 *             "<1st>", "</ a>", "<a -x='1'/>" -> BAD_NAME
 *   checkAttributes : doublon, '<', &nbsp; -> DUPLICATE_ATTRIBUTE, LT_IN_ATTRIBUTE, BAD_REFERENCE ;
 *                     "a > b ]]> c" -> vide
 *   checkReferences : "Tom &amp; Jerry &#233; &#x20AC;" -> vide ; "Tom & Jerry", "&amp",
 *                     "&AMP;", "&#X41;", "&#0;", "&#99999999999;" -> BAD_REFERENCE ; "&#x10FFFF;" -> vide
 *   checkText : "if (a[b[0]]> 3)" -> CDATA_END_IN_TEXT ; "a>b" -> vide
 *   checkComment : " ok ", "", " - " -> vide ; " TODO -- x ", " a-" -> BAD_COMMENT
 *   diagnose sur les 21 fichiers de fixtures/ex01 : la regle du nom de fichier,
 *     ET (regle == OK) == verdict du parseur du JDK
 *   lignes : 07 -> 2, 14 -> 2, 15 -> 2, 17 -> 4, 18 -> 2, 19 -> 4, 20 -> 2, 21 -> NO_ROOT/0
 *
 *
 * Indices techniques Java (a lire seulement si le plan a la main est
 * clair mais que la traduction en code bloque) :
 *
 *   - int[] cps = name.codePoints().toArray();
 *   - NameStartChar ::= ":" | [A-Z] | "_" | [a-z] | [#xC0-#xD6] | [#xD8-#xF6] | [#xF8-#x2FF]
 *       | [#x370-#x37D] | [#x37F-#x1FFF] | [#x200C-#x200D] | [#x2070-#x218F] | [#x2C00-#x2FEF]
 *       | [#x3001-#xD7FF] | [#xF900-#xFDCF] | [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]
 *     NameChar ::= NameStartChar | "-" | "." | [0-9] | #xB7 | [#x0300-#x036F] | [#x203F-#x2040]
 *   - Caractere XML 1.0 permis : #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
 *   - doc.startsWith("/>", j) ; doc.indexOf(quote, j + 1) ; body.matches("#x[0-9a-fA-F]+")
 *   - Integer.parseInt(digits, 16) dans un try/catch (NumberFormatException) -> -1
 *   - Set<String> seen = new HashSet<>(); if (!seen.add(name)) ... (add rend false si deja present)
 *   - Deque<Tag> open = new ArrayDeque<>(); open.push(tag); open.pop(); open.peek();
 *   - switch (tag.kind()) { case START, EMPTY -> { ... } case END -> { ... } }
 */
public class Exercise01_WellFormednessDiagnoser {

    public enum Rule {
        OK, BAD_NAME, MALFORMED_TAG, DUPLICATE_ATTRIBUTE, LT_IN_ATTRIBUTE, BAD_REFERENCE,
        CDATA_END_IN_TEXT, BAD_COMMENT, MISPLACED_DECLARATION, MISMATCHED_END_TAG,
        END_WITHOUT_START, UNCLOSED_ELEMENT, CONTENT_OUTSIDE_ROOT, MULTIPLE_ROOTS, NO_ROOT
    }

    public enum Kind { START, END, EMPTY }

    public record Attribute(String name, String value) {
    }

    /** start = index du '<', end = index juste APRES le '>'. */
    public record Tag(Kind kind, String name, List<Attribute> attributes, int start, int end) {
    }

    /** line = ligne (1, 2, ...) ou commence la construction fautive ; 0 si OK ou NO_ROOT. */
    public record Diagnosis(Rule rule, int line) {
    }

    /** Levee par readTag : la regle violee et l'index du '<' de la balise. */
    public static final class XmlProblem extends RuntimeException {
        public final Rule rule;
        public final int index;

        public XmlProblem(Rule rule, int index) {
            super(rule + " @" + index);
            this.rule = rule;
            this.index = index;
        }
    }

    public static boolean isXmlName(String name) {
        throw new UnsupportedOperationException("TODO 1 : implementer isXmlName()");
    }

    public static Tag readTag(String doc, int lt) {
        throw new UnsupportedOperationException("TODO 2 : implementer readTag()");
    }

    public static Optional<Rule> checkAttributes(Tag tag) {
        throw new UnsupportedOperationException("TODO 3 : implementer checkAttributes()");
    }

    public static Optional<Rule> checkReferences(String text) {
        throw new UnsupportedOperationException("TODO 4 : implementer checkReferences()");
    }

    public static Optional<Rule> checkText(String text) {
        throw new UnsupportedOperationException("TODO 5 : implementer checkText()");
    }

    public static Optional<Rule> checkComment(String content) {
        throw new UnsupportedOperationException("TODO 6 : implementer checkComment()");
    }

    public static Diagnosis diagnose(String doc) {
        throw new UnsupportedOperationException("TODO 7 : implementer diagnose()");
    }

    public static void main(String[] args) throws IOException {
        for (String n : List.of("order", "_plat.du-jour", "café·menu", "a:b", "xml-foo")) {
            ExerciseChecker.check("isXmlName(\"" + n + "\") == true (et le JDK est d'accord)",
                    isXmlName(n) && jdkSaysWellFormed("<" + n + "/>"));
        }
        for (String n : List.of("1st", "-x", ".a", "·a")) {
            ExerciseChecker.check("isXmlName(\"" + n + "\") == false (et le JDK est d'accord)",
                    !isXmlName(n) && !jdkSaysWellFormed("<" + n + "/>"));
        }
        ExerciseChecker.check("isXmlName(\"a b\") et isXmlName(\"\") == false", !isXmlName("a b") && !isXmlName(""));

        ExerciseChecker.check("readTag(<item sku=\"X1\" qty = '2'/>) == EMPTY item [sku=X1, qty=2] 0..26",
                readTag("<item sku=\"X1\" qty = '2'/>", 0).equals(new Tag(Kind.EMPTY, "item",
                        List.of(new Attribute("sku", "X1"), new Attribute("qty", "2")), 0, 26)));
        ExerciseChecker.check("readTag(x</order >, 1) == END order 1..10",
                readTag("x</order >", 1).equals(new Tag(Kind.END, "order", List.of(), 1, 10)));
        ExerciseChecker.check("readTag(<note>) == START note sans attribut",
                readTag("<note>x</note>", 0).equals(new Tag(Kind.START, "note", List.of(), 0, 6)));
        for (String bad : List.of("<a b='1'c='2'/>", "<a b=1/>", "<a/ >", "<a x='1\"/>", "<a")) {
            ExerciseChecker.check("readTag(" + bad + ") -> MALFORMED_TAG", ruleOfReadTag(bad) == Rule.MALFORMED_TAG);
        }
        for (String bad : List.of("<1st>", "</ a>", "<a -x='1'/>")) {
            ExerciseChecker.check("readTag(" + bad + ") -> BAD_NAME", ruleOfReadTag(bad) == Rule.BAD_NAME);
        }

        ExerciseChecker.check("checkAttributes : doublon -> DUPLICATE_ATTRIBUTE",
                checkAttributes(tagWith("sku", "X1", "qty", "2", "sku", "X9")).equals(Optional.of(Rule.DUPLICATE_ATTRIBUTE)));
        ExerciseChecker.check("checkAttributes : '<' -> LT_IN_ATTRIBUTE",
                checkAttributes(tagWith("test", "stock < 10")).equals(Optional.of(Rule.LT_IN_ATTRIBUTE)));
        ExerciseChecker.check("checkAttributes : &nbsp; -> BAD_REFERENCE",
                checkAttributes(tagWith("label", "Prix&nbsp;")).equals(Optional.of(Rule.BAD_REFERENCE)));
        ExerciseChecker.check("checkAttributes : 'a > b ]]> c' est permis",
                checkAttributes(tagWith("note", "a > b ]]> c", "q", "&quot;x&quot;")).isEmpty());

        ExerciseChecker.check("checkReferences(Tom &amp; Jerry &#233; &#x20AC;) -> vide",
                checkReferences("Tom &amp; Jerry &#233; &#x20AC;").isEmpty());
        for (String bad : List.of("Tom & Jerry", "&amp", "&AMP;", "&#X41;", "&#0;", "&#99999999999;", "&#x;")) {
            ExerciseChecker.check("checkReferences(" + bad + ") -> BAD_REFERENCE",
                    checkReferences(bad).equals(Optional.of(Rule.BAD_REFERENCE)));
        }
        ExerciseChecker.check("checkReferences(&#x10FFFF;) -> vide", checkReferences("&#x10FFFF;").isEmpty());

        ExerciseChecker.check("checkText(if (a[b[0]]> 3)) -> CDATA_END_IN_TEXT",
                checkText("if (a[b[0]]> 3)").equals(Optional.of(Rule.CDATA_END_IN_TEXT)));
        ExerciseChecker.check("checkText(a>b) -> vide", checkText("a>b").isEmpty());
        ExerciseChecker.check("checkText(Tom & Jerry) -> BAD_REFERENCE", checkText("Tom & Jerry").equals(Optional.of(Rule.BAD_REFERENCE)));

        ExerciseChecker.check("checkComment(' ok '), (''), (' - ') -> vide",
                checkComment(" ok ").isEmpty() && checkComment("").isEmpty() && checkComment(" - ").isEmpty());
        ExerciseChecker.check("checkComment(' TODO -- x ') -> BAD_COMMENT", checkComment(" TODO -- x ").equals(Optional.of(Rule.BAD_COMMENT)));
        ExerciseChecker.check("checkComment(' a-') -> BAD_COMMENT", checkComment(" a-").equals(Optional.of(Rule.BAD_COMMENT)));

        List<Path> files;
        try (Stream<Path> s = Files.list(Fixtures.path("ex01"))) {
            files = s.filter(p -> p.toString().endsWith(".xml")).sorted().toList();
        }
        ExerciseChecker.check("21 fichiers de test trouves", files.size() == 21);
        for (Path file : files) {
            String name = file.getFileName().toString();
            Rule expected = Rule.valueOf(name.substring(3, name.length() - 4));
            String doc = Files.readString(file);
            Diagnosis d = diagnose(doc);
            boolean jdk = jdkSaysWellFormed(doc);
            ExerciseChecker.check(name + " -> " + expected + " (obtenu " + d.rule() + ", JDK well-formed=" + jdk + ")",
                    d.rule() == expected && (expected == Rule.OK) == jdk);
        }

        ExerciseChecker.check("ligne 07-DUPLICATE_ATTRIBUTE == 2 (debut de la balise)", lineFor("07-DUPLICATE_ATTRIBUTE.xml") == 2);
        ExerciseChecker.check("ligne 14-MISPLACED_DECLARATION == 2", lineFor("14-MISPLACED_DECLARATION.xml") == 2);
        ExerciseChecker.check("ligne 15-MISMATCHED_END_TAG == 2", lineFor("15-MISMATCHED_END_TAG.xml") == 2);
        ExerciseChecker.check("ligne 17-END_WITHOUT_START == 4", lineFor("17-END_WITHOUT_START.xml") == 4);
        ExerciseChecker.check("ligne 18-UNCLOSED_ELEMENT == 2 (<items>, l'assiette du dessus)", lineFor("18-UNCLOSED_ELEMENT.xml") == 2);
        ExerciseChecker.check("ligne 19-CONTENT_OUTSIDE_ROOT == 4", lineFor("19-CONTENT_OUTSIDE_ROOT.xml") == 4);
        ExerciseChecker.check("ligne 20-MULTIPLE_ROOTS == 2", lineFor("20-MULTIPLE_ROOTS.xml") == 2);
        ExerciseChecker.check("21-NO_ROOT -> Diagnosis(NO_ROOT, 0)",
                diagnose(Fixtures.read("ex01/21-NO_ROOT.xml")).equals(new Diagnosis(Rule.NO_ROOT, 0)));
        ExerciseChecker.check("document vide -> NO_ROOT", diagnose("").rule() == Rule.NO_ROOT);

        ExerciseChecker.summary();
    }

    // ------------------------------------------------------------------
    // Deja ecrit : outils de test (ne pas modifier)
    // ------------------------------------------------------------------

    private static Rule ruleOfReadTag(String tag) {
        try {
            readTag(tag, 0);
            return Rule.OK;
        } catch (XmlProblem p) {
            return p.rule;
        }
    }

    private static Tag tagWith(String... nameValues) {
        List<Attribute> attrs = new java.util.ArrayList<>();
        for (int i = 0; i < nameValues.length; i += 2) {
            attrs.add(new Attribute(nameValues[i], nameValues[i + 1]));
        }
        return new Tag(Kind.EMPTY, "x", attrs, 0, 1);
    }

    private static int lineFor(String fixture) {
        return diagnose(Fixtures.read("ex01/" + fixture)).line();
    }

    /** Numero de ligne (1, 2, ...) du caractere d'index index. */
    static int lineOf(String doc, int index) {
        int line = 1;
        for (int k = 0; k < index && k < doc.length(); k++) {
            if (doc.charAt(k) == '\n') {
                line++;
            }
        }
        return line;
    }

    /** L'arbitre : le vrai parseur DOM du JDK dit-il "well-formed" ? */
    static boolean jdkSaysWellFormed(String doc) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            builder.setErrorHandler(new DefaultHandler());
            builder.parse(new InputSource(new StringReader(doc)));
            return true;
        } catch (SAXParseException e) {
            return false;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
