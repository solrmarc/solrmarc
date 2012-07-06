package org.blacklight;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    AuthorMappingTests.class,
    CallnumMappingTests.class,
    OtherMappingTests.class,
    PublicationMappingTests.class,
    SubjectMappingTests.class,
    TitleMappingTests.class
    })

     
public class AllMappingTests
{
}
