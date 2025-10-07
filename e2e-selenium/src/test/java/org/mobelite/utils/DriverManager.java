package org.mobelite.utils;

import com.codeborne.selenide.Configuration;

public class DriverManager {
  public static void setup() {
    Configuration.browser = "chrome";
    Configuration.timeout = 10000;
  }
}
