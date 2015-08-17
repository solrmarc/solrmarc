# Introduction #

SolrMarc can index your marc records into an Apache SOLR index. It contains an updated version of marc4j that improves handling of UTF-8 characters, is more forgiving of malformed marc data, and can recover from data errors gracefully. The SolrMarc indexer is used by Blacklight (http://projectblacklight.org/) and VuFind (http://www.vufind.org/) but it can also be used as a standalone project.

More information:  http://code.google.com/p/solrmarc/w/list

## Documentation ##

**Getting Started
  * [from a binary distribution](GettingStartedFromABinaryDistribution.md)
  * [from a source distribution](GettingStartedFromASourceDistribution.md)
  * [with Blacklight](BlacklightHowTo.md)
  * [with VuFind](VufindHowTo.md)** [Configuring SolrMarc](ConfiguringSolrMarc.md)
  * [The config.properties File](ConfigProperties.md)
  * [The index.properties File](IndexProperties.md)
    * [Pre-Defined Custom Indexing Routines](CustomIndexingRoutines.md)