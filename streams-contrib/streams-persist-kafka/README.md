streams-persist-kafka
=====================

Read and write to Kafka

Example reader / writer configuration:

    kafka {
        brokerlist = "localhost:9092"
    	zkconnect = "localhost:2181"
    	topic = "topic"
    	groupId = "group"
    }

