package com.project.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApiResponse<T> {
  private int statusCode;
  private String message;
  private T data;
  private LocalDateTime timestamp;
}
