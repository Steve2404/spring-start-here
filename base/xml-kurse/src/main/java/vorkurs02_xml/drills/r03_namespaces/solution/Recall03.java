package vorkurs02_xml.drills.r03_namespaces.solution;

import vorkurs02_xml.drills.Data;
import xmlkit.XmlKit;

import java.util.Map;

/**
 * Corrige du drill 3 : les namespaces (0.2.12 -> 0.2.13).
 */
public class Recall03 {

    public static void main(String[] args) {
        String campus = Data.campusText();
        // D01 : la racine prend le namespace PAR DEFAUT.
        System.out.println("D01 : " + XmlKit.value(campus, "namespace-uri(/*)") + " " + XmlKit.value(campus, "local-name(/*)"));
        // D02 : g:grade est dans le namespace lie a g.
        System.out.println("D02 : " + XmlKit.value(campus, "namespace-uri((//*[local-name()='grade'])[1])"));
        // D03 : un attribut SANS prefixe n'est dans aucun namespace, meme sous un defaut.
        System.out.println("D03 : [" + XmlKit.value(campus, "namespace-uri((//*[local-name()='student'])[1]/@ref)") + "]");
        // D04 : la meme regle, sur un document a soi.
        String d04 = "<r xmlns=\"urn:x\" a=\"1\"/>";
        System.out.println("D04 : [" + XmlKit.value(d04, "namespace-uri(/*)") + "] [" + XmlKit.value(d04, "namespace-uri(/*/@a)") + "]");
        // D05 : xmlns="" annule le defaut pour ce sous-arbre.
        System.out.println("D05 : [" + XmlKit.value("<r xmlns=\"urn:x\"><c xmlns=\"\"/></r>", "namespace-uri(/*/*)") + "]");
        // D06 : un prefixe redefini plus bas : la liaison la plus proche gagne.
        System.out.println("D06 : " + XmlKit.value("<p:r xmlns:p=\"urn:a\"><p:c xmlns:p=\"urn:b\"/></p:r>", "namespace-uri(/*/*)"));
        // D07 : deux ecritures, un meme nom etendu.
        String a = "<x:r xmlns:x=\"urn:same\"/>";
        String b = "<r xmlns=\"urn:same\"/>";
        String ea = XmlKit.value(a, "concat(namespace-uri(/*), '|', local-name(/*))");
        String eb = XmlKit.value(b, "concat(namespace-uri(/*), '|', local-name(/*))");
        System.out.println("D07 : " + ea.equals(eb) + " " + XmlKit.value(a, "name(/*)") + " " + XmlKit.value(b, "name(/*)"));
        // D08 : un prefixe non declare : bien forme pour XML 1.0, refuse par les namespaces.
        System.out.println("D08 : " + XmlKit.wellFormed("<a:r/>") + " " + XmlKit.wellFormedNs("<a:r/>"));
        // D09 : xml est reserve a son URI.
        System.out.println("D09 : " + XmlKit.wellFormedNs("<r xmlns:xml=\"urn:autre\"/>"));
        // D10 : en XPath, un nom SANS prefixe = sans namespace. Il faut lier un prefixe soi-meme.
        System.out.println("D10 : " + XmlKit.value(campus, "count(//c:course)", Map.of("c", "urn:campus"))
                + " " + XmlKit.value(campus, "count(//course)"));
        // D11 : resoudre un QName (la valeur d'un xsi:type) avec des liaisons donnees.
        Map<String, String> scope = Map.of("", "urn:d", "m", "urn:money");
        System.out.println("D11 : " + expand("m:Euro", scope, true) + " " + expand("Euro", scope, true) + " " + expand("rate", scope, false));
    }

    static String expand(String qName, Map<String, String> scope, boolean element) {
        int colon = qName.indexOf(':');
        String prefix = colon < 0 ? "" : qName.substring(0, colon);
        String local = qName.substring(colon + 1);
        // Le defaut ne vaut que pour les elements (et les valeurs QName comme xsi:type), jamais pour les attributs.
        String uri = colon < 0 && !element ? null : scope.get(prefix);
        return uri == null || uri.isEmpty() ? local : "{" + uri + "}" + local;
    }
}
