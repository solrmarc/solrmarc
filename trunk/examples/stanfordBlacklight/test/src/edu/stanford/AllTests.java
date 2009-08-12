package edu.stanford;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    	AccessTests.class,
        AuthorTests.class,
        CallNumberLoppingTests.class,
        CallNumberTests.class,
        CallNumberUnitTests.class,
        DiacriticTests.class,
        FormatTests.class,
        ItemInfoTests.class,
        LanguageTests.class,
        MiscellaneousFieldTests.class,
        PhysicalTests.class,
        PublicationTests.class,
        StandardNumberTests.class,
        SubjectTests.class,
        TableOfContentsTests.class,
        TitleSearchTests.class,
        TitleSearchVernTests.class,
        TitleTests.class,
        UrlTests.class,
        VernFieldsTests.class
        })

        
public class AllTests 
{
}
