Input File Data
==============

== Location

We located the file in HDFS, location hdfs://user/cloud/WebLogData.csv, and also locally
at /home/cloud/WebLogData.csv.  However, we are assuming that the final code will not
load data from hdfs (otherwise, there would be no point in doing streaming.)  Instead,
we will be doing reads from hdfs.

== Data Schema

We do not know the data schema exactly

It seems to be an:

 * ID (Integer) -- We assume this is a link into a User table
 * UnknownID (Integer) -- Possibly a cookie or other user identifier
 * Timestamp (DateTime) -- A Timestamp of the event
 * Page -- The page visited
 * Count -- Unknown what we are counting here, could be a duration (which would be useful for sessions).

== Our Usage

We currently use the following:

 * ID
 * Timestamp

Other fields are not needed for the current sessionization approach.

== Project Data Example

```
  209876,5215945,2015-10-15 13:15:00,/product/Fiction,110
  209269,9439975,2015-10-19 18:30:00,/cart/Political suicide,131
```



=== Original Data

We had been testing so far with "original data" from the project we forked from. We still maintain 
this code in the codebase, as an example

```
   66.249.283.146 - - [25/Dec/2015 06:14:01 +0000] "GET /s.html HTTP/1.1" 200 11179 "-" "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"
   66.249.260.21 - - [25/Dec/2015 06:14:01 +0000] "GET /b.html HTTP/1.1" 200 11179 "-" "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"
   66.249.181.233 - - [25/Dec/2015 06:14:01 +0000] "GET /bar.html HTTP/1.1" 200 11179 "-" "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"
```
 


