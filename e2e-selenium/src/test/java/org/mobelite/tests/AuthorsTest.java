package org.mobelite.tests;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.mobelite.pages.AuthorsPage;
import org.mobelite.utils.DriverManager;

public class AuthorsTest {

  AuthorsPage authorsPage;

  @BeforeClass
  public void setup() {
    DriverManager.setup();
    authorsPage = new AuthorsPage();
  }

  @Test(description = "Verify authors page loads correctly")
  public void testAuthorsPageLoad() {
    authorsPage.open();
    authorsPage.verifyPageLoaded();
  }
}
