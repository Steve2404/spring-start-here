package vorkurs02_xml.projects.p02_textgate;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du projet 2 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON TextGate, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "E01 texte=Tom &amp; Jerry | attribut=Tom &amp; Jerry | relu=oui,oui",
            "E02 texte=a &lt; b | attribut=a &lt; b | relu=oui,oui",
            "E03 texte=prix > 3 | attribut=prix > 3 | relu=oui,oui",
            "E04 texte=fin ]]&gt; ici | attribut=fin ]]> ici | relu=oui,oui",
            "E05 texte=il dit \"oui\" | attribut=il dit &quot;oui&quot; | relu=oui,oui",
            "E06 texte=l'été | attribut=l'été | relu=oui,oui",
            "E07 texte=ligne1\\nligne2 | attribut=ligne1&#10;ligne2 | relu=oui,oui",
            "E08 texte=col1\\tcol2 | attribut=col1&#9;col2 | relu=oui,oui",
            "E09 texte=ancien&#13;mac | attribut=ancien&#13;mac | relu=oui,oui",
            "E10 texte=smile 😀 | attribut=smile 😀 | relu=oui,oui",
            "E11 REFUS U+0001",
            "N01 [a\\tb] -> [a b] | parseur [a b]",
            "N02 [un\\r\\ndeux] -> [un deux] | parseur [un deux]",
            "N03 [  deux  espaces  ] -> [  deux  espaces  ] | parseur [  deux  espaces  ]",
            "N04 [garde&#9;tab] -> [garde\\ttab] | parseur [garde\\ttab]",
            "N05 [garde&#10;ligne] -> [garde\\nligne] | parseur [garde\\nligne]",
            "N06 [&amp;lt;] -> [&lt;] | parseur [&lt;]",
            "N07 [x&#x20;y] -> [x y] | parseur [x y]",
            "N08 [vieux\\rmac] -> [vieux mac] | parseur [vieux mac]",
            "T01 [un\\r\\ndeux] -> [un\\ndeux] | parseur [un\\ndeux]",
            "T02 [trois\\rquatre] -> [trois\\nquatre] | parseur [trois\\nquatre]",
            "T03 [  blancs\\tgardes  ] -> [  blancs\\tgardes  ] | parseur [  blancs\\tgardes  ]",
            "T04 [&#13;protege] -> [\\rprotege] | parseur [\\rprotege]",
            "R01 &lt; -> [<] | parseur OK",
            "R02 &#65; -> [A] | parseur OK",
            "R03 &#x41; -> [A] | parseur OK",
            "R04 &#X41; -> REFUS syntaxe | parseur KO 1:6",
            "R05 &#x1F600; -> [😀] | parseur OK",
            "R06 &#0; -> REFUS caractere interdit | parseur KO 1:8",
            "R07 &#xD800; -> REFUS caractere interdit | parseur KO 1:12",
            "R08 &Amp; -> REFUS entite inconnue Amp | parseur KO 1:9",
            "R09 &amp;lt; -> [&lt;] | parseur OK",
            "R10 &eacute; -> REFUS entite inconnue eacute | parseur KO 1:12",
            "X01 [Bonjour, L'equipe ACME & ses amis] | parseur [Bonjour, L'equipe ACME & ses amis]",
            "X02 [premier] | parseur [premier]",
            "X03 [[ABC] <b>&code;</b>fin] | parseur [[ABC] <b>&code;</b>fin]",
            "X04 REFUS RECURSION a -> b -> a | parseur KO 1:11",
            "X05 REFUS LIMITE 100 expansions depassee | parseur accepte, 2000 caracteres",
            "X06 REFUS ENTITE INCONNUE inconnue | parseur KO 4:17",
            "BALISAGE DECLARATION DOCTYPE COMMENT PI START_TAG START_TAG END_TAG START_TAG CDATA END_TAG START_TAG END_TAG",
            "M01 OK | parseur OK",
            "M02 COMMENTAIRE_INVALIDE | parseur KO 1:20",
            "M03 COMMENTAIRE_INVALIDE | parseur KO 1:23",
            "M04 OK | parseur OK",
            "M05 CIBLE_RESERVEE XML | parseur KO 1:9",
            "M06 OK | parseur OK",
            "M07 OK mais DOCTYPE rapport != racine autre | parseur OK",
            "M08 DOCTYPE_DOUBLE | parseur KO 1:22",
            "C01 <![CDATA[if (a < b && c) {}]]> | relu=oui",
            "C02 <![CDATA[fin ]]]]><![CDATA[> milieu ]]]]><![CDATA[> fin]]> | relu=oui");
            // EXPECTED-END

    static final List<String> API = List.of(
            "codePoints()", "appendCodePoint(", "XmlKit.value(", "XmlKit.wellFormed(", "Deque", "putIfAbsent(",
            "!javax.xml", "!org.w3c", "!org.xml", "!XmlKit.select", "!XmlKit.validate", "!XmlKit.wellFormedNs");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "TextGate", args, EXPECTED, API);
    }
}
