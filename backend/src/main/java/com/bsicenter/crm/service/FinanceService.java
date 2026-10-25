package com.bsicenter.crm.service;

import com.bsicenter.crm.dto.FinanceRequests.*;
import com.bsicenter.crm.exception.ApiError;
import com.bsicenter.crm.repository.Db;
import com.bsicenter.crm.security.Access;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service
public class FinanceService {
  private final Db db;
  private final Access a;
  private final CommissionRuleService rules;

  public FinanceService(Db db, Access a, CommissionRuleService rules) {
    this.db = db;
    this.a = a;
    this.rules = rules;
  }

  public static BigDecimal amount(BigDecimal n) {
    if (n.scale() > 0 && n.stripTrailingZeros().scale() > 0)
      throw ApiError.bad("مبالغ باید به تومان و بدون اعشار باشند");
    return n.setScale(0);
  }

  public static BigDecimal commission(BigDecimal total, BigDecimal rate) {
    return total.multiply(rate).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
  }

  @Transactional
  public Object invoice(@Valid Invoice b) {
    a.sales();
    db.one(
        "select id from customers where id=? and branch_id=? and active",
        b.customerId(),
        a.branch());
    if (b.visitId() != null)
      db.one(
          "select id from visits where id=? and branch_id=? and customer_id=? and"
              + " status='COMPLETED'",
          b.visitId(),
          a.branch(),
          b.customerId());
    b.items().stream()
        .map(Line::productId)
        .filter(Objects::nonNull)
        .distinct()
        .sorted()
        .forEach(
            id ->
                db.one(
                    "select id from products where id=? and branch_id=? for update",
                    id,
                    a.branch()));
    if (b.visitId() != null) {
      var quantities = new HashMap<String, Integer>();
      for (var line : b.items()) {
        if (line.serviceId() != null) {
          String key = line.serviceId() + ":" + line.staffId();
          int quantity = quantities.merge(key, line.quantity(), Integer::sum);
          if (db.id(
                  "select count(*) from visit_services where visit_id=? and service_id=? and"
                      + " staff_id=? and status='COMPLETED'",
                  b.visitId(),
                  line.serviceId(),
                  line.staffId())
              < quantity)
            throw ApiError.bad("تعداد ردیف‌های خدمت از خدمات تکمیل‌شده مراجعه بیشتر است");
        }
      }
    }
    long id =
        db.id(
            "insert into invoices(branch_id,customer_id,visit_id,total,tax,created_by)"
                + " values(?,?,?,0,?,?) returning id",
            a.branch(),
            b.customerId(),
            b.visitId(),
            amount(b.tax()),
            a.uid());
    BigDecimal total = amount(b.tax()), discount = BigDecimal.ZERO;
    for (var l : b.items()) {
      if ((l.serviceId() == null) == (l.productId() == null))
        throw ApiError.bad("هر ردیف باید فقط یک خدمت یا محصول داشته باشد");
      String table = l.serviceId() != null ? "services" : "products";
      long itemId = l.serviceId() != null ? l.serviceId() : l.productId();
      var item =
          db.one(
              "select * from " + table + " where id=? and branch_id=? and active",
              itemId,
              a.branch());
      BigDecimal price = Db.money(item, "price"),
          cost = Db.money(item, l.serviceId() != null ? "material_cost" : "cost"),
          rate = BigDecimal.ZERO;
      if (l.serviceId() != null) {
        if (l.staffId() == null) throw ApiError.bad("پرسنل ارائه‌دهنده خدمت الزامی است");
        var s =
            db.one(
                "select s.* from staff s join staff_services ss on ss.staff_id=s.id where s.id=?"
                    + " and s.branch_id=? and ss.service_id=? and s.active",
                l.staffId(),
                a.branch(),
                l.serviceId());
        rate = Db.money(s, "commission_rate");
        if (b.visitId() != null
            && db.id(
                    "select count(*) from visit_services where visit_id=? and service_id=? and"
                        + " staff_id=? and status='COMPLETED'",
                    b.visitId(),
                    l.serviceId(),
                    l.staffId())
                < l.quantity())
          throw ApiError.bad("خدمت و پرسنل با خدمات تکمیل‌شده مراجعه تطابق ندارد");
      } else {
        if (l.staffId() != null) throw ApiError.bad("ردیف محصول نباید پرسنل داشته باشد");
        if (Db.number(item, "stock") < l.quantity()) throw ApiError.bad("موجودی محصول کافی نیست");
        db.run("update products set stock=stock-? where id=?", l.quantity(), itemId);
        db.run(
            "insert into stock_movements(product_id,quantity,reason) values(?,?,?)",
            itemId,
            -l.quantity(),
            "فروش فاکتور " + id);
      }
      BigDecimal net =
          price.multiply(BigDecimal.valueOf(l.quantity())).subtract(amount(l.discount()));
      if (net.signum() < 0) throw ApiError.bad("تخفیف بیشتر از مبلغ ردیف است");
      var quote =
          l.serviceId() == null
              ? null
              : rules.quote(l.staffId(), l.serviceId(), net, l.quantity(), rate);
      if (quote != null) rate = quote.rate();
      long lineId =
          db.id(
              "insert into"
                  + " invoice_items(invoice_id,service_id,product_id,staff_id,description,quantity,unit_price,unit_cost,discount,total,commission_rate)"
                  + " values(?,?,?,?,?,?,?,?,?,?,?) returning id",
              id,
              l.serviceId(),
              l.productId(),
              l.staffId(),
              item.get("name"),
              l.quantity(),
              price,
              cost,
              l.discount(),
              net,
              rate);
      if (l.serviceId() != null)
        db.run(
            "insert into"
                + " commissions(invoice_item_id,staff_id,basis,rate,amount,rule_id,rule_snapshot)"
                + " values(?,?,?,?,?,?,?)",
            lineId,
            l.staffId(),
            net,
            rate,
            quote.amount(),
            quote.ruleId(),
            quote.snapshot());
      total = total.add(net);
      discount = discount.add(l.discount());
    }
    db.run("update invoices set total=?,discount=? where id=?", total, discount, id);
    if (total.signum() == 0) db.run("update invoices set status='PAID' where id=?", id);
    a.audit("ISSUE", "INVOICE", id, null);
    return detail(id);
  }

