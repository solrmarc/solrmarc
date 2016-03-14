# SolrMarc 2.9: Getting Started

## About This Guide

## What is SolrMarc?

SolrMarc is a utility that reads in MARC records from a file, extracts information from various fields as specified in an indexing configuration script, and adds that information to a specified SOLR index.

SolrMarc provides a rich set of techniques for mapping the tags, fields, and subfields contained in the MARC record to the fields you wish to include in your SOLR index record, but it also allows the creation of custom index functions and/or custom indexing scripts if you cannot achieve what you require using the predefined mapping techniques.

### Software Dependencies

To build SolrMarc you will need the java development kit (JDK) version 1.5 or newer and ant version 1.7 or newer.

#### Java
To check the version of the java development kit (JDK), type
javac -version

To download the proper version of the java development kit, go to http://java.oracle.com  Your operating system packager may also provide Java via the OpenJDK.


#### Ant
To check the ant version, type
`ant -version`


Solr
While SolrMarc can build a Solr index on its own and includes an installation of Jetty (a web-application server) configured to run with the latest release version of Solr.   If however you already have Solr installed and configured on your system, it is relatively simply to change the configuration files to reference your already-installed Solr instance. 

Note: SolrMarc currently is not tested against Solr 5.x versions.

## SolrMarc 2.9:  Getting Started 

There are three different ways in which you can obtain/create a working SolrMarc:  

1. Start from a Full Source distribution from the googlecode download page 
      or a full git checkout
1.  Start from a Pre-built source distribution from the googlecode download page. (??)
1. Start from a Pre-built binary distribution from the GitHub releases page (??)

The process you will then take for each of the cases to create a SolrMarc installation which is customized for your needs is similar.  The details of where edits and additions should be made in some cases are different for each of these different methods, but the steps that you have to take are largely the same.


### Getting Started from a Full Source distribution

Use git to checkout a copy of the repository:

`git clone https://github.com/solrmarc/solrmarc`

or visit https://github.com/solrmarc/solrmarc for more information.

Change into the newly created directory (`solrmarc` if you use the above command verbatim)

Next, at the command prompt, run 

`ant init`

The initialization process will ask which of the examples should be used to base your local site on:
`GenericBlacklight`,  `GenericVuFind`,  `stanfordBlacklight`,  `UvaBlacklight`,   or  `none`.

If you select one of the provided examples, all of the necessary files will be copied to the local_build directory, and you will then be ready to build a working copy of SolrMarc.   You can skip forward to the section on Customizing SolrMarc built from a Full Source distribution, or a SVN checkout

If you specify ‘none’ then you will have to answer a few more questions before you will be able to build SolrMarc, and even then you will have to extensively modify the index.properties files before your SolrMarc will be able to produce a useful solr index.   (Since the default index specification file is simply a placeholder, which specifies the record id and the title as the only fields that are added to the solr index.)

The questions it will ask are as follows:  (Note that for all of them you can accept the default responses by hitting return)

- Enter prefix for site-specific config and index properties file names:
(ie. for  `myprefix_config.properties`  and   `myprefix_index.properties`  enter  `myprefix`) 
default response: `myprefix`

- Enter java heap size to use in generated scripts for running site-specific SolrMarc Indexer: 
default response: `-Xmx256m`

While 256MB is sufficient to load the demo, a real-world indexing job requires much more. The developer uses 1024MB on his desktop and 2048MB on the server where the indexing job will include millions of records.

* Enter encoding of MARC records:
  * `MARC8` - Longstanding standard encoding scheme used by U.S. libraries
  * `UNIMARC` - Encoding scheme used in many places in Europe
  * `UTF8` - Unicode character encoding scheme, used in some newer systems
  * `BESTGUESS` - You want the program to try to determine the encoding

 default response: `MARC8`

Note that if you have a pretty good guess as to the encoding that is used in the records you will be processing, SolrMarc can run faster.  If some records are in MARC8 while others will be in UTF8, SolrMarc will be able to handle them, since SolrMarc translates all records to UTF-8 and for the records that are already in UTF-8, the translation step will be skipped.   Also note that the BESTGUESS option will try to make a determination as to the character encoding used on a record-by-record basis, however since many records do not have large numbers of “special characters” (ie. ones outside the basic 7-bit ascii range) the information available for deciding the correct encoding can be minimal.) 

