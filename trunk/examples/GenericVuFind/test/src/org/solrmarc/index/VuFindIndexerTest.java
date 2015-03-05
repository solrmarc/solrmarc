package org.solrmarc.index;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.DataFieldImpl;
import org.marc4j.marc.impl.LeaderImpl;
import org.marc4j.marc.impl.RecordImpl;
import org.marc4j.marc.impl.SubfieldImpl;
import org.marc4j.marc.impl.VariableFieldImpl;
import org.solrmarc.callnum.DeweyCallNumber;
import org.solrmarc.callnum.LCCallNumber;
import org.solrmarc.index.VuFindIndexer;


public class VuFindIndexerTest {
    
    private VuFindIndexer indexer = null;

    Leader genericLeader = null;
    Record callNumRec = null;
    
    @Before
    public void initCallNumRec() {
        Record callNumRec = new RecordImpl();
        genericLeader = new LeaderImpl("                        ");
        callNumRec.setLeader(genericLeader);

        // Rubbish: tests not yet using a pre-inited record
        String callNumLC = "PS 1234.5 .G78";
        DataField df090LC = new DataFieldImpl("090", ' ', ' ');
        df090LC.addSubfield(new SubfieldImpl('a', callNumLC));
        df090LC.addSubfield(new SubfieldImpl('t', "LC"));
        callNumRec.addVariableField(df090LC);
        
        String callNumDDC = "324.987 B34";
        DataField df090DDC = new DataFieldImpl("090", ' ', ' ');
        df090DDC.addSubfield(new SubfieldImpl('a', callNumDDC));
        df090DDC.addSubfield(new SubfieldImpl('t', "DDC"));
        callNumRec.addVariableField(df090DDC);
        
    }
    
    @Test
    public void testGetCallNumberByType() {
        
        // Init records
        Record myCallNumRec = new RecordImpl();
        myCallNumRec.setLeader(genericLeader);;
        
        String callNumLC = "PS 1234.5 .G78";
        DataField df090LC = new DataFieldImpl("090", ' ', ' ');
        df090LC.addSubfield(new SubfieldImpl('a', callNumLC));
        df090LC.addSubfield(new SubfieldImpl('t', "LC"));
        myCallNumRec.addVariableField(df090LC);
        
        String callNumDDC = "324.987 B34";
        DataField df090DDC = new DataFieldImpl("090", ' ', ' ');
        df090DDC.addSubfield(new SubfieldImpl('a', callNumDDC));
        df090DDC.addSubfield(new SubfieldImpl('t', "DDC"));
        myCallNumRec.addVariableField(df090DDC);
        
        assertTrue(VuFindIndexer.getCallNumberByType(myCallNumRec, "090a", "t", "XXX").isEmpty());

        Set<String> lcCallStrings = new HashSet<>();
        lcCallStrings.add(callNumLC);
        Set<String> lcCallFound = VuFindIndexer.getCallNumberByType(myCallNumRec, "090a", "t", "LC");
        assertEquals(1, lcCallFound.size());
        assertTrue(lcCallFound.containsAll(lcCallStrings));

        Set<String> deweyCallSet = new HashSet<>();
        deweyCallSet.add(callNumDDC);
        assertTrue(VuFindIndexer.getCallNumberByType(myCallNumRec, "090a", "t", "DDC").containsAll(deweyCallSet));
    }
    
    @Test
    public void testGetDeweySortableByType() {
        
        // Init records
        Record myCallNumRec = new RecordImpl();
        myCallNumRec.setLeader(genericLeader);;
        
        String callNumLC = "PS 1234.5 .G78";
        DataField df090LC = new DataFieldImpl("090", ' ', ' ');
        df090LC.addSubfield(new SubfieldImpl('a', callNumLC));
        df090LC.addSubfield(new SubfieldImpl('t', "LC"));
        myCallNumRec.addVariableField(df090LC);
        
        String callNumDDC = "324.987 B34";
        DataField df090DDC = new DataFieldImpl("090", ' ', ' ');
        df090DDC.addSubfield(new SubfieldImpl('a', callNumDDC));
        df090DDC.addSubfield(new SubfieldImpl('t', "DDC"));
        myCallNumRec.addVariableField(df090DDC);
        
        assertEquals(new DeweyCallNumber(callNumDDC).getShelfKey(), 
                VuFindIndexer.getDeweySortableByType(myCallNumRec, "090a", "t", "DDC"));
    }
    
    @Test
    public void testGetLCSortableByType() {
        
        // Init records
        Record myCallNumRec = new RecordImpl();
        myCallNumRec.setLeader(genericLeader);;
        
        String callNumLC = "PS 1234.5 .G78";
        DataField df090LC = new DataFieldImpl("090", ' ', ' ');
        df090LC.addSubfield(new SubfieldImpl('a', callNumLC));
        df090LC.addSubfield(new SubfieldImpl('t', "LC"));
        myCallNumRec.addVariableField(df090LC);
        
        String callNumDDC = "324.987 B34";
        DataField df090DDC = new DataFieldImpl("090", ' ', ' ');
        df090DDC.addSubfield(new SubfieldImpl('a', callNumDDC));
        df090DDC.addSubfield(new SubfieldImpl('t', "DDC"));
        myCallNumRec.addVariableField(df090DDC);
        
        assertEquals(new LCCallNumber(callNumLC).getShelfKey(), 
                VuFindIndexer.getLCSortableByType(myCallNumRec, "090a", "t", "LC"));
    }
}
