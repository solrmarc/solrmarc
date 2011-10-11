package org.solrmarc.tools;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.marc4j.MarcStreamWriter;
import org.marc4j.marc.Record;

public class HathiPlunderer extends InputStream
{
    private static boolean debug = false;
    private static boolean print = false;
    private static boolean add856 = false;
    private static BufferedReader in;
    private int fetchCount = 0;
    private static int numInBuf = 0;
    private static String urlBase = "http://catalog.hathitrust.org/api/volumes/full/json/";

    private int chunkSize = 20;
    private int maxToFetch = -1;
    private int numToSkip = 0;
    private String fetchBuf[] = null;
    private ByteArrayOutputStream baos = null;
    private int baosOffset;
    private byte[] baBuf;
    private Set<String> idSet;
    
    public HathiPlunderer(BufferedReader in, int maxToFetch, int numToSkip, int chunkSize)
    {
        this.maxToFetch = maxToFetch;
        this.numToSkip = numToSkip;
        this.chunkSize = chunkSize;
        if (chunkSize > 20) chunkSize = 20;
        fetchBuf = new String[chunkSize];
        baos = new ByteArrayOutputStream();
        baBuf = null;
        baosOffset = 0;
        idSet = new TreeSet<String>();
    }

    @Override
    public void close() throws IOException
    {
        // TODO Auto-generated method stub        
    }

    @Override
    public boolean markSupported()
    {
        return(false);        
    }

    @Override
    public int read() throws IOException
    {
        if (baosOffset == baos.size())
        {
            baos.reset();
            if (!fillBuffer(baos))
            {
                return(-1);
            }
            baosOffset = 0;
            baBuf = baos.toByteArray();
        }
        int result = baBuf[baosOffset++];
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len)
    {
        if (baosOffset == baos.size())
        {
            baos.reset();
            if (!fillBuffer(baos))
            {
                return(-1);
            }
            baosOffset = 0;
            baBuf = baos.toByteArray();
        }
        if (baosOffset + len > baos.size())
        {
            len = baos.size() - baosOffset;
        }
        System.arraycopy(baBuf, baosOffset, b, off, len);
        baosOffset += len;
        return(len);
    }
    
