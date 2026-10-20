package com.bsicenter.crm.integration.sms;

import com.bsicenter.crm.repository.Db;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

/**
 * Optional adapter contract: HTTPS POST {to,body,clientReference}; 2xx JSON {id}. Provider must
 * deduplicate clientReference. Ambiguous failures need manual review.
 */
@Component
public class SmsWorker {
  final Db db;
  final TransactionTemplate tx;
  final ObjectMapper json;
  final String url, token;
  final boolean enabled;
  final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

  public SmsWorker(
      Db db,
      TransactionTemplate tx,
      ObjectMapper json,
      @Value("${app.sms-url}") String url,
      @Value("${app.sms-token}") String token,
      @Value("${app.sms-enabled}") boolean enabled) {
    this.db = db;
    this.tx = tx;
    this.json = json;
    this.url = url;
    this.token = token;
    this.enabled = enabled;
    if (enabled && (!url.startsWith("https://") || token.isBlank()))
      throw new IllegalArgumentException("SMS requires HTTPS URL and token");
  }

  @Scheduled(fixedDelay = 30000)
  public void send() {
    if (!enabled) return;
    for (int n = 0; n < 20; n++) {
      var m =
          tx.execute(
              s -> {
                var rows =
                    db.list(
                        "select m.*,c.mobile,c.marketing_consent,c.active,p.status"
                            + " appointment_status,p.version current_version,p.starts_at"
                            + " appointment_start,i.status invoice_status from messages m join"
                            + " customers c on c.id=m.customer_id left join campaigns cp on"
                            + " cp.id=m.campaign_id left join appointments p on"
                            + " p.id=m.appointment_id left join invoices i on i.id=m.invoice_id"
                            + " join branches b on b.id=m.branch_id join salons sa on sa.id=b.salon_id where b.active and sa.active and sa.plan<>'BASIC' and sa.expires_on>=(now() at time zone 'Asia/Tehran')::date and m.status='QUEUED' and m.scheduled_at<=now() and"
                            + " (m.campaign_id is null or cp.status='QUEUED') order by m.id for"
                            + " update of m skip locked limit 1");
                if (rows.isEmpty()) return null;
                var row = rows.get(0);
                boolean marketing =
                    Set.of("MARKETING", "BIRTHDAY", "FOLLOWUP").contains(row.get("kind"));
                boolean staleAppointment =
                    row.get("appointment_id") != null
                        && (!Set.of("BOOKED", "CONFIRMED").contains(row.get("appointment_status"))
                            || !Objects.equals(
                                row.get("appointment_version"), row.get("current_version"))
                            || ((java.sql.Timestamp) row.get("appointment_start"))
                                .toInstant()
                                .isBefore(java.time.Instant.now()));
                boolean staleInvoice =
                    row.get("invoice_id") != null
                        && ("VOID".equals(row.get("invoice_status"))
                            || ((java.sql.Timestamp) row.get("created_at"))
                                .toInstant()
                                .plus(Duration.ofHours(23))
                                .isBefore(java.time.Instant.now()));
                if ((marketing && !Boolean.TRUE.equals(row.get("marketing_consent")))
                    || !Boolean.TRUE.equals(row.get("active"))
                    || staleAppointment
                    || staleInvoice) {
                  db.run("update messages set status='CANCELLED' where id=?", row.get("id"));
                  row.put("skip", true);
                  return row;
                }
                db.run("update messages set status='SENDING' where id=?", row.get("id"));
                return row;
              });
      if (m == null) return;
      if (m.containsKey("skip")) continue;
      try {
        var payload =
            json.writeValueAsString(
                Map.of(
                    "to",
                    m.get("mobile"),
                    "body",
                    m.get("body"),
                    "clientReference",
                    "bsicrm-" + m.get("id")));
        var request =
            HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
        var r = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() < 200 || r.statusCode() >= 300)
          throw new IllegalStateException("Provider status " + r.statusCode());
        String providerId = json.readTree(r.body()).path("id").asText();
        if (providerId.isBlank() || providerId.length() > 120)
          throw new IllegalStateException("Invalid provider response");
        db.run(
            "update messages set status='SENT',provider_id=?,sent_at=now() where id=?",
            providerId,
            m.get("id"));
      } catch (Exception e) {
        db.run(
            "update messages set status='FAILED',error='ارسال ناموفق یا نامشخص؛ بررسی سرویس‌دهنده"
                + " لازم است' where id=?",
            m.get("id"));
      }
    }
  }
}
