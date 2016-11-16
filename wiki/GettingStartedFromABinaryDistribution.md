### About This Document ###

This getting started guide describes the SolrMarc binary distribution for Blacklight and VuFind, defines its software dependencies, guides the user through using the SolrMarc binary to index a file of MARC records.

<br />
### About SolrMarc ###

SolrMarc is a utility that reads in MARC records from a file, extracts information from various fields as specified in an indexing configuration script, and adds that information to a specified SOLR index.

SolrMarc provides a rich set of techniques for mapping the tags, fields, and subfields contained in the MARC record to the fields you wish to include in your SOLR index record, but it also allows the creation of custom index functions if you cannot achieve what you require using the predefined mapping techniques.

Currently, SolrMarc is configured to work with:
  * [Blacklight](http://blacklightopac.org/)
  * [Vufind](VufindHowTo.md)


NOTE: If you anticipate a need for custom indexing functions, you will need to download the SolrMarc source code and build the package using the instructions from the [GettingStartedFromASourceDistribution](GettingStartedFromASourceDistribution.md) document on this wiki.  However with the recent addition of support for custom indexing scripts, you can now accomplish anything you can do with a custom indexing function, by writing a custom script that will be interpreted at runtime.

<br />
### Software Dependencies ###

SolrMarc requires the java runtime environment (JRE) version 1.5 or newer (or version 1.5 or newer of the java development kit (JDK).)

To check the version of your installed java at the command prompt type:

> _java -version_

To download the proper version of the java, go to http://java.sun.com/javase/downloads/index.jsp

<br />
### Downloading the SolrMarc Binary Distribution ###

To get the binary distribution, download `SolrMarc_GenericBlacklight_Binary_Unix.tar.gz`, `SolrMarc_GenericBlacklight_Binary_PC.zip`, `SolrMarc_GenericVuFind_Binary_Unix.tar.gz` or `  	  SolrMarc_GenericVuFind_Binary_PC.zip` from [the project's downloads page](http://code.google.com/p/solrmarc/downloads/list). The binary distribution of SolrMarc, is a simpler option. The binary distribution is delivered as a single large .zip or .tar file. When unpacked this distribution will create a directory containing a large SolrMarc.jar file, a number of .properties files, and a couple of sub-directories.

The only difference between the two binary distributions is that the Unix version contains a number of bash shell scripts for running the SolrMarc indexer or for running the the other utility programs associated with running SolrMarc, whereas the PC version of the binary distribution contain batch files to perform the same tasks.

<br />
### Unpacking the Binary Distribution ###
Create a directory and copy the SolrMarc distribution you just made into it. You can do this anywhere, but for this example let's create a directory called `indexer` at the top level of either your blacklight or your vufind directory. Now unzip the distribution file. On unix, the command is _tar zxvf filename_. On windows, you can run winzip or some similar program.

<br />
### Configuring SolrMarc ###
SolrMarc uses a series of Java properties files for its configuration, and these are placed in the directory you created and unpacked the binary distribution.  Some of the values in these properties files **may** need to be set before you will be able to run SolrMarc to produce an index for your VuFind or Blacklight installation.

As distributed, the Binary releases of SolrMarc are configured to point at and write to the solr directory inside the jetty directory that is unpacked with the rest of SolrMarc.  If you want to use a different Solr installation you already have elsewhere in your system you will need to modify the config.properties file.

<br />
### Running SolrMarc ###
You will then be ready index MARC records into the solr index that will be used by your implementation of Blacklight or VuFind via the following command:

> `indexer/indexfile /path/to/marcrecords.mrc`

The command will display informational messages and warnings while it is running, processing MARC records.

To index the sample record included in the demo distribution in the test\_data directory use the following commands:

> `indexer/indexfile ./test_data/test_data.utf8.mrc`<br />
> `indexer/indexfile ./test_data/lc_records.utf8.mrc`

or to index both at one time:

> `cat ./test_data/*.mrc | indexer/indexfile `


<br />
### Changing your indexing options ###
Chances are you aren't going to want to index your own data exactly the way we have things set up for the demo application. Here's how to start making changes to the index mappings.

Go to the `indexer` directory where you unpacked SolrMarc. The properties file that control how SolrMarc will run and what fields will be added to the Solr index will be right next to the SolrMarc.jar file that contains all of the code for running SolrMarc.

<br />
#### demo\_config.properties ####
The main configuration file is named demo\_config.properties. A Blacklight example of it is shown below.
```
# Properties for the Java import program
#  for more documentation, see 
#  http://code.google.com/p/solrmarc/wiki/ConfiguringSolrMarc

# solrmarc.solr.war.path - must point to either a war file for the version of Solr that
# you want to use, or to a directory of jar files extracted from a Solr war files.  If
# this is not provided, SolrMarc can only work by communicating with a running Solr server.
# solrmarc.solr.war.path - must point to either a war file for the version of Solr that
# you want to use, or to a directory of jar files extracted from a Solr war files.  If
# this is not provided, SolrMarc can only work by communicating with a running Solr server.
solrmarc.solr.war.path=jetty/webapps/solr.war

# solrmarc.custom.jar.path - Jar containing custom java code to use in indexing. 
# If solr.indexer below is defined (other than the default of org.solrmarc.index.SolrIndexer)
# you MUST define this value to be the Jar containing the class listed there. 
solrmarc.custom.jar.path=

# - solr.indexer.properties - indicates how to populate Solr index fields from
#   marc data.  This is the core configuration file for solrmarc.
solr.indexer.properties = demo_index.properties, demo_local_index.properties

# - solr.indexer - full name of java class with custom indexing functions. This 
#   class must extend org.solrmarc.index.SolrIndexer, which is default
solr.indexer = org.solrmarc.index.SolrIndexer

# - marc_permissive - if true, try to recover from errors, including records
#  with errors, when possible
marc_permissive = true

# --- Solr instance properties -------------------------------------------------
# - solr.path - path to your Solr instance
solr.path = jetty/solr

# - solr.data.dir - path to data directory for your Solr instance 
#   (note: Solr can be configured to use a different data directory)
solr.data.dir = jetty/solr/data

# - solr.hosturl - URL of running solr search engine for getting
#   updates to Solr index via http (?).
solr.hosturl = http://localhost:8983/solr


# -- MARC data properties ------------------------------------------------------

# - marc.default_encoding - possible values are MARC8, UTF-8, UNIMARC, BESTGUESS
marc.default_encoding = MARC8

# - marc.to_utf_8 - if true, this will convert records in our import file from 
#   MARC8 encoding into UTF-8 encoding on output to index
marc.to_utf_8 = true

# - marc.include_erros - when error in marc record, dump description of error 
#   to field in solr index an alternative way to trap the indexing error 
#   messages that are logged during index time.  Nice for staff b/c they can 
#   search for errors and see ckey and record fields in discovery portal.  This 
#   field is NOT used for other queries.  Solr schema.xml must have field 
#   marc_error.
marc.include_errors = false
```
Depending on your local MARC records, you might want to change the default encoding, or other values. If you need lots of customization it's probably better to build a custom distribution from source. For instructions on building a custom distribution, please see the [GettingStarted](GettingStarted.md) doc on this wiki.

<br />
#### demo\_index.properties ####
The configuration file that handles all of the mappings from MARC to Solr is `demo_index.properties`.  The `demo_index.properties` that is configured to work with the Blacklight demo application looks like this:
```
 for more information on solrmarc mappings, 
#  see http://code.google.com/p/solrmarc/w/list 
#
# GenericBlacklight uses these conventions, mostly. 
#   _t for indexed fields (for searching)
#   _display for stored fields (for display in UI)
#   _facet for facet fields 
#   _sort for sorting fields (fields used to sort results)
#
#   see jetty/solr/conf/schema.xml in Blacklight demo project
#   see http://blacklight.rubyforge.org/  DEMO_README file

id = 001, first
marc_display = FullRecordAsMARC
text = custom, getAllSearchableFields(100, 900)

language_facet = 008[35-37]:041a:041d, language_map.properties
# format is for facet, display, and selecting partial for display in show view
format = 000[6-7]:000[6]:007[0], (map.format), first
isbn_t = 020a, (pattern_map.isbn_clean)
material_type_display = custom, removeTrailingPunct(300aa)

# Title fields
#    primary title 
title_t = custom, getLinkedFieldCombined(245a)
title_display = custom, removeTrailingPunct(245a)
title_vern_display = custom, getLinkedField(245a)
#    subtitle
subtitle_t = custom, getLinkedFieldCombined(245b)
subtitle_display = custom, removeTrailingPunct(245b)
subtitle_vern_display = custom, getLinkedField(245b)
#    additional title fields
title_addl_t = custom, getLinkedFieldCombined(245abnps:130[a-z]:240[a-gk-s]:210ab:222ab:242abnp:243[a-gk-s]:246[a-gnp]:247[a-gnp])
title_added_entry_t = custom, getLinkedFieldCombined(700[gk-pr-t]:710[fgk-t]:711fgklnpst:730[a-gk-t]:740anp)
title_series_t = custom, getLinkedFieldCombined(440anpv:490av)
title_sort = custom, getSortableTitle

# Author fields
author_t = custom, getLinkedFieldCombined(100abcegqu:110abcdegnu:111acdegjnqu)
author_addl_t = custom, getLinkedFieldCombined(700abcegqu:710abcdegnu:711acdegjnqu)
author_display = custom, removeTrailingPunct(100abcdq:110[a-z]:111[a-z])
author_vern_display = custom, getLinkedField(100abcdq:110[a-z]:111[a-z])
author_sort = custom, getSortableAuthor

# Subject fields
subject_t = custom, getLinkedFieldCombined(600[a-u]:610[a-u]:611[a-u]:630[a-t]:650[a-e]:651ae:653aa:654[a-e]:655[a-c])
subject_addl_t = custom, getLinkedFieldCombined(600[v-z]:610[v-z]:611[v-z]:630[v-z]:650[v-z]:651[v-z]:654[v-z]:655[v-z])
subject_topic_facet = custom, removeTrailingPunct(600abcdq:610ab:611ab:630aa:650aa:653aa:654ab:655ab)
subject_era_facet = custom, removeTrailingPunct(650y:651y:654y:655y)
subject_geo_facet = custom, removeTrailingPunct(651a:650z)

# Publication fields
published_display = custom, removeTrailingPunct(260a)
published_vern_display = custom, getLinkedField(260a)
# used for facet and display, and copied for sort
pub_date = custom, getDate

# Call Number fields
lc_callnum_display = 050ab, first
lc_1letter_facet = 050a[0], callnumber_map.properties, first
lc_alpha_facet = 050a, (pattern_map.lc_alpha), first
lc_b4cutter_facet = 050a, first

# URL Fields
url_fulltext_display = custom, getFullTextUrls
url_suppl_display = custom, getSupplUrls

```
See [ConfiguringSolrMarc](ConfiguringSolrMarc.md) for more information about configuration options for these files.

<br />