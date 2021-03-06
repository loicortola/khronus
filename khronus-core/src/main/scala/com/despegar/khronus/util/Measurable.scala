package com.despegar.khronus.util

import com.despegar.khronus.model.{ Metric, MonitoringSupport }
import com.despegar.khronus.util.log.Logging

import scala.concurrent.duration.Duration
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Success

trait Measurable extends Logging with SlowMetricsRecorder {

  private def now = System.currentTimeMillis()

  def measureTime[T](label: String, text: String, doLog: Boolean = true)(block: ⇒ T): T = {
    val start = now
    val blockReturn = block
    val elapsed = now - start
    if (doLog) log.info(s"$text - time spent: ${elapsed}ms")
    recordTime(label, elapsed)
    blockReturn
  }

  def measureTime[T](label: String, metric: Metric, duration: Duration)(block: ⇒ T): T = {
    if (!metric.isSystem) {
      measureTime(formatLabel(label, metric, duration), s"${p(metric, duration)} $label")(block)
    } else {
      block
    }
  }

  def measureFutureTime[T](label: String, metric: Metric, duration: Duration)(block: ⇒ Future[T])(implicit ec: ExecutionContext): Future[T] = {
    if (!metric.isSystem) {
      measureFutureTime(formatLabel(label, metric, duration), s"${p(metric, duration)} $label")(block)
    } else {
      block
    }
  }

  def measureFutureTime[T](label: String, text: String)(block: ⇒ Future[T])(implicit ec: ExecutionContext): Future[T] = {
    val start = now
    block andThen {
      case Success(_) ⇒ {
        val elapsed = now - start
        log.debug(s"$text - time spent: ${elapsed}ms")
        recordTime(label, elapsed)
      }
    }
  }
}
