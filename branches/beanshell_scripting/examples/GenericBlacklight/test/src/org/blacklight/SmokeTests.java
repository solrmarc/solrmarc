package org.blacklight;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.junit.*;

/**
 * junit4 tests for generic blacklight example
 * 
 * @author Naomi Dushay
 */
public class SmokeTests extends org.blacklight.DemoIxTest
{

    @Before
    public final void setup() throws ParserConfigurationException, IOException,
            SAXException
    {
        createIxInitVars("test_data.utf8.mrc");
    }

    /**
     * Test id field
     */
    @Test
    public final void testId() throws ParserConfigurationException,
            SAXException, IOException
    {
        String fldName = "id";
        assertStringFieldProperties(fldName);
        assertFieldNotMultiValued(fldName);
        assertFieldStored(fldName);
        assertFieldIndexed(fldName);

        assertDocPresent("00282214");

        assertSingleResult("00282214", fldName, "00282214");
    }

    /**
     * Hokey horrible way to create a test index.
     */
    // @Test
    public final void makeIndex() throws ParserConfigurationException,
            IOException, SAXException
    {
        createIxInitVars("unicornWHoldings.mrc");
    }

}
