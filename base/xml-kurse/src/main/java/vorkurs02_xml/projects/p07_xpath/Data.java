package vorkurs02_xml.projects.p07_xpath;

import java.util.List;

/**
 * Les donnees du projet 7 (ne pas modifier). Consigne : TODO.md.
 */
public final class Data {

    private Data() {
    }

    /** L'organigramme interroge par toutes les requetes. */
    public static final String COMPANY = """
            <?xml version="1.0" encoding="UTF-8"?>
            <company name="Acme">
              <department name="IT">
                <employee id="e1" role="manager"><name>Alice</name><salary>6000</salary></employee>
                <employee id="e2" reportsTo="e1"><name>Bob</name><salary>4000</salary></employee>
                <employee id="e3" reportsTo="e1"><name>Chloe</name><salary>4200</salary></employee>
                <department name="Dev">
                  <employee id="e4" role="manager" reportsTo="e1"><name>Dan</name><salary>5500</salary></employee>
                  <employee id="e5" reportsTo="e4"><name>Eva</name><salary>4800</salary></employee>
                </department>
              </department>
              <department name="Sales">
                <employee id="e6" role="manager"><name>Farid</name><salary>5000</salary></employee>
                <employee id="e7"><name>  Gina   Lopez </name><salary>3900</salary></employee>
              </department>
              <department name="Legal">
                <employee id="e8" role="manager"><name>Hugo</name><salary>5200</salary></employee>
              </department>
              <r:reviews xmlns:r="urn:hr:reviews">
                <r:review employee="e2"><r:score>5</r:score></r:review>
                <r:review employee="e5"><r:score>3</r:score></r:review>
                <r:review employee="e7"><r:score>4</r:score></r:review>
              </r:reviews>
            </company>
            """;

    /** Les chemins que TON moteur doit evaluer (etape 3). */
    public static final List<String> PATHS = List.of(
            "/company/department/employee/name",
            "//employee[@role='manager']/name",
            "//department[@name='IT']/employee[2]",
            "//department/employee[last()]",
            "//employee[1]",
            "/company/*[@name='Sales']/employee",
            "//employee[salary='4000']/name",
            "//department[department]",
            "//employee[@reportsTo][2]/name",
            "/company/department[3]/employee/salary",
            "//department/department/employee[@id='e5']",
            "/company/nothing//name");
}
