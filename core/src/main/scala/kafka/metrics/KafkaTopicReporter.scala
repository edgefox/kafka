package kafka.metrics

import kafka.utils.{VerifiableProperties, Logging}
import com.yammer.metrics.Metrics
import java.util.concurrent.TimeUnit
import kafka.producer.{ProducerConfig}

private trait KafkaTopicReporterMBean extends KafkaMetricsReporterMBean


private class KafkaTopicReporter extends KafkaMetricsReporter
                                              with KafkaTopicReporterMBean
                                              with Logging {
  private var underlying: TopicReporter = null
  private var props: VerifiableProperties = null
  private var running = false
  private var initialized = false


  override def getMBeanName = "kafka:type=kafka.metrics.KafkaTopicReporter"


  override def init(props: VerifiableProperties) {
    synchronized {
                   if (!initialized) {
                     val metricsConfig = new KafkaMetricsConfig(props)
                     this.props = props
                     this.underlying = new TopicReporter(Metrics.defaultRegistry(), new ProducerConfig(props.props), "broker%s".format(props.getString("broker.id")))
                     if (props.getBoolean("metrics.enabled", default = true)) {
                       initialized = true
                       startReporter(metricsConfig.pollingIntervalSecs)
                     }
                   }
                 }
  }


  override def startReporter(pollingPeriodSecs: Long) {
    synchronized {
                   if (initialized && !running) {
                     underlying.start(pollingPeriodSecs, TimeUnit.SECONDS)
                     running = true
                     info("Started Kafka Topic metrics reporter with polling period %d seconds".format(pollingPeriodSecs))
                   }
                 }
  }


  override def stopReporter() {
    synchronized {
                   if (initialized && running) {
                     underlying.shutdown()
                     running = false
                     info("Stopped Kafka Topic metrics reporter")
                     underlying = new TopicReporter(Metrics.defaultRegistry(), new ProducerConfig(props.props), "broker%s".format(props.getString("broker.id")))
                   }
                 }
  }
}
