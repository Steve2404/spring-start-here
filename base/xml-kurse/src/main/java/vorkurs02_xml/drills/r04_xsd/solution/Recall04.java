package vorkurs02_xml.drills.r04_xsd.solution;

import xmlkit.XmlKit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Corrige du drill 4 : ecrire des schemas XSD (0.2.14 -> 0.2.15).
 */
public class Recall04 {

    static final String XS = "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"";

    /** Ecrit un schema sans namespace cible dans un fichier temporaire. */
    static Path schema(String body) throws IOException {
        return write("<xs:schema " + XS + ">" + body + "</xs:schema>");
    }

    static Path write(String xsd) throws IOException {
        Path p = Files.createTempFile("drill", ".xsd");
        p.toFile().deleteOnExit();
        Files.writeString(p, xsd);
        return p;
    }

    /** Les seuls CODES des erreurs (les documents tiennent sur une ligne). */
    static List<String> codes(Path xsd, String doc) {
        return XmlKit.validateXsd(xsd, doc).stream().map(e -> e.substring(e.indexOf(' ') + 1)).toList();
    }

    static String both(Path xsd, String ok, String bad) {
        return codes(xsd, ok) + " " + codes(xsd, bad);
    }

    public static void main(String[] args) throws IOException {
        // D01 : un element global de type predefini.
        Path d01 = schema("<xs:element name=\"r\" type=\"xs:int\"/>");
        System.out.println("D01 : " + both(d01, "<r>5</r>", "<r>x</r>"));
        // D02 : un type simple anonyme restreint par un pattern (ancre sur toute la valeur).
        Path d02 = schema("<xs:element name=\"r\"><xs:simpleType><xs:restriction base=\"xs:string\">"
                + "<xs:pattern value=\"[A-Z]{2}[0-9]\"/></xs:restriction></xs:simpleType></xs:element>");
        System.out.println("D02 : " + both(d02, "<r>AB1</r>", "<r>AB12</r>"));
        // D03 : un intervalle : 1 inclus, 10 exclu.
        Path d03 = schema("<xs:element name=\"r\"><xs:simpleType><xs:restriction base=\"xs:int\">"
                + "<xs:minInclusive value=\"1\"/><xs:maxExclusive value=\"10\"/></xs:restriction></xs:simpleType></xs:element>");
        System.out.println("D03 : " + both(d03, "<r>1</r>", "<r>10</r>"));
        // D04 : une enumeration.
        Path d04 = schema("<xs:element name=\"r\"><xs:simpleType><xs:restriction base=\"xs:string\">"
                + "<xs:enumeration value=\"rouge\"/><xs:enumeration value=\"vert\"/></xs:restriction></xs:simpleType></xs:element>");
        System.out.println("D04 : " + both(d04, "<r>vert</r>", "<r>Vert</r>"));
        // D05 : une sequence avec cardinalites : a (1), puis b de 0 a 2 fois.
        Path d05 = schema("<xs:element name=\"r\"><xs:complexType><xs:sequence><xs:element name=\"a\"/>"
                + "<xs:element name=\"b\" minOccurs=\"0\" maxOccurs=\"2\"/></xs:sequence></xs:complexType></xs:element>");
        System.out.println("D05 : " + both(d05, "<r><a/></r>", "<r><a/><b/><b/><b/></r>"));
        // D06 : un choix : a OU b, pas les deux.
        Path d06 = schema("<xs:element name=\"r\"><xs:complexType><xs:choice><xs:element name=\"a\"/><xs:element name=\"b\"/>"
                + "</xs:choice></xs:complexType></xs:element>");
        System.out.println("D06 : " + both(d06, "<r><b/></r>", "<r><a/><b/></r>"));
        // D07 : all : chacun au plus une fois, dans n'importe quel ordre.
        Path d07 = schema("<xs:element name=\"r\"><xs:complexType><xs:all><xs:element name=\"a\"/><xs:element name=\"b\"/>"
                + "</xs:all></xs:complexType></xs:element>");
        System.out.println("D07 : " + both(d07, "<r><b/><a/></r>", "<r><a/><a/><b/></r>"));
        // D08 : un attribut obligatoire ; un autre a une valeur fixee.
        Path d08 = schema("<xs:element name=\"r\"><xs:complexType><xs:attribute name=\"id\" type=\"xs:string\" use=\"required\"/>"
                + "<xs:attribute name=\"v\" type=\"xs:string\" fixed=\"2\"/></xs:complexType></xs:element>");
        System.out.println("D08 : " + codes(d08, "<r/>") + " " + codes(d08, "<r id=\"x\" v=\"3\"/>"));
        // D09 : du texte ET un attribut : simpleContent + extension.
        Path d09 = schema("<xs:element name=\"prix\"><xs:complexType><xs:simpleContent><xs:extension base=\"xs:decimal\">"
                + "<xs:attribute name=\"devise\" type=\"xs:string\"/></xs:extension></xs:simpleContent></xs:complexType></xs:element>");
        System.out.println("D09 : " + both(d09, "<prix devise=\"EUR\">9.5</prix>", "<prix devise=\"EUR\">neuf</prix>"));
        // D10 : avec targetNamespace, la racine doit etre DANS ce namespace.
        Path d10 = write("<xs:schema " + XS + " targetNamespace=\"urn:t\" elementFormDefault=\"qualified\">"
                + "<xs:element name=\"r\" type=\"xs:string\"/></xs:schema>");
        System.out.println("D10 : " + both(d10, "<r xmlns=\"urn:t\">x</r>", "<r>x</r>"));
        // D11 : seul un element GLOBAL peut etre la racine d'un document.
        System.out.println("D11 : " + codes(d05, "<a/>"));
    }
}
