package com.bsicenter.crm.service;

import com.bsicenter.crm.dto.PageResult;
import com.bsicenter.crm.dto.TaskRequests.*;
import com.bsicenter.crm.exception.ApiError;
import com.bsicenter.crm.repository.Db;
import com.bsicenter.crm.security.Access;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

  private final Db db;
  private final Access access;

  public TaskService(Db db, Access access) {
    this.db = db;
    this.access = access;
  }

  private void authorize() {
    access.permission("tasks.read");
  }

  private boolean manager() {
    return Set.of("ADMIN", "MANAGER").contains(access.role());
  }

  private String scope() {
    return "t.branch_id=?" + (manager() ? "" : " and t.assigned_to=" + access.uid());
  }

  private static final String SELECT =
      "select t.*,c.first_name||' '||c.last_name customer_name,u.name assignee_name from"
          + " follow_up_tasks t left join customers c on c.id=t.customer_id join app_users u on"
          + " u.id=t.assigned_to ";

  private Map<String, Object> scoped(long id, boolean lock) {
    return db.one(
        SELECT + "where t.id=? and " + scope() + (lock ? " for update of t" : ""),
        id,
        access.branch());
  }

  public static void verifyVersion(long actual, Long requested) {
    if (requested == null || requested != actual)
      throw ApiError.conflict("اطلاعات توسط کاربر دیگری تغییر کرده است؛ نسخه تازه را دریافت کنید");
  }

  public static boolean allowedTransition(String old, String next) {
    return switch (old) {
      case "OPEN" -> Set.of("IN_PROGRESS", "DONE", "CANCELLED").contains(next);
      case "IN_PROGRESS" -> Set.of("OPEN", "DONE", "CANCELLED").contains(next);
      case "DONE", "CANCELLED" -> next.equals("OPEN");
      default -> false;
    };
  }

  private void validate(Write request) {
    if (!manager() && request.assignedTo() != access.uid())
      throw ApiError.bad("واگذاری به دیگران فقط برای مدیر مجاز است");
    db.one(
        "select id from app_users where id=? and branch_id=? and active and role in"
            + " ('ADMIN','MANAGER','RECEPTION','MARKETING')",
        request.assignedTo(),
        access.branch());
    if (request.customerId() != null)
      db.one(
          "select id from customers where id=? and branch_id=?",
          request.customerId(),
          access.branch());
  }

  private void event(long id, String kind, String body) {
    db.run(
        "insert into task_events(task_id,user_id,kind,body) values(?,?,?,?)",
        id,
        access.uid(),
        kind,
        body);
  }

  public Object list(String status, String q, boolean mine, boolean overdue, int page, int size) {
    authorize();
    PageResult.validate(page, size);
    if (q.length() > 100) throw ApiError.bad("جستجو حداکثر ۱۰۰ کاراکتر است");
    var params = new ArrayList<Object>();
    params.add(access.branch());
    String where = scope();
    if (mine && manager()) {
      where += " and t.assigned_to=?";
      params.add(access.uid());
    }
    if (status.equals("ACTIVE")) where += " and t.status in ('OPEN','IN_PROGRESS')";
    else if (!status.equals("ALL")) {
      if (!Set.of("OPEN", "IN_PROGRESS", "DONE", "CANCELLED").contains(status))
        throw ApiError.bad("وضعیت نامعتبر است");
      where += " and t.status=?";
      params.add(status);
    }
    if (overdue) where += " and t.due_at<now() and t.status in ('OPEN','IN_PROGRESS')";
    where += " and (t.title ilike ? or coalesce(c.first_name||' '||c.last_name,'') ilike ?)";
    params.add("%" + q + "%");
    params.add("%" + q + "%");
    long total =
        db.id(
            "select count(*) from follow_up_tasks t left join customers c on c.id=t.customer_id"
                + " where "
                + where,
            params.toArray());
    params.add(size);
    params.add(page * size);
    var rows =
        db.list(
            SELECT
                + "where "
                + where
                + " order by case when t.status in ('OPEN','IN_PROGRESS') then 0 else 1"
                + " end,t.due_at,t.id limit ? offset ?",
            params.toArray());
    return PageResult.of(rows, page, size, total);
  }

  public Object summary() {
    authorize();
    return db.one(
        "select count(*) filter(where status='OPEN') open,count(*) filter(where"
            + " status='IN_PROGRESS') in_progress,count(*) filter(where status='DONE')"
            + " done,count(*) filter(where status in ('OPEN','IN_PROGRESS') and due_at<now())"
            + " overdue,count(*) filter(where status in ('OPEN','IN_PROGRESS') and (due_at at time"
            + " zone 'Asia/Tehran')::date=(now() at time zone 'Asia/Tehran')::date) today from"
            + " follow_up_tasks t where "
            + scope(),
        access.branch());
  }

  public Object assignees() {
    authorize();
    return db.list(
        "select id,name,role from app_users where branch_id=? and active and role in"
            + " ('ADMIN','MANAGER','RECEPTION','MARKETING')"
            + (manager() ? "" : " and id=" + access.uid())
            + " order by name",
        access.branch());
  }

  public Object detail(long id) {
    authorize();
    return Map.of(
        "task",
        scoped(id, false),
        "events",
        db.list(
            "select e.id,e.kind,e.body,e.created_at,u.name author from task_events e join app_users"
                + " u on u.id=e.user_id where task_id=? order by e.id",
            id));
  }

  @Transactional
  public Object create(Write request) {
    access.permission("tasks.write");
    authorize();
    validate(request);
    long id =
        db.id(
            "insert into"
                + " follow_up_tasks(branch_id,customer_id,assigned_to,created_by,title,description,due_at,priority)"
                + " values(?,?,?,?,?,?,?,?) returning id",
            access.branch(),
            request.customerId(),
            request.assignedTo(),
            access.uid(),
            request.title().trim(),
            Objects.toString(request.description(), ""),
            request.dueAt(),
            request.priority());
    event(id, "CREATED", "پیگیری ایجاد شد");
    access.audit("CREATE", "TASK", id, null);
    return scoped(id, false);
  }

  @Transactional
  public Object update(long id, Write request) {
    access.permission("tasks.write");
    authorize();
    var old = scoped(id, true);
    verifyVersion(Db.number(old, "version"), request.version());
    validate(request);
    if (!Set.of("OPEN", "IN_PROGRESS").contains(old.get("status")))
      throw ApiError.bad("ابتدا پیگیری بسته‌شده را بازگشایی کنید");
    db.run(
        "update follow_up_tasks set"
            + " title=?,description=?,customer_id=?,assigned_to=?,due_at=?,priority=?,version=version+1,updated_at=now()"
            + " where id=?",
        request.title().trim(),
        Objects.toString(request.description(), ""),
        request.customerId(),
        request.assignedTo(),
        request.dueAt(),
        request.priority(),
        id);
    event(id, "UPDATED", "عنوان، مسئول یا زمان پیگیری به‌روزرسانی شد");
    access.audit("UPDATE", "TASK", id, null);
    return scoped(id, false);
  }

  @Transactional
  public Object transition(long id, Transition request) {
    access.permission("tasks.write");
    authorize();
    var row = scoped(id, true);
    verifyVersion(Db.number(row, "version"), request.version());
    if (!allowedTransition(row.get("status").toString(), request.status()))
      throw ApiError.bad("تغییر وضعیت پیگیری معتبر نیست");
    db.run(
        "update follow_up_tasks set status=?,completed_at=case when ?='DONE' then now() else null"
            + " end,version=version+1,updated_at=now() where id=?",
        request.status(),
        request.status(),
        id);
    event(id, "STATUS", request.status());
    access.audit(request.status(), "TASK", id, null);
    return scoped(id, false);
  }

  @Transactional
  public Object comment(long id, Comment request) {
    access.permission("tasks.write");
    authorize();
    scoped(id, true);
    event(id, "COMMENT", request.body().trim());
    return Map.of("ok", true);
  }
}
