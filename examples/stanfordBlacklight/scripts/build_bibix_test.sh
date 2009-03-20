#! /bin/bash
# build_bibix_test.sh
# Import a single marc file into a Solr index  (Stanford Blacklight 
#  Naomi Dushay 2008-10-12

SOLRMARC_BASEDIR=../../..
RAW_DATA_DIR=$SOLRMARC_BASEDIR/test/data
BLACKLIGHT_BASEDIR=~
SOLR_DATA_DIR=$BLACKLIGHT_BASEDIR/solr/solr1.4/data
CONFIG_PROPS=bibix_config.properties
SOLRMARC_JAR=$SOLRMARC_BASEDIR/dist/stanfordBlacklightSolrmarc.jar

JAVA_HOME=/usr/lib/jvm/java

# TODO: determine today's date and create log dir with today's date, with a suffix if necessary

# temporary! - take an argument for the date of the log subdirectory
EXPECTED_ARGS = 1;
if [$# -ne $EXPECTED_ARGS]
then
  echo "   Usage: `basename $0` log_subdir(yyyy-mm-dd)"
  exit 65
fi

LOG_SUBDIR=$1

# create new dist jar
ant ../dist

# get index directory ready 
mv $SOLR_DATA_DIR/index $SOLR_DATA_DIR/index_b4_$LOG_SUBDIR

# create log directory
LOG_PARENT_DIR=$RAW_DATA_DIR/logs
mkdir $LOG_PARENT_DIR
LOG_DIR=$LOG_PARENT_DIR/$LOG_SUBDIR
mkdir $LOG_DIR


# index the file
java -Xmx1g -Xms1g -cp dist:dist/lib -Dmarc.path=$RAW_DATA_DIR/unicornWHoldings.mrc -Dsolr.optimize_at_end="true" -jar $SOLRMARC_JAR $CONFIG_PROPS &>$LOG_DIR/log.txt

exit 0
