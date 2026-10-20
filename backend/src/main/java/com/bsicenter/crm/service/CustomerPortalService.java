package com.bsicenter.crm.service;

import com.bsicenter.crm.dto.BookingRequests.Create;
import com.bsicenter.crm.exception.ApiError;
import com.bsicenter.crm.repository.Db;
import com.bsicenter.crm.security.Access;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerPortalService {
  private final Db db; private final Access access; private final BookingService booking;
  public CustomerPortalService(Db db, Access access, BookingService booking) { this.db=db; this.access=access; this.booking=booking; }
  private void customer() { access.roles("CUSTOMER"); }
  public Object me() { customer(); return db.one("select c.id,c.first_name,c.last_name,c.mobile,c.birth_date,c.city,c.address,c.contact_channel,c.marketing_consent,b.name branch_name,s.name salon_name,s.expires_on from customers c join branches b on b.id=c.branch_id join salons s on s.id=b.salon_id where c.id=? and c.branch_id=?", access.customerId(),access.branch()); }
  public Object appointments() { customer(); return db.list("select p.*, (select string_agg(s.name,'، ') from appointment_items x join services s on s.id=x.service_id where x.appointment_id=p.id) service_names, (select string_agg(t.name,'، ') from appointment_items x join staff t on t.id=x.staff_id where x.appointment_id=p.id) staff_names from appointments p where p.customer_id=? order by p.starts_at desc limit 100", access.customerId()); }
  public Object catalog() { customer(); return Map.of("services", db.list("select id,name,category,description,price,duration_minutes,buffer_minutes from services where branch_id=? and active order by name", access.branch()), "staff", db.list("select t.id,t.name,t.specialty,t.work_start,t.work_end,t.work_days,coalesce((select json_agg(service_id)::text from staff_services where staff_id=t.id),'[]') service_ids from staff t where branch_id=? and active order by name", access.branch())); }
  @Transactional public Object book(Create body) { customer(); if (body.customerId()!=access.customerId()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,"مشتری نامعتبر است"); return booking.create(body); }
  @Transactional
  public Object cancel(long id, String reason) {
    customer();
    var p = db.one("select * from appointments where id=? and customer_id=? for update", id, access.customerId());
    if (p == null) throw ApiError.missing();
    if (!Set.of("BOOKED", "CONFIRMED").contains(p.get("status")))
      throw ApiError.bad("این نوبت قابل لغو نیست");
    if (((java.sql.Timestamp)p.get("starts_at")).toInstant().isBefore(java.time.Instant.now())) throw ApiError.bad("زمان نوبت گذشته است؛ با سالن تماس بگیرید");
    String safeReason = reason == null || reason.isBlank() ? "لغو توسط مشتری" : reason.trim();
    db.run("update appointments set status='CANCELLED',cancel_reason=?,version=version+1,updated_at=now() where id=?", safeReason, id);
    db.run("update appointment_items set active=false where appointment_id=?", id);
    access.audit("CANCELLED", "APPOINTMENT", id, safeReason);
    return Map.of("ok", true);
  }

  public Object availability(com.bsicenter.crm.dto.BookingRequests.Availability body) {
    customer();
    if(body.excludeAppointmentId()!=null) db.one("select id from appointments where id=? and customer_id=?",body.excludeAppointmentId(),access.customerId());
    return booking.availability(body);
  }
  @Transactional public Object reschedule(long id,com.bsicenter.crm.dto.BookingRequests.Reschedule body) {
    customer();db.one("select id from appointments where id=? and customer_id=? and starts_at>now() for update",id,access.customerId());
    return booking.reschedule(id,body);
  }
  public Object history() {
    customer();long id=access.customerId();
    return Map.of("invoices",db.list("select i.id,i.created_at,i.total,i.status,coalesce((select sum(p.amount) from payments p where p.invoice_id=i.id and not p.reversed),0) paid from invoices i where i.customer_id=? order by i.created_at desc limit 100",id),
      "loyalty",db.list("select points,reason,created_at from loyalty_entries where customer_id=? order by id desc limit 100",id),
      "points",db.id("select coalesce(sum(points),0) from loyalty_entries where customer_id=?",id));
  }
  @Transactional public Object profile(String first,String last,String city,String address,String channel,boolean consent) {
    customer();long id=access.customerId();
    db.run("update customers set first_name=?,last_name=?,city=?,address=?,contact_channel=?,marketing_consent=?,consent_at=case when ? and not marketing_consent then now() when not ? then null else consent_at end where id=?",first.trim(),last.trim(),city,address,channel,consent,consent,consent,id);
    db.run("update app_users set name=? where id=?",first.trim()+" "+last.trim(),access.uid());access.audit("PROFILE","CUSTOMER",id,null);return me();
  }
}
