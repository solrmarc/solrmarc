Installing and using the binary distribution of SolrMarc

The binary distribution of SolrMarc contains one large jar file that contains most of 
the java code, the java libraries, and the properties files needed for running SolrMarc.

The distribution contains a number of shell scripts for configuring, testing and running 
SolrMarc to index MARC records into your Solr index.  Because SolrMarc needs to know where
your Solr configuration files are, where the Solr index data files should go and where the 
solr java library jar files (or solr.war file containing the solr jar files) for the 
version of solr you will be using is located, you will need to configure the binary 
distribution before you will be able to use it.  

To configure SolrMarc you need to run two shell scripts which are included as a part of 
the binary distribution.  Note that both of these configuration shell scripts ( setsolrhome  
and  setsolrwar ) use the java command  jar  internally, so you need to have a Java JDK 
installed, not just a Java Runtime Environment.
 

setsolrwar /full/path/to/solr.war
    
    or

setsolrwar ../relative/path/from/current/directory/to/solr.war

    or

setsolrwar /full/path/to/directory/with/solrjars
    
    or

setsolrwar ../relative/path/from/current/directory/to/directory/with/solrjars


This command actually modifies several of the shell scripts that are included as a part
of the binary distribution setting a shell variable that is then passed to the java command
that starts SolrMarc, so that it can find the version of solr you are using.


Next you will need to tell SolrMarc where the configuration files for you installation of 
solr is located, via the shell script  setsolrhome .  You can also use use that command to
set the URL on the local machine where the solr server will be run.   If you will be running 
solr using jetty, the URL will probably be:  http://localhost:8983/solr   if you are running
solr via tomcat, the URL will probably be:  http://localhost:8080/solr  

The URL is important because SolrMarc writes directly to the solr index data files, and the 
running solr server (in jetty or tomcat) will need to be notified when SolrMarc finishes 
updating the index.  Otherwise the solr server will continue to show and return results from
before SolrMarc was run, until the next time the solr server is restarted.

To view the current settings that will be used by SolrMarc when it is run, you can run 

setsolrhome 

without any parameters.   It will display something like the following:

jar = /full/path/to/where/binary/distribution/was/unpacked/Vanilla_Blacklight_SolrMarc.jar
config = demo_config.properties
solrpath = @SOLR_PATH@
url = http://localhost:8983/solr/update

Note that the solrpath displayed is set to a placeholder value @SOLR_PATH@, trying to run 
a SolrMarc command that accesses the solr index will fail horribly.

To set the value to use for solrpath you need to run the command and pass in the path of where
the solr configuration can be found:

setsolrhome /full/path/to/directory/containing/solr/conf

    or
       
setsolrhome ./relative/path/to/directory/containing/solr/conf


Note that if the url at which the solr server will be running is incorrect, this command can also 
be used to set that value as well.  Thusly:

setsolrhome /full/path/to/directory/containing/solr/conf  http://localhost:8080/solr

    or
       
setsolrhome ./relative/path/to/directory/containing/solr/conf  http://localhost:8080/solr


Also if the solr server is already running at a particular URL address, you can simply specify
the URL of where the solr server is running, and this command will talk to that server to retrieve
the full path of where the solr configuration and the solr data are located.

setsolrhome  http://localhost:8080/solr

    or
       
setsolrhome  http://localhost:8080/solr




