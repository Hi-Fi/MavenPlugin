package org.robotframework.mavenplugin;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasXPath;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


public class AcceptanceTestMojoTest
        extends AbstractMojoTestCase {

    public void setup()
            throws Exception {
        super.setUp();
    }

    public void testShouldSucceed()
            throws Exception {
        File pom = getTestFile("src/test/resources/pom-success.xml");
        AcceptanceTestMojo mojo = (AcceptanceTestMojo) lookupMojo("acceptance-test", pom);
        mojo.execute();
        File xunitFile = getTestFile("target/robotframework-reports/TEST-robot-success.xml");
        assertTrue("missing xunit test report " + xunitFile, xunitFile.exists());

        Document xunit = parseDocument(xunitFile);
        assertThat(xunit, hasXPath("/testsuite[@errors='0']"));
        assertThat(xunit, hasXPath("/testsuite[@failures='0']"));
        assertThat(xunit, hasXPath("/testsuite[@tests='2']"));
    }

    public void testShouldFail()
            throws Exception {
        File pom = getTestFile("src/test/resources/pom-fail.xml");
        AcceptanceTestMojo mojo = (AcceptanceTestMojo) lookupMojo("acceptance-test", pom);

        mojo.execute();

        // output.xml contains failure
        File xunitFile = getTestFile("target/robotframework-reports/TEST-robot-fail.xml");
        assertTrue("missing xunit test report " + xunitFile, xunitFile.exists());

        Document xunit = parseDocument(xunitFile);

        assertThat(xunit, hasXPath("/testsuite[@failures='5']"));
        assertThat(xunit, hasXPath("/testsuite[@errors='0']"));
        assertThat(xunit, hasXPath("/testsuite[@tests='5']"));

        assertThat(xunit, hasXPath("//failure[@message = '1.0 != 2.0']"));
        assertThat(xunit, hasXPath("//failure[@message = '4.0 != 5.0']"));
        assertThat(xunit, hasXPath("//failure[@message = '5.0 != 6.0']"));
        assertThat(xunit, hasXPath("//failure[@message = '9.0 != 10.0']"));
        assertThat(xunit, hasXPath("//failure[@message = '11.0 != 12.0']"));

    }

    private Document parseDocument(File xunitFile)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
        Document xunit = builder.parse(xunitFile);
        return xunit;
    }

    public void testHsqlShouldPass()
            throws Exception {
        File pom = getTestFile("src/test/resources/pom-hsqldb.xml");
        AcceptanceTestMojo mojo = (AcceptanceTestMojo) lookupMojo("acceptance-test", pom);
        mojo.execute();

        File xunitFile = getTestFile("target/robotframework-reports/TEST-robot-hsqldb.xml");
        assertTrue("missing xunit test report " + xunitFile, xunitFile.exists());

        Document xunit = parseDocument(xunitFile);
        assertThat(xunit, hasXPath("/testsuite[@errors='0']"));
        assertThat(xunit, hasXPath("/testsuite[@failures='0']"));
    }

    public void testErrorSuiteNotFound()
            throws Exception {
        File pom = getTestFile("src/test/resources/pom-error.xml");
        AcceptanceTestMojo mojo = (AcceptanceTestMojo) lookupMojo("acceptance-test", pom);
        mojo.execute();
        File xunitFile = getTestFile("target/robotframework-reports/TEST-nothing-here.xml");
        assertTrue("missing xunit test report " + xunitFile, xunitFile.exists());

        Document xunit = parseDocument(xunitFile);
        assertThat(xunit, hasXPath("/testsuite[@errors='1']"));
        assertThat(xunit, hasXPath("/testsuite/testcase/error[@message = 'Invalid test data or command line options (Returncode 252).']"));
    }

    public void testErrorInTestCase() {
        // TODO assert that xunitfile is not overwritten, if available
    }


}
