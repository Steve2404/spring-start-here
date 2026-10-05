package xmlkit;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * OUTIL FOURNI (ne pas modifier) : un vrai processeur XML, utilise comme une BOITE NOIRE.
 *
 * Jusqu'a la section 0.2.16, le cours ne montre encore aucune API Java pour XML : tu ecris
 * toi-meme tes outils (diagnostic, normalisation, arbre, DTD, namespaces). Ce kit sert
 * d'ARBITRE (que dit un vrai parseur ?) et fait ce qu'on ne code pas a la main
 * (valider contre un XSD, evaluer un XPath). A partir de 0.2.17, tu utilises les vraies
 * API (DOM, SAX, StAX...) et le kit est interdit dans les projets.
 *
 * Toutes les methodes lisent un document donne sous forme de texte. Les entites externes
 * et les DTD externes ne sont JAMAIS chargees depuis le disque ou le reseau (sauf ce que
 * tu passes explicitement a validateDtd).
 */
public final class XmlKit {

    private XmlKit() {
    }

    /**
     * Le document est-il bien forme (XML 1.0, sans les regles des namespaces) ?
     *
     * @return "OK", ou "KO ligne:colonne" (la position ou le parseur S'EST APERCU de l'erreur)
     */
    public static String wellFormed(String xml) {
        return parseSax(xml, false);
    }

    /** Comme wellFormed, mais avec en plus les contraintes des namespaces (prefixe declare, xmlns...). */
    public static String wellFormedNs(String xml) {
        return parseSax(xml, true);
    }

