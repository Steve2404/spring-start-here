package vorkurs02_xml.solutions;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.validation.SchemaFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Corrige de l'exercice 17. A ne consulter qu'apres avoir essaye par
 * vous-meme dans vorkurs02_xml.exercises.Exercise17_XmlSecurity.
 */
public class Solution17_XmlSecurity {

    public enum Risk { DOCTYPE, EXTERNAL_DTD, INTERNAL_ENTITY, EXTERNAL_ENTITY, PARAMETER_ENTITY }

    public record ImportResult(boolean accepted, int items, String note, Set<Risk> risks) {
    }

    private static final String DISALLOW_DOCTYPE = "http://apache.org/xml/features/disallow-doctype-decl";
    private static final String EXTERNAL_GENERAL = "http://xml.org/sax/features/external-general-entities";
    private static final String EXTERNAL_PARAMETER = "http://xml.org/sax/features/external-parameter-entities";
    private static final String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    public static DocumentBuilder secureDocumentBuilder() throws Exception {
        // Defense en profondeur (0.2.21 S3 / S7) : on INTERDIT le DOCTYPE (refus explicite), et on
        // ferme quand meme les autres portes au cas ou une ligne serait retiree un jour.
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        f.setFeature(DISALLOW_DOCTYPE, true);
        f.setFeature(EXTERNAL_GENERAL, false);
        f.setFeature(EXTERNAL_PARAMETER, false);
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        f.setXIncludeAware(false);
        f.setExpandEntityReferences(false);
        return f.newDocumentBuilder();
    }

    public static DocumentBuilder legacyDocumentBuilder(int maxExpansions) throws Exception {
        // Pour d'anciens fichiers de CONFIANCE qui utilisent des entites INTERNES : DOCTYPE permis,
        // mais rien d'externe (ni entite, ni DTD) et un plafond d'expansions bas (anti billion laughs).
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        f.setFeature(EXTERNAL_GENERAL, false);
        f.setFeature(EXTERNAL_PARAMETER, false);
        f.setFeature(LOAD_EXTERNAL_DTD, false);
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        f.setAttribute("jdk.xml.entityExpansionLimit", String.valueOf(maxExpansions));
        DocumentBuilder builder = f.newDocumentBuilder();
        builder.setErrorHandler(new DefaultHandler());
        return builder;
    }

    public static SAXParser secureSaxParser() throws Exception {
        // Features sur la FACTORY, puis la propriete d'acces externe sur le PARSEUR (cours 0.2.21 S3).
        SAXParserFactory f = SAXParserFactory.newInstance();
        f.setNamespaceAware(true);
        f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        f.setFeature(DISALLOW_DOCTYPE, true);
        SAXParser parser = f.newSAXParser();
        parser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        return parser;
    }

    public static XMLInputFactory secureStaxFactory() {
        // StAX n'a pas "disallow-doctype" : on coupe le support DTD (les entites declarees deviennent
        // "non declarees" -> erreur) et les entites externes.
        XMLInputFactory f = XMLInputFactory.newFactory();
        f.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        f.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        f.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        return f;
    }

    public static SchemaFactory secureSchemaFactory() throws SAXException {
        // Un XSD peut lui aussi aller chercher d'autres fichiers (include/import) : on les interdit.
        SchemaFactory f = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        f.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        f.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return f;
    }

    private static final Pattern DOCTYPE = Pattern.compile("<!DOCTYPE");
    private static final Pattern EXTERNAL_DTD = Pattern.compile("<!DOCTYPE\\s+\\S+\\s+(SYSTEM|PUBLIC)");
    private static final Pattern PARAMETER_ENTITY = Pattern.compile("<!ENTITY\\s+%");
    private static final Pattern EXTERNAL_ENTITY = Pattern.compile("<!ENTITY\\s+(%\\s+)?\\S+\\s+(SYSTEM|PUBLIC)");
    private static final Pattern INTERNAL_ENTITY = Pattern.compile("<!ENTITY\\s+[^%\\s]\\S*\\s+[\"']");

    public static Set<Risk> risks(String xmlText) {
        // Un tri AVANT parsing, sur le texte brut : il ne remplace pas un parseur securise, il sert
        // a EXPLIQUER un refus (les messages d'exception sont traduits et changent de version en version).
        Set<Risk> risks = EnumSet.noneOf(Risk.class);
        if (DOCTYPE.matcher(xmlText).find()) {
            risks.add(Risk.DOCTYPE);
        }
        if (EXTERNAL_DTD.matcher(xmlText).find()) {
            risks.add(Risk.EXTERNAL_DTD);
        }
        if (INTERNAL_ENTITY.matcher(xmlText).find()) {
            risks.add(Risk.INTERNAL_ENTITY);
        }
        if (EXTERNAL_ENTITY.matcher(xmlText).find()) {
            risks.add(Risk.EXTERNAL_ENTITY);
        }
        if (PARAMETER_ENTITY.matcher(xmlText).find()) {
            risks.add(Risk.PARAMETER_ENTITY);
        }
        return risks;
    }

    public static ImportResult importOrder(Path xml) throws Exception {
        // Politique : le chemin strict d'abord ; un repli "legacy" UNIQUEMENT si le seul risque est
        // un DOCTYPE avec des entites internes, et avec un plafond de 1000 expansions.
        Set<Risk> risks = risks(Files.readString(xml));
        Document doc;
        if (risks.isEmpty()) {
            doc = parseOrNull(secureDocumentBuilder(), xml);
        } else if (EnumSet.of(Risk.DOCTYPE, Risk.INTERNAL_ENTITY).containsAll(risks)) {
            doc = parseOrNull(legacyDocumentBuilder(1000), xml);
        } else {
            doc = null;
        }
        if (doc == null) {
            return new ImportResult(false, 0, "", risks);
        }
        return new ImportResult(true, doc.getElementsByTagName("item").getLength(),
                doc.getElementsByTagName("note").item(0).getTextContent(), risks);
    }

    private static Document parseOrNull(DocumentBuilder builder, Path xml) {
        try {
            builder.setErrorHandler(new DefaultHandler());
            return builder.parse(xml.toFile());
        } catch (Exception e) {
            return null;
        }
    }
}
