#! /bin/bash
# index_file.sh
# Import a single marc file into a Solr index
# $Id: indexfile.sh 

E_BADARGS=65
config=@DEFAULTCONFIG@

if [ $# -ne 2 && $# -ne 3 ]
then
  echo "    Usage: `basename $0` config.properties ./path/to/marc.mrc [./path/to/ids_to_delete.del]"
  exit $E_BADARGS
fi

java -Xmx1024m -Done-jar.class.path="@LOCALJAR@|@SOLRJAR@" -jar SolrMarc.jar $1 $2 $3

exit 0

