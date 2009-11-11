SolrMarc is a utility that reads in MaRC records from a file, extracts 
information from various fields as specified in an indexing configuration 
script, and adds that information to a specified Apache Solr index.

SolrMarc provides a rich set of techniques for mapping the tags, fields, and 
subfields contained in the MARC record to the fields you wish to include in your
Solr documents, but it also allows the creation of custom indexing functions if 
you cannot achieve what you require using the predefined mapping techniques.

SolrMarc comes with an improved version of marc4j that improves handling of 
UTF-8 characters, is more forgiving of malformed marc data, and can recover from
data errors gracefully. 

A number of example uses of SolrMarc are included in the examples directory.

GenericBlacklight - used to create a demo index for 
Blacklight (http://projectblacklight.org)

GenericVuFind - used to create a basic index for VuFind (http://vufind.org)

stanfordVuFind - an example of a richly populated Solr index created for the
original Stanford VuFind implementation.

stanfordBlacklight - an example of a richly populated Solr index created for
Stanford's Blacklight implementation (http://searchworks.stanford.edu/)

UvaBlacklight - an example of the indexing used for the University of Virginia's
Blacklight implementation.


Aside from inline comments, the best place to find documentation is on the 
SolrMarc project pages at

http://code.google.com/p/solrmarc/w/list
