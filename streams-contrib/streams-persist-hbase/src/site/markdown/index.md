streams-persist-hbase
=====================

Read and write to HBase

Example reader or writer configuration:

    hbase {
        rootdir = "hdfs://cloudera-vm-local:8020/hbase"
        zookeeper {
            quorum = "cloudera-vm-local"
            property {
                clientPort = 2181
            }
        }
        table = "test_table"
        family = "test_family"
        qualifier = "test_column"
    }

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
