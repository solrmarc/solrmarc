These are files that are used (or at least could be used) as part of the automated tests for SolrMarc.  Some have been moved here as a placeholder for files that didn't belong at the top level of this repository, but they _could_ be used in automated tests.

The textfile  	indextest.txt  is the driver for the class ParameterizedIndexTest.  That test reads this file, extracts the lines defining the input, the index specification and expected output for those inputs.   It then builds the specified indexer method, send the specified MARC record to that method, and then compares the output produced to the expected value. You can optionally specify readerProperties to pass to the MARC reader, to control how it operates.   

The format of the file is as follows (note blank lines are ignored, and lines starting with # are comments)

    readerProps: marc.permissive=true, marc.to_utf_8=true, marc.unicode_normalize=false 
    record: u4.mrc(u4)
    indexSpec: 245a
    #The princes of Ha\u0300-tie\u0302n (1682-1867) /
    expect: The princes of Hà-tiên (1682-1867) /
