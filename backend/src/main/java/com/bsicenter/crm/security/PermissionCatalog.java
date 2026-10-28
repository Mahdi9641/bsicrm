package com.bsicenter.crm.security;

import java.util.*;

/** The backend is the only authority for permission codes and role boundaries. */
public final class PermissionCatalog {
  private PermissionCatalog() {}
  public record Definition(String code, String label, String module) {}
  public static final Set<String> BASE_ROLES=Set.of("ADMIN","MANAGER","RECEPTION","STYLIST","MARKETING","FINANCE","CUSTOMER");
  private static final Map<String,Definition> DEFINITIONS=new LinkedHashMap<>();
  private static final Map<String,Set<String>> DEFAULTS=new LinkedHashMap<>();
  static {
    add("dashboard.read","نمای کلی — مشاهده","dashboard");
    add("appointments.read","نوبت‌ها — مشاهده","appointments");
    add("appointments.write","نوبت‌ها — ثبت و تغییر","appointments");
    add("visits.read","پذیرش و عملیات — مشاهده","visits");
    add("visits.write","پذیرش و عملیات — ثبت و تغییر","visits");
    add("customers.read","پرونده مشتریان — مشاهده","customers");
    add("customers.write","پرونده مشتریان — ثبت و تغییر","customers");
    add("services.read","خدمات — مشاهده","services");
    add("services.write","خدمات — ثبت و تغییر","services");
    add("resources.read","منابع و تجهیزات — مشاهده","resources");
    add("resources.write","منابع و تجهیزات — ثبت و تغییر","resources");
    add("products.read","محصولات — مشاهده","products");
    add("products.write","محصولات — ثبت و تغییر","products");
    add("invoices.read","فاکتور و پرداخت — مشاهده","invoices");
    add("invoices.write","فاکتور و پرداخت — ثبت و تغییر","invoices");
    add("finance.read","حساب‌ها و هزینه‌ها — مشاهده","finance");
    add("finance.write","حساب‌ها و هزینه‌ها — ثبت و تغییر","finance");
    add("staff.read","پرسنل — مشاهده","staff");
    add("staff.write","پرسنل — ثبت و تغییر","staff");
    add("commissions.read","کمیسیون — مشاهده","commissions");
    add("commissions.write","کمیسیون — ثبت و تغییر","commissions");
    add("commission-rules.read","قواعد کمیسیون — مشاهده","commission-rules");
    add("commission-rules.write","قواعد کمیسیون — ثبت و تغییر","commission-rules");
    add("loyalty.read","باشگاه مشتریان — مشاهده","loyalty");
    add("loyalty.write","باشگاه مشتریان — ثبت و تغییر","loyalty");
    add("rfm.read","تحلیل مشتریان — مشاهده","rfm");
    add("rfm.write","تحلیل مشتریان — ثبت و تغییر","rfm");
    add("campaigns.read","کمپین‌ها — مشاهده","campaigns");
    add("campaigns.write","کمپین‌ها — ثبت و تغییر","campaigns");
    add("events.read","رویدادها — مشاهده","events");
    add("events.write","رویدادها — ثبت و تغییر","events");
    add("tasks.read","پیگیری‌ها — مشاهده","tasks");
    add("tasks.write","پیگیری‌ها — ثبت و تغییر","tasks");
    add("messages.read","پیام‌ها — مشاهده","messages");
    add("messages.write","پیام‌ها — ثبت و تغییر","messages");
    add("reports.read","گزارش‌ها — مشاهده","reports");
    add("settings.read","تنظیمات سامانه — مشاهده","settings");
    add("settings.write","تنظیمات سامانه — ثبت و تغییر","settings");
    add("salons.read","مدیریت سالن‌ها — مشاهده","salons");
    add("salons.write","مدیریت سالن‌ها — ثبت و تغییر","salons");
    add("branches.read","مدیریت شعب — مشاهده","branches");
    add("branches.write","مدیریت شعب — ثبت و تغییر","branches");
    add("users.read","مدیریت کاربران — مشاهده","users");
    add("users.write","مدیریت کاربران — ثبت و تغییر","users");
    add("roles.read","مدیریت نقش‌ها — مشاهده","roles");
    add("roles.write","مدیریت نقش‌ها — ثبت و تغییر","roles");
    add("subscription.read","اشتراک — مشاهده","subscription");
    add("subscription.write","اشتراک — ثبت و تغییر","subscription");
    add("audit.read","تاریخچه فعالیت‌ها — مشاهده","audit");
    add("security.read","امنیت حساب — مشاهده","security");
    add("security.write","امنیت حساب — ثبت و تغییر","security");
    add("portal.appointments.read","نوبت‌های شخصی — مشاهده","portal.appointments");
    add("portal.appointments.write","نوبت‌های شخصی — ثبت و تغییر","portal.appointments");
    add("portal.profile.read","پروفایل شخصی — مشاهده","portal.profile");
    add("portal.profile.write","پروفایل شخصی — ثبت و تغییر","portal.profile");
    add("portal.history.read","خرید و امتیاز شخصی — مشاهده","portal.history");
    add("visits.execute","اجرای خدمت — شروع و تکمیل","visits");
    DEFAULTS.put("ADMIN", Set.of("appointments.read","appointments.write","audit.read","branches.read","branches.write","campaigns.read","campaigns.write","commission-rules.read","commission-rules.write","commissions.read","commissions.write","customers.read","customers.write","dashboard.read","events.read","events.write","finance.read","finance.write","invoices.read","invoices.write","loyalty.read","loyalty.write","messages.read","messages.write","products.read","products.write","reports.read","resources.read","resources.write","rfm.read","rfm.write","roles.read","roles.write","salons.read","salons.write","security.read","security.write","services.read","services.write","settings.read","settings.write","staff.read","staff.write","subscription.read","subscription.write","tasks.read","tasks.write","users.read","users.write","visits.execute","visits.read","visits.write"));
    DEFAULTS.put("MANAGER", Set.of("appointments.read","appointments.write","audit.read","branches.read","branches.write","campaigns.read","campaigns.write","commission-rules.read","commission-rules.write","commissions.read","commissions.write","customers.read","customers.write","dashboard.read","events.read","events.write","finance.read","finance.write","invoices.read","invoices.write","loyalty.read","loyalty.write","messages.read","messages.write","products.read","products.write","reports.read","resources.read","resources.write","rfm.read","rfm.write","roles.read","roles.write","security.read","security.write","services.read","services.write","staff.read","staff.write","subscription.read","subscription.write","tasks.read","tasks.write","users.read","users.write","visits.execute","visits.read","visits.write"));
    DEFAULTS.put("RECEPTION", Set.of("appointments.read","appointments.write","customers.read","customers.write","dashboard.read","invoices.read","invoices.write","loyalty.read","products.read","security.read","security.write","services.read","tasks.read","tasks.write","visits.execute","visits.read","visits.write"));
    DEFAULTS.put("STYLIST", Set.of("appointments.read","commissions.read","dashboard.read","security.read","security.write","visits.execute","visits.read"));
    DEFAULTS.put("MARKETING", Set.of("campaigns.read","campaigns.write","customers.read","dashboard.read","events.read","events.write","loyalty.read","messages.read","messages.write","rfm.read","rfm.write","security.read","security.write","tasks.read","tasks.write"));
    DEFAULTS.put("FINANCE", Set.of("commission-rules.read","commissions.read","commissions.write","customers.read","dashboard.read","finance.read","finance.write","invoices.read","invoices.write","reports.read","security.read","security.write"));
    DEFAULTS.put("CUSTOMER", Set.of("portal.appointments.read","portal.appointments.write","portal.history.read","portal.profile.read","portal.profile.write","security.read","security.write"));
  }
  private static void add(String code,String label,String module){DEFINITIONS.put(code,new Definition(code,label,module));}
  public static List<Definition> definitions(){return List.copyOf(DEFINITIONS.values());}
  public static Set<String> defaults(String role){return DEFAULTS.getOrDefault(role,Set.of());}
  public static boolean known(String code){return DEFINITIONS.containsKey(code);}
  public static Set<String> ceiling(String role){
    if(!BASE_ROLES.contains(role))return Set.of();
    if(Set.of("ADMIN","MANAGER","CUSTOMER","STYLIST").contains(role))return defaults(role);
    var allowed=new HashSet<>(defaults("MANAGER"));
    allowed.removeIf(p->Set.of("branches","users","roles","subscription","audit").contains(DEFINITIONS.get(p).module()));
    return allowed;
  }
  public static Set<String> normalize(Collection<String> permissions){
    var result=new TreeSet<>(permissions);
    // A denied read also removes writes. Never silently re-grant denied reads.
    result.removeIf(p->p.endsWith(".write")&&!result.contains(p.substring(0,p.length()-6)+".read"));
    if(!result.contains("visits.read"))result.remove("visits.execute");
    result.addAll(Set.of("security.read","security.write"));
    return Collections.unmodifiableSet(result);
  }
  public static List<String> menus(Set<String> permissions){
    return permissions.stream().filter(p->p.endsWith(".read")).map(p->p.substring(0,p.length()-5)).sorted().toList();
  }
}
