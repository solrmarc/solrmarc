package edu.stanford;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    	AccessTests.class,
        AuthorTests.class,
        CallNumberTests.class,
        DiacriticTests.class,
        DisplayFieldTests.class,
        FormatTests.class,
        LocationTests.class,
        StandardNumberTests.class,
        StanfordFieldTests.class,
        SubjectTests.class,
        TitleTests.class,
        UrlTests.class,
        VernacularTests.class
        })

        
public class AllBibIxTests 
{
}
