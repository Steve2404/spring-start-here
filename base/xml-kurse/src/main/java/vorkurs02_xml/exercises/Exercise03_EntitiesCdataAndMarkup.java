package vorkurs02_xml.exercises;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import vorkurs02_xml.ExerciseChecker;
import vorkurs02_xml.Fixtures;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EXERCICE 3 - Entites, CDATA, commentaires et PI : rejouer le parseur a la main (niveau : challenge)
 * =================================================================================================
 *
 * Rappel express du decoupage en "boites magiques" : voir Exercise01_WellFormednessDiagnoser.java.
 *
 * -- Le contexte --
 *
 * fixtures/ex03/newsletter.xml declare des entites dans son DOCTYPE
 * (internal subset) et ses <p> melangent texte, references, CDATA,
 * commentaires et processing instructions. Ton but final (TODO 7) :
 * calculer, SANS parseur, le texte exact que DOM rendra pour chaque <p>
 * avec getTextContent(). Pour y arriver, tu construis les briques du
 * cours 0.2.7 / 0.2.8 : lookahead apres '<', CDATA, cible de PI,
 * declarations d'entites, texte de remplacement, expansion recursive
 * avec detection de cycle et limite anti-"billion laughs".
 *
 * Faits verifies avec le parseur du JDK 17 (a garder en tete) :
 *   - si une entite est declaree 2 fois, la PREMIERE declaration gagne ;
 *   - "<!ENTITY e '&#60;b&#62;'>" puis &e; -> ERREUR : les references de
 *     caracteres sont remplacees des la declaration, le texte "<b>" est
 *     ensuite relu comme du balisage (et <b> n'est jamais ferme) ;
 *   - "<!ENTITY e '&#38;#60;'>" puis &e; -> "<" (double echappement) ;
 *   - e1 -> e2 -> e1 : "Recursive entity reference ... e1 -> e2 -> e1" ;
 *   - 5 niveaux de 10 references (lol5) : plus de 64000 expansions ->
 *     "JAXP00010001 ... limit imposed by the JDK". 4 niveaux passent
 *     (30000 caracteres).
 *
 *
 * ==================================================================
 * TODO 1 : classifyMarkup(doc, lt)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Apres un '<', le parseur est comme un facteur devant une porte : il
 * regarde les 2 a 9 caracteres suivants pour savoir QUI habite la.
 * "<!--" commentaire, "<![CDATA[" CDATA, "<!DOCTYPE" doctype, "<?" PI
 * (ou DECLARATION si c'est "<?xml" + blanc tout au debut, position 0),
 * "</" balise de fin, "<" + lettre/_/: balise de debut. Tout le reste
 * (ex. "<!foo", "< a", "<1") : INVALID.
 *
 * -- Essayons a la main --
 *
 *   ("<?xml version='1.0'?><a/>", 0)  -> DECLARATION
 *   ("<a><?xml x?></a>", 3)           -> PI  (pas en position 0)
 *   ("<!---->", 0)                    -> COMMENT
 *   ("<!foo>", 0)                     -> INVALID
 *
 * -- Le plan --
 *
 *   1. Tester les prefixes du plus long/specifique au plus general.
 *   2. Pour "<?", distinguer DECLARATION (lt == 0, "<?xml" + blanc) et PI.
 *   3. "</" ; puis lettre/_/: ; sinon INVALID.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non, une suite de if. L'ORDRE des tests est tout l'exercice.
 *
 *
 * ==================================================================
 * TODO 2 : toCdata(text)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Une section CDATA est un sac transparent : tout y est pris a la lettre
 * (< et & compris)... sauf "]]>", qui FERME le sac. Si ton texte le
 * contient, tu fermes le sac entre "]]" et ">", puis tu en ouvres un
 * autre. Le parseur recolle les morceaux.
 *
 * -- Essayons a la main --
 *
 *   "if (x]]>y)" -> "<![CDATA[if (x]]]]><![CDATA[>y)]]>"
 *   ""           -> "<![CDATA[]]>"
 *
 * -- Le plan --
 *
 *   1. Remplacer chaque "]]>" par "]]" + fin de CDATA + debut de CDATA + ">".
 *   2. Entourer du debut et de la fin.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non : une ligne. (Mais trouve-la toi-meme sur papier d'abord.)
 *
 *
 * ==================================================================
 * TODO 3 : isValidPiTarget(target)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * La cible d'une PI est un nom (version ASCII simplifiee ici :
 * lettre, '_' ou ':' puis lettres, chiffres, . _ : -). Le nom "xml",
 * quelle que soit la casse, est reserve a la declaration ("[xX][mM][lL]
 * n'est pas permis", dit le parseur). "xml-stylesheet" est permis.
 *
 * -- Essayons a la main --
 *
 *   render, xml-stylesheet, _a:b -> true ; xml, XmL, 1x, "" -> false
 *
 * -- Le plan --
 *
 *   1. Le nom respecte la forme ; 2. et n'est pas "xml" (casse ignoree).
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non.
 *
 *
 * ==================================================================
 * TODO 4 : parseEntityDeclarations(internalSubset)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Le DOCTYPE est un dictionnaire de raccourcis : <!ENTITY year "2026">.
 * Tu dois le lire et rendre la Map nom -> valeur ECRITE (brute, sans
 * rien decoder), dans l'ordre du fichier. Trois pieges :
 *   - un nom declare 2 fois : garder la PREMIERE valeur ;
 *   - "<!ENTITY % nom ...>" est une entite PARAMETRE (pour la DTD
 *     elle-meme, jamais pour le texte) : l'ignorer ;
 *   - "<!ENTITY nom SYSTEM 'fichier'>" est EXTERNE : l'ignorer ici
 *     (l'exercice 17 montrera pourquoi c'est dangereux).
 * Les valeurs peuvent etre entre " ou entre '.
 *
 * -- Essayons a la main --
 *
 *   subset de newsletter.xml ->
 *   {company="Acme &amp; Fils", copy="&#169;", year="2026",
 *    footer="&copy; &year; &company;", signature="&#38;#60;L&#233;a&#38;#62;"}
 *   (year vaut 2026, pas 1999 ; "internal" n'y est pas)
 *
 * -- Le plan --
 *
 *   1. Trouver chaque "<!ENTITY" avec une expression reguliere.
 *   2. Sauter les parametres et les externes.
 *   3. Ajouter seulement si le nom est absent.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non, mais ecris la regex en deux temps sur papier (voir indices).
 *
 *
 * ==================================================================
 * TODO 5 : replacementText(literal)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Quand le parseur LIT une declaration, il remplace tout de suite les
 * references de CARACTERES (&#169; &#x263A;) mais laisse les references
 * d'ENTITES (&amp; &year;) pour plus tard. C'est ce "texte de
 * remplacement" qui sera recopie a chaque &nom;.
 *
 * -- Essayons a la main --
 *
 *   "&#169;"                        -> "(c)" (le caractere copyright)
 *   "Acme &amp; Fils"               -> "Acme &amp; Fils" (inchange)
 *   "&#38;#60;L&#233;a&#38;#62;"    -> "&#60;Lea&#62;" (e accent ; &#38; = '&')
 *
 * -- Le plan --
 *
 *   1. Remplacer chaque &#decimal; et &#xhexa; par son caractere.
 *   2. Ne pas toucher aux autres '&'.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Non : Matcher.appendReplacement fait la boucle.
 *
 *
 * ==================================================================
 * TODO 6 : expand(decls, content, maxExpansions)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Tu remplaces chaque raccourci par son texte, et si ce texte contient
 * encore des raccourcis, tu recommences (comme des poupees russes).
 * Trois gardes-fous :
 *   - une poupee qui se contient elle-meme (e1 -> e2 -> e1) : boucle
 *     infinie -> IllegalStateException dont le message contient le
 *     chemin "e1 -> e2 -> e1" ;
 *   - trop de poupees au total (> maxExpansions references d'ENTITES
 *     DECLAREES developpees, les 5 predefinies et les &#..; ne comptent
 *     pas) -> IllegalStateException contenant "Limite" ;
 *   - un raccourci inconnu -> IllegalArgumentException("Entite non declaree : zz") ;
 *   - un texte de remplacement qui contient '<' -> IllegalStateException
 *     (dans cet exercice, les entites ne transportent que du texte).
 *
 * -- Essayons a la main --
 *
 *   expand(newsletter, "&footer;", 100)
 *     footer -> "&copy; &year; &company;"          (1 expansion)
 *       copy -> "(c)" ; year -> "2026" ; company -> "Acme &amp; Fils"  (3 de plus)
 *       &amp; -> "&" (predefinie, ne compte pas)
 *     -> "(c) 2026 Acme & Fils", 4 expansions
 *
 *   lol0 = "lol", lol1 = 10 x "&lol0;", ..., lol4 = 10 x "&lol3;"
 *   &lol4; -> 1 + 10 + 100 + 1000 + 10000 = 11111 expansions, 30000 caracteres
 *   -> maxExpansions = 11111 passe, 11110 leve l'exception.
 *
 * -- Le plan --
 *
 *   1. Parcourir le texte ; recopier ce qui n'est pas '&'.
 *   2. Sur "&nom;" : predefinie -> son caractere ; "#..." -> TODO 5 ;
 *      sinon : cycle ? compteur ? declaree ? texte de remplacement (TODO 5)
 *      avec '<' ? puis developper CE texte recursivement.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui : la recursion a besoin de 3 choses en plus de ta signature :
 * le StringBuilder de sortie, le CHEMIN des entites ouvertes (Deque)
 * et un compteur PARTAGE par tous les niveaux (un int[1] suffit).
 * Ecris expandInto(out, decls, text, path, count, max).
 *
 *
 * ==================================================================
 * TODO 7 : textContent(elementContent, decls)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * elementContent est ce qui est ECRIT entre <p> et </p>. Predis
 * getTextContent() : les commentaires et les PI disparaissent, le
 * contenu d'une CDATA est recopie tel quel (meme "&amp;"), le texte
 * normal est developpe (TODO 6, limite 64000 comme le JDK). Une balise
 * enfant -> IllegalArgumentException (hors perimetre).
 *
 * -- Essayons a la main --
 *
 *   "<!-- note --><?render bold?>Code : <![CDATA[a < b]]> fin"
 *     commentaire -> rien ; PI -> rien ; "Code : " ; "a < b" ; " fin"
 *     -> "Code : a < b fin"
 *
 * -- Le plan --
 *
 *   1. Tant qu'il reste du contenu : developper le texte jusqu'au '<'.
 *   2. Au '<' : classifyMarkup -> sauter commentaire / PI, recopier CDATA,
 *      sinon exception.
 *
 * -- Ce plan a-t-il besoin d'une boite magique separee ? --
 *
 * Oui : TODO 1 et TODO 6.
 *
 *
 * Exemple a verifier :
 *
 *   classifyMarkup : 9 cas, dont DECLARATION seulement en position 0
 *   toCdata : 2 cas exacts + aller-retour JDK sur 3 textes
 *   isValidPiTarget : 3 true, 4 false
 *   parseEntityDeclarations(subset de newsletter.xml) == la Map du TODO 4 (ordre compris)
 *   replacementText : les 3 cas du TODO 5
 *   expand : footer, forward reference, cycle "e1 -> e2 -> e1", entite inconnue,
 *            balisage interdit, lol4 = 30000 caracteres (comme le JDK), limite 11111/11110
 *   textContent de chacun des 5 <p> == getTextContent() du JDK
 *
 *
 * Indices techniques Java (a lire seulement si le plan a la main est
 * clair mais que la traduction en code bloque) :
 *
 *   - doc.startsWith("<![CDATA[", lt)
 *   - text.replace("]]>", "]]]]><![CDATA[>")
 *   - target.matches("[A-Za-z_:][A-Za-z0-9._:-]*") && !target.equalsIgnoreCase("xml")
 *   - Pattern.compile("<!ENTITY\\s+(%\\s+)?(\\S+)\\s+(?:([\"'])(.*?)\\3|(?:SYSTEM|PUBLIC)[^>]*)\\s*>", Pattern.DOTALL)
 *     groupe 1 = "%" si parametre, groupe 4 = valeur si interne (null si externe)
 *   - map.putIfAbsent(nom, valeur) : la 1re declaration gagne
 *   - Matcher m = Pattern.compile("&#(x[0-9a-fA-F]+|[0-9]+);").matcher(s);
 *     while (m.find()) m.appendReplacement(sb, Matcher.quoteReplacement(...)); m.appendTail(sb);
 *   - new String(Character.toChars(codePoint))
 *   - Deque<String> path = new ArrayDeque<>(); path.addLast(n); ...; path.removeLast();
 *     String.join(" -> ", path) + " -> " + n
 *   - int[] count = new int[1]; if (++count[0] > max) ...
 */
public class Exercise03_EntitiesCdataAndMarkup {

    public enum Markup { DECLARATION, COMMENT, CDATA, DOCTYPE, PI, END_TAG, START_TAG, INVALID }

    public static Markup classifyMarkup(String doc, int lt) {
        throw new UnsupportedOperationException("TODO 1 : implementer classifyMarkup()");
    }

    public static String toCdata(String text) {
        throw new UnsupportedOperationException("TODO 2 : implementer toCdata()");
    }

    public static boolean isValidPiTarget(String target) {
        throw new UnsupportedOperationException("TODO 3 : implementer isValidPiTarget()");
    }

    public static Map<String, String> parseEntityDeclarations(String internalSubset) {
        throw new UnsupportedOperationException("TODO 4 : implementer parseEntityDeclarations()");
    }

    public static String replacementText(String literal) {
        throw new UnsupportedOperationException("TODO 5 : implementer replacementText()");
    }

    public static String expand(Map<String, String> decls, String content, int maxExpansions) {
        throw new UnsupportedOperationException("TODO 6 : implementer expand()");
    }

    public static String textContent(String elementContent, Map<String, String> decls) {
        throw new UnsupportedOperationException("TODO 7 : implementer textContent()");
    }

    public static void main(String[] args) {
        ExerciseChecker.check("classifyMarkup : DECLARATION en position 0",
                classifyMarkup("<?xml version='1.0'?><a/>", 0) == Markup.DECLARATION);
        ExerciseChecker.check("classifyMarkup : <?xml ailleurs -> PI", classifyMarkup("<a><?xml x?></a>", 3) == Markup.PI);
        ExerciseChecker.check("classifyMarkup : <?xml-stylesheet en 0 -> PI", classifyMarkup("<?xml-stylesheet href='a'?>", 0) == Markup.PI);
        ExerciseChecker.check("classifyMarkup : <!----> -> COMMENT", classifyMarkup("<!---->", 0) == Markup.COMMENT);
        ExerciseChecker.check("classifyMarkup : CDATA", classifyMarkup("x<![CDATA[a]]>", 1) == Markup.CDATA);
        ExerciseChecker.check("classifyMarkup : DOCTYPE", classifyMarkup("<!DOCTYPE a>", 0) == Markup.DOCTYPE);
        ExerciseChecker.check("classifyMarkup : END_TAG", classifyMarkup("</a>", 0) == Markup.END_TAG);
        ExerciseChecker.check("classifyMarkup : START_TAG", classifyMarkup("<_a/>", 0) == Markup.START_TAG);
        ExerciseChecker.check("classifyMarkup : <!foo, < a, <1 -> INVALID",
                classifyMarkup("<!foo>", 0) == Markup.INVALID && classifyMarkup("< a/>", 0) == Markup.INVALID
                        && classifyMarkup("<1/>", 0) == Markup.INVALID);

        ExerciseChecker.check("toCdata(if (x]]>y))", toCdata("if (x]]>y)").equals("<![CDATA[if (x]]]]><![CDATA[>y)]]>"));
        ExerciseChecker.check("toCdata(\"\")", toCdata("").equals("<![CDATA[]]>"));
        for (String s : List.of("if (x]]>y)", "]]>]]>", "<b>&amp;</b> ]] >")) {
            ExerciseChecker.check("aller-retour JDK de toCdata(" + s + ")",
                    jdkParse("<a>" + toCdata(s) + "</a>").getDocumentElement().getTextContent().equals(s));
        }

        ExerciseChecker.check("isValidPiTarget : render, xml-stylesheet, _a:b -> true",
                isValidPiTarget("render") && isValidPiTarget("xml-stylesheet") && isValidPiTarget("_a:b"));
        ExerciseChecker.check("isValidPiTarget : xml, XmL, 1x, \"\" -> false",
                !isValidPiTarget("xml") && !isValidPiTarget("XmL") && !isValidPiTarget("1x") && !isValidPiTarget(""));

        String file = Fixtures.read("ex03/newsletter.xml");
        Matcher subsetMatcher = Pattern.compile("<!DOCTYPE\\s+\\w+\\s*\\[(.*?)\\]>", Pattern.DOTALL).matcher(file);
        subsetMatcher.find();
        Map<String, String> decls = parseEntityDeclarations(subsetMatcher.group(1));
        ExerciseChecker.check("parseEntityDeclarations : 5 entites, dans l'ordre, year = 2026 (la 1re gagne)",
                List.copyOf(decls.keySet()).equals(List.of("company", "copy", "year", "footer", "signature"))
                        && decls.get("year").equals("2026"));
        ExerciseChecker.check("parseEntityDeclarations : valeurs BRUTES",
                decls.get("company").equals("Acme &amp; Fils") && decls.get("footer").equals("&copy; &year; &company;")
                        && decls.get("signature").equals("&#38;#60;L&#233;a&#38;#62;"));
        ExerciseChecker.check("parseEntityDeclarations : SYSTEM ignore",
                parseEntityDeclarations("<!ENTITY ext SYSTEM 'secret.txt'><!ENTITY ok \"x\">").equals(Map.of("ok", "x")));

        ExerciseChecker.check("replacementText(&#169;) == (c)", replacementText("&#169;").equals("©"));
        ExerciseChecker.check("replacementText(Acme &amp; Fils) inchange", replacementText("Acme &amp; Fils").equals("Acme &amp; Fils"));
        ExerciseChecker.check("replacementText(signature) == &#60;Lea&#62;",
                replacementText("&#38;#60;L&#233;a&#38;#62;").equals("&#60;Léa&#62;"));

        ExerciseChecker.check("expand(&footer;) == (c) 2026 Acme & Fils", expand(decls, "&footer;", 100).equals("© 2026 Acme & Fils"));
        ExerciseChecker.check("expand(&footer;) tient en 4 expansions", expand(decls, "&footer;", 4).length() == 18);
        ExerciseChecker.check("expand : reference vers une entite declaree PLUS LOIN -> F",
                expand(Map.of("e", "&f;", "f", "F"), "&e;", 10).equals("F"));
        ExerciseChecker.check("expand : cycle -> message avec e1 -> e2 -> e1",
                errorOf(() -> expand(Map.of("e1", "&e2;", "e2", "&e1;"), "x&e1;", 100)).contains("e1 -> e2 -> e1"));
        ExerciseChecker.check("expand : entite inconnue -> IllegalArgumentException",
                errorOf(() -> expand(Map.of("e", "&zz;"), "&e;", 10)).startsWith("IllegalArgumentException"));
        ExerciseChecker.check("expand : '&#60;b&#62;' -> IllegalStateException (balisage)",
                errorOf(() -> expand(Map.of("e", "&#60;b&#62;"), "&e;", 10)).startsWith("IllegalStateException"));

        Map<String, String> lol = new java.util.HashMap<>(Map.of("lol0", "lol"));
        StringBuilder dtd = new StringBuilder("<!DOCTYPE a [<!ENTITY lol0 'lol'>");
        for (int i = 1; i <= 4; i++) {
            String v = ("&lol" + (i - 1) + ";").repeat(10);
            lol.put("lol" + i, v);
            dtd.append("<!ENTITY lol").append(i).append(" '").append(v).append("'>");
        }
        String lol4 = expand(lol, "&lol4;", 11_111);
        ExerciseChecker.check("expand(&lol4;, 11111) == 30000 caracteres, comme le JDK",
                lol4.length() == 30_000 && lol4.equals(jdkParse(dtd + "]><a>&lol4;</a>").getDocumentElement().getTextContent()));
        ExerciseChecker.check("expand(&lol4;, 11110) -> Limite",
                errorOf(() -> expand(lol, "&lol4;", 11_110)).contains("Limite"));

        Document dom = jdkParse(file);
        NodeList ps = dom.getElementsByTagName("p");
        Matcher p = Pattern.compile("<p>(.*?)</p>", Pattern.DOTALL).matcher(file);
        int i = 0;
        while (p.find()) {
            String expected = ps.item(i++).getTextContent();
            ExerciseChecker.check("<p> n." + i + " : textContent == JDK [" + expected + "]", textContent(p.group(1), decls).equals(expected));
        }
        ExerciseChecker.check("5 <p> compares", i == 5);
        ExerciseChecker.check("textContent avec balise enfant -> IllegalArgumentException",
                errorOf(() -> textContent("a <b>x</b>", decls)).startsWith("IllegalArgumentException"));

        ExerciseChecker.summary();
    }

    // ------------------------------------------------------------------
    // Deja ecrit : outils de test (ne pas modifier)
    // ------------------------------------------------------------------

    private static String errorOf(Runnable action) {
        try {
            action.run();
            return "";
        } catch (RuntimeException e) {
            return e.getClass().getSimpleName() + ": " + e.getMessage();
        }
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
