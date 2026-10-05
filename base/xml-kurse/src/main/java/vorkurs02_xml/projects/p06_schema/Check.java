package vorkurs02_xml.projects.p06_schema;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du projet 6 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON SchemaCheck et TON catalog.xsd, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "good-1 VALIDE",
            "good-2 VALIDE",
            "bad-isbn INVALIDE 3 cvc-pattern-valid ; 3 cvc-attribute.3",
            "bad-lang INVALIDE 3 cvc-enumeration-valid ; 3 cvc-attribute.3",
            "bad-price-zero INVALIDE 6 cvc-minExclusive-valid ; 6 cvc-complex-type.2.2",
            "bad-price-max INVALIDE 6 cvc-maxInclusive-valid ; 6 cvc-complex-type.2.2",
            "bad-price-digits INVALIDE 6 cvc-fractionDigits-valid ; 6 cvc-complex-type.2.2",
            "bad-currency INVALIDE 6 cvc-pattern-valid ; 6 cvc-attribute.3",
            "bad-meta-twice INVALIDE 7 cvc-complex-type.2.4.a",
            "bad-year INVALIDE 7 cvc-datatype-valid.1.2.1 ; 7 cvc-type.3.1.3",
            "bad-authors INVALIDE 5 cvc-complex-type.2.4.e",
            "bad-no-author INVALIDE 6 cvc-complex-type.2.4.a",
            "bad-order INVALIDE 4 cvc-complex-type.2.4.a",
            "bad-both-places INVALIDE 8 cvc-complex-type.2.4.d",
            "bad-shelf INVALIDE 8 cvc-pattern-valid ; 8 cvc-type.3.1.3",
            "bad-missing-isbn INVALIDE 3 cvc-complex-type.4",
            "bad-status INVALIDE 3 cvc-complex-type.3.1",
            "bad-no-namespace INVALIDE 2 cvc-elt.1.a",
            "BILAN : 2 valides, 16 invalides",
            "CAUSE cvc-complex-type.2.4.a : bad-meta-twice, bad-no-author, bad-order",
            "CAUSE cvc-complex-type.2.4.d : bad-both-places",
            "CAUSE cvc-complex-type.2.4.e : bad-authors",
            "CAUSE cvc-complex-type.3.1 : bad-status",
            "CAUSE cvc-complex-type.4 : bad-missing-isbn",
            "CAUSE cvc-datatype-valid.1.2.1 : bad-year",
            "CAUSE cvc-elt.1.a : bad-no-namespace",
            "CAUSE cvc-enumeration-valid : bad-lang",
            "CAUSE cvc-fractionDigits-valid : bad-price-digits",
            "CAUSE cvc-maxInclusive-valid : bad-price-max",
            "CAUSE cvc-minExclusive-valid : bad-price-zero",
            "CAUSE cvc-pattern-valid : bad-currency, bad-isbn, bad-shelf");
            // EXPECTED-END

    static final List<String> API = List.of(
            "targetNamespace", "elementFormDefault=\"qualified\"", "xs:simpleType", "xs:restriction", "xs:pattern",
            "xs:enumeration", "xs:minExclusive", "xs:maxInclusive", "xs:fractionDigits", "xs:simpleContent",
            "xs:extension", "default=\"EUR\"", "xs:all", "xs:gYear", "xs:sequence", "xs:choice", "maxOccurs=\"3\"",
            "xs:anyURI", "use=\"required\"", "fixed=\"catalogued\"", "maxOccurs=\"unbounded\"",
            "XmlKit.file(", "XmlKit.validateXsd(", "TreeMap",
            "!javax.xml", "!org.w3c", "!org.xml");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "SchemaCheck", args, EXPECTED, API);
    }
}
