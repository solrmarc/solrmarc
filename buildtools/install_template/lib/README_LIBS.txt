This directory contains the jars that are a necessary part of SolrMarc, 
all jars in this directory will be dynamically loaded at runtime.
This directory must be named "lib" and must exist as a subdirectory parallel
to the main solrmarc jar file  (named  solrmarc_core.jar )

If you have any jars that are required by your custom indexing code they _can_
be placed in this directory, but a better practice is to place them in the lib_local
directory.