  public Object invoices() {
    a.sales();
    return db.list(
        "select i.*,c.first_name||' '||c.last_name customer_name,coalesce((select sum(p.amount)"
            + " from payments p where p.invoice_id=i.id and not reversed),0) paid from invoices i"
            + " join customers c on c.id=i.customer_id where i.branch_id=? order by i.id desc limit"
            + " 200",
        a.branch());
  }

  public Object detail(long id) {
    a.sales();
    var row =
        db.one(
            "select i.*,c.first_name||' '||c.last_name customer_name,c.mobile from invoices i join"
                + " customers c on c.id=i.customer_id where i.id=? and i.branch_id=?",
            id,
            a.branch());
    return Map.of(
        "invoice",
        row,
        "items",
        db.list(
            "select x.*,s.name staff_name from invoice_items x left join staff s on s.id=x.staff_id"
                + " where invoice_id=? order by x.id",
            id),
        "payments",
        db.list(
            "select p.*,a.name account_name from payments p join accounts a on a.id=p.account_id"
                + " where invoice_id=? order by p.id",
            id));
  }

  @Transactional
  public Object pay(long id, @Valid Payment p) {
    a.sales();
    var i = db.one("select * from invoices where id=? and branch_id=? for update", id, a.branch());
    var existing = db.list("select * from payments where idempotency_key=?", p.idempotencyKey());
    if (!existing.isEmpty()) {
      var e = existing.get(0);
      if (Db.number(e, "invoice_id") != id
          || Db.money(e, "amount").compareTo(p.amount()) != 0
          || !e.get("method").equals(p.method())
          || Db.number(e, "account_id") != p.accountId())
        throw ApiError.conflict("کلید پرداخت قبلاً برای درخواست دیگری استفاده شده است");
      return e;
    }
    if (i.get("status").equals("VOID")) throw ApiError.bad("فاکتور باطل شده است");
    db.one(
        "select id from accounts where id=? and branch_id=? and active", p.accountId(), a.branch());
    BigDecimal paid =
        db.jdbc.queryForObject(
            "select coalesce(sum(amount),0) from payments where invoice_id=? and not reversed",
            BigDecimal.class,
            id);
    BigDecimal remaining = Db.money(i, "total").subtract(paid);
    if (amount(p.amount()).compareTo(remaining) > 0)
      throw ApiError.bad("مبلغ پرداخت از مانده فاکتور بیشتر است");
    long paymentId =
        db.id(
            "insert into"
                + " payments(invoice_id,amount,method,account_id,tracking_code,idempotency_key,created_by)"
                + " values(?,?,?,?,?,?,?) returning id",
            id,
            p.amount(),
            p.method(),
            p.accountId(),
            p.trackingCode(),
            p.idempotencyKey(),
            a.uid());
    db.run(
        "insert into financial_entries(branch_id,account_id,amount,kind,payment_id)"
            + " values(?,?,?,'RECEIPT',?)",
        a.branch(),
        p.accountId(),
        p.amount(),
        paymentId);
    boolean full = paid.add(p.amount()).compareTo(Db.money(i, "total")) == 0;
    db.run("update invoices set status=? where id=?", full ? "PAID" : "PARTIAL", id);
    if (full) {
      BigDecimal unit = Db.money(a.settings(), "point_unit");
      int points = Db.money(i, "total").divide(unit, 0, RoundingMode.DOWN).intValueExact();
      db.run(
          "insert into loyalty_entries(customer_id,invoice_id,points,reason) values(?,?,?,'تسویه"
              + " فاکتور') on conflict(invoice_id) where invoice_id is not null do nothing",
          i.get("customer_id"),
          id,
          points);
    }
    a.audit("PAYMENT", "INVOICE", id, "شناسه پرداخت=" + paymentId);
    return db.one("select * from payments where id=?", paymentId);
  }

