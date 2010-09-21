package edu.stanford;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CallNumberUnitTests.class,
        CallNumberTests.class,
        CallNumberLoppingTests.class,
        ItemInfoTests.class,
        org.solrmarc.tools.CallNumberUnitTests.class
        })

        
public class AllCallnumTests 
{
}
