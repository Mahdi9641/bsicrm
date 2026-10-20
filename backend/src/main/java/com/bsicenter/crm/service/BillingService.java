package com.bsicenter.crm.service;

import com.bsicenter.crm.repository.Db;
import com.bsicenter.crm.security.Access;
import com.bsicenter.crm.exception.ApiError;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillingService {
  final Db db;final Access a;final PaymentGateway gateway;
  public BillingService(Db db,Access a,PaymentGateway gateway){this.db=db;this.a=a;this.gateway=gateway;}
  public Object status(){
    a.roles("ADMIN","MANAGER");
    return Map.of("salon",a.settings(),"plans",db.list("select * from subscription_plans where active order by price"),"orders",db.list("select * from subscription_orders where salon_id=? order by created_at desc limit 100",a.salon()),"onlineEnabled",gateway.enabled());
  }
  @Transactional public Object order(String plan,String method,UUID key){
    a.roles("ADMIN","MANAGER");
    long salon=a.salon();db.one("select id from salons where id=? for update",salon);
    var existing=db.list("select * from subscription_orders where salon_id=? and idempotency_key=?",salon,key);
    if(!existing.isEmpty()) {
      var old=existing.get(0);
      if(!old.get("plan_code").equals(plan) || !old.get("payment_method").equals(method)) throw ApiError.conflict("شناسهٔ درخواست قبلاً برای سفارش دیگری استفاده شده است");
      return old;
    }
    if(method.equals("ZARINPAL") && !gateway.enabled()) throw ApiError.bad("درگاه آنلاین هنوز تنظیم نشده است");
    var p=db.one("select * from subscription_plans where code=? and active",plan);
    UUID id=UUID.randomUUID();
    db.run("insert into subscription_orders(id,salon_id,plan_code,amount,duration_days,payment_method,created_by,idempotency_key) values(?,?,?,?,?,?,?,?)",id,salon,plan,p.get("price"),p.get("duration_days"),method,a.uid(),key);
    a.audit("ORDER","SUBSCRIPTION",salon,id.toString());
    return db.one("select * from subscription_orders where id=?",id);
  }
  @Transactional public Object pay(UUID id){
    a.roles("ADMIN","MANAGER");
    var order=db.one("select * from subscription_orders where id=? and salon_id=? for update",id,a.salon());
    if(!order.get("status").equals("PENDING") || !order.get("payment_method").equals("ZARINPAL")) throw ApiError.bad("سفارش قابل پرداخت آنلاین نیست");
    if(order.get("authority")!=null) return Map.of("url",gateway.paymentUrl(order.get("authority").toString()));
    var result=gateway.request(id,Db.money(order,"amount"));
    db.run("update subscription_orders set authority=? where id=?",result.get("authority"),id);
    return result;
  }
  @Transactional public Object reference(UUID id,String reference){
    a.roles("ADMIN","MANAGER");
    var row=db.one("select * from subscription_orders where id=? and salon_id=? and status='PENDING' and payment_method='MANUAL' for update",id,a.salon());
    db.run("update subscription_orders set reference=? where id=?",reference.trim(),id);a.audit("PAYMENT_REFERENCE","SUBSCRIPTION",a.salon(),id.toString());return Map.of("ok",true);
  }
  public static LocalDate renewalStart(LocalDate currentEnd,LocalDate today){return currentEnd!=null && !currentEnd.isBefore(today)?currentEnd.plusDays(1):today;}
  private Object activate(Map<String,Object> order,String ref,Long reviewer){
    if(order.get("status").equals("PAID"))return order;
    if(!order.get("status").equals("PENDING"))throw ApiError.conflict("سفارش قبلاً بسته شده است");
    var salon=db.one("select * from salons where id=? for update",order.get("salon_id"));
    LocalDate today=LocalDate.now(ZoneId.of("Asia/Tehran"));
    LocalDate end=((java.sql.Date)salon.get("expires_on")).toLocalDate();
    // A different plan starts immediately; same-plan renewal preserves remaining days.
    LocalDate start=salon.get("plan").equals(order.get("plan_code"))?renewalStart(end,today):today;
    LocalDate expires=start.plusDays(Db.number(order,"duration_days")-1);
    db.run("update salons set plan=?,expires_on=? where id=?",order.get("plan_code"),expires,order.get("salon_id"));
    db.run("update subscription_orders set status='PAID',verified_reference=?,reviewed_by=?,paid_at=now(),starts_on=?,expires_on=? where id=?",ref,reviewer,start,expires,order.get("id"));
    db.run("insert into audit_logs(branch_id,user_id,action,entity,entity_id,detail) select min(id),?,'ACTIVATE','SUBSCRIPTION',?,? from branches where salon_id=?",reviewer,order.get("salon_id"),order.get("id").toString(),order.get("salon_id"));
    return db.one("select * from subscription_orders where id=?",order.get("id"));
  }
  @Transactional public Object review(UUID id,boolean approve,String ref){
    a.roles("ADMIN");
    var order=db.one("select * from subscription_orders where id=? for update",id);
    if(!order.get("payment_method").equals("MANUAL"))throw ApiError.bad("پرداخت آنلاین فقط با تأیید درگاه فعال می‌شود");
    if(approve){
      if(ref==null || ref.isBlank())throw ApiError.bad("شناسهٔ پرداخت تأییدشده الزامی است");
      return activate(order,"MANUAL:"+ref.trim(),a.uid());
    }
    if(order.get("status").equals("PAID"))throw ApiError.conflict("سفارش پرداخت‌شده قابل رد نیست");
    db.run("update subscription_orders set status='REJECTED',reviewed_by=? where id=?",a.uid(),id);a.audit("REJECT","SUBSCRIPTION",Db.number(order,"salon_id"),id.toString());return Map.of("ok",true);
  }
  @Transactional public Object verify(String authority){
    var order=db.one("select * from subscription_orders where authority=? and payment_method='ZARINPAL' for update",authority);
    if(order.get("status").equals("PAID"))return order;
    String ref=gateway.verify(authority,Db.money(order,"amount"));
    return activate(order,ref,null);
  }
}