  @Transactional
  public Object voidInvoice(long id, @Valid Reason r) {
    a.finance();
    var i = db.one("select * from invoices where id=? and branch_id=? for update", id, a.branch());
    if (i.get("status").equals("VOID")) throw ApiError.bad("فاکتور قبلاً باطل شده است");
    if (db.id(
            "select count(*) from commissions c join invoice_items x on x.id=c.invoice_item_id"
                + " where x.invoice_id=? and c.status='SETTLED'",
            id)
        > 0) throw ApiError.bad("فاکتور دارای کمیسیون تسویه‌شده است؛ ابتدا تطبیق مالی انجام دهید");
    for (var p :
        db.list("select * from payments where invoice_id=? and not reversed for update", id)) {
      db.run(
          "update payments set reversed=true,reversal_reason=? where id=?",
          r.reason(),
          p.get("id"));
      db.run(
          "insert into financial_entries(branch_id,account_id,amount,kind,payment_id)"
              + " values(?,?,?,'REFUND',?)",
          a.branch(),
          p.get("account_id"),
          Db.money(p, "amount").negate(),
          p.get("id"));
    }
    for (var l :
        db.list(
            "select * from invoice_items where invoice_id=? and product_id is not null order by"
                + " product_id",
            id)) {
      db.run(
          "update products set stock=stock+? where id=?", l.get("quantity"), l.get("product_id"));
      db.run(
          "insert into stock_movements(product_id,quantity,reason) values(?,?,?)",
          l.get("product_id"),
          l.get("quantity"),
          "ابطال فاکتور " + id);
    }
    db.run("update invoices set status='VOID',void_reason=? where id=?", r.reason(), id);
    db.run(
        "update commissions set status='VOID' where invoice_item_id in(select id from invoice_items"
            + " where invoice_id=?)",
        id);
    var loyalty = db.list("select points from loyalty_entries where invoice_id=?", id);
    if (!loyalty.isEmpty())
      db.run(
          "insert into loyalty_entries(customer_id,points,reason) values(?,?,?)",
          i.get("customer_id"),
          -Db.number(loyalty.get(0), "points"),
          "برگشت امتیاز فاکتور " + id);
    a.audit("VOID", "INVOICE", id, r.reason());
    return Map.of("ok", true);
  }

  public Object finance() {
    a.finance();
    return Map.of(
        "accounts",
        db.list(
            "select x.*,coalesce((select sum(amount) from financial_entries f where"
                + " f.account_id=x.id),0) balance from accounts x where branch_id=?",
            a.branch()),
        "entries",
        db.list(
            "select f.*,x.name account_name from financial_entries f join accounts x on"
                + " x.id=f.account_id where f.branch_id=? order by f.id desc limit 200",
            a.branch()),
        "expenses",
        db.list("select * from expenses where branch_id=? order by id desc limit 100", a.branch()));
  }

