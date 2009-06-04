package edu.stanford;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    	AccessTests.class,
        AuthorTests.class,
        CallNumberTests.class,
        DiacriticTests.class,
        EditionTests.class,
        FormatTests.class,
        ItemInfoTests.class,
        LanguageTests.class,
        MiscellaneousFieldTests.class,
        PhysicalTests.class,
        PublicationTests.class,
        TableOfContentsTests.class,
        StandardNumberTests.class,
        SubjectTests.class,
        TableOfContentsTests.class,
        TitleSearchTests.class,
        TitleTests.class,
        UrlTests.class,
        VernFieldsTests.class,
        TitleSearchVernTests.class
        })

        
public class AllBibIxTests 
{
}
