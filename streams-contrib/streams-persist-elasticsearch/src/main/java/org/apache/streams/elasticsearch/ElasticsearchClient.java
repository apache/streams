package org.apache.streams.elasticsearch;

/*
 * #%L
 * streams-persist-elasticsearch
 * %%
 * Copyright (C) 2013 - 2014 Apache Streams Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.elasticsearch.Version;
import org.elasticsearch.client.Client;

/**
 * Created by sblackmon on 2/10/14.
 */
public class ElasticsearchClient {

    private Client client;
    private Version version;

    public ElasticsearchClient(Client client, Version version) {
        this.client = client;
        this.version = version;
    }

    public Client getClient() {
        return client;
    }

    public Version getVersion() {
        return version;
    }
}