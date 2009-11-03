package org.solrmarc.solr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Future;

import org.marc4j.MarcException;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.Record;

public class RemoteSolrSearcher
{
    Object solrSearcher = null;
    String solrBaseURL;
    String solrFieldContainingEncodedMarcRecord;
    MarcStreamWriter output;
    String query;
    
    public RemoteSolrSearcher(String solrBaseURL, String query, String solrFieldContainingEncodedMarcRecord)
    {
//      refedSolrSearcher = solrCore.getSearcher();
//      solrSearcher = refedSolrSearcher.get();
        this.solrBaseURL = solrBaseURL;  
        this.solrFieldContainingEncodedMarcRecord = solrFieldContainingEncodedMarcRecord;
        this.query = query;
    }
    
    public int handleAll()
    {
        output = new MarcStreamWriter(System.out, "UTF8");
        if (solrFieldContainingEncodedMarcRecord == null) solrFieldContainingEncodedMarcRecord = "marc_display";
        /*String queryparts[] = query.split(":");
        if (queryparts.length != 2) 
        {
            //System.err.println("Error query must be of the form    field:term");
            System.out.println("Error: query must be of the form    field:term  " + query);
            return 0;
        }*/
        String encQuery;
        try
        {
            encQuery = java.net.URLEncoder.encode(query, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            encQuery = query;
        }
        String resultSet[] = getIdSet(encQuery);
        String recordStr = null;
        for (String id : resultSet)
        {
            recordStr = getFieldFromDocumentGivenDocID(id, solrFieldContainingEncodedMarcRecord);
            Record record = null;
            if (recordStr.startsWith("<?xml version"))
            {
                record = getRecordFromXMLString(recordStr);            
            }
            else
            {
                record = getRecordFromRawMarc(recordStr);
            }
            if (record != null)  
            {
                output.write(record);
                System.out.flush();
            }
        }
        output.close();
        return 0;
    }
   
    private String getFieldFromDocumentGivenDocID(String id, String solrFieldContainingEncodedMarcRecord2)
    {
        String fullURLStr = solrBaseURL + "/select/?q=id%3A"+id+"&wt=json&indent=on&fl="+solrFieldContainingEncodedMarcRecord2;
        URL fullURL = null;
        try
        {
            fullURL = new URL(fullURLStr);
        }
        catch (MalformedURLException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        BufferedReader sIn = null;
        try
        {
            sIn = new BufferedReader( new InputStreamReader(fullURL.openStream(), "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String line;
        int numFound = 0;
        String result = null;
        int count = 0;
        try
        {
            while ((line = sIn.readLine()) != null)
            {
                if (line.contains("\"<?xml version"))
                {
                    result = line.replaceFirst(".*<\\?xml", "<?xml");
                    result = result.replaceFirst("</collection>.*", "</collection>");
                    result = result.replaceAll("\\\\\"", "\"");
                }
                else
                {
                    continue;
                }
            }
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return(result);
    }

    public String[] getIdSet(String query) 
    {
        String fullURLStr = solrBaseURL + "/select/?q="+query+"&wt=json&indent=on&fl=id&start=0&rows=10000";
        URL fullURL = null;
        try
        {
            fullURL = new URL(fullURLStr);
        }
        catch (MalformedURLException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        BufferedReader sIn = null;
        try
        {
            sIn = new BufferedReader( new InputStreamReader(fullURL.openStream(), "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String line;
        int numFound = 0;
        String resultSet[] = null;
        int count = 0;
        try
        {
            while ((line = sIn.readLine()) != null)
            {
                if (line.contains("\"numFound\""))
                {
                    String numFoundStr = line.replaceFirst(".*numFound[^0-9]*([0-9]*).*", "$1");
                    numFound = Integer.parseInt(numFoundStr);
                    resultSet = new String[numFound];
                }
                else if (line.contains("\"id\":[")) 
                {
                    String id = line.replaceFirst(".*:.\"([-A-Za-z0-9_]*).*", "$1");
                    resultSet[count++] = id;
                }
            }
        }
        catch (NumberFormatException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return(resultSet);
    }
    /**
     * Extract the marc record from binary marc
     * @param marcRecordStr
     * @return
     */
    private Record getRecordFromRawMarc(String marcRecordStr)
    {
        MarcStreamReader reader;
        boolean tryAgain = false;
        do {
            try {
                tryAgain = false;
                reader = new MarcStreamReader(new ByteArrayInputStream(marcRecordStr.getBytes("UTF8")));
                if (reader.hasNext())
                {
                    Record record = reader.next(); 
                    return(record);
                }
            }
            catch( MarcException me)
            {
                me.printStackTrace();
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
        } while (tryAgain);
        return(null);
    }
    
    // error output
    static BufferedWriter errOut = null;
    
    /**
     * Extract marc record from MarcXML
     * @param marcRecordStr MarcXML string
     * @return marc4j Record
     */
    public Record getRecordFromXMLString(String marcRecordStr)
    {
        MarcXmlReader reader;
        boolean tryAgain = false;
        do {
            try {
                tryAgain = false;
                reader = new MarcXmlReader(new ByteArrayInputStream(marcRecordStr.getBytes("UTF8")));
                if (reader.hasNext())
                {
                    Record record = reader.next(); 
//                    if (verbose)
//                    {
//                        System.out.println(record.toString());
//                        System.out.flush();
//                    }
                    return(record);
                }
            }
            catch( MarcException me)
            {
                try
                {
                    errOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("badRecs.xml"))));
                    errOut.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><collection xmlns=\"http://www.loc.gov/MARC21/slim\">");
                }
                catch (FileNotFoundException e)
                {
                    // e.printStackTrace();
                    System.err.println(e.getMessage());
                }
                catch (IOException e)
                {
                    // e.printStackTrace();
                    System.err.println(e.getMessage());
                }
                String trimmed = marcRecordStr.substring(marcRecordStr.indexOf("<record>"));
                trimmed = trimmed.replaceFirst("</collection>", "");
                trimmed = trimmed.replaceAll("><", ">\n<");
                try
                {
                    errOut.write(trimmed);
                }
                catch (IOException e)
                {
                    // e.printStackTrace();
                    System.err.println(e.getMessage());
                }
                if (marcRecordStr.contains("<subfield code=\"&#31;\">"))
                {
                    // rewrite input string and try again.
                    marcRecordStr = marcRecordStr.replaceAll("<subfield code=\"&#31;\">(.)", "<subfield code=\"$1\">");
                    tryAgain = true;
                }
                else if (extractLeader(marcRecordStr).contains("&#")) //.("<leader>[^<>&]*&#[0-9]+;[^<>&]*</leader>"))
                {
                    // rewrite input string and try again.
                    // 07585nam a2200301 a 4500
                    String leader = extractLeader(marcRecordStr).replaceAll("&#[0-9]+;", "0");
                    marcRecordStr = marcRecordStr.replaceAll("<leader>[^<]*</leader>", leader);
                    tryAgain = true;
                }
                else
                {
                    me.printStackTrace();
                    //System.out.println("The bad record is: "+ marcRecordStr);
                    System.err.println("The bad record is: "+ marcRecordStr);
                }
            }
            catch (UnsupportedEncodingException e)
            {
                // e.printStackTrace();
                System.err.println(e.getMessage());
            }
        } while (tryAgain);
        return(null);

    }
        
 
    /**
     * Extract the leader from the marc record string
     * @param marcRecordStr marc record as a String
     * @return Leader leader string for the marc record
     */
    private String extractLeader(String marcRecordStr)
    {
        final String leadertag1 = "<leader>";
        final String leadertag2 = "</leader>";
        String leader = null;
        try {
            leader = marcRecordStr.substring(marcRecordStr.indexOf(leadertag1), marcRecordStr.indexOf(leadertag2)+leadertag2.length() );
        }
        catch (IndexOutOfBoundsException e)
        {}
        return leader;
    }

    public static void main(String args[])
    {
        String baseURLStr = "http://localhost:8983/solr";
        String query = null;
        String field = "marc_display";
        for (int i = 0; i < args.length; i++)
        {
            if (args[i].startsWith("http")) baseURLStr = args[i];
            else if (args[i].contains(":")) query = args[i];
            else field = args[i];
        }
        RemoteSolrSearcher searcher = new RemoteSolrSearcher(baseURLStr, query, field);
        searcher.handleAll();
        
    }
}
