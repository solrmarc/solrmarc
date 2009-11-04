#! /bin/bash
# build_test_index.sh
# Import a single marc file into a Solr index  (Stanford Blacklight)
#  Naomi Dushay 2008-10-12

BLACKLIGHT_HOMEDIR=/home/blacklight
SOLRMARC_BASEDIR=$BLACKLIGHT_HOMEDIR/solrmarc
SITE_BASEDIR=..
DIST_DIR=$SOLRMARC_BASEDIR/dist

RAW_DATA_DIR=$SITE_BASEDIR/test/data
#RAW_DATA_DIR=$BLACKLIGHT_HOMEDIR/data/unicorn/latest
#SOLR_DATA_DIR=$BLACKLIGHT_HOMEDIR/data/solr/dataBuild
SOLR_DATA_DIR=$SITE_BASEDIR/test/data/solr/data
SOLRMARC_JAR=$DIST_DIR/swSolrmarc.jar

JAVA_HOME=/usr/lib/jvm/java

# TODO: determine today's date and create log dir with today's date, with a suffix if necessary

# temporary! - take an argument for the date of the log subdirectory
LOG_SUBDIR=$1

# create new dist jar
ant -buildfile $SOLRMARC_BASEDIR/build.xml build

# get index directory ready 
mv $SOLR_DATA_DIR/index $SOLR_DATA_DIR/index_b4_$LOG_SUBDIR
mv $SOLR_DATA_DIR/spellchecker $SOLR_DATA_DIR/spellchecker_b4_$LOG_SUBDIR
mv $SOLR_DATA_DIR/spellcheckerFile $SOLR_DATA_DIR/spellcheckerFile_b4_$LOG_SUBDIR
mv $SOLR_DATA_DIR/spellcheckerJaroWin $SOLR_DATA_DIR/spellcheckerJaroWin_b4_$LOG_SUBDIR

# create log directory
LOG_PARENT_DIR=$RAW_DATA_DIR/logs
mkdir $LOG_PARENT_DIR
LOG_DIR=$LOG_PARENT_DIR/$LOG_SUBDIR
mkdir $LOG_DIR

# index the file
#java -Xmx1g -Xms1g -Dsolr.data.dir=$SOLR_DATA_DIR -jar $SOLRMARC_JAR $RAW_DATA_DIR/physicalTests.mrc &>$LOG_DIR/log.txt
java -Xmx1g -Xms1g -Dsolr.data.dir=$SOLR_DATA_DIR -Dsolr.optimize_at_end="true" -jar $SOLRMARC_JAR $RAW_DATA_DIR/unicornWHoldings.mrc &>$LOG_DIR/log.txt

exit 0
