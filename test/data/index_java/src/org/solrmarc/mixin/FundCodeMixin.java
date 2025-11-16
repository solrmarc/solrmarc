package org.solrmarc.mixin;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.marc4j.marc.Record;
import org.solrmarc.index.SolrIndexer;
import org.solrmarc.index.SolrIndexerMixin;
import org.solrmarc.index.indexer.IndexerSpecException;
import org.solrmarc.tools.Utils;


public class FundCodeMixin extends SolrIndexerMixin
{
    Map<String, String[]> booklistData = null;
    
    public String getDateReceived(Record rec, String urlList)
    {
        if (booklistData == null)
        {
            readBooklistData(urlList);
        }
        String docID = rec.getControlNumber();
        if (!booklistData.containsKey(docID))
        {
            return(null);
        }
        else
        {
            String result = null;
            String data[] = booklistData.get(docID);
            for (int i = 0; i < data.length; i += 2)
            {
                if (result == null) 
                    result = data[i];
                else if (result.compareTo(data[i]) < 0)
                    result = data[i];
            }
            return(result);
        }
    }
    
    public Set<String> getFundCode(Record rec, String urlList)
    {
        Set<String> result = new LinkedHashSet<String>();
        if (booklistData == null)
        {
            readBooklistData(urlList);
        }
        String docID = rec.getControlNumber();
        if (!booklistData.containsKey(docID))
        {
            return(result);
        }
        else
        {
            String data[] = booklistData.get(docID);
            for (int i = 0; i < data.length; i += 2)
            {
                if (data[i+1].length() != 0)
                    result.add(data[i+1]);
            }
        }
        return(result);
    }
    
    public Set<String> getBookPlateURL(Record rec, String urlList, String translationMapProps) throws Exception
    {
        String mapName = SolrIndexer.instance().loadTranslationMap(translationMapProps);
        Object translationMap = SolrIndexer.instance().findMap(mapName);
        Set<String> fundcodes = this.getFundCode(rec, urlList);
        Set<String> result = new LinkedHashSet<String>();
        if (fundcodes.size() == 0)
        {
            return(result);
        }
        for (String fundcode : fundcodes)
        {
            String fundcodeURL = SolrIndexer.instance().remap(fundcode, translationMap, false);
            if (fundcodeURL != null)
            {
                result.add(fundcodeURL);
            }
        }
        return(result);
    }
    

    private void readBooklistData(String urlList)
    {
        String urls[] = urlList.split("[|]");
        booklistData = new LinkedHashMap<String, String[]>();
        InputStreamReader input;
        for (String urlstr : urls)
        {
            try{
                if (urlstr.startsWith("http:"))
                {
                    URL url = new URL(urlstr);
                    URLConnection conn = url.openConnection();
                    input = new InputStreamReader(conn.getInputStream());
                }
                else
                {
                    continue;
                }
                BufferedReader reader = new BufferedReader(input);
                String line;
                Date today = new Date();
                while ((line = reader.readLine()) != null)
                {
                    String fields[] = line.split("\\|");
                    // discard bad data, ie.  something that was received at some date in the future
                    DateFormat format = new SimpleDateFormat("yyyyMMdd");
                    Date dateReceived = format.parse(fields[0], new ParsePosition(0));
                    if (dateReceived.after(today)) continue;
                    
                    String docID = "u"+fields[9];
                    String dateAndFundcode[] = new String[]{fields[0], fields[11]};
                    if (booklistData.containsKey(docID))
                    {
                        String[] data = booklistData.get(docID);
                        boolean dataexists = false;
                        for (int i = 0; i < data.length; i += 2)
                        {
                            if (data[i].equals(dateAndFundcode[0]) && data[i+1].equals(dateAndFundcode[1]))
                            {
                                dataexists = true;
                                break;
                            }
                        }       
                        if (!dataexists)
                        {
                            String combinedData[] = new String[data.length + 2];
                            System.arraycopy(data, 0, combinedData, 0, data.length);
                            System.arraycopy(dateAndFundcode, 0, combinedData, data.length, 2);
                            booklistData.put(docID, combinedData);
                        }
                    }
                    else
                    {
                        booklistData.put(docID, dateAndFundcode);
                    }
                }
            }
            catch (FileNotFoundException e)
            {
                throw new IndexerSpecException(e, "Cannot find resource at URL: "+urlstr);
            }
            catch (IOException e)
            {
                throw new IndexerSpecException(e, "Error reading resource at URL: "+urlstr);
            }
        }
    }
    
}
