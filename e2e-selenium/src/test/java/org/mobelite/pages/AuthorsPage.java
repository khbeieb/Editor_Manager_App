package org.mobelite.pages;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Condition.visible;

public class AuthorsPage extends BasePage {
  public void open() {
    openUrl("http://localhost:4200");
  }

  public void verifyPageLoaded() {
    $("[data-testid='authors-title']").shouldBe(visible);
  }
}
