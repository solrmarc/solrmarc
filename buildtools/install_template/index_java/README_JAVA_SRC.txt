You should place custom java indexing routines in the src subdirectory. 
All java source files place in that directory will be compiled when SolrMarc is run. 
The java class files generated for those source files will be placed the bin 
subdirectory and then be dynamically loaded by SolrMarc.