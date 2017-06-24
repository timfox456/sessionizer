Running on The Cluster With TCP Socket Data
------------------------------


### How to use

Do the following Steps:

### Step 1: Build

1. Build using:

```bash
mvn clean package
```

or

```bash
sbt clean package
```

### Step 2: Choose HDFS versus local files

2: We can choose whether the output will go to HDFS or to local files. HDFS is obviously
more suited for production.


Create the following directories in HDFS (if outputting to hdfs)

 - /user/cloud/ss/checkpoint
 - /user/cloud/ss/input
 - /user/cloud/ss/results
 - /user/cloud/ss/tmp

We can do this by

```bash
hdfs dfs -mkdir /user/cloud/ss/checkpoint
hdfs dfs -mkdir /user/cloud/ss/input
hdfs dfs -mkdir /user/cloud/ss/results
hdfs dfs -mkdir /user/cloud/ss/tmp
```

If going to local files, we can do the following:

```bash
   mkdir ~/ss/checkpoint
   mkdir ~/ss/input
   mkdir ~/ss/results
   mkdir ~/ss/tmp
```


### Step 3 (Optional), run a hello world Test

3. Run a "Hello World" TestStreaming command

This I have gotten to work.  We are using our data generator.

Open up a window and use netcat

```bash
./scripts/generator_run.sh | nc -lk 9999
```

Then, in a separate window, execute the following:

If you are writing out to hdfs

```bash
spark-submit --class com.createksolutions.sessionizer.TestStreaming --master local[4] --deploy-mode client --executor-memory 512M  SparkStreamingSessionization.jar socket hdfs://namenode:9000/user/cloud/ss/results hdfs://namenode:9000/user/cloud/ss/checkpoint localhost 9999 
```

If you are writing out to local file:

```bash
spark-submit --class com.createksolutions.sessionizer.TestStreaming --master local[4] --deploy-mode client --executor-memory 512M  SparkStreamingSessionization.jar socket ~/ss/results ~/ss/checkpoint localhost 9999 
```

In the first window, type in several lines of text, e.g.

```
test 1
test 2
test 3
```

### Step 4 (Optional) Check out the output of the test.

4. Then check the output of the test.  It should be in ~/output/lines-*.  Look for a directory with a nonzero file size part-*

```bash
cat ~/ss/output/lines*/part*
```

You should see whatever you entered.

### Step 5: Run Test With Real Data

6. Now run and test with real data. You will need two windows open one for generating the data, and the other for receiving it.

Here is the test data.

Run the generator to run with data.

```bash
./scripts/generator_run.sh nc -lk 9999
```

If you want to send your output to HDFS, do as follows:

```
spark-submit --class com.createksolutions.sessionizer.SessionizeData --master local[4] --deploy-mode client --executor-memory 512M  SparkStreamingSessionization.jar socket hdfs://namenode:9000/user/cloud/ss/results hdfs://namenode:9000/user/cloud/ss/checkpoint localhost 9999 
```

or you can run the script:

```bash
bash scripts/listener_ss_socket_hdfsout.sh
```

If you want to send your output to local files, do this

```
spark-submit --class com.createksolutions.sessionizer.SessionizeData --master local[4] --deploy-mode client --executor-memory 512M  SparkStreamingSessionization.jar socket ~/ss/results ~/ss/checkpoint localhost 9999 
```

or you can run the script:

```bash
bash scripts/listener_ss_socket_localout.sh
```

The generator will be generating data like this:

Example:
```
209876,5215945,2015-10-15 13:15:00,/product/Fiction,110
209269,9439975,2015-10-19 18:30:00,/cart/Political suicide,131
380163,4304843,2015-10-20 13:30:00,/product/book_bus_songs,47
822321,8088764,2015-10-23 18:45:00,/product/Science explorer,310
943997,7956641,2015-10-12 14:25:00,/cart/driving_jarvis_ham,175
521945,3414785,2015-10-16 19:20:00,/checkout/the_world_a35,310
950220,2831583,2015-10-23 18:45:00,/product/Science anytime,75
```

