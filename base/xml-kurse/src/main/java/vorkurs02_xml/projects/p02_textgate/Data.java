package vorkurs02_xml.projects.p02_textgate;

import java.util.List;

/**
 * Les donnees du projet 2 (ne pas modifier). Consigne : TODO.md.
 * Attention : les sequences Java \t \n \r et \u0001 sont de VRAIS caracteres de controle dans ces textes.
 */
public final class Data {

    private Data() {
    }

    /** Des valeurs metier a ecrire dans un fichier XML, telles quelles (etape 1). */
    public static final List<String> VALUES = List.of(
            "Tom & Jerry",
            "a < b",
            "prix > 3",
            "fin ]]> ici",
            "il dit \"oui\"",
            "l'été",
            "ligne1\nligne2",
            "col1\tcol2",
            "ancien\rmac",
            "smile 😀",
            "bip\u0001");

    /** Des valeurs d'attributs telles qu'ecrites dans le fichier source, entre les guillemets (etape 2). */
    public static final List<String> RAW_ATTRIBUTES = List.of(
            "a\tb",
            "un\r\ndeux",
            "  deux  espaces  ",
            "garde&#9;tab",
            "garde&#10;ligne",
            "&amp;lt;",
            "x&#x20;y",
            "vieux\rmac");

    /** Des contenus d'element tels qu'ecrits dans le fichier source (etape 2). */
    public static final List<String> RAW_TEXTS = List.of(
            "un\r\ndeux",
            "trois\rquatre",
            "  blancs\tgardes  ",
            "&#13;protege");

    /** Des references a decoder (etape 3). */
    public static final List<String> REFERENCES = List.of(
            "&lt;", "&#65;", "&#x41;", "&#X41;", "&#x1F600;", "&#0;", "&#xD800;", "&Amp;", "&amp;lt;", "&eacute;");

    /** Des documents avec des entites internes (etape 4). Le texte voulu est celui de l'element racine. */
    public static final List<String> ENTITY_DOCS = List.of(
            """
                    <!DOCTYPE note [
                      <!ENTITY firme "ACME">
                      <!ENTITY signature "L'equipe &firme;">
                    ]>
                    <note>Bonjour, &signature; &amp; ses amis</note>""",
            """
                    <!DOCTYPE note [
                      <!ENTITY e "premier">
                      <!ENTITY e "second">
                      <!ENTITY % p "parametre">
                      <!ENTITY ext SYSTEM "ext.txt">
                    ]>
                    <note>&e;</note>""",
            """
                    <!DOCTYPE note [
                      <!ENTITY code "&#65;&#66;&#x43;">
                    ]>
                    <note>[&code;] <!-- note interne --><![CDATA[<b>&code;</b>]]><?trace on?>fin</note>""",
            """
                    <!DOCTYPE note [
                      <!ENTITY a "debut &b;">
                      <!ENTITY b "milieu &a;">
                    ]>
                    <note>&a;</note>""",
            """
                    <!DOCTYPE note [
                      <!ENTITY l0 "ha">
                      <!ENTITY l1 "&l0;&l0;&l0;&l0;&l0;&l0;&l0;&l0;&l0;&l0;">
                      <!ENTITY l2 "&l1;&l1;&l1;&l1;&l1;&l1;&l1;&l1;&l1;&l1;">
                      <!ENTITY l3 "&l2;&l2;&l2;&l2;&l2;&l2;&l2;&l2;&l2;&l2;">
                    ]>
                    <note>&l3;</note>""",
            """
                    <!DOCTYPE note [
                      <!ENTITY gras "<b>fort</b>">
                    ]>
                    <note>&inconnue;</note>""");

    /** Un document complet : chaque '<' ouvre une sorte de balisage differente (etape 5). */
    public static final String MARKUP_DOC = """
            <?xml version="1.0"?>
            <!DOCTYPE rapport SYSTEM "rapport.dtd">
            <!-- genere automatiquement -->
            <?xml-stylesheet href="style.css"?>
            <rapport>
              <titre>Bilan</titre>
              <code><![CDATA[if (a < b) { x(); }]]></code>
              <vide/>
            </rapport>
            """;

    /** Des petits documents : un commentaire, une PI ou un DOCTYPE a juger (etape 5). */
    public static final List<String> MARKUP_CASES = List.of(
            "<a><!-- ok - simple --></a>",
            "<a><!-- interdit -- ici --></a>",
            "<a><!-- tiret final ---></a>",
            "<a><?trace niveau=2?></a>",
            "<a><?XML version?></a>",
            "<a><?xml-stylesheet href='s.css'?></a>",
            "<!DOCTYPE rapport><autre/>",
            "<!DOCTYPE a><!DOCTYPE a><a/>");

    /** Des textes a proteger dans une section CDATA (etape 6). */
    public static final List<String> CDATA_TEXTS = List.of(
            "if (a < b && c) {}",
            "fin ]]> milieu ]]> fin");
}
