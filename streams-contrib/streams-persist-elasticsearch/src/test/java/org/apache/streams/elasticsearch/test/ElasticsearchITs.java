package org.apache.streams.elasticsearch.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ElasticsearchPersistWriterIT.class,
        ElasticsearchPersistUpdaterIT.class,
        ElasticsearchParentChildWriterIT.class,
        ElasticsearchParentChildUpdaterIT.class,
        DatumFromMetadataProcessorIT.class
})

public class ElasticsearchITs {
    // the class remains empty,
    // used only as a holder for the above annotations
}