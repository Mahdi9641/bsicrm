package com.bsicenter.crm.dto;

import com.bsicenter.crm.exception.ApiError;
import java.util.List;

public record PageResult<T>(
    List<T> content, int page, int size, long totalElements, long totalPages) {
  public static <T> PageResult<T> of(List<T> rows, int page, int size, long total) {
    return new PageResult<>(rows, page, size, total, (total + size - 1) / size);
  }

  public static void validate(int page, int size) {
    if (page < 0 || page > 10000 || size < 1 || size > 100)
      throw ApiError.bad("صفحه‌بندی معتبر نیست");
  }
}
