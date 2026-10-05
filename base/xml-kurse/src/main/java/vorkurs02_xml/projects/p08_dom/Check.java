package vorkurs02_xml.projects.p08_dom;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du projet 8 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON DomLab, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "DOCUMENT PI COMMENT ELEMENT",
            "RACINE playlist name=Dimanche | PI player [version=\"2\"]",
            "PISTE t1 \"Blue in Green\" 337s noeuds=7 elements=3 tags=[jazz, modal]",
            "PISTE t2 \"Rock & Roll <live>\" 225s noeuds=11 elements=6 tags=[]",
            "PISTE t3 \"So What\" 562s noeuds=4 elements=4 tags=[jazz]",
            "ATTRIBUT absent : [] present=false",
            "PUBS boucle naive : 4 au depart, 2 retirees, reste 2",
            "PUBS liste figee : 2 retirees, reste 0",
            "NS par nom ecrit : item=1 o:item=2",
            "NS par URI : orders=1 common=1 other=1 tous=3",
            "NS A : nom=o:item prefixe=o local=item uri=urn:shop:orders",
            "NS B : nom=item prefixe=null local=item uri=urn:shop:common",
            "NS C : nom=o:item prefixe=o local=item uri=urn:other",
            "NS sans namespaces : nom=o:item local=null uri=null",
            "MIGRATION nettoyes=18 renommes=4 deplaces=2 vides=2",
            "V2 <clients version=\"2\"><client id=\"c1\"><name>Adam Smith</name><email>adam@x.org</email></client><client id=\"c2\"><vip>no</vip><name>Lea Dubois</name></client><client id=\"c3\"><vip>yes</vip><name>Zoe Martin</name><email>zoe@mail.fr</email><phone>0601020304</phone></client></clients>");
            // EXPECTED-END

    static final List<String> API = List.of(
            "DocumentBuilderFactory.newInstance()", "setNamespaceAware(", "newDocumentBuilder()",
            "getFirstChild()", "getNextSibling()", "getNodeType()", "getDocumentElement()",
            "ProcessingInstruction", "getTarget()", "getData()",
            "getElementsByTagName(", "getTextContent()", "getChildNodes()", "getAttribute(", "hasAttribute(",
            "removeChild(", "getParentNode()",
            "getElementsByTagNameNS(", "getPrefix()", "getLocalName()", "getNamespaceURI()",
            "renameNode(", "setAttribute(", "createElement(", "insertBefore(", "removeAttribute(", "setTextContent(",
            "hasChildNodes()", "hasAttributes()", "appendChild",
            "TransformerFactory", "OutputKeys.OMIT_XML_DECLARATION", "DOMSource", "StreamResult",
            "!XmlKit", "!SAXParser", "!XMLInputFactory", "!javax.xml.xpath", "!javax.xml.validation");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "DomLab", args, EXPECTED, API);
    }
}