    private static String parseSax(String xml, boolean namespaces) {
        try {
            SAXParserFactory f = SAXParserFactory.newInstance();
            f.setNamespaceAware(namespaces);
            f.setFeature("http://xml.org/sax/features/external-general-entities", false);
            f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            f.newSAXParser().parse(new InputSource(new StringReader(xml)), new DefaultHandler());
            return "OK";
        } catch (SAXParseException e) {
            return "KO " + e.getLineNumber() + ":" + e.getColumnNumber();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Valide le document contre sa DTD (DOCTYPE interne et/ou externe).
     *
     * @param externals les DTD externes autorisees : nom du fichier (ex. "library.dtd") vers son texte.
     *                  Un SYSTEM qui designe un autre fichier provoque une erreur.
     * @return "VALID", "INVALID n (ligne l)" avec n le nombre d'erreurs de validite et l la ligne
     *         de la premiere, ou "NOT_WELL_FORMED ligne:colonne"
     */
    public static String validateDtd(String xml, Map<String, String> externals) {
        List<Integer> errors = new ArrayList<>();
        try {
            SAXParserFactory f = SAXParserFactory.newInstance();
            f.setValidating(true);
            var parser = f.newSAXParser();
            parser.parse(new InputSource(new StringReader(xml)), new DefaultHandler() {
                @Override
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
                    String name = systemId == null ? "" : systemId.substring(systemId.lastIndexOf('/') + 1);
                    String text = externals.get(name);
                    if (text == null) {
                        throw new SAXException("ressource externe refusee : " + systemId);
                    }
                    return new InputSource(new StringReader(text));
                }

                @Override
                public void error(SAXParseException e) {
                    errors.add(e.getLineNumber());
                }
            });
        } catch (SAXParseException e) {
            return "NOT_WELL_FORMED " + e.getLineNumber() + ":" + e.getColumnNumber();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return errors.isEmpty() ? "VALID" : "INVALID " + errors.size() + " (ligne " + errors.get(0) + ")";
    }

    /**
     * Evalue une expression XPath 1.0 et rend sa valeur en TEXTE (comme la fonction string()).
     * Le document est lu par un parseur namespace-aware ; les entites internes sont developpees.
     *
     * @param prefixes les prefixes utilisables dans l'expression (prefixe vers URI), ou Map.of()
     */
    public static String value(String xml, String xpath, Map<String, String> prefixes) {
        try {
            return xpath(prefixes).evaluate(xpath, parse(xml));
        } catch (XPathExpressionException e) {
            throw new IllegalArgumentException("XPath invalide : " + xpath, e);
        }
    }

    public static String value(String xml, String xpath) {
        return value(xml, xpath, Map.of());
    }

    /**
     * Evalue une expression XPath 1.0 qui rend des NOEUDS, dans l'ordre du document. Chaque noeud est rendu ainsi :
     * element = son nom tel qu'ecrit (ex. "e:item") ; attribut = "@nom=valeur" ; texte = le texte entre guillemets ;
     * commentaire = "<!--texte-->" ; processing instruction = "<?cible donnees?>" ; document = "/".
     */
    public static List<String> select(String xml, String xpath, Map<String, String> prefixes) {
        try {
            NodeList nodes = (NodeList) xpath(prefixes).evaluate(xpath, parse(xml), XPathConstants.NODESET);
            List<String> out = new ArrayList<>();
            for (int i = 0; i < nodes.getLength(); i++) {
                out.add(render(nodes.item(i)));
            }
            return out;
        } catch (XPathExpressionException e) {
            throw new IllegalArgumentException("XPath invalide (ou ne rend pas des noeuds) : " + xpath, e);
        }
    }

    public static List<String> select(String xml, String xpath) {
        return select(xml, xpath, Map.of());
    }

    private static String render(Node n) {
        return switch (n.getNodeType()) {
            case Node.ELEMENT_NODE -> n.getNodeName();
            case Node.ATTRIBUTE_NODE -> "@" + n.getNodeName() + "=" + ((Attr) n).getValue();
            case Node.TEXT_NODE, Node.CDATA_SECTION_NODE -> "\"" + n.getNodeValue() + "\"";
            case Node.COMMENT_NODE -> "<!--" + n.getNodeValue() + "-->";
            case Node.PROCESSING_INSTRUCTION_NODE -> "<?" + n.getNodeName() + " " + n.getNodeValue() + "?>";
            case Node.DOCUMENT_NODE -> "/";
            default -> n.getNodeName();
        };
    }

    /**
     * Valide un document contre un schema XSD.
     *
     * @return la liste des erreurs, une par entree, au format "ligne code" (ex. "4 cvc-pattern-valid") ;
     *         vide si le document est valide. Si le SCHEMA lui-meme est faux : une seule entree
     *         "SCHEMA ligne code". Si le document n'est pas bien forme : "NOT_WELL_FORMED ligne".
     */
    public static List<String> validateXsd(Path xsd, String xml) {
        Schema schema;
        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            sf.setErrorHandler(null);
            schema = sf.newSchema(new StreamSource(xsd.toFile()));
        } catch (SAXParseException e) {
            return List.of("SCHEMA " + e.getLineNumber() + " " + code(e));
        } catch (SAXException e) {
            return List.of("SCHEMA 0 " + code(e));
        }
        List<String> errors = new ArrayList<>();
        try {
            Validator v = schema.newValidator();
            v.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException e) {
                }

                @Override
                public void error(SAXParseException e) {
                    errors.add(e.getLineNumber() + " " + code(e));
                }

                @Override
                public void fatalError(SAXParseException e) throws SAXException {
                    throw e;
                }
            });
            v.validate(new StreamSource(new StringReader(xml)));
        } catch (SAXParseException e) {
            return List.of("NOT_WELL_FORMED " + e.getLineNumber());
        } catch (SAXException | IOException e) {
            throw new IllegalStateException(e);
        }
        return errors;
    }

    /** Le code d'une erreur de schema (ex. "cvc-pattern-valid") : le debut du message, identique dans toutes les langues. */
    private static String code(SAXException e) {
        String m = String.valueOf(e.getMessage());
        int colon = m.indexOf(':');
        return colon > 0 && !m.substring(0, colon).contains(" ") ? m.substring(0, colon) : "?";
    }

    /**
     * Le chemin d'un fichier rangé dans le DOSSIER DU PAQUET d'une classe, quel que soit le dossier
     * de lancement (racine du depot ou base/xml-kurse). Ex. XmlKit.file(Data.class, "files/a.xml").
     */
    public static Path file(Class<?> anchor, String relative) {
        Path direct = Path.of("src/main/java", anchor.getPackageName().replace('.', '/'), relative);
        if (Files.exists(direct)) {
            return direct;
        }
        try (Stream<Path> walk = Files.walk(Path.of(""), 3)) {
            return walk.map(p -> p.resolve(direct)).filter(Files::exists).findFirst().orElse(direct);
        } catch (IOException e) {
            return direct;
        }
    }

    private static Document parse(String xml) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            f.setFeature("http://xml.org/sax/features/external-general-entities", false);
            f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder b = f.newDocumentBuilder();
            b.setErrorHandler(new DefaultHandler()); // silencieux : sinon "[Fatal Error]" sur la console
            return b.parse(new InputSource(new StringReader(xml)));
        } catch (SAXParseException e) {
            throw new IllegalArgumentException("document mal forme (ligne " + e.getLineNumber() + ")", e);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static XPath xpath(Map<String, String> prefixes) {
        XPath x = XPathFactory.newInstance().newXPath();
        x.setNamespaceContext(new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                return prefixes.getOrDefault(prefix, XMLConstants.NULL_NS_URI);
            }

            @Override
            public String getPrefix(String uri) {
                return null;
            }

            @Override
            public Iterator<String> getPrefixes(String uri) {
                return null;
            }
        });
        return x;
    }
}
