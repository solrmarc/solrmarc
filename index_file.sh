#! /bin/sh

file=$1
java -Xmx1024m -Dmarc.path="$file" -Dsolr.optimize_at_end="false" -Dsolr.hosturl="" -jar dist/MarcImporter.jar $2
