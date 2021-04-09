package com.lyft.data.gateway.ha.notifier;

import com.github.dikhan.pagerduty.client.events.PagerDutyEventsClient;
import com.github.dikhan.pagerduty.client.events.domain.Payload;
import com.github.dikhan.pagerduty.client.events.domain.Severity;
import com.github.dikhan.pagerduty.client.events.domain.TriggerIncident;
import com.github.dikhan.pagerduty.client.events.exceptions.NotifyEventException;
import com.lyft.data.gateway.ha.config.PagerDutyConfiguration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Properties;

public class PagerDutyNotifier implements Notifier {

  private final Properties props;
  private final PagerDutyEventsClient pagerDutyEventsClient = PagerDutyEventsClient.create();

  public PagerDutyNotifier(PagerDutyConfiguration pdConfig) {
    this.props = System.getProperties();
    this.props.put("integration_key", pdConfig.getIntegrationKey());
  }


  @Override
  public void sendNotification(String subject, String content) {
    sendPdAlert("Presto Error: " + subject, content);
  }

  @Override
  public void sendNotification(String from, List<String> recipients, String subject,
      String content) {
  }

  public void sendPdAlert(String subject, String content){
    Payload payload = Payload.Builder.newBuilder()
        .setSummary(content)
        .setSource("production")
        .setSeverity(Severity.ERROR)
        .setTimestamp(OffsetDateTime.now())
        .build();
    TriggerIncident incident = TriggerIncident.TriggerIncidentBuilder
        .newBuilder(String.valueOf(this.props.get("integration_key")), payload)
        .setDedupKey(subject)
        .build();
    try {
      pagerDutyEventsClient.trigger(incident);
    } catch (NotifyEventException e) {
      e.printStackTrace();
    }
  }
}
