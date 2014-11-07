package com.google.gplus.provider;

import com.google.api.services.plus.Plus;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.google.gplus.configuration.UserInfo;
import org.apache.streams.util.api.requests.backoff.BackOffStrategy;

import java.util.concurrent.BlockingQueue;

/**
 *
 */
public class GPlusUserDataProvider extends AbstractGPlusProvider{
    @Override
    protected Runnable getDataCollector(BackOffStrategy strategy, BlockingQueue<StreamsDatum> queue, Plus plus, UserInfo userInfo) {
        return new GPlusUserDataCollector(plus, strategy, queue, userInfo);
    }
}
