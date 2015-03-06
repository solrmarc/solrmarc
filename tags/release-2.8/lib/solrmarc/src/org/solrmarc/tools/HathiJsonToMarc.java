package org.solrmarc.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.marc4j.MarcException;
import org.marc4j.MarcReader;
import org.marc4j.MarcWriter;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.util.JsonParser;
import org.xml.sax.InputSource;


public class HathiJsonToMarc implements MarcReader
{
    static MarcFactory mf = null;

    JsonParser parser;
    MarcReader reader = null;
    PipedWriter toMarcXML = null;
    PipedReader usedInMarcXML = null;
    Record nextRecord = null;
    private int parserCode;
    boolean add856 = false;

    public HathiJsonToMarc(Reader in)
    {
        this(in, false);
    }
    
    public HathiJsonToMarc(Reader in, boolean add856)
    {
        this.add856 = add856;
        if (mf == null) 
        {
            if(System.getProperty("org.marc4j.marc.MarcFactory") == null)
            {
                System.setProperty("org.marc4j.marc.MarcFactory", "org.marc4j.marc.impl.NoSortMarcFactoryImpl");
            }
            mf = MarcFactory.newInstance();
        }
        parser = new JsonParser(0);
        parser.setInput("input", in, false);
        toMarcXML = new PipedWriter();
        try
        {
            usedInMarcXML = new PipedReader(toMarcXML);
            reader = new MarcXmlReader(new InputSource(usedInMarcXML));
            toMarcXML.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><collection xmlns=\"http://www.loc.gov/MARC21/slim\">");
        }
        catch (IOException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
    
    public HathiJsonToMarc(InputStream in)
    {
        this(in, false);
    }
    
    public HathiJsonToMarc(InputStream in, boolean add856)
    {
        this.add856 = add856;
        if (mf == null) 
        {
            if(System.getProperty("org.marc4j.marc.MarcFactory") == null)
            {
                System.setProperty("org.marc4j.marc.MarcFactory", "org.marc4j.marc.impl.NoSortMarcFactoryImpl");
            }
            mf = MarcFactory.newInstance();
        }
        parser = new JsonParser(0);
        parser.setInput("input", in, "UTF8", false);
        toMarcXML = new PipedWriter();
        try
        {
            usedInMarcXML = new PipedReader(toMarcXML);
            reader = new MarcXmlReader(new InputSource(usedInMarcXML));
            toMarcXML.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><collection xmlns=\"http://www.loc.gov/MARC21/slim\">");
        }
        catch (IOException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
    
//    public HathiJsonToMarc(Reader in, boolean rawXML)
//    {
//        
//        if (mf == null) 
//        {
//            if(System.getProperty("org.marc4j.marc.MarcFactory") == null)
//            {
//                System.setProperty("org.marc4j.marc.MarcFactory", "org.marc4j.marc.impl.NoSortMarcFactoryImpl");
//            }
//            mf = MarcFactory.newInstance();
//        }
//        parser = new JsonParser(0);
//        parser.setInput("input", in, false);
//    //    toMarcXML = System.out;
//        try
//        {
//            PrintStream out = new PrintStream(System.out, true, "UTF-8");
//            System.setOut(out);
//            System.out.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?><collection xmlns=\"http://www.loc.gov/MARC21/slim\">");
//        }
//        catch (UnsupportedEncodingException e1)
//        {
//            // TODO Auto-generated catch block
//            e1.printStackTrace();
//        }
//    }
    
    public boolean hasNext()
    {
        parserCode = parser.getEventCode();
        while (nextRecord == null && parserCode != JsonParser.EVT_INPUT_ENDED)
        {
            if (parserCode == 0 || parserCode == JsonParser.EVT_OBJECT_ENDED)
                parserCode = parser.next();
            else if (parserCode == JsonParser.EVT_OBJECT_BEGIN) 
            {
                nextRecord = next();
            }
        }
        if (nextRecord != null)
        {
            return(true);
        }
        if (parserCode == JsonParser.EVT_INPUT_ENDED)  
        {
            if (toMarcXML != null)
            {
                try
                {
                    toMarcXML.write("</collection>");
                    toMarcXML.flush();
                    toMarcXML.close();
                    toMarcXML = null;
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            else 
            {
                System.out.println("</collection>");
            }
            return(false);
        }
        throw new MarcException("Malformed JSON input");
    }
    
    public Record next()
    {
        if (nextRecord != null)
        {
            Record tmpRecord = nextRecord;
            nextRecord = null;
            return(tmpRecord);
        }
        int arrlevel = 0;
        int level = 0;
        String value = null;
        String mname = null;
        Record curRecord = null;
        String mnameStack[] = new String[20];
         
        do {
            mname = parser.getMemberName();
            if (parserCode == JsonParser.EVT_OBJECT_BEGIN)
            {
                mnameStack[level++] = mname;
                
          //      if (level == 3) id = mname;
//                  if (items != null && itemdata == null && mname.equals("items"))
//                  {
//                  itemdata = new LinkedHashMap<String, String>();
//                  }
            }
            else if (parserCode == JsonParser.EVT_OBJECT_ENDED)
            {
                if (level > 0) 
                {
                    level--;
                    if (mnameStack[level+1] != null && mnameStack[level+1].equals("items"))  
                    {
                        if (toMarcXML != null)
                        {
                            return(curRecord);
                        }
                        else 
                        {
                            return null;
                        }
                    }
                }
//                  if (items != null && itemdata != null)
//                  {
//                  String enumcron = itemdata.get("enumcron");
//                  if (itemdata.get("usRightsString") != null && itemdata.get("usRightsString").equals("Full view"))
//                  {
//                  while (items.get(enumcron) != null)
//                  {
//                  enumcron = enumcron + " ";
//                  }
//                  items.put(enumcron, itemdata);
//                  itemdata = null;
//                  }
//                  }
            }
            else if (parserCode == JsonParser.EVT_ARRAY_BEGIN)
            {
                arrlevel ++;
//                  if (mname.equals("items"))
//                  {
//                  items = new TreeMap<String, Map<String, String>>(new StringNaturalCompare());
//                  }
            }
            else if (parserCode == JsonParser.EVT_ARRAY_ENDED)
            {
                arrlevel --;
//                  if (mname.equals("items"))
//                  {
//                  AddHoldingsToRecord(curRecord, items);
//                  items = null;
//                  }
            }
            else if (parserCode == JsonParser.EVT_OBJECT_MEMBER)
            {
                mname = parser.getMemberName();
                value = parser.getMemberValue();
                if (JsonParser.isQuoted(value))
                {
                    value = JsonParser.stripQuotes(value);
                }
                value = value.replaceAll("⁄", "/");
                if (mname.equals("marc-xml"))
                {
                    String marcxml = value.replaceFirst("<[?][^?]*[?]><collection[^>]*>", "");
                    marcxml = marcxml.replaceFirst("</collection>", "");
                    try
                    {
                        if (toMarcXML != null)
                        {
                            toMarcXML.write(marcxml);
                            toMarcXML.flush();
                        }
                        else
                            System.out.print(marcxml);
                    }
                    catch (IOException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (reader != null && reader.hasNext())
                    {
                        curRecord = reader.next();
                        fix880field(curRecord);
                        if (add856) make856fields(curRecord);
                    }
                }
//                  if (items != null && itemdata != null)
//                  {
//                  if (JsonParser.isQuoted(value))
//                  value = JsonParser.stripQuotes(value);
//                  itemdata.put(mname, value);
//                  }
            }
            parserCode = parser.next();
            
        } while (parserCode != JsonParser.EVT_INPUT_ENDED);
        return(null);
    }

    static Comparator compare = new StringNaturalCompare();

    private static void make856fields(Record curRecord)
    {
        List<VariableField> dfs = (List<VariableField>)curRecord.getVariableFields("974"); 
        Map<String, String> sortedMap = new TreeMap<String, String>(compare);
        for (VariableField dfv : dfs)
        {
            DataField df = (DataField)dfv;
            Subfield rights = null;
            if ((rights = df.getSubfield('r')) != null && (rights.getData().startsWith("pd") || rights.getData().equals("world")))
            {
                Subfield labelField = df.getSubfield('z');
                Subfield identField = df.getSubfield('u');
                if (identField == null) continue;
                String label = (labelField != null) ? labelField.getData() : "";
                String sortlabel = (labelField != null) ? labelField.getData() : identField.getData();
                // default URL prefix is   http://hdl.handle.net/2027/
                String value = "http://hdl.handle.net/2027/" + identField.getData().trim() + "||" + rights.getData() + "||" + label;
                sortedMap.put(sortlabel, value);
            }
        }
        for (String key : sortedMap.keySet())
        {
            String value = sortedMap.get(key);
            DataField newdf = mf.newDataField("856", '4', ' ');
            String parts[] = value.split("[|][|]");
            Subfield sfu = mf.newSubfield('u', parts[0]);
            Subfield sfr = mf.newSubfield('r', parts[1]);
            Subfield sfz = parts.length > 2 ? mf.newSubfield('z', parts[2]) : null;
            newdf.addSubfield(sfu);
            newdf.addSubfield(sfr);
            if (sfz != null) newdf.addSubfield(sfz);
            curRecord.addVariableField(newdf);
        }
    }
    
    
    private static void fix880field(Record curRecord)
    {
        List<DataField> dfs = (List<DataField>)curRecord.getDataFields(); 
        List<DataField> todelete = new ArrayList<DataField>();
        for (DataField df : dfs)
        {
            Subfield sf = null;
            if (!df.getTag().equals("880") && (sf = df.getSubfield('6')) != null)
            {
                Subfield sfother = null;
                for (DataField dfother : dfs)
                {
                    if (dfother != df && !dfother.getTag().equals("880") && (sfother = dfother.getSubfield('6')) != null &&
                            sfother.getData().equals(sf.getData()))
                    {
                        int which = hasOtherScript(df, dfother);
                        if (which == 2)
                        {
                            retag(df, dfother);
                        }
                        else if (which == 1)
                        {
                            retag(dfother, df);
                        }
                        else // duplicate ? -- delete it
                        {
                            //mark for deletion
                            todelete.add(dfother); 
                        }
                        break;
                    }
                }
            }
        }
        for (DataField del : todelete)
        {
            curRecord.removeVariableField(del);
        }
    }

    private static void retag(DataField df, DataField dfother)
    {
        Subfield sf6 = df.getSubfield('6');
        sf6.setData("880-" + sf6.getData());

        sf6 = dfother.getSubfield('6');
        sf6.setData(df.getTag() + "-" + sf6.getData());
        dfother.setTag("880");
    }

    private static int hasOtherScript(DataField df1, DataField df2)
    {
        int sum1 = 0, sum2 = 0;
        int count1 = 0, count2 = 0;
        for (Subfield sf : (List<Subfield>)df1.getSubfields())
        {
            for (char c : sf.getData().toCharArray())
            {
                sum1 += (int)c;
                if ((int)c > 0x7f) count1++;
            }
        }
        for (Subfield sf : (List<Subfield>)df2.getSubfields())
        {
            for (char c : sf.getData().toCharArray())
            {
                sum2 += (int)c;
                if ((int)c > 0x7f) count2++;
            }
        }
        if (sum1 > sum2 && count1 > count2) return(1);
        else if (sum1 < sum2 && count1 < count2) return(2);
        else if (sum1 == sum2 && count1 > count2) return(1);
        else if (sum1 == sum2 && count1 < count2) return(2);
        else if (count1 > count2) return(1);
        else if (count1 < count2) return(2);
        else return(0);
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        boolean debug = false;
        boolean rawXML = false;
        BufferedReader in = null;
        MarcWriter writer = new MarcStreamWriter(System.out, "UTF-8", true);
        while (args.length >= 1 && args[0].startsWith("-"))
        {
            if (args[0].equals("-d"))
            {
                debug = true;
            }
            else if (args[0].equals("-x"))
            {
                rawXML = true;
            }
            String newArgs[] = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, args.length-1);
            args = newArgs;
        }
        
        if (args.length == 1) 
        {
            try
            {
                in = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
 //               name = args[0];
            }
            catch (FileNotFoundException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else if (args.length > 1)
        {
            Vector<InputStream> inputs = new Vector<InputStream>();
            for (String arg : args)
            {
                File file = new File(arg);
                if (file.exists() && file.canRead())
                {
                    InputStream instr = null;
                    try
                    {
                        instr = new FileInputStream(file);
                        inputs.add(instr);
                    }
                    catch (FileNotFoundException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            in = new BufferedReader(new InputStreamReader(new SequenceInputStream(inputs.elements())));
        }
        else
        {
            in = new BufferedReader(new InputStreamReader(System.in));
        }
        
        HathiJsonToMarc marcreader = null;
        if (rawXML)
        {
            marcreader =  new HathiJsonToMarc(in, true); 
            while (marcreader.hasNext())
            {
                Record rec = marcreader.next();
                System.out.println();
                //if (debug)
                    //System.out.println(rec.toString());
                //else
                   //writer.write(rec);
            }

        }
        else 
        {
            marcreader = new HathiJsonToMarc(in); 
        
            while (marcreader.hasNext())
            {
                Record rec = marcreader.next();
                if (debug)
                    System.out.println(rec.toString());
                else
                    writer.write(rec);
            }
            writer.close();
        }
    }

//        private static void AddHoldingsToRecord(Record curRecord, TreeMap<String, Map<String, String>> items)
//        {
//            for (String key : items.keySet())
//            {
//                MarcFactory mf = MarcFactory.newInstance();
//                DataField df = mf.newDataField("999", ' ', ' ');
//                Map<String, String> values = items.get(key);
//                for (String subkey : values.keySet())
//                {
//                    Subfield sf = mf.newSubfield(subkey.charAt(0), values.get(subkey));
//                    df.addSubfield(sf);
//                }
//                curRecord.addVariableField(df);
//            }
//            
//        }

}

