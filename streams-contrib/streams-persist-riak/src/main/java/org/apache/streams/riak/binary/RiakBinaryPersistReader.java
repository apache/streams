package org.apache.streams.riak.binary;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistReader;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.riak.pojo.RiakConfiguration;

import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.api.commands.kv.ListKeys;
import com.basho.riak.client.api.commands.kv.MultiFetch;
import com.basho.riak.client.core.RiakFuture;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import org.apache.commons.lang.NotImplementedException;
import org.joda.time.DateTime;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by sblackmon on 1/16/17.
 */
public class RiakBinaryPersistReader implements StreamsPersistReader {

  private RiakConfiguration configuration;
  public RiakBinaryClient client;

  public RiakBinaryPersistReader(RiakConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public String getId() {
    return "RiakBinaryPersistReader";
  }

  @Override
  public void prepare(Object configurationObject) {
    client = RiakBinaryClient.getInstance(this.configuration);
  }

  @Override
  public void cleanUp() {
    client = null;
  }

  @Override
  public synchronized StreamsResultSet readAll() {

    Queue<StreamsDatum> readAllQueue = constructQueue();

    Namespace ns = new Namespace(configuration.getDefaultBucketType(), configuration.getDefaultBucket());

    ListKeys lk = new ListKeys.Builder(ns).build();

    ListKeys.Response listKeysResponse = null;
    try {
      listKeysResponse = client.client().execute(lk);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }

    MultiFetch multiFetch = new MultiFetch.Builder().addLocations(listKeysResponse).build();
    MultiFetch.Response multiFetchResponse = null;
    try {
      multiFetchResponse = client.client().execute(multiFetch);
    } catch (ExecutionException e) {
      e.printStackTrace();
      return null;
    } catch (InterruptedException e) {
      e.printStackTrace();
      return null;
    }

    for (RiakFuture<FetchValue.Response, Location> f : multiFetchResponse) {
      try {
        FetchValue.Response response = f.get();
        readAllQueue.add(new StreamsDatum(response.getValue(String.class), f.getQueryInfo().getKeyAsString()));
      }
      catch (ExecutionException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    return new StreamsResultSet(readAllQueue);
  }

  @Override
  public void startStream() {
    throw new NotImplementedException();
  }

  @Override
  public StreamsResultSet readCurrent() {
    throw new NotImplementedException();
  }

  @Override
  public StreamsResultSet readNew(BigInteger sequence) {
    throw new NotImplementedException();
  }

  @Override
  public StreamsResultSet readRange(DateTime start, DateTime end) {
    throw new NotImplementedException();
  }

  @Override
  public boolean isRunning() {
    return Objects.nonNull(client);
  }

  private Queue<StreamsDatum> constructQueue() {
    return Queues.synchronizedQueue(new LinkedBlockingQueue<StreamsDatum>(10000));
  }
}
