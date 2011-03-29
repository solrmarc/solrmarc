#! /bin/bash
# index_rest_sirsi.sh
# Script to finish importing if full indexing script didn't finish.
#  Naomi Dushay 2008-10-12

BLACKLIGHT_HOMEDIR=/home/blacklight
SOLRMARC_BASEDIR=$BLACKLIGHT_HOMEDIR/solrmarc
SITE_BASEDIR=..
DIST_DIR=$SOLRMARC_BASEDIR/dist

#RAW_DATA_DIR=$SITE_BASEDIR/test/data
RAW_DATA_DIR=$BLACKLIGHT_HOMEDIR/data/unicorn/latest
SOLR_DATA_DIR=$BLACKLIGHT_HOMEDIR/data/solr/dataBuild
SOLRMARC_JAR=$DIST_DIR/swSolrmarc.jar

JAVA_HOME=/usr/lib/jvm/java

# TODO: determine today's date and create log dir with today's date, with a suffix if necessary

# temporary! - take an argument for the date of the log subdirectory
 echo "   Usage: `basename $0` log_subdir(yyyy-mm-dd)"
LOG_SUBDIR=$1

# create new dist jar
#ant -buildfile $SOLRMARC_BASEDIR/build.xml build

# get index directories ready
#mv $SOLR_DATA_DIR/index $SOLR_DATA_DIR/index_b4_$LOG_SUBDIR
#mv $SOLR_DATA_DIR/spellchecker $SOLR_DATA_DIR/spellchecker_b4_$LOG_SUBDIR
#mv $SOLR_DATA_DIR/spellcheckerFile $SOLR_DATA_DIR/spellcheckerFile_b4_$LOG_SUBDIR
#mv $SOLR_DATA_DIR/spellcheckerJaroWin $SOLR_DATA_DIR/spellcheckerJaroWin_b4_$LOG_SUBDIR

# create log directory
LOG_PARENT_DIR=$RAW_DATA_DIR/logs
#mkdir $LOG_PARENT_DIR
LOG_DIR=$LOG_PARENT_DIR/$LOG_SUBDIR
#mkdir $LOG_DIR

# index the files
#nohup java -Xmx16g -Xms16g -jar $SOLRMARC_JAR $RAW_DATA_DIR/uni_00000000_00499999.marc &>$LOG_DIR/log000-049.txt
#nohup java -Xmx16g -Xms16g -jar $SOLRMARC_JAR $RAW_DATA_DIR/uni_00500000_00999999.marc &>$LOG_DIR/log050-099.txt
#nohup java -Xmx16g -Xms16g -jar $SOLRMARC_JAR $RAW_DATA_DIR/uni_01000000_01499999.marc &>$LOG_DIR/log100-149.txt
#nohup java -Xmx16g -Xms16g -jar $SOLRMARC_JAR $RAW_DATA_DIR/uni_01500000_01999999.marc &>$LOG_DIR/log150-199.txt
#nohup java -Xmx16g -Xms16g -jar $SOLRMARC_JAR $RAW_DATA_DIR/uni_02000000_02499999.marc &>$LOG_DIR/log200-249.txt
#nohup java -Xmx16g -Xms16g -jar $SOLRMARC_JAR $RAW_DATA_DIR/uni_02500000_02999999.marc &>$LOG_DIR/log250-299.txt
#nohup java -Xmx16g -Xms16g -jar $SOLRMARC_JAR $RAW_DATA_DIR/uni_03000000_03499999.marc &>$LOG_DIR/log300-349.txt
#nohup java -Xmx16g -Xms16g -jar $SOLRMARC_JAR $RAW_DATA_DIR/uni_03500000_03999999.marc &>$LOG_DIR/log350-399.txt
#nohup java -Xmx16g -Xms16g -jar $SOLRMARC_JAR $RAW_DATA_DIR/uni_04000000_04499999.marc &>$LOG_DIR/log400-449.txt
#nohup java -Xmx16g -Xms16g -jar $SOLRMARC_JAR $RAW_DATA_DIR/uni_04500000_04999999.marc &>$LOG_DIR/log450-499.txt
#nohup java -Xmx16g -Xms16g -jar $SOLRMARC_JAR $RAW_DATA_DIR/uni_05000000_05499999.marc &>$LOG_DIR/log500-549.txt
#nohup java -Xmx16g -Xms16g -jar $SOLRMARC_JAR $RAW_DATA_DIR/uni_05500000_05999999.marc &>$LOG_DIR/log550-599.txt
#nohup java -Xmx16g -Xms16g -jar $SOLRMARC_JAR $RAW_DATA_DIR/uni_06000000_06499999.marc &>$LOG_DIR/log600-649.txt
#nohup java -Xmx16g -Xms16g -jar $SOLRMARC_JAR $RAW_DATA_DIR/uni_06500000_06999999.marc &>$LOG_DIR/log650-699.txt
#nohup java -Xmx16g -Xms16g -jar $SOLRMARC_JAR $RAW_DATA_DIR/uni_07000000_07499999.marc &>$LOG_DIR/log700-749.txt
#nohup java -Xmx16g -Xms16g -jar $SOLRMARC_JAR $RAW_DATA_DIR/uni_07500000_07999999.marc &>$LOG_DIR/log750-799.txt
nohup java -Xmx16g -Xms16g -Dsolr.optimize_at_end="true" -jar $SOLRMARC_JAR $RAW_DATA_DIR/uni_08000000_08499999.marc &>$LOG_DIR/log800-849.txt

exit 0
