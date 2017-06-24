Sessionizer
------------------------------

### About

I based this on Cloudera's blog (which did not work out of the box).

Blog: http://blog.cloudera.com/blog/2014/11/how-to-do-near-real-time-sessionization-with-spark-streaming-and-apache-hadoop/

Developer:

  * Tim Fox, tfox@createksolutions.com

### Status

Sample code from here, https://github.com/tmalaska/SparkStreaming.Sessionization, modified further
for its needs

- I Removed hbase, so we are just writing to a file in hdfs.
- I also updated maven build and added SBT build.
- Note: more documentation is in the 'doc' directory


### Problem
This is an example of how to use Spark Streaming to Sessionize web log data by ip address.  
This will mean that we are sessionizing in NRT and landing the results on HDFS.


- Number of events
- Number of active sessions
- Average session time
- Number of new sessions
- Number of dead sessions

### Input Data
  * [Input Data Information](doc/FileFormat.md)

### How to Build 

1. Build using:

```bash
mvn clean package
```

or

```bash
sbt clean package
```


### How to Run

  * [Running over TCP socket](doc/RunSocket.md)

Spark Streaming is not able to run local input (though it can produce local ouput.)
I do not support running from hdfs files, as this never worked even in the original
CLoudera project.

### Aggregations

I am keeping track of counts in an hdfs file (in the current approach). Here is 
some information on the aggreagations we do:

  * [Aggregations](doc/Aggregations.md)

### Miscellaneous

  * [Various HowTos](doc/HOW_TO.md)
  * [Visualizations](doc/Visualizations.md)
  
