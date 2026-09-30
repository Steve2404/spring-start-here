package vorkurs02_xml.exercises;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import vorkurs02_xml.ExerciseChecker;
import vorkurs02_xml.Fixtures;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * EXERCICE 5 - Compiler un modele de contenu DTD en expression reguliere (niveau : challenge / entretien)
 * =====================================================================================================
 *
 * Rappel express du decoupage en "boites magiques" : voir Exercise01_WellFormednessDiagnoser.java.
 *
 * -- Le contexte --
 *
 * Question d'entretien classique : "comment un validateur DTD verifie-t-il
 * <!ELEMENT section (heading, (item | bundle)*, note?)> ?" Reponse : un
 * modele de contenu EST une expression reguliere, dont les lettres sont
 * des noms d'elements. Si on ecrit la suite des enfants d'un element
 * sous la forme "heading;item;bundle;note;", il suffit de la comparer a
 * la regex (?:heading;)(?:(?:item;)|(?:bundle;))*(?:note;)? .
 *
 * Tu vas ecrire ce compilateur (tokenizer + descente recursive), puis un
 * mini-validateur. main() compare ton verdict a celui du vrai parseur
 * validant du JDK sur les 8 fichiers de fixtures/ex05 (DTD :
 * catalog.dtd, ouvre-la).
 *
 * Verdicts du JDK a retrouver (verifies) :
 *   02 : bundle avec UN seul item      -> "(item,item+)" incomplet
 *   03 : item avec 3 tags              -> "(name,price,(tag,tag?)?)" viole
 *   04 : note AVANT les items          -> section viole son modele
 *   05 : du texte "promo" dans <item>  -> interdit (contenu "elements seulement")
 *   06 : <color> non declare           -> 2 erreurs (color, puis item)
 *   07 : <br/> dans <heading>          -> "(#PCDATA)" INTERDIT tout enfant element
 *   08 : <catalog> vide                -> "(section)+" incomplet
 *
 *
 * ==================================================================
 * TODO 1 : tokenize(model)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Avant de lire une phrase, on la coupe en mots. Ici les "mots" sont :
 * les 7 symboles ( ) , | ? * + (un caractere chacun), et les noms (ou
 * #PCDATA) qui vont jusqu'au prochain symbole ou blanc. Les blancs ne
 * sont que des separateurs.
 *
 * -- Essayons a la main --
 *
 *   "(heading, (item | bundle)*, note?)"
 *   -> [(, heading, ",", (, item, |, bundle, ), *, ",", note, ?, )]
 *
 * -- Le plan --
 *
 *   1. Blanc -> sauter. Symbole -> jeton d'un caractere.
 *   2. Sinon avancer jusqu'au prochain symbole/blanc -> un jeton nom.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non.
 *
 *
 * ==================================================================
 * TODO 2 : toRegex(model)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Tu traduis une recette DTD en recette regex, piece par piece :
 *   nom X            -> (?:X;)
 *   #PCDATA          -> ""  (le texte n'est pas un enfant element)
 *   ( a , b , c )    -> (?:ABC)          (a la suite)
 *   ( a | b | c )    -> (?:A|B|C)        (au choix)
 *   suivi de ? * +   -> recopie tel quel (la regex Java a les memes !)
 *   EMPTY            -> ""   (aucun enfant)
 *   ANY              -> (?:[^;]+;)*   (n'importe quels enfants)
 * Un groupe ne melange jamais ',' et '|' (sinon IllegalArgumentException).
 *
 * -- Essayons a la main --
 *
 *   "(title,author+,year?)"  -> "(?:(?:title;)(?:author;)+(?:year;)?)"
 *   "(#PCDATA|b|i)*"         -> "(?:|(?:b;)|(?:i;))*"
 *   "(#PCDATA)"              -> "(?:)"
 *
 * -- Le plan --
 *
 *   1. EMPTY / ANY : cas a part.
 *   2. Sinon tokenize, puis lire UNE particule a partir du jeton 0.
 *   3. Il ne doit rester aucun jeton.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui, deux, qui s'appellent l'une l'autre (recursion croisee) :
 *   particle(tokens, pos) : un nom, #PCDATA ou "(" -> group(...), puis
 *                           le quantificateur eventuel ;
 *   group(tokens, pos)    : particule, puis (separateur particule)*
 *                           jusqu'a ")".
 * La position courante doit etre partagee et modifiable : int[] pos.
 *
 *
 * ==================================================================
 * TODO 3 : childSequence(element)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Tu fais l'appel des enfants DIRECTS de l'element, seulement ceux qui
 * sont des elements (pas le texte, pas les commentaires, pas les
 * petits-enfants), et tu ecris "nom;" pour chacun.
 *
 * -- Essayons a la main --
 *
 *   <item><name>A</name> <price>1</price><!--x--><tag>t</tag></item>
 *   -> "name;price;tag;"
 *
 * -- Le plan --
 *
 *   1. De getFirstChild() a null par getNextSibling().
 *   2. Garder les ELEMENT_NODE, ajouter nom + ';'.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non.
 *
 *
 * ==================================================================
 * TODO 4 : hasOnlyWhitespaceText(element)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Dans un contenu "elements seulement" comme (name, price), les retours
 * a la ligne et l'indentation sont toleres, mais pas un vrai mot comme
 * "promo". Regarde les noeuds texte DIRECTS : tous blancs ?
 *
 * -- Le plan --
 *
 *   1. Parcourir les enfants directs ; un TEXT_NODE non blanc -> false.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non.
 *
 *
 * ==================================================================
 * TODO 5 : allowsText(model)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Qui a le droit d'avoir du texte ? ANY, et les modeles qui contiennent
 * #PCDATA. Personne d'autre (EMPTY non plus).
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non : une ligne.
 *
 *
 * ==================================================================
 * TODO 6 : parseElementDeclarations(dtd)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Lis le texte de la DTD et rends la Map nom -> modele, dans l'ordre,
 * avec les blancs internes du modele ramenes a UN espace.
 *
 * -- Essayons a la main --
 *
 *   "<!ELEMENT item    (name, price, (tag, tag?)?)>" -> item = "(name, price, (tag, tag?)?)"
 *   "<!ELEMENT br      EMPTY>"                      -> br = "EMPTY"
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non : une regex et un replaceAll.
 *
 *
 * ==================================================================
 * TODO 7 : validate(doc, decls)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Tu es l'inspecteur : tu visites chaque element du document dans
 * l'ordre et tu ecris au plus UNE remarque par element :
 *   "nom -> non declare" : pas de modele pour ce nom ;
 *   "nom -> enfants"     : la suite des enfants ne colle pas a la regex ;
 *   "nom -> texte"       : les enfants collent, mais il y a du vrai
 *                          texte alors que le modele ne l'autorise pas.
 *
 * -- Essayons a la main --
 *
 *   06-undeclared.xml : ... item (name;price;color;) ne colle pas -> "item -> enfants"
 *                       puis on visite color -> "color -> non declare"
 *   05-text-in-item.xml : item = "name;price;" colle, mais "promo" -> "item -> texte"
 *
 * -- Le plan --
 *
 *   1. getElementsByTagName("*") donne tous les elements, ordre du document.
 *   2. Pour chacun : modele absent ? regex ? texte ?
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui : TODO 2 a 5.
 *
 *
 * Exemple a verifier :
 *
 *   tokenize : l'exemple du TODO 1
 *   toRegex : les 3 exemples du TODO 2, EMPTY -> "", ANY -> "(?:[^;]+;)*", melange , et | -> exception
 *   regex compilees : (item, item+) refuse "item;" et accepte "item;item;" ; 3 tags refuses ; etc.
 *   childSequence de l'exemple du TODO 3 == "name;price;tag;"
 *   parseElementDeclarations(catalog.dtd) : 13 entrees, item et br comme au TODO 6
 *   validate : 01 -> [] ; 02 -> [bundle -> enfants] ; 03 -> [item -> enfants] ;
 *              04 -> [section -> enfants] ; 05 -> [item -> texte] ;
 *              06 -> [item -> enfants, color -> non declare] ; 07 -> [heading -> enfants] ;
 *              08 -> [catalog -> enfants]
 *   ET pour chaque fichier : validate(...).isEmpty() == verdict du parseur validant du JDK
 *
 *
 * Indices techniques Java (a lire seulement si le plan a la main est
 * clair mais que la traduction en code bloque) :
 *
 *   - "(),|?*+".indexOf(c) >= 0 ; Character.isWhitespace(c)
 *   - int[] pos = {0}; String t = tokens.get(pos[0]++);
 *   - "?*+".contains(tokens.get(pos[0]))
 *   - String.join(sep, parts) avec sep = "" (sequence) ou "|" (choix)
 *   - for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling())
 *       if (n.getNodeType() == Node.ELEMENT_NODE) ...
 *   - Pattern.compile("<!ELEMENT\\s+(\\S+)\\s+(.*?)\\s*>", Pattern.DOTALL) ; model.replaceAll("\\s+", " ")
 *   - Pattern.matches(regex, sequence)
 */
public class Exercise05_ContentModelCompiler {

    public static List<String> tokenize(String model) {
        throw new UnsupportedOperationException("TODO 1 : implementer tokenize()");
    }

    public static String toRegex(String model) {
        throw new UnsupportedOperationException("TODO 2 : implementer toRegex()");
    }

    public static String childSequence(Element element) {
        throw new UnsupportedOperationException("TODO 3 : implementer childSequence()");
    }

    public static boolean hasOnlyWhitespaceText(Element element) {
        throw new UnsupportedOperationException("TODO 4 : implementer hasOnlyWhitespaceText()");
    }

    public static boolean allowsText(String model) {
        throw new UnsupportedOperationException("TODO 5 : implementer allowsText()");
    }

    public static Map<String, String> parseElementDeclarations(String dtd) {
        throw new UnsupportedOperationException("TODO 6 : implementer parseElementDeclarations()");
    }

    public static List<String> validate(Document doc, Map<String, String> decls) {
        throw new UnsupportedOperationException("TODO 7 : implementer validate()");
    }

    public static void main(String[] args) throws Exception {
        ExerciseChecker.check("tokenize((heading, (item | bundle)*, note?))",
                tokenize("(heading, (item | bundle)*, note?)").equals(
                        List.of("(", "heading", ",", "(", "item", "|", "bundle", ")", "*", ",", "note", "?", ")")));

        ExerciseChecker.check("toRegex((title,author+,year?))",
                toRegex("(title,author+,year?)").equals("(?:(?:title;)(?:author;)+(?:year;)?)"));
        ExerciseChecker.check("toRegex((#PCDATA|b|i)*)", toRegex("(#PCDATA|b|i)*").equals("(?:|(?:b;)|(?:i;))*"));
        ExerciseChecker.check("toRegex((#PCDATA)) == (?:)", toRegex("(#PCDATA)").equals("(?:)"));
        ExerciseChecker.check("toRegex(EMPTY) == \"\" et toRegex(ANY) == (?:[^;]+;)*",
                toRegex("EMPTY").isEmpty() && toRegex(" ANY ").equals("(?:[^;]+;)*"));
        ExerciseChecker.check("toRegex((a, b | c)) -> IllegalArgumentException", throwsIae(() -> toRegex("(a, b | c)")));

        ExerciseChecker.check("(item, item+) : 'item;' refuse, 'item;item;item;' accepte",
                !Pattern.matches(toRegex("(item, item+)"), "item;") && Pattern.matches(toRegex("(item, item+)"), "item;item;item;"));
        String item = toRegex("(name, price, (tag, tag?)?)");
        ExerciseChecker.check("(name, price, (tag, tag?)?) : 0, 1, 2 tags ok ; 3 tags refuses",
                Pattern.matches(item, "name;price;") && Pattern.matches(item, "name;price;tag;")
                        && Pattern.matches(item, "name;price;tag;tag;") && !Pattern.matches(item, "name;price;tag;tag;tag;"));
        String section = toRegex("(heading, (item | bundle)*, note?)");
        ExerciseChecker.check("section : heading;item;bundle;item;note; ok, heading;note;item; refuse",
                Pattern.matches(section, "heading;item;bundle;item;note;") && !Pattern.matches(section, "heading;note;item;"));
        ExerciseChecker.check("ANY accepte x;y; et EMPTY refuse x;",
                Pattern.matches(toRegex("ANY"), "x;y;") && !Pattern.matches(toRegex("EMPTY"), "x;"));

        Element sample = parseString("<item><name>A</name> <price>1</price><!--x--><tag>t<b>x</b></tag></item>").getDocumentElement();
        ExerciseChecker.check("childSequence == name;price;tag;", childSequence(sample).equals("name;price;tag;"));
        ExerciseChecker.check("hasOnlyWhitespaceText : blancs seulement -> true", hasOnlyWhitespaceText(sample));
        ExerciseChecker.check("hasOnlyWhitespaceText : 'promo' -> false",
                !hasOnlyWhitespaceText(parseString("<item>promo <name>A</name></item>").getDocumentElement()));
        ExerciseChecker.check("allowsText : (#PCDATA | b)*, ANY -> true ; (a, b), EMPTY -> false",
                allowsText("(#PCDATA | b)*") && allowsText("ANY") && !allowsText("(a, b)") && !allowsText("EMPTY"));

        Map<String, String> decls = parseElementDeclarations(Fixtures.read("ex05/catalog.dtd"));
        ExerciseChecker.check("parseElementDeclarations : 13 declarations", decls.size() == 13);
        ExerciseChecker.check("parseElementDeclarations : item et br",
                decls.get("item").equals("(name, price, (tag, tag?)?)") && decls.get("br").equals("EMPTY"));
        ExerciseChecker.check("parseElementDeclarations : ordre du fichier (catalog en 1er, extra en dernier)",
                decls.keySet().iterator().next().equals("catalog") && List.copyOf(decls.keySet()).get(12).equals("extra"));

        expect(decls, "01-valid.xml", List.of());
        expect(decls, "02-bundle-too-small.xml", List.of("bundle -> enfants"));
        expect(decls, "03-three-tags.xml", List.of("item -> enfants"));
        expect(decls, "04-note-not-last.xml", List.of("section -> enfants"));
        expect(decls, "05-text-in-item.xml", List.of("item -> texte"));
        expect(decls, "06-undeclared.xml", List.of("item -> enfants", "color -> non declare"));
        expect(decls, "07-empty-and-any.xml", List.of("heading -> enfants"));
        expect(decls, "08-no-section.xml", List.of("catalog -> enfants"));

        ExerciseChecker.summary();
    }

    // ------------------------------------------------------------------
    // Deja ecrit : outils de test (ne pas modifier)
    // ------------------------------------------------------------------

    private static void expect(Map<String, String> decls, String file, List<String> expected) throws Exception {
        Path path = Fixtures.path("ex05/" + file);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "file");
        List<String> got = validate(factory.newDocumentBuilder().parse(path.toFile()), decls);
        boolean jdk = jdkSaysValid(path);
        ExerciseChecker.check(file + " -> " + expected + " (obtenu " + got + ", JDK valide=" + jdk + ")",
                got.equals(expected) && got.isEmpty() == jdk);
    }

    private static boolean throwsIae(Runnable r) {
        try {
            r.run();
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private static Document parseString(String xml) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    /** L'arbitre : le parseur validant du JDK trouve-t-il zero erreur de validite ? */
    static boolean jdkSaysValid(Path xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "file");
        boolean[] valid = {true};
        var builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new ErrorHandler() {
            public void warning(SAXParseException e) {
            }

            public void error(SAXParseException e) {
                valid[0] = false;
            }

            public void fatalError(SAXParseException e) throws SAXException {
                throw e;
            }
        });
        builder.parse(xml.toFile());
        return valid[0];
    }
}
