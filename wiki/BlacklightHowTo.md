### About This Document ###

This getting started guide describes the SolrMarc binary distribution for Blacklight, defines its software dependencies, guides the user through using the SolrMarc binary to index a file of MARC records.

<br />
### About SolrMarc ###

SolrMarc is a utility that reads in MARC records from a file, extracts information from various fields as specified in an indexing configuration script, and adds that information to a specified SOLR index.

SolrMarc provides a rich set of techniques for mapping the tags, fields, and subfields contained in the MARC record to the fields you wish to include in your SOLR index record, but it also allows the creation of custom index functions if you cannot achieve what you require using the predefined mapping techniques.

Currently, SolrMarc is configured to work with:
  * [Blacklight](http://blacklightopac.org/)
  * [Vufind](VufindHowTo.md)


NOTE: If you anticipate a need for custom indexing functions, you will need to download the SolrMarc source code and build the package using the instructions from the [GettingStarted](GettingStarted.md) document on this wiki.

<br />
### Software Dependencies ###

SolrMarc requires the java runtime environment (JRE) version 1.5 or newer (or version 1.5 or newer of the java development kit (JDK).)

To check the version of your installed java at the command prompt type:

> _java -version_

To download the proper version of the java, go to http://java.sun.com/javase/downloads/index.jsp

<br />
### Downloading the SolrMarc Binary Distribution ###

To get the binary distribution, download `Binary_Vanilla_Blacklight_SolrMarc_Unix.tar.gz` or `Binary_Vanilla_Blacklight_SolrMarc_PC.zip` from [the project's downloads page](http://code.google.com/p/solrmarc/downloads/list). The binary distribution consists of a single large jar file containing all of the code, libraries and data files to configure and run SolrMarc.

The only difference between the two binary distributions is that the Unix version contains a number of bash shell scripts for running the SolrMarc indexer or for running the the other utility programs associated with running SolrMarc, whereas the PC version of the binary distribution contain batch files to perform the same tasks.

<br />
### Unpacking the Binary Distribution ###

Create a directory and copy the solrmarc distribution you just made into it. You can do this anywhere, but for this example let's create a directory called `indexer` at the top level of your blacklight directory. Now unzip the distribution file. On unix, the command is `tar zxvf Binary_Vanilla_Blacklight_SolrMarc_Unix.tar.gz`. On windows, you can run winzip or some similar program.

<br />
### Configuring SolrMarc ###

SolrMarc uses a series of Java properties files for its configuration, and these are stored inside the single large jar file that is included in the binary distribution. Some of the values in these properties files need to be set before you will be able to run SolrMarc to produce an index for your Blacklight installation.

If you unpack the binary distribution into a directory named `indexer` inside the Blacklight demo distribution, all you need to do is run two shell scripts to configure the SolrMarc indexer.

First run:
> `indexer/setsolrwar ./jetty/webapps/solr.war`

then run:

> `indexer/setsolrhome ./jetty/solr`

### Running SolrMarc ###

You will then be ready index MARC record into the solr index that will be used by your implementation of Blacklight via the following command:

> `indexer/indexfile /path/to/marcrecords.mrc`

The command will display informational messages and warnings while it is running, processing MARC records.

To index the sample record included in the Blacklight demo distribution in the data directory use the following commands:

> `indexer/indexfile ./data/test_data.utf8.mrc`<br />
> `indexer/indexfile ./data/lc_records.utf8.mrc`

or to index both at one time:

> `cat ./data/*.mrc | indexer/indexfile`

### Changing your indexing options ###

Chances are you aren't going to want to index your own data exactly the way we have things set up for the demo application. Here's how to start making changes to the index mappings.

Go to the `indexer` directory where you unpacked solrmarc. The .jar file you see there contains several configuration files that you can extract, edit, and replace. The basic pattern looks like this:

  1. extract the file: `jar xvf Vanilla_Blacklight_SolrMarc.jar demo_index.properties`
  1. edit the file you just extracted (`demo_index.properties in this case`)
  1. replace the file: `jar uvf Vanilla_Blacklight_SolrMarc.jar demo_index.properties`

#### demo\_config.properties ####

The main configuration file is named demo\_config.properties, an example of it is shown below:
```
 # Path to your solr instance
 solr.path = /usr/local/blacklight/solr
 solr.indexer = org.solrmarc.index.SolrIndexer
 solr.indexer.properties = demo_index.properties
 #optional URL of running solr search engine to cause updates to be recognized.
 solr.hosturl = http://localhost:8983/solr/update
 marc.to_utf_8 = true
 marc.permissive = true
 marc.default_encoding = MARC8
 marc.include_errors = false
```
Depending on your local marc records, you might want to change the default encoding, or other values. If you need lots of customization it's probably better to build a custom distribution from source.

#### demo\_index.properties ####

The configuration that handles all of the mappings from marc to solr is demo\_index.properties. You will probably want to use it to get started, and then as you shape what your institution's marc mappings look like it will be worthwhile to build your own distribution from source. The demo\_index.properties that is configured to work with the blacklight demo application looks like this:
id = 001, first
marc_display = FullRecordAsMARC
text = custom, getAllSearchableFields(100, 900)

language_facet = 008[35-37]:041a:041d, language_map.properties
# format is for facet, display, and selecting partial for display in show view
format = 007[0-1]:000[6-7]:000[6], (map.format), first
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


# MAPPINGS

# format mapping
#    leader 06-07
map.format.aa = Book
map.format.ab = Serial
map.format.am = Book
map.format.as = Serial
map.format.ta = Book
map.format.tm = Book
#    leader 06
map.format.c = Musical Score
map.format.d = Musical Score
map.format.e = Map or Globe
map.format.f = Map or Globe
map.format.i = Non-musical Recording
map.format.j = Musical Recording
map.format.k = Image
map.format.m = Computer File
#    007[0]  when it doesn't clash with above
map.format.h = Microform
map.format.q = Musical Score
map.format.v = Video
#    none of the above
map.format = Unknown

pattern_map.lc_alpha.pattern_0 = ^([A-Z]{1,3})\\d+.*=>$1

pattern_map.isbn_clean.pattern_0 = ([- 0-9]*[0-9]).*=>$1}}}


See [ConfiguringSolrMarc] for more information about configuration options for these files. ```