package org.solrmarc.marc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class BooklistReader extends SolrReIndexer
{

    public BooklistReader(String properties, String[] args) throws IOException
    {
        super(properties, args);
        if (solrFieldContainingEncodedMarcRecord == null) solrFieldContainingEncodedMarcRecord = "marc_display";
    }
    
    public void readBooklist(String filename)
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
            String line;
            while ((line = reader.readLine()) != null)
            {
                String fields[] = line.split("\\|");
                Map<String, String> valuesToAdd = new LinkedHashMap<String, String>();
                valuesToAdd.put("fund_code_facet", fields[11]);
                valuesToAdd.put("date_received_facet", fields[0]);
                readAndUpdate("id", "u"+fields[9], valuesToAdd);
                commit(false);
            }
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
//    private Record lookup(String doc_id)
//    {
//        RefCounted<SolrIndexSearcher> rs = solrCore.getSearcher();
//        SolrIndexSearcher s = rs.get();
//        Term t = new Term("id", doc_id);
//        int docNo;
//        String marcRecord = null;
//        try
//        {
//            docNo = s.getFirstMatch(t);
//            if (docNo > 0)
//            {
//                Document doc = s.doc(docNo);
//                Field field = doc.getField("marc_display");
//                marcRecord = field.stringValue();
//            }
//            else
//            {
//                URL url = new URL("http://solrpowr.lib.virginia.edu:8080/solr/select/?q=id%3A"+doc_id+"&start=0&rows=1");
//                InputStream stream = url.openStream();
//                //The evaluate methods in the XPath and XPathExpression interfaces are used to parse an XML document with XPath expressions. The XPathFactory class is used to create an XPath object. Create an XPathFactory object with the static newInstance method of the XPathFactory class.
//
//                XPathFactory  factory = XPathFactory.newInstance();
//
//                // Create an XPath object from the XPathFactory object with the newXPath method.
//
//                XPath xPath = factory.newXPath();
//
//                // Create and compile an XPath expression with the compile method of the XPath object. As an example, select the title of the article with its date attribute set to January-2004. An attribute in an XPath expression is specified with an @ symbol. For further reference on XPath expressions, see the XPath specification for examples on creating an XPath expression.
//
//                XPathExpression  xPathExpression=
//                    xPath.compile("/response/result/doc/arr[@name='marc_display']/str");
//                
//                InputSource inputSource = new InputSource(stream);
//                marcRecord = xPathExpression.evaluate(inputSource);
//            }
//            
//            MarcXmlReader reader = new MarcXmlReader(new ByteArrayInputStream(marcRecord.getBytes("UTF8")));
//            if (reader.hasNext())
//            {
//                Record rec = reader.next();
//                return(rec);
//            }
//
//        }
//        catch (IOException e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        catch (XPathExpressionException e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return(null);
//    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        String properties = "import.properties";
        if(args.length > 0 && args[0].endsWith(".properties"))
        {
            properties = args[0];
            String newArgs[] = new String[args.length-1];
            System.arraycopy(args, 1, newArgs, 0, args.length-1);
            args = newArgs;
        }
        System.out.println("Loading properties from " + properties);
        
        BooklistReader reader = null;
        try
        {
            reader = new BooklistReader(properties, args);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        }
        reader.readBooklist("booklists.txt");
        
        reader.finish();

    }

}
