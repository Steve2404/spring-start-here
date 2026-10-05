package vorkurs02_xml.projects.p12_security.solution;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import vorkurs02_xml.projects.p12_security.Data;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Corrige du projet 12 : la politique d'import sure (0.2.21). Chaque API de lecture est configuree
 * pour refuser ce dont l'import n'a pas besoin ; un chemin "ancien format" est permis, mais borne.
 */
public class SafeImport {

    enum Risk { DOCTYPE, EXTERNAL_DTD, INTERNAL_ENTITY, EXTERNAL_ENTITY, PARAMETER_ENTITY }

    static final List<String> FILES = List.of(
            "clean.xml", "legacy-internal.xml", "external-dtd.xml", "param-entity.xml", "lol-small.xml", "lol-big.xml");

    private static final String DISALLOW_DOCTYPE = "http://apache.org/xml/features/disallow-doctype-decl";
    private static final String EXTERNAL_GENERAL = "http://xml.org/sax/features/external-general-entities";
    private static final String EXTERNAL_PARAMETER = "http://xml.org/sax/features/external-parameter-entities";
    private static final String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    public static void main(String[] args) throws Exception {
        for (String f : FILES) {
            System.out.println("RISQUES " + f + " : " + risks(Files.readString(Data.file(f))));
        }
        for (String f : FILES) {
            System.out.println("STRICT " + f + " : DOM " + dom(f) + " | SAX " + sax(f) + " | STAX " + stax(f));
        }
        for (String f : List.of("legacy-internal.xml", "lol-small.xml", "lol-big.xml")) {
            System.out.println("ANCIEN " + f + " : " + legacy(f));
        }
        for (String f : FILES) {
            System.out.println("IMPORT " + f + " : " + importOrder(f));
        }
        System.out.println("SCHEMA main.xsd : " + schema());
    }

    // ---------- Etape 1 : le tri sur le texte brut ----------

    private static final Pattern DOCTYPE = Pattern.compile("<!DOCTYPE");
    private static final Pattern EXTERNAL_DTD = Pattern.compile("<!DOCTYPE\\s+\\S+\\s+(SYSTEM|PUBLIC)");
    private static final Pattern PARAMETER_ENTITY = Pattern.compile("<!ENTITY\\s+%");
    private static final Pattern EXTERNAL_ENTITY = Pattern.compile("<!ENTITY\\s+(%\\s+)?\\S+\\s+(SYSTEM|PUBLIC)");
    private static final Pattern INTERNAL_ENTITY = Pattern.compile("<!ENTITY\\s+[^%\\s]\\S*\\s+[\"']");

    static Set<Risk> risks(String text) {
        // Ce tri ne REMPLACE pas un parseur configure : il sert a EXPLIQUER un refus,
        // car les messages d'exception changent selon la langue et la version.
        Set<Risk> risks = EnumSet.noneOf(Risk.class);
        if (DOCTYPE.matcher(text).find()) {
            risks.add(Risk.DOCTYPE);
        }
        if (EXTERNAL_DTD.matcher(text).find()) {
            risks.add(Risk.EXTERNAL_DTD);
        }
        if (INTERNAL_ENTITY.matcher(text).find()) {
            risks.add(Risk.INTERNAL_ENTITY);
        }
        if (EXTERNAL_ENTITY.matcher(text).find()) {
            risks.add(Risk.EXTERNAL_ENTITY);
        }
        if (PARAMETER_ENTITY.matcher(text).find()) {
            risks.add(Risk.PARAMETER_ENTITY);
        }
        return risks;
    }

    // ---------- Etape 2 : trois lecteurs stricts ----------

    static DocumentBuilder strictDom() throws Exception {
        // Defense en profondeur : on interdit le DOCTYPE, ET on ferme les autres portes au cas ou.
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        f.setFeature(DISALLOW_DOCTYPE, true);
        f.setFeature(EXTERNAL_GENERAL, false);
        f.setFeature(EXTERNAL_PARAMETER, false);
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        f.setXIncludeAware(false);
        f.setExpandEntityReferences(false);
        DocumentBuilder b = f.newDocumentBuilder();
        b.setErrorHandler(new DefaultHandler()); // pas de "[Fatal Error]" sur la console
        return b;
    }

