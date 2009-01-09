#! /bin/bash
# index_file.sh
# Import a single marc file into a Solr index
# $Id$

E_BADARGS=65
EXPECTED_ARGS=2

if [ $# -ne $EXPECTED_ARGS ]
then
  echo "    Usage: `basename $0` ./path/to/marc.mrc ./path/to/import.properties"
  exit $E_BADARGS
fi

file=$1
java -Xmx1024m -Dmarc.path="$file" -jar dist/MarcImporter.jar $2

exit 0

