package vorkurs02_xml.projects.p03_tree;

import java.util.List;

/**
 * Les donnees du projet 3 (ne pas modifier). Consigne : TODO.md.
 */
public final class Data {

    private Data() {
    }

    /** Le document a mettre en arbre (etapes 1 a 4). */
    public static final String ORDER = """
            <?xml version="1.0"?>
            <!-- commande du jour -->
            <?routage file=A?>
            <commande id="c1" client="Lea">
              <ligne ref="p1"><produit>Stylo</produit><quantite>3</quantite><prix>1.50</prix></ligne>
              <!-- promo -->
              <ligne ref="p2"><produit>Cahier &amp; agenda</produit><quantite>1</quantite><prix>4.20</prix></ligne>
              <note>Livrer <b>avant</b> midi</note>
            </commande>
            """;

    /** Un document soumis a la chaine de controle (etape 5). */
    public record Submission(String id, String text) {
    }

    /** Le DOCTYPE commun aux commandes : la DTD est interne, dans le document lui-meme. */
    public static final String DTD = """
            <!DOCTYPE commande [
              <!ELEMENT commande (ligne+)>
              <!ATTLIST commande id ID #REQUIRED devise CDATA "EUR">
              <!ELEMENT ligne (produit, quantite, prix)>
              <!ELEMENT produit (#PCDATA)>
              <!ELEMENT quantite (#PCDATA)>
              <!ELEMENT prix (#PCDATA)>
            ]>
            """;

    public static final List<Submission> ORDERS = List.of(
            new Submission("O01", DTD + """
                    <commande id="c1">
                      <ligne><produit>Stylo</produit><quantite>3</quantite><prix>1.50</prix></ligne>
                    </commande>"""),
            new Submission("O02", DTD + """
                    <commande id="c2">
                      <ligne><produit>Stylo</produit><prix>1.50</prix></ligne>
                    </commande>"""),
            new Submission("O03", DTD + """
                    <commande id="c3">
                      <ligne><produit>Stylo</produit><quantite>3</quantite><prix>1.50</prix>
                    </commande>"""),
            new Submission("O04", DTD + """
                    <commande id="c4">
                      <ligne><produit>Stylo</produit><quantite>-2</quantite><prix>1.50</prix></ligne>
                      <ligne><produit>Gomme</produit><quantite>1</quantite><prix>gratuit</prix></ligne>
                    </commande>"""),
            new Submission("O05", """
                    <commande id="c5">
                      <ligne><produit>Stylo</produit><quantite>3</quantite><prix>1.50</prix></ligne>
                    </commande>"""),
            new Submission("O06", DTD + """
                    <commande>
                      <ligne><produit>Stylo</produit><quantite>3</quantite><prix>1.50</prix><remise>10</remise></ligne>
                    </commande>"""),
            new Submission("O07", DTD + """
                    <commande id="c7" devise="CHF">
                      <ligne><produit>Stylo</produit><quantite>2</quantite><prix>1.50</prix></ligne>
                      <ligne><produit>Gomme</produit><quantite>4</quantite><prix>0.25</prix></ligne>
                    </commande>"""));
}