- Do you want to use the 'builtin' solr configuration, jetty configuration and solr.war file
     or do you want to use a 'custom' one you have already installed.
     Note: You can start from the builtin version and modify to suit your needs. 
     Or you can change the one used later by modifying the build.properties file.
default response: builtin

If you answer “custom”, you will be asked for the URL of where your Solr server will be running, the full path location of your solr home directory, and the full path of the solr.war file that the Solr server uses for running solr.   

Note one option that you can use if the Solr server will be running on a different machine than where you intend to run SolrMarc, and you want to remotely access that running Solr server, enter the URL of the Solr server for the first response, and then enter “REMOTE” for the question: “Enter full path of Solr home directory”  and enter an empty string for the question: “Enter full path of where solr.war file is located (include solr.war at end)”

These custom settings are now written out to a build.properties file, in the local_build directory, and they will be used when you run the “ant dist” target to correctly fill in values in the configuration files.

### Customizing SolrMarc built from a Full Source distribution, or a SVN checkout or customizing SolrMarc based on a pre-built Source distribution.

If you look in the “local_build” directory after you complete the “ant init” step, you will see a number of directories, a couple of properties files, and a build.xml file.  These files and directories are explained below.   At this point you will be at virtually the same point that you would be if you had merely obtained a pre-built source distribution.

At this point you can modify the files in the local_build directory to customize your installation.  
-You can modify the xxxx_config.properties file that specifies how the overall SolrMarc program will run, how MARC files should be processed as they are read, what file (or files) defines the indexing specification to use, whether Marc processing errors should be added to the Solr index.  
-You can modify the xxxx_index.properties files to change what fields are added to the index and where and how the data for those fields is extracted. (Note that any fields added to this file must­ match either a field declaration in the schema.xml file that is a part of the solr configuration or match a dynamic field declaration in that file.)

You can:

- add custom indexing java routines in the src directory, and reference them in the `xxxx_index.properties` file.
- add custom java-like indexing scripts in the index_scripts directory,  and reference them in the `xxxx_index.properties` file.
- add or modify translation maps that map values found in the MARC records to more-readable values.
- change entries found in the `build.properties` file that are copied into the `xxxx_config.properties` file or into the bash script or batch files from the `script_templates` directory.
- modify the solr configuration, which can be done by editing files or adding new files in the solrConf directory, or by going directly to the test/solr directory and adding or editing files there.  
- You can add tests to the test/data/indextest.txt file so that you can help ensure that as you make any of the above changes you don’t accidently break some other aspect of your indexing configuration.  


After you have made the desired changes, you should run “ant dist” which will copy all the necessary files to the distribution directory.  You can change to that directory, or merely add `dist/bin` to your executable path, and run the indexing utilities (described in a later section) from any command prompt.
Note: you should refrain from editing files in the distribution directory directly when you are working from a source distribution, since those changes could be overwritten the next time you run “ant dist”.

## Files/directories in local_build directory, or Prebuilt Source Distribution,

`build.xml` 
 Generated Ant build file to compile custom source code, bundle classes and additional data into jar, and generate working distribution directory by copying and/or modifying files found in the local_build directory.

`build.properties`
 Used by the Ant build process to define properties used by the build process.  Some of the values defined in this file are inserted into the xxxxx_config.properties file as it is copied into the distribution directory.  Other values are inserted into the bash scripts or batch files that are copied from the script_templates directory to the dist/bin directory.

`xxxxx_config.properties`
 Specifies values used to configure a particular run of SolrMarc.  Note that some of the values in this file can be expressed in terms of named properties (such as  solr.path = ${solr.path} ) and the value of that property will be substituted in (usually) as the ant build process copies this file to the distribution directory.

