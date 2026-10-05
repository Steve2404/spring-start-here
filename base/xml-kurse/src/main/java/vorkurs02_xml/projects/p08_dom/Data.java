package vorkurs02_xml.projects.p08_dom;

import xmlkit.XmlKit;

import java.nio.file.Path;

/**
 * Les donnees du projet 8 (ne pas modifier). Consigne : TODO.md.
 * Les documents sont de vrais fichiers, dans le dossier files/ a cote de ce fichier.
 */
public final class Data {

    private Data() {
    }

    /** Le chemin d'un fichier de files/ : Data.file("playlist.xml"). */
    public static Path file(String name) {
        return XmlKit.file(Data.class, "files/" + name);
    }
}
