package vorkurs02_xml.drills.r10_kata.solution;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import vorkurs02_xml.drills.Data;
import xmlkit.XmlKit;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Corrige du kata 10 : tout le chapitre 0.2 melange.
 */
public class Recall10 {

    public static void main(String[] args) throws Exception {
        String campus = Data.campusText();
        Map<String, String> ns = Map.of("c", "urn:campus", "g", "urn:campus:grades");

        // D01 : echapper < " & dans un attribut entre guillemets doubles ; ' reste brut.
        System.out.println("D01 : " + XmlKit.value("<r a=\"&lt;&quot;&amp;'\"/>", "string(/r/@a)"));
        // D02 : compter par URI de namespace.
        System.out.println("D02 : " + XmlKit.value(campus, "count(//*[namespace-uri()='urn:campus:grades'])"));
        // D03 : a+ exige au moins un a.
        System.out.println("D03 : " + XmlKit.validateDtd("<!DOCTYPE r [<!ELEMENT r (a+)><!ELEMENT a EMPTY>]><r/>", Map.of()));
        // D04 : positiveInteger refuse 0.
        Path xsd = Files.createTempFile("kata", ".xsd");
        xsd.toFile().deleteOnExit();
        Files.writeString(xsd, "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><xs:element name=\"q\" type=\"xs:positiveInteger\"/></xs:schema>");
        System.out.println("D04 : " + XmlKit.validateXsd(xsd, "<q>0</q>"));
        // D05 : l'enseignant qui a un autre cours plus loin : une comparaison de node-sets.
        System.out.println("D05 : " + XmlKit.value(campus, "//c:course[c:teacher = following-sibling::c:course/c:teacher]/c:teacher", ns));

        // D06 : DOM : retirer les personnes sans email, en figeant la liste d'abord.
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        Document doc = f.newDocumentBuilder().parse(Data.campus().toFile());
        NodeList people = doc.getElementsByTagNameNS("urn:campus", "person");
        List<Element> frozen = new ArrayList<>();
        for (int i = 0; i < people.getLength(); i++) {
            frozen.add((Element) people.item(i));
        }
        for (Element p : frozen) {
            if (!p.hasAttribute("email")) {
                p.getParentNode().removeChild(p);
            }
        }
        System.out.println("D06 : " + people.getLength());

        // D07 : SAX : somme des credits des cours qui ont au moins un etudiant.
        int[] total = {0};
        SAXParserFactory sf = SAXParserFactory.newInstance();
        sf.setNamespaceAware(true);
        sf.newSAXParser().parse(Data.campus().toFile(), new DefaultHandler() {
            int credits;
            boolean counted;

            @Override
            public void startElement(String uri, String localName, String qName, Attributes atts) {
                if (localName.equals("course")) {
                    credits = Integer.parseInt(atts.getValue("credits"));
                    counted = false;
                } else if (localName.equals("student") && !counted) {
                    total[0] += credits;
                    counted = true;
                }
            }
        });
        System.out.println("D07 : " + total[0]);

        // D08 : StAX : le nom de la personne S3, puis on s'arrete.
        String name = null;
        try (InputStream in = Files.newInputStream(Data.campus())) {
            XMLStreamReader r = XMLInputFactory.newFactory().createXMLStreamReader(in);
            while (r.hasNext() && name == null) {
                if (r.next() == XMLStreamConstants.START_ELEMENT && r.getLocalName().equals("person")
                        && "S3".equals(r.getAttributeValue(null, "id"))) {
                    name = r.getElementText();
                }
            }
            r.close();
        }
        System.out.println("D08 : " + name);

        // D09 : StAX sans DTD : une entite declaree dans le DOCTYPE devient inconnue -> refus.
        XMLInputFactory safe = XMLInputFactory.newFactory();
        safe.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        String verdict;
        try {
            XMLStreamReader r = safe.createXMLStreamReader(new StringReader("<!DOCTYPE r [<!ENTITY e \"x\">]><r>&e;</r>"));
            while (r.hasNext()) {
                r.next();
            }
            verdict = "accepte";
        } catch (XMLStreamException e) {
            verdict = "refuse";
        }
        System.out.println("D09 : " + verdict);

        // D10 : construire un document et l'ecrire.
        Document bilan = f.newDocumentBuilder().newDocument();
        Element root = bilan.createElement("bilan");
        root.setAttribute("etudiants", String.valueOf(doc.getElementsByTagNameNS("urn:campus", "person").getLength()));
        root.setAttribute("cours", XmlKit.value(campus, "count(//c:course)", ns));
        bilan.appendChild(root);
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter out = new StringWriter();
        t.transform(new DOMSource(bilan), new StreamResult(out));
        System.out.println("D10 : " + out);
    }
}
