package vorkurs02_xml.exercises;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import vorkurs02_xml.ExerciseChecker;
import vorkurs02_xml.Fixtures;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EXERCICE 2 - Echapper, decoder, normaliser : faire exactement ce que fait le parseur (niveau : challenge)
 * ======================================================================================================
 *
 * Rappel express du decoupage en "boites magiques" : voir Exercise01_WellFormednessDiagnoser.java.
 *
 * -- Le contexte --
 *
 * Le cours 0.2.6 dit : le texte qu'on ECRIT dans le fichier n'est pas
 * le texte que l'application RECOIT. Entre les deux, le parseur :
 *   - normalise les fins de ligne (CR LF et CR seul -> LF),
 *   - remplace les references (&lt; &#233; &#xE9; ...),
 *   - dans les ATTRIBUTS seulement : transforme chaque blanc litteral
 *     (tab, saut de ligne) en espace.
 *
 * Tu vas ecrire les deux sens du voyage : ECRIRE du XML sans jamais
 * perdre une information (escape...), et PREDIRE ce que le parseur va
 * rendre (decode..., normalize...). A chaque fois, main() demande au
 * vrai parseur du JDK de confirmer, par un aller-retour :
 *     parse("<a>" + escapeText(s) + "</a>").getTextContent() == s
 *
 * Fichier de test : fixtures/ex02/attributes.xml (ouvre-le dans un
 * editeur qui montre les tabulations et le CRLF de la ligne "crlf").
 *
 *
 * ==================================================================
 * TODO 1 : isLegalXml10Char(cp)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Unicode est une immense boite de crayons, mais XML 1.0 refuse
 * certains crayons : les caracteres de controle (sauf tab, LF, CR), les
 * "demi-caracteres" D800-DFFF, et FFFE/FFFF. Meme ecrits en &#1; ils
 * restent interdits (le parseur l'a confirme : "&#1" est invalide).
 *
 * -- Essayons a la main --
 *
 *   0x41 'A' -> true ; 0x9 tab -> true ; 0x1 -> false ; 0x1F600 emoji -> true
 *   0xFFFE -> false ; 0xD800 -> false ; 0x10FFFF -> true ; 0x110000 -> false
 *
 * -- Le plan --
 *
 *   1. Tester les 3 blancs permis, puis les 3 plages (voir indices).
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non : une seule expression booleenne. Mais TOUS les TODO suivants
 * l'utilisent.
 *
 *
 * ==================================================================
 * TODO 2 : normalizeLineEnds(raw)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Windows ecrit "retour chariot + saut de ligne" (CR LF), les vieux Mac
 * juste CR, Linux juste LF. Pour que tout le monde se comprenne, le
 * parseur ramene TOUT a LF, avant meme de regarder les balises.
 *
 * -- Essayons a la main --
 *
 *   "a\r\nb\rc\n"  -> "a\nb\nc\n"
 *   "\r\r\n"       -> "\n\n"   (un CR seul, puis un couple CR LF)
 *
 * -- Le plan --
 *
 *   1. Remplacer d'abord les couples CR LF.
 *   2. Puis les CR restants.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non. Mais reflechis a l'ORDRE : l'inverse donnerait "\n\n" pour "\r\n".
 *
 *
 * ==================================================================
 * TODO 3 : escapeText(text)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Tu glisses une lettre dans une enveloppe XML. Certains caracteres
 * ressemblent a des instructions pour le facteur : '<' (une balise
 * commence) et '&' (une reference commence). Tu les deguises en &lt; et
 * &amp;. Le '>' seul est inoffensif... sauf s'il termine "]]>". Et un
 * CR brut serait "corrige" en LF par le parseur : pour le garder, ecris
 * &#13;. Un caractere interdit (TODO 1) : IllegalArgumentException avec
 * le message "Caractere interdit en XML 1.0 : U+0001".
 *
 * -- Essayons a la main --
 *
 *   "1 < 2 && 3 > 2"  -> "1 &lt; 2 &amp;&amp; 3 > 2"
 *   "a]]>b"           -> "a]]&gt;b"
 *   "x\r\ny"          -> "x&#13;\ny"
 *   "\u0001"          -> IllegalArgumentException("... U+0001")
 *
 * -- Le plan --
 *
 *   1. Parcourir les CODEPOINTS (un emoji fait 2 char en Java).
 *   2. Refuser un codepoint interdit.
 *   3. & -> &amp; ; < -> &lt; ; > precede de "]]" -> &gt; ; CR -> &#13;
 *   4. Sinon recopier (appendCodePoint).
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui : "refuser un caractere interdit avec un beau message" sert aussi
 * aux TODO 4 et 5 : checkLegal(cp).
 *
 *
 * ==================================================================
 * TODO 4 : escapeAttribute(value, quote)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Une valeur d'attribut est entre guillemets : SEUL le guillemet choisi
 * comme delimiteur doit etre deguise (&quot; ou &apos;), l'autre peut
 * rester tel quel. Et surtout : le parseur transforme tout tab / saut
 * de ligne LITTERAL en espace. Pour qu'un "a\tb" revienne intact, il
 * faut ecrire "a&#9;b" (une reference n'est PAS normalisee).
 *
 * -- Essayons a la main --
 *
 *   ("il dit \"oui\" & l'autre", '"')  -> "il dit &quot;oui&quot; &amp; l'autre"
 *   ("il dit \"oui\" & l'autre", '\'') -> "il dit \"oui\" &amp; l&apos;autre"
 *   ("a\tb\nc\r\nd", '"')             -> "a&#9;b&#10;c&#13;&#10;d"
 *   ("a<b>c", '"')                     -> "a&lt;b>c"
 *
 * -- Le plan --
 *
 *   1. Codepoint par codepoint, refuser l'interdit.
 *   2. & < \t \n \r -> leur forme echappee.
 *   3. " et ' -> echappes seulement si c'est le delimiteur.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non : un switch. checkLegal existe deja.
 *
 *
 * ==================================================================
 * TODO 5 : decodeReferences(source)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * C'est le decodeur de messages secrets : chaque "&...;" est remplace
 * par le caractere qu'il represente. Le piege : UNE seule passe.
 * "&amp;lt;" donne "&lt;" (le & vient d'etre produit, on ne le relit
 * pas). Entite inconnue ("&nbsp;") ou '&' sans ';' ->
 * IllegalArgumentException ; reference vers un caractere interdit
 * ("&#0;") -> IllegalArgumentException aussi.
 *
 * -- Essayons a la main --
 *
 *   "1 &lt; 2 &amp;&amp; x &#233;&#xE9; &quot;&apos;&gt;" -> "1 < 2 && x ee \"'>" (e accent)
 *   "&#x1F600;" -> l'emoji (2 char en Java !)
 *   "&amp;lt;"  -> "&lt;"
 *
 * -- Le plan --
 *
 *   1. Recopier jusqu'au prochain '&'.
 *   2. Trouver le ';' (absent -> exception), decoder le corps, avancer.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui : "corps de reference -> codepoint" (decodeOne) se raconte seul :
 * 5 noms, decimal, hexa, sinon exception, et controle checkLegal.
 *
 *
 * ==================================================================
 * TODO 6 : normalizeAttributeValue(rawSource)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * rawSource est ce qui est ECRIT entre les guillemets dans le fichier.
 * Predis ce que getAttribute() rendra. Recette du parseur, DANS CET
 * ORDRE : fins de ligne, puis chaque blanc litteral -> espace, puis
 * seulement les references. Consequence verifiee :
 *   "a\tb"         -> "a b"     (tab litteral : espace)
 *   "a&#9;b"       -> "a\tb"    (reference : garde le tab)
 *   "x\r\r\ny"     -> "x  y"    (CR, puis CR LF : 2 fins de ligne, 2 espaces)
 *   "a&#13;&#10;b" -> "a\r\nb"
 *   "  a  b  "     -> "  a  b  " (les espaces ne sont PAS fusionnes pour un attribut CDATA)
 *
 * -- Le plan --
 *
 *   1. normalizeLineEnds.
 *   2. \t et \n litteraux -> ' '.
 *   3. decodeReferences.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui, et elles existent deja : TODO 2 et TODO 5.
 *
 *
 * ==================================================================
 * TODO 7 : toXmlElement(name, attributes, text)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Tu fabriques un element complet, pret a etre colle dans un fichier.
 * Pour chaque attribut, choisis le guillemet malin : '"' par defaut,
 * mais '\'' si la valeur contient des " et aucun ' (zero echappement).
 * Texte null -> element vide "<name .../>".
 *
 * -- Essayons a la main --
 *
 *   ("entry", {key=motd, note=il dit "oui"}, "a < b")
 *     -> <entry key="motd" note='il dit "oui"'>a &lt; b</entry>
 *   ("entry", {q="'}, null)  (contient les deux)
 *     -> <entry q="&quot;'"/>
 *
 * -- Le plan --
 *
 *   1. "<" + nom.
 *   2. Pour chaque attribut (dans l'ordre de la Map) : choisir le
 *      guillemet, ecrire  espace nom = guillemet valeurEchappee guillemet.
 *   3. text null -> "/>" ; sinon ">" + escapeText + "</nom>".
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui : TODO 3 et TODO 4.
 *
 *
 * Exemple a verifier :
 *
 *   isLegalXml10Char : A, tab, emoji, 10FFFF -> true ; 1, FFFE, D800, 110000 -> false
 *   normalizeLineEnds("a\r\nb\rc\n") == "a\nb\nc\n" ; ("\r\r\n") == "\n\n"
 *   escapeText : voir "Essayons a la main" + aller-retour JDK sur 5 textes pieges
 *   escapeAttribute : voir "Essayons a la main" + aller-retour JDK avec ' et "
 *   decodeReferences : voir "Essayons a la main" + meme resultat que le JDK ;
 *                      "&nbsp;", "&#0;", "Tom & Jerry" -> IllegalArgumentException
 *   normalizeAttributeValue : 5 cas du TODO 6 + les 7 <entry> de attributes.xml
 *                             donnent EXACTEMENT getAttribute("value") du JDK
 *   toXmlElement : les 2 cas du TODO 7 + aller-retour JDK
 *
 *
 * Indices techniques Java (a lire seulement si le plan a la main est
 * clair mais que la traduction en code bloque) :
 *
 *   - Char ::= #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
 *   - int[] cps = text.codePoints().toArray(); ... out.appendCodePoint(c);
 *   - String.format("Caractere interdit en XML 1.0 : U+%04X", c)
 *   - switch (c) { case '&' -> ...; case '<' -> ...; default -> ... } (c est un int : '&' marche)
 *   - s.replace("\r\n", "\n").replace('\r', '\n')
 *   - body.matches("#[0-9]{1,7}") ; Integer.parseInt(body.substring(2), 16)
 *   - map.forEach((k, v) -> ...) respecte l'ordre d'une LinkedHashMap
 */
public class Exercise02_EscapingAndNormalization {

    public static boolean isLegalXml10Char(int cp) {
        throw new UnsupportedOperationException("TODO 1 : implementer isLegalXml10Char()");
    }

    public static String normalizeLineEnds(String raw) {
        throw new UnsupportedOperationException("TODO 2 : implementer normalizeLineEnds()");
    }

    public static String escapeText(String text) {
        throw new UnsupportedOperationException("TODO 3 : implementer escapeText()");
    }

    public static String escapeAttribute(String value, char quote) {
        throw new UnsupportedOperationException("TODO 4 : implementer escapeAttribute()");
    }

    public static String decodeReferences(String source) {
        throw new UnsupportedOperationException("TODO 5 : implementer decodeReferences()");
    }

    public static String normalizeAttributeValue(String rawSource) {
        throw new UnsupportedOperationException("TODO 6 : implementer normalizeAttributeValue()");
    }

    public static String toXmlElement(String name, Map<String, String> attributes, String text) {
        throw new UnsupportedOperationException("TODO 7 : implementer toXmlElement()");
    }

    public static void main(String[] args) {
        ExerciseChecker.check("isLegalXml10Char : A, tab, emoji, 10FFFF -> true",
                isLegalXml10Char('A') && isLegalXml10Char(0x9) && isLegalXml10Char(0x1F600) && isLegalXml10Char(0x10FFFF));
        ExerciseChecker.check("isLegalXml10Char : 1, FFFE, D800, 110000 -> false",
                !isLegalXml10Char(0x1) && !isLegalXml10Char(0xFFFE) && !isLegalXml10Char(0xD800) && !isLegalXml10Char(0x110000));

        ExerciseChecker.check("normalizeLineEnds(a\\r\\nb\\rc\\n) == a\\nb\\nc\\n", normalizeLineEnds("a\r\nb\rc\n").equals("a\nb\nc\n"));
        ExerciseChecker.check("normalizeLineEnds(\\r\\r\\n) == \\n\\n", normalizeLineEnds("\r\r\n").equals("\n\n"));

        ExerciseChecker.check("escapeText(1 < 2 && 3 > 2)", escapeText("1 < 2 && 3 > 2").equals("1 &lt; 2 &amp;&amp; 3 > 2"));
        ExerciseChecker.check("escapeText(a]]>b) == a]]&gt;b", escapeText("a]]>b").equals("a]]&gt;b"));
        ExerciseChecker.check("escapeText(x\\r\\ny) == x&#13;\\ny", escapeText("x\r\ny").equals("x&#13;\ny"));
        ExerciseChecker.check("escapeText(\\u0001) -> IllegalArgumentException U+0001",
                messageOf(() -> escapeText("ok\u0001")).contains("U+0001"));
        ExerciseChecker.check("escapeText(\\uFFFE) -> IllegalArgumentException U+FFFE",
                messageOf(() -> escapeText("\uFFFE")).contains("U+FFFE"));
        for (String s : List.of("1 < 2 && 3 > 2", "a]]>b ]] > ]]>", "ligne1\r\nligne2\rfin", "emoji \uD83D\uDE00 ok", "  blancs\tconserves  ")) {
            ExerciseChecker.check("aller-retour JDK du texte [" + visible(s) + "]",
                    jdkParse("<a>" + escapeText(s) + "</a>").getDocumentElement().getTextContent().equals(s));
        }

        String both = "il dit \"oui\" & l'autre";
        ExerciseChecker.check("escapeAttribute(..., '\"')", escapeAttribute(both, '"').equals("il dit &quot;oui&quot; &amp; l'autre"));
        ExerciseChecker.check("escapeAttribute(..., '\\'')", escapeAttribute(both, '\'').equals("il dit \"oui\" &amp; l&apos;autre"));
        ExerciseChecker.check("escapeAttribute(a\\tb\\nc\\r\\nd)", escapeAttribute("a\tb\nc\r\nd", '"').equals("a&#9;b&#10;c&#13;&#10;d"));
        ExerciseChecker.check("escapeAttribute(a<b>c)", escapeAttribute("a<b>c", '"').equals("a&lt;b>c"));
        for (String v : List.of(both, "a\tb\nc\r\nd", "  x  ", "<&>\"'")) {
            ExerciseChecker.check("aller-retour JDK de l'attribut [" + visible(v) + "] avec \" et '",
                    jdkParse("<a v=\"" + escapeAttribute(v, '"') + "\"/>").getDocumentElement().getAttribute("v").equals(v)
                            && jdkParse("<a v='" + escapeAttribute(v, '\'') + "'/>").getDocumentElement().getAttribute("v").equals(v));
        }

        String refs = "1 &lt; 2 &amp;&amp; x &#233;&#xE9; &quot;&apos;&gt;";
        ExerciseChecker.check("decodeReferences(" + refs + ")", decodeReferences(refs).equals("1 < 2 && x \u00e9\u00e9 \"'>"));
        ExerciseChecker.check("decodeReferences(&#x1F600;) == l'emoji (2 char)", decodeReferences("&#x1F600;").equals("\uD83D\uDE00"));
        ExerciseChecker.check("decodeReferences(&amp;lt;) == &lt; (une seule passe)", decodeReferences("&amp;lt;").equals("&lt;"));
        for (String s : List.of(refs, "&#x1F600;", "&amp;lt;", "a&#10;b&#x9;c")) {
            ExerciseChecker.check("decodeReferences == JDK pour [" + s + "]",
                    decodeReferences(s).equals(jdkParse("<a>" + s + "</a>").getDocumentElement().getTextContent()));
        }
        for (String bad : List.of("&nbsp;", "&#0;", "Tom & Jerry", "&#xFFFE;")) {
            ExerciseChecker.check("decodeReferences(" + bad + ") -> IllegalArgumentException",
                    !messageOf(() -> decodeReferences(bad)).isEmpty());
        }

        ExerciseChecker.check("normalizeAttributeValue(a\\tb) == a b", normalizeAttributeValue("a\tb").equals("a b"));
        ExerciseChecker.check("normalizeAttributeValue(a&#9;b) == a\\tb", normalizeAttributeValue("a&#9;b").equals("a\tb"));
        ExerciseChecker.check("normalizeAttributeValue(x\\r\\r\\ny) == 'x  y'", normalizeAttributeValue("x\r\r\ny").equals("x  y"));
        ExerciseChecker.check("normalizeAttributeValue(a&#13;&#10;b) == a\\r\\nb", normalizeAttributeValue("a&#13;&#10;b").equals("a\r\nb"));
        ExerciseChecker.check("normalizeAttributeValue('  a  b  ') inchange", normalizeAttributeValue("  a  b  ").equals("  a  b  "));

        String file = Fixtures.read("ex02/attributes.xml");
        Document dom = jdkParse(file);
        NodeList entries = dom.getElementsByTagName("entry");
        Matcher m = Pattern.compile("key=\"(\\w+)\" value=([\"'])(.*?)\\2", Pattern.DOTALL).matcher(file);
        int count = 0;
        while (m.find()) {
            Element e = (Element) entries.item(count++);
            String predicted = normalizeAttributeValue(m.group(3));
            ExerciseChecker.check("attributes.xml [" + m.group(1) + "] : prediction == JDK [" + visible(e.getAttribute("value")) + "]",
                    e.getAttribute("key").equals(m.group(1)) && predicted.equals(e.getAttribute("value")));
        }
        ExerciseChecker.check("les 7 <entry> ont ete comparees", count == 7 && entries.getLength() == 7);

        Map<String, String> attrs = new LinkedHashMap<>();
        attrs.put("key", "motd");
        attrs.put("note", "il dit \"oui\"");
        String built = toXmlElement("entry", attrs, "a < b");
        ExerciseChecker.check("toXmlElement avec guillemet malin", built.equals("<entry key=\"motd\" note='il dit \"oui\"'>a &lt; b</entry>"));
        ExerciseChecker.check("toXmlElement : aller-retour JDK",
                jdkParse(built).getDocumentElement().getAttribute("note").equals("il dit \"oui\"")
                        && jdkParse(built).getDocumentElement().getTextContent().equals("a < b"));
        ExerciseChecker.check("toXmlElement(q=\"', null) == <entry q=\"&quot;'\"/>",
                toXmlElement("entry", Map.of("q", "\"'"), null).equals("<entry q=\"&quot;'\"/>"));

        ExerciseChecker.summary();
    }

    // ------------------------------------------------------------------
    // Deja ecrit : outils de test (ne pas modifier)
    // ------------------------------------------------------------------

    private static String messageOf(Runnable action) {
        try {
            action.run();
            return "";
        } catch (IllegalArgumentException e) {
            return e.getMessage() == null ? "IAE" : e.getMessage();
        }
    }

    private static String visible(String s) {
        return s.replace("\t", "\\t").replace("\r", "\\r").replace("\n", "\\n");
    }

    static Document jdkParse(String xml) {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
