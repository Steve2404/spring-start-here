package vorkurs02_xml.drills;

import xmlkit.XmlKit;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Les donnees communes a tous les drills (ne pas modifier) : le fichier files/campus.xml.
 */
public final class Data {

    private Data() {
    }

    /** Le chemin de files/campus.xml. */
    public static Path campus() {
        return XmlKit.file(Data.class, "files/campus.xml");
    }

    /** Le texte de files/campus.xml. */
    public static String campusText() {
        try {
            return Files.readString(campus());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
