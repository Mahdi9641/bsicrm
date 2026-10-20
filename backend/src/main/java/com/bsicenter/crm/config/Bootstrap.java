package com.bsicenter.crm.config;

import com.bsicenter.crm.repository.Db;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class Bootstrap implements CommandLineRunner {
  final Db db;
  final PasswordEncoder passwords;
  final String admin;
  final boolean demo;

  public Bootstrap(
      Db db,
      PasswordEncoder passwords,
      @Value("${app.admin-password}") String admin,
      @Value("${app.demo-data}") boolean demo) {
    this.db = db;
    this.passwords = passwords;
    this.admin = admin;
    this.demo = demo;
  }

  @Override
  @Transactional
  public void run(String... args) {
    if (admin.length() < 12 || admin.length() > 72)
      throw new IllegalArgumentException("ADMIN_PASSWORD must be 12–72 characters");
    if (db.id("select count(*) from app_users") == 0)
      db.run(
          "insert into app_users(username,password_hash,name,role,branch_id) values('admin',?,'مدیر"
              + " سالن','ADMIN',1)",
          passwords.encode(admin));
    if (!demo || db.id("select count(*) from customers") > 0) return;
    String[] staff = {"نگار محمدی", "سارا رضایی", "مهسا احمدی"};
    for (String name : staff)
      db.run(
          "insert into staff(branch_id,name,mobile,specialty,work_days)"
              + " values(1,?,'09120000000','متخصص زیبایی','1,2,3,4,5,6,7')",
          name);
    String[] services = {
      "رنگ و بالیاژ مو", "کوتاهی و استایل", "فیشیال تخصصی", "مانیکور و ژلیش", "میکاپ ویژه"
    };
    int[] prices = {2800000, 650000, 1200000, 800000, 3500000}, durations = {120, 45, 60, 60, 90};
    for (int n = 0; n < services.length; n++)
      db.run(
          "insert into services(branch_id,name,category,price,material_cost,duration_minutes)"
              + " values(1,?,'زیبایی',?,?,?)",
          services[n],
          prices[n],
          prices[n] / 5,
          durations[n]);
    db.run("insert into staff_services select t.id,s.id from staff t cross join services s");
    db.run(
        "insert into products(branch_id,name,price,cost,stock,supplier) values(1,'ماسک موی"
            + " ترمیم‌کننده',780000,450000,12,'تأمین‌کننده نمونه'),(1,'سرم آبرسان"
            + " پوست',950000,600000,2,'تأمین‌کننده نمونه')");
    String[] first = {
      "آوا", "نازنین", "رها", "هلیا", "درسا", "یلدا", "لیلا", "مهتاب", "پرنیا", "نسترن", "مریم",
      "شیدا"
    };
    String[] last = {
      "مهرآیین", "کیانی", "راد", "احمدی", "رحیمی", "زارع", "بهرامی", "صادقی", "امینی", "محمدی",
      "شریفی", "رضایی"
    };
    long uid = db.id("select id from app_users where username='admin'");
    for (int n = 0; n < first.length; n++) {
      long c =
          db.id(
              "insert into"
                  + " customers(branch_id,first_name,last_name,mobile,marketing_consent,consent_at,acquisition_channel,city,created_at)"
                  + " values(1,?,?,?,true,now(),'INSTAGRAM','تهران',now()-interval '180 days')"
                  + " returning id",
              first[n],
              last[n],
              "09120000" + String.format("%03d", n + 1));
      int visits = n < 3 ? 6 : n < 6 ? 3 : 1;
      for (int k = 0; k < visits; k++) {
        int days = n >= 9 ? 110 + k * 10 : k * 24 + n % 7;
        long v =
            db.id(
                "insert into visits(branch_id,customer_id,status,check_in_at,completed_at)"
                    + " values(1,?,'COMPLETED',now()-(? * interval '1 day'),now()-(? * interval '1"
                    + " day')) returning id",
                c,
                days,
                days);
        long service = n % 5 + 1, member = n % 3 + 1;
        db.run(
            "insert into"
                + " visit_services(visit_id,service_id,staff_id,status,started_at,completed_at,rating)"
                + " values(?,?,?,'COMPLETED',now()-(? * interval '1 day'),now()-(? * interval '1"
                + " day'),5)",
            v,
            service,
            member,
            days,
            days);
        int price = prices[n % 5];
        long inv =
            db.id(
                "insert into"
                    + " invoices(branch_id,customer_id,visit_id,total,status,created_by,created_at)"
                    + " values(1,?,?,?,'PAID',?,now()-(? * interval '1 day')) returning id",
                c,
                v,
                price,
                uid,
                days);
        long item =
            db.id(
                "insert into"
                    + " invoice_items(invoice_id,service_id,staff_id,description,quantity,unit_price,unit_cost,discount,total,commission_rate)"
                    + " values(?,?,?,?,1,?,?,0,?,30) returning id",
                inv,
                service,
                member,
                services[n % 5],
                price,
                price / 5,
                price);
        db.run(
            "insert into commissions(invoice_item_id,staff_id,basis,rate,amount)"
                + " values(?,?,?,30,?)",
            item,
            member,
            price,
            price * 3 / 10);
        long pay =
            db.id(
                "insert into"
                    + " payments(invoice_id,amount,method,account_id,idempotency_key,created_by,created_at)"
                    + " values(?,?,'CARD',2,?,?,now()-(? * interval '1 day')) returning id",
                inv,
                price,
                UUID.randomUUID(),
                uid,
                days);
        db.run(
            "insert into financial_entries(branch_id,account_id,amount,kind,payment_id,created_at)"
                + " values(1,2,?,'RECEIPT',?,now()-(? * interval '1 day'))",
            price,
            pay,
            days);
        db.run(
            "insert into loyalty_entries(customer_id,invoice_id,points,reason) values(?,?,?,'داده"
                + " نمونه: تسویه فاکتور')",
            c,
            inv,
            price / 100000);
      }
    }
    db.run(
        "insert into campaigns(branch_id,name,objective,segment,body,budget) values(1,'دوباره"
            + " بدرخش','بازگشت مشتریان','AT_RISK','مشتری گرامی، مشتاق دیدار دوباره شما"
            + " هستیم.',1500000)");
    db.run(
        "insert into events(branch_id,name,description,starts_at,ends_at,venue,capacity,budget)"
            + " values(1,'عصر زیبایی و مراقبت','آشنایی با روتین مراقبت پوست',now()+interval '7"
            + " days',now()+interval '7 days 3 hours','شعبه مرکزی',30,5000000)");
    db.run(
        "insert into"
            + " follow_up_tasks(branch_id,customer_id,assigned_to,created_by,title,description,due_at,priority,status)"
            + " values(1,1,?,?,'تماس پس از فیشیال','نظر مشتری درباره تجربه مراجعه و مراقبت پس از"
            + " خدمت ثبت شود.',now()+interval '2 hours','HIGH','OPEN'),(1,2,?,?,'هماهنگی مراجعه"
            + " بعدی','زمان مناسب مراجعه بعدی با مشتری هماهنگ شود.',now()-interval '1"
            + " day','NORMAL','IN_PROGRESS'),(1,null,?,?,'بررسی موجودی محصولات','موجودی محصولات"
            + " کم‌تعداد بررسی و نیاز خرید مشخص شود.',now()+interval '1 day','NORMAL','OPEN')",
        uid,
        uid,
        uid,
        uid,
        uid,
        uid);
  }
}
