package vorkurs02_xml.projects.p01_gate;

import java.util.List;

/**
 * Les donnees du projet 1 (ne pas modifier). Consigne : TODO.md.
 */
public final class Data {

    private Data() {
    }

    /** Un document soumis au portier : son identifiant et son texte brut. */
    public record Submission(String id, String text) {
    }

    public static final List<Submission> SUBMISSIONS = List.of(
            new Submission("D01", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <catalogue saison="ete">
                      <livre id="b1" langue="fr">
                        <titre>Dune</titre>
                        <stock/>
                      </livre>
                      <livre id="b2" langue="en">
                        <titre>Emma</titre>
                      </livre>
                    </catalogue>
                    """),
            new Submission("D02", "<personne id='p1'><prénom>Léa</prénom><nom>Martin</nom><actif/></personne>"),
            new Submission("D03", """
                    <?xml encoding="UTF-8" version="1.0"?>
                    <a/>
                    """),
            new Submission("D04", """

                    <?xml version="1.0"?>
                    <a/>
                    """),
            new Submission("D05", """
                    <prix>
                      <1erPrix>10</1erPrix>
                    </prix>
                    """),
            new Submission("D06", "<a b-c=\"1\" 2d=\"x\"/>"),
            new Submission("D07", "<a x=\"1\"y=\"2\"/>"),
            new Submission("D08", "<a x=1/>"),
            new Submission("D09", """
                    <a>
                      <b>texte</b x="1">
                    </a>
                    """),
            new Submission("D10", "<a x=\"1\" y=\"2\" x=\"3\"/>"),
            new Submission("D11", """
                    <livre>
                      <Titre>Dune</titre>
                    </livre>
                    """),
            new Submission("D12", "<p><b><i>gras italique</b></i></p>"),
            new Submission("D13", "<a></a></b>"),
            new Submission("D14", """
                    <commande>
                      <ligne>
                        <article>stylo</article>
                      </ligne>
                    """),
            new Submission("D15", "<a/>\nfin du document\n"),
            new Submission("D16", "<a/>\n<b/>\n"),
            new Submission("D17", "<?xml version=\"1.0\"?>\n\n"),
            new Submission("D18", "<xmlConfig mode = 'test'>\n  <cle>valeur</cle>\n</xmlConfig>\n"),
            new Submission("D19", "<?xml version=\"1.0\" standalone=\"oui\"?>\n<a/>\n"),
            new Submission("D20", "<?xml encoding=\"UTF-8\"?>\n<a/>\n"));

    /** Les noms des champs d'une fiche de BOOKS, dans l'ordre. */
    public static final List<String> FIELDS = List.of("id", "titre", "auteur", "annee", "langue");

    /** Des fiches « a plat » : des valeurs separees par ';', sans aucune structure. */
    public static final List<String> BOOKS = List.of(
            "b1;Dune;Herbert;1965;fr",
            "b2;Emma;Austen;1815;en",
            "b3;Le Horla;Maupassant;1887;fr");
}
