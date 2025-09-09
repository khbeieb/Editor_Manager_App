package com.project.utils;

/**
 * Lightweight HTTP status codes
 */
public final class HttpStatus {
  private HttpStatus() {} // prevent instantiation

  // 2xx Success
  public static final int OK = 200;
  public static final int CREATED = 201;
  public static final int NO_CONTENT = 204;

  // 4xx Client errors
  public static final int BAD_REQUEST = 400;
  public static final int UNAUTHORIZED = 401;
  public static final int FORBIDDEN = 403;
  public static final int NOT_FOUND = 404;
  public static final int CONFLICT = 409;

  // 5xx Server errors
  public static final int INTERNAL_SERVER_ERROR = 500;
  public static final int BAD_GATEWAY = 502;
  public static final int SERVICE_UNAVAILABLE = 503;
}
