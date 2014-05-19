package org.apache.streams.data.moreover;

/*
 * #%L
 * streams-provider-moreover
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

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsResultSet;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.Queue;

public class MoreoverResultSetWrapper extends StreamsResultSet {

    public MoreoverResultSetWrapper(MoreoverResult underlying) {
        super((Queue<StreamsDatum>)underlying);
    }

}
