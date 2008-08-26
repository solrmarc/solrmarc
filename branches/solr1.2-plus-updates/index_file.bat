
set file=%1
java -Xmx1024M -Dmarc.path="%file" -Dsolr.optimize_at_end="false" -Dsolr.hosturl="" -jar dist/MarcImporter.jar %2