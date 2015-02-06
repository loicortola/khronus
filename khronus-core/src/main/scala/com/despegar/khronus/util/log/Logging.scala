/*
 * =========================================================================================
 * Copyright © 2014 the khronus project <https://github.com/hotels-tech/khronus>
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
package com.despegar.khronus.util.log

import com.despegar.khronus.model.Metric
import com.typesafe.scalalogging.Logger
import org.apache.commons.lang.time.FastDateFormat
import org.slf4j.LoggerFactory

import scala.concurrent.duration.Duration

trait Logging {

  import com.despegar.khronus.util.log.Logging._

  def loggerName = this.getClass().getName()

  val log = Logger(LoggerFactory.getLogger(loggerName))

  def p(metric: Metric, duration: Duration) = Logging.p(metric, duration)

  def p(metric: Metric, ts: Long) = Logging.p(metric, ts)

  def date(ts: Long) = df.format(ts)
}

object Logging {
  val df = FastDateFormat.getInstance("dd/MM/yyyy hh:mm:ss")

  def p(metric: Metric, duration: Duration) = s"[$metric-$duration]"

  def p(metric: Metric, durationStr: String) = s"[$metric-$durationStr]"

  def p(metric: Metric, ts: Long) =
    s"[$metric-${
      df.format(ts)
    }]"

}