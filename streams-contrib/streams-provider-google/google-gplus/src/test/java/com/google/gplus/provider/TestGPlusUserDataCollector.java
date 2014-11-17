package com.google.gplus.provider;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.google.gplus.configuration.UserInfo;
import org.apache.streams.util.api.requests.backoff.BackOffStrategy;
import org.apache.streams.util.api.requests.backoff.impl.ConstantTimeBackOffStrategy;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Basic Units for {@link com.google.gplus.provider.GPlusUserDataCollector}
 */
public class TestGPlusUserDataCollector {

    private static final String NO_ERROR = "no error";


    /**
     * Test that on success a datum will be added to the queue.
     * @throws Exception
     */
    @Test
    public void testSucessfullPull() throws Exception {
        Plus plus = createMockPlus(0, null);
        BackOffStrategy backOff = new ConstantTimeBackOffStrategy(1);
        BlockingQueue<StreamsDatum> datums = new LinkedBlockingQueue<>();
        UserInfo user = new UserInfo();
        user.setUserId("A");

        GPlusUserDataCollector collector = new GPlusUserDataCollector(plus, backOff, datums, user);
        collector.run();

        assertEquals(1, datums.size());
        StreamsDatum datum = datums.take();
        assertNotNull(datum);
        assertEquals(NO_ERROR, datum.getId());
        assertNotNull(datum.getDocument());
        assertTrue(datum.getDocument() instanceof String);
    }

    /**
     * Test that on failure, no datums are output
     * @throws Exception
     */
    @Test
    public void testFail() throws Exception {
        Plus plus = createMockPlus(3, mock(GoogleJsonResponseException.class));
        UserInfo user = new UserInfo();
        user.setUserId("A");
        BlockingQueue<StreamsDatum> datums = new LinkedBlockingQueue<>();
        BackOffStrategy backOffStrategy = new ConstantTimeBackOffStrategy(1);

        GPlusUserDataCollector collector = new GPlusUserDataCollector(plus, backOffStrategy, datums, user);
        collector.run();

        assertEquals(0, datums.size());
    }



    private Plus createMockPlus(final int succedOnTry, final Throwable throwable) {
        Plus plus = mock(Plus.class);
        doAnswer(new Answer() {
            @Override
            public Plus.People answer(InvocationOnMock invocationOnMock) throws Throwable {
                return createMockPeople(succedOnTry, throwable);
            }
        }).when(plus).people();
        return plus;
    }

    private Plus.People createMockPeople(final int succedOnTry, final Throwable throwable) {
        Plus.People people = mock(Plus.People.class);
        try {
            when(people.get(anyString())).thenAnswer(new Answer<Plus.People.Get>() {
                @Override
                public Plus.People.Get answer(InvocationOnMock invocationOnMock) throws Throwable {
                    return createMockGetNoError(succedOnTry, throwable);
                }
            });
        } catch (IOException ioe) {
            fail("No Excpetion should have been thrown while creating mocks");
        }
        return people;
    }

    private Plus.People.Get createMockGetNoError(final int succedOnTry, final Throwable throwable) {
        Plus.People.Get get = mock(Plus.People.Get.class);
        try {
            doAnswer(new Answer() {
                private int counter =0;

                @Override
                public Person answer(InvocationOnMock invocationOnMock) throws Throwable {
                    if(counter == succedOnTry) {
                        Person p = new Person();
                        p.setId(NO_ERROR);
                        return p;
                    } else {
                        ++counter;
                        throw throwable;
                    }
                }
            }).when(get).execute();
        } catch (IOException ioe) {
            fail("No Excpetion should have been thrown while creating mocks");
        }
        return get;
    }



}
