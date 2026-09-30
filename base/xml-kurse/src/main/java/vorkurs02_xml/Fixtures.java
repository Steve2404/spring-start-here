package vorkurs02_xml;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Acces aux fichiers de test (.xml, .xsd, .dtd) ranges dans
 * src/main/resources/vorkurs02_xml/fixtures/.
 *
 * Pourquoi passer par le classpath et pas par Path.of("fixtures/...") ?
 * Parce que le dossier de travail change selon la facon de lancer le
 * programme (IntelliJ ouvert sur le depot, sur base/xml-kurse, ou mvn).
 * Le classpath, lui, est toujours le meme : target/classes contient une
 * copie des ressources. Deja ecrit : tu n'as rien a coder ici.
 */
public final class Fixtures {

    private Fixtures() {
    }

    /** Chemin reel d'un fichier de test, ex. path("ex08/order-ok.xml"). */
    public static Path path(String relative) {
        String name = "/vorkurs02_xml/fixtures/" + relative;
        URL url = Fixtures.class.getResource(name);
        if (url == null) {
            throw new IllegalArgumentException("Fixture introuvable sur le classpath : " + name
                    + " (as-tu recompile / copie src/main/resources ?)");
        }
        try {
            return Path.of(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Contenu texte (UTF-8) d'un fichier de test. */
    public static String read(String relative) {
        try {
            return Files.readString(path(relative), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
