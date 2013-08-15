#! /bin/bash
# merge_summary_holdings
# Process two files of marc records, normal records and summary holdings info.  
# Merge the summary holdings into the corresponding normal marc record
# $Id: merge_summary_holdings

E_BADARGS=65

scriptdir=$( (cd -P $(dirname $0) && pwd) )
if ! [ -e $scriptdir/SolrMarc.jar ] 
then
  scriptdir=$( (cd -P $(dirname $0)/.. && pwd) )
fi

if ! [ -p /dev/stdin ]
then  
  if [ $# -eq 0 ]
  then
    echo "    Usage: `basename $0` [-v] [-a] -s summaryHoldingsFile.mrc  normalMarcFile.mrc"
    echo "       or: cat normalMarcFile.mrc | `basename $0` [-v] [-a] -s summaryHoldingsFile.mrc "
    exit $E_BADARGS
  fi
fi

java -Dsolrmarc.main.class="org.solrmarc.tools.MergeSummaryHoldings"  -jar $scriptdir/SolrMarc.jar $1 $2 $3 $4 $5

exit 0

