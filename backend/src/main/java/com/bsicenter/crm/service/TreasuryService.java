package com.bsicenter.crm.service;

import com.bsicenter.crm.dto.TreasuryRequests.*;
import com.bsicenter.crm.exception.ApiError;
import com.bsicenter.crm.repository.Db;
import com.bsicenter.crm.security.Access;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TreasuryService {
  private final Db db;
  private final Access access;

  public TreasuryService(Db db, Access access) {
    this.db = db;
    this.access = access;
  }

  @Transactional
  public Object account(Account r) {
    access.permission("finance.write");
    long id =
        db.id(
            "insert into accounts(branch_id,name) values(?,?) returning id",
            access.branch(),
            r.name());
    db.run(
        "insert into financial_entries(branch_id,account_id,amount,kind) values(?,?,?,'OPENING')",
        access.branch(),
        id,
        FinanceService.amount(r.openingBalance()));
    access.audit("OPENING", "ACCOUNT", id, "افتتاح حساب و ثبت مانده اولیه");
    return Map.of("id", id);
  }

  public Object transfers() {
    access.permission("finance.read");
    return db.list(
        "select t.*,a.name from_name,b.name to_name from account_transfers t join accounts a on"
            + " a.id=t.from_account_id join accounts b on b.id=t.to_account_id where t.branch_id=?"
            + " order by t.id desc limit 200",
        access.branch());
  }

  @Transactional
  public Object transfer(Transfer r) {
    access.permission("finance.write");
    if (r.fromAccountId() == r.toAccountId())
      throw ApiError.bad("حساب مبدا و مقصد باید متفاوت باشند");
    for (long id : new TreeSet<>(List.of(r.fromAccountId(), r.toAccountId())))
      db.one(
          "select id from accounts where id=? and branch_id=? and active for update",
          id,
          access.branch());
    var old =
        db.list("select * from account_transfers where idempotency_key=?", r.idempotencyKey());
    if (!old.isEmpty()) {
      var t = old.get(0);
      if (Db.number(t, "branch_id") != access.branch()
          || Db.number(t, "from_account_id") != r.fromAccountId()
          || Db.number(t, "to_account_id") != r.toAccountId()
          || Db.money(t, "amount").compareTo(r.amount()) != 0
          || !Objects.equals(t.get("reason"), r.reason()))
        throw ApiError.conflict("کلید تکرار متعلق به انتقال دیگری است");
      return Map.of("id", t.get("id"), "replayed", true);
    }
    var amount = FinanceService.amount(r.amount());
    if (Db.money(
                db.one(
                    "select coalesce(sum(amount),0) balance from financial_entries where"
                        + " account_id=?",
                    r.fromAccountId()),
                "balance")
            .compareTo(amount)
        < 0) throw ApiError.bad("مانده حساب مبدا کافی نیست");
    long id =
        db.id(
            "insert into"
                + " account_transfers(branch_id,from_account_id,to_account_id,amount,reason,idempotency_key,created_by)"
                + " values(?,?,?,?,?,?,?) returning id",
            access.branch(),
            r.fromAccountId(),
            r.toAccountId(),
            amount,
            r.reason(),
            r.idempotencyKey(),
            access.uid());
    db.run(
        "insert into financial_entries(branch_id,account_id,amount,kind,transfer_id)"
            + " values(?,?,?,'TRANSFER_OUT',?),(?,?,?,'TRANSFER_IN',?)",
        access.branch(),
        r.fromAccountId(),
        amount.negate(),
        id,
        access.branch(),
        r.toAccountId(),
        amount,
        id);
    access.audit("TRANSFER", "ACCOUNT_TRANSFER", id, r.reason());
    return Map.of("id", id, "replayed", false);
  }
}
