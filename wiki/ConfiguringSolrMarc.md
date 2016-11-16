

# About This Guide #

This configuring SolrMarc guide explains the setup of the two configuration files that control the operation of SolrMarc and gives examples of custom indexing routines.

# About SolrMarc #

A configurable Java-based program for indexing MARC records into a Solr index.

The SolrMarc program reads in MARC records stored in standard binary (ISO 2709) or XML format and uses a configurable and customizable script for extracting values from the fields and sub-fields of the MARC record to build an index entry for adding to Solr.  Additionally, so that it can run faster, rather than building the records and POSTing them to a Solr search engine, this program is capable of writing directly to the index directory of the Solr search engine.  Standard HTTP POSTing is also available if desired.

# config.properties File #

The main configuration of the program is currently done via a properties file that is created and stored in the jar file produced by the build process. The name of this file can be passed in as the first parameter on the command line, however you can declare one particular config.properties file to be the default.  Then if a command doesn't specify which config.properties file to use, that default one will be used.

See the [ConfigProperties](ConfigProperties.md) page for details on the contents of the config.properties file.

# index.properties File #

All of SolrMarc's indexing logic is handled by an index.properties file, which controls the way in which MARC fields and subfields are mapped to specific Solr fields and provides several mechanisms for processing the data that is being indexed.

See the [IndexProperties](IndexProperties.md) page for details on the structure of the index.properties file.