    public boolean fillBuffer(ByteArrayOutputStream os)
    {
        String line;
        try
        {
            boolean keepGoing = true;
            while ((line = in.readLine()) != null)
            {
                if (line.trim().matches("[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]"))
                {
                    line = line.trim();
                    if (!idSet.contains(line))
                    {
                        idSet.add(line);
                        keepGoing = fetchID(line, os );
                    }
                }
                else
                {
                    String parts[] = line.split("\t", 6);
                    if (parts[1].equals("allow"))
                    {
                        if (!idSet.contains(parts[3]))
                        {
                            idSet.add(parts[3]);
                            keepGoing = fetchID(parts[3], os);
                        }
                    }
                }
                if (os != null && os.size() > 0)
                {
                    return(true);
                }
                if (!keepGoing) 
                {
                    break;
                }
            }
            if (numInBuf > 0) 
            {
                processFetch(fetchBuf, numInBuf, os);
                numInBuf = 0;
                if (os != null && os.size() > 0)
                {
                    return(true);
                }
            }
            return(false);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return(false);
    }
    
    public void readInput(PrintStream debugOut)
    {
        int c;
        try
        {
            String pattern = "\"recordURL\":\"";
            int patternState = 0;
            StringBuffer buf = null;
            while ((c = read()) != -1)
            {
                if (patternState == pattern.length())
                {
                    buf = new StringBuffer();
                    buf.append(pattern).append((char)c);
                    patternState++;
                }
                else if (patternState > pattern.length())
                {
                    buf.append((char)c);
                    if (c == '"')
                    {
                        debugOut.println(buf.toString());
                        patternState = 0;
                        buf = null;
                    }
                }
                else if (c == pattern.charAt(patternState))
                {
                    patternState++;
                }
                else
                {
                    patternState = 0;
                }
            }
        }
        catch (IOException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
//        String line;
//        try
//        {
//            Set<String> idSet = new TreeSet<String>();
//            boolean keepGoing = true;
//            while ((line = in.readLine()) != null)
//            {
//                if (line.trim().matches("[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]"))
//                {
//                    line = line.trim();
//                    if (!idSet.contains(line))
//                    {
//                        idSet.add(line);
//                        keepGoing = fetchID(line, (os != null) ? os : out);
//                    }
//                }
//                else
//                {
//                    String parts[] = line.split("\t", 6);
//                    if (parts[1].equals("allow"))
//                    {
//                        if (!idSet.contains(parts[3]))
//                        {
//                            idSet.add(parts[3]);
//                            keepGoing = fetchID(parts[3], (os != null) ? os : out);
//                        }
//                    }
//                }
//                if (os != null && os.size() > 0)
//                {
//                    if (debug) 
//                    { 
//                        Pattern pattern = Pattern.compile("\"recordURL\":\"[^\"]*\"", 0);
//                        Matcher matcher = pattern.matcher(os.toString("UTF8"));
//                        while (matcher.find())
//                        {
//                            debugOut.println(matcher.group());
//                        }
//                    }
//                    else if (out != null)
//                    {
//                        out.write(os.toByteArray());
//                    }
//                    os.reset();
//                }
//                if (!keepGoing) 
//                {
//                    break;
//                }
//            }
//        }
//        catch (IOException e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }


    private boolean fetchID(String id, OutputStream os)
    {
        if (numToSkip > 0) { numToSkip--; return true; }
        fetchCount++;
        if (maxToFetch > 0 && fetchCount > maxToFetch) 
        {
            if (numInBuf > 0) processFetch(fetchBuf, numInBuf, os);
            numInBuf = 0;
            return false;
        }
        fetchBuf[numInBuf++]= id;
        if (numInBuf == fetchBuf.length)
        {
            processFetch(fetchBuf, numInBuf, os);
            numInBuf = 0;
        }
        return(true);
    }


    
    private void processFetch(String[] fetchBuf2, int numInBuf2, OutputStream os)
    {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < numInBuf2; i++)
        {
            if (i > 0) buf.append("|");
            buf.append("recordnumber:").append(fetchBuf2[i]);
        }
        String fullUrlStr = urlBase + buf.toString();
        InputStream in = null; 
        try {
            URL url = new URL(fullUrlStr);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            HttpURLConnection httpConn = (HttpURLConnection) urlConn;
            httpConn.setAllowUserInteraction(false);
            httpConn.connect();
            in = httpConn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(in);
            
            int read = 0;
            int bufSize = 512;
            byte[] buffer = new byte[bufSize];
            while(true)
            {
                read = bis.read(buffer);
                if(read==-1)
                {
                    break;
                }
                os.write(buffer, 0, read);
            }
        } catch (MalformedURLException e) {
            // DEBUG
//            Log.e("DEBUG: ", e.toString());
        } catch (IOException e) {
            // DEBUG
//            Log.e("DEBUG: ", e.toString());
        } 
    }


    public static BufferedReader initReader(String[] args)
    {
        InputStream is = null;
        if (args.length >= 1)
        {
            Vector<InputStream> inputs = new Vector<InputStream>();
            for (String arg : args)
            {
                if (arg.startsWith("http://"))
                {
                    try
                    {
                        URL url = new URL(arg);
                        is = url.openStream();
                        if (arg.contains(".gz")) 
                        {
                            is = new GZIPInputStream(is);
                        }
                        inputs.add(is);

                    }
                    catch (MalformedURLException e)
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
                else
                {
                    File file = new File(arg);
                    if (file.exists() && file.canRead())
                    {
                        InputStream instr = null;
                        try
                        {
                            instr = new FileInputStream(file);
                            if (arg.contains(".gz")) 
                            {
                                instr = new GZIPInputStream(instr);
                            }
                            inputs.add(instr);
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
                }
            }
            if (args.length > 1)
            {
                is = new SequenceInputStream(inputs.elements());
            }
            else
            {
                is = inputs.firstElement();
            }
        }
        else
        {
            is = new BufferedInputStream(System.in);
            is.mark(10);
            byte magic[] = new byte[2];
            try { 
                is.read(magic);
                is.reset();
            }
            catch (IOException e)
            {
//                logger.error("Fatal error: Exception reading from stdin");
                throw new IllegalArgumentException("Fatal error: Exception reading from stdin");
            }
            int magicNum = ((int) magic[0] & 0xff) | ((magic[1] << 8) & 0xff00);
            if (GZIPInputStream.GZIP_MAGIC == magicNum) 
            {
                try
                {
                    is = new GZIPInputStream(is);
                }
                catch (IOException e)
                {
                    throw new IllegalArgumentException("Fatal error: Initializing GZipInputStream for stdin");
                }
            }

        }
        in = new BufferedReader(new InputStreamReader(is));
        return(in);
    }


    /**
     * @param args
     */
    public static void main(String[] args)
    {
        int chunkSize = 20;
        int maxToFetch = -1;
        int numToSkip = 0;
        
        PrintStream out = null;
        try
        {
            out = new PrintStream(System.out, true, "UTF8");
        }
        catch (UnsupportedEncodingException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        while (args.length >= 1 && args[0].startsWith("-"))
        {
            int skip = 1;
            if (args[0].equals("-d"))
            {
                debug = true;
            }
            else if (args[0].equals("-v"))
            {
                print = true;
            }
            else if (args[0].equals("-856"))
            {
                add856 = true;
            }
            else if (args[0].equals("-n"))
            {
                maxToFetch  = Integer.parseInt(args[1]);
                skip = 2;
            }
            else if (args[0].equals("-s"))
            {
                numToSkip  = Integer.parseInt(args[1]);
                skip = 2;
            }
            else if (args[0].equals("-c"))
            {
                int val = Integer.parseInt(args[1]);
                if (val <= 20) 
                {
                    chunkSize = val;
                    skip = 2;
                }
            }
            String newArgs[] = new String[args.length - skip];
            System.arraycopy(args, skip, newArgs, 0, args.length-skip);
            args = newArgs;
        }

        in = initReader(args);

        HathiPlunderer reader = new HathiPlunderer(in, maxToFetch, numToSkip, chunkSize);
        if (debug)
        {
            reader.readInput(out);
        }
        else
        {
            HathiJsonToMarc hathiReader = new HathiJsonToMarc(reader, add856);
            MarcStreamWriter writer = new MarcStreamWriter(out, "UTF8", true);
            while (hathiReader.hasNext())
            {
                Record record = hathiReader.next();
                if (print) 
                {
                    System.out.println(record.toString());
                }
                else
                {
                    writer.write(record);
                    out.flush();
                }
            }
        }
    }

}
