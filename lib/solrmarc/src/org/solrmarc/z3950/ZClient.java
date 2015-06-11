package org.solrmarc.z3950;
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.ByteArrayInputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import com.k_int.gen.Z39_50_APDU_1995.DefaultDiagFormat_type;
import com.k_int.gen.Z39_50_APDU_1995.InitializeResponse_type;
import com.k_int.gen.Z39_50_APDU_1995.NamePlusRecord_type;
import com.k_int.gen.Z39_50_APDU_1995.PresentResponse_type;
import com.k_int.gen.Z39_50_APDU_1995.Records_type;
import com.k_int.gen.Z39_50_APDU_1995.SearchResponse_type;
import com.k_int.gen.Z39_50_APDU_1995.record_inline13_type;
import com.k_int.z3950.client.SynchronousOriginBean;

//for OID Register
import org.jzkit.a2j.codec.util.OIDRegister;
import org.jzkit.a2j.gen.AsnUseful.EXTERNAL_type;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.marc.Record;
import org.marc4j.marc.impl.ControlFieldImpl;
//import org.solrmarc.marc.MarcTranslatedReader;

import com.k_int.IR.SearchException;
import com.k_int.IR.InvalidQueryException;
import com.k_int.IR.Syntaxes.marc.iso2709;


/**
* ZClient : A Simple Z3950 command line client to test the toolkit
*
* @version:    $Id$
* @author:     Ian Ibbotson (ian.ibbotson@k-int.com)
*
*/
public class ZClient extends SynchronousOriginBean
{
    public static final char ISO2709_RS = 035;
    public static final char ISO2709_FS = 036;
    public static final char ISO2709_IDFS = 037;
    private static final String PREFIX_QUERY_TYPE = "PREFIX";
    private static final String CCL_QUERY_TYPE = "CCL";
    
    boolean verbose = false;
    
    private int auth_type = 0; // 0=none, 1=anonymous, 2=open, 3=idpass
    private String principal = null;
    private String group = null;
    private String credentials = null;
    private String querytype = PREFIX_QUERY_TYPE;
    private String current_result_set_name = null;
    private int result_set_count=0;
    private String element_set_name = null; // Default to a null (no) element set name
    private OIDRegister reg = OIDRegister.getRegister();

//    private com.k_int.z3950.IRClient.Z3950Origin orig = null;
    
    public static void main(String args[])
    {
         ZClient newclient = new ZClient();
         Package ir_package = Package.getPackage("com.k_int.IR");
         Package a2j_runtime_package = Package.getPackage("com.k_int.codec.runtime");         
        
//         System.err.println("JZKit command line z39.50 client $Revision: 1.53 $");    
         newclient.openConnection("ceres.lib.virginia.edu", "2200");
   //      newclient.cmdBase("Unicorn");
         newclient.cmdElements("F");
         newclient.cmdFormat("usmarc");
         newclient.verbose = true;

         MarcStreamWriter writer = new MarcStreamWriter(System.out);
         Iterator<Record> recIter = newclient.getRecordByIDStr("1");
         Record rec = recIter.next();
         writer.write(rec);
         System.err.println(rec.toString());
         
         System.exit(0);
     
    }
    
    public ZClient()
    {
        super();
    }

// Commands