`xxxxx_index.properties`
 Specifies all of the indexing rules for mapping fields and subfields in the marc records that will be processed to the solr index fields that will be added to the solr index by SolrMarc. Note that any fields added to this file must­ match either a field declaration in the schema.xml file that is a part of the solr configuration or match a dynamic field declaration in that file.  The schema that will be used by default can be found in the solrConf directory listed below.

`bin`
 Directory where class files from custom indexing methods are placed. Also where the .jar file containing all of those custom class files are placed.

`buildtools`
 Contains files that add functionality used by the ant build process, or are used by the smoketest target in the ant build process
`extra_data`
 Directory to contain resources that are to be placed in the custom .jar file, these might be text files that are used by custom indexing methods. Most sites will not need any of these, and therefore can safely ignore this directory.

`index_scripts`
 Directory to contain custom java-like beanshell script files to handle custom indexing tasks.  This is an alternative to creating custom java indexing routines, it is slightly easier to create and modify these scripts, but slightly slower to run them as compared to compiled java versions implementing the same code. 

`lib`
 Contains the already-compiled SolrMarc.jar file that is virtually identical for all users of SolrMarc.   The only difference being that the name of the “default configuration” to use will be recorded in the Manifest within the Jar file as the file is copied to the distribution directory by the “ant dist” target.

`script_templates`
 Contains a number of bash scripts and batch files that invoke SolrMarc in a number of useful ways.  The Ant build process will copy the platform-appropriate versions of these files to the bin subdirectory of the distribution directory.  In the process of copying them the Ant build process will take some values defined in the build.properties file, and substitute them into this script files.

`solrConf`
 Contains modifications to the configuration of solr. Usually this would consist merely of a copy of solrconfig.xml and schema.xml which would then be placed in dist/jetty/solr/conf directory.  However it can also include a different version of solr.war or even a multicore solr.xml file and subdirectories containing the configurations for each of those multiple cores.

`src`
 Contains java source code defining custom indexing routines.  Note that the main custom class must extend the SolrMarc class org.solrmarc.index.SolrIndexer  .  The Ant build process should detect the fact that a custom indexing class is included here, and correctly set the ant properties  custom.jar.name.jar   and  custom.indexer.class  that will be used when copying the xxxxx_config.properties file to the distribution directory.

