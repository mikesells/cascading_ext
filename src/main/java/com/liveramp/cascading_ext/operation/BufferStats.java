/**
 *  Copyright 2012 LiveRamp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.liveramp.cascading_ext.operation;

import cascading.flow.FlowProcess;
import cascading.operation.Buffer;
import cascading.operation.BufferCall;
import cascading.tuple.TupleEntry;
import com.liveramp.cascading_ext.operation.forwarding.ForwardingBuffer;
import com.liveramp.cascading_ext.util.OperationStatsUtils;

import java.util.Iterator;

public class BufferStats extends ForwardingBuffer {

  private final ForwardingBufferCall wrapper = new ForwardingBufferCall();

  public static final String INPUT_RECORDS_COUNTER_NAME = "Input groups";
  public static final String OUTPUT_RECORDS_COUNTER_NAME = "Output records";

  private final String prefixInputRecords;
  private final String prefixOutputRecords;

  public BufferStats(Buffer buffer) {
    this(OperationStatsUtils.getStackPosition(1) + " - " + buffer.getClass().getSimpleName(), buffer);
  }

  @SuppressWarnings("unchecked")
  public BufferStats(String name, Buffer buffer) {
    super(buffer);
    this.prefixInputRecords = name + " - " + INPUT_RECORDS_COUNTER_NAME;
    this.prefixOutputRecords = name + " - " + OUTPUT_RECORDS_COUNTER_NAME;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void operate(FlowProcess process, BufferCall call) {
    wrapper.setDelegate(call);
    super.operate(process, wrapper);
    process.increment(OperationStatsUtils.COUNTER_CATEGORY, prefixInputRecords, 1);
    int output = wrapper.getOutputCollector().getCount();
    if (output > 0) {
      process.increment(OperationStatsUtils.COUNTER_CATEGORY, prefixOutputRecords, output);
    }
  }

  private static class ForwardingBufferCall<Context> extends OperationStatsUtils.ForwardingOperationCall<Context, BufferCall<Context>> implements BufferCall<Context> {

    @Override
    public TupleEntry getGroup() {
      return delegate.getGroup();
    }

    @Override
    public Iterator<TupleEntry> getArgumentsIterator() {
      return delegate.getArgumentsIterator();
    }

    @Override
    public void setDelegate(BufferCall<Context> delegate) {
      super.setDelegate(delegate);
      collector.setOutputCollector(delegate.getOutputCollector());
    }
  }
}
