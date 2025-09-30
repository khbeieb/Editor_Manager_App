package com.project.ui.cucumber.runners;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(
  key = PLUGIN_PROPERTY_NAME,
  value = "pretty, html:target/cucumber-html-report/cucumber.html, io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
)
@ConfigurationParameter(
  key = GLUE_PROPERTY_NAME,
  value = "com.project.ui.cucumber.steps,com.project.ui.cucumber.fixtures"
)
public class CucumberE2ERunnerTest {
}
