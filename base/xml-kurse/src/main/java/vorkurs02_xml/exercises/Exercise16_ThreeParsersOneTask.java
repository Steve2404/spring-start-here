package vorkurs02_xml.exercises;

import vorkurs02_xml.ExerciseChecker;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

/**
 * EXERCICE 16 - DOM, SAX, StAX : la meme tache trois fois, et savoir choisir (niveau : avance / entretien)
 * ======================================================================================================
 *
 * Rappel express du decoupage en "boites magiques" : voir Exercise01_WellFormednessDiagnoser.java.
 *
 * -- Le contexte --
 *
 * main() GENERE un catalogue de 30 000 produits dans un fichier temporaire :
 *
 *   <catalog>
 *     <product sku="P0" category="book"><name>Item 0</name><price>1.00</price></product>
 *     <product sku="P1" category="music"><name>Item 1</name><price>1.37</price></product>
 *     ...   categorie : i % 3 -> book / music / toy ; prix en centimes : 100 + (i * 37) % 10000
 *
 * Tache (0.2.20 S5) : nombre de livres et somme de leurs prix, en DOM,
 * en SAX et en StAX. Les trois doivent donner EXACTEMENT le meme
 * resultat que le calcul direct fait par main(). Puis deux taches ou un
 * modele brille : s'arreter tot (StAX) et trier tout le document (DOM).
 *
 * Fait verifie : le DOM de ce fichier contient 90 001 elements (1 + 3 x
 * 30 000) en memoire ; SAX et StAX n'en gardent aucun.
 *
 *
 * ==================================================================
 * TODO 1 : cents(price)
 * ==================================================================
 *
 * "12.34" -> 1234 (long), via BigDecimal (jamais de double pour de
 * l'argent : 0.1 + 0.2 != 0.3). strip() d'abord.
 *
 *
 * ==================================================================
 * TODO 2 : domTotals(xml)
 * ==================================================================
 *
 * Parse tout, getElementsByTagName("product"), garde category == "book",
 * additionne cents(price). -> Totals(nombre, somme en centimes).
 *
 *
 * ==================================================================
 * TODO 3 : saxTotals(xml)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Personne ne te montre le catalogue entier : on te le LIT a voix haute.
 * Au debut de chaque produit, retiens s'il est un livre ; accumule le
 * texte ; a la FIN de <price>, si c'est un livre, compte et additionne.
 * (Parseur non namespace-aware ici : qName suffit, pas de namespace.)
 *
 *
 * ==================================================================
 * TODO 4 : staxTotals(xml)
 * ==================================================================
 *
 * Meme logique avec ta propre boucle next() ; sur START <price> d'un
 * livre : getElementText().
 *
 *
 * ==================================================================
 * TODO 5 : staxFirstAtLeast(xml, minCents)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * "Trouve-moi le 1er produit a au moins 100.90 EUR, puis arrete." Compte
 * les START_ELEMENT lus (catalog compris) jusqu'au <price> gagnant inclus,
 * et rends FirstMatch(sku, elementsRead). Aucun -> null.
 * Pour le produit i gagnant : elementsRead = 3 * i + 4 (catalog + 3
 * elements par produit precedent + product, name, price du gagnant).
 *
 *
 * ==================================================================
 * TODO 6 : domTopExpensive(xml, n)
 * ==================================================================
 *
 * Les n sku les plus chers (prix decroissant, puis sku croissant en
 * ordre de String en cas d'egalite). Trier TOUT le document : il faut
 * tout avoir en main -> DOM.
 *
 *
 * ==================================================================
 * TODO 7 : chooseParser(needs)
 * ==================================================================
 *
 * -- Le probleme, explique comme a un tout petit enfant --
 *
 * Choisir son outil comme on choisit une valise, avec CETTE table (a
 * appliquer dans cet ordre, la 1re regle qui s'applique gagne) :
 *   1. besoin de MODIFIER le document                    -> DOM
 *   2. acces aleatoire (aller-retour) ET fichier pas enorme -> DOM
 *   3. besoin de s'ARRETER tot                          -> STAX
 *   4. fichier enorme                                   -> SAX
 *   5. sinon (petit fichier, lecture simple)            -> DOM
 *
 *
 * Exemple a verifier : les 3 Totals identiques au calcul direct ;
 * staxFirstAtLeast et domTopExpensive identiques au calcul direct ;
 * 7 cas de chooseParser.
 *
 *
 * Indices techniques Java (a lire seulement si le plan a la main est
 * clair mais que la traduction en code bloque) :
 *
 *   - new BigDecimal(s.strip()).movePointRight(2).longValueExact()
 *   - SAXParserFactory.newInstance().newSAXParser().parse(file, handler)
 *   - XMLInputFactory.newFactory().createXMLStreamReader(inputStream)
 *   - Comparator.comparingLong((Element p) -> ...).reversed().thenComparing(p -> p.getAttribute("sku"))
 *   - des tableaux a une case (int[] count = {0}) pour modifier depuis une classe anonyme
 */
public class Exercise16_ThreeParsersOneTask {

    public record Totals(int count, long cents) {
    }

    public record FirstMatch(String sku, int elementsRead) {
    }

    public enum Parser { DOM, SAX, STAX }

    public record Needs(boolean modify, boolean randomAccess, boolean hugeFile, boolean earlyStop) {
    }

