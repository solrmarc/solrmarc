package org.solrmarc.z3950;
// Title:       TestClient
// @version:    $Id$
// Copyright:   Copyright (C) 1999,2000 Knowledge Integration Ltd.
// @author:     Ian Ibbotson (ibbo@k-int.com)
// Company:     KI
// Description: 
//


//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2.1 of
// the license, or (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU general Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite
// 330, Boston, MA  02111-1307, USA.
// 

import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import org.jzkit.a2j.codec.util.OIDRegister;

import com.k_int.IR.IREvent;
import com.k_int.IR.IRQuery;
import com.k_int.IR.InformationFragment;
import com.k_int.IR.SearchException;
import com.k_int.IR.SearchTask;
import com.k_int.IR.Searchable;
import com.k_int.z3950.IRClient.Z3950Origin;

public class Z3950Client
{
  public static void main(String args[])
  {
    Package ir_package = Package.getPackage("com.k_int.IR");

    System.out.println("JZKit/IR Test z39.50 client $Revision: 1.39 $");
    System.out.println("Using IR Interfaces : " +ir_package.getSpecificationTitle()+ " " +
                                         ir_package.getSpecificationVersion()+ " " +
                         ir_package.getSpecificationVendor());

    System.out.println("Connecting to "+"virgo.lib.virginia.edu"+" on port "+new Integer(2200));

//    if ( args.length != 5 )
//    {
//      System.out.println("Usage TestClient host port recsyn database rpn");
//      System.exit(1);
//    }

    OIDRegister reg = OIDRegister.getRegister();

    Properties p = new Properties();

    // p.put("ServiceHost","bagel.indexdata.dk");
    p.put("ServiceHost","virgo.lib.virginia.edu");
    p.put("ServicePort","2200");
    p.put("service_short_name","virgo");
    p.put("service_long_name","virgo");
    p.put("default_record_syntax","usmarc");
    p.put("default_element_set_name","F");

    // Remember you don't really need to do this...
    Observer fragment_count_observer = new Observer() 
                                           {
                                             public void update(Observable o, Object arg)
                                             {
                                               IREvent e = (IREvent)arg;
 
                                               if ( e.event_type == IREvent.SOURCE_RESET )
                                               {
                                                 System.err.println("TIME: Sub Fragment source reset");
                                               }
                                               else if ( e.event_type == IREvent.FRAGMENT_COUNT_CHANGE )
                                               {
                                                 System.err.println("TIME: Number of fragments has changed to "+e.event_info);
                                               }
                                             }
                                           };

    Observer[] all_observers = new Observer[] { fragment_count_observer };

    Searchable s = new Z3950Origin();
    s.init(p);

    IRQuery e = new IRQuery();
    e.collections.add("Unicorn");
//    e.hints.put("record_syntax", "usmarc");
//    e.hints.put("elements", "F");
    e.query = new com.k_int.IR.QueryModels.PrefixString("@attrset bib-1 @attr 1=1016 \"^C2\"");
  //   e.query = new com.k_int.IR.QueryModels.CCLString("ti=Jefferson");
    // e.query = new com.k_int.IR.QueryModels.CCLString("ti=sheffield or ti=\"support groups\"");
    // e.sorting = "on-missing use null by @attrset bib-1 @attr 1=4";
    // e.sorting = "Desc nocase by @attrset bib-1 @attr 1=4";

    System.err.println("Searching");

    try
    {
      SearchTask st = (SearchTask) s.createTask(e,null,all_observers);
      int status = st.evaluate(60000);

      System.err.println("Private task status: "+st.lookupPrivateStatusCode(st.getPrivateTaskStatusCode()));

      Enumeration rs_enum = st.getTaskResultSet().elements();

      System.err.println("Dumping result set");
      while ( rs_enum.hasMoreElements() )
      {
        InformationFragment f = (InformationFragment)rs_enum.nextElement();
        System.out.println("Length of Next search element: "+f.toString().length());
      }

      st.destroyTask();
    }
    catch ( SearchException se )
    {
      se.printStackTrace();
    }
    catch ( com.k_int.IR.TimeoutExceededException tee )
    {
      tee.printStackTrace();
    }

    s.destroy();
    System.exit(0);
  }
}
