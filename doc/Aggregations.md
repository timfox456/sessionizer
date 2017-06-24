Aggregations
============

Here are the aggregations we store:

   * TOTAL_SESSION_TIME 
   * UNDER_A_MINUTE_COUNT
   * ONE_TO_TEN_MINUTE_COUNT
   * OVER_TEN_MINUTES_COUNT
   * NEW_SESSION_COUNTS
   * TOTAL_SESSION_COUNTS
   * EVENT_COUNTS
   * DEAD_SESSION_COUNTS
   * REVISTE_COUNT
   * TOTAL_SESSION_EVENT_COUNTS 

The original code used Hbase as a store for aggregations, but currently we are storing the aggregations 
files in HDFS.  



This is stored in the AllCounts hashmap, which is then committed to hdfs.

Spark does not replace old AllValues, instead, the results are written once per micro-batch to hdfs, where it
can be read by reading the latest one.

As an example (in hdfs)

```
   hdfs dfs -ls ss/results/allcounts*/  #Get the latest one
   hdfs dfs -cat ss/results/allcounts-??????/*  # Read the values
```

If you store to the local

```
  ls -l ~/ss/results/allcounts*/ # Get the latest one
  cat ~/ss/results/allcounts*/*
```

=== Database
Ultimately, we do recommend storing the counts in a database. Hbase will work, but any sql database would be fine.

=== Visualization
Here is an example of potential visualization of the counts

  * [Visualizations](Visualizations.md)
