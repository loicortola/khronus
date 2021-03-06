/*
 * =========================================================================================
 * Copyright © 2015 the khronus project <https://github.com/hotels-tech/khronus>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 * =========================================================================================
 */

package com.despegar.khronus.store

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

import com.datastax.driver.core.Session
import com.despegar.khronus.model.{ Timestamp, _ }
import com.despegar.khronus.util.{ Measurable, Settings }
import com.esotericsoftware.kryo.io.{ Input, Output }

import scala.concurrent.duration._

trait CounterBucketStoreSupport extends BucketStoreSupport[CounterBucket] {
  override def bucketStore: BucketStore[CounterBucket] = Buckets.counterBucketStore
}

class CassandraCounterBucketStore(session: Session) extends CassandraBucketStore[CounterBucket](session) with Measurable {

  override def limit: Int = Settings.Counter.BucketLimit

  override def fetchSize: Int = Settings.Counter.BucketFetchSize

  override def tableName(duration: Duration): String = s"counterBucket${duration.length}${duration.unit}"

  override def toBucket(windowDuration: Duration, timestamp: Long, counts: Array[Byte]) = {
    new CounterBucket(Timestamp(timestamp).toBucketNumber(windowDuration), deserializeCounts(counts))
  }

  override def serializeBucket(metric: Metric, windowDuration: Duration, bucket: CounterBucket): ByteBuffer = {
    val baos = new ByteArrayOutputStream()
    val output = new Output(baos)
    output.writeByte(1)
    output.writeVarLong(bucket.counts, true)
    output.flush()
    baos.flush()
    output.close()
    val array: Array[Byte] = baos.toByteArray
    val buffer = ByteBuffer.wrap(array)
    recordGauge(formatLabel("serializedBucketBytes", metric, windowDuration), array.length)
    output.close()
    buffer
  }

  private def deserializeCounts(buffer: Array[Byte]): Long = {
    val input = new Input(buffer)
    val version = input.readByte()
    if (version == 1) {
      //TODO: versioned
    }
    val count = input.readVarLong(true)
    input.close()
    count
  }

  override def ttl(windowDuration: Duration): Int = Settings.Counter.BucketRetentionPolicy
}