We can then check the output in hdfs.  It should be in the following

for hdfs: "/user/cloud/ss/results/"

for local: "~/ss/results/"

### Inspect the Session Output

If you are using HDFS output

Check for nonzero part-* files:
```bash
hdfs dfs -ls ss/results/*/session*
```

To view, pelase see these:
```bash
hdfs dfs -cat ss/results/*/session*
```

If you are using local output

Check for nonzero part-* files:
```bash
ls -l ~/ss/results/*/session*
```

To view, pelase see these:
```bash
cat ~/ss/results/*/session*
```
j
You should get something like this as a result:

```
2016-01-23 22:14:38	87284,dunno,2016-01-23 22:14:40,/cart/Assessment book,code
2016-01-23 22:14:38	87284,dunno,2016-01-23 22:14:44,/checkout/Applied psychology,code
2016-01-23 22:14:38	87284,dunno,2016-01-23 22:14:48,/checkout/california_science_a40,code
2016-01-23 22:14:38	87284,dunno,2016-01-23 22:14:52,/cart/Drama Queen,code
2016-01-23 22:14:46	63569,dunno,2016-01-23 22:14:46,/cart/Making history,code
2016-01-23 22:14:37	48102,dunno,2016-01-23 22:14:37,/product/spotlight_books,code
2016-01-23 22:14:52	61123,dunno,2016-01-23 22:14:52,/cart/Assessment book,code
2016-01-23 22:14:39	92297,dunno,2016-01-23 22:14:39,/checkout/the_world_a35,code
```

It will generate a new file for each batch, approximately every 10 seconds or so. 

There are some other files generated for the purpose of debug -- they show intermdiate steps.

### Inspect the Counts/Aggregations

Check for nonzero part-* files:
```bash
ls -l ~/ss/results/allcounts/*
```

To view, pelase see these:
```bash
cat ~/ss/results/allcounts*/*
```

You should get something like this as a result:

```
Map(DEAD_SESSION_COUNTS -> 0, TOTAL_SESSION_TIME -> 5948000, UNDER_A_MINUTE_COUNT -> 1224, OVER_TEN_MINUTES_COUNT -> 0, EVENT_COUNTS -> 2477, TOTAL_SESSION_COUNTS -> 1224, TOTAL_SESSION_EVENT_COUNTS -> 1, NEW_SESSION_COUNTS -> 521, ONE_TO_TEN_MINUTE_COUNT -> 0)
Map(DEAD_SESSION_COUNTS -> 0, TOTAL_SESSION_TIME -> 0, UNDER_A_MINUTE_COUNT -> 0, OVER_TEN_MINUTES_COUNT -> 0, EVENT_COUNTS -> 0, TOTAL_SESSION_COUNTS -> 0, TOTAL_SESSION_EVENT_COUNTS -> 0, NEW_SESSION_COUNTS -> 0, ONE_TO_TEN_MINUTE_COUNT -> 0)
Map(DEAD_SESSION_COUNTS -> 0, TOTAL_SESSION_TIME -> 5539000, UNDER_A_MINUTE_COUNT -> 1236, OVER_TEN_MINUTES_COUNT -> 0, EVENT_COUNTS -> 2402, TOTAL_SESSION_COUNTS -> 1236, TOTAL_SESSION_EVENT_COUNTS -> 1, NEW_SESSION_COUNTS -> 582, ONE_TO_TEN_MINUTE_COUNT -> 0)
Map(DEAD_SESSION_COUNTS -> 0, TOTAL_SESSION_TIME -> 0, UNDER_A_MINUTE_COUNT -> 0, OVER_TEN_MINUTES_COUNT -> 0, EVENT_COUNTS -> 0, TOTAL_SESSION_COUNTS -> 0, TOTAL_SESSION_EVENT_COUNTS -> 0, NEW_SESSION_COUNTS -> 0, ONE_TO_TEN_MINUTE_COUNT -> 0)
```

Note the counts, for exmaple, Under_A_Minute_count is 1224.

It will generate a new file for each batch, approximately every 10 seconds or so.






 
