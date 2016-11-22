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

package org.apache.streams.core;

import java.io.Serializable;

@Deprecated
public class DatumStatusCounter implements Serializable {

  private volatile int attempted = 0;
  private volatile int success = 0;
  private volatile int fail = 0;
  private volatile int partial = 0;
  private volatile int emitted = 0;

  public int getAttempted() {
    return this.attempted;
  }

  public int getSuccess() {
    return this.success;
  }

  public int getFail() {
    return this.fail;
  }

  public int getPartial() {
    return this.partial;
  }

  public int getEmitted() {
    return this.emitted;
  }

  public DatumStatusCounter() {
  }

  /**
   * accumulate partial DatumStatusCounter.
   * @param datumStatusCounter DatumStatusCounter
   */
  @Deprecated
  public void add(DatumStatusCounter datumStatusCounter) {
    this.attempted += datumStatusCounter.getAttempted();
    this.success += datumStatusCounter.getSuccess();
    this.partial = datumStatusCounter.getPartial();
    this.fail += datumStatusCounter.getFail();
    this.emitted += datumStatusCounter.getEmitted();
  }

  @Deprecated
  public void incrementAttempt() {
    this.attempted += 1;
  }

  @Deprecated
  public void incrementAttempt(int counter) {
    this.attempted += counter;
  }

  /**
   * increment specific DatumStatus by 1.
   * @param workStatus DatumStatus
   */
  @Deprecated
  public synchronized void incrementStatus(DatumStatus workStatus) {
    // add this to the record counter
    switch (workStatus) {
      case SUCCESS:
        this.success++;
        break;
      case PARTIAL:
        this.partial++;
        break;
      case FAIL:
        this.fail++;
        break;
      default:
        break;
    }
    this.emitted += 1;
  }

  /**
   * increment specific DatumStatus by count.
   * @param workStatus DatumStatus
   * @param counter counter
   */
  @Deprecated
  public synchronized void incrementStatus(DatumStatus workStatus, int counter) {
    // add this to the record counter
    switch (workStatus) {
      case SUCCESS:
        this.success += counter;
        break;
      case PARTIAL:
        this.partial += counter;
        break;
      case FAIL:
        this.fail += counter;
        break;
      default:
        break;
    }
    this.emitted += counter;
  }

  @Override
  public String toString() {
    return "DatumStatusCounter{"
        + "attempted=" + attempted
        + ", success=" + success
        + ", fail=" + fail
        + ", partial=" + partial
        + ", emitted=" + emitted
        + '}';
  }
}
