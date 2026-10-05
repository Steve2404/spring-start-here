package vorkurs02_xml.projects.p06_schema;

import xmlkit.XmlKit;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Les donnees du projet 6 (ne pas modifier). Consigne : TODO.md.
 * Les documents sont de vrais fichiers, dans le dossier files/ a cote de ce fichier.
 */
public final class Data {

    private Data() {
    }

    /** Les documents a juger, dans l'ordre d'affichage (noms de fichiers sans ".xml"). */
    public static final List<String> DOCUMENTS = List.of(
            "good-1", "good-2", "bad-isbn", "bad-lang", "bad-price-zero", "bad-price-max", "bad-price-digits",
            "bad-currency", "bad-meta-twice", "bad-year", "bad-authors", "bad-no-author", "bad-order",
            "bad-both-places", "bad-shelf", "bad-missing-isbn", "bad-status", "bad-no-namespace");

    /** Le texte d'un document, par exemple text("good-1"). */
    public static String text(String name) {
        try {
            return Files.readString(XmlKit.file(Data.class, "files/" + name + ".xml"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
