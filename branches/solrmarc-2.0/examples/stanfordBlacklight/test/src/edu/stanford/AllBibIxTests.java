package edu.stanford;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    	AccessTests.class,
        AuthorTests.class,
        CallNumberTests.class,
        DiacriticTests.class,
        DisplayFieldsPropsTests.class,
        EditionTests.class,
        FacetFieldsPropsTests.class,
        FormatTests.class,
        LanguageTests.class,
        LocationTests.class,
        PhysicalTests.class,
        PublicationTests.class,
        SearchFieldsPropsTests.class,
        SeriesTests.class,
        SortFieldsPropsTests.class,
        StandardNumberTests.class,
        MiscellaneousFieldTests.class,
        SubjectTests.class,
        TitleTests.class,
        UrlTests.class,
        VernacularTests.class
        })

        
public class AllBibIxTests 
{
}
