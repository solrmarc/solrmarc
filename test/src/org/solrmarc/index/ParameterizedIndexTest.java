package org.solrmarc.index;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;
import org.solrmarc.index.indexer.AbstractValueIndexer;
import org.solrmarc.index.indexer.MultiValueIndexer;
import org.solrmarc.index.indexer.ValueIndexerFactory;
import org.solrmarc.marc.MarcReaderFactory;
import org.solrmarc.tools.PropertyUtils;

@RunWith(Parameterized.class)
public class ParameterizedIndexTest
{
    static int cnt = 0;
    String testNumber;
    String config;
    String recordFilename;
    String indexSpec;
    String expectedValue;
    Pattern parameterParse = Pattern.compile("([^(]*)([(][^)]*[)])");
    Pattern indexSpecParse = Pattern.compile("(([^,]*[.]properties)([ ]*,[ ]*(.*[.]properties))*)[(]([^)]*)[)]");
    Pattern singleSpecParse = Pattern.compile("(([-A-Za-z0-9_]*)[ ]*=[ ]*)?(.*)");
    static String dataDirectory;
    static String dataFile;
    private static ValueIndexerFactory factory;
    static
    {
        factory = ValueIndexerFactory.initialize(new String[]{System.getProperty("test.data.dir", "test/data")});
    }
    
    public ParameterizedIndexTest(String config, String recordFilename, String indexSpec, String expectedValue)
    {
        this.testNumber = ""+(cnt++);
        this.config = config;
        this.recordFilename = recordFilename;
        this.indexSpec = indexSpec;
        this.expectedValue = expectedValue;
    }
    
    
    @Test
    /**
     * for each line specified in the test file 
     *    (see org.solrmarc.index.indexValues javadoc below)
     * run the indicated test data file through MarcMappingOnly, which will get the
     * marc to solr mappings specified in yourSite_index.properties file, and
     * look for the indicated solr field value in the indicated solr field.
     */
    public void verifyIndexingResults() throws Exception 
    {
        boolean ordered = false;

        Properties readerProps = setReaderProperties(factory, config);

        Record record = getRecord(factory, readerProps, recordFilename);

        MultiValueIndexer indexer = createIndexer(factory, indexSpec);
        
        Collection<String> result = indexer.getFieldData(record);
        
        String expected[];
        if (expectedValue.startsWith("*ordered*"))
        {
            ordered = true;
            expectedValue = expectedValue.substring(9).trim();
        }
        if (expectedValue.length() > 0)
            expected = expectedValue.split("[|]");
        else
            expected = new String[0];
        if (ordered) 
            assertThat(result, contains(expected));
        else 
            assertThat(result, containsInAnyOrder(expected));
        System.out.println("Test " + testNumber + " : " + config + " : " + recordFilename + " : " + indexSpec + " --> " + expectedValue);
    }


