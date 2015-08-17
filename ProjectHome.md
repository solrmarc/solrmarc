Solrmarc can index your marc records into apache solr. It also comes with an improved version of marc4j that improves handling of UTF-8 characters, is more forgiving of malformed marc data, and can recover from data errors gracefully. This indexer is used by blacklight (http://blacklight.rubyforge.org) and vufind (http://www.vufind.org/) but it can also be used as a standalone project.

SolrMarc issue tracker can be found at: http://jira.projectblacklight.org/jira/browse/SOLRMARC

## Documentation ##

**Getting Started
  * [from a binary distribution](GettingStartedFromABinaryDistribution.md)
  * [from a source distribution](GettingStartedFromASourceDistribution.md)
  * [with Blacklight](BlacklightHowTo.md)
  * [with VuFind](VufindHowTo.md)** [Configuring SolrMarc](ConfiguringSolrMarc.md)
  * [The config.properties File](ConfigProperties.md)
  * [The index.properties File](IndexProperties.md)
    * [Pre-Defined Custom Indexing Methods](CustomIndexingRoutines.md)
    * [Writing Your Own Custom Indexing Methods in java](WritingCustomMethods.md)
    * [Writing Your Own Custom Indexing Scripts in BeanShell](WritingCustomScripts.md)