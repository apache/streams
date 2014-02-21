package org.apache.streams.kafka;

/**
 * Created by sblackmon on 12/15/13.
 */
import kafka.producer.Partitioner;
import kafka.utils.VerifiableProperties;

public class StreamsPartitioner implements Partitioner<String> {
    public StreamsPartitioner (VerifiableProperties props) {

    }

    public int partition(String key, int a_numPartitions) {
        int partition = 0;
        int offset = key.lastIndexOf('.');
        if (offset > 0) {
            partition = Integer.parseInt( key.substring(offset+1)) % a_numPartitions;
        }
        return partition;
    }

}
