# Various how-to's

#### Access HDFS from code

Run

    hdfs getconf -nnRpcAddresses
    
You will get a response like
    
    namenode:9000
    
Now you can use the name and the port, like this
    
    hdfs dfs -ls hdfs://namenode:9000/
