streams-persist-hdfs
=====================

Read and write to HDFS

Example reader configuration:

    hdfs {
        host = "localhost"
        port = "50070"
        path = "/user/cloudera"
        user = "cloudera"
        password = "cloudera"
    }
    
Example writer configuration:

    hdfs {
        host = "localhost"
        port = "50070"
        path = "/user/cloudera"
        user = "cloudera"
        password = "cloudera"
        writerPath = "/history/twitter/example"
        writerFilePrefix = "streams-"
    }
    
        