    public static long cents(String price) {
        throw new UnsupportedOperationException("TODO 1 : implementer cents()");
    }

    public static Totals domTotals(Path xml) throws Exception {
        throw new UnsupportedOperationException("TODO 2 : implementer domTotals()");
    }

    public static Totals saxTotals(Path xml) throws Exception {
        throw new UnsupportedOperationException("TODO 3 : implementer saxTotals()");
    }

    public static Totals staxTotals(Path xml) throws Exception {
        throw new UnsupportedOperationException("TODO 4 : implementer staxTotals()");
    }

    public static FirstMatch staxFirstAtLeast(Path xml, long minCents) throws Exception {
        throw new UnsupportedOperationException("TODO 5 : implementer staxFirstAtLeast()");
    }

    public static List<String> domTopExpensive(Path xml, int n) throws Exception {
        throw new UnsupportedOperationException("TODO 6 : implementer domTopExpensive()");
    }

    public static Parser chooseParser(Needs needs) {
        throw new UnsupportedOperationException("TODO 7 : implementer chooseParser()");
    }

    public static void main(String[] args) throws Exception {
        ExerciseChecker.check("cents(12.34) == 1234, cents(' 1.00 ') == 100", cents("12.34") == 1234 && cents(" 1.00 ") == 100);

        int n = 30_000;
        Path xml = generate(n);
        try {
            Totals expected = expectedTotals(n);
            ExerciseChecker.check("attendu : 10000 livres", expected.count() == 10_000);
            ExerciseChecker.check("DOM : 90 001 elements en memoire",
                    DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml.toFile())
                            .getElementsByTagName("*").getLength() == 90_001);

            ExerciseChecker.check("domTotals == calcul direct " + expected, domTotals(xml).equals(expected));
            ExerciseChecker.check("saxTotals == calcul direct", saxTotals(xml).equals(expected));
            ExerciseChecker.check("staxTotals == calcul direct", staxTotals(xml).equals(expected));

            int winner = IntStream.range(0, n).filter(i -> priceCents(i) >= 10_090).findFirst().orElseThrow();
            FirstMatch first = staxFirstAtLeast(xml, 10_090);
            ExerciseChecker.check("staxFirstAtLeast(100.90 EUR) == P" + winner + " apres " + (3 * winner + 4) + " elements",
                    first != null && first.equals(new FirstMatch("P" + winner, 3 * winner + 4)));
            ExerciseChecker.check("staxFirstAtLeast(1 000 000 EUR) == null", staxFirstAtLeast(xml, 100_000_000) == null);

            List<String> top = IntStream.range(0, n).boxed()
                    .sorted(Comparator.comparingLong((Integer i) -> priceCents(i)).reversed().thenComparing(i -> "P" + i))
                    .limit(4).map(i -> "P" + i).toList();
            ExerciseChecker.check("domTopExpensive(4) == " + top, domTopExpensive(xml, 4).equals(top));
        } finally {
            Files.deleteIfExists(xml);
        }

        ExerciseChecker.check("chooseParser(modifier, enorme) == DOM", chooseParser(new Needs(true, false, true, false)) == Parser.DOM);
        ExerciseChecker.check("chooseParser(acces aleatoire, petit) == DOM", chooseParser(new Needs(false, true, false, false)) == Parser.DOM);
        ExerciseChecker.check("chooseParser(acces aleatoire, enorme) == SAX", chooseParser(new Needs(false, true, true, false)) == Parser.SAX);
        ExerciseChecker.check("chooseParser(arret tot, enorme) == STAX", chooseParser(new Needs(false, false, true, true)) == Parser.STAX);
        ExerciseChecker.check("chooseParser(arret tot, petit) == STAX", chooseParser(new Needs(false, false, false, true)) == Parser.STAX);
        ExerciseChecker.check("chooseParser(enorme) == SAX", chooseParser(new Needs(false, false, true, false)) == Parser.SAX);
        ExerciseChecker.check("chooseParser(rien de special) == DOM", chooseParser(new Needs(false, false, false, false)) == Parser.DOM);

        ExerciseChecker.summary();
    }

    // ------------------------------------------------------------------
    // Deja ecrit : generation des donnees et calcul direct (ne pas modifier)
    // ------------------------------------------------------------------

    private static long priceCents(int i) {
        return 100 + (i * 37L) % 10_000;
    }

    private static String category(int i) {
        return switch (i % 3) {
            case 0 -> "book";
            case 1 -> "music";
            default -> "toy";
        };
    }

    private static Path generate(int n) throws Exception {
        Path file = Files.createTempFile("catalog-", ".xml");
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<catalog>\n");
            for (int i = 0; i < n; i++) {
                long c = priceCents(i);
                w.write(String.format(Locale.ROOT, "  <product sku=\"P%d\" category=\"%s\"><name>Item %d</name><price>%d.%02d</price></product>%n",
                        i, category(i), i, c / 100, c % 100));
            }
            w.write("</catalog>\n");
        }
        return file;
    }

    private static Totals expectedTotals(int n) {
        int count = 0;
        long sum = 0;
        for (int i = 0; i < n; i++) {
            if (category(i).equals("book")) {
                count++;
                sum += priceCents(i);
            }
        }
        return new Totals(count, sum);
    }
}