  @Transactional
  public Object expense(@Valid Expense e) {
    a.finance();
    db.one(
        "select id from accounts where id=? and branch_id=? and active", e.accountId(), a.branch());
    long id =
        db.id(
            "insert into expenses(branch_id,account_id,title,category,amount,created_by)"
                + " values(?,?,?,?,?,?) returning id",
            a.branch(),
            e.accountId(),
            e.title(),
            e.category(),
            amount(e.amount()),
            a.uid());
    db.run(
        "insert into financial_entries(branch_id,account_id,amount,kind,expense_id)"
            + " values(?,?,?,'EXPENSE',?)",
        a.branch(),
        e.accountId(),
        e.amount().negate(),
        id);
    a.audit("EXPENSE", "EXPENSE", id, null);
    return Map.of("id", id);
  }

  public Object commissions() {
    a.roles("ADMIN", "MANAGER", "FINANCE", "STYLIST");
    a.plan("STANDARD");
    return db.list(
        "select c.*,s.name staff_name,x.description,i.id invoice_id,i.status invoice_status from"
            + " commissions c join staff s on s.id=c.staff_id join invoice_items x on"
            + " x.id=c.invoice_item_id join invoices i on i.id=x.invoice_id where s.branch_id=?"
            + (a.stylist() ? " and s.id=" + a.staffId() : "")
            + " order by c.id desc limit 200",
        a.branch());
  }

  @Transactional
  public Object settle(long id, @Valid Settlement s) {
    a.finance();
    a.plan("STANDARD");
    var ref =
        db.one(
            "select i.id from commissions c join invoice_items x on x.id=c.invoice_item_id join"
                + " invoices i on i.id=x.invoice_id where c.id=? and i.branch_id=?",
            id,
            a.branch());
    db.one("select id from invoices where id=? for update", ref.get("id"));
    var c =
        db.one(
            "select c.*,i.status invoice_status from commissions c join invoice_items x on"
                + " x.id=c.invoice_item_id join invoices i on i.id=x.invoice_id where c.id=? for"
                + " update of c",
            id);
    String old = c.get("status").toString();
    if (!(old.equals("CALCULATED") && s.status().equals("APPROVED")
        || old.equals("APPROVED") && s.status().equals("SETTLED")))
      throw ApiError.bad("ترتیب تأیید و تسویه معتبر نیست");
    if (s.status().equals("SETTLED")) {
      if (!c.get("invoice_status").equals("PAID"))
        throw ApiError.bad("فاکتور ابتدا باید تسویه شود");
      if (s.accountId() == null) throw ApiError.bad("حساب پرداخت الزامی است");
      db.one(
          "select id from accounts where id=? and branch_id=? and active",
          s.accountId(),
          a.branch());
      db.run(
          "insert into financial_entries(branch_id,account_id,amount,kind)"
              + " values(?,?,?,'COMMISSION')",
          a.branch(),
          s.accountId(),
          Db.money(c, "amount").negate());
    }
    db.run(
        "update commissions set status=?,settled_at=case when ?='SETTLED' then now() else null end"
            + " where id=?",
        s.status(),
        s.status(),
        id);
    a.audit(s.status(), "COMMISSION", id, null);
    return Map.of("ok", true);
  }

  public Object invoiceLink(long id) {
    a.sales();
    db.one("select id from invoices where id=? and branch_id=?", id, a.branch());
    String token = AuthService.random();
    db.run(
        "insert into invoice_links(invoice_id,token_hash,expires_at) values(?,?,now()+interval '24"
            + " hours')",
        id,
        AuthService.hash(token));
    a.audit("SHARE_LINK", "INVOICE", id, null);
    return Map.of("path", "/invoice/" + token, "expiresIn", 86400);
  }

  public Object publicInvoice(String token) {
    if (token.length() > 100) throw ApiError.missing();
    var i =
        db.one(
            "select i.id,i.branch_id,i.total,i.discount,i.tax,i.status,i.created_at from invoice_links l join"
                + " invoices i on i.id=l.invoice_id where l.token_hash=? and l.expires_at>now()",
            AuthService.hash(token));
    return Map.of(
        "invoice",
        i,
        "items",
        db.list(
            "select description,quantity,unit_price,discount,total from invoice_items where"
                + " invoice_id=?",
            i.get("id")),
        "organization",
        db.one("select s.name organization from salons s join branches b on b.salon_id=s.id where b.id=?",i.get("branch_id")).get("organization"));
  }
}