    static String dom(String file) throws Exception {
        try {
            Document d = strictDom().parse(Data.file(file).toFile());
            return "accepte (" + d.getElementsByTagName("item").getLength() + " item)";
        } catch (SAXParseException e) {
            return "refuse ligne " + e.getLineNumber();
        }
    }

    static String sax(String file) throws Exception {
        // Les fonctionnalites vont sur la FABRIQUE ; la propriete d'acces externe, sur le PARSEUR.
        SAXParserFactory f = SAXParserFactory.newInstance();
        f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        f.setFeature(DISALLOW_DOCTYPE, true);
        SAXParser p = f.newSAXParser();
        p.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        try {
            p.parse(Data.file(file).toFile(), new DefaultHandler());
            return "accepte";
        } catch (SAXParseException e) {
            return "refuse ligne " + e.getLineNumber();
        }
    }

    static String stax(String file) throws Exception {
        // StAX n'a pas "disallow-doctype" : il IGNORE la DTD. Un DOCTYPE inutilise passe donc,
        // mais une entite declaree dedans devient "non declaree" et la lecture echoue.
        XMLInputFactory f = XMLInputFactory.newFactory();
        f.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        f.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        f.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        try (InputStream in = Files.newInputStream(Data.file(file))) {
            XMLStreamReader r = f.createXMLStreamReader(in);
            try {
                while (r.hasNext()) {
                    r.next();
                }
                return "accepte";
            } finally {
                r.close();
            }
        } catch (XMLStreamException e) {
            return "refuse";
        }
    }

    // ---------- Etape 3 : le chemin "ancien format", borne ----------

    static DocumentBuilder legacyDom(int maxExpansions) throws Exception {
        // Pour d'anciens fichiers de confiance a entites INTERNES : DOCTYPE permis, rien d'externe,
        // et un plafond d'expansions bas contre l'explosion d'entites.
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        f.setFeature(EXTERNAL_GENERAL, false);
        f.setFeature(EXTERNAL_PARAMETER, false);
        f.setFeature(LOAD_EXTERNAL_DTD, false);
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        f.setAttribute("jdk.xml.entityExpansionLimit", String.valueOf(maxExpansions));
        DocumentBuilder b = f.newDocumentBuilder();
        b.setErrorHandler(new DefaultHandler());
        return b;
    }

    static String legacy(String file) throws Exception {
        try {
            Document d = legacyDom(1000).parse(Data.file(file).toFile());
            String note = d.getElementsByTagName("note").item(0).getTextContent();
            return "accepte, note de " + note.length() + " caracteres : " + (note.length() > 40 ? note.substring(0, 40) + "..." : note);
        } catch (SAXParseException e) {
            return "refuse (limite d'expansions)";
        }
    }

    // ---------- Etape 4 : la politique ----------

    static String importOrder(String file) throws Exception {
        // Le chemin strict d'abord ; l'ancien chemin SEULEMENT si les seuls risques sont
        // un DOCTYPE et des entites internes.
        Set<Risk> risks = risks(Files.readString(Data.file(file)));
        DocumentBuilder builder;
        if (risks.isEmpty()) {
            builder = strictDom();
        } else if (EnumSet.of(Risk.DOCTYPE, Risk.INTERNAL_ENTITY).containsAll(risks)) {
            builder = legacyDom(1000);
        } else {
            return "REFUSE " + risks;
        }
        try {
            Document d = builder.parse(Data.file(file).toFile());
            return "ACCEPTE " + d.getElementsByTagName("item").getLength() + " item, note="
                    + d.getElementsByTagName("note").item(0).getTextContent();
        } catch (SAXParseException e) {
            return "REFUSE au parsing " + risks;
        }
    }

    // ---------- Etape 5 : un schema ne va pas chercher d'autres fichiers ----------

    static String schema() {
        try {
            SchemaFactory f = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            f.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            f.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            f.setErrorHandler(null);
            f.newSchema(new StreamSource(Data.file("main.xsd").toFile()));
            return "accepte";
        } catch (SAXException e) {
            // xs:include demande un autre fichier : l'acces externe est ferme, le schema est refuse.
            return "refuse (" + e.getClass().getSimpleName() + ")";
        }
    }
}