    private MultiValueIndexer createIndexer(ValueIndexerFactory factory, String indexSpec)
    {
        MultiValueIndexer indexer = null;
        Matcher indexSpecMatcher = indexSpecParse.matcher(indexSpec);
        if (indexSpecMatcher.matches())
        {
            String indexSpecFile = indexSpecMatcher.group(1);
            String specName = indexSpecMatcher.group(5);
            String[] indexSpecs = indexSpecFile.split("[ ]*,[ ]*");
            File[] specFiles = new File[indexSpecs.length];
            int i = 0;
            for (String ixSpec : indexSpecs)
            {
                File specFile = new File(indexSpec);
                if (!specFile.isAbsolute()) specFile = PropertyUtils.findFirstExistingFile(factory.getHomeDirs(), ixSpec);
                specFiles[i++] = specFile;
            }
            try
            {
                List<AbstractValueIndexer<?>> indexers = factory.createValueIndexers(specFiles);
                for (AbstractValueIndexer<?> ix : indexers)
                {
                    for (String fn : ix.getSolrFieldNames())
                    {
                        if (fn.equals(specName))
                        {
                            indexer = (MultiValueIndexer) ix;
                        }
                    }
                }
            }
            catch (IllegalAccessException | InstantiationException | IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return(indexer);
        }
        else // its a single spec
        {
            Matcher singleSpecMatcher = singleSpecParse.matcher(indexSpec);
            if (singleSpecMatcher.matches())
            {
                String indexname = singleSpecMatcher.group(2);
                String fullSpec = singleSpecMatcher.group(3);
                if (indexname == null || indexname.length() == 0)
                {
                    indexname = "test_"+testNumber;
                }
                indexer = factory.createValueIndexer(indexname, fullSpec);
                return(indexer);
            }
        }
        return(null);
    }

    private Properties setReaderProperties(ValueIndexerFactory factory, String config) throws IOException
    {
        Matcher configMatcher = parameterParse.matcher(config);
        String configFile;
        String configAdditionStr = null;
        if (config.length() == 0) 
        {
            configFile = "marcreader.properties";
        }
        else if (config.matches("[-A-Za-z0-9_.]*[ ]*=.*"))
        {
            configFile = "marcreader.properties";
            configAdditionStr = config;
        }
        else if (configMatcher.matches())
        {
            configFile = configMatcher.group(1);
            if (configFile.length() == 0) 
            {
                configFile = "marcreader.properties";
            }
            configAdditionStr = configMatcher.group(2);
        }
        else 
        {
            configFile = config;
        }
        if (configAdditionStr == null) configAdditionStr = "";
        String[] configAdditions = configAdditionStr.split(",");
        
        Properties readerProps = new Properties();
        String propertyFileAsURLStr = PropertyUtils.getPropertyFileAbsoluteURL(factory.getHomeDirs(), configFile, false, null);

        readerProps.load(PropertyUtils.getPropertyFileInputStream(propertyFileAsURLStr));
        
        for (String configAddition : configAdditions)
        {
            String[] propParts = configAddition.split("=");
            if (propParts.length == 2)
            {
                readerProps.setProperty(propParts[0].trim(), propParts[1].trim());
            }
        }
        return readerProps;
    }
    
    private Record getRecord(ValueIndexerFactory factory, Properties readerProps, String recordFilename)
    {
        String recordToLookAt = null;  // null means just get the first record from the named file
        if (recordFilename.matches("[^(]*[(][^)]*[)]"))
        {
            String recParts[] = recordFilename.split("[()]");
            recordFilename = recParts[0];
            recordToLookAt = recParts[1];
        }
        String fullPath = dataDirectory + File.separator + "records" + File.separator + recordFilename;
        MarcReader reader = MarcReaderFactory.instance().makeReader(readerProps, factory.getHomeDirs(), fullPath);

        Record record = null;
        while (reader.hasNext())
        {
            record = reader.next();
            if (recordToLookAt == null || record.getControlNumber().equals(recordToLookAt))
                break;
        }
        return record;
    }

    @Parameters
    /**
     * reads in the file at test.data.path/test.data.file (usually 
     *    test.data.path = yourSiteDirectory/test/data
     *    test.data.file = indextest.txt
	 *   and puts the tests indicated there into a collection of arrays, where 
	 *   each item in the collection has this structure:
     *     it[0] = sequentially increasing ordinal number of test
     *     it[1] = config.properties file  or +  indicating to simply use the default config values
     *     it[2] = name of file containing marc records to be indexed for test
     *     it[3] = name of solr field to be checked in resulting solr doc
     *     it[4] = value expected in solr field
     */
    public static Collection<String[]> indexValues()
    {
        dataDirectory = System.getProperty("test.data.dir", "test/data");
        dataFile = System.getProperty("test.data.file",  "indextest.txt");
        String fullIndexTestFilename = dataDirectory + File.separator + dataFile;
        File file = new File(fullIndexTestFilename);
        List<String[]> result = new LinkedList<String[]>();
        BufferedReader rIn = null;
        try
        {
            rIn = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
        }
        catch (FileNotFoundException e)
        {
            dataFile = null;
        }
        if (dataFile != null)
        {
            try {
                String line;
                int testNumber = 0;
                String[] testdata = new String[]{"","","",""};
                while (( line = rIn.readLine()) != null)
                {
                	if (line.startsWith("#") || line.trim().length() == 0) continue;
                	String split[];
                    if (line.startsWith("readerProps:"))
                    {
                        testdata[0] = line.substring("readerProps:".length()).trim();
                    }
                    else if (line.startsWith("record:"))
                    {
                        testdata[1] = line.substring("record:".length()).trim();
                    }
                    else if (line.startsWith("indexSpec:"))
                    {
                        testdata[2] = line.substring("indexSpec:".length()).trim();
                    }
                    else if (line.startsWith("expect:"))
                    {
                        testdata[3] = line.substring("expect:".length()).trim();
                        result.add(testdata);
                        testdata = new String[]{"","","",""};
                    }
                }
                rIn.close();
            }
            catch(IOException ioe)
            {}
            
            return(result);
        }
        String[] testdata = new String[] 
            {
                "",
                "specTestRecs.mrc(u8)",
                "id = 001, first",
                "u8"
            };
        result.add(testdata);
        testdata = new String[] 
                {
                    "",
                    "1156470.mrc",
                    "035a, map(\"[(][Oo][Cc][Oo][Ll][Cc][)][^0-9]*[0]*([0-9]+)=>$1\",\"ocm[0]*([0-9]+)[ ]*[0-9]*=>$1\",\"ocn[0]*([0-9]+).*=>$1\", \"on[0]*([0-9]+).*=>$1\")",
                    "12275114"
                };
        result.add(testdata);
        testdata = new String[] 
                {
                    "",
                    "1156470.mrc",
                    "oclc_pattern_map_test.properties(oclc_num)",
                    "12275114"
                };
        result.add(testdata);
        testdata = new String[] 
                {
                    "",
                    "1156470.mrc",
                    "oclc_p_num = 035a, oclc_num_pattern_map.properties(oclc_num)",
                    "12275114"
                };
        result.add(testdata);
        testdata = new String[] 
                {
                    "",
                    "specTestRecs.xml",
                    "subject_facet =600[a-z]:610[a-z]:611[a-z]:630[a-z]:650[a-z]:651[a-z]:655[a-z]:690[a-z], join (\" -- \"), cleanEnd, unique",
                    "Translating and interpreting -- Soviet Union -- History|Russian literature -- Translations from foreign languages -- History and criticism"

                };
        result.add(testdata);
        testdata = new String[] 
                {
                    "",
                    "title_k.mrc",
                    "custom, getSortableTitle",
                    "morton hoffman papers"
                };
        result.add(testdata);
        testdata = new String[] 
                {
                    "marc.permissive=true,marc.to_utf_8=true,marc.unicode_normalize=C", 
                    "u4.mrc(u4)", 
                    "245a", 
                 //   "The princes of Hà-tiên (1682-1867) /"
                    "The princes of H\u00E0-ti\u00EAn (1682-1867) /"
                };
        result.add(testdata);
        testdata = new String[] 
                {
                    "marc.permissive=true,marc.to_utf_8=true,marc.unicode_normalize=false", 
                    "u4.mrc(u4)", 
                    "245a", 
                 //   "The princes of Hà-tiên (1682-1867) /"
                    "The princes of Ha\u0300-tie\u0302n (1682-1867) /"
                };
        
        result.add(testdata);
        return(result);

    }
}
