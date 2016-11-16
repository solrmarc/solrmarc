# Introduction #
This page details some code profiling performed against [r1202](https://code.google.com/p/solrmarc/source/detail?r=1202) of the trunk, built using the VuFind presets.

The dataset used for the import is the USQ catalogue, with Solrmarc altered to stop at a pre-determined record count (usually 1,000 records or 10,000 records).

Before attaching a profiler the dataset was indexed into a fresh index a few times to gauge normal performance: ~130+ records per second.

With a profiler attached to the JVM during CPU profiling the performance drops to about 2 records per second, hence the low dataset of 1,000 records used during the early exploratory benchmarks.

## Details ##

The saved NetBeans snapshots are linked with each test. You should be able to download them and explore the considerable amount of data at your own leisure. My knowledge of the Solrmarc codebase is far from complete, so more eyes looking over this will get better data.

**This is a work in progress**

**Test 1** : [Results](http://code.google.com/p/solrmarc/downloads/detail?name=SnapshotCPU1.nps)<br />
The largest (single method) CPU hog here was surprisingly the logger. Pumping a line to screen for every record was consuming 16.2% of CPU for the app. To view this start from the method hotpost, right-click it and follow the call tree back up the original call into the logger inside the 'addToIndex()' method.

In my own Solrmarc install I'd always altered this just to lower the size of log files, but I never considered that it would be a real performance hit. The following modification in 'MarcImporter.java' was applied... the number is arbitrary, I use 5,000 in production, but its just personal preference.

```
if (added)
{
  recsIndexedCounter++;
  if (recsIndexedCounter == 1 || recsIndexedCounter % 100 == 0) {
    logger.info("Added record " + recsReadCounter + " read from file: " + record.getControlNumber());
  }
}
```

I ran the test again, and as expected, logging impact on CPU basically disappeared.

**Test 2** : [Results](http://code.google.com/p/solrmarc/downloads/detail?name=SnapshotCPU2.nps)<br />
Some points observed:
  * 66.2% of time is spent adding records to the index, 13.2% is spent reading/parsing marc data and 12.2% is spent in finish(); the commit is presumably the major (only?) time sink there.
  * The last significant method hotspot (locking) seems an unavoidable and understandable side effect of the commit at the end of the application, with only a single invocation.
  * If you follow the call tree of the iterator next() method there's a few areas I don't know enough about to comment on, but might show promise:
    * The UnicodeNormalizationFilter takes 16% of total execution time. The iterator calls don't seem to be the culprits, but they did leave me looking at that area where I noticed 11.7% of time is spent doing duplicate removal... could it be trimmed down?
    * At first I thought the iterator was leading me back to the snowball library used in stemming, but then I noticed it was used in UnicodeNormalizationFilter as well... and it's part of the de-dup process. Is that normal?

TBC

**Test 3** : [Results](http://code.google.com/p/solrmarc/downloads/detail?name=SnapshotCPU3.nps)<br />
_~~This test is still running, and~~ its an exact copy of Test2, but run against 10,000 records to confirm results scale._