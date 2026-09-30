package vorkurs02_xml.solutions;

/**
 * Corrige de l'exercice 9. A ne consulter qu'apres avoir essaye par
 * vous-meme dans vorkurs02_xml.exercises.Exercise09_XsdAuthoring.
 */
public class Solution09_XsdAuthoring {

    public static String isbnType() {
        // Un pattern XSD est implicitement ancre (^...$) : pas besoin de les ecrire,
        // et "[0-9]{10}" impose exactement 10 chiffres apres 978/979.
        return """
                <xs:simpleType name="IsbnType">
                  <xs:restriction base="xs:string">
                    <xs:pattern value="97[89][0-9]{10}"/>
                  </xs:restriction>
                </xs:simpleType>
                """;
    }

    public static String languageType() {
        // Une enumeration ferme la liste : toute autre valeur (meme "FR") est refusee.
        return """
                <xs:simpleType name="LanguageType">
                  <xs:restriction base="xs:string">
                    <xs:enumeration value="de"/>
                    <xs:enumeration value="fr"/>
                    <xs:enumeration value="en"/>
                  </xs:restriction>
                </xs:simpleType>
                """;
    }

    public static String amountType() {
        // minExclusive 0 refuse 0 lui-meme (minInclusive l'accepterait) ; fractionDigits 2
        // refuse 12.505 mais accepte 12.5 (c'est un MAXIMUM de decimales).
        return """
                <xs:simpleType name="AmountType">
                  <xs:restriction base="xs:decimal">
                    <xs:minExclusive value="0"/>
                    <xs:maxInclusive value="9999.99"/>
                    <xs:fractionDigits value="2"/>
                  </xs:restriction>
                </xs:simpleType>
                """;
    }

    public static String priceType() {
        // Texte + attribut = simpleContent/extension : on ETEND un type simple avec un attribut.
        // Le type de l'attribut est anonyme (inline) car il ne sert qu'ici.
        return """
                <xs:complexType name="PriceType">
                  <xs:simpleContent>
                    <xs:extension base="c:AmountType">
                      <xs:attribute name="currency" default="EUR">
                        <xs:simpleType>
                          <xs:restriction base="xs:string">
                            <xs:pattern value="[A-Z]{3}"/>
                          </xs:restriction>
                        </xs:simpleType>
                      </xs:attribute>
                    </xs:extension>
                  </xs:simpleContent>
                </xs:complexType>
                """;
    }

    public static String metaType() {
        // xs:all = chaque enfant au plus une fois, dans N'IMPORTE QUEL ordre (au contraire de sequence).
        return """
                <xs:complexType name="MetaType">
                  <xs:all>
                    <xs:element name="publisher" type="xs:string"/>
                    <xs:element name="year" type="xs:gYear" minOccurs="0"/>
                  </xs:all>
                </xs:complexType>
                """;
    }

    public static String bookType() {
        // Les types nommes se referencent avec le prefixe du targetNamespace (c:...), les types
        // predefinis avec xs:. Les attributs se declarent APRES le modele de contenu.
        return """
                <xs:complexType name="BookType">
                  <xs:sequence>
                    <xs:element name="title" type="xs:string"/>
                    <xs:element name="author" type="xs:string" maxOccurs="3"/>
                    <xs:element name="price" type="c:PriceType"/>
                    <xs:element name="meta" type="c:MetaType" minOccurs="0"/>
                    <xs:choice>
                      <xs:element name="ebookUrl" type="xs:anyURI"/>
                      <xs:element name="shelf">
                        <xs:simpleType>
                          <xs:restriction base="xs:string">
                            <xs:pattern value="[A-Z][0-9]{2}"/>
                          </xs:restriction>
                        </xs:simpleType>
                      </xs:element>
                    </xs:choice>
                  </xs:sequence>
                  <xs:attribute name="isbn" type="c:IsbnType" use="required"/>
                  <xs:attribute name="lang" type="c:LanguageType" default="fr"/>
                  <xs:attribute name="status" type="xs:string" fixed="catalogued"/>
                </xs:complexType>
                """;
    }

    public static String catalogElement() {
        // Seul l'element GLOBAL (enfant direct de xs:schema) peut etre la racine d'une instance.
        return """
                <xs:element name="catalog">
                  <xs:complexType>
                    <xs:sequence>
                      <xs:element name="book" type="c:BookType" minOccurs="0" maxOccurs="unbounded"/>
                    </xs:sequence>
                  </xs:complexType>
                </xs:element>
                """;
    }
}
