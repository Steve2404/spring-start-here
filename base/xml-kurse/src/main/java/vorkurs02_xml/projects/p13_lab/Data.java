package vorkurs02_xml.projects.p13_lab;

import xmlkit.XmlKit;

import java.nio.file.Path;
import java.util.List;

/**
 * Les donnees du projet 13 (ne pas modifier). Consigne : TODO.md.
 * Les documents et le schema sont de vrais fichiers, dans le dossier files/ a cote de ce fichier.
 */
public final class Data {

    private Data() {
    }

    /** Les commandes a importer, dans l'ordre du rapport. */
    public static final List<String> FILES = List.of(
            "01-ok.xml", "02-ok-prefixed.xml", "03-not-wellformed.xml", "04-doctype.xml", "05-wrong-namespace.xml",
            "06-no-namespace.xml", "07-schema-enum.xml", "08-schema-missing.xml", "09-business-quantity.xml",
            "10-business-multi.xml", "11-schema-many.xml");

    /** Le chemin d'un fichier de files/ : Data.file("01-ok.xml"), Data.file("lab-order.xsd"). */
    public static Path file(String name) {
        return XmlKit.file(Data.class, "files/" + name);
    }
}
