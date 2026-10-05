package vorkurs02_xml.projects.p06_schema.solution;

import vorkurs02_xml.projects.p06_schema.Data;
import xmlkit.XmlKit;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Corrige du projet 6 : le controle d'un lot contre TON schema (0.2.14 -> 0.2.15).
 * Le vrai travail est dans catalog.xsd ; ce main ne fait que juger et regrouper.
 */
public class SchemaCheck {

    public static void main(String[] args) {
        // Le schema est cherche dans le dossier du paquet de CETTE classe : le tien, ou celui du corrige.
        Path xsd = XmlKit.file(SchemaCheck.class, "catalog.xsd");
        Map<String, TreeSet<String>> causes = new TreeMap<>();
        int valid = 0;
        for (String name : Data.DOCUMENTS) {
            List<String> errors = XmlKit.validateXsd(xsd, Data.text(name));
            if (errors.isEmpty()) {
                valid++;
                System.out.println(name + " VALIDE");
                continue;
            }
            System.out.println(name + " INVALIDE " + String.join(" ; ", errors));
            // La CAUSE est la premiere erreur : les suivantes en sont souvent la consequence
            // (cvc-attribute.3 suit cvc-pattern-valid pour la meme valeur d'attribut).
            String first = errors.get(0).substring(errors.get(0).indexOf(' ') + 1);
            causes.computeIfAbsent(first, k -> new TreeSet<>()).add(name);
        }
        System.out.println("BILAN : " + valid + " valides, " + (Data.DOCUMENTS.size() - valid) + " invalides");
        causes.forEach((code, docs) -> System.out.println("CAUSE " + code + " : " + String.join(", ", docs)));
    }
}
