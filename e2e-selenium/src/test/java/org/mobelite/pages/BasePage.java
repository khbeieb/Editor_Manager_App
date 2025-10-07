package org.mobelite.pages;

import static com.codeborne.selenide.Selenide.$;

public abstract class BasePage {
  protected void openUrl(String url) {
    com.codeborne.selenide.Selenide.open(url);
  }
}