`translation_maps`
 Contains properties files defining translations maps that are used and referenced by indexing specification entries in the xxxxx_index.properties file, or by custom indexing routines, or custom indexing scripts.  These translation maps usually map short mnemonic strings found in MARC records to longer strings that are more meaningful to the user.  ( For instance mapping the three letter language value found in a MARC record to the full value of that language: eng=English

`test`
 Contains several subdirectories associated with testing a SolrMarc installation.

`test/bin`
 Contains compiled versions of site specific junit testing code, and a .jar file that is built contain all such compiled junit testing code.  Many sites will not have any such specialized tests.
`test/data`
 Contains files used as data by various tests that are performed by the “test” target in the Ant build process.  Mostly this consists of sample MARC  records that are indexed by one or another of the ant tests.  
One notable file in this directory is `indextest.txt` in which each line specifies a specific test that ought to be performed.  A sample line appears below:

```blacklight_config.properties, u11.mrc, barcode_facet, 11-1001```

This says use the configuration: `blacklight_config.properties`, and run the indexer on the file u11.mrc,  and then in the index field named “barcode_facet” produced by the indexing process, the value ought to be the string “11-1001”  

`test/jetty`
 Contains an installation of jetty that for the most part will be copied directly to the distribution directory

`test/solr`
 Contains a solr configuration that will be used as the basis for what is copied to the dist/jetty directory.  Files that are in the solrConf directory (described above) will be used to override the default versions of them that are found within this directory.

`test/src`
 Contains the source code for site specific junit testing code.  Many sites will not have any such specialized tests and will rely on the higher level ant test targets: “smoketest” and “indextest”


### Getting Started from a Prebuilt Binary distribution

(warning: currently this project does not supply prebuilt binary distributions)

The directory that results from installing a Prebuilt binary distribution, downloaded from the solrmarc googlecode download page, is virtually identical to the distribution directory that would result if you installed a source distribution.  However since it doesn’t include the “local_build” directory described above, any changes to the distribution that would require re-compiling java code cannot be done with a binary distribution.   

You can:

- modify the xxxx_config.properties file that specifies how the overall SolrMarc program will run, how MARC files should be processed as they are read, what file (or files) defines the indexing specification to use, whether Marc processing errors should be added to the Solr index.  
- modify the xxxx_index.properties files to change what fields are added to the index and where and how the data for those fields is extracted. (Note that any fields added to this file must­ match either a field declaration in the schema.xml file that is a part of the solr configuration or match a dynamic field declaration in that file.)
- add custom java-like indexing scripts in the index_scripts directory, and reference them in the xxxx_index.properties file.
- add or modify translation maps that map values found in the MARC records to more-readable values.
- modify the solr configuration, which can be done by editing files in the jetty/solr directory and adding or editing files there.  


#### Configuration files:

The above initialization step, will create a directory named local_build, and place all of the files necessary to configure, build and test a version of SolrMarc.    If you selected one of the provided examples, and if your Solr index needs are the same as what is produced by that examples indexing specification, you are ready to start indexing records.  If however you need to handle records differently from the way the example Fill in the xxxx_config and xxxx_index files in the site specific directory that you've just created using the template files that were created in the build.

`xxxx_config.properties`: controls overall operation of the program

`xxxx_index.properties`: where the mapping from MARC tags to SOLR fields is specified

Because it is possible to have multiple instances of SolrMarc running on a server, the prefix helps the system administrator know to which instance the configuration files refer.

Configuration file: bob working to create generator script to fill in site specific values.
```
# Properties for the Java import program
# $Id: importSamples.properties 17 2008-06-20 14:40:13Z wayne.graham $

# Path to your solr instance
solr.path = /usr/local/projects/bl-demo/bl-demo/jetty/solr
solr.indexer = org.solrmarc.index.SolrIndexer
solr.indexer.properties = demo_index.properties

#optional URL of running solr search engine to cause updates to be recognized.
solr.hosturl = http://localhost:8983/solr/update

# Path to your marc file
marc.to_utf_8 = true
marc.permissive = true
marc.default_encoding = MARC8
marc.include_errors = true
```

Index file:
Long, fairly complex.
[Look at index.properties from demo app and pull out examples to show what file looks like.
[point out to an index file and show some reasonable answers==demo app, for example]

[see also MarcImporter file]

*** NEED TO RE-EDIT THE MARC IMPORTER FILE; EDIT WIKI DOC!***

### Building the Importer Program

Once you have finished editing the xxxx_config.properties file and the xxxx_index.properties file located in your site specific directory, change back to the top level directory where you first ran  ant init,  and run   ant build   which will compile all of the standard source code for the importer, compile any custom source code that you have created in your site specific directory, and create a large jar file containing all of the compiled code, any jar files needed by the code, and the properties files that control how the program runs, (including your xxxx_ config.properties, and your  xxxx_index.properties.)  Assuming no errors are encountered in the build process, the build is now complete.

A directory called .dist/ is created by the build process. The builder places the jar file that contains all the code and libraries for SolrMarc to run in this directory.   Additionally, several shell scripts/batch files that make using the indexing program and its associated utilities much easier to run are placed in this dist directory.


### Batch Files/Shell Scripts

* `indexfile` —indexes file of MARC records, adding all data from those records to your Solr index.

*  `printrecord`—looks at the MARC records and transforms them into human-readable format.
  Specify the name of the MARC file, prints all records to the screen.
  Helpful for troubleshooting indexing operations, and for selecting tags/fields/subfields for mapping.

* `indextest`—processes the file of MARC records and displays to the screen what it will be sending  to SOLR for the records in the file.

* `filterrecords` -could be called “marcgrep” selects only those records that have only certain values in certain fields or certain fields.
  Specify the pattern you're seeking in the file on the command line.

* `getrecord`—specify a record id, and extracts that record from a MARC file.

* `updatesolrserver`—solr utility program that sends a commit message to the URL where solr is running to update a running instance with new index data.

* `optimizesolr`—optimizes the solr index to be a single segment.

