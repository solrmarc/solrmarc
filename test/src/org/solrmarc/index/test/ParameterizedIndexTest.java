package org.solrmarc.index.test;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

import java.io.File;
import java.io.IOException;
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
    
    public ParameterizedIndexTest(String config, String recordFilename, String indexSpec, String expectedValue)
    {
        this.testNumber = ""+(cnt++);
        this.config = config;
        this.recordFilename = recordFilename;
        this.indexSpec = indexSpec;
        this.expectedValue = expectedValue;
    }
    
    private void initLogging(String[] homeDirs)
    {
        for (String dir : homeDirs)
        {
            File log4jProps = new File(dir, "log4j.properties");
            if (log4jProps.exists())
            {
                LogManager.resetConfiguration();
                PropertyConfigurator.configure(log4jProps.getAbsolutePath());
                return;
            }
        }
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
        ValueIndexerFactory vif = ValueIndexerFactory.initialize(new String[]{dataDirectory});
        initLogging(vif.getHomeDirs());
        Properties readerProps = setReaderProperties(vif, config);

        Record record = getRecord(vif, readerProps, recordFilename);

        MultiValueIndexer indexer = createIndexer(vif, indexSpec);
        
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


    private MultiValueIndexer createIndexer(ValueIndexerFactory vif, String indexSpec)
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
                if (!specFile.isAbsolute()) specFile = PropertyUtils.findFirstExistingFile(vif.getHomeDirs(), ixSpec);
                specFiles[i++] = specFile;
            }
            try
            {
                List<AbstractValueIndexer<?>> indexers = vif.createValueIndexers(specFiles);
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
                indexer = vif.createValueIndexer(indexname, fullSpec);
                return(indexer);
            }
        }
        return(null);
    }

    private Properties setReaderProperties(ValueIndexerFactory vif, String config) throws IOException
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
        String propertyFileAsURLStr = PropertyUtils.getPropertyFileAbsoluteURL(vif.getHomeDirs(), configFile, false, null);

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
    
    private Record getRecord(ValueIndexerFactory vif, Properties readerProps, String recordFilename)
    {
        String recordToLookAt = null;  // null means just get the first record from the named file
        if (recordFilename.matches("[^(]*[(][^)]*[)]"))
        {
            String recParts[] = recordFilename.split("[()]");
            recordFilename = recParts[0];
            recordToLookAt = recParts[1];
        }
        String fullPath = dataDirectory + File.separator + "records" + File.separator + recordFilename;
        MarcReader reader = MarcReaderFactory.instance().makeReader(readerProps, vif.getHomeDirs(), fullPath);

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
    public static Collection<String[]> indexValues() throws Exception
    {
        dataDirectory = System.getProperty("test.data.path");
        dataFile = System.getProperty("test.data.file");
        List<String[]> result = new LinkedList<String[]>();
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
                    "The princes of Hà-tiên (1682-1867) /"
                };
        result.add(testdata);
        return(result);
        /*
        String fullIndexTestFilename = dataDirectory + File.separator + dataFile;
        File file = new File(fullIndexTestFilename);
        BufferedReader rIn = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String line;
        List<String[]> result = new LinkedList<String[]>();
        int testNumber = 0;
        while (( line = rIn.readLine()) != null)
        {
        	if (line.startsWith("#") || line.trim().length() == 0) continue;
        	String split[];
        	if (line.matches(".*[(]rec.?[,].*[)].?,.*"))
        	{
        	    split = new String[4];
        	    String smallSplit[] = line.split(", ?",3);
        	    split[0] = smallSplit[0];
        	    split[1] = smallSplit[1];
        	    int index = smallSplit[2].indexOf("),");
        	    if (index != -1)
        	    {
                    split[2] = smallSplit[2].substring(0, index+1);
                    split[3] = smallSplit[2].substring(index+2).trim();
        	    }
        	}
        	else if (line.matches(".*[,][ ]*'.*[,].*['],.*"))
            {
                split = new String[4];
                String smallSplit[] = line.split(", ?",3);
                split[0] = smallSplit[0];
                split[1] = smallSplit[1];
                int index = smallSplit[2].indexOf("',");
                if (index != -1)
                {
                    split[2] = smallSplit[2].substring(0, index+1);
                    split[3] = smallSplit[2].substring(index+2).trim();
                }
            }
            else
                split = line.split(", ", 4);
            if (split.length == 4) 
            {
                String testParms[] = new String[5];
                System.arraycopy(split, 0, testParms, 1, 4);
                testParms[0] = "" + testNumber;
                testNumber++;
                result.add(testParms);
            }
        }
        rIn.close();
        return(result);
    */
    }
}