    public boolean openConnection(String hostname, String portnum)
    {    
        boolean respVal = false;
        
        try
        {
            if (verbose) System.err.println("Attempting connection to "+ hostname + " : " + portnum);
            InitializeResponse_type resp = connect(hostname, portnum,
                    auth_type, principal, group, credentials);
    
            if (verbose) System.err.println("Received response from connect");
            respVal = resp.result.booleanValue();
            if (respVal != true)
            {
                if (verbose) System.err.println("  Failed to establish association");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (respVal);
    }
    
    public boolean openConnection(String hostname, String portnum, String userID, String password)
    {    
        boolean respVal = false;
        
        try
        {
            if (verbose) System.err.println("Attempting connection to "+ hostname + " : " + portnum);
            InitializeResponse_type resp = connect(hostname, portnum,
                    ((userID == null) ? 0 : 3), userID, group, password);
    
            if (verbose) System.err.println("Received response from connect");
            respVal = resp.result.booleanValue();
            if (respVal != true)
            {
                if (verbose) System.err.println("  Failed to establish association");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (respVal);
    }

    public int cmdFind(String args, String resultSetName)
    {
        SearchResponse_type resp = null;
        int numResults = 0;
        current_result_set_name = resultSetName;

        if (verbose) System.err.println("Calling find, query= " + args);
        try
        {
            if (querytype.equalsIgnoreCase("CCL"))
            {
                resp = sendSearch(new com.k_int.IR.QueryModels.CCLString(args),
                        null, current_result_set_name, element_set_name);
            }
            else
            {
                resp = sendSearch(new com.k_int.IR.QueryModels.PrefixString(
                        args), null, current_result_set_name, element_set_name);
            }
        }
        catch (SearchException se)
        {
            cat.warn("Problem processing query", se);
            if (verbose) System.err.println(se.toString());

            // If possible, the search response information will be passed along
            // with
            // the exception
            if (se.additional != null)
            {
                resp = (SearchResponse_type) (se.additional);
            }
        }
        catch (InvalidQueryException iqe)
        {
            cat.warn("Problem parsing query", iqe);
            if (verbose) System.err.println(iqe);
        }
        catch (Exception e)
        {
            cat.warn("Problem processing query", e);
            if (verbose) System.err.println(e.toString());
        }

        if (resp != null)
        {
            numResults = resp.resultCount.intValue();
            if (verbose) System.err.println("NumResults = " + numResults);

            if ((resp.records != null)
                    && (resp.numberOfRecordsReturned.intValue() > 0))
            {
                if (verbose) System.err.println("  Search has piggyback records");
            }
        }
        return(numResults);
    }
    
    public int cmdFind(String args)
    {
        String curResSetName = "RS" + (result_set_count++);
        return(cmdFind(args, curResSetName));       
    }

//    public Record getRecordByIDNum(int idnum)
//    {
//        Iterator<Record> iter = getRecordBySearchStr("1016", "^C"+idnum);
//        
//        
//        
//        cmdFind("@attrset bib-1 @attr 1=1016 \"^C"+idnum+"\"");
//        if (verbose) System.err.println("requesting record by ID:" + idnum);
//        Record rec = getRecord(1);
//        if (verbose) System.err.println("getting record by ID:" + idnum);
//        if (rec != null)
//        {
//            if (verbose) System.err.println("adding ID to record:" + idnum);
//            rec.addVariableField(new ControlFieldImpl("001", "u"+idnum));
//        }
//        return(rec);
//    }
    
    public Iterator<Record> getRecordByIDStr(String idstr)
    {
        int numFound = cmdFind("@attrset bib-1 @attr 1=1016 \"^C"+idstr+"\"", "default");
        Z3950RecordIter iter = new Z3950RecordIter(this, "default", numFound, "u"+idstr);
        return(iter);
        
        
//        cmdFind("@attrset bib-1 @attr 1=1016 \"^C"+idnum+"\"");
//        if (verbose) System.err.println("requesting record by ID:" + idnum);
//        Record rec = getRecord(1);
//        if (verbose) System.err.println("getting record by ID:" + idnum);
//        if (rec != null)
//        {
//            if (verbose) System.err.println("adding ID to record:" + idnum);
//            rec.addVariableField(new ControlFieldImpl("001", "u"+idnum));
//        }
//        return(rec);
    }
    
    public Iterator<Record> getRecordBySearchStr(String attr, String value)
    {
        int numFound = cmdFind("@attrset bib-1 @attr 1="+attr+" \""+value+"\"", "default");
        Z3950RecordIter iter = new Z3950RecordIter(this, "default", numFound);
        return(iter);
    }
    
    public Record getRecord(int startAt)
    {
        return(getRecord(startAt, current_result_set_name));
    }
    
    public Record getRecord(int startAt, String resultSetName)
    {
        // Format for present command is n [ + n ]
        // System.err.println("Present "+args);
        String setname = resultSetName;

        try
        {
            PresentResponse_type resp = sendPresent(startAt, 1, element_set_name, setname);

//            System.err.println("\n  Present Response");

            if (resp.referenceId != null)
            {
//                System.err.println("  Reference ID : " + new String(resp.referenceId));
            }

//            System.err.println("  Number of records : " + resp.numberOfRecordsReturned);
//            System.err.println("  Next RS Position : " + resp.nextResultSetPosition);
//            System.err.println("  Present Status : " + resp.presentStatus);
//            System.err.println("");

            Records_type r = resp.records;

            if (r.which == Records_type.responserecords_CID)
            {
                Vector v = (Vector) (r.o);
                int num_records = v.size();
 //               System.err.println("Response contains " + num_records + " Response Records");
                for (Enumeration recs = v.elements(); recs.hasMoreElements();)
                {
                    NamePlusRecord_type npr = (NamePlusRecord_type) (recs
                            .nextElement());

                    if (null != npr)
                    {
  //                      System.err.println("[" + npr.name + "] ");

                        if (npr.record.which == record_inline13_type.retrievalrecord_CID)
                        {
                            // RetrievalRecord is an external
                            EXTERNAL_type et = (EXTERNAL_type) npr.record.o;
                            // System.err.println(" Direct
                            // Reference="+et.direct_reference+"] ");
                            // dumpOID(et.direct_reference);
                            // Just rely on a toString method for now
                            if (et.direct_reference.length == 6)
                            {
                                if (et.direct_reference[(et.direct_reference.length) - 1] == 10) // USMarc
                                {
  //                                  System.err.print("USMarc: ");
                                    iso2709 rec = new iso2709((byte[]) et.encoding.o);
                                    Record marcRec = rec.getRecord();
                                    return(marcRec);
                                }
                            }
                        }
                    }
                }

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            if (verbose) System.err.println("Exception processing show command " + e);
        }
        return(null);
    }
    
//    public String getStringByIDNum(int idnum)
//    {
//        if (verbose) System.err.println("Calling getStringByIDNum id="+ idnum);
//    	cmdFind("@attrset bib-1 @attr 1=1016 \"^C"+idnum+"\"");
//    	if (verbose) System.err.println("Calling getString id="+ idnum);
//        byte[] rec = getBytes(1);
//        if (verbose) System.err.println("bytes count="+ rec.length);
//        ByteArrayInputStream bs = new ByteArrayInputStream(rec);
//        if (verbose) System.err.println("made ByteArrayInputStream");
//        MarcStreamReader m1 = new MarcStreamReader(bs);
//        if (verbose) System.err.println("made MarcStreamReader");
//        MarcTranslatedReader mr = new MarcTranslatedReader(m1, true);
//        System.err.println("made MarcTranslatedReader");
//        String result = null;
//        if (verbose) System.err.println("checking for next");
//        if (mr.hasNext())
//        {
//            if (verbose) System.err.println("Getting next");
//        	try {
//        		Record marc = mr.next();
//        		if (verbose) System.err.println("got Record: "+ marc.toString());
//                result = marc.toString();
//                if (verbose) System.err.println("String len="+ result.length());
//        	}
//        	catch (Throwable e)
//        	{
//        	    if (verbose) System.err.println("Exception: "+ e.getMessage());
//        		e.printStackTrace();
//        	}
//        }
//        return(result);
//    }
    
    public byte[] getBytesByIDNum(int idnum)
    {
        System.err.println("Calling getBytesByIDNum id="+ idnum);
    	cmdFind("@attrset bib-1 @attr 1=1016 \"^C"+idnum+"\"");
        System.err.println("Calling getBytes id="+ idnum);
        byte[] rec = getBytes(1);
        System.err.println("bytes count="+ rec.length);
        return(rec);
    }
    
//	public String getStringByIDNum(int idnum)
//	{
//		byte[] bytes = getBytesByIDNum(idnum);
//		/*try
//		{
//			String s = new String(bytes);
//			return s;
//		}catch(java.io.UnsupportedEncodingException e){
//			
//		}
//		return null;
//		*/
//		String s = new String(bytes);
//		return s;
//	}
	
    public byte[] getBytes(int startAt)
    {
        // Format for present command is n [ + n ]
        // System.err.println("Present "+args);
        String setname = current_result_set_name;

        try
        {
            PresentResponse_type resp = sendPresent(startAt, 1, element_set_name, setname);

//            System.err.println("\n  Present Response");

            if (resp.referenceId != null)
            {
//                System.err.println("  Reference ID : " + new String(resp.referenceId));
            }

//            System.err.println("  Number of records : " + resp.numberOfRecordsReturned);
//            System.err.println("  Next RS Position : " + resp.nextResultSetPosition);
//            System.err.println("  Present Status : " + resp.presentStatus);
//            System.err.println("");

            Records_type r = resp.records;

            if (r.which == Records_type.responserecords_CID)
            {
                Vector v = (Vector) (r.o);
                int num_records = v.size();
 //               System.err.println("Response contains " + num_records + " Response Records");
                for (Enumeration recs = v.elements(); recs.hasMoreElements();)
                {
                    NamePlusRecord_type npr = (NamePlusRecord_type) (recs
                            .nextElement());

                    if (null != npr)
                    {
  //                      System.err.println("[" + npr.name + "] ");

                        if (npr.record.which == record_inline13_type.retrievalrecord_CID)
                        {
                            // RetrievalRecord is an external
                            EXTERNAL_type et = (EXTERNAL_type) npr.record.o;
                            // System.err.println(" Direct
                            // Reference="+et.direct_reference+"] ");
                            // dumpOID(et.direct_reference);
                            // Just rely on a toString method for now
                            if (et.direct_reference.length == 6)
                            {
                                if (et.direct_reference[(et.direct_reference.length) - 1] == 10) // USMarc
                                {
                                    return(((byte[]) et.encoding.o));
                                }
                            }
                        }
                    }
                }

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.err.println("Exception processing show command " + e);
        }
        return(null);
    }

    public void cmdBase(String args)
    {
        clearAllDatabases();

        try
        {
            java.util.StringTokenizer st = new java.util.StringTokenizer(args, " ");

            clearAllDatabases();

            while (st.hasMoreTokens())
            {
                addDatabase(st.nextToken());
            }

            // System.err.println("dbnames:"+db_names);
        }
        catch (Exception e)
        {
            System.err.println("Exception processing base command " + e);
        }
    }

    //
    // Change the element set name in use (Normally "F" or "B")
    //
    public void cmdElements(String args)
    {
        // Right now, args is prefixed with a space... So junk it.
        try
        {
            java.util.StringTokenizer st = new java.util.StringTokenizer(args, " ");
            if (st.hasMoreTokens())
            {
                setElementSetName(st.nextToken());
            }
            else
            {
                setElementSetName(null);
            }
        }
        catch (Exception e)
        {
            System.err.println("Exception processing base command " + e);
        }
    }

    //
    // Change the format 
    //
    public void cmdFormat(String args)
    {
        // Right now, args is prefixed with a space... So junk it.
        try
        {
            java.util.StringTokenizer st = new java.util.StringTokenizer(args, " ");
            if (st.hasMoreTokens())
            {
                String requested_syntax = st.nextToken();
                if (reg.oidByName(requested_syntax) != null)
                {
                    setRecordSyntax(requested_syntax);
                }
                else
                {
                    System.err.println("Unknown Record Syntax");
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("Exception processing format command " + e);
        }
    } 

////
//// Change the element set name in use (Normally "F" or "B")
////
//public void cmdAuth(String args)
//{
// try
// {
//   java.util.StringTokenizer st = new java.util.StringTokenizer(args," ,");
//
//   if ( st.hasMoreTokens() )
//   {
//     String type = st.nextToken();
//     if ( type.equals("anon") )
//     {
//       System.err.println("Will use anonymous authentication");
//       auth_type = 1;
//     }
//     else if ( type.equals("open") )
//     {
//      System.err.println("Will use open authentication");
//       if ( st.hasMoreTokens() )
//       {
//         auth_type = 2;
//         principal = st.nextToken();
//         System.err.println("Open auth string will be "+principal);
//       }
//       else
//       {
//         System.err.println("Asked for open authentication but no open string supplied, No auth will be used");
//       }
//     }
//     else if ( type.equals("idpass") )
//     {
//      System.err.println("Will use idpass authentication");
//       auth_type = 3;
//       if ( st.hasMoreTokens() )
//         principal = st.nextToken();
//       if ( st.hasMoreTokens() )
//         group = st.nextToken();
//       if ( st.hasMoreTokens() )
//         credentials = st.nextToken();
//     }
//     else
//     {
//       System.err.println("Unrecognised auth type, no authentication will be used");
//       auth_type = 0;
//     }
//   }
//   else
//   {
//     System.err.println("No auth type, no authentication will be used");
//   }
// }
// catch ( Exception e )
// {
//   System.err.println("Exception processing base command "+e);
// }
//}

//public void cmdQueryType(String args)
//{
// // Right now, args is prefixed with a space... So junk it.
// try
// {
//   java.util.StringTokenizer st = new java.util.StringTokenizer(args," ");
//   if ( st.hasMoreTokens() )
//   {
//     String type = st.nextToken();
//     System.err.println("Set query type to "+type);
//
//     if ( type.equals("CCL") )
//       querytype=CCL_QUERY_TYPE;
//     else
//       querytype=PREFIX_QUERY_TYPE;
//   }
// }
// catch ( Exception e )
// {
//   System.err.println("Exception processing base command "+e);
// }
//}

//public void dumpOID(int[] oid)
//{
// System.err.print("{");
// for ( int i = 0; i < oid.length; i++ )
// {
//   System.err.print(oid[i]+" ");
// }
// System.err.println("}");
//}

public void displayRecords(Records_type r)
{
 if ( r != null )
 {
   switch ( r.which )
   {
     case Records_type.responserecords_CID:
         Vector v = (Vector)(r.o);
         int num_records = v.size();
         System.err.println("Response contains "+num_records+" Response Records");
         for ( Enumeration recs = v.elements(); recs.hasMoreElements(); ) 
         {
             NamePlusRecord_type npr = (NamePlusRecord_type)(recs.nextElement());

             if ( null != npr )
             {
               System.err.print("["+npr.name+"] ");

               switch ( npr.record.which )
               {
                 case record_inline13_type.retrievalrecord_CID:
                   // RetrievalRecord is an external
                   EXTERNAL_type et = (EXTERNAL_type)npr.record.o;
                   // System.err.println("  Direct Reference="+et.direct_reference+"] ");
                   // dumpOID(et.direct_reference);
                   // Just rely on a toString method for now
                   if ( et.direct_reference.length == 6 )
                   {
                     switch(et.direct_reference[(et.direct_reference.length)-1])
                     {
//                       case 1: // Unimarc
//                         System.err.print("Unimarc ");
//                         DisplayISO2709((byte[])et.encoding.o);
//                         break;
//                       case 3: // CCF
//                         System.err.print("CCF ");
//                         break;
                       case 10: // US Marc
  //                       System.err.print("USMarc: ");
                         DisplayISO2709((byte[])et.encoding.o);
                         // byte[] ba = (byte[])et.encoding.o;
                         // System.err.println("Bytes:");
                         // for ( int i=0; i<ba.length;i++ ) {
                         //   System.err.print(ba[i]);
                         // }
                         // System.err.println("");
                         break;
//                       case 11: // UK Marc
//                         System.err.print("UkMarc ");
//                         DisplayISO2709((byte[])et.encoding.o);
//                         break;
//                       case 12: // Normarc
//                         System.err.print("Normarc ");
//                         DisplayISO2709((byte[])et.encoding.o);
//                         break;
//                       case 13: // Librismarc
//                         System.err.print("Librismarc ");
//                         DisplayISO2709((byte[])et.encoding.o);
//                         break;
//                       case 14: // Danmarc
//                         System.err.print("Danmarc ");
//                         DisplayISO2709((byte[])et.encoding.o);
//                         break;
//                       case 15: // Finmarc
//                         System.err.print("Finmarc ");
//                         DisplayISO2709((byte[])et.encoding.o);
//                         break;
//              case 100: // Explain
//                cat.debug("Explain record");
//                // Write display code....
//                break;
//              case 101: // SUTRS
//                System.err.print("SUTRS ");
//                System.err.println((String)et.encoding.o);
//                break;
//              case 102: // Opac
//                cat.debug("Opac record");
//                // Write display code....
//                break;
//              case 105: // GRS1
//                System.err.print("GRS1 ");
//                displayGRS((java.util.Vector)et.encoding.o);
//                break;
                       default:
                System.err.print("Unknown.... ");
                         System.err.println(et.encoding.o.toString());
                         break;
                     }
                   }
                   else if ( ( et.direct_reference.length == 7 ) &&
                             ( et.direct_reference[5] == 109 ) )
                   {
                     switch(et.direct_reference[6])
                     {
                       case 3: // HTML
                         System.err.print("HTML ");
                            String html_rec = null;
                            if ( et.encoding.o instanceof byte[] )
                                html_rec = new String((byte[])et.encoding.o);
                            else
                                html_rec = et.encoding.o.toString();                             
                         System.err.println(html_rec.toString());
                         break;
                       case 9: // SGML
                         System.err.print("SGML ");
                         System.err.println(et.encoding.o.toString());
                         break;
                       case 10: // XML
                         System.err.print("XML ");
                         System.err.println(new String((byte[])(et.encoding.o)));
                         break;
                       default:
                         System.err.println(et.encoding.o.toString());
                         break;
                      }
                   }
                   else
                     System.err.println("Unknown direct reference OID: "+et.direct_reference);
                   break;
                 case record_inline13_type.surrogatediagnostic_CID:
                   System.err.println("SurrogateDiagnostic");
                   break;
                 case record_inline13_type.startingfragment_CID:
                   System.err.println("StartingFragment");
                   break;
                 case record_inline13_type.intermediatefragment_CID:
                   System.err.println("IntermediateFragment");
                   break;
                 case record_inline13_type.finalfragment_CID:
                   System.err.println("FinalFragment");
                   break;
                 default:
                   System.err.println("Unknown Record type for NamePlusRecord");
                   break;
               }
             }
             else
             {
               System.err.println("Error... record ptr is null");
             }
         }
         break;
     case Records_type.nonsurrogatediagnostic_CID:
        DefaultDiagFormat_type diag = (DefaultDiagFormat_type)r.o;
         System.err.println("    Non surrogate diagnostics : "+diag.condition);
        if ( diag.addinfo != null )
        {
           // addinfo is VisibleString in v2, InternationalString in V3
           System.err.println("Additional Info: "+diag.addinfo.o.toString());
        }
         break;
     case Records_type.multiplenonsurdiagnostics_CID:
         System.err.println("    Multiple non surrogate diagnostics");
         break;
     default:
         System.err.println("    Unknown choice for records response : "+r.which);
         break;
   }
 }

 // if ( null != e.getPDU().presentResponse.otherInfo )
 //   System.err.println("  Has other information");
 System.err.println("");

}


    private void DisplayISO2709(byte[] octets)
    {
        com.k_int.IR.Syntaxes.marc.iso2709 rec = new com.k_int.IR.Syntaxes.marc.iso2709(octets);
        System.err.println(rec);
    }

    private void displayGRS(Vector v)
    {
        com.k_int.IR.Syntaxes.GRS1 grs_rec = new com.k_int.IR.Syntaxes.GRS1("Repository",
                                                                    "Coll", 
                                        "", 
                                        v,
                                        null);
        System.err.println(grs_rec.toString());
    }

    public void setElementSetName(String element_set_name)
    {
        this.element_set_name = element_set_name;
    }

    public String getElementSetName()
    {
        return element_set_name;
    }
}
