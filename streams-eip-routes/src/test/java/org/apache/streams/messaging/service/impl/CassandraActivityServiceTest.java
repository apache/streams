/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.streams.messaging.service.impl;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CassandraActivityServiceTest {

    private CassandraActivityService cassandraActivityService;

    @Before
    public void setup(){
//        cassandraActivityService = new CassandraActivityService();
    }

    @Ignore
    @Test
    public void getActivititiesForFilterTest(){
        List<String> activities = cassandraActivityService.getActivitiesForFilters(Arrays.asList("r501"), new Date(0));
    }

    @Ignore
    @Test
    public void receiveExchangeTest(){
        Exchange e = createMock(Exchange.class);
        List<Exchange> grouped = new ArrayList<Exchange>();
        Exchange e2 = createMock(Exchange.class);
        grouped.add(e2);
        Message m = createMock(Message.class);

        String activityJson = "{\n" +
                "\"id\":\"id2\",\n" +
                "\"verb\":\"verb2\",\n" +
                "\"displayName\":\"displayname2\",\n" +
                "\"target\":{\n" +
                "\t\"id\":\"targetid2\",\n" +
                "\t\"displayName\":\"targetname2\"\n" +
                "\t},\n" +
                "\t\"object\":{\n" +
                "\t\"id\":\"objectid2\",\n" +
                "\t\"displayName\":\"objectname2\"\n" +
                "\t},\n" +
                "\t\"actor\":{\n" +
                "\t\"id\":\"actorid2\",\n" +
                "\t\"displayName\":\"actorname2\"\n" +
                "\t}\n" +
                "\t\n" +
                "\t}";

        expect(e.getProperty(Exchange.GROUPED_EXCHANGE, List.class)).andReturn(grouped);
        expect(e2.getIn()).andReturn(m);
        expect(m.getBody(String.class)).andReturn(activityJson);

        replay(e, e2, m);

        cassandraActivityService.receiveExchange(e);
        //List<String> myTest = cassandraActivityService.getActivitiesForQuery("select * from coltest");
    }
}
