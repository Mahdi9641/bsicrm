package com.bsicenter.crm.integration.sms;

import com.bsicenter.crm.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationScheduler {
  private final NotificationService service;
  private final boolean enabled;

  public NotificationScheduler(
      NotificationService service, @Value("${app.automation-enabled}") boolean enabled) {
    this.service = service;
    this.enabled = enabled;
  }

  @Scheduled(
      fixedDelayString = "${app.automation-interval-ms:60000}",
      initialDelayString = "${app.automation-initial-delay-ms:15000}")
  public void run() {
    if (enabled) service.generate();
  }
}
