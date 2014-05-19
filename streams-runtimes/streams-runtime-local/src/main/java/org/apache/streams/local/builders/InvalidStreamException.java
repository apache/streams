package org.apache.streams.local.builders;

/*
 * #%L
 * streams-runtime-local
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

/**
 * Exception that indicates a malformed data stream in some way.
 */
public class InvalidStreamException extends RuntimeException {

    public InvalidStreamException() {
        super();
    }

    public InvalidStreamException(String s) {
        super(s);
    }

    public InvalidStreamException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public InvalidStreamException(Throwable throwable) {
        super(throwable);
    }
}
