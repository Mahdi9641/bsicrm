package com.bsicenter.crm.repository;

import com.bsicenter.crm.exception.ApiError;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class Db {
  public final JdbcTemplate jdbc;

  public Db(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> list(String sql, Object... args) {
    return jdbc.queryForList(sql, args);
  }

  public Map<String, Object> one(String sql, Object... args) {
    var rows = list(sql, args);
    if (rows.isEmpty()) throw ApiError.missing();
    return rows.get(0);
  }

  public long id(String sql, Object... args) {
    return jdbc.queryForObject(sql, Long.class, args);
  }

  public int run(String sql, Object... args) {
    return jdbc.update(sql, args);
  }

  public static long number(Map<String, Object> r, String key) {
    return ((Number) r.get(key)).longValue();
  }

  public static BigDecimal money(Map<String, Object> r, String key) {
    return new BigDecimal(r.get(key).toString());
  }
